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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupItem
import dev.sasikanth.rss.reader.feeds.ui.FeedListItem
import dev.sasikanth.rss.reader.feeds.ui.sheet.expanded.bottomPaddingOfSourceItem
import dev.sasikanth.rss.reader.resources.icons.DragIndicator
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.ReorderableLazyListState
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.pinnedFeeds

internal fun LazyListScope.pinnedSources(
  reorderableLazyListState: ReorderableLazyListState,
  pinnedSources: List<Source>,
  selectedSources: Set<Source>,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  onTogglePinnedSection: () -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
) {
  if (pinnedSources.isNotEmpty()) {
    stickyHeader(key = "PinnedFeedsHeader") {
      PinnedFeedsHeader(
        modifier = Modifier.animateItem(),
        isPinnedSectionExpanded = isPinnedSectionExpanded,
        onToggleSection = onTogglePinnedSection
      )
    }

    if (isPinnedSectionExpanded) {
      items(
        count = pinnedSources.size,
        key = { "PinnedSource: ${pinnedSources[it].id}" },
        contentType = {
          if (pinnedSources[it].sourceType == SourceType.FeedGroup) {
            "FeedGroupItem"
          } else {
            "FeedListItem"
          }
        },
      ) { index ->
        val source = pinnedSources[index]
        val startPadding = 24.dp
        val endPadding = 24.dp
        val topPadding = 4.dp
        val bottomPadding = bottomPaddingOfSourceItem(index, pinnedSources.size)
        val interactionSource = remember { MutableInteractionSource() }

        ReorderableItem(state = reorderableLazyListState, key = "PinnedSource: ${source.id}") {
          when (source) {
            is FeedGroup -> {
              FeedGroupItem(
                feedGroup = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                selected = selectedSources.any { it.id == source.id },
                onFeedGroupSelected = onToggleSourceSelection,
                onFeedGroupClick = onSourceClick,
                onOptionsClick = { onToggleSourceSelection(source) },
                dragHandle = { DragHandle(this, interactionSource) },
                interactionSource = interactionSource,
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
              FeedListItem(
                feed = source,
                canShowUnreadPostsCount = canShowUnreadPostsCount,
                isInMultiSelectMode = isInMultiSelectMode,
                isFeedSelected = selectedSources.any { it.id == source.id },
                onFeedClick = onSourceClick,
                onFeedSelected = onToggleSourceSelection,
                onOptionsClick = { onToggleSourceSelection(source) },
                dragHandle = { DragHandle(this, interactionSource) },
                interactionSource = interactionSource,
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
          }
        }
      }
    }

    item {
      HorizontalDivider(
        modifier = Modifier.padding(top = 24.dp).animateItem(),
        color = AppTheme.colorScheme.tintedSurface
      )
    }
  }
}

@Composable
private fun DragHandle(
  scope: ReorderableCollectionItemScope,
  interactionSource: MutableInteractionSource
) {
  with(scope) {
    Box(modifier = Modifier.requiredSize(40.dp), contentAlignment = Alignment.Center) {
      Icon(
        modifier =
          Modifier.requiredSize(20.dp).draggableHandle(interactionSource = interactionSource),
        imageVector = TwineIcons.DragIndicator,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurfaceVariant,
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
      Modifier.background(AppTheme.colorScheme.bottomSheet)
        .padding(start = 32.dp, end = 20.dp)
        .padding(vertical = 12.dp)
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.pinnedFeeds),
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
