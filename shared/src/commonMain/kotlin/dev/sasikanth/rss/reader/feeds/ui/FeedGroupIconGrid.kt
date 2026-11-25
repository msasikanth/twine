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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.ui.TranslucentStyle

private val FeedGroupIconShape = RoundedCornerShape(25)

@Composable
internal fun FeedGroupIconGrid(
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  iconSize: Dp = 14.dp,
  modifier: Modifier = Modifier,
  cutoutColor: (() -> Color)? = null,
) {
  val colorScheme = AppTheme.colorScheme
  val translucentStyle = LocalTranslucentStyles.current.default

  Layout(
    modifier = modifier.requiredSize(iconSize * 2),
    content = {
      FeedGroupSlotIcon(
        index = 2,
        feedHomepageLinks = feedHomepageLinks,
        feedIconLinks = feedIconLinks,
        feedShowFavIconSettings = feedShowFavIconSettings,
        colorScheme = colorScheme,
        translucentStyle = translucentStyle,
      )

      FeedGroupSlotIcon(
        index = 1,
        feedHomepageLinks = feedHomepageLinks,
        feedIconLinks = feedIconLinks,
        feedShowFavIconSettings = feedShowFavIconSettings,
        colorScheme = colorScheme,
        translucentStyle = translucentStyle,
        modifier = Modifier.drawSiblingHalo(colorScheme, cutoutColor),
      )

      FeedGroupSlotIcon(
        index = 0,
        feedHomepageLinks = feedHomepageLinks,
        feedIconLinks = feedIconLinks,
        feedShowFavIconSettings = feedShowFavIconSettings,
        colorScheme = colorScheme,
        translucentStyle = translucentStyle,
        modifier = Modifier.drawSiblingHalo(colorScheme, cutoutColor),
      )
    },
  ) { measurables, constraints ->
    val iconPx = iconSize.roundToPx()
    val defaultContainerPx = (iconSize * 2).roundToPx()
    val containerWidth = constraints.constrainWidth(defaultContainerPx)
    val containerHeight = constraints.constrainHeight(defaultContainerPx)
    val childConstraints = Constraints.fixed(iconPx, iconPx)

    val topRight = measurables[0].measure(childConstraints)
    val middle = measurables[1].measure(childConstraints)
    val bottomLeft = measurables[2].measure(childConstraints)

    layout(containerWidth, containerHeight) {
      topRight.placeRelative(x = containerWidth - iconPx, y = 0)
      middle.placeRelative(x = (containerWidth - iconPx) / 2, y = (containerHeight - iconPx) / 2)
      bottomLeft.placeRelative(x = 0, y = containerHeight - iconPx)
    }
  }
}

private fun Modifier.drawSiblingHalo(
  colorScheme: AppColorScheme,
  cutoutColor: (() -> Color)?,
): Modifier = drawWithCache {
  val spread = 1.dp.toPx()
  val haloTopLeft = Offset(-spread, -spread)
  val haloSize = Size(size.width + spread * 2f, size.height + spread * 2f)
  val radius = minOf(size.width, size.height) * 0.25f + spread
  val haloCornerRadius = CornerRadius(radius, radius)
  onDrawBehind {
    val color = cutoutColor?.invoke() ?: colorScheme.backdrop
    drawRoundRect(
      color = color,
      topLeft = haloTopLeft,
      size = haloSize,
      cornerRadius = haloCornerRadius,
    )
  }
}

@Composable
private fun FeedGroupSlotIcon(
  index: Int,
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  colorScheme: AppColorScheme,
  translucentStyle: TranslucentStyle,
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
    Box(
      modifier.drawWithCache {
        val outline = FeedGroupIconShape.createOutline(size, layoutDirection, this)
        onDrawBehind {
          val placeholderColor = translucentStyle.background.compositeOver(colorScheme.backdrop)
          drawOutline(outline, placeholderColor)
        }
      }
    )
  }
}
