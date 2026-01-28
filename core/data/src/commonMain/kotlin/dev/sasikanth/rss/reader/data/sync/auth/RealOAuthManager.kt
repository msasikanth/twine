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

package dev.sasikanth.rss.reader.data.sync.auth

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.UserRepository
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
  private val tokenProvider: OAuthTokenProvider,
  private val userRepository: UserRepository,
) : OAuthManager {

  private val redirectUri = "twine://oauth"
  private var codeVerifier: String? = null

  private var pendingServiceType: ServiceType? = null

  override fun getAuthUrl(serviceType: ServiceType): String {
    return when (serviceType) {
      ServiceType.DROPBOX -> {
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

        // Placeholder user data, replace if Dropbox approves getting user info via API
        userRepository.saveUser(
          id = "1",
          name = "Dropbox User",
          email = "user@dropbox",
          avatarUrl = "",
          token = response.accessToken,
          refreshToken = response.refreshToken ?: "",
          serviceType = serviceType,
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
internal data class DropboxName(
  @SerialName("display_name") val displayName: String,
)
