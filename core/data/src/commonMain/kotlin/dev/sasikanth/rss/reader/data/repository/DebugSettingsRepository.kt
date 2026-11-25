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
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class DebugSettingsRepository(
  private val dataStore: DataStore<Preferences>,
  private val appInfo: AppInfo,
) {

  private val showFpsOverlayKey = booleanPreferencesKey("debug_show_fps_overlay")
  private val showWindowSizeOverlayKey = booleanPreferencesKey("debug_show_window_size_overlay")
  private val subscriptionOverrideKey = stringPreferencesKey("debug_subscription_override")
  private val logSeverityKey = stringPreferencesKey("debug_log_severity")

  val showFpsOverlay: Flow<Boolean> =
    if (!appInfo.isDebugBuild) {
      flowOf(false)
    } else {
      dataStore.data
        .map { preferences -> preferences[showFpsOverlayKey] ?: false }
        .distinctUntilChanged()
    }

  val showWindowSizeOverlay: Flow<Boolean> =
    if (!appInfo.isDebugBuild) {
      flowOf(false)
    } else {
      dataStore.data
        .map { preferences -> preferences[showWindowSizeOverlayKey] ?: false }
        .distinctUntilChanged()
    }

  val subscriptionOverride: Flow<DebugSubscriptionOverride> =
    if (!appInfo.isDebugBuild) {
      flowOf(DebugSubscriptionOverride.Default)
    } else {
      dataStore.data
        .map { preferences -> mapToSubscriptionOverride(preferences[subscriptionOverrideKey]) }
        .distinctUntilChanged()
    }

  val logSeverity: Flow<DebugLogSeverity> =
    if (!appInfo.isDebugBuild) {
      flowOf(DebugLogSeverity.Info)
    } else {
      dataStore.data
        .map { preferences -> mapToLogSeverity(preferences[logSeverityKey]) }
        .distinctUntilChanged()
    }

  suspend fun subscriptionOverrideImmediate(): DebugSubscriptionOverride {
    if (!appInfo.isDebugBuild) return DebugSubscriptionOverride.Default
    return subscriptionOverride.first()
  }

  suspend fun logSeverityImmediate(): DebugLogSeverity {
    if (!appInfo.isDebugBuild) return DebugLogSeverity.Info
    return logSeverity.first()
  }

  suspend fun toggleShowFpsOverlay(value: Boolean) {
    if (!appInfo.isDebugBuild) return
    dataStore.edit { preferences -> preferences[showFpsOverlayKey] = value }
  }

  suspend fun toggleShowWindowSizeOverlay(value: Boolean) {
    if (!appInfo.isDebugBuild) return
    dataStore.edit { preferences -> preferences[showWindowSizeOverlayKey] = value }
  }

  suspend fun updateSubscriptionOverride(value: DebugSubscriptionOverride) {
    if (!appInfo.isDebugBuild) return
    dataStore.edit { preferences -> preferences[subscriptionOverrideKey] = value.name }
  }

  suspend fun updateLogSeverity(value: DebugLogSeverity) {
    if (!appInfo.isDebugBuild) return
    dataStore.edit { preferences -> preferences[logSeverityKey] = value.name }
  }

  private fun mapToSubscriptionOverride(pref: String?): DebugSubscriptionOverride {
    if (pref.isNullOrBlank()) return DebugSubscriptionOverride.Default
    return runCatching { DebugSubscriptionOverride.valueOf(pref) }
      .getOrDefault(DebugSubscriptionOverride.Default)
  }

  private fun mapToLogSeverity(pref: String?): DebugLogSeverity {
    if (pref.isNullOrBlank()) return DebugLogSeverity.Info
    return runCatching { DebugLogSeverity.valueOf(pref) }.getOrDefault(DebugLogSeverity.Info)
  }
}

enum class DebugSubscriptionOverride {
  Default,
  ForcePro,
  ForceFree,
}

enum class DebugLogSeverity {
  Verbose,
  Debug,
  Info,
  Warn,
  Error,
}
