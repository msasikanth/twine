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
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import twine.shared.generated.resources.Res

private const val DISCOVERY_FEEDS_URL =
  "https://raw.githubusercontent.com/msasikanth/twine/refs/heads/main/shared/src/commonMain/composeResources/files/discovery_feeds.json"

@Inject
@AppScope
class DiscoveryRepository(private val httpClient: HttpClient) {

  private val json = Json { ignoreUnknownKeys = true }

  suspend fun groups(): List<DiscoveryGroup> {
    return try {
      val response = httpClient.get(DISCOVERY_FEEDS_URL)
      val content = response.bodyAsText()
      json.decodeFromString<List<DiscoveryGroup>>(content)
    } catch (e: Exception) {
      Logger.e(e) { "Failed to fetch discovery feeds from remote, falling back to local" }
      val bytes = Res.readBytes("files/discovery_feeds.json")
      val content = bytes.decodeToString()
      json.decodeFromString<List<DiscoveryGroup>>(content)
    }
  }
}
