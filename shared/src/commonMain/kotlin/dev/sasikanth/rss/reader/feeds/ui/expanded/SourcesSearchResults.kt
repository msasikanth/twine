/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.feeds.ui.expanded

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem

internal fun LazyListScope.sourcesSearchResults(
  searchResults: LazyPagingItems<Feed>,
  selectedSources: Set<Source>,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
) {
  items(
    count = searchResults.itemCount,
    key = searchResults.itemKey { "SearchResult:${it.id}" },
    contentType = { "FeedListItem" },
  ) { index ->
    val feed = searchResults[index]
    val startPadding = 24.dp
    val endPadding = 24.dp
    val topPadding = 8.dp
    val bottomPadding = bottomPaddingOfSourceItem(index, searchResults.itemCount)

    if (feed != null) {
      FeedListItem(
        feed = feed,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        isInMultiSelectMode = isInMultiSelectMode,
        isFeedSelected = selectedSources.any { it.id == feed.id },
        onFeedClick = onSourceClick,
        onFeedSelected = onToggleSourceSelection,
        onOptionsClick = { onToggleSourceSelection(feed) },
        modifier =
          Modifier.padding(
            start = startPadding,
            top = topPadding,
            end = endPadding,
            bottom = bottomPadding
          )
      )
    }
  }
}
