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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.ui.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedListItem(
  feed: Feed,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = 4.dp)
        .then(modifier)
        .clip(RoundedCornerShape(16.dp))
        .background(AppTheme.colorScheme.tintedSurface)
        .combinedClickable(
          onClick = { onFeedSelected(feed) },
          onLongClick = {
            // TODO: Add support for feeds multi selection
            onFeedInfoClick(feed)
          }
        )
  ) {
    Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier.requiredSize(36.dp).background(Color.White, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          url = feed.icon,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier =
            Modifier.requiredSize(28.dp).clip(RoundedCornerShape(4.dp)).align(Alignment.Center),
        )
      }

      Spacer(Modifier.requiredWidth(12.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 1,
        overflow = TextOverflow.Clip
      )

      Spacer(Modifier.requiredWidth(12.dp))

      val numberOfUnreadPosts = feed.numberOfUnreadPosts
      if (numberOfUnreadPosts > 0) {
        Badge(
          containerColor = AppTheme.colorScheme.tintedForeground,
          contentColor = AppTheme.colorScheme.tintedBackground,
          modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 16.dp)
        ) {
          Text(
            text = feed.numberOfUnreadPosts.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterVertically)
          )
        }
      }
    }
  }
}
