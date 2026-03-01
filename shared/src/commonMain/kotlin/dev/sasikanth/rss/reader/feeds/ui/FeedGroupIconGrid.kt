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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles

@Composable
internal fun FeedGroupIconGrid(
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  iconSize: Dp = 14.dp,
  modifier: Modifier = Modifier,
) {
  val containerSize = iconSize * 2
  val translucentStyle = LocalTranslucentStyles.current.default

  Box(
    modifier =
      modifier
        .requiredSize(containerSize)
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
  ) {
    val placeholderColor = translucentStyle.background.compositeOver(AppTheme.colorScheme.backdrop)
    val iconShape = RoundedCornerShape(25)

    // Top-right
    FeedIcon(
      index = 2,
      feedHomepageLinks = feedHomepageLinks,
      feedIconLinks = feedIconLinks,
      feedShowFavIconSettings = feedShowFavIconSettings,
      iconShape = iconShape,
      placeholderColor = placeholderColor,
      modifier = Modifier.requiredSize(iconSize).align(Alignment.TopEnd),
    )

    // Middle
    Box(
      modifier = Modifier.requiredSize(iconSize + 1.dp).align(Alignment.Center),
      contentAlignment = Alignment.Center,
    ) {
      FeedIcon(
        index = 1,
        feedHomepageLinks = feedHomepageLinks,
        feedIconLinks = feedIconLinks,
        feedShowFavIconSettings = feedShowFavIconSettings,
        iconShape = iconShape,
        placeholderColor = placeholderColor,
        modifier =
          Modifier.dropShadow(iconShape) {
              color = Color.Black
              spread = 1.dp.toPx()
              blendMode = BlendMode.DstOut
            }
            .requiredSize(iconSize),
      )
    }

    // Bottom-left
    Box(
      modifier = Modifier.requiredSize(iconSize + 1.dp).align(Alignment.BottomStart),
      contentAlignment = Alignment.Center,
    ) {
      FeedIcon(
        index = 0,
        feedHomepageLinks = feedHomepageLinks,
        feedIconLinks = feedIconLinks,
        feedShowFavIconSettings = feedShowFavIconSettings,
        iconShape = iconShape,
        placeholderColor = placeholderColor,
        modifier =
          Modifier.dropShadow(iconShape) {
              color = Color.Black
              spread = 1.dp.toPx()
              blendMode = BlendMode.DstOut
            }
            .requiredSize(iconSize),
      )
    }
  }
}

@Composable
private fun FeedIcon(
  index: Int,
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  iconShape: Shape,
  placeholderColor: Color,
  modifier: Modifier = Modifier,
) {
  val homepageLink = feedHomepageLinks.getOrNull(index)
  val iconLink = feedIconLinks.getOrNull(index)
  val showFavIconSetting = feedShowFavIconSettings.getOrNull(index) ?: true

  if (!homepageLink.isNullOrBlank() || !iconLink.isNullOrBlank()) {
    FeedIcon(
      icon = iconLink.orEmpty(),
      homepageLink = homepageLink.orEmpty(),
      showFeedFavIcon = showFavIconSetting,
      contentDescription = null,
      modifier = modifier,
    )
  } else {
    Box(modifier.background(placeholderColor, iconShape))
  }
}
