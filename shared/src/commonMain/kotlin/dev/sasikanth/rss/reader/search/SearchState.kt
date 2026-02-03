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
package dev.sasikanth.rss.reader.search

import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
data class SearchState(
  val searchResults: Flow<PagingData<ResolvedPost>>,
  val searchInProgress: Boolean,
  val searchSortOrder: SearchSortOrder,
  val selectedSource: Source?,
  val onlyBookmarked: Boolean,
  val onlyUnread: Boolean,
) {

  companion object {
    val DEFAULT =
      SearchState(
        searchResults = emptyFlow(),
        searchInProgress = false,
        searchSortOrder = SearchSortOrder.Newest,
        selectedSource = null,
        onlyBookmarked = false,
        onlyUnread = false,
      )
  }

  fun reset() =
    copy(
      searchResults = emptyFlow(),
      searchInProgress = false,
      searchSortOrder = SearchSortOrder.Newest,
      selectedSource = null,
      onlyBookmarked = false,
      onlyUnread = false,
    )
}
