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
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FreshRssSyncProvider(
  private val userRepository: UserRepository,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
) : APIServiceProvider {

  override val cloudService: ServiceType = ServiceType.FRESH_RSS

  override val isPremium: Boolean = true

  override fun isSignedIn(): Flow<Boolean> {
    return userRepository.user().map {
      it != null && it.serverUrl != null && it.serviceType == ServiceType.FRESH_RSS
    }
  }

  override suspend fun isSignedInImmediate(): Boolean {
    val user = userRepository.currentUser()
    return user != null && user.serverUrl != null && user.serviceType == ServiceType.FRESH_RSS
  }

  override suspend fun signOut() {
    userRepository.deleteUser()
    rssRepository.deleteAllLocalData()
    settingsRepository.updateLastSyncedAt(Instant.DISTANT_PAST)
  }
}
