/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun BottomSheetCollapsedContent(
  pinnedSources: List<Source>,
  numberOfFeeds: Int,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box {
    LazyRow(
      modifier = modifier.fillMaxWidth().padding(start = 20.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(end = 24.dp)
    ) {
      stickyHeader {
        val shadowColors =
          arrayOf(
            0.85f to AppTheme.colorScheme.tintedBackground,
            0.9f to AppTheme.colorScheme.tintedBackground.copy(alpha = 0.4f),
            1f to Color.Transparent
          )

        HomeBottomBarItem(
          selected = activeSource == null,
          onClick = onHomeSelected,
          modifier =
            Modifier.drawWithCache {
                onDrawBehind {
                  val brush =
                    Brush.horizontalGradient(
                      colorStops = shadowColors,
                    )
                  drawRect(
                    brush = brush,
                  )
                }
              }
              .padding(end = 4.dp)
        )
      }

      items(pinnedSources.size) { index ->
        when (val source = pinnedSources[index]) {
          is FeedGroup -> {
            FeedGroupBottomBarItem(
              feedGroup = source,
              canShowUnreadPostsCount = canShowUnreadPostsCount,
              selected = activeSource?.id == source.id,
              onClick = { onSourceClick(source) }
            )
          }
          is Feed -> {
            FeedBottomBarItem(
              badgeCount = source.numberOfUnreadPosts,
              homePageUrl = source.homepageLink,
              feedIconUrl = source.icon,
              canShowUnreadPostsCount = canShowUnreadPostsCount,
              onClick = { onSourceClick(source) },
              selected = activeSource?.id == source.id
            )
          }
        }
      }
    }

    if (pinnedSources.isEmpty() && numberOfFeeds > 0) {
      Box(
        modifier = Modifier.fillMaxWidth().requiredHeight(height = 64.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          modifier = Modifier.padding(start = 24.dp),
          text = LocalStrings.current.noPinnedSources,
          color = AppTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}
