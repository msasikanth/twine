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

import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class RealOAuthTokenProvider(private val userRepository: UserRepository) : OAuthTokenProvider {

  override fun isSignedIn(serviceType: ServiceType): Flow<Boolean> {
    return when (serviceType) {
      ServiceType.DROPBOX -> userRepository.user().map { it != null && it.serverUrl == null }
      else -> flowOf(false)
    }
  }

  override suspend fun isSignedInImmediate(serviceType: ServiceType): Boolean {
    return when (serviceType) {
      ServiceType.DROPBOX -> {
        val user = userRepository.currentUser()
        user != null && user.serverUrl == null
      }
      else -> false
    }
  }

  override suspend fun getAccessToken(serviceType: ServiceType): String? {
    return when (serviceType) {
      ServiceType.DROPBOX -> userRepository.currentUser()?.token
      else -> null
    }
  }

  override suspend fun saveAccessToken(serviceType: ServiceType, token: String?) {
    when (serviceType) {
      ServiceType.DROPBOX -> {
        if (!(token.isNullOrBlank())) {
          userRepository.updateToken(token)
        }
      }
      else -> {
        throw IllegalStateException("Access token is not supported for $serviceType")
      }
    }
  }

  override suspend fun getRefreshToken(serviceType: ServiceType): String? {
    return when (serviceType) {
      ServiceType.DROPBOX -> userRepository.currentUser()?.refreshToken
      else -> null
    }
  }

  override suspend fun saveRefreshToken(serviceType: ServiceType, token: String?) {
    when (serviceType) {
      ServiceType.DROPBOX -> {
        if (!(token.isNullOrBlank())) {
          userRepository.updateRefreshToken(token)
        }
      }
      else -> {
        throw IllegalStateException("Refresh token is not supported for $serviceType")
      }
    }
  }
}
