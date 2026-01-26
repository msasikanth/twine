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
import dev.sasikanth.rss.reader.data.repository.UserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
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
  private val tokenProvider: OAuthTokenProvider,
  private val userRepository: UserRepository,
) : OAuthManager {

  private val redirectUri = "twine://oauth"
  private var codeVerifier: String? = null

  private var pendingServiceType: ServiceType? = null

  override fun getAuthUrl(serviceType: ServiceType): String {
    return when (serviceType) {
      CloudStorageProvider.DROPBOX -> {
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

  override fun setPendingProvider(serviceType: ServiceType) {
    pendingServiceType = serviceType
  }

  override suspend fun handleRedirect(uri: String): Boolean {
    val url = Url(uri.replace("#", "?"))
    val code = url.parameters["code"]
    if (code != null && pendingServiceType != null && codeVerifier != null) {
      val serviceType = pendingServiceType!!
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

        val userInfo: DropboxUserInfo =
          httpClient
            .post("https://api.dropboxapi.com/2/users/get_current_account") {
              header(HttpHeaders.Authorization, "Bearer ${response.accessToken}")
            }
            .body()

        userRepository.saveUser(
          id = userInfo.accountId,
          name = userInfo.name.displayName,
          email = userInfo.email,
          avatarUrl = userInfo.profilePhotoUrl,
          token = response.accessToken,
          refreshToken = response.refreshToken ?: ""
        )

        tokenProvider.saveAccessToken(serviceType, response.accessToken)
        if (response.refreshToken != null) {
          tokenProvider.saveRefreshToken(serviceType, response.refreshToken)
        }
        pendingServiceType = null
        codeVerifier = null

        return true
      } catch (e: Exception) {
        Logger.e("AuthError", e)
      }
    }

    return false
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

@Serializable
internal data class DropboxUserInfo(
  @SerialName("account_id") val accountId: String,
  val name: DropboxName,
  val email: String,
  @SerialName("profile_photo_url") val profilePhotoUrl: String? = null,
)

@Serializable
internal data class DropboxName(
  @SerialName("display_name") val displayName: String,
)
