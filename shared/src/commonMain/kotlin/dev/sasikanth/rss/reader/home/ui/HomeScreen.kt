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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.NewArticlesScrollToTopButton
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.sheet.BOTTOM_SHEET_PEEK_HEIGHT
import dev.sasikanth.rss.reader.feeds.ui.sheet.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeState
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Newsstand
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.noFeeds
import twine.shared.generated.resources.noNewPosts
import twine.shared.generated.resources.noNewPostsSubtitle
import twine.shared.generated.resources.swipeUpGetStarted

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
internal fun HomeScreen(
  viewModel: HomeViewModel,
  feedsViewModel: FeedsViewModel,
  onVisiblePostChanged: (Int) -> Unit,
  openSearch: () -> Unit,
  openBookmarks: () -> Unit,
  openSettings: () -> Unit,
  openPost: (Int, PostWithMetadata) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openFeedInfoSheet: (feedId: String) -> Unit,
  openAddFeedScreen: () -> Unit,
  openGroupScreen: (groupId: String) -> Unit,
  openPaywall: () -> Unit,
  onBottomSheetStateChanged: (SheetValue) -> Unit,
  modifier: Modifier = Modifier,
  useDarkTheme: Boolean = false,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val feedsState by feedsViewModel.state.collectAsStateWithLifecycle()
  val linkHandler = LocalLinkHandler.current
  val density = LocalDensity.current
  val dynamicColorState = LocalDynamicColorState.current

  val posts = state.posts?.collectAsLazyPagingItems()
  val featuredPosts by
    featuredPosts({ posts }, state.homeViewMode)
      .collectAsStateWithLifecycle(initialValue = persistentListOf())

  val postsListState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { featuredPosts.size })
  val bottomSheetState =
    rememberStandardBottomSheetState(
      initialValue = state.feedsSheetState,
      confirmValueChange = {
        if (it != SheetValue.Hidden) {
          viewModel.dispatch(HomeEvent.FeedsSheetStateChanged(it))
        } else {
          viewModel.dispatch(HomeEvent.FeedsSheetStateChanged(SheetValue.PartiallyExpanded))
        }

        onBottomSheetStateChanged(it)

        true
      }
    )
  val bottomSheetScaffoldState =
    rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

  val showScrollToTop by remember { derivedStateOf { postsListState.firstVisibleItemIndex > 0 } }
  val unreadSinceLastSync = state.unreadSinceLastSync

  LaunchedEffect(state.activeSource) {
    if (state.activeSource != state.prevActiveSource) {
      bottomSheetState.partialExpand()

      viewModel.dispatch(HomeEvent.UpdatePrevActiveSource(state.activeSource))
      viewModel.dispatch(HomeEvent.UpdateVisibleItemIndex(0))
    }
  }

  LaunchedEffect(featuredPostsPagerState, featuredPosts) {
    if (featuredPosts.isEmpty()) return@LaunchedEffect

    snapshotFlow {
        runCatching {
            val settledPage = featuredPostsPagerState.settledPage
            featuredPostsPagerState.getOffsetFractionForPage(settledPage)
          }
          .getOrNull()
          ?: 0f
      }
      .collect { offset ->
        // The default snap position of the pager is 0.5f, that means the targetPage
        // state only changes after reaching half way point. We instead want it to scale
        // as we start swiping.
        //
        // Instead of using EPSILON for snap threshold, we are doing that calculation
        // as the page offset changes
        //
        val settledPage = featuredPostsPagerState.settledPage
        val activePost = runCatching { featuredPosts[settledPage] }.getOrNull()

        if (activePost == null) return@collect

        val fromItem =
          when {
            offset < -EPSILON -> {
              runCatching { featuredPosts[settledPage - 1] }.getOrNull() ?: activePost
            }
            else -> activePost
          }
        val toItem =
          when {
            offset > EPSILON -> {
              runCatching { featuredPosts[settledPage + 1] }.getOrNull() ?: activePost
            }
            else -> activePost
          }

        val fromSeedColor = fromItem.seedColor?.let { Color(it) }
        val toSeedColor = toItem.seedColor?.let { Color(it) }

        dynamicColorState.animate(
          fromSeedColor = fromSeedColor,
          toSeedColor = toSeedColor,
          progress = offset
        )
      }
  }

  BackHandler(
    enabled = state.feedsSheetState == SheetValue.Expanded && !(feedsState.isInMultiSelectMode),
    onBack = { coroutineScope.launch { bottomSheetState.partialExpand() } }
  )

  Scaffold(modifier) { scaffoldPadding ->
    val sheetPeekHeight by
      animateDpAsState(
        targetValue =
          if (postsListState.isScrollingTowardsUp()) {
            BOTTOM_SHEET_PEEK_HEIGHT + scaffoldPadding.calculateBottomPadding().coerceAtLeast(16.dp)
          } else {
            0.dp
          },
        label = "Sheet Peek Height Animation",
      )

    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      content = { bottomSheetScaffoldContentPadding ->
        Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorScheme.backdrop)) {
          val hasFeeds = state.hasFeeds
          val appBarScrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()

          HomeScreenContentScaffold(
            modifier = Modifier.nestedScroll(appBarScrollBehaviour.nestedScrollConnection),
            homeTopAppBar = {
              HomeTopAppBar(
                source = state.activeSource,
                postsType = state.postsType,
                listState = postsListState,
                hasUnreadPosts = state.hasUnreadPosts,
                scrollBehavior = appBarScrollBehaviour,
                onSearchClicked = openSearch,
                onBookmarksClicked = openBookmarks,
                onSettingsClicked = openSettings,
                onPostTypeChanged = { viewModel.dispatch(HomeEvent.OnPostsTypeChanged(it)) },
                onMarkPostsAsRead = { viewModel.dispatch(HomeEvent.MarkPostsAsRead(it)) }
              )
            },
            body = { paddingValues ->
              val topOffset =
                remember(paddingValues, featuredPosts) {
                  val topPaddingPx =
                    with(density) { paddingValues.calculateTopPadding().roundToPx() }
                  if (featuredPosts.isEmpty()) {
                    postsListState.layoutInfo.beforeContentPadding
                  } else {
                    topPaddingPx
                  }
                }

              LaunchedEffect(state.activePostIndex, featuredPosts.isNotEmpty()) {
                if (featuredPosts.isEmpty()) {
                  return@LaunchedEffect
                }

                val activePostIndex = state.activePostIndex
                val numberOfFeaturedPosts = featuredPosts.size

                if (activePostIndex < numberOfFeaturedPosts && numberOfFeaturedPosts > 0) {
                  postsListState.scrollToItem(0)
                  featuredPostsPagerState.scrollToPage(activePostIndex)
                } else {
                  // Since indexes start from 0, we are increasing the featured posts size by one
                  val featuredPostsLastIndex = (numberOfFeaturedPosts - 1).coerceAtLeast(0)
                  val adjustedIndex = (activePostIndex - featuredPostsLastIndex).coerceAtLeast(0)

                  // Since we apply top content padding to the LazyColumn, we are offsetting
                  // the scroll so that the actual item is visible at top of the page for user.
                  postsListState.scrollToItem(adjustedIndex, scrollOffset = topOffset.unaryMinus())
                }
              }

              LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) {
                val firstVisibleItemIndexAfterOffset =
                  postsListState.layoutInfo.visibleItemsInfo
                    .firstOrNull { itemInfo ->
                      itemInfo.offset >= topOffset || itemInfo.offset == 0
                    }
                    ?.index
                    ?: 0

                val adjustedIndex =
                  if (firstVisibleItemIndexAfterOffset == 0) {
                    featuredPostsPagerState.settledPage
                  } else {
                    firstVisibleItemIndexAfterOffset + featuredPosts.lastIndex.coerceAtLeast(0)
                  }

                onVisiblePostChanged(adjustedIndex)
              }

              Box(modifier = Modifier.fillMaxSize()) {
                val pullToRefreshState = rememberPullToRefreshState()

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
                    PullToRefreshContent(
                      pullToRefreshState = pullToRefreshState,
                      state = state,
                      paddingValues = paddingValues,
                      onRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) }
                    ) {
                      NoNewPosts()
                    }
                  }
                  else -> {
                    PullToRefreshContent(
                      pullToRefreshState = pullToRefreshState,
                      state = state,
                      paddingValues = paddingValues,
                      onRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) }
                    ) {
                      PostsList(
                        modifier = Modifier.fillMaxSize(),
                        paddingValues = paddingValues,
                        featuredPosts = featuredPosts,
                        posts = { posts },
                        useDarkTheme = useDarkTheme,
                        listState = postsListState,
                        featuredPostsPagerState = featuredPostsPagerState,
                        homeViewMode = state.homeViewMode,
                        markPostAsReadOnScroll = {
                          viewModel.dispatch(HomeEvent.MarkFeaturedPostsAsRead(it))
                        },
                        postsScrolled = { viewModel.dispatch(HomeEvent.OnPostItemsScrolled(it)) },
                        markScrolledPostsAsRead = {
                          viewModel.dispatch(HomeEvent.MarkScrolledPostsAsRead)
                        },
                        onPostClicked = { post, postIndex -> openPost(postIndex, post) },
                        onPostBookmarkClick = {
                          viewModel.dispatch(HomeEvent.OnPostBookmarkClick(it))
                        },
                        onPostCommentsClick = { commentsLink ->
                          coroutineScope.launch { linkHandler.openLink(commentsLink) }
                        },
                        onPostSourceClick = { feedId ->
                          viewModel.dispatch(HomeEvent.OnPostSourceClicked(feedId))
                        },
                        updateReadStatus = { postId, updatedReadStatus ->
                          viewModel.dispatch(
                            HomeEvent.UpdatePostReadStatus(postId, updatedReadStatus)
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
          AnimatedVisibility(
            modifier =
              Modifier.fillMaxWidth()
                .requiredHeight(BOTTOM_SHEET_PEEK_HEIGHT)
                .align(Alignment.BottomCenter),
            visible = postsListState.isScrollingTowardsUp(),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
          ) {
            Box(
              modifier =
                Modifier.matchParentSize()
                  .background(Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
            )
          }

          NewArticlesScrollToTopButton(
            unreadSinceLastSync = unreadSinceLastSync,
            canShowScrollToTop = showScrollToTop,
            modifier =
              Modifier.padding(
                bottom =
                  bottomSheetScaffoldContentPadding
                    .calculateBottomPadding()
                    .coerceAtLeast(scaffoldPadding.calculateBottomPadding())
              ),
            onLoadNewArticlesClick = { viewModel.dispatch(HomeEvent.LoadNewArticlesClick) },
          ) {
            postsListState.animateScrollToItem(0)
          }
        }

        if (bottomSheetState.currentValue == SheetValue.Expanded) {
          Box(
            modifier =
              Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { coroutineScope.launch { bottomSheetState.partialExpand() } }
              }
          )
        }
      },
      sheetContent = {
        FeedsBottomSheet(
          feedsViewModel = feedsViewModel,
          darkTheme = useDarkTheme,
          bottomSheetProgress =
            @Suppress("INVISIBLE_REFERENCE") {
              bottomSheetState.anchoredDraggableState.progress(
                SheetValue.PartiallyExpanded,
                SheetValue.Expanded
              )
            },
          openFeedInfoSheet = openFeedInfoSheet,
          openGroupScreen = openGroupScreen,
          openGroupSelectionSheet = openGroupSelectionSheet,
          openAddFeedScreen = openAddFeedScreen,
          openPaywall = openPaywall,
        )
      },
      containerColor = Color.Transparent,
      sheetContainerColor = Color.Transparent,
      sheetContentColor = Color.Unspecified,
      sheetShadowElevation = 0.dp,
      sheetTonalElevation = 0.dp,
      sheetPeekHeight = sheetPeekHeight,
      sheetShape = RectangleShape,
      sheetSwipeEnabled = !feedsState.isInMultiSelectMode,
      sheetDragHandle = null
    )
  }
}

