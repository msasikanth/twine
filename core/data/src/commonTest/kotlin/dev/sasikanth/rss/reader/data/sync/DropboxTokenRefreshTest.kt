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

import dev.sasikanth.rss.reader.core.model.local.ServiceType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class DropboxTokenRefreshTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun should_refresh_token_when_401_occurs() = runTest {
    val tokenProvider = FakeTokenProvider()
    var requestCount = 0

    val mockEngine = MockEngine { request ->
      requestCount++
      when (request.url.toString()) {
        "https://api.dropboxapi.com/oauth2/token" -> {
          respond(
            content = """{"access_token": "new_token", "refresh_token": "new_refresh_token"}""",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
          )
        }
        "https://content.dropboxapi.com/2/files/download" -> {
          if (request.headers[HttpHeaders.Authorization] == "Bearer old_token") {
            respond(
              content =
                """{"error_summary": "expired_access_token/.", "error": {".tag": "expired_access_token"}}""",
              status = HttpStatusCode.Unauthorized,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
          } else if (request.headers[HttpHeaders.Authorization] == "Bearer new_token") {
            respond(content = "success_content", status = HttpStatusCode.OK)
          } else {
            respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
          }
        }
        else -> respond(content = "not found", status = HttpStatusCode.NotFound)
      }
    }

    val httpClient = HttpClient(mockEngine) { install(ContentNegotiation) { json(json) } }

    val provider =
      DropboxCloudServiceProvider(
        httpClient = httpClient,
        tokenProvider = tokenProvider,
        onSignOut = {}
      )
    val result = provider.download("test.json")

    assertEquals("success_content", result)
    assertEquals("new_token", tokenProvider.accessToken)
    assertEquals("new_refresh_token", tokenProvider.refreshToken)
    assertEquals(3, requestCount)
  }
}

private class FakeTokenProvider(
  var accessToken: String? = "old_token",
  var refreshToken: String? = "refresh_token"
) : OAuthTokenProvider {
  override fun isSignedIn(serviceType: ServiceType): Flow<Boolean> = flowOf(accessToken != null)

  override suspend fun isSignedInImmediate(serviceType: ServiceType): Boolean = accessToken != null

  override suspend fun getAccessToken(serviceType: ServiceType): String? = accessToken

  override suspend fun saveAccessToken(serviceType: ServiceType, token: String?) {
    accessToken = token
  }

  override suspend fun getRefreshToken(serviceType: ServiceType): String? = refreshToken

  override suspend fun saveRefreshToken(serviceType: ServiceType, token: String?) {
    refreshToken = token
  }
}
