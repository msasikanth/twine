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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy
import androidx.paging.LoadState
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CompactFloatingActionButton
import dev.sasikanth.rss.reader.components.LocalDynamicColorState
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Feed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal val BOTTOM_SHEET_PEEK_HEIGHT = 96.dp
private val BOTTOM_SHEET_CORNER_SIZE = 32.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(homePresenter: HomePresenter, modifier: Modifier = Modifier) {
  val coroutineScope = rememberCoroutineScope()
  val state by homePresenter.state.collectAsState()
  val feedsState by homePresenter.feedsPresenter.state.collectAsState()

  val bottomSheetState =
    rememberStandardBottomSheetState(
      initialValue = state.feedsSheetState,
      confirmValueChange = {
        if (it != SheetValue.Hidden) {
          homePresenter.dispatch(HomeEvent.FeedsSheetStateChanged(it))
          true
        } else {
          false
        }
      }
    )
  val bottomSheetScaffoldState =
    rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

  val listState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { state.featuredPosts?.size ?: 0 })

  val linkHandler = LocalLinkHandler.current

  LaunchedEffect(Unit) {
    homePresenter.effects.collectLatest { effect ->
      when (effect) {
        HomeEffect.MinimizeSheet -> {
          bottomSheetState.partialExpand()
        }
      }
    }
  }

  val bottomSheetProgress by bottomSheetState.progress()
  val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

  val sheetPeekHeight =
    BOTTOM_SHEET_PEEK_HEIGHT +
      WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

  BottomSheetScaffold(
    modifier = modifier,
    scaffoldState = bottomSheetScaffoldState,
    content = { _ ->
      Box {
        val featuredPosts = state.featuredPosts
        val posts = state.posts?.collectAsLazyPagingItems()
        val hasFeeds = state.hasFeeds
        val dynamicColorState = LocalDynamicColorState.current

        LaunchedEffect(featuredPosts) {
          if (featuredPosts.isNullOrEmpty()) {
            dynamicColorState.reset()
          }
        }

        val swipeRefreshState =
          rememberPullRefreshState(
            refreshing = state.isRefreshing,
            onRefresh = { homePresenter.dispatch(HomeEvent.OnSwipeToRefresh) }
          )
        val canSwipeToRefresh = hasFeeds == true

        HomeScreenContentLayout(
          modifier = Modifier.pullRefresh(state = swipeRefreshState, enabled = canSwipeToRefresh),
          homeTopAppBar = {
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
          },
          body = { paddingValues ->
            Box {
              when {
                hasFeeds == null || (posts == null || featuredPosts == null) -> {
                  // no-op
                }
                featuredPosts.isNotEmpty() ||
                  (posts.itemCount > 0 || posts.loadState.refresh == LoadState.Loading) -> {
                  PostsList(
                    paddingValues = paddingValues,
                    featuredPosts = featuredPosts,
                    posts = posts,
                    featuredItemBlurEnabled = state.featuredItemBlurEnabled,
                    listState = listState,
                    featuredPostsPagerState = featuredPostsPagerState,
                    onPostClicked = { homePresenter.dispatch(HomeEvent.OnPostClicked(it)) },
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
                modifier =
                  Modifier.windowInsetsPadding(WindowInsets.statusBars).align(Alignment.TopCenter)
              )
            }
          },
        )

        CompactFloatingActionButton(
          label = LocalStrings.current.scrollToTop,
          visible = showScrollToTop,
          modifier = Modifier.padding(end = 16.dp, bottom = sheetPeekHeight + 16.dp),
        ) {
          listState.animateScrollToItem(0)
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
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
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

@Composable
private fun HomeScreenContentLayout(
  homeTopAppBar: @Composable () -> Unit,
  body: @Composable (PaddingValues) -> Unit,
  modifier: Modifier = Modifier,
) {
  SubcomposeLayout(
    modifier = Modifier.fillMaxSize().then(modifier),
  ) { constraints ->
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight
    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    val topBarPlaceables =
      subcompose("topBar") { homeTopAppBar() }.map { it.measure(looseConstraints) }
    val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0

    val bodyConstraints = looseConstraints.copy(maxHeight = layoutHeight)
    val bodyPlaceables =
      subcompose("body") { body(PaddingValues(top = topBarHeight.toDp())) }
        .map { it.measure(bodyConstraints) }

    layout(layoutWidth, layoutHeight) {
      bodyPlaceables.fastForEach { it.placeRelative(0, 0) }
      topBarPlaceables.fastForEach { it.placeRelative(0, 0) }
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
private fun SheetState.progress(): State<Float> {
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
