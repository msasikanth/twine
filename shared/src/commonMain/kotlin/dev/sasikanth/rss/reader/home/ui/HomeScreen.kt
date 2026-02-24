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

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.pinned.PinnedSourcesBottomBar
import dev.sasikanth.rss.reader.feeds.ui.pinned.rememberPinnedSourcesBottomBarScrollBehavior
import dev.sasikanth.rss.reader.home.HomeEffect
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
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalInAppRating
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.PINNED_SOURCES_BOTTOM_BAR_HEIGHT
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
  triggerSync: Boolean,
  openPost: (Int, ResolvedPost) -> Unit,
  onMenuClicked: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val feedsState by feedsViewModel.state.collectAsStateWithLifecycle()
  val linkHandler = LocalLinkHandler.current
  val density = LocalDensity.current
  val dynamicColorState = LocalDynamicColorState.current
  val sizeClass = LocalWindowSizeClass.current
  val shouldBlockImage = LocalBlockImage.current
  val inAppRating = LocalInAppRating.current

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        HomeEffect.RequestInAppRating -> inAppRating.request()
      }
    }
  }

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
    remember(state.featuredPosts, state.themeVariant, sizeClass, shouldBlockImage) {
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
  val showScrollToTop by remember { derivedStateOf { postsListState.firstVisibleItemIndex > 0 } }
  val unreadSinceLastSync = state.unreadSinceLastSync

  val appBarScrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val bottomBarScrollState = rememberPinnedSourcesBottomBarScrollBehavior()

  LaunchedEffect(triggerSync) {
    if (triggerSync) {
      viewModel.dispatch(HomeEvent.OnSwipeToRefresh)
    }
  }

  LaunchedEffect(state.activeSource) {
    if (state.activeSource != state.prevActiveSource) {
      viewModel.dispatch(HomeEvent.UpdatePrevActiveSource(state.activeSource))
      viewModel.dispatch(HomeEvent.UpdateVisibleItemIndex(0))
      bottomBarScrollState.state.heightOffset = 0f
      appBarScrollBehaviour.state.heightOffset = 0f
      appBarScrollBehaviour.state.contentOffset = 0f
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
    featuredPosts,
    itemProvider = { index -> featuredPosts.getOrNull(index) },
  ) { fromItem, toItem, offset ->
    val fromSeedColor = fromItem?.seedColor?.let { Color(it) }
    val toSeedColor = toItem?.seedColor?.let { Color(it) }

    if (state.themeVariant == ThemeVariant.Dynamic) {
      dynamicColorState.animate(
        fromSeedColor = fromSeedColor,
        toSeedColor = toSeedColor,
        progress = offset,
      )
    }
  }

  Scaffold(
    modifier =
      modifier.onPreviewKeyEvent { event ->
        when {
          event.isMetaPressed && event.key == Key.R && event.type == KeyEventType.KeyUp -> {
            viewModel.dispatch(HomeEvent.OnSwipeToRefresh)
            true
          }
          else -> false
        }
      },
    bottomBar = {
      val canShowBottomBar = state.showPinnedSources && feedsState.pinnedSources.isNotEmpty()
      if (canShowBottomBar) {
        val scaffoldBottomPadding =
          if (platform == Platform.Desktop) {
            16.dp
          } else {
            0.dp
          }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          AppTheme(useDarkTheme = true) {
            PinnedSourcesBottomBar(
              modifier = Modifier.padding(bottom = scaffoldBottomPadding),
              pinnedSources = feedsState.pinnedSources,
              activeSource = feedsState.activeSource,
              canShowUnreadPostsCount = feedsState.canShowUnreadPostsCount,
              onSourceClick = { feed -> feedsViewModel.dispatch(FeedsEvent.OnSourceClick(feed)) },
              onHomeSelected = { feedsViewModel.dispatch(FeedsEvent.OnHomeSelected) },
              scrollBehavior = bottomBarScrollState,
            )
          }
        }
      }
    },
  ) { scaffoldPadding ->
    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorScheme.backdrop)) {
      val hasFeeds = state.hasFeeds
      val nestedScrollModifier =
        if (platform !is Platform.Desktop) {
          Modifier.nestedScroll(appBarScrollBehaviour.nestedScrollConnection)
            .nestedScroll(bottomBarScrollState.nestedScrollConnection)
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
              val topPaddingPx = with(density) { paddingValues.calculateTopPadding().roundToPx() }
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

          val saveVisibleItemIndex by
            rememberUpdatedState({
              val firstVisibleItemInfoAfterOffset =
                postsListState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                  itemInfo.offset >= topOffset || itemInfo.offset == 0
                }
              val firstVisibleItemIndexAfterOffset = firstVisibleItemInfoAfterOffset?.index ?: 0
              val firstVisibleItemKey = firstVisibleItemInfoAfterOffset?.key as? String
              val settledPage = featuredPostsPagerState.settledPage

              viewModel.dispatch(
                HomeEvent.OnScreenStopped(
                  firstVisibleItemIndex = firstVisibleItemIndexAfterOffset,
                  firstVisibleItemKey = firstVisibleItemKey,
                  settledPage = settledPage,
                )
              )
            })

          LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) { saveVisibleItemIndex() }

          DisposableEffect(Unit) { onDispose { saveVisibleItemIndex() } }

          val pullToRefreshState = rememberPullToRefreshState()

          when {
            hasFeeds == null || posts == null -> {
              // no-op
            }
            !hasFeeds -> {
              NoFeeds { onMenuClicked?.invoke() }
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
                  markFeaturedPostAsReadOnScroll = {
                    viewModel.dispatch(HomeEvent.MarkFeaturedPostsAsRead(it))
                  },
                  onVisiblePostsChanged = { visiblePosts, firstVisibleItemIndex ->
                    viewModel.dispatch(
                      HomeEvent.OnVisiblePostsChanged(
                        visiblePosts = visiblePosts,
                        firstVisibleItemIndex = firstVisibleItemIndex,
                      )
                    )
                  },
                  onPostClicked = { post, _ -> viewModel.dispatch(HomeEvent.OnPostClicked(post)) },
                  onFeaturedPostClicked = { post ->
                    viewModel.dispatch(HomeEvent.OnPostClicked(post))
                  },
                  onPostBookmarkClick = { viewModel.dispatch(HomeEvent.OnPostBookmarkClick(it)) },
                  onPostCommentsClick = { commentsLink ->
                    coroutineScope.launch { linkHandler.openLink(commentsLink) }
                  },
                  onPostSourceClick = { feedId ->
                    viewModel.dispatch(HomeEvent.OnPostSourceClicked(feedId))
                  },
                  updateReadStatus = { postId, updatedReadStatus ->
                    viewModel.dispatch(HomeEvent.UpdatePostReadStatus(postId, updatedReadStatus))
                  },
                  modifier = Modifier.fillMaxSize(),
                )
              }
            }
          }
        },
      )

      val canShowBottomBar = state.showPinnedSources && feedsState.pinnedSources.isNotEmpty()
      if (canShowBottomBar) {
        val colorScheme = AppTheme.colorScheme
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .requiredHeight(PINNED_SOURCES_BOTTOM_BAR_HEIGHT)
              .align(Alignment.BottomCenter)
              .graphicsLayer { translationY = -bottomBarScrollState.state.heightOffset }
              .drawBehind {
                drawRect(Brush.verticalGradient(listOf(Color.Transparent, colorScheme.backdrop)))
              }
        )
      }

      NewArticlesScrollToTopButton(
        unreadSinceLastSync = unreadSinceLastSync,
        canShowScrollToTop = showScrollToTop,
        modifier =
          Modifier.padding(scaffoldPadding).graphicsLayer {
            translationY =
              bottomBarScrollState.state.heightOffset
                .unaryMinus()
                .coerceAtMost(PINNED_SOURCES_BOTTOM_BAR_HEIGHT.toPx())
          },
        onLoadNewArticlesClick = {
          coroutineScope.launch {
            launch {
              animate(initialValue = bottomBarScrollState.state.heightOffset, targetValue = 0f) {
                value,
                _ ->
                bottomBarScrollState.state.heightOffset = value
              }
            }
            launch {
              animate(initialValue = appBarScrollBehaviour.state.heightOffset, targetValue = 0f) {
                value,
                _ ->
                appBarScrollBehaviour.state.heightOffset = value
              }
            }
            launch {
              animate(initialValue = appBarScrollBehaviour.state.contentOffset, targetValue = 0f) {
                value,
                _ ->
                appBarScrollBehaviour.state.contentOffset = value
              }
            }
            postsListState.animateScrollToItem(0)
          }
          viewModel.dispatch(HomeEvent.LoadNewArticlesClick)
        },
      ) {
        coroutineScope.launch {
          launch {
            animate(initialValue = bottomBarScrollState.state.heightOffset, targetValue = 0f) {
              value,
              _ ->
              bottomBarScrollState.state.heightOffset = value
            }
          }
          launch {
            animate(initialValue = appBarScrollBehaviour.state.heightOffset, targetValue = 0f) {
              value,
              _ ->
              appBarScrollBehaviour.state.heightOffset = value
            }
          }
          launch {
            animate(initialValue = appBarScrollBehaviour.state.contentOffset, targetValue = 0f) {
              value,
              _ ->
              appBarScrollBehaviour.state.contentOffset = value
            }
          }
          postsListState.animateScrollToItem(0)
        }
      }
    }
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
      color = AppTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.swipeUpGetStarted),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Icon(
      imageVector = Icons.Rounded.KeyboardArrowUp,
      contentDescription = null,
      tint = AppTheme.colorScheme.primary,
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
      tint = AppTheme.colorScheme.onSurface,
      modifier = Modifier.requiredSize(80.dp),
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Text(
      text = stringResource(Res.string.noNewPosts),
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = stringResource(Res.string.noNewPostsSubtitle),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )
  }
}
