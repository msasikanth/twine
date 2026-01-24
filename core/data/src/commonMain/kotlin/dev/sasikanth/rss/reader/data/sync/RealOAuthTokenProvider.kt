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

import dev.sasikanth.rss.reader.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RealOAuthTokenProvider(private val userRepository: UserRepository) : OAuthTokenProvider {

  override fun isSignedIn(serviceType: ServiceType): Flow<Boolean> {
    return when (serviceType) {
      CloudStorageService.DROPBOX -> userRepository.user().map { it != null }
      else -> kotlinx.coroutines.flow.flowOf(false)
    }
  }

  override suspend fun isSignedInImmediate(serviceType: ServiceType): Boolean {
    return when (serviceType) {
      CloudStorageService.DROPBOX -> userRepository.userBlocking() != null
      else -> false
    }
  }

  override suspend fun getAccessToken(serviceType: ServiceType): String? {
    return when (serviceType) {
      CloudStorageService.DROPBOX -> userRepository.userBlocking()?.token
      else -> null
    }
  }

  override suspend fun saveAccessToken(serviceType: ServiceType, token: String?) {
    when (serviceType) {
      CloudStorageService.DROPBOX -> {
        if (!(token.isNullOrBlank())) {
          userRepository.updateToken(token)
        }
      }
    }
  }

  override suspend fun getRefreshToken(serviceType: ServiceType): String? {
    return when (serviceType) {
      CloudStorageService.DROPBOX -> userRepository.userBlocking()?.refreshToken
      else -> null
    }
  }

  override suspend fun saveRefreshToken(serviceType: ServiceType, token: String?) {
    when (serviceType) {
      CloudStorageService.DROPBOX -> {
        if (!(token.isNullOrBlank())) {
          userRepository.updateRefreshToken(token)
        }
      }
    }
  }
}
