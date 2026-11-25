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
package dev.sasikanth.rss.reader.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.app.AppPlatform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class DebugSettingsRepositoryTest {

  private class InMemoryPreferencesDataStore(initial: Preferences = emptyPreferences()) :
    DataStore<Preferences> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<Preferences> = state

    override suspend fun updateData(
      transform: suspend (t: Preferences) -> Preferences
    ): Preferences = state.updateAndGet { current -> transform(current) }
  }

  private fun appInfo(isDebugBuild: Boolean) =
    AppInfo(
      versionCode = 1,
      versionName = "1.0.0",
      isDebugBuild = isDebugBuild,
      isFoss = false,
      isGoogleDriveSupported = false,
      cachePath = { "/tmp" },
      platform = AppPlatform.Android,
    )

  @Test
  fun debugBuildShouldEmitUpdatesAndDeduplicateUnrelatedPreferences() = runTest {
    val dataStore = InMemoryPreferencesDataStore()
    val repository =
      DebugSettingsRepository(dataStore = dataStore, appInfo = appInfo(isDebugBuild = true))

    val fpsEmissions = mutableListOf<Boolean>()
    val windowSizeEmissions = mutableListOf<Boolean>()
    val subscriptionEmissions = mutableListOf<DebugSubscriptionOverride>()
    val severityEmissions = mutableListOf<DebugLogSeverity>()

    val jobs =
      listOf(
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
          repository.showFpsOverlay.toList(fpsEmissions)
        },
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
          repository.showWindowSizeOverlay.toList(windowSizeEmissions)
        },
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
          repository.subscriptionOverride.toList(subscriptionEmissions)
        },
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
          repository.logSeverity.toList(severityEmissions)
        },
      )

    assertEquals(listOf(false), fpsEmissions)
    assertEquals(listOf(false), windowSizeEmissions)
    assertEquals(listOf(DebugSubscriptionOverride.Default), subscriptionEmissions)
    assertEquals(listOf(DebugLogSeverity.Info), severityEmissions)

    dataStore.edit { it[stringPreferencesKey("unrelated")] = "ignored" }
    assertEquals(1, fpsEmissions.size)
    assertEquals(1, windowSizeEmissions.size)
    assertEquals(1, subscriptionEmissions.size)
    assertEquals(1, severityEmissions.size)

    repository.toggleShowFpsOverlay(true)
    repository.toggleShowWindowSizeOverlay(true)
    repository.updateSubscriptionOverride(DebugSubscriptionOverride.ForceFree)
    repository.updateLogSeverity(DebugLogSeverity.Verbose)

    assertEquals(listOf(false, true), fpsEmissions)
    assertEquals(listOf(false, true), windowSizeEmissions)
    assertEquals(
      listOf(DebugSubscriptionOverride.Default, DebugSubscriptionOverride.ForceFree),
      subscriptionEmissions,
    )
    assertEquals(listOf(DebugLogSeverity.Info, DebugLogSeverity.Verbose), severityEmissions)
    assertEquals(DebugSubscriptionOverride.ForceFree, repository.subscriptionOverrideImmediate())
    assertEquals(DebugLogSeverity.Verbose, repository.logSeverityImmediate())

    jobs.forEach { it.cancel() }
  }

  @Test
  fun releaseBuildShouldAlwaysReturnSafeDefaultsAndIgnoreWrites() = runTest {
    val prePopulated =
      preferencesOf(
        booleanPreferencesKey("debug_show_fps_overlay") to true,
        booleanPreferencesKey("debug_show_window_size_overlay") to true,
        stringPreferencesKey("debug_subscription_override") to "ForcePro",
        stringPreferencesKey("debug_log_severity") to "Error",
      )
    val dataStore = InMemoryPreferencesDataStore(prePopulated)
    val repository =
      DebugSettingsRepository(dataStore = dataStore, appInfo = appInfo(isDebugBuild = false))

    assertFalse(repository.showFpsOverlay.first())
    assertFalse(repository.showWindowSizeOverlay.first())
    assertEquals(DebugSubscriptionOverride.Default, repository.subscriptionOverride.first())
    assertEquals(DebugLogSeverity.Info, repository.logSeverity.first())
    assertEquals(DebugSubscriptionOverride.Default, repository.subscriptionOverrideImmediate())
    assertEquals(DebugLogSeverity.Info, repository.logSeverityImmediate())

    repository.toggleShowFpsOverlay(false)
    repository.toggleShowWindowSizeOverlay(false)
    repository.updateSubscriptionOverride(DebugSubscriptionOverride.ForceFree)
    repository.updateLogSeverity(DebugLogSeverity.Verbose)

    val stored = dataStore.data.first()
    assertTrue(stored[booleanPreferencesKey("debug_show_fps_overlay")] == true)
    assertTrue(stored[booleanPreferencesKey("debug_show_window_size_overlay")] == true)
    assertEquals("ForcePro", stored[stringPreferencesKey("debug_subscription_override")])
    assertEquals("Error", stored[stringPreferencesKey("debug_log_severity")])
  }
}
