/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
import dev.sasikanth.rss.reader.core.model.local.FeaturedPostItem
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
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
  listState: LazyListState,
  featuredPostsPagerState: PagerState,
  homeViewMode: HomeViewMode,
  posts: () -> LazyPagingItems<ResolvedPost>,
  postsScrolled: (List<String>) -> Unit,
  markScrolledPostsAsRead: () -> Unit,
  markPostAsReadOnScroll: (String) -> Unit,
  onPostClicked: (post: ResolvedPost, postIndex: Int) -> Unit,
  onFeaturedPostClicked: (post: ResolvedPost) -> Unit,
  onPostBookmarkClick: (ResolvedPost) -> Unit,
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
        val postIds: List<String> =
          items.mapNotNull {
            val keyString = (it.key as? String)
            if (keyString.isNullOrBlank()) {
              null
            } else {
              PostListKey.decode(keyString).postId
            }
          }
        postsScrolled(postIds)
      }
      .debounce(2.seconds)
      .collect { markScrolledPostsAsRead() }
  }

  LazyColumn(
    modifier = modifier,
    state = listState,
    contentPadding =
      PaddingValues(top = topContentPadding, bottom = BOTTOM_SHEET_PEEK_HEIGHT + 120.dp),
  ) {
    if (featuredPosts.isNotEmpty()) {
      item(contentType = "featured_items") {
        FeaturedSection(
          paddingValues = paddingValues,
          featuredPosts = featuredPosts,
          pagerState = featuredPostsPagerState,
          markPostAsReadOnScroll = markPostAsReadOnScroll,
          onItemClick = { post, _ -> onFeaturedPostClicked(post) },
          onPostBookmarkClick = onPostBookmarkClick,
          onPostCommentsClick = onPostCommentsClick,
          onPostSourceClick = onPostSourceClick,
          updateReadStatus = updateReadStatus,
        )
      }
    }

    val posts = posts.invoke()
    items(
      count = posts.itemCount,
      key = { index ->
        val post = posts.peek(index)

        if (post != null) {
          PostListKey.from(post).encode()
        } else {
          index
        }
      },
      contentType = { "post_item" },
    ) { index ->
      val post = posts[index] ?: return@items

      when (homeViewMode) {
        HomeViewMode.Default,
        HomeViewMode.Simple -> {
          PostListItem(
            item = post,
            onClick = { onPostClicked(post, index) },
            onPostBookmarkClick = { onPostBookmarkClick(post) },
            onPostCommentsClick = { onPostCommentsClick(post.commentsLink!!) },
            onPostSourceClick = { onPostSourceClick(post.sourceId) },
            updatePostReadStatus = { updatedReadStatus ->
              updateReadStatus(post.id, updatedReadStatus)
            },
            reduceReadItemAlpha = true,
          )
        }
        HomeViewMode.Compact -> {
          CompactPostListItem(
            item = post,
            showDivider = index != posts.itemCount - 1,
            onClick = { onPostClicked(post, index) },
            onPostBookmarkClick = { onPostBookmarkClick(post) },
            onPostCommentsClick = { onPostCommentsClick(post.commentsLink!!) },
            updatePostReadStatus = { updatedReadStatus ->
              updateReadStatus(post.id, updatedReadStatus)
            },
            reduceReadItemAlpha = true,
          )
        }
      }
    }
  }
}
