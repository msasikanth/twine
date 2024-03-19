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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun FeedListItem(
  feed: Feed,
  selected: Boolean,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .clickable { onFeedSelected(feed) }
        .background(AppTheme.colorScheme.tintedSurface)
        .padding(vertical = 8.dp)
        .padding(start = 8.dp)
        .then(modifier),
  ) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier.requiredSize(36.dp).clip(MaterialTheme.shapes.small).background(Color.White),
        contentAlignment = Alignment.Center
      ) {
        AsyncImage(
          url = feed.icon,
          contentDescription = null,
          modifier = Modifier.requiredSize(28.dp).clip(MaterialTheme.shapes.small),
        )
      }

      Spacer(Modifier.requiredWidth(12.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.textEmphasisHigh
      )

      IconButton(onClick = { onFeedInfoClick(feed) }) {
        Icon(
          imageVector = Icons.Rounded.MoreVert,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    }
  }
}
