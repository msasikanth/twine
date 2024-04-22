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

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CompactFloatingActionButton
import dev.sasikanth.rss.reader.components.LocalDynamicColorState
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetScaffold
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetScaffoldState
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetState
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeErrorType
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomePresenter
import dev.sasikanth.rss.reader.home.HomeState
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Feed
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.resources.strings.TwineStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverse
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val BOTTOM_SHEET_PEEK_HEIGHT = 96.dp
private val BOTTOM_SHEET_CORNER_SIZE = 32.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeScreen(homePresenter: HomePresenter, modifier: Modifier = Modifier) {
  val coroutineScope = rememberCoroutineScope()
  val state by homePresenter.state.collectAsState()
  val feedsState by homePresenter.feedsPresenter.state.collectAsState()

  val bottomSheetState =
    rememberBottomSheetState(
      state.feedsSheetState,
      confirmStateChange = {
        homePresenter.dispatch(HomeEvent.FeedsSheetStateChanged(it))
        true
      }
    )
  val bottomSheetScaffoldState =
    rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

  val listState = rememberLazyListState()
  val featuredPostsPagerState = rememberPagerState(pageCount = { state.featuredPosts?.size ?: 0 })

  val bottomSheetSwipeTransition =
    updateTransition(
      targetState = bottomSheetState.offsetProgress,
      label = "Bottom Sheet Swipe Progress"
    )
  val bottomSheetCornerSize by
    bottomSheetSwipeTransition.animateDp { BOTTOM_SHEET_CORNER_SIZE * it.inverse() }

  val strings = LocalStrings.current
  val linkHandler = LocalLinkHandler.current

  LaunchedEffect(Unit) {
    homePresenter.effects.collectLatest { effect ->
      when (effect) {
        HomeEffect.MinimizeSheet -> {
          bottomSheetState.collapse()
        }
        is HomeEffect.ShowError -> {
          val errorMessage = errorMessageForErrorType(effect.homeErrorType, strings)
          if (errorMessage != null) {
            bottomSheetScaffoldState.snackbarHostState.showSnackbar(message = errorMessage)
          }
        }
      }
    }
  }

  Box(modifier = modifier) {
    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      topBar = {
        HomeTopAppBar(
          source = state.activeSource,
          postsType = state.postsType,
          listState = listState,
          onSearchClicked = { homePresenter.dispatch(HomeEvent.SearchClicked) },
          onBookmarksClicked = { homePresenter.dispatch(HomeEvent.BookmarksClicked) },
          onSettingsClicked = { homePresenter.dispatch(HomeEvent.SettingsClicked) },
          onPostTypeChanged = { homePresenter.dispatch(HomeEvent.OnPostsTypeChanged(it)) }
        )
      },
      content = { paddingValues ->
        HomeScreenContent(
          paddingValues = paddingValues,
          state = state,
          listState = listState,
          featuredPostsPagerState = featuredPostsPagerState,
          onSwipeToRefresh = { homePresenter.dispatch(HomeEvent.OnSwipeToRefresh) },
          onPostClicked = { homePresenter.dispatch(HomeEvent.OnPostClicked(it)) },
          onPostBookmarkClick = { homePresenter.dispatch(HomeEvent.OnPostBookmarkClick(it)) },
          onPostCommentsClick = { commentsLink ->
            coroutineScope.launch { linkHandler.openLink(commentsLink) }
          },
          onPostSourceClick = { feedId ->
            homePresenter.dispatch(HomeEvent.OnPostSourceClicked(feedId))
          },
          onNoFeedsSwipeUp = { coroutineScope.launch { bottomSheetState.expand() } },
          onTogglePostReadStatus = { postId, postRead ->
            homePresenter.dispatch(HomeEvent.TogglePostReadStatus(postId, postRead))
          }
        )
      },
      sheetContent = {
        FeedsBottomSheet(
          feedsPresenter = homePresenter.feedsPresenter,
          bottomSheetSwipeTransition = bottomSheetSwipeTransition,
          closeSheet = { coroutineScope.launch { bottomSheetState.collapse() } },
          selectedFeedChanged = {
            coroutineScope.launch {
              listState.scrollToItem(0)
              featuredPostsPagerState.scrollToPage(0)
            }
          }
        )
      },
      snackbarHost = {
        val snackbarModifier =
          if (bottomSheetState.isExpanded) {
            Modifier.padding(bottom = BOTTOM_SHEET_PEEK_HEIGHT)
              .windowInsetsPadding(
                WindowInsets.systemBars.only(
                  WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
              )
          } else {
            Modifier
          }

        SnackbarHost(hostState = it, modifier = snackbarModifier) { snackbarData ->
          Snackbar(
            modifier = Modifier.padding(12.dp),
            content = {
              Text(text = snackbarData.message, maxLines = 4, overflow = TextOverflow.Ellipsis)
            },
            action = null,
            actionOnNewLine = false,
            shape = SnackbarDefaults.shape,
            backgroundColor = SnackbarDefaults.color,
            contentColor = SnackbarDefaults.contentColor,
            elevation = 0.dp
          )
        }
      },
      floatingActionButton = {
        val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

        CompactFloatingActionButton(
          label = LocalStrings.current.scrollToTop,
          visible = showScrollToTop,
          modifier =
            Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
              .padding(end = 16.dp, bottom = 16.dp),
        ) {
          listState.animateScrollToItem(0)
        }
      },
      backgroundColor = AppTheme.colorScheme.surfaceContainerLowest,
      sheetBackgroundColor = AppTheme.colorScheme.tintedBackground,
      sheetContentColor = AppTheme.colorScheme.tintedForeground,
      sheetElevation = 0.dp,
      sheetPeekHeight = BOTTOM_SHEET_PEEK_HEIGHT,
      sheetShape =
        RoundedCornerShape(topStart = bottomSheetCornerSize, topEnd = bottomSheetCornerSize),
      sheetGesturesEnabled = !feedsState.isInMultiSelectMode
    )
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun HomeScreenContent(
  paddingValues: PaddingValues,
  state: HomeState,
  listState: LazyListState,
  featuredPostsPagerState: PagerState,
  onSwipeToRefresh: () -> Unit,
  onPostClicked: (PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
  onNoFeedsSwipeUp: () -> Unit,
  onTogglePostReadStatus: (String, Boolean) -> Unit,
) {
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
    rememberPullRefreshState(refreshing = state.isRefreshing, onRefresh = onSwipeToRefresh)
  val canSwipeToRefresh = hasFeeds == true

  Box(Modifier.fillMaxSize().pullRefresh(state = swipeRefreshState, enabled = canSwipeToRefresh)) {
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
          onPostClicked = onPostClicked,
          onPostBookmarkClick = onPostBookmarkClick,
          onPostCommentsClick = onPostCommentsClick,
          onPostSourceClick = onPostSourceClick,
          onTogglePostReadClick = onTogglePostReadStatus
        )
      }
      !hasFeeds -> {
        NoFeeds(onNoFeedsSwipeUp)
      }
      featuredPosts.isEmpty() && posts.itemCount == 0 -> {
        NoNewPosts()
      }
    }

    PullRefreshIndicator(
      refreshing = state.isRefreshing,
      state = swipeRefreshState,
      modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars).align(Alignment.TopCenter)
    )
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

private fun errorMessageForErrorType(errorType: HomeErrorType, twineStrings: TwineStrings) =
  when (errorType) {
    HomeErrorType.UnknownFeedType -> twineStrings.errorUnsupportedFeed
    HomeErrorType.FailedToParseXML -> twineStrings.errorMalformedXml
    HomeErrorType.Timeout -> twineStrings.errorRequestTimeout
    is HomeErrorType.Unknown -> errorType.e.message
    is HomeErrorType.FeedNotFound -> twineStrings.errorFeedNotFound(errorType.statusCode.value)
    is HomeErrorType.ServerError -> twineStrings.errorServer(errorType.statusCode.value)
    HomeErrorType.TooManyRedirects -> twineStrings.errorTooManyRedirects
    is HomeErrorType.UnAuthorized -> twineStrings.errorUnAuthorized(errorType.statusCode.value)
    is HomeErrorType.UnknownHttpStatusError ->
      twineStrings.errorUnknownHttpStatus(errorType.statusCode.value)
  }
