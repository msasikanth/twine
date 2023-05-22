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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.statusBars
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ListItemRippleTheme
import dev.sasikanth.rss.reader.utils.relativeDurationString
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun PostsList(
  featuredPosts: ImmutableList<PostWithMetadata>,
  posts: ImmutableList<PostWithMetadata>,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  onPostClicked: (post: PostWithMetadata) -> Unit
) {
  val statusBarPadding =
    if (featuredPosts.isEmpty()) {
      WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    } else {
      0.dp
    }

  LazyColumn(contentPadding = PaddingValues(top = statusBarPadding, bottom = 136.dp)) {
    if (featuredPosts.isNotEmpty()) {
      item {
        FeaturedPostItems(
          featuredPosts = featuredPosts,
          onItemClick = onPostClicked,
          onFeaturedItemChange = onFeaturedItemChange
        )
      }
    }

    itemsIndexed(posts) { i, post ->
      PostListItem(post) { onPostClicked(post) }
      if (i != posts.size - 1) {
        Divider(
          modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    }
  }
}

@Composable
private fun PostListItem(item: PostWithMetadata, onClick: () -> Unit) {
  CompositionLocalProvider(LocalRippleTheme provides ListItemRippleTheme) {
    Row(
      modifier = Modifier.clickable(onClick = onClick).padding(24.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          style = MaterialTheme.typography.titleSmall,
          text = item.title,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 2
        )
        PostMetadata(post = item)
      }

      item.imageUrl?.let { url ->
        AsyncImage(
          url = url,
          modifier =
            Modifier.requiredSize(width = 128.dp, height = 72.dp).clip(RoundedCornerShape(12.dp)),
          contentDescription = null,
          contentScale = ContentScale.Crop
        )
      }
    }
  }
}

@Composable
private fun PostMetadata(post: PostWithMetadata) {
  val feedName = post.feedName ?: "Unknown"
  val postPublishedAt = post.date.relativeDurationString()

  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      modifier = Modifier.requiredWidthIn(max = 72.dp),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = feedName,
      color = AppTheme.colorScheme.textEmphasisHigh,
      overflow = TextOverflow.Ellipsis
    )

    Text(
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = "•",
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    Text(
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = postPublishedAt,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Left
    )
  }
}