@Composable
private fun PullToRefreshContent(
  pullToRefreshState: PullToRefreshState,
  state: HomeState,
  paddingValues: PaddingValues,
  onRefresh: () -> Unit,
  content: @Composable () -> Unit,
) {
  PullToRefreshBox(
    state = pullToRefreshState,
    isRefreshing = state.isSyncing,
    onRefresh = onRefresh,
    indicator = {
      Indicator(
        modifier =
          Modifier.align(Alignment.TopCenter).padding(top = paddingValues.calculateTopPadding()),
        isRefreshing = state.isSyncing,
        containerColor = AppTheme.colorScheme.primaryContainer,
        color = AppTheme.colorScheme.primary,
        state = pullToRefreshState
      )
    }
  ) {
    content()
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
      imageVector = TwineIcons.Newsstand,
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

@Composable
fun featuredPosts(
  posts: () -> LazyPagingItems<PostWithMetadata>?,
  homeViewMode: HomeViewMode,
): Flow<ImmutableList<FeaturedPostItem>> {
  val seedColorExtractor = LocalSeedColorExtractor.current
  val shouldBlockImage = LocalBlockImage.current
  val posts = posts.invoke()

  if (shouldBlockImage) return emptyFlow()

  return remember(posts?.itemSnapshotList?.hashCode(), homeViewMode) {
    flow {
      if (homeViewMode != HomeViewMode.Default || posts == null || posts.itemCount == 0) {
        emit(persistentListOf())
        return@flow
      }

      val featuredPostsCount = minOf(posts.itemCount, Constants.NUMBER_OF_FEATURED_POSTS.toInt())
      val mutablePostsList =
        MutableList(featuredPostsCount) { index ->
          val post = posts[index]
          if (post != null && post.imageUrl.isNullOrBlank().not()) {
            val existingSeedColor = seedColorExtractor.cachedSeedColor(post.imageUrl)

            FeaturedPostItem(
              postWithMetadata = post,
              seedColor = existingSeedColor,
            )
          } else {
            null
          }
        }

      emit(mutablePostsList.filterNotNull().toImmutableList())

      val updatedPosts =
        mutablePostsList
          .mapNotNull { item ->
            if (item == null) return@mapNotNull null

            if (item.seedColor != null) return@mapNotNull item

            return@mapNotNull if (!item.postWithMetadata.imageUrl.isNullOrBlank()) {
              val seedColor = seedColorExtractor.calculateSeedColor(item.postWithMetadata.imageUrl)
              item.copy(seedColor = seedColor)
            } else {
              null
            }
          }
          .toImmutableList()

      emit(updatedPosts)
    }
  }
}

@Composable
private fun LazyListState.isScrollingTowardsUp(): Boolean {
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
