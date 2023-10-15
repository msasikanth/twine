/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

  private val browserTypeKey = stringPreferencesKey("pref_browser_type")
  private val enableFeaturedItemBlurKey = booleanPreferencesKey("pref_enable_blur")

  val browserType: Flow<BrowserType> =
    dataStore.data.map { preferences ->
      mapToBrowserType(preferences[browserTypeKey]) ?: BrowserType.Default
    }

  val enableFeaturedItemBlur: Flow<Boolean> =
    dataStore.data.map { preferences -> preferences[enableFeaturedItemBlurKey] ?: true }

  suspend fun updateBrowserType(browserType: BrowserType) {
    dataStore.edit { preferences -> preferences[browserTypeKey] = browserType.name }
  }

  suspend fun toggleFeaturedItemBlur(value: Boolean) {
    dataStore.edit { preferences -> preferences[enableFeaturedItemBlurKey] = value }
  }

  private fun mapToBrowserType(pref: String?): BrowserType? {
    if (pref.isNullOrBlank()) return null
    return BrowserType.valueOf(pref)
  }
}

enum class BrowserType {
  Default,
  InApp
}
