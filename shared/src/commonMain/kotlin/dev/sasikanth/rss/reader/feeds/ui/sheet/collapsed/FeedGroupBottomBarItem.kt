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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.ui.AppTheme
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
    modifier = modifier.graphicsLayer { alpha = if (selected || !hasActiveSource) 1f else 0.25f }
  ) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
      Box(
        modifier =
          Modifier.requiredSize(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .background(AppTheme.colorScheme.secondary.copy(alpha = 0.16f))
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
            2.dp
          } else {
            0.dp
          }

        FeedGroupIconGrid(
          icons = icons,
          iconSize = iconSize,
          iconShape = MaterialTheme.shapes.small,
          verticalArrangement = Arrangement.spacedBy(iconSpacing),
          horizontalArrangement = Arrangement.spacedBy(iconSpacing),
        )
      }
    }

    val badgeCount = feedGroup.numberOfUnreadPosts
    if (badgeCount > 0 && canShowUnreadPostsCount) {
      UnreadCountBadge(badgeCount)
    }
  }
}
