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

package dev.sasikanth.rss.reader.discovery

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.DiscoveryGroup
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.utils.currentMoment
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import twine.shared.generated.resources.Res

private const val DISCOVERY_FEEDS_URL =
  "https://raw.githubusercontent.com/msasikanth/twine/refs/heads/main/shared/src/commonMain/composeResources/files/discovery_feeds.json"

@Inject
@AppScope
class DiscoveryRepository(
  private val httpClient: HttpClient,
  private val settingsRepository: SettingsRepository,
) {

  private val json = Json { ignoreUnknownKeys = true }

  suspend fun groups(): List<DiscoveryGroup> {
    val (lastFetchTime, cache) = settingsRepository.discoveryFeedsCache.first()
    val isCacheExpired =
      lastFetchTime == null ||
        (Instant.currentMoment() - lastFetchTime) >= 1.days ||
        cache.isNullOrBlank()

    if (!isCacheExpired && cache.isNotBlank()) {
      return try {
        json.decodeFromString<List<DiscoveryGroup>>(cache)
      } catch (e: Exception) {
        Logger.e(e) { "Failed to decode discovery feeds from cache" }
        fetchFromRemoteAndCache()
      }
    }

    return fetchFromRemoteAndCache()
  }

  private suspend fun fetchFromRemoteAndCache(): List<DiscoveryGroup> {
    return try {
      val response = httpClient.get(DISCOVERY_FEEDS_URL)
      val content = response.bodyAsText()
      val groups = json.decodeFromString<List<DiscoveryGroup>>(content)
      settingsRepository.updateDiscoveryFeedsCache(content, Instant.currentMoment())
      groups
    } catch (e: Exception) {
      Logger.e(e) { "Failed to fetch discovery feeds from remote, falling back to cache or local" }
      val (_, cache) = settingsRepository.discoveryFeedsCache.first()
      if (!cache.isNullOrBlank()) {
        try {
          return json.decodeFromString<List<DiscoveryGroup>>(cache)
        } catch (e: Exception) {
          Logger.e(e) { "Failed to decode discovery feeds from cache during fallback" }
        }
      }

      val bytes = Res.readBytes("files/discovery_feeds.json")
      val content = bytes.decodeToString()
      json.decodeFromString<List<DiscoveryGroup>>(content)
    }
  }
}
