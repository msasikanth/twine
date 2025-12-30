/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.feeds.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.data.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.feeds.ui.sheet.expanded.bottomPaddingOfSourceItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.allFeeds
import twine.shared.generated.resources.editFeeds
import twine.shared.generated.resources.feedsSortAlphabetical
import twine.shared.generated.resources.feedsSortLatest
import twine.shared.generated.resources.feedsSortOldest

internal fun LazyListScope.allSources(
  numberOfFeeds: Int,
  numberOfFeedGroups: Int,
  sources: LazyPagingItems<SourceListItem>,
  selectedSources: Set<Source>,
  feedsSortOrder: FeedsOrderBy,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit
) {
  if (sources.itemCount > 0) {
    item(key = "AllFeedsHeader") {
      AllFeedsHeader(
        modifier = Modifier.animateItem(),
        feedsCount = numberOfFeeds,
        feedsSortOrder = feedsSortOrder,
        onFeedsSortChanged = onFeedsSortChanged
      )
    }

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
    ) { index ->
      when (val sourceItem = sources[index]) {
        SourceListItem.Separator -> {
          Spacer(Modifier.requiredHeight(8.dp))
        }
        is SourceListItem.SourceItem -> {
          when (val source = sourceItem.source) {
            is FeedGroup -> {
              val startPadding = 24.dp
              val endPadding = 24.dp
              val topPadding = 4.dp
              val bottomPadding = bottomPaddingOfSourceItem(index, sources.itemCount)

              FeedGroupItem(
                feedGroup = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                selected = selectedSources.any { it.id == source.id },
                onFeedGroupSelected = onToggleSourceSelection,
                onFeedGroupClick = onSourceClick,
                onOptionsClick = { onToggleSourceSelection(source) },
                modifier =
                  Modifier.padding(
                      start = startPadding,
                      top = topPadding,
                      end = endPadding,
                      bottom = bottomPadding
                    )
                    .animateItem(),
              )
            }
            is Feed -> {
              // When there are even number of feed groups, we are offsetting the index
              // to make sure the spacing is properly applied to feed list items after the
              // separator.
              val transformedIndex =
                if (numberOfFeedGroups % 2 == 0 && numberOfFeedGroups > 0) {
                  index - 1
                } else {
                  index
                }
              val startPadding = 24.dp
              val endPadding = 24.dp
              val topPadding = 4.dp
              val bottomPadding = bottomPaddingOfSourceItem(transformedIndex, sources.itemCount)

              FeedListItem(
                feed = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                isFeedSelected = selectedSources.any { it.id == source.id },
                onFeedClick = onSourceClick,
                onFeedSelected = onToggleSourceSelection,
                onOptionsClick = { onToggleSourceSelection(source) },
                modifier =
                  Modifier.padding(
                      start = startPadding,
                      top = topPadding,
                      end = endPadding,
                      bottom = bottomPadding
                    )
                    .animateItem()
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
internal fun AllFeedsHeader(
  feedsCount: Int,
  feedsSortOrder: FeedsOrderBy,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier =
      Modifier.then(modifier).padding(start = 32.dp, end = 20.dp).padding(vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    var showSortDropdown by remember { mutableStateOf(false) }
    val allFeedsLabel = stringResource(Res.string.allFeeds)

    Row(
      modifier =
        Modifier.weight(1f).clearAndSetSemantics {
          contentDescription = "${allFeedsLabel}: ${feedsCount}"
        }
    ) {
      Text(
        text = allFeedsLabel,
        style = MaterialTheme.typography.titleLarge,
        color = AppTheme.colorScheme.textEmphasisHigh,
      )

      Spacer(Modifier.requiredWidth(8.dp))

      Text(
        text = feedsCount.toString(),
        style = MaterialTheme.typography.titleLarge,
        color = AppTheme.colorScheme.primary,
      )
    }

    Spacer(Modifier.requiredWidth(12.dp))

    Box {
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

      TextButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
        onClick = { showSortDropdown = true },
        shape = MaterialTheme.shapes.large
      ) {
        val orderText =
          when (feedsSortOrder) {
            FeedsOrderBy.Latest -> stringResource(Res.string.feedsSortLatest)
            FeedsOrderBy.Oldest -> stringResource(Res.string.feedsSortOldest)
            FeedsOrderBy.Alphabetical -> stringResource(Res.string.feedsSortAlphabetical)
            FeedsOrderBy.Pinned -> {
              throw IllegalStateException(
                "Cannot use the following feed sort order here: $feedsSortOrder"
              )
            }
          }

        Text(
          text = orderText,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.primary
        )

        Spacer(Modifier.width(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = stringResource(Res.string.editFeeds),
          tint = AppTheme.colorScheme.primary
        )
      }

      DropdownMenu(
        modifier = Modifier.widthIn(min = 132.dp),
        expanded = showSortDropdown,
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
        onDismissRequest = { showSortDropdown = false }
      ) {
        val translucentStyle = LocalTranslucentStyles.current

        FeedsOrderBy.entries
          .filter { it != FeedsOrderBy.Pinned }
          .forEach { sortOrder ->
            val label =
              when (sortOrder) {
                FeedsOrderBy.Latest -> stringResource(Res.string.feedsSortLatest)
                FeedsOrderBy.Oldest -> stringResource(Res.string.feedsSortOldest)
                FeedsOrderBy.Alphabetical -> stringResource(Res.string.feedsSortAlphabetical)
                FeedsOrderBy.Pinned -> {
                  throw IllegalStateException(
                    "Cannot use the following feed sort order here: $feedsSortOrder"
                  )
                }
              }

            val color =
              if (feedsSortOrder == sortOrder) {
                translucentStyle.default.background
              } else {
                Color.Unspecified
              }
            val labelColor =
              if (feedsSortOrder == sortOrder) {
                AppTheme.colorScheme.secondary
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
