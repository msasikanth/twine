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
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.components.NewArticlesScrollToTopButton
import dev.sasikanth.rss.reader.core.model.local.FeaturedPostItem
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsState
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.pinned.PinnedSourcesBottomBar
import dev.sasikanth.rss.reader.feeds.ui.pinned.rememberPinnedSourcesBottomBarScrollBehavior
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeState
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowUp
import dev.sasikanth.rss.reader.resources.icons.Newsstand
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalInAppRating
import dev.sasikanth.rss.reader.utils.LocalRootWindowSizeClass
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.PINNED_SOURCES_BOTTOM_BAR_HEIGHT
import dev.sasikanth.rss.reader.utils.iosBottomSafeAreaPadding
import kotlin.math.abs
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.noFeeds
import twine.shared.generated.resources.noNewPosts
import twine.shared.generated.resources.noNewPostsSubtitle
import twine.shared.generated.resources.swipeLeftGetStarted
import twine.shared.generated.resources.swipeRightGetStarted

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
  val state by viewModel.state.collectAsStateWithLifecycle()
  val feedsState by feedsViewModel.state.collectAsStateWithLifecycle()
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

  // Keyed to the window, not the pane, so the featured section stays hidden when this
  // screen is squeezed into a split's list pane.
  val rootSizeClass = LocalRootWindowSizeClass.current
  val forceShowAllPosts =
    shouldBlockImage ||
      rootSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

  val posts =
    remember(
        state.allPosts,
        state.feedPosts,
        state.showFeaturedSection,
        forceShowAllPosts,
        state.activeSource?.id,
      ) {
        if (state.showFeaturedSection && !forceShowAllPosts) {
          state.feedPosts
        } else {
          state.allPosts
        }
      }
      ?.collectAsLazyPagingItems()
  val postsProvider = remember(posts) { { posts } }

  val allFeaturedPosts by state.featuredPosts.collectAsStateWithLifecycle()
  val featuredPosts =
    remember(allFeaturedPosts, forceShowAllPosts) {
      if (forceShowAllPosts) {
        persistentListOf()
      } else {
        allFeaturedPosts
      }
    }

  LaunchedEffect(forceShowAllPosts) {
    viewModel.dispatch(HomeEvent.UpdateFeaturedSectionVisibility(visible = !forceShowAllPosts))
  }

  LaunchedEffect(triggerSync) {
    if (triggerSync) {
      viewModel.dispatch(HomeEvent.OnSwipeToRefresh)
    }
  }

  LaunchedEffect(state.activeSource) {
    if (state.activeSource != state.prevActiveSource) {
      viewModel.dispatch(HomeEvent.UpdatePrevActiveSource(state.activeSource))
      viewModel.dispatch(HomeEvent.UpdateVisibleItemIndex(0))
    }
  }

  val activeReaderPostId by viewModel.activeReaderPostId.collectAsStateWithLifecycle()

  HomeContent(
    state = state,
    feedsState = feedsState,
    posts = postsProvider,
    featuredPosts = featuredPosts,
    dispatch = viewModel::dispatch,
    feedsDispatch = feedsViewModel::dispatch,
    onMenuClicked = onMenuClicked,
    modifier = modifier,
    activeReaderPostId = activeReaderPostId,
  )
}

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
private fun HomeContent(
  state: HomeState,
  feedsState: FeedsState,
  posts: () -> LazyPagingItems<ResolvedPost>?,
  featuredPosts: ImmutableList<FeaturedPostItem>,
  dispatch: (HomeEvent) -> Unit,
  feedsDispatch: (FeedsEvent) -> Unit,
  onMenuClicked: (() -> Unit)?,
  modifier: Modifier = Modifier,
  activeReaderPostId: String? = null,
) {
  val linkHandler = LocalLinkHandler.current
  val coroutineScope = rememberCoroutineScope()
  val density = LocalDensity.current
  val dynamicColorState = LocalDynamicColorState.current
  val postsListState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { featuredPosts.size })
  val showScrollToTop by
    remember(featuredPosts, posts) {
      derivedStateOf {
        val hasContent = featuredPosts.isNotEmpty() || (posts()?.itemCount ?: 0) > 0
        hasContent && postsListState.firstVisibleItemIndex > 0
      }
    }
  val unreadSinceLastSync =
    if (state.isSyncing) {
      state.unreadSinceLastSync?.copy(hasNewArticles = false)
    } else {
      state.unreadSinceLastSync
    }

  val canShowBottomBar = platform !is Platform.Desktop && state.showPinnedSources
  val appBarScrollBehaviour = TopAppBarDefaults.enterAlwaysScrollBehavior()
  val bottomBarScrollState =
    rememberPinnedSourcesBottomBarScrollBehavior(canScroll = { canShowBottomBar })
  val homeScrollBehavior = rememberHomeScrollBehavior(appBarScrollBehaviour, bottomBarScrollState)

  val homeFocusRequester = remember { FocusRequester() }
  if (platform == Platform.Desktop) {
    LaunchedEffect(Unit) { homeFocusRequester.requestFocus() }
  }
  var refreshShortcutArmed by remember { mutableStateOf(false) }

  val onSourceClick =
    remember(feedsDispatch) { { feed: Source -> feedsDispatch(FeedsEvent.OnSourceClick(feed)) } }
  val onHomeSelected = remember(feedsDispatch) { { feedsDispatch(FeedsEvent.OnHomeSelected) } }
  val onPinnedSourceOrderChanged =
    remember(feedsDispatch) {
      { newPinnedSources: List<Source> ->
        feedsDispatch(FeedsEvent.OnPinnedSourcePositionChanged(newPinnedSources))
      }
    }

  LaunchedEffect(state.activeSource) {
    if (state.activeSource != state.prevActiveSource) {
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
        dispatch(HomeEvent.OnPostsSortFilterApplied(postsType, postsSortOrder))
      },
      onDismiss = { dispatch(HomeEvent.ShowPostsSortFilter(show = false)) },
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
      modifier.focusRequester(homeFocusRequester).focusable().onPreviewKeyEvent { event ->
        when {
          event.key == Key.R && event.type == KeyEventType.KeyDown && event.isMetaPressed -> {
            if (!refreshShortcutArmed) {
              refreshShortcutArmed = true
              dispatch(HomeEvent.OnSwipeToRefresh)
            }
            true
          }
          event.key == Key.R && event.type == KeyEventType.KeyUp -> {
            refreshShortcutArmed = false
            false
          }
          else -> false
        }
      },
    bottomBar = {
      if (canShowBottomBar) {
        val scaffoldBottomPadding =
          if (platform == Platform.Desktop) {
            16.dp
          } else {
            0.dp
          }

        val bottomBarModifier =
          remember(scaffoldBottomPadding, onMenuClicked, density) {
            Modifier.padding(bottom = scaffoldBottomPadding)
          }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
          AppTheme(useDarkTheme = true) {
            PinnedSourcesBottomBar(
              modifier = bottomBarModifier,
              pinnedSources = feedsState.pinnedSources,
              activeSource = feedsState.activeSource,
              canShowUnreadPostsCount = feedsState.canShowUnreadPostsCount,
              onSourceClick = onSourceClick,
              onHomeSelected = onHomeSelected,
              onPinnedSourceOrderChanged = onPinnedSourceOrderChanged,
              scrollBehavior = bottomBarScrollState,
            )
          }
        }
      }
    },
  ) { scaffoldPadding ->
    val colorScheme = AppTheme.colorScheme
    Box(
      modifier =
        Modifier.fillMaxSize()
          .drawBehind { drawRect(colorScheme.backdrop) }
          .iosBottomSafeAreaPadding()
    ) {
      val nestedScrollModifier =
        if (platform !is Platform.Desktop) {
          Modifier.nestedScroll(homeScrollBehavior.nestedScrollConnection)
        } else {
          Modifier
        }

      val latestState by rememberUpdatedState(state)
      val latestPosts by rememberUpdatedState(posts())
      val latestFeaturedPosts by rememberUpdatedState(featuredPosts)

      HomeScreenContentScaffold(
        modifier = Modifier.then(nestedScrollModifier),
        bottomPadding = scaffoldPadding.calculateBottomPadding(),
        homeTopAppBar = {
          HomeTopAppBar(
            source = latestState.activeSource,
            postsType = latestState.postsType,
            listState = postsListState,
            hasUnreadPosts = latestState.hasUnreadPosts,
            confirmMarkAllAsRead = latestState.confirmMarkAllAsRead,
            scrollBehavior = if (platform !is Platform.Desktop) appBarScrollBehaviour else null,
            onMenuClicked = onMenuClicked,
            onShowPostsSortFilter = { dispatch(HomeEvent.ShowPostsSortFilter(true)) },
            onMarkPostsAsRead = { dispatch(HomeEvent.MarkPostsAsRead(it)) },
          )
        },
        body = { paddingValues ->
          val featuredPosts = latestFeaturedPosts
          val state = latestState
          val posts = latestPosts

          val topOffset =
            remember(paddingValues) {
              with(density) { paddingValues.calculateTopPadding().roundToPx() }
            }

          LaunchedEffect(
            state.activePostIndex,
            state.activePostScrollOffset,
            featuredPosts.isNotEmpty(),
          ) {
            val activePostIndex = state.activePostIndex
            val savedScrollOffset = state.activePostScrollOffset
            val numberOfFeaturedPosts = featuredPosts.size
            val targetIsFeatured =
              activePostIndex < numberOfFeaturedPosts && numberOfFeaturedPosts > 0

            snapshotFlow { targetIsFeatured || (latestPosts?.itemCount ?: 0) > 0 }.first { it }

            snapshotFlow { postsListState.isScrollInProgress }.first { !it }

            if (activePostIndex < numberOfFeaturedPosts && numberOfFeaturedPosts > 0) {
              postsListState.scrollToItem(0, scrollOffset = -(savedScrollOffset ?: 0))
              featuredPostsPagerState.scrollToPage(activePostIndex)
            } else {
              // activePostIndex counts featured posts first, then list posts. In the
              // LazyColumn the featured section always occupies item 0 (even when the
              // featured list is empty), so the list index of a post is its position
              // among non-featured posts plus one.
              val adjustedIndex = (activePostIndex - numberOfFeaturedPosts + 1).coerceAtLeast(0)

              postsListState.scrollToItem(
                adjustedIndex,
                scrollOffset = -(savedScrollOffset ?: topOffset),
              )
            }
          }

          val saveVisibleItemIndex by rememberUpdatedState {
            val firstVisibleItem = postsListState.layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstVisibleItem != null) {
              dispatch(
                HomeEvent.OnScreenStopped(
                  firstVisibleItemIndex = firstVisibleItem.index,
                  firstVisibleItemKey = firstVisibleItem.key as? String,
                  firstVisibleItemOffset = firstVisibleItem.offset,
                  settledPage = featuredPostsPagerState.settledPage,
                )
              )
            }
          }

          LifecycleEventEffect(event = Lifecycle.Event.ON_START) {
            dispatch(HomeEvent.OnScreenStarted)
          }

          LifecycleEventEffect(event = Lifecycle.Event.ON_STOP) { saveVisibleItemIndex() }

          DisposableEffect(Unit) { onDispose { saveVisibleItemIndex() } }

          val pullToRefreshState = rememberPullToRefreshState()

          when {
            state.hasFeeds == null || posts == null -> {
              // no-op
            }
            !state.hasFeeds -> {
              NoFeeds { onMenuClicked?.invoke() }
            }
            featuredPosts.isEmpty() && posts.itemCount == 0 -> {
              PullToRefreshContent(
                pullToRefreshState = pullToRefreshState,
                state = state,
                paddingValues = paddingValues,
                onRefresh = { dispatch(HomeEvent.OnSwipeToRefresh) },
              ) {
                NoNewPosts()
              }
            }
            else -> {
              PullToRefreshContent(
                pullToRefreshState = pullToRefreshState,
                state = state,
                paddingValues = paddingValues,
                onRefresh = { dispatch(HomeEvent.OnSwipeToRefresh) },
              ) {
                val backdropColor = AppTheme.colorScheme.backdrop
                PostsList(
                  paddingValues = paddingValues,
                  featuredPosts = featuredPosts,
                  listState = postsListState,
                  featuredPostsPagerState = featuredPostsPagerState,
                  homeViewMode = state.homeViewMode,
                  postsType = state.postsType,
                  markAsReadOn = state.markAsReadOn,
                  posts = { posts },
                  markFeaturedPostAsReadOnScroll = {
                    dispatch(HomeEvent.MarkFeaturedPostsAsRead(it))
                  },
                  onVisiblePostsChanged = { visiblePosts, firstVisibleItemIndex ->
                    dispatch(
                      HomeEvent.OnVisiblePostsChanged(
                        visiblePosts = visiblePosts,
                        firstVisibleItemIndex = firstVisibleItemIndex,
                      )
                    )
                  },
                  onPostClicked = { post, _ -> dispatch(HomeEvent.OnPostClicked(post)) },
                  onFeaturedPostClicked = { post -> dispatch(HomeEvent.OnPostClicked(post)) },
                  onPostBookmarkClick = { dispatch(HomeEvent.OnPostBookmarkClick(it)) },
                  onPostCommentsClick = { commentsLink ->
                    coroutineScope.launch { linkHandler.openLink(commentsLink) }
                  },
                  onPostSourceClick = { feedId -> dispatch(HomeEvent.OnPostSourceClicked(feedId)) },
                  updateReadStatus = { postId, updatedReadStatus ->
                    dispatch(HomeEvent.UpdatePostReadStatus(postId, updatedReadStatus))
                  },
                  activeReaderPostId = activeReaderPostId,
                  modifier =
                    Modifier.fillMaxSize().drawWithContent {
                      drawContent()
                      drawRect(
                        brush =
                          Brush.verticalGradient(0.85f to Color.Transparent, 1f to backdropColor)
                      )
                    },
                )
              }
            }
          }
        },
      )

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
          dispatch(HomeEvent.LoadNewArticlesClick)
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
          containerColor = AppTheme.colorScheme.surfaceContainerHigh,
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
private fun NoFeeds(onNoFeedsSwipe: () -> Unit) {
  val layoutDirection = LocalLayoutDirection.current
  val isRtl = layoutDirection == LayoutDirection.Rtl

  Column(
    modifier =
      Modifier.padding(horizontal = 16.dp).fillMaxSize().pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          if (isRtl) {
            if (dragAmount.x < 0 && abs(dragAmount.x) > abs(dragAmount.y)) {
              onNoFeedsSwipe()
            }
          } else {
            if (dragAmount.x > 0 && dragAmount.x > abs(dragAmount.y)) {
              onNoFeedsSwipe()
            }
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
      text =
        if (isRtl) {
          stringResource(Res.string.swipeLeftGetStarted)
        } else {
          stringResource(Res.string.swipeRightGetStarted)
        },
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Icon(
      imageVector = TwineIcons.ArrowUp,
      contentDescription = null,
      tint = AppTheme.colorScheme.primary,
      modifier = Modifier.graphicsLayer { rotationZ = if (isRtl) -90f else 90f },
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

@Preview(locale = "en")
@Composable
private fun HomePreview() {
  AppTheme {
    HomeContent(
      state = HomeState.default(),
      feedsState = FeedsState.DEFAULT,
      posts = { null },
      featuredPosts = persistentListOf(),
      dispatch = {},
      feedsDispatch = {},
      onMenuClicked = {},
    )
  }
}
