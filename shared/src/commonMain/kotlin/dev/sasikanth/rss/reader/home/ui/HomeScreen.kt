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

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.NewArticlesScrollToTopButton
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Feed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.inverse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.noFeeds
import twine.shared.generated.resources.noNewPosts
import twine.shared.generated.resources.noNewPostsSubtitle
import twine.shared.generated.resources.swipeUpGetStarted

internal val BOTTOM_SHEET_PEEK_HEIGHT = 96.dp
private val BOTTOM_SHEET_CORNER_SIZE = 32.dp

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
  val featuredPosts by
    featuredPosts(posts, state.homeViewMode).collectAsState(initial = persistentListOf())

  val postsListState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { featuredPosts.size })
  val bottomSheetState =
    rememberStandardBottomSheetState(
      initialValue = state.feedsSheetState,
      confirmValueChange = {
        if (it != SheetValue.Hidden) {
          homePresenter.dispatch(HomeEvent.FeedsSheetStateChanged(it))
        } else {
          homePresenter.dispatch(HomeEvent.FeedsSheetStateChanged(SheetValue.PartiallyExpanded))
        }

        onBottomSheetStateChanged(it)

        true
      }
    )
  val bottomSheetScaffoldState =
    rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

  val bottomSheetProgress by bottomSheetState.progressAsState()
  val showScrollToTop by remember { derivedStateOf { postsListState.firstVisibleItemIndex > 0 } }
  val unreadSinceLastSync = state.unreadSinceLastSync

  LaunchedEffect(Unit) {
    homePresenter.effects.collectLatest { effect ->
      when (effect) {
        is HomeEffect.ScrollPostListTo -> {
          if (effect.index < Constants.NUMBER_OF_FEATURED_POSTS) {
            featuredPostsPagerState.scrollToPage(effect.index)
          } else {
            // Since indexes start from 0, we are increasing the featured posts size by one
            val adjustedIndex = (effect.index - featuredPosts.size + 1).coerceAtLeast(0)
            postsListState.scrollToItem(adjustedIndex)
          }
        }
      }
    }
  }

  AppTheme(useDarkTheme = true) {
    Scaffold(modifier) { scaffoldPadding ->
      val bottomPadding = scaffoldPadding.calculateBottomPadding()
      val sheetPeekHeight by
        animateDpAsState(
          targetValue =
            if (postsListState.isScrollingUp()) {
              BOTTOM_SHEET_PEEK_HEIGHT + bottomPadding
            } else {
              0.dp
            },
          label = "Sheet Peek Height Animation"
        )
      val isBottomSheetHidden by remember { derivedStateOf { sheetPeekHeight == 0.dp } }

      LaunchedEffect(isBottomSheetHidden) { onBottomSheetHidden(isBottomSheetHidden) }

      BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        content = { bottomSheetScaffoldContentPadding ->
          AppTheme(useDarkTheme = useDarkTheme) {
            Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorScheme.backdrop)) {
              val hasFeeds = state.hasFeeds

              HomeScreenContentScaffold(
                homeTopAppBar = {
                  HomeTopAppBar(
                    source = state.activeSource,
                    currentDateTime = state.currentDateTime,
                    postsType = state.postsType,
                    listState = postsListState,
                    hasFeeds = hasFeeds,
                    hasUnreadPosts = state.hasUnreadPosts,
                    homeViewMode = state.homeViewMode,
                    onSearchClicked = { homePresenter.dispatch(HomeEvent.SearchClicked) },
                    onBookmarksClicked = { homePresenter.dispatch(HomeEvent.BookmarksClicked) },
                    onSettingsClicked = { homePresenter.dispatch(HomeEvent.SettingsClicked) },
                    onPostTypeChanged = {
                      homePresenter.dispatch(HomeEvent.OnPostsTypeChanged(it))
                    },
                    onMarkPostsAsRead = { homePresenter.dispatch(HomeEvent.MarkPostsAsRead(it)) },
                    onChangeHomeViewMode = {
                      homePresenter.dispatch(HomeEvent.ChangeHomeViewMode(it))
                    }
                  )
                },
                body = { paddingValues ->
                  Box(modifier = Modifier.fillMaxSize()) {
                    when {
                      hasFeeds == null || posts == null -> {
                        // no-op
                      }
                      !hasFeeds && posts.loadState.refresh is LoadState.NotLoading -> {
                        NoFeeds { coroutineScope.launch { bottomSheetState.expand() } }
                      }
                      featuredPosts.isEmpty() &&
                        posts.itemCount == 0 &&
                        posts.loadState.refresh is LoadState.NotLoading -> {
                        NoNewPosts()
                      }
                      else -> {
                        val pullToRefreshState = rememberPullToRefreshState()

                        PullToRefreshBox(
                          state = pullToRefreshState,
                          isRefreshing = state.isSyncing,
                          onRefresh = { homePresenter.dispatch(HomeEvent.OnSwipeToRefresh) },
                          indicator = {
                            Indicator(
                              modifier =
                                Modifier.align(Alignment.TopCenter)
                                  .padding(top = paddingValues.calculateTopPadding()),
                              isRefreshing = state.isSyncing,
                              containerColor = AppTheme.colorScheme.primaryContainer,
                              color = AppTheme.colorScheme.primary,
                              state = pullToRefreshState
                            )
                          }
                        ) {
                          PostsList(
                            modifier = Modifier.fillMaxSize(),
                            paddingValues = paddingValues,
                            featuredPosts = featuredPosts,
                            posts = posts,
                            useDarkTheme = useDarkTheme,
                            listState = postsListState,
                            featuredPostsPagerState = featuredPostsPagerState,
                            homeViewMode = state.homeViewMode,
                            markPostAsRead = {
                              homePresenter.dispatch(HomeEvent.MarkFeaturedPostsAsRead(it))
                            },
                            postsScrolled = {
                              homePresenter.dispatch(HomeEvent.OnPostItemsScrolled(it))
                            },
                            markScrolledPostsAsRead = {
                              homePresenter.dispatch(HomeEvent.MarkScrolledPostsAsRead)
                            },
                            onPostClicked = { post, postIndex ->
                              homePresenter.dispatch(HomeEvent.OnPostClicked(post, postIndex))
                            },
                            onPostBookmarkClick = {
                              homePresenter.dispatch(HomeEvent.OnPostBookmarkClick(it))
                            },
                            onPostCommentsClick = { commentsLink ->
                              coroutineScope.launch { linkHandler.openLink(commentsLink) }
                            },
                            onPostSourceClick = { feedId ->
                              homePresenter.dispatch(HomeEvent.OnPostSourceClicked(feedId))
                            },
                            onTogglePostReadClick = { postId, postRead ->
                              homePresenter.dispatch(
                                HomeEvent.TogglePostReadStatus(postId, postRead)
                              )
                            }
                          )
                        }
                      }
                    }
                  }
                },
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

              NewArticlesScrollToTopButton(
                unreadSinceLastSync = unreadSinceLastSync,
                canShowScrollToTop = showScrollToTop,
                modifier =
                  Modifier.padding(
                    end = 16.dp,
                    bottom =
                      bottomSheetScaffoldContentPadding
                        .calculateBottomPadding()
                        .coerceAtLeast(scaffoldPadding.calculateBottomPadding()) + 16.dp
                  ),
                onLoadNewArticlesClick = { homePresenter.dispatch(HomeEvent.LoadNewArticlesClick) },
              ) {
                postsListState.animateScrollToItem(0)
              }
            }
          }
        },
        sheetContent = {
          FeedsBottomSheet(
            feedsPresenter = homePresenter.feedsPresenter,
            bottomSheetProgress = bottomSheetProgress,
            closeSheet = { coroutineScope.launch { bottomSheetState.partialExpand() } },
            selectedFeedChanged = {
              coroutineScope.launch {
                postsListState.scrollToItem(0)
                featuredPostsPagerState.scrollToPage(0)
              }
            }
          )
        },
        containerColor = Color.Transparent,
        sheetContainerColor = AppTheme.colorScheme.tintedBackground,
        sheetContentColor = AppTheme.colorScheme.tintedForeground,
        sheetShadowElevation = 0.dp,
        sheetTonalElevation = 0.dp,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape =
          RoundedCornerShape(
            topStart = BOTTOM_SHEET_CORNER_SIZE * bottomSheetProgress.inverse(),
            topEnd = BOTTOM_SHEET_CORNER_SIZE * bottomSheetProgress.inverse()
          ),
        sheetSwipeEnabled = !feedsState.isInMultiSelectMode,
        sheetDragHandle = null
      )
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
      text = stringResource(Res.string.noFeeds),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.swipeUpGetStarted),
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
      text = stringResource(Res.string.noNewPosts),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.noNewPostsSubtitle),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Center
    )
  }
}

@Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
private fun SheetState.progressAsState(): State<Float> {
  return derivedStateOf {
    when {
      currentValue == SheetValue.Expanded && targetValue == SheetValue.Expanded -> 1f
      currentValue == SheetValue.Expanded && targetValue == SheetValue.PartiallyExpanded ->
        1f - anchoredDraggableState.progress
      currentValue == SheetValue.PartiallyExpanded && targetValue == SheetValue.PartiallyExpanded ->
        if (anchoredDraggableState.progress == 1f) 0f else anchoredDraggableState.progress
      currentValue == SheetValue.PartiallyExpanded && targetValue == SheetValue.Expanded ->
        anchoredDraggableState.progress
      else -> 0f
    }
  }
}

@Composable
fun featuredPosts(
  posts: LazyPagingItems<PostWithMetadata>?,
  homeViewMode: HomeViewMode
): Flow<ImmutableList<FeaturedPostItem>> {
  val seedColorExtractor = LocalSeedColorExtractor.current
  return remember(posts?.itemSnapshotList?.items, homeViewMode) {
    flow {
      if (homeViewMode != HomeViewMode.Default || posts == null || posts.itemCount == 0) {
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

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
  var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
  var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
  return remember(this) {
      derivedStateOf {
        if (previousIndex != firstVisibleItemIndex) {
            previousIndex > firstVisibleItemIndex
          } else {
            previousScrollOffset >= firstVisibleItemScrollOffset
          }
          .also {
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
          }
      }
    }
    .value
}
