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
import kotlinx.coroutines.flow.Flow

interface OAuthTokenProvider {
  fun isSignedIn(serviceType: ServiceType): Flow<Boolean>

  suspend fun isSignedInImmediate(serviceType: ServiceType): Boolean

  suspend fun getAccessToken(serviceType: ServiceType): String?

  suspend fun saveAccessToken(serviceType: ServiceType, token: String?)

  suspend fun getRefreshToken(serviceType: ServiceType): String?

  suspend fun saveRefreshToken(serviceType: ServiceType, token: String?)
}
