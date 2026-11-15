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
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class RefreshPolicy(private val dataStore: DataStore<Preferences>) {

  internal constructor(
    dataStore: DataStore<Preferences>,
    clock: Clock,
  ) : this(dataStore = dataStore) {
    this.clock = clock
  }

  companion object Companion {
    private val UPDATE_DURATION = 60.minutes
  }

  private var clock: Clock = Clock.System
  private val lastRefreshedAtKey = stringPreferencesKey("pref_last_updated_at")
  private val lastSyncedAtKey = stringPreferencesKey("pref_last_sync_time")

  val lastRefreshedAtFlow: Flow<LocalDateTime> =
    dataStore.data
      .map { preferences -> preferences[lastRefreshedAtKey] }
      .distinctUntilChanged()
      .map { timestamp ->
        val instant = if (timestamp != null) Instant.parse(timestamp) else clock.now()
        instant.toLocalDateTime(TimeZone.currentSystemDefault())
      }
      .distinctUntilChanged()

  val lastSyncedAtFlow: Flow<Instant?> =
    dataStore.data
      .map { preferences -> preferences[lastSyncedAtKey]?.let { Instant.parse(it) } }
      .distinctUntilChanged()

  suspend fun updateLastRefreshedAt() {
    dataStore.edit { preferences -> preferences[lastRefreshedAtKey] = clock.now().toString() }
  }

  suspend fun updateLastSyncedAt() {
    dataStore.edit { preferences -> preferences[lastSyncedAtKey] = clock.now().toString() }
  }

  suspend fun hasLastRefreshedAt(): Boolean =
    dataStore.data.map { preferences -> preferences[lastRefreshedAtKey] != null }.first()

  suspend fun hasExpired(): Boolean {
    val lastSyncedAt = fetchLastSyncedAt() ?: return true
    val currentTime = clock.now()
    val lastSyncDuration = currentTime - lastSyncedAt

    return lastSyncDuration > UPDATE_DURATION
  }

  suspend fun fetchLastRefreshedAt(): Instant? =
    dataStore.data
      .map { preferences -> preferences[lastRefreshedAtKey] ?: return@map null }
      .first()
      ?.let { Instant.parse(it) }

  suspend fun fetchLastSyncedAt(): Instant? =
    dataStore.data
      .map { preferences -> preferences[lastSyncedAtKey] ?: return@map null }
      .first()
      ?.let { Instant.parse(it) }

  suspend fun clear() {
    dataStore.edit { preferences ->
      preferences.remove(lastRefreshedAtKey)
      preferences.remove(lastSyncedAtKey)
    }
  }
}
