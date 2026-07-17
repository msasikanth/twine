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
import dev.sasikanth.rss.reader.data.utils.generateSecureRandomBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okio.ByteString.Companion.encodeUtf8

internal const val DROPBOX_CLIENT_ID = "qtxdwxyzi69tuxp"

internal const val TWINE_REDIRECT_URI = "twine://oauth"

// Google requires one OAuth client per platform, so each platform supplies its own client ID
internal expect val GOOGLE_DRIVE_CLIENT_ID: String

// Google "Desktop app" OAuth clients require their client secret (which Google treats as
// non-confidential for installed apps) during token exchange; iOS/Android clients have none.
internal expect val GOOGLE_DRIVE_CLIENT_SECRET: String?

// Google only accepts the reversed-client-ID custom scheme as the redirect for
// native app clients, unlike Dropbox which allows arbitrary schemes.
internal val GOOGLE_DRIVE_REDIRECT_URI: String
  get() =
    "com.googleusercontent.apps." +
      GOOGLE_DRIVE_CLIENT_ID.removeSuffix(".apps.googleusercontent.com") +
      ":/oauth2redirect"

class RealOAuthManager(
  private val httpClient: HttpClient,
  private val tokenProvider: OAuthTokenProvider,
  private val userRepository: UserRepository,
  private val signOutFromService: suspend (ServiceType) -> Unit,
) : OAuthManager {

  private val redirectServer = OAuthRedirectServer()
  private var activeRedirectUri: String? = null
  private var codeVerifier: String? = null

  private var pendingServiceType: ServiceType? = null

  private val _loopbackRedirects = MutableSharedFlow<String>(extraBufferCapacity = 1)
  override val loopbackRedirects: SharedFlow<String> = _loopbackRedirects

  override fun getAuthUrl(serviceType: ServiceType): String {
    if (serviceType != ServiceType.DROPBOX && serviceType != ServiceType.GOOGLE_DRIVE) {
      return ""
    }

    val loopbackRedirectUri = redirectServer.start { uri -> _loopbackRedirects.tryEmit(uri) }

    return when (serviceType) {
      ServiceType.DROPBOX -> {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        val redirectUri = loopbackRedirectUri ?: TWINE_REDIRECT_URI
        activeRedirectUri = redirectUri
        URLBuilder("https://www.dropbox.com/oauth2/authorize")
          .apply {
            parameters.append("client_id", DROPBOX_CLIENT_ID)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", "files.content.read files.content.write")
            parameters.append("token_access_type", "offline")
            parameters.append("code_challenge", codeChallenge)
            parameters.append("code_challenge_method", "S256")
          }
          .buildString()
      }
      ServiceType.GOOGLE_DRIVE -> {
        codeVerifier = generateCodeVerifier()
        val codeChallenge = generateCodeChallenge(codeVerifier!!)
        val redirectUri = loopbackRedirectUri ?: GOOGLE_DRIVE_REDIRECT_URI
        activeRedirectUri = redirectUri
        URLBuilder("https://accounts.google.com/o/oauth2/v2/auth")
          .apply {
            parameters.append("client_id", GOOGLE_DRIVE_CLIENT_ID)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "code")
            parameters.append("scope", "https://www.googleapis.com/auth/drive.appdata")
            parameters.append("access_type", "offline")
            parameters.append("prompt", "consent")
            parameters.append("code_challenge", codeChallenge)
            parameters.append("code_challenge_method", "S256")
          }
          .buildString()
      }
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
        val (accessToken, refreshToken) =
          when (serviceType) {
            ServiceType.GOOGLE_DRIVE -> {
              val response: GoogleTokenResponse =
                httpClient
                  .submitForm(
                    url = "https://oauth2.googleapis.com/token",
                    formParameters =
                      Parameters.build {
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("client_id", GOOGLE_DRIVE_CLIENT_ID)
                        GOOGLE_DRIVE_CLIENT_SECRET?.let { append("client_secret", it) }
                        append("redirect_uri", activeRedirectUri ?: GOOGLE_DRIVE_REDIRECT_URI)
                        append("code_verifier", verifier)
                      },
                  )
                  .body()
              response.accessToken to response.refreshToken
            }
            else -> {
              val response: DropboxTokenResponse =
                httpClient
                  .submitForm(
                    url = "https://api.dropboxapi.com/oauth2/token",
                    formParameters =
                      Parameters.build {
                        append("code", code)
                        append("grant_type", "authorization_code")
                        append("client_id", DROPBOX_CLIENT_ID)
                        append("redirect_uri", activeRedirectUri ?: TWINE_REDIRECT_URI)
                        append("code_verifier", verifier)
                      },
                  )
                  .body()
              response.accessToken to response.refreshToken
            }
          }

        val previousServiceType = userRepository.currentUser()?.serviceType
        if (previousServiceType != null && previousServiceType != serviceType) {
          signOutFromService(previousServiceType)
        }

        // Placeholder user data, replace if providers approve getting user info via API
        val (placeholderName, placeholderEmail) =
          when (serviceType) {
            ServiceType.GOOGLE_DRIVE -> "Google Drive User" to "user@googledrive"
            else -> "Dropbox User" to "user@dropbox"
          }
        userRepository.saveUser(
          id = "1",
          name = placeholderName,
          email = placeholderEmail,
          avatarUrl = "",
          token = accessToken,
          refreshToken = refreshToken ?: "",
          serviceType = serviceType,
        )

        tokenProvider.saveAccessToken(serviceType, accessToken)
        if (refreshToken != null) {
          tokenProvider.saveRefreshToken(serviceType, refreshToken)
        }
        pendingServiceType = null
        codeVerifier = null
        activeRedirectUri = null

        return true
      } catch (e: Exception) {
        Logger.e("AuthError", e)
      }
    }

    return false
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun generateCodeVerifier(): String {
    val bytes = generateSecureRandomBytes(32)
    return Base64.UrlSafe.encode(bytes).trimEnd('=')
  }

  @OptIn(ExperimentalEncodingApi::class)
  private fun generateCodeChallenge(verifier: String): String {
    val hash = verifier.encodeUtf8().sha256().toByteArray()
    return Base64.UrlSafe.encode(hash).trimEnd('=')
  }
}

@Serializable
internal data class DropboxTokenResponse(
  @SerialName("access_token") val accessToken: String,
  @SerialName("refresh_token") val refreshToken: String? = null,
)

@Serializable internal data class DropboxName(@SerialName("display_name") val displayName: String)

@Serializable
internal data class GoogleTokenResponse(
  @SerialName("access_token") val accessToken: String,
  @SerialName("refresh_token") val refreshToken: String? = null,
)
