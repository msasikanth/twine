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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

internal fun LazyGridScope.allSources(
  numberOfFeeds: Int,
  numberOfFeedGroups: Int,
  sources: LazyPagingItems<SourceListItem>,
  selectedSources: Set<Source>,
  feedsSortOrder: FeedsOrderBy,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  gridItemSpan: GridItemSpan,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit
) {
  if (sources.itemCount > 0) {
    item(key = "AllFeedsHeader", span = { GridItemSpan(2) }) {
      AllFeedsHeader(
        feedsCount = numberOfFeeds,
        feedsSortOrder = feedsSortOrder,
        onFeedsSortChanged = onFeedsSortChanged
      )
    }

    val feedGroupGridItemSpan = GridItemSpan(1)
    items(
      count = sources.itemCount,
      key =
        sources.itemKey {
          when (it) {
            SourceListItem.Separator -> "Separator"
            is SourceListItem.SourceItem -> it.source.id
          }
        },
      contentType = {
        when (val sourceItem = sources[it]) {
          SourceListItem.Separator -> "Separator"
          is SourceListItem.SourceItem -> {
            if (sourceItem.source.sourceType == SourceType.FeedGroup) {
              "FeedGroupItem"
            } else {
              "FeedListItem"
            }
          }
          null -> null
        }
      },
      span = {
        when (val sourceItem = sources[it]) {
          SourceListItem.Separator -> GridItemSpan(2)
          is SourceListItem.SourceItem -> {
            when (sourceItem.source) {
              is FeedGroup -> feedGroupGridItemSpan
              is Feed -> gridItemSpan
              else -> GridItemSpan(2)
            }
          }
          null -> GridItemSpan(2)
        }
      }
    ) { index ->
      when (val sourceItem = sources[index]) {
        SourceListItem.Separator -> {
          Spacer(Modifier.requiredHeight(40.dp))
        }
        is SourceListItem.SourceItem -> {
          when (val source = sourceItem.source) {
            is FeedGroup -> {
              val startPadding = startPaddingOfSourceItem(feedGroupGridItemSpan, index)
              val endPadding = endPaddingOfSourceItem(feedGroupGridItemSpan, index)
              val topPadding = topPaddingOfSourceItem(feedGroupGridItemSpan, index)
              val bottomPadding = bottomPaddingOfSourceItem(index, sources.itemCount)

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
              // When there are even number of feed groups, we are offsetting the index
              // to make sure the spacing is properly applied to feed list items after the
              // separator.
              val transformedIndex =
                if (numberOfFeedGroups % 2 == 0) {
                  index - 1
                } else {
                  index
                }
              val startPadding = startPaddingOfSourceItem(gridItemSpan, transformedIndex)
              val endPadding = endPaddingOfSourceItem(gridItemSpan, transformedIndex)
              val topPadding = topPaddingOfSourceItem(gridItemSpan, transformedIndex)
              val bottomPadding = bottomPaddingOfSourceItem(transformedIndex, sources.itemCount)

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
                  )
              )
            }
          }
        }
        null -> {
          // no-op
        }
      }
    }
  }
}

@Composable
private fun AllFeedsHeader(
  feedsCount: Int,
  feedsSortOrder: FeedsOrderBy,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier =
      Modifier.padding(start = 32.dp, end = 20.dp).padding(vertical = 12.dp).then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    var showSortDropdown by remember { mutableStateOf(false) }

    Text(
      text = LocalStrings.current.allFeeds,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    Spacer(Modifier.requiredWidth(8.dp))

    Text(
      modifier = Modifier.weight(1f),
      text = feedsCount.toString(),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.tintedForeground,
    )

    Spacer(Modifier.requiredWidth(12.dp))

    Box {
      TextButton(onClick = { showSortDropdown = true }, shape = MaterialTheme.shapes.large) {
        val orderText =
          when (feedsSortOrder) {
            FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
            FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
            FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
            FeedsOrderBy.Pinned -> {
              throw IllegalStateException(
                "Cannot use the following feed sort order here: $feedsSortOrder"
              )
            }
          }

        Text(
          text = orderText,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground
        )

        Spacer(Modifier.width(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = LocalStrings.current.editFeeds,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }

      DropdownMenu(
        modifier = Modifier.requiredWidth(132.dp),
        expanded = showSortDropdown,
        onDismissRequest = { showSortDropdown = false }
      ) {
        FeedsOrderBy.entries
          .filter { it != FeedsOrderBy.Pinned }
          .forEach { sortOrder ->
            val label =
              when (sortOrder) {
                FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
                FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
                FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
                FeedsOrderBy.Pinned -> {
                  throw IllegalStateException(
                    "Cannot use the following feed sort order here: $feedsSortOrder"
                  )
                }
              }

            val color =
              if (feedsSortOrder == sortOrder) {
                AppTheme.colorScheme.tintedSurface
              } else {
                Color.Unspecified
              }
            val labelColor =
              if (feedsSortOrder == sortOrder) {
                AppTheme.colorScheme.onSurface
              } else {
                AppTheme.colorScheme.textEmphasisHigh
              }

            DropdownMenuItem(
              modifier = Modifier.background(color),
              onClick = {
                onFeedsSortChanged(sortOrder)
                showSortDropdown = false
              },
              text = { Text(label, color = labelColor) }
            )
          }
      }
    }
  }
}
