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

package dev.sasikanth.rss.reader.data.refreshpolicy

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.AppPlatform
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class RefreshPolicyAndSettingsFlowTest {

  private class MutableClock(var currentInstant: Instant) : Clock {
    override fun now(): Instant = currentInstant

    fun advanceBy(duration: Duration) {
      currentInstant += duration
    }
  }

  private class InMemoryPreferencesDataStore(initial: Preferences = emptyPreferences()) :
    DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state

    override suspend fun updateData(
      transform: suspend (t: Preferences) -> Preferences
    ): Preferences = state.updateAndGet { current -> transform(current) }
  }

  private val appInfo =
    AppInfo(
      versionCode = 1,
      versionName = "1.0.0",
      isDebugBuild = true,
      isFoss = false,
      isGoogleDriveSupported = false,
      cachePath = { "/tmp" },
      platform = AppPlatform.iOS,
    )

  @Test
  fun lastRefreshedAtFlowShouldNotReEmitWhenUnrelatedPreferencesChange() = runTest {
    val clock = MutableClock(Instant.parse("2026-11-15T12:00:00Z"))
    val dataStore = InMemoryPreferencesDataStore()
    val refreshPolicy = RefreshPolicy(dataStore = dataStore, clock = clock)
    val emissions = mutableListOf<LocalDateTime>()

    val job =
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        refreshPolicy.lastRefreshedAtFlow.toList(emissions)
      }

    assertEquals(1, emissions.size)

    val unrelatedKey = stringPreferencesKey("unrelated_key")
    clock.advanceBy(10.seconds)
    dataStore.edit { it[unrelatedKey] = "value-1" }
    clock.advanceBy(10.seconds)
    dataStore.edit { it[unrelatedKey] = "value-2" }

    assertEquals(1, emissions.size)
    job.cancel()
  }

  @Test
  fun lastRefreshedAtFlowShouldEmitFreshTimestampAfterClear() = runTest {
    val timeZone = TimeZone.currentSystemDefault()
    val launchInstant = Instant.parse("2026-11-15T12:00:00Z")
    val clock = MutableClock(launchInstant)
    val dataStore = InMemoryPreferencesDataStore()
    val refreshPolicy = RefreshPolicy(dataStore = dataStore, clock = clock)
    val emissions = mutableListOf<LocalDateTime>()

    val job =
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        refreshPolicy.lastRefreshedAtFlow.toList(emissions)
      }

    assertFalse(refreshPolicy.hasLastRefreshedAt())
    assertEquals(listOf(launchInstant.toLocalDateTime(timeZone)), emissions)

    val refreshedInstant = Instant.parse("2026-11-15T12:05:00Z")
    clock.currentInstant = refreshedInstant
    refreshPolicy.updateLastRefreshedAt()

    assertTrue(refreshPolicy.hasLastRefreshedAt())
    assertEquals(
      listOf(launchInstant.toLocalDateTime(timeZone), refreshedInstant.toLocalDateTime(timeZone)),
      emissions,
    )

    val unrelatedKey = stringPreferencesKey("unrelated_key")
    clock.advanceBy(15.seconds)
    dataStore.edit { it[unrelatedKey] = "after-refresh" }
    assertEquals(2, emissions.size)

    val clearedInstant = Instant.parse("2026-11-15T12:10:00Z")
    clock.currentInstant = clearedInstant
    refreshPolicy.clear()

    assertFalse(refreshPolicy.hasLastRefreshedAt())
    assertEquals(
      listOf(
        launchInstant.toLocalDateTime(timeZone),
        refreshedInstant.toLocalDateTime(timeZone),
        clearedInstant.toLocalDateTime(timeZone),
      ),
      emissions,
    )

    clock.advanceBy(15.seconds)
    dataStore.edit { it[unrelatedKey] = "after-clear" }
    assertEquals(3, emissions.size)

    job.cancel()
  }

  @Test
  fun settingsRepositoryFlowsShouldDeduplicateUnrelatedPreferenceEdits() = runTest {
    val dataStore = InMemoryPreferencesDataStore()
    val settingsRepository = SettingsRepository(dataStore = dataStore, appInfo = appInfo)
    val homeViewModeEmissions = mutableListOf<HomeViewMode>()
    val showUnreadCountEmissions = mutableListOf<Boolean>()

    val job1 =
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        settingsRepository.homeViewMode.toList(homeViewModeEmissions)
      }
    val job2 =
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        settingsRepository.showUnreadPostsCount.toList(showUnreadCountEmissions)
      }

    assertEquals(listOf(HomeViewMode.Default), homeViewModeEmissions)
    assertEquals(listOf(true), showUnreadCountEmissions)

    settingsRepository.updateReaderFontScaleFactor(1.2f)
    settingsRepository.toggleAmoled(true)
    settingsRepository.incrementUserSessionCount()

    assertEquals(1, homeViewModeEmissions.size)
    assertEquals(1, showUnreadCountEmissions.size)

    settingsRepository.updateHomeViewMode(HomeViewMode.Compact)
    assertEquals(listOf(HomeViewMode.Default, HomeViewMode.Compact), homeViewModeEmissions)
    assertEquals(1, showUnreadCountEmissions.size)

    job1.cancel()
    job2.cancel()
  }
}
