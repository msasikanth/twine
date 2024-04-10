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
package dev.sasikanth.rss.reader.feeds

import androidx.compose.runtime.Immutable
import androidx.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.ui.FeedsViewMode
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
internal data class FeedsState(
  val feedsInBottomBar: Flow<PagingData<Feed>>,
  val feedsInExpandedView: Flow<PagingData<Feed>>,
  val pinnedFeeds: Flow<PagingData<Feed>>,
  val feedsSearchResults: Flow<PagingData<Feed>>,
  val selectedFeed: Feed?,
  val canShowUnreadPostsCount: Boolean,
  val feedsViewMode: FeedsViewMode,
  val feedsSortOrder: FeedsOrderBy,
  val isPinnedSectionExpanded: Boolean,
  val selectedFeeds: Set<Feed>,
) {

  val isInMultiSelectMode: Boolean
    get() = selectedFeeds.isNotEmpty()

  companion object {

    val DEFAULT =
      FeedsState(
        feedsInBottomBar = emptyFlow(),
        feedsInExpandedView = emptyFlow(),
        pinnedFeeds = emptyFlow(),
        feedsSearchResults = emptyFlow(),
        selectedFeed = null,
        canShowUnreadPostsCount = false,
        feedsViewMode = FeedsViewMode.List,
        feedsSortOrder = FeedsOrderBy.Latest,
        isPinnedSectionExpanded = true,
        selectedFeeds = emptySet()
      )
  }
}
