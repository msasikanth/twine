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
  }

  override suspend fun upload(fileName: String, data: String): Boolean {
    val token = tokenProvider.getAccessToken(id) ?: return false

    val response =
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

    if (response.status != HttpStatusCode.OK) {
      val errorBody = response.bodyAsText()
      Logger.e { "Dropbox upload failed for $fileName: ${response.status}. Body: $errorBody" }
    }

    return response.status == HttpStatusCode.OK
  }

  override suspend fun download(fileName: String): String? {
    val token = tokenProvider.getAccessToken(id) ?: return null

    val response =
      httpClient.post("https://content.dropboxapi.com/2/files/download") {
        header(HttpHeaders.Authorization, "Bearer $token")
        header("Dropbox-API-Arg", Json.encodeToString(buildJsonObject { put("path", fileName) }))
        setBody("")
      }

    return if (response.status == HttpStatusCode.OK) {
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
  }

  override suspend fun listFiles(prefix: String): List<String> {
    val token = tokenProvider.getAccessToken(id) ?: return emptyList()

    val response =
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

    if (response.status == HttpStatusCode.OK) {
      val body = response.body<JsonObject>()
      val entries = body["entries"]?.jsonArray ?: return emptyList()
      return entries
        .mapNotNull { it.jsonObject["path_display"]?.jsonPrimitive?.content }
        .filter { it.startsWith(prefix) }
    } else {
      val errorBody = response.bodyAsText()
      Logger.e { "Dropbox listFiles failed: ${response.status}. Body: $errorBody" }
      return emptyList()
    }
  }

  override suspend fun deleteFile(fileName: String): Boolean {
    val token = tokenProvider.getAccessToken(id) ?: return false

    val response =
      httpClient.post("https://api.dropboxapi.com/2/files/delete_v2") {
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(buildJsonObject { put("path", fileName) })
      }

    if (response.status != HttpStatusCode.OK) {
      val errorBody = response.bodyAsText()
      Logger.e { "Dropbox deleteFile failed for $fileName: ${response.status}. Body: $errorBody" }
    }

    return response.status == HttpStatusCode.OK
  }
}
