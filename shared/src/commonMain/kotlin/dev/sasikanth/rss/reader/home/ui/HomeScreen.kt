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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CompactFloatingActionButton
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Feed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.inverse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

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

  val listState = rememberLazyListState()
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
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

  AppTheme(useDarkTheme = true) {
    Scaffold(modifier) { scaffoldPadding ->
      val density = LocalDensity.current
      val bottomPadding = scaffoldPadding.calculateBottomPadding()
      val targetSheetPeekHeight =
        remember(bottomPadding) { BOTTOM_SHEET_PEEK_HEIGHT + bottomPadding }
      var sheetPeekHeight by
        remember(targetSheetPeekHeight) { mutableStateOf(targetSheetPeekHeight) }

      // Since `animateScrollToItem` doesn't trigger nested scroll connection
      // we are manually animating the sheet peek height back to target sheet peek height
      var scrollToTopClicked by remember { mutableStateOf(false) }
      val scrollToTopAnimatedSheetPeekHeight by
        animateDpAsState(
          targetValue = if (scrollToTopClicked) targetSheetPeekHeight else 0.dp,
          finishedListener = { scrollToTopClicked = false }
        )

      LaunchedEffect(scrollToTopAnimatedSheetPeekHeight) {
        if (scrollToTopClicked) {
          sheetPeekHeight = scrollToTopAnimatedSheetPeekHeight
        }
      }

      LaunchedEffect(sheetPeekHeight) { onBottomSheetHidden(sheetPeekHeight == 0.dp) }

      val nestedScrollConnection = remember {
        object : NestedScrollConnection {
          override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
          ): Offset {
            val delta = consumed.y.toInt()
            val sheetPeekHeightInPx = with(density) { sheetPeekHeight.roundToPx() }
            val newSheetPeekHeight = sheetPeekHeightInPx + delta * 2

            sheetPeekHeight =
              with(density) {
                newSheetPeekHeight.coerceIn(0, targetSheetPeekHeight.roundToPx()).toDp()
              }

            return Offset.Zero
          }
        }
      }

      BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        content = { bottomSheetScaffoldContentPadding ->
          AppTheme(useDarkTheme = useDarkTheme) {
            Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorScheme.backdrop)) {
              val hasFeeds = state.hasFeeds
              val swipeRefreshState =
                rememberPullRefreshState(
                  refreshing = state.isRefreshing,
                  onRefresh = { homePresenter.dispatch(HomeEvent.OnSwipeToRefresh) }
                )
              val canSwipeToRefresh = hasFeeds == true

              HomeScreenContentScaffold(
                modifier =
                  Modifier.pullRefresh(state = swipeRefreshState, enabled = canSwipeToRefresh),
                homeTopAppBar = {
                  HomeTopAppBar(
                    source = state.activeSource,
                    currentDateTime = state.currentDateTime,
                    postsType = state.postsType,
                    listState = listState,
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
                      posts.itemCount > 0 -> {
                        PostsList(
                          modifier = Modifier.nestedScroll(nestedScrollConnection),
                          paddingValues = paddingValues,
                          featuredPosts = featuredPosts,
                          posts = posts,
                          useDarkTheme = useDarkTheme,
                          listState = listState,
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
                            homePresenter.dispatch(HomeEvent.TogglePostReadStatus(postId, postRead))
                          }
                        )
                      }
                      !hasFeeds -> {
                        NoFeeds { coroutineScope.launch { bottomSheetState.expand() } }
                      }
                      featuredPosts.isEmpty() && posts.itemCount == 0 -> {
                        NoNewPosts()
                      }
                    }

                    PullRefreshIndicator(
                      refreshing = state.isRefreshing,
                      state = swipeRefreshState,
                      modifier = Modifier.padding(paddingValues).align(Alignment.TopCenter)
                    )
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

              CompactFloatingActionButton(
                label = LocalStrings.current.scrollToTop,
                visible = showScrollToTop,
                modifier =
                  Modifier.padding(
                    end = 16.dp,
                    bottom =
                      bottomSheetScaffoldContentPadding
                        .calculateBottomPadding()
                        .coerceAtLeast(scaffoldPadding.calculateBottomPadding()) + 16.dp
                  ),
              ) {
                scrollToTopClicked = true
                listState.animateScrollToItem(0)
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
                listState.scrollToItem(0)
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
  return remember(posts?.loadState, homeViewMode) {
    flow {
      if (homeViewMode != HomeViewMode.Default) {
        emit(persistentListOf())
        return@flow
      }

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
