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
import androidx.paging.filter
import dev.sasikanth.rss.reader.feeds.ui.FeedsListItemType
import dev.sasikanth.rss.reader.models.local.Feed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

@Immutable
internal data class FeedsState(
  val feedsListItemTypes: Flow<PagingData<FeedsListItemType>>,
  val selectedFeed: Feed?,
  val numberOfPinnedFeeds: Long
) {

  val feedsOnly: Flow<PagingData<FeedsListItemType.FeedListItem>> =
    feedsListItemTypes
      .map { feeds ->
        feeds.filter { feedListItemType -> feedListItemType is FeedsListItemType.FeedListItem }
      }
      .filterIsInstance()

  val canPinFeeds: Boolean
    get() = numberOfPinnedFeeds < 10L

  companion object {

    val DEFAULT =
      FeedsState(feedsListItemTypes = emptyFlow(), selectedFeed = null, numberOfPinnedFeeds = 0)
  }
}
