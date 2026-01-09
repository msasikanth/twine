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

import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RealOAuthTokenProvider(private val settingsRepository: SettingsRepository) :
  OAuthTokenProvider {

  override fun isSignedIn(providerId: String): Flow<Boolean> {
    return when (providerId) {
      "dropbox" -> settingsRepository.dropboxAccessToken.map { it != null }
      else -> kotlinx.coroutines.flow.flowOf(false)
    }
  }

  override suspend fun getAccessToken(providerId: String): String? {
    return when (providerId) {
      "dropbox" -> settingsRepository.dropboxAccessToken.first()
      else -> null
    }
  }

  override suspend fun saveAccessToken(providerId: String, token: String?) {
    when (providerId) {
      "dropbox" -> settingsRepository.updateDropboxAccessToken(token)
    }
  }
}
