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

import io.ktor.http.URLBuilder
import io.ktor.http.Url

class RealOAuthManager(private val tokenProvider: RealOAuthTokenProvider) : OAuthManager {
  // Use placeholders for client IDs. In a real app these would be in build config or secured.
  private val dropboxClientId = "qtxdwxyzi69tuxp"

  private val redirectUri = "twine://oauth"

  override fun getAuthUrl(providerId: String): String {
    return when (providerId) {
      "dropbox" -> {
        URLBuilder("https://www.dropbox.com/oauth2/authorize")
          .apply {
            parameters.append("client_id", dropboxClientId)
            parameters.append("redirect_uri", redirectUri)
            parameters.append("response_type", "token")
            parameters.append("scope", "files.content.read files.content.write")
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

  override suspend fun handleRedirect(uri: String) {
    val url = Url(uri.replace("#", "?"))
    val token = url.parameters["access_token"]
    if (token != null && pendingProviderId != null) {
      tokenProvider.saveAccessToken(pendingProviderId!!, token)
      pendingProviderId = null
    }
  }
}
