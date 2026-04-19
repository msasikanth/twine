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
package dev.sasikanth.rss.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.resources.icons.ArrowUp
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.newArticles
import twine.shared.generated.resources.scrollToTop

@Composable
internal fun BoxScope.NewArticlesScrollToTopButton(
  unreadSinceLastSync: UnreadSinceLastSync?,
  canShowScrollToTop: Boolean,
  modifier: Modifier = Modifier,
  onLoadNewArticlesClick: () -> Unit,
  onScrollToTopClick: suspend () -> Unit,
) {
  if (unreadSinceLastSync != null) {
    val coroutineScope = rememberCoroutineScope()
    val colorScheme = AppTheme.colorScheme
    val shape = CircleShape

    AnimatedVisibility(
      visible = unreadSinceLastSync.hasNewArticles || canShowScrollToTop,
      enter = fadeIn() + expandIn(expandFrom = Alignment.Center, clip = false),
      exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.Center, clip = false),
      modifier =
        modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = 16.dp)
          .dropShadow(shape = CircleShape) {
            color = Color.Black.copy(alpha = 0.4f)
            offset = Offset(0f, 16.dp.toPx())
            radius = 32.dp.toPx()
            spread = 0f
          }
          .dropShadow(shape = CircleShape) {
            color = Color.Black.copy(alpha = 0.016f)
            offset = Offset(0f, 4.dp.toPx())
            radius = 8.dp.toPx()
            spread = 0f
          }
          .drawBehind {
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(outline = outline, color = colorScheme.bottomSheet)
          }
          .drawWithContent {
            drawContent()
            val outline = shape.createOutline(size, layoutDirection, this)
            drawOutline(
              outline = outline,
              color = colorScheme.bottomSheetBorder,
              style = Stroke(width = 1.dp.toPx()),
            )
          }
          .padding(4.dp),
    ) {
      AppTheme(useDarkTheme = true) {
        Row(
          modifier = Modifier.height(IntrinsicSize.Min),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          AnimatedVisibility(visible = unreadSinceLastSync.hasNewArticles) {
            Row {
              val endPadding by
                animateDpAsState(targetValue = if (canShowScrollToTop) 12.dp else 16.dp)

              TextButton(
                modifier = Modifier.fillMaxHeight(),
                onClick = onLoadNewArticlesClick,
                shape = RoundedCornerShape(50),
                colors =
                  ButtonDefaults.textButtonColors(contentColor = AppTheme.colorScheme.onSurface),
                contentPadding =
                  PaddingValues(start = 12.dp, top = 8.dp, end = endPadding, bottom = 8.dp),
                content = {
                  unreadSinceLastSync.apply {
                    OverlappedFeedIcons(
                      feedHomepageLinks = feedHomepageLinks,
                      feedIcons = feedIcons,
                      feedShowFavIconSettings = feedShowFavIconSettings,
                    )
                  }

                  Spacer(modifier = Modifier.requiredWidth(12.dp))

                  Text(
                    text = stringResource(Res.string.newArticles),
                    style = MaterialTheme.typography.labelLarge,
                  )
                },
              )
            }
          }

          AnimatedVisibility(visible = unreadSinceLastSync.hasNewArticles && canShowScrollToTop) {
            VerticalDivider(modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp))
          }

          AnimatedVisibility(
            visible = canShowScrollToTop,
            enter =
              if (unreadSinceLastSync.hasNewArticles) {
                fadeIn() + expandHorizontally()
              } else {
                fadeIn()
              },
            exit =
              if (unreadSinceLastSync.hasNewArticles) {
                fadeOut() + shrinkHorizontally()
              } else {
                fadeOut()
              },
          ) {
            IconButton(
              icon = TwineIcons.ArrowUp,
              contentDescription = stringResource(Res.string.scrollToTop),
              onClick = { coroutineScope.launch { onScrollToTopClick() } },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun OverlappedFeedIcons(
  feedHomepageLinks: List<String>,
  feedIcons: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val extraSmallShape = MaterialTheme.shapes.extraSmall

  Layout(
    modifier = modifier,
    content = {
      val iconsCount = maxOf(feedHomepageLinks.size, feedIcons.size)
      for (index in 0 until iconsCount) {
        val homepageLink = feedHomepageLinks.getOrNull(index)
        val icon = feedIcons.getOrNull(index)
        val showFeedFavIcon = feedShowFavIconSettings.getOrNull(index) ?: true

        if (!homepageLink.isNullOrBlank() || !icon.isNullOrBlank()) {
          Box(
            modifier =
              Modifier.drawBehind {
                  val outline = extraSmallShape.createOutline(size, layoutDirection, this)
                  drawOutline(outline, colorScheme.bottomSheet)
                }
                .padding(horizontal = 2.dp, vertical = 1.dp)
          ) {
            FeedIcon(
              icon = icon.orEmpty(),
              homepageLink = homepageLink.orEmpty(),
              showFeedFavIcon = showFeedFavIcon,
              contentDescription = null,
              modifier = Modifier.requiredSize(20.dp),
            )
          }
        }
      }
    },
  ) { measurables, constraints ->
    val placeables = measurables.map { it.measure(constraints) }
    val height = placeables.maxOfOrNull { it.height } ?: 0
    val totalWidth =
      if (placeables.isNotEmpty()) {
        val firstItemWidth = placeables.first().width
        val remainingItemsWidth = placeables.drop(1).sumOf { it.width }
        val overlap = (12 * (placeables.size - 1)).dp.roundToPx()

        firstItemWidth + remainingItemsWidth - overlap
      } else {
        0
      }

    layout(totalWidth, height) {
      var x = 0
      placeables.forEachIndexed { index, placeable ->
        placeable.placeRelative(x = x, y = 0, zIndex = index.toFloat())
        x += placeable.width - 12.dp.roundToPx()
      }
    }
  }
}
