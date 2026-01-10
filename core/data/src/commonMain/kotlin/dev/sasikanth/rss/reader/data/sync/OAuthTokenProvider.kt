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

import kotlinx.coroutines.flow.Flow

interface OAuthTokenProvider {
  fun isSignedIn(providerId: String): Flow<Boolean>

  suspend fun getAccessToken(providerId: String): String?

  suspend fun saveAccessToken(providerId: String, token: String?)

  suspend fun getRefreshToken(providerId: String): String?

  suspend fun saveRefreshToken(providerId: String, token: String?)
}
