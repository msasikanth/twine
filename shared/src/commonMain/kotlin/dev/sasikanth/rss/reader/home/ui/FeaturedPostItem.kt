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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.relativeDurationString

private val featuredImageAspectRatio: Float
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.77f
      WindowWidthSizeClass.Medium -> 2.5f
      WindowWidthSizeClass.Expanded -> 1.9f
      else -> 1.77f
    }

@Composable
internal fun FeaturedPostItem(
  item: PostWithMetadata,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit
) {
  val isLargeScreenLayout =
    LocalWindowSizeClass.current.widthSizeClass == WindowWidthSizeClass.Expanded
  Box(modifier = Modifier.clip(MaterialTheme.shapes.extraLarge).clickable(onClick = onClick)) {
    if (isLargeScreenLayout) {
      LargeScreenFeaturedPostItem(item, onBookmarkClick, onCommentsClick)
    } else {
      DefaultFeaturedPostItem(item, onBookmarkClick, onCommentsClick)
    }
  }
}

@Composable
private fun DefaultFeaturedPostItem(
  item: PostWithMetadata,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit
) {
  Column {
    AsyncImage(
      url = item.imageUrl!!,
      modifier =
        Modifier.clip(MaterialTheme.shapes.extraLarge)
          .aspectRatio(featuredImageAspectRatio)
          .background(AppTheme.colorScheme.surfaceContainerLowest),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

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
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      postLink = item.link,
      postBookmarked = item.bookmarked,
      commentsLink = item.commentsLink,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      modifier = Modifier.padding(start = 16.dp, end = 0.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
private fun LargeScreenFeaturedPostItem(
  item: PostWithMetadata,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    AsyncImage(
      url = item.imageUrl!!,
      modifier =
        Modifier.clip(MaterialTheme.shapes.extraLarge)
          .aspectRatio(featuredImageAspectRatio)
          .weight(0.92f)
          .background(AppTheme.colorScheme.surfaceContainerLowest),
      contentDescription = null,
      contentScale = ContentScale.Crop
    )

    Spacer(modifier = Modifier.requiredWidth(8.dp))

    Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
      Text(
        modifier = Modifier.padding(top = 16.dp),
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
          text = item.description,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.textEmphasisHigh,
          minLines = 3,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
        )
      }

      PostMetadata(
        feedName = item.feedName,
        postPublishedAt = item.date.relativeDurationString(),
        postLink = item.link,
        postBookmarked = item.bookmarked,
        commentsLink = item.commentsLink,
        onBookmarkClick = onBookmarkClick,
        onCommentsClick = onCommentsClick,
      )
    }
  }
}
