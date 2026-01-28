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

package dev.sasikanth.rss.reader.data.sync.dropbox

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.sync.FileCloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.auth.DROPBOX_CLIENT_ID
import dev.sasikanth.rss.reader.data.sync.auth.DropboxTokenResponse
import dev.sasikanth.rss.reader.data.sync.auth.OAuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Dropbox is a better alternative for cross-platform sync as it provides a consistent API across
 * all platforms and is easy to integrate with Ktor in commonMain.
 */
class DropboxCloudServiceProvider(
  private val httpClient: HttpClient,
  private val tokenProvider: OAuthTokenProvider,
  private val onSignOut: suspend () -> Unit,
) : FileCloudServiceProvider {

  override val cloudService = ServiceType.DROPBOX

  override fun isSignedIn(): Flow<Boolean> = tokenProvider.isSignedIn(cloudService)

  override suspend fun isSignedInImmediate(): Boolean =
    tokenProvider.isSignedInImmediate(cloudService)

  override suspend fun signOut() {
    onSignOut()
  }

  private suspend fun refreshAccessToken(): String? {
    val refreshToken = tokenProvider.getRefreshToken(cloudService) ?: return null
    return try {
      val response: DropboxTokenResponse =
        httpClient
          .submitForm(
            url = "https://api.dropboxapi.com/oauth2/token",
            formParameters =
              Parameters.build {
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
                append("client_id", DROPBOX_CLIENT_ID)
              }
          )
          .body()

      tokenProvider.saveAccessToken(cloudService, response.accessToken)
      if (response.refreshToken != null) {
        tokenProvider.saveRefreshToken(cloudService, response.refreshToken)
      }
      response.accessToken
    } catch (e: Exception) {
      Logger.e(e) { "Failed to refresh Dropbox access token" }
      null
    }
  }

  private suspend fun executeWithToken(block: suspend (String) -> HttpResponse): HttpResponse {
    val token = tokenProvider.getAccessToken(cloudService) ?: throw Exception("Not signed in")
    var response = block(token)

    if (response.status == HttpStatusCode.Unauthorized) {
      val errorBody = response.bodyAsText()
      if (errorBody.contains("expired_access_token")) {
        val newToken = refreshAccessToken()
        if (newToken != null) {
          response = block(newToken)
        }
      }
    }
    return response
  }

  override suspend fun upload(fileName: String, data: String): Boolean {
    return try {
      val response = executeWithToken { token ->
        httpClient.post("https://content.dropboxapi.com/2/files/upload") {
          header(HttpHeaders.Authorization, "Bearer $token")
          header(
            "Dropbox-API-Arg",
            Json.encodeToString(
              buildJsonObject {
                put("path", fileName)
                put("mode", "overwrite")
                put("mute", true)
              }
            )
          )
          contentType(ContentType.Application.OctetStream)
          setBody(data.encodeToByteArray())
        }
      }

      if (response.status != HttpStatusCode.OK) {
        val errorBody = response.bodyAsText()
        Logger.e { "Dropbox upload failed for $fileName: ${response.status}. Body: $errorBody" }
      }

      response.status == HttpStatusCode.OK
    } catch (e: Exception) {
      Logger.e(e) { "Dropbox upload failed for $fileName" }
      false
    }
  }

  override suspend fun download(fileName: String): String? {
    return try {
      val response = executeWithToken { token ->
        httpClient.post("https://content.dropboxapi.com/2/files/download") {
          header(HttpHeaders.Authorization, "Bearer $token")
          header("Dropbox-API-Arg", Json.encodeToString(buildJsonObject { put("path", fileName) }))
          setBody("")
        }
      }

      if (response.status == HttpStatusCode.OK) {
        response.bodyAsText()
      } else {
        val errorBody = response.bodyAsText()
        val isFileNotFound =
          response.status == HttpStatusCode.Conflict && errorBody.contains("path/not_found")
        if (!isFileNotFound) {
          Logger.e { "Dropbox download failed for $fileName: ${response.status}. Body: $errorBody" }
        }
        null
      }
    } catch (e: Exception) {
      Logger.e(e) { "Dropbox download failed for $fileName" }
      null
    }
  }

  override suspend fun listFiles(prefix: String): List<String> {
    return try {
      val response = executeWithToken { token ->
        httpClient.post("https://api.dropboxapi.com/2/files/list_folder") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(
            buildJsonObject {
              put("path", "")
              put("recursive", false)
              put("include_media_info", false)
              put("include_deleted", false)
              put("include_has_explicit_shared_members", false)
              put("include_mounted_folders", true)
            }
          )
        }
      }

      if (response.status == HttpStatusCode.OK) {
        val body = response.body<JsonObject>()
        val entries = body["entries"]?.jsonArray ?: return emptyList()
        entries
          .mapNotNull { it.jsonObject["path_display"]?.jsonPrimitive?.content }
          .filter { it.startsWith(prefix) }
      } else {
        val errorBody = response.bodyAsText()
        Logger.e { "Dropbox listFiles failed: ${response.status}. Body: $errorBody" }
        emptyList()
      }
    } catch (e: Exception) {
      Logger.e(e) { "Dropbox listFiles failed" }
      emptyList()
    }
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    return try {
      val response = executeWithToken { token ->
        httpClient.post("https://api.dropboxapi.com/2/files/delete_v2") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(buildJsonObject { put("path", fileName) })
        }
      }

      if (response.status != HttpStatusCode.OK) {
        val errorBody = response.bodyAsText()
        Logger.e { "Dropbox deleteFile failed for $fileName: ${response.status}. Body: $errorBody" }
      }

      response.status == HttpStatusCode.OK
    } catch (e: Exception) {
      Logger.e(e) { "Dropbox deleteFile failed for $fileName" }
      false
    }
  }
}
