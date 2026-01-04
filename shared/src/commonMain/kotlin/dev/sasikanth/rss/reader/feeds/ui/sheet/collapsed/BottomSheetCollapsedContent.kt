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

package dev.sasikanth.rss.reader.feeds.ui.sheet.collapsed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.sheet.BOTTOM_SHEET_PEEK_HEIGHT
import dev.sasikanth.rss.reader.resources.icons.ExpandContent
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.feeds
import twine.shared.generated.resources.noPinnedSources

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BottomSheetCollapsedContent(
  pinnedSources: List<Source>,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  openFeeds: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier.height(IntrinsicSize.Min)) {
    LazyRow(
      modifier = Modifier.fillMaxWidth().height(BOTTOM_SHEET_PEEK_HEIGHT),
      verticalAlignment = Alignment.CenterVertically,
      contentPadding = PaddingValues(start = 8.dp, end = 64.dp),
      overscrollEffect = null
    ) {
      if (activeSource != null && activeSource.pinnedAt == null) {
        item {
          SourceItem(
            source = activeSource,
            activeSource = activeSource,
            canShowUnreadPostsCount = canShowUnreadPostsCount,
            onHomeSelected = onHomeSelected,
            onSourceClick = onSourceClick
          )
        }

        item {
          VerticalDivider(
            modifier = Modifier.requiredHeight(24.dp),
            color = AppTheme.colorScheme.outlineVariant,
          )
        }
      }

      items(pinnedSources.size) { index ->
        SourceItem(
          source = pinnedSources[index],
          activeSource = activeSource,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          onHomeSelected = onHomeSelected,
          onSourceClick = onSourceClick
        )
      }

      if (pinnedSources.isEmpty() && activeSource == null) {
        item {
          Text(
            text = stringResource(Res.string.noPinnedSources),
            color = AppTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillParentMaxWidth()
          )
        }
      }
    }

    val bottomSheetBackground = AppTheme.colorScheme.bottomSheet
    Row(
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .fillMaxHeight()
          .background(
            Brush.horizontalGradient(
              0f to Color.Transparent,
              0.2f to bottomSheetBackground,
              1f to bottomSheetBackground,
            )
          )
          .pointerInput(Unit) {},
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (pinnedSources.isNotEmpty() || activeSource != null) {
        Spacer(modifier = Modifier.width(16.dp))

        CircularIconButton(
          icon = TwineIcons.ExpandContent,
          label = stringResource(Res.string.feeds),
          onClick = openFeeds,
        )

        Spacer(modifier = Modifier.width(12.dp))
      }
    }
  }
}

@Composable
private fun SourceItem(
  source: Source,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onHomeSelected: () -> Unit,
  onSourceClick: (Source) -> Unit,
) {
  when (val source = source) {
    is FeedGroup -> {
      FeedGroupBottomBarItem(
        feedGroup = source,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        }
      )
    }
    is Feed -> {
      FeedBottomBarItem(
        badgeCount = source.numberOfUnreadPosts,
        homePageUrl = source.homepageLink,
        feedIconUrl = source.icon,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
  }
}
