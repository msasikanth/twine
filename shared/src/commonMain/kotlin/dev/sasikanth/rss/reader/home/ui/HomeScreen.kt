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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CompactFloatingActionButton
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Feed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

private val BOTTOM_SHEET_CORNER_SIZE = 36.dp

@Composable
internal fun HomeScreen(
  homePresenter: HomePresenter,
  useDarkTheme: Boolean = false,
  modifier: Modifier = Modifier,
  onBottomSheetStateChanged: (SheetValue) -> Unit,
  onBottomSheetHidden: (isHidden: Boolean) -> Unit,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by homePresenter.state.collectAsState()
  val feedsState by homePresenter.feedsPresenter.state.collectAsState()
  val linkHandler = LocalLinkHandler.current

  val posts = state.posts?.collectAsLazyPagingItems()
  val featuredPosts by featuredPosts(posts).collectAsState(initial = persistentListOf())

  val listState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { featuredPosts.size })
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
  val hasFeeds = state.hasFeeds
  val swipeRefreshState =
    rememberPullRefreshState(
      refreshing = state.isRefreshing,
      onRefresh = { homePresenter.dispatch(HomeEvent.OnSwipeToRefresh) }
    )
  val canSwipeToRefresh = hasFeeds == true

  Scaffold(
    modifier = modifier.pullRefresh(state = swipeRefreshState, enabled = canSwipeToRefresh),
    containerColor = AppTheme.colorScheme.backdrop,
    topBar = {
      HomeTopAppBar(
        source = state.activeSource,
        postsType = state.postsType,
        listState = listState,
        hasFeeds = hasFeeds,
        hasUnreadPosts = state.hasUnreadPosts,
        onSearchClicked = { homePresenter.dispatch(HomeEvent.SearchClicked) },
        onBookmarksClicked = { homePresenter.dispatch(HomeEvent.BookmarksClicked) },
        onSettingsClicked = { homePresenter.dispatch(HomeEvent.SettingsClicked) },
        onPostTypeChanged = { homePresenter.dispatch(HomeEvent.OnPostsTypeChanged(it)) },
        onMarkPostsAsRead = { homePresenter.dispatch(HomeEvent.MarkPostsAsRead(it)) }
      )
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize()) {
      when {
        hasFeeds == null || posts == null -> {
          // no-op
        }
        posts.itemCount > 0 -> {
          PostsList(
            paddingValues = innerPadding,
            featuredPosts = featuredPosts,
            posts = posts,
            useDarkTheme = useDarkTheme,
            listState = listState,
            featuredPostsPagerState = featuredPostsPagerState,
            markPostAsRead = { homePresenter.dispatch(HomeEvent.MarkFeaturedPostsAsRead(it)) },
            postsScrolled = { homePresenter.dispatch(HomeEvent.OnPostItemsScrolled(it)) },
            markScrolledPostsAsRead = { homePresenter.dispatch(HomeEvent.MarkScrolledPostsAsRead) },
            onPostClicked = { post, postIndex ->
              homePresenter.dispatch(HomeEvent.OnPostClicked(post, postIndex))
            },
            onPostBookmarkClick = { homePresenter.dispatch(HomeEvent.OnPostBookmarkClick(it)) },
            onPostCommentsClick = { commentsLink ->
              coroutineScope.launch { linkHandler.openLink(commentsLink) }
            },
            onPostSourceClick = { feedId ->
              homePresenter.dispatch(HomeEvent.OnPostSourceClicked(feedId))
            },
            onTogglePostReadClick = { postId, postRead ->
              homePresenter.dispatch(HomeEvent.TogglePostReadStatus(postId, postRead))
            }
          )
        }
        !hasFeeds -> {
          NoFeeds {
            // TODO: Feed management page
          }
        }
        featuredPosts.isEmpty() && posts.itemCount == 0 -> {
          NoNewPosts()
        }
      }

      PullRefreshIndicator(
        refreshing = state.isRefreshing,
        state = swipeRefreshState,
        modifier = Modifier.padding(innerPadding).align(Alignment.TopCenter)
      )

      val navBarScrimColor = AppTheme.colorScheme.backdrop
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
            .navigationBarsPadding()
            .padding(top = 24.dp)
            .align(Alignment.BottomCenter)
      )

      CompactFloatingActionButton(
        label = LocalStrings.current.scrollToTop,
        visible = showScrollToTop,
        modifier =
          Modifier.padding(
            end = 16.dp,
            bottom =
              innerPadding
                .calculateBottomPadding()
                .coerceAtLeast(innerPadding.calculateBottomPadding()) + 16.dp
          ),
      ) {
        listState.animateScrollToItem(0)
      }
    }
  }
}

@Composable
private fun NoFeeds(onNoFeedsSwipeUp: () -> Unit) {
  Column(
    modifier =
      Modifier.padding(horizontal = 16.dp).fillMaxSize().pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          if (dragAmount.y < 0) {
            onNoFeedsSwipeUp()
          }
        }
      },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = LocalStrings.current.noFeeds,
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = LocalStrings.current.swipeUpGetStarted,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Icon(
      imageVector = Icons.Rounded.KeyboardArrowUp,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
  }
}

@Composable
private fun NoNewPosts() {
  Column(
    modifier =
      Modifier.padding(horizontal = 16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = TwineIcons.Feed,
      contentDescription = null,
      tint = AppTheme.colorScheme.textEmphasisHigh,
      modifier = Modifier.requiredSize(80.dp)
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Text(
      text = LocalStrings.current.noNewPosts,
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = LocalStrings.current.noNewPostsSubtitle,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
fun featuredPosts(
  posts: LazyPagingItems<PostWithMetadata>?
): Flow<ImmutableList<FeaturedPostItem>> {
  val seedColorExtractor = LocalSeedColorExtractor.current
  return remember(posts?.loadState) {
    flow {
      if (posts == null || posts.itemCount == 0) {
        emit(persistentListOf())
        return@flow
      }

      val size = minOf(posts.itemCount, Constants.NUMBER_OF_FEATURED_POSTS.toInt())
      val mutablePostsList =
        MutableList(size) { index ->
          val post = posts[index]
          if (post != null && post.imageUrl.isNullOrBlank().not()) {
            FeaturedPostItem(
              postWithMetadata = post,
              seedColor = null,
            )
          } else {
            null
          }
        }

      emit(mutablePostsList.filterNotNull().toImmutableList())

      mutablePostsList.forEachIndexed { index, post ->
        post?.let {
          if (it.seedColor == null) {
            mutablePostsList[index] =
              it.copy(
                seedColor = seedColorExtractor.calculateSeedColor(it.postWithMetadata.imageUrl)
              )
          }
        }
      }

      emit(mutablePostsList.filterNotNull().toImmutableList())
    }
  }
}
