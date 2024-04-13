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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.relativeDurationString
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlinx.collections.immutable.ImmutableList

private val postListPadding
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Expanded -> PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(0.dp)
    }

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun PostsList(
  paddingValues: PaddingValues,
  featuredPosts: ImmutableList<PostWithMetadata>,
  posts: LazyPagingItems<PostWithMetadata>,
  featuredItemBlurEnabled: Boolean,
  listState: LazyListState,
  featuredPostsPagerState: PagerState,
  onPostClicked: (post: PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
  onTogglePostReadClick: (String, Boolean) -> Unit,
) {
  val topContentPadding =
    if (featuredPosts.isEmpty()) {
      paddingValues.calculateTopPadding()
    } else {
      0.dp
    }

  LazyColumn(
    state = listState,
    contentPadding = PaddingValues(top = topContentPadding, bottom = 128.dp)
  ) {
    item {
      FeaturedSection(
        paddingValues = paddingValues,
        pagerState = featuredPostsPagerState,
        featuredPosts = featuredPosts,
        featuredItemBlurEnabled = featuredItemBlurEnabled,
        onItemClick = onPostClicked,
        onPostBookmarkClick = onPostBookmarkClick,
        onPostCommentsClick = onPostCommentsClick,
        onPostSourceClick = onPostSourceClick,
        onTogglePostReadClick = onTogglePostReadClick
      )
    }

    items(posts.itemCount) { index ->
      val post = posts[index]
      if (post != null) {
        PostListItem(
          item = post,
          reduceReadItemAlpha = true,
          onClick = { onPostClicked(post) },
          onPostBookmarkClick = { onPostBookmarkClick(post) },
          onPostCommentsClick = { onPostCommentsClick(post.commentsLink!!) },
          onPostSourceClick = { onPostSourceClick(post.sourceId) },
          togglePostReadClick = { onTogglePostReadClick(post.id, post.read) }
        )
      } else {
        Box(Modifier.requiredHeight(132.dp))
      }

      if (index != posts.itemCount - 1) {
        HorizontalDivider(
          modifier = Modifier.fillParentMaxWidth().padding(horizontal = 24.dp),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    }
  }
}

@Composable
fun PostListItem(
  item: PostWithMetadata,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  onPostSourceClick: () -> Unit,
  togglePostReadClick: () -> Unit,
  reduceReadItemAlpha: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
) {
  Column(
    modifier =
      Modifier.clickable(onClick = onClick)
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        .padding(postListPadding)
        .alpha(if (item.read && reduceReadItemAlpha) 0.65f else 1f)
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 20.dp, end = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        modifier = Modifier.weight(1f).align(Alignment.Top),
        style = MaterialTheme.typography.titleMedium,
        text = item.title.ifBlank { item.description },
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      item.imageUrl?.let { url ->
        AsyncImage(
          url = url,
          modifier =
            Modifier.requiredSize(width = 128.dp, height = 72.dp)
              .clip(RoundedCornerShape(12.dp))
              .align(Alignment.CenterVertically),
          contentDescription = null,
          contentScale = ContentScale.Crop
        )
      }
    }

    PostMetadata(
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      config = postMetadataConfig,
      postLink = item.link,
      postRead = item.read,
      postBookmarked = item.bookmarked,
      commentsLink = item.commentsLink,
      onBookmarkClick = onPostBookmarkClick,
      onCommentsClick = onPostCommentsClick,
      onSourceClick = onPostSourceClick,
      onTogglePostReadClick = togglePostReadClick,
      modifier = Modifier.padding(start = 24.dp, end = 12.dp)
    )
  }
}
