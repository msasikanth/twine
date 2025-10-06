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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants.BADGE_COUNT_TRIM_LIMIT
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting

@Composable
internal fun FeedGroupBottomBarItem(
  feedGroup: FeedGroup,
  canShowUnreadPostsCount: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  hasActiveSource: Boolean = false,
  selected: Boolean = false,
) {
  Box(
    modifier = modifier.graphicsLayer { alpha = if (selected || !hasActiveSource) 1f else 0.5f }
  ) {
    Box(contentAlignment = Alignment.Center) {
      SelectionIndicator(selected = selected, animationProgress = 1f)
      Box(
        modifier =
          Modifier.requiredSize(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(AppTheme.colorScheme.tintedSurface)
            .padding(8.dp),
        contentAlignment = Alignment.Center
      ) {
        val showFeedFavIcon = LocalShowFeedFavIconSetting.current
        val icons = if (showFeedFavIcon) feedGroup.feedHomepageLinks else feedGroup.feedIconLinks
        val iconSize =
          if (icons.size > 2) {
            16.dp
          } else {
            18.dp
          }

        val iconSpacing =
          if (icons.size > 2) {
            4.dp
          } else {
            0.dp
          }

        FeedGroupIconGrid(
          icons = icons,
          iconSize = iconSize,
          iconShape = CircleShape,
          verticalArrangement = Arrangement.spacedBy(iconSpacing),
          horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        )
      }
    }

    val badgeCount = feedGroup.numberOfUnreadPosts
    if (badgeCount > 0 && canShowUnreadPostsCount) {
      Badge(
        containerColor = AppTheme.colorScheme.tintedForeground,
        contentColor = AppTheme.colorScheme.tintedBackground,
        modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 16.dp).align(Alignment.TopEnd),
      ) {
        val badgeText =
          if (badgeCount > BADGE_COUNT_TRIM_LIMIT) {
            "+$BADGE_COUNT_TRIM_LIMIT"
          } else {
            badgeCount.toString()
          }

        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelSmall,
          modifier =
            Modifier.align(Alignment.CenterVertically).graphicsLayer {
              translationY = -2.toDp().toPx()
            }
        )
      }
    }
  }
}
