/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.darkAppColorScheme
import dev.sasikanth.rss.reader.ui.lightAppColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
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
    AnimatedVisibility(
      visible = unreadSinceLastSync.hasNewArticles || canShowScrollToTop,
      enter = slideInVertically { it },
      exit = slideOutVertically { it },
      modifier = modifier.align(Alignment.BottomCenter)
    ) {
      val buttonShape = RoundedCornerShape(50)
      Box(
        modifier =
          Modifier.padding(bottom = 16.dp)
            .background(AppTheme.colorScheme.bottomSheet, buttonShape)
            .border(1.dp, AppTheme.colorScheme.bottomSheetBorder, buttonShape)
            .dropShadow(shape = buttonShape) {
              color = Color.Black.copy(alpha = 0.4f)
              offset = Offset(0f, 16.dp.toPx())
              radius = 32.dp.toPx()
              spread = 0f
            }
            .dropShadow(shape = buttonShape) {
              color = Color.Black.copy(alpha = 0.016f)
              offset = Offset(0f, 4.dp.toPx())
              radius = 8.dp.toPx()
              spread = 0f
            }
            .padding(4.dp),
      ) {
        AppTheme(useDarkTheme = true) {
          Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            AnimatedVisibility(visible = unreadSinceLastSync.hasNewArticles) {
              Row {
                val endPadding by
                  animateDpAsState(targetValue = if (canShowScrollToTop) 12.dp else 16.dp)

                TextButton(
                  modifier = Modifier.fillMaxHeight(),
                  onClick = onLoadNewArticlesClick,
                  shape = RoundedCornerShape(50),
                  colors =
                    ButtonDefaults.textButtonColors(
                      contentColor = AppTheme.colorScheme.onSurface,
                    ),
                  contentPadding =
                    PaddingValues(
                      start = 12.dp,
                      top = 8.dp,
                      end = endPadding,
                      bottom = 8.dp,
                    ),
                  content = {
                    val showFeedFavIcon = LocalShowFeedFavIconSetting.current
                    val icons =
                      if (showFeedFavIcon) unreadSinceLastSync.feedHomepageLinks
                      else unreadSinceLastSync.feedIcons

                    OverlappedFeedIcons(icons = icons)

                    Spacer(modifier = Modifier.requiredWidth(12.dp))

                    Text(
                      text = stringResource(Res.string.newArticles),
                      style = MaterialTheme.typography.labelLarge,
                    )
                  }
                )
              }
            }

            AnimatedVisibility(visible = unreadSinceLastSync.hasNewArticles && canShowScrollToTop) {
              VerticalDivider(
                modifier = Modifier.fillMaxHeight().padding(vertical = 16.dp),
              )
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
                onClick = { coroutineScope.launch { onScrollToTopClick() } },
                content = {
                  Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = stringResource(Res.string.scrollToTop),
                    tint = AppTheme.colorScheme.onSurface,
                  )
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OverlappedFeedIcons(
  icons: List<String>,
  modifier: Modifier = Modifier,
) {
  Layout(
    modifier = modifier,
    content = {
      icons.forEach { icon ->
        Box(
          modifier =
            Modifier.background(AppTheme.colorScheme.bottomSheet, MaterialTheme.shapes.extraSmall)
              .padding(horizontal = 2.dp, vertical = 1.dp)
        ) {
          FeedIcon(
            url = icon,
            contentDescription = null,
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier.requiredSize(20.dp)
          )
        }
      }
    }
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

@Preview
@Composable
internal fun NewArticlesScrollToTopButtonPreview_OnlyNewArticles() {
  CompositionLocalProvider(
    LocalDynamicColorState provides
      rememberDynamicColorState(lightAppColorScheme(), darkAppColorScheme())
  ) {
    AppTheme {
      Box(modifier = Modifier.padding(16.dp)) {
        NewArticlesScrollToTopButton(
          unreadSinceLastSync =
            UnreadSinceLastSync(
              newArticleCount = 0,
              hasNewArticles = true,
              feedHomepageLinks = listOf("https://theverge.com", "https://wired.com"),
              feedIcons = listOf("https://icon.horse/theverge.com", "https://icon.horse/wired.com")
            ),
          canShowScrollToTop = false,
          onLoadNewArticlesClick = {},
          onScrollToTopClick = {}
        )
      }
    }
  }
}

@Preview
@Composable
internal fun NewArticlesScrollToTopButtonPreview_OnlyScrollToTop() {
  CompositionLocalProvider(
    LocalDynamicColorState provides
      rememberDynamicColorState(lightAppColorScheme(), darkAppColorScheme())
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      NewArticlesScrollToTopButton(
        unreadSinceLastSync =
          UnreadSinceLastSync(
            newArticleCount = 0,
            hasNewArticles = false,
            feedHomepageLinks = emptyList(),
            feedIcons = emptyList()
          ),
        canShowScrollToTop = true,
        onLoadNewArticlesClick = {},
        onScrollToTopClick = {}
      )
    }
  }
}

@Preview
@Composable
internal fun NewArticlesScrollToTopButtonPreview_Both() {
  CompositionLocalProvider(
    LocalDynamicColorState provides
      rememberDynamicColorState(lightAppColorScheme(), darkAppColorScheme())
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      NewArticlesScrollToTopButton(
        unreadSinceLastSync =
          UnreadSinceLastSync(
            newArticleCount = 0,
            hasNewArticles = true,
            feedHomepageLinks = listOf("https://theverge.com", "https://wired.com"),
            feedIcons = listOf("https://icon.horse/theverge.com", "https://icon.horse/wired.com")
          ),
        canShowScrollToTop = true,
        onLoadNewArticlesClick = {},
        onScrollToTopClick = {}
      )
    }
  }
}
