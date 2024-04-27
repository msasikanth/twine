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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

internal fun LazyGridScope.pinnedSources(
  pinnedSources: LazyPagingItems<Source>,
  selectedSources: Set<Source>,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  gridItemSpan: GridItemSpan,
  onTogglePinnedSection: () -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
) {
  if (pinnedSources.itemCount > 0) {
    item(key = "PinnedFeedsHeader", span = { GridItemSpan(2) }) {
      PinnedFeedsHeader(
        isPinnedSectionExpanded = isPinnedSectionExpanded,
        onToggleSection = onTogglePinnedSection
      )
    }

    if (isPinnedSectionExpanded) {
      items(
        count = pinnedSources.itemCount,
        key = pinnedSources.itemKey { "PinnedSource:${it.id}" },
        contentType = {
          if (pinnedSources[it]?.sourceType == SourceType.FeedGroup) {
            "FeedGroupItem"
          } else {
            "FeedListItem"
          }
        },
        span = { gridItemSpan }
      ) { index ->
        val source = pinnedSources[index]
        if (source != null) {
          val startPadding = startPaddingOfSourceItem(gridItemSpan, index)
          val endPadding = endPaddingOfSourceItem(gridItemSpan, index)
          val topPadding = topPaddingOfSourceItem(gridItemSpan, index)
          val bottomPadding = bottomPaddingOfSourceItem(index, pinnedSources.itemCount)

          when (source) {
            is FeedGroup -> {
              FeedGroupItem(
                feedGroup = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                selected = selectedSources.any { it.id == source.id },
                onFeedGroupSelected = onToggleSourceSelection,
                onFeedGroupClick = onSourceClick,
                modifier =
                  Modifier.padding(
                    start = startPadding,
                    top = topPadding,
                    end = endPadding,
                    bottom = bottomPadding
                  ),
              )
            }
            is Feed -> {
              FeedListItem(
                feed = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                isFeedSelected = selectedSources.any { it.id == source.id },
                onFeedClick = onSourceClick,
                onFeedSelected = onToggleSourceSelection,
                modifier =
                  Modifier.padding(
                    start = startPadding,
                    top = topPadding,
                    end = endPadding,
                    bottom = bottomPadding
                  ),
              )
            }
          }
        }
      }
    }

    item(span = { GridItemSpan(2) }) {
      HorizontalDivider(
        modifier = Modifier.padding(top = 24.dp),
        color = AppTheme.colorScheme.tintedSurface
      )
    }
  }
}

@Composable
private fun PinnedFeedsHeader(
  isPinnedSectionExpanded: Boolean,
  modifier: Modifier = Modifier,
  onToggleSection: () -> Unit
) {
  Row(
    modifier =
      Modifier.padding(start = 32.dp, end = 20.dp).padding(vertical = 12.dp).then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = LocalStrings.current.pinnedFeeds,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    val icon =
      if (isPinnedSectionExpanded) {
        Icons.Filled.ExpandLess
      } else {
        Icons.Filled.ExpandMore
      }

    Spacer(Modifier.requiredWidth(12.dp))

    IconButton(onClick = onToggleSection) {
      Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
    }
  }
}
