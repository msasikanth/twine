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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.ui.sheet.BOTTOM_SHEET_PEEK_HEIGHT
import kotlin.time.Duration.Companion.seconds
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach

@OptIn(FlowPreview::class)
@Composable
internal fun PostsList(
  paddingValues: PaddingValues,
  featuredPosts: ImmutableList<FeaturedPostItem>,
  useDarkTheme: Boolean,
  listState: LazyListState,
  featuredPostsPagerState: PagerState,
  homeViewMode: HomeViewMode,
  posts: () -> LazyPagingItems<PostWithMetadata>,
  postsScrolled: (List<String>) -> Unit,
  markScrolledPostsAsRead: () -> Unit,
  markPostAsReadOnScroll: (String) -> Unit,
  onPostClicked: (post: PostWithMetadata, postIndex: Int) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
  updateReadStatus: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val topContentPadding =
    if (featuredPosts.isEmpty()) {
      paddingValues.calculateTopPadding()
    } else {
      0.dp
    }

  LaunchedEffect(listState) {
    snapshotFlow { listState.layoutInfo.visibleItemsInfo }
      .onEach { items ->
        val postIds =
          items
            .filter { it.contentType == "post_item" && it.key is String }
            .map { it.key as String }

        postsScrolled(postIds)
      }
      .debounce(2.seconds)
      .collect { markScrolledPostsAsRead() }
  }

  LazyColumn(
    modifier = modifier,
    state = listState,
    contentPadding =
      PaddingValues(top = topContentPadding, bottom = BOTTOM_SHEET_PEEK_HEIGHT + 120.dp)
  ) {
    if (featuredPosts.isNotEmpty()) {
      item(contentType = "featured_items") {
        FeaturedSection(
          paddingValues = paddingValues,
          pagerState = featuredPostsPagerState,
          featuredPosts = featuredPosts,
          useDarkTheme = useDarkTheme,
          markPostAsReadOnScroll = markPostAsReadOnScroll,
          onItemClick = onPostClicked,
          onPostBookmarkClick = onPostBookmarkClick,
          onPostCommentsClick = onPostCommentsClick,
          onPostSourceClick = onPostSourceClick,
          updateReadStatus = updateReadStatus,
        )
      }
    }

    val posts = posts.invoke()
    items(
      count = (posts.itemCount - featuredPosts.size).coerceAtLeast(0),
      key = { index ->
        val adjustedIndex = index + featuredPosts.size
        val post = posts.peek(adjustedIndex)
        post?.id ?: adjustedIndex
      },
      contentType = { "post_item" }
    ) { index ->
      val adjustedIndex = index + featuredPosts.size
      val post = posts[adjustedIndex] ?: return@items

      when (homeViewMode) {
        HomeViewMode.Default,
        HomeViewMode.Simple -> {
          PostListItem(
            item = post,
            darkTheme = useDarkTheme,
            reduceReadItemAlpha = true,
            onClick = { onPostClicked(post, adjustedIndex) },
            onPostBookmarkClick = { onPostBookmarkClick(post) },
            onPostCommentsClick = { onPostCommentsClick(post.commentsLink!!) },
            onPostSourceClick = { onPostSourceClick(post.sourceId) },
            updatePostReadStatus = { updatedReadStatus ->
              updateReadStatus(post.id, updatedReadStatus)
            }
          )
        }
        HomeViewMode.Compact -> {
          CompactPostListItem(
            item = post,
            reduceReadItemAlpha = true,
            darkTheme = useDarkTheme,
            showDivider = index != posts.itemCount - 1,
            onClick = { onPostClicked(post, adjustedIndex) },
            onPostBookmarkClick = { onPostBookmarkClick(post) },
            onPostCommentsClick = { onPostCommentsClick(post.commentsLink!!) },
            updatePostReadStatus = { updatedReadStatus ->
              updateReadStatus(post.id, updatedReadStatus)
            }
          )
        }
      }
    }
  }
}
