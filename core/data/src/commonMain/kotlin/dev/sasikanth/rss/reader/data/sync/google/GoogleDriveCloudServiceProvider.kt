/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.sasikanth.rss.reader.data.sync.google

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.sync.FileCloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.auth.GOOGLE_DRIVE_CLIENT_ID
import dev.sasikanth.rss.reader.data.sync.auth.GOOGLE_DRIVE_CLIENT_SECRET
import dev.sasikanth.rss.reader.data.sync.auth.GoogleTokenResponse
import dev.sasikanth.rss.reader.data.sync.auth.OAuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Google Drive sync provider that stores sync files in the app data folder (`appDataFolder` space),
 * which is hidden from the user's Drive and scoped to this app.
 */
class GoogleDriveCloudServiceProvider(
  private val httpClient: HttpClient,
  private val tokenProvider: OAuthTokenProvider,
  private val onSignOut: suspend () -> Unit,
) : FileCloudServiceProvider {

  override val cloudService = ServiceType.GOOGLE_DRIVE

  override val isPremium: Boolean = true

  override fun isSignedIn(): Flow<Boolean> = tokenProvider.isSignedIn(cloudService)

  override suspend fun isSignedInImmediate(): Boolean =
    tokenProvider.isSignedInImmediate(cloudService)

  override suspend fun signOut() {
    onSignOut()
  }

  private suspend fun refreshAccessToken(): String? {
    val refreshToken = tokenProvider.getRefreshToken(cloudService) ?: return null
    return try {
      val response: GoogleTokenResponse =
        httpClient
          .submitForm(
            url = "https://oauth2.googleapis.com/token",
            formParameters =
              Parameters.build {
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
                append("client_id", GOOGLE_DRIVE_CLIENT_ID)
                GOOGLE_DRIVE_CLIENT_SECRET?.let { append("client_secret", it) }
              },
          )
          .body()

      tokenProvider.saveAccessToken(cloudService, response.accessToken)
      if (response.refreshToken != null) {
        tokenProvider.saveRefreshToken(cloudService, response.refreshToken)
      }
      response.accessToken
    } catch (e: Exception) {
      Logger.e(e) { "Failed to refresh Google Drive access token" }
      null
    }
  }

  private suspend fun executeWithToken(block: suspend (String) -> HttpResponse): HttpResponse {
    val token = tokenProvider.getAccessToken(cloudService) ?: throw Exception("Not signed in")
    var response = block(token)

    if (response.status == HttpStatusCode.Unauthorized) {
      val newToken = refreshAccessToken()
      if (newToken != null) {
        response = block(newToken)
      }
    }
    return response
  }

  // Drive files are flat within the app data folder, so the leading "/" used by
  // other file providers is stripped from Drive file names and re-added on listing.
  private fun driveFileName(fileName: String): String = fileName.removePrefix("/")

  private suspend fun findFileId(fileName: String): String? {
    val response = executeWithToken { token ->
      httpClient.get("https://www.googleapis.com/drive/v3/files") {
        header(HttpHeaders.Authorization, "Bearer $token")
        parameter("spaces", "appDataFolder")
        parameter("q", "name = '${driveFileName(fileName)}' and trashed = false")
        parameter("fields", "files(id, name)")
      }
    }

    if (response.status != HttpStatusCode.OK) return null

    val body = response.body<JsonObject>()
    return body["files"]?.jsonArray?.firstOrNull()?.jsonObject?.get("id")?.jsonPrimitive?.content
  }

  override suspend fun upload(fileName: String, data: String): Boolean {
    return try {
      val existingFileId = findFileId(fileName)
      val response =
        if (existingFileId != null) {
          executeWithToken { token ->
            httpClient.patch("https://www.googleapis.com/upload/drive/v3/files/$existingFileId") {
              header(HttpHeaders.Authorization, "Bearer $token")
              parameter("uploadType", "media")
              contentType(ContentType.Application.Json)
              setBody(data)
            }
          }
        } else {
          val metadata =
            Json.encodeToString(
              buildJsonObject {
                put("name", driveFileName(fileName))
                putJsonArray("parents") { add("appDataFolder") }
              }
            )
          val boundary = "twine_drive_upload"
          val multipartBody = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata)
            append("\r\n--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
            append(data)
            append("\r\n--$boundary--")
          }

          executeWithToken { token ->
            httpClient.post("https://www.googleapis.com/upload/drive/v3/files") {
              header(HttpHeaders.Authorization, "Bearer $token")
              parameter("uploadType", "multipart")
              header(HttpHeaders.ContentType, "multipart/related; boundary=$boundary")
              setBody(multipartBody.encodeToByteArray())
            }
          }
        }

      if (!response.status.isSuccess()) {
        val errorBody = response.bodyAsText()
        Logger.e {
          "Google Drive upload failed for $fileName: ${response.status}. Body: $errorBody"
        }
      }

      response.status.isSuccess()
    } catch (e: Exception) {
      Logger.e(e) { "Google Drive upload failed for $fileName" }
      false
    }
  }

  override suspend fun download(fileName: String): String? {
    return try {
      val fileId = findFileId(fileName) ?: return null
      val response = executeWithToken { token ->
        httpClient.get("https://www.googleapis.com/drive/v3/files/$fileId") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("alt", "media")
        }
      }

      if (response.status == HttpStatusCode.OK) {
        response.bodyAsText()
      } else {
        if (response.status != HttpStatusCode.NotFound) {
          val errorBody = response.bodyAsText()
          Logger.e {
            "Google Drive download failed for $fileName: ${response.status}. Body: $errorBody"
          }
        }
        null
      }
    } catch (e: Exception) {
      Logger.e(e) { "Google Drive download failed for $fileName" }
      null
    }
  }

  override suspend fun listFiles(prefix: String): List<String> {
    return try {
      val response = executeWithToken { token ->
        httpClient.get("https://www.googleapis.com/drive/v3/files") {
          header(HttpHeaders.Authorization, "Bearer $token")
          parameter("spaces", "appDataFolder")
          parameter("q", "trashed = false")
          parameter("fields", "files(id, name)")
          parameter("pageSize", "1000")
        }
      }

      if (response.status == HttpStatusCode.OK) {
        val body = response.body<JsonObject>()
        val files = body["files"]?.jsonArray ?: return emptyList()
        files
          .mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
          .map { "/$it" }
          .filter { it.startsWith(prefix) }
      } else {
        val errorBody = response.bodyAsText()
        Logger.e { "Google Drive listFiles failed: ${response.status}. Body: $errorBody" }
        emptyList()
      }
    } catch (e: Exception) {
      Logger.e(e) { "Google Drive listFiles failed" }
      emptyList()
    }
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    return try {
      val fileId = findFileId(fileName) ?: return false
      val response = executeWithToken { token ->
        httpClient.delete("https://www.googleapis.com/drive/v3/files/$fileId") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }
      }

      if (!response.status.isSuccess()) {
        val errorBody = response.bodyAsText()
        Logger.e {
          "Google Drive deleteFile failed for $fileName: ${response.status}. Body: $errorBody"
        }
      }

      response.status.isSuccess()
    } catch (e: Exception) {
      Logger.e(e) { "Google Drive deleteFile failed for $fileName" }
      false
    }
  }
}
