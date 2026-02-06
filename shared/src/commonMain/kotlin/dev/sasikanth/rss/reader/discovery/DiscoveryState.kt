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

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.input.TextFieldValue
import dev.sasikanth.rss.reader.core.model.DiscoveryGroup
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class DiscoveryState(
  val groups: ImmutableList<DiscoveryGroup>,
  val searchQuery: TextFieldValue,
  val isLoading: Boolean,
  val addedFeedLinks: Set<String>,
  val inProgressFeedLinks: Set<String>,
) {
  companion object {
    val DEFAULT =
      DiscoveryState(
        groups = persistentListOf(),
        searchQuery = TextFieldValue(),
        isLoading = false,
        addedFeedLinks = emptySet(),
        inProgressFeedLinks = emptySet(),
      )
  }

  val filteredGroups: List<DiscoveryGroup>
    get() {
      if (searchQuery.text.isBlank()) return groups

      return groups.mapNotNull { group ->
        val filteredFeeds =
          group.feeds.filter { feed ->
            feed.name.contains(searchQuery.text, ignoreCase = true) ||
              feed.description.contains(searchQuery.text, ignoreCase = true)
          }
        if (filteredFeeds.isNotEmpty()) {
          group.copy(feeds = filteredFeeds)
        } else {
          null
        }
      }
    }
}
