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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.NewArticlesScrollToTopButton
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.sheet.BOTTOM_SHEET_PEEK_HEIGHT
import dev.sasikanth.rss.reader.feeds.ui.sheet.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeState
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Newsstand
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.SYSTEM_SCRIM
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalDynamicColorEnabled
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
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
  onVisiblePostChanged: (Int, String?) -> Unit,
  openPost: (Int, ResolvedPost) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openFeedInfoSheet: (feedId: String) -> Unit,
  openAddFeedScreen: () -> Unit,
  openGroupScreen: (groupId: String) -> Unit,
  openPaywall: () -> Unit,
  onMenuClicked: (() -> Unit)? = null,
  onBottomSheetStateChanged: (SheetValue) -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val feedsState by feedsViewModel.state.collectAsStateWithLifecycle()
  val linkHandler = LocalLinkHandler.current
  val density = LocalDensity.current
  val dynamicColorState = LocalDynamicColorState.current
  val dynamicColorEnabled = LocalDynamicColorEnabled.current
  val sizeClass = LocalWindowSizeClass.current
  val shouldBlockImage = LocalBlockImage.current

  LaunchedEffect(Unit) { viewModel.openPost.collect { (index, post) -> openPost(index, post) } }

  val posts =
    remember(state.homeViewMode, state.allPosts, state.feedPosts, shouldBlockImage, sizeClass) {
        val forceShowAllPosts =
          shouldBlockImage ||
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

        if (state.homeViewMode == HomeViewMode.Default && !forceShowAllPosts) {
          state.feedPosts
        } else {
          state.allPosts
        }
      }
      ?.collectAsLazyPagingItems()
  val featuredPosts by
    remember(state.featuredPosts, dynamicColorEnabled, sizeClass, shouldBlockImage) {
        if (
          shouldBlockImage ||
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)
        ) {
          flowOf(persistentListOf())
        } else {
          state.featuredPosts
        }
      }
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
      },
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

  if (state.showPostsSortFilter) {
    PostsPreferencesSheet(
      postsType = state.postsType,
      postsSortOrder = state.postsSortOrder,
      onApply = { postsType, postsSortOrder ->
        viewModel.dispatch(HomeEvent.OnPostsSortFilterApplied(postsType, postsSortOrder))
      },
      onDismiss = { viewModel.dispatch(HomeEvent.ShowPostsSortFilter(show = false)) },
    )
  }

  featuredPostsPagerState.CollectItemTransition(
    key = featuredPosts,
    itemProvider = { index -> featuredPosts.getOrNull(index) },
  ) { fromItem, toItem, offset ->
    val fromSeedColor = fromItem?.seedColor?.let { Color(it) }
    val toSeedColor = toItem?.seedColor?.let { Color(it) }

    if (dynamicColorEnabled) {
      dynamicColorState.animate(
        fromSeedColor = fromSeedColor,
        toSeedColor = toSeedColor,
        progress = offset,
      )
    }
  }

  BackHandler(
    enabled = state.feedsSheetState == SheetValue.Expanded && !(feedsState.isInMultiSelectMode),
    onBack = { coroutineScope.launch { bottomSheetState.partialExpand() } },
  )

  Scaffold(
    modifier =
      modifier.onPreviewKeyEvent { event ->
        println(event.key)
        when {
          event.isMetaPressed && event.key == Key.R && event.type == KeyEventType.KeyUp -> {
            viewModel.dispatch(HomeEvent.OnSwipeToRefresh)
            true
          }
          else -> false
        }
      }
  ) { scaffoldPadding ->
    val sheetPeekHeight by
      animateDpAsState(
        targetValue =
          if (postsListState.isScrollingTowardsUp()) {
            val scaffoldBottomPadding =
              if (platform == Platform.Android) {
                scaffoldPadding.calculateBottomPadding().coerceAtLeast(16.dp)
              } else {
                12.dp
              }
            BOTTOM_SHEET_PEEK_HEIGHT + scaffoldBottomPadding
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
          val nestedScrollModifier =
            if (platform !is Platform.Desktop) {
              Modifier.nestedScroll(appBarScrollBehaviour.nestedScrollConnection)
            } else {
              Modifier
            }

          HomeScreenContentScaffold(
            modifier = Modifier.then(nestedScrollModifier),
            homeTopAppBar = {
              val scrollBehavior =
                if (platform !is Platform.Desktop) {
                  appBarScrollBehaviour
                } else {
                  null
                }

              HomeTopAppBar(
                source = state.activeSource,
                postsType = state.postsType,
                listState = postsListState,
                hasUnreadPosts = state.hasUnreadPosts,
                scrollBehavior = scrollBehavior,
                onMenuClicked = onMenuClicked,
                onShowPostsSortFilter = { viewModel.dispatch(HomeEvent.ShowPostsSortFilter(true)) },
                onMarkPostsAsRead = { viewModel.dispatch(HomeEvent.MarkPostsAsRead(it)) },
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
                val firstVisibleItemInfoAfterOffset =
                  postsListState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                    itemInfo.offset >= topOffset || itemInfo.offset == 0
                  }
                val firstVisibleItemIndexAfterOffset = firstVisibleItemInfoAfterOffset?.index ?: 0

                val (adjustedIndex, postId) =
                  if (featuredPosts.isEmpty()) {
                    val postId =
                      (firstVisibleItemInfoAfterOffset?.key as? String)?.let {
                        PostListKey.decode(it).postId
                      }
                    firstVisibleItemIndexAfterOffset to postId
                  } else if (firstVisibleItemIndexAfterOffset == 0) {
                    val settledPage = featuredPostsPagerState.settledPage
                    val postId = featuredPosts.getOrNull(settledPage)?.resolvedPost?.id
                    settledPage to postId
                  } else {
                    val postId =
                      (firstVisibleItemInfoAfterOffset?.key as? String)?.let {
                        PostListKey.decode(it).postId
                      }
                    (firstVisibleItemIndexAfterOffset + featuredPosts.lastIndex.coerceAtLeast(0)) to
                      postId
                  }

                onVisiblePostChanged(adjustedIndex, postId)
              }

              val pullToRefreshState = rememberPullToRefreshState()

              when {
                hasFeeds == null || posts == null -> {
                  // no-op
                }
                !hasFeeds -> {
                  NoFeeds { coroutineScope.launch { bottomSheetState.expand() } }
                }
                featuredPosts.isEmpty() && posts.itemCount == 0 -> {
                  PullToRefreshContent(
                    pullToRefreshState = pullToRefreshState,
                    state = state,
                    paddingValues = paddingValues,
                    onRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) },
                  ) {
                    NoNewPosts()
                  }
                }
                else -> {
                  PullToRefreshContent(
                    pullToRefreshState = pullToRefreshState,
                    state = state,
                    paddingValues = paddingValues,
                    onRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) },
                  ) {
                    PostsList(
                      paddingValues = paddingValues,
                      featuredPosts = featuredPosts,
                      listState = postsListState,
                      featuredPostsPagerState = featuredPostsPagerState,
                      homeViewMode = state.homeViewMode,
                      posts = { posts },
                      postsScrolled = { viewModel.dispatch(HomeEvent.OnPostItemsScrolled(it)) },
                      markScrolledPostsAsRead = {
                        viewModel.dispatch(HomeEvent.MarkScrolledPostsAsRead)
                      },
                      markPostAsReadOnScroll = {
                        viewModel.dispatch(HomeEvent.MarkFeaturedPostsAsRead(it))
                      },
                      onPostClicked = { post, _ ->
                        viewModel.dispatch(HomeEvent.OnPostClicked(post))
                      },
                      onFeaturedPostClicked = { post ->
                        viewModel.dispatch(HomeEvent.OnPostClicked(post))
                      },
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
                      },
                      modifier = Modifier.fillMaxSize(),
                    )
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
            exit = slideOutVertically { it },
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

        val bottomSheetScrimPointerInput =
          if (bottomSheetState.currentValue == SheetValue.Expanded) {
            Modifier.pointerInput(Unit) {
              detectTapGestures { coroutineScope.launch { bottomSheetState.partialExpand() } }
            }
          } else {
            Modifier
          }

        Box(
          modifier =
            Modifier.fillMaxSize()
              .drawBehind @Suppress("INVISIBLE_REFERENCE") {
                val bottomSheetProgress =
                  bottomSheetState.anchoredDraggableState.progress(
                    SheetValue.PartiallyExpanded,
                    SheetValue.Expanded,
                  )

                drawRect(color = SYSTEM_SCRIM, alpha = bottomSheetProgress)
              }
              .then(bottomSheetScrimPointerInput)
        )
      },
      sheetContent = {
        FeedsBottomSheet(
          feedsViewModel = feedsViewModel,
          bottomSheetProgress =
            @Suppress("INVISIBLE_REFERENCE") {
              bottomSheetState.anchoredDraggableState.progress(
                SheetValue.PartiallyExpanded,
                SheetValue.Expanded,
              )
            },
          openFeedInfoSheet = openFeedInfoSheet,
          openGroupScreen = openGroupScreen,
          openGroupSelectionSheet = openGroupSelectionSheet,
          openAddFeedScreen = openAddFeedScreen,
          openPaywall = openPaywall,
          openFeeds = { coroutineScope.launch { bottomSheetState.expand() } },
          closeFeeds = { coroutineScope.launch { bottomSheetState.partialExpand() } },
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
      sheetDragHandle = null,
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
  if (platform == Platform.Desktop) {
    Box(modifier = Modifier.fillMaxSize()) { content() }
  } else {
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
          state = pullToRefreshState,
        )
      },
    ) {
      content()
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
    verticalArrangement = Arrangement.Center,
  ) {
    Text(
      text = stringResource(Res.string.noFeeds),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.swipeUpGetStarted),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Icon(
      imageVector = Icons.Rounded.KeyboardArrowUp,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground,
    )
  }
}

@Composable
private fun NoNewPosts() {
  Column(
    modifier =
      Modifier.padding(horizontal = 16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      imageVector = TwineIcons.Newsstand,
      contentDescription = null,
      tint = AppTheme.colorScheme.textEmphasisHigh,
      modifier = Modifier.requiredSize(80.dp),
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Text(
      text = stringResource(Res.string.noNewPosts),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.noNewPostsSubtitle),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
private fun LazyListState.isScrollingTowardsUp(): Boolean {
  var isScrollingTowardsUp by remember(this) { mutableStateOf(true) }
  var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
  var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

  LaunchedEffect(this) {
    snapshotFlow { firstVisibleItemIndex to firstVisibleItemScrollOffset }
      .collect { (currentIndex, currentScrollOffset) ->
        val isScrollingUp =
          if (previousIndex != currentIndex) {
            previousIndex > currentIndex
          } else {
            previousScrollOffset >= currentScrollOffset
          }

        isScrollingTowardsUp = isScrollingUp
        previousIndex = currentIndex
        previousScrollOffset = currentScrollOffset
      }
  }

  return isScrollingTowardsUp
}
