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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.relativeDurationString
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PostsList(
  featuredPosts: ImmutableList<PostWithMetadata>,
  posts: ImmutableList<PostWithMetadata>,
  selectedFeed: Feed?,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  listState: LazyListState = rememberLazyListState(),
  onPostClicked: (post: PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit
) {
  val featuredPostsPagerState = rememberPagerState(pageCount = { featuredPosts.size })

  LaunchedEffect(selectedFeed) {
    listState.scrollToItem(0)
    featuredPostsPagerState.scrollToPage(0)
  }

  LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 240.dp)) {
    item {
      FeaturedSection(
        pagerState = featuredPostsPagerState,
        featuredPosts = featuredPosts,
        onItemClick = onPostClicked,
        onPostBookmarkClick = onPostBookmarkClick,
        onFeaturedItemChange = onFeaturedItemChange,
        onSearchClicked = onSearchClicked,
        onBookmarksClicked = onBookmarksClicked,
        onSettingsClicked = onSettingsClicked
      )
    }

    itemsIndexed(posts) { i, post ->
      PostListItem(
        item = post,
        onClick = { onPostClicked(post) },
        onPostBookmarkClick = { onPostBookmarkClick(post) }
      )
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
fun PostListItem(item: PostWithMetadata, onClick: () -> Unit, onPostBookmarkClick: () -> Unit) {
  Column(modifier = Modifier.clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        modifier = Modifier.weight(1f),
        style = MaterialTheme.typography.titleSmall,
        text = item.title,
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 2
      )

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

    PostMetadata(
      modifier = Modifier.padding(start = 24.dp, end = 12.dp),
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      postLink = item.link,
      postBookmarked = item.bookmarked,
      onBookmarkClick = onPostBookmarkClick
    )
  }
}
