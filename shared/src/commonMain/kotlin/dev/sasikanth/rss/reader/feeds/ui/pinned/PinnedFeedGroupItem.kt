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
package dev.sasikanth.rss.reader.feeds.ui.pinned

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.UnreadBadge
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.ui.AppTheme

private val PinnedFeedGroupShape = RoundedCornerShape(16.dp)

@Composable
internal fun PinnedFeedGroupItem(
  feedGroup: FeedGroup,
  canShowUnreadPostsCount: Boolean,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  hasActiveSource: Boolean = false,
  selected: Boolean = false,
  isDragging: Boolean = false,
) {
  val colorScheme = AppTheme.colorScheme
  Box(
    modifier = modifier.graphicsLayer { alpha = if (selected || !hasActiveSource) 1f else 0.25f }
  ) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
      val clickableModifier =
        if (onClick != null) Modifier.clickable(enabled = !isDragging, onClick = onClick)
        else Modifier

      Box(
        modifier =
          Modifier.requiredSize(48.dp)
            .clip(PinnedFeedGroupShape)
            .then(clickableModifier)
            .drawWithCache {
              val outline = PinnedFeedGroupShape.createOutline(size, layoutDirection, this)
              val strokeWidth = 1.dp.toPx()
              val halfStroke = strokeWidth / 2f
              val borderStroke = Stroke(width = strokeWidth)
              val borderTopLeft = Offset(halfStroke, halfStroke)
              val borderSize = Size(size.width - strokeWidth, size.height - strokeWidth)
              val radius = (16.dp.toPx() - halfStroke).coerceAtLeast(0f)
              val borderCornerRadius = CornerRadius(radius, radius)
              onDrawWithContent {
                drawOutline(outline, colorScheme.secondary.copy(alpha = 0.16f))
                drawContent()
                drawRoundRect(
                  color = colorScheme.outlineVariant,
                  topLeft = borderTopLeft,
                  size = borderSize,
                  cornerRadius = borderCornerRadius,
                  style = borderStroke,
                )
              }
            },
        contentAlignment = Alignment.Center,
      ) {
        val groupCutoutColor =
          remember(colorScheme) {
            { colorScheme.secondary.copy(alpha = 0.16f).compositeOver(colorScheme.bottomSheet) }
          }
        FeedGroupIconGrid(
          feedHomepageLinks = feedGroup.feedHomepageLinks,
          feedIconLinks = feedGroup.feedIconLinks,
          feedShowFavIconSettings = feedGroup.feedShowFavIconSettings,
          cutoutColor = groupCutoutColor,
        )
      }
    }

    val badgeCount = feedGroup.numberOfUnreadPosts
    if (!isDragging && badgeCount > 0 && canShowUnreadPostsCount) {
      UnreadBadge(badgeCount, modifier = Modifier.align(Alignment.TopEnd))
    }
  }
}
