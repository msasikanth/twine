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
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal const val DROPBOX_CLIENT_ID = "qtxdwxyzi69tuxp"

class RealOAuthManager(
  private val httpClient: HttpClient,
  private val tokenProvider: OAuthTokenProvider
) : OAuthManager {

  private val redirectUri = "twine://oauth"
  private var codeVerifier: String? = null

  override fun getAuthUrl(providerId: String): String {
    return when (providerId) {
      "dropbox" -> {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        URLBuilder("https://www.dropbox.com/oauth2/authorize")
          .apply {
            parameters.append("client_id", DROPBOX_CLIENT_ID)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", "files.content.read files.content.write")
            parameters.append("token_access_type", "offline")
            parameters.append("code_challenge", codeChallenge)
            parameters.append("code_challenge_method", "plain")
          }
          .buildString()
      }
      else -> ""
    }
  }

  private var pendingProviderId: String? = null

  override fun setPendingProvider(providerId: String) {
    pendingProviderId = providerId
  }

  override suspend fun handleRedirect(uri: String): String? {
    val url = Url(uri.replace("#", "?"))
    val code = url.parameters["code"]
    if (code != null && pendingProviderId != null && codeVerifier != null) {
      val providerId = pendingProviderId!!
      val verifier = codeVerifier!!
      try {
        val response: DropboxTokenResponse =
          httpClient
            .submitForm(
              url = "https://api.dropboxapi.com/oauth2/token",
              formParameters =
                Parameters.build {
                  append("code", code)
                  append("grant_type", "authorization_code")
                  append("client_id", DROPBOX_CLIENT_ID)
                  append("redirect_uri", redirectUri)
                  append("code_verifier", verifier)
                }
            )
            .body()

        tokenProvider.saveAccessToken(providerId, response.accessToken)
        if (response.refreshToken != null) {
          tokenProvider.saveRefreshToken(providerId, response.refreshToken)
        }
        pendingProviderId = null
        codeVerifier = null

        return providerId
      } catch (e: Exception) {
        Logger.e("AuthError", e)
      }
    }

    return null
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun generateCodeVerifier(): String {
    val bytes = Random.nextBytes(32)
    return Base64.UrlSafe.encode(bytes).trimEnd('=')
  }

  private fun generateCodeChallenge(verifier: String): String {
    return verifier
  }
}

@Serializable
internal data class DropboxTokenResponse(
  @SerialName("access_token") val accessToken: String,
  @SerialName("refresh_token") val refreshToken: String? = null,
)
