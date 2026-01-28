/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.feeds.ui.sheet.collapsed

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants.BADGE_COUNT_TRIM_LIMIT

@Composable
internal fun FeedBottomBarItem(
  badgeCount: Long,
  homePageUrl: String,
  feedIconUrl: String,
  showFeedFavIcon: Boolean,
  canShowUnreadPostsCount: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  hasActiveSource: Boolean = false,
  selected: Boolean = false,
) {
  Box(
    modifier =
      modifier.size(64.dp).graphicsLayer {
        alpha = if (selected || !hasActiveSource) 1f else 0.25f
      },
    contentAlignment = Alignment.Center
  ) {
    val shape = MaterialTheme.shapes.large

    Box(
      modifier = Modifier.requiredSize(48.dp).clickable(onClick = onClick),
      contentAlignment = Alignment.Center
    ) {
      FeedIcon(
        icon = feedIconUrl,
        homepageLink = homePageUrl,
        showFeedFavIcon = showFeedFavIcon,
        contentDescription = null,
        shape = RoundedCornerShape(17.dp),
        modifier = Modifier.matchParentSize()
      )

      Box(
        modifier =
          Modifier.matchParentSize().border(1.dp, AppTheme.colorScheme.outlineVariant, shape)
      )
    }

    if (badgeCount > 0 && canShowUnreadPostsCount) {
      UnreadCountBadge(badgeCount)
    }
  }
}

@Composable
internal fun BoxScope.UnreadCountBadge(badgeCount: Long, modifier: Modifier = Modifier) {
  Badge(
    containerColor = AppTheme.colorScheme.onSurface,
    contentColor = AppTheme.colorScheme.surface,
    modifier =
      modifier
        .sizeIn(minWidth = 31.dp, minHeight = 24.dp)
        .align(Alignment.TopEnd)
        .border(2.dp, AppTheme.colorScheme.bottomSheet, RoundedCornerShape(50))
        .padding(1.dp),
  ) {
    val badgeText =
      if (badgeCount > BADGE_COUNT_TRIM_LIMIT) {
        "+$BADGE_COUNT_TRIM_LIMIT"
      } else {
        badgeCount.toString()
      }

    Text(
      text = badgeText,
      style = MaterialTheme.typography.labelMedium,
      modifier = Modifier.align(Alignment.CenterVertically)
    )
  }
}
