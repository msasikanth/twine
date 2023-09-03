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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun PostMetadata(
  feedName: String,
  postPublishedAt: String,
  postLink: String,
  postBookmarked: Boolean,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier =
      Modifier.padding(
          top = 8.dp,
          bottom = 8.dp,
        )
        .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.requiredWidthIn(max = 72.dp),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = feedName,
      color = AppTheme.colorScheme.textEmphasisMed,
      overflow = TextOverflow.Ellipsis
    )

    Text(
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = "•",
      color = AppTheme.colorScheme.textEmphasisMed
    )

    Text(
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = postPublishedAt,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Left
    )

    PostOptionsButtonRow(
      postLink = postLink,
      postBookmarked = postBookmarked,
      onBookmarkClick = onBookmarkClick
    )
  }
}

@Composable
private fun PostOptionsButtonRow(
  postLink: String,
  postBookmarked: Boolean,
  onBookmarkClick: () -> Unit
) {
  Row {
    PostOptionIconButton(
      icon =
        if (postBookmarked) {
          TwineIcons.Bookmarked
        } else {
          TwineIcons.Bookmark
        },
      iconTint =
        if (postBookmarked) {
          AppTheme.colorScheme.tintedForeground
        } else {
          Color.White
        },
      contentDescription = LocalStrings.current.bookmark,
      onClick = onBookmarkClick
    )

    PostOptionShareIconButton(postLink)
  }
}

@Composable
internal fun PostOptionIconButton(
  icon: ImageVector,
  contentDescription: String,
  modifier: Modifier = Modifier,
  iconTint: Color = Color.White,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(40.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onClick)
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Composable internal expect fun PostOptionShareIconButton(postLink: String)
