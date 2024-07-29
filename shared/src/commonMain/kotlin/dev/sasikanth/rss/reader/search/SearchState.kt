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
package dev.sasikanth.rss.reader.search

import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
internal data class SearchState(
  val searchResults: Flow<PagingData<PostWithMetadata>>,
  val searchInProgress: Boolean,
  val searchSortOrder: SearchSortOrder
) {

  companion object {
    val DEFAULT =
      SearchState(
        searchResults = emptyFlow(),
        searchInProgress = false,
        searchSortOrder = SearchSortOrder.Newest
      )
  }

  fun reset() =
    copy(
      searchResults = emptyFlow(),
      searchInProgress = false,
      searchSortOrder = SearchSortOrder.Newest
    )
}
