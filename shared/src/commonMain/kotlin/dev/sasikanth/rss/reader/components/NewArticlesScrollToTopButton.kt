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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.adamglin.composeshadow.dropShadow
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.ui.AppTheme
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
    AppTheme(useDarkTheme = true) {
      val coroutineScope = rememberCoroutineScope()
      AnimatedVisibility(
        visible = unreadSinceLastSync.hasNewArticles || canShowScrollToTop,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = Modifier.align(Alignment.BottomCenter)
      ) {
        val buttonShape = RoundedCornerShape(50)
        Box(
          modifier =
            modifier
              .background(AppTheme.colorScheme.bottomSheet, buttonShape)
              .border(1.dp, AppTheme.colorScheme.bottomSheetBorder, buttonShape)
              .dropShadow(
                shape = buttonShape,
                color = Color.Black.copy(alpha = 0.4f),
                offsetX = 0.dp,
                offsetY = 16.dp,
                blur = 32.dp,
                spread = 0.dp,
              )
              .dropShadow(
                shape = buttonShape,
                color = Color.Black.copy(alpha = 0.016f),
                offsetX = 0.dp,
                offsetY = 4.dp,
                blur = 8.dp,
                spread = 0.dp,
              )
              .padding(4.dp),
        ) {
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

                    icons.forEachIndexed { index, icon ->
                      val overlappedIconModifier =
                        if (index > 0) {
                          Modifier.offset(x = (-12 * index).dp)
                        } else {
                          Modifier
                        }

                      FeedIcon(
                        url = icon,
                        contentDescription = null,
                        modifier =
                          Modifier.requiredSize(24.dp)
                            .then(overlappedIconModifier)
                            .border(
                              width = 2.dp,
                              color = AppTheme.colorScheme.bottomSheet,
                              shape = MaterialTheme.shapes.extraSmall
                            )
                            .clip(MaterialTheme.shapes.extraSmall)
                            .zIndex(index.toFloat())
                      )
                    }

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

            AnimatedVisibility(visible = canShowScrollToTop) {
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
