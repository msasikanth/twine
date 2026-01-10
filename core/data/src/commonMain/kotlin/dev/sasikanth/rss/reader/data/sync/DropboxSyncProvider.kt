/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.sync

import co.touchlab.kermit.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.submitForm
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.*

/**
 * Dropbox is a better alternative for cross-platform sync as it provides a consistent API across
 * all platforms and is easy to integrate with Ktor in commonMain.
 */
class DropboxSyncProvider(
  private val httpClient: HttpClient,
  private val tokenProvider: OAuthTokenProvider
) : CloudSyncProvider {
  override val id: String = "dropbox"
  override val name: String = "Dropbox"

  override fun isSignedIn(): Flow<Boolean> = tokenProvider.isSignedIn(id)

  override suspend fun signOut() {
    tokenProvider.saveAccessToken(id, null)
    tokenProvider.saveRefreshToken(id, null)
  }

  private suspend fun refreshAccessToken(): String? {
    val refreshToken = tokenProvider.getRefreshToken(id) ?: return null
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

      tokenProvider.saveAccessToken(id, response.accessToken)
      if (response.refreshToken != null) {
        tokenProvider.saveRefreshToken(id, response.refreshToken)
      }
      response.accessToken
    } catch (e: Exception) {
      Logger.e(e) { "Failed to refresh Dropbox access token" }
      null
    }
  }

  private suspend fun executeWithToken(block: suspend (String) -> HttpResponse): HttpResponse {
    val token = tokenProvider.getAccessToken(id) ?: throw Exception("Not signed in")
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
