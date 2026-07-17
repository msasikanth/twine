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

package dev.sasikanth.rss.reader.refresh

import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.initializers.Initializer
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FeedsRefreshInitializer(
  private val refreshPolicy: RefreshPolicy,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: SyncCoordinator,
  private val dispatchersProvider: DispatchersProvider,
) : Initializer {

  companion object {
    private val CHECK_INTERVAL = 60.minutes
  }

  override fun initialize() {
    val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io)
    scope.launch {
      while (isActive) {
        delay(CHECK_INTERVAL)

        try {
          if (settingsRepository.enableAutoSync.first() && refreshPolicy.hasExpired()) {
            syncCoordinator.pull()
          }
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          // Skip this cycle; try again on the next tick
        }
      }
    }
  }
}
