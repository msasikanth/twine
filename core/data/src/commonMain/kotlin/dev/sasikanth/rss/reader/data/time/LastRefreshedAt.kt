/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.data.time

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class LastRefreshedAt(private val dataStore: DataStore<Preferences>) {

  companion object {
    private val UPDATE_DURATION = 60.minutes
  }

  private val lastUpdatedAtKey = stringPreferencesKey("pref_last_updated_at")

  val dateTimeFlow: Flow<LocalDateTime> =
    dataStore.data.map { preferences ->
      val timeZone = TimeZone.currentSystemDefault()
      val instantString =
        preferences[lastUpdatedAtKey] ?: return@map Clock.System.now().toLocalDateTime(timeZone)
      val instant = Instant.parse(instantString)
      instant.toLocalDateTime(timeZone)
    }

  suspend fun refresh() {
    dataStore.edit { preferences -> preferences[lastUpdatedAtKey] = Clock.System.now().toString() }
  }

  suspend fun hasExpired(): Boolean {
    val lastUpdatedAt = fetchLastUpdatedAt() ?: return true
    val currentTime = Clock.System.now()
    val lastUpdateDuration = currentTime - lastUpdatedAt

    return lastUpdateDuration > UPDATE_DURATION
  }

  private suspend fun fetchLastUpdatedAt() =
    dataStore.data
      .map { preferences -> preferences[lastUpdatedAtKey] ?: return@map null }
      .first()
      ?.let { Instant.parse(it) }
}
