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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.relativeDurationString

private val featuredImageAspectRatio: Float
  @Composable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.77f
      WindowWidthSizeClass.Medium -> 2.5f
      else -> 1.77f
    }

@Composable
internal fun FeaturedPostItem(
  item: PostWithMetadata,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit
) {
  Column(modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable(onClick = onClick)) {
    Box {
      AsyncImage(
        url = item.imageUrl!!,
        modifier =
          Modifier.clip(MaterialTheme.shapes.extraLarge)
            .aspectRatio(featuredImageAspectRatio)
            .background(AppTheme.colorScheme.surfaceContainerLowest),
        contentDescription = null,
        contentScale = ContentScale.Crop
      )

      PostSourceChip(post = item, modifier = Modifier.align(Alignment.BottomStart))
    }

    Spacer(modifier = Modifier.requiredHeight(8.dp))

    Text(
      modifier = Modifier.padding(horizontal = 16.dp),
      text = item.title,
      style = MaterialTheme.typography.headlineSmall,
      color = AppTheme.colorScheme.textEmphasisHigh,
      minLines = 2,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )

    if (item.description.isNotBlank()) {
      Spacer(modifier = Modifier.requiredHeight(8.dp))

      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = item.description,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colorScheme.textEmphasisHigh,
        minLines = 3,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    PostMetadata(
      modifier = Modifier.padding(start = 16.dp, end = 0.dp),
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      postLink = item.link,
      postBookmarked = item.bookmarked,
      onBookmarkClick = onBookmarkClick
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun PostSourceChip(post: PostWithMetadata, modifier: Modifier = Modifier) {
  val feedName = post.feedName
  val verticalPadding = 8.dp
  val startPadding = 8.dp
  val endPadding = 16.dp
  val margin = 12.dp

  Row(
    modifier =
      Modifier.padding(margin)
        .background(color = Color.Black, shape = RoundedCornerShape(50))
        .padding(
          start = startPadding,
          top = verticalPadding,
          end = endPadding,
          bottom = verticalPadding
        )
        .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.clip(CircleShape).background(Color.White)) {
      AsyncImage(
        url = post.feedIcon,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.requiredSize(16.dp)
      )
    }

    Text(
      style = MaterialTheme.typography.labelMedium,
      maxLines = 1,
      text = feedName.uppercase().take(12),
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Left,
      overflow = TextOverflow.Clip
    )
  }
}
