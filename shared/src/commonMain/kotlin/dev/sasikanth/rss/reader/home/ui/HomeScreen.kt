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
@file:OptIn(ExperimentalMaterialApi::class)

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.ime
import com.moriatsushi.insetsx.navigationBars
import com.moriatsushi.insetsx.statusBarsPadding
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetScaffold
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetScaffoldState
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetState
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.feeds.ui.BottomSheetPrimaryActionButton
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeViewModelFactory
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverseProgress
import dev.sasikanth.rss.reader.utils.openBrowser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

private val BOTTOM_SHEET_PEEK_HEIGHT = 112.dp
private val BOTTOM_SHEET_CORNER_SIZE = 32.dp

@Composable
fun HomeScreen(
  homeViewModelFactory: HomeViewModelFactory,
  onFeaturedItemChange: (imageUrl: String?) -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

  val viewModel = homeViewModelFactory.viewModel
  val state by viewModel.state.collectAsState()
  val (featuredPosts, posts) = state

  val bottomSheetState =
    rememberBottomSheetState(
      state.feedsSheetState,
      confirmStateChange = {
        viewModel.dispatch(HomeEvent.FeedsSheetStateChanged(it))
        true
      }
    )
  val bottomSheetScaffoldState =
    rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState)

  val bottomSheetSwipeTransition =
    updateTransition(
      targetState = bottomSheetState.offsetProgress,
      label = "Bottom Sheet Swipe Progress"
    )
  val bottomSheetCornerSize by
    bottomSheetSwipeTransition.animateDp { BOTTOM_SHEET_CORNER_SIZE * it.inverseProgress() }

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        is HomeEffect.OpenPost -> {
          openBrowser(effect.post.link)
        }
        HomeEffect.MinimizeSheet -> {
          bottomSheetState.collapse()
        }
        is HomeEffect.ShowError -> {
          if (!effect.message.isNullOrBlank()) {
            bottomSheetScaffoldState.snackbarHostState.showSnackbar(message = effect.message)
          }
        }
      }
    }
  }

  LaunchedEffect(bottomSheetState.targetValue) {
    if (bottomSheetState.targetValue == BottomSheetValue.Collapsed) {
      viewModel.dispatch(HomeEvent.OnCancelAddFeedClicked)
    }
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal))
  ) {
    BottomSheetScaffold(
      scaffoldState = bottomSheetScaffoldState,
      content = {
        HomeScreenContent(
          featuredPosts = featuredPosts,
          posts = posts,
          isRefreshing = state.isRefreshing,
          onSwipeToRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) },
          onPostClicked = { viewModel.dispatch(HomeEvent.OnPostClicked(it)) },
          onFeaturedItemChange = onFeaturedItemChange,
          onNoFeedsSwipeUp = { coroutineScope.launch { bottomSheetState.expand() } }
        )
      },
      backgroundColor = Color.Transparent,
      sheetContent = {
        FeedsBottomSheet(
          feedsViewModel = homeViewModelFactory.feedsViewModel,
          bottomSheetSwipeTransition = bottomSheetSwipeTransition,
          showingFeedLinkEntry = state.canShowFeedLinkEntry,
          closeSheet = { coroutineScope.launch { bottomSheetState.collapse() } }
        )
      },
      sheetBackgroundColor = AppTheme.colorScheme.tintedBackground,
      sheetContentColor = AppTheme.colorScheme.tintedForeground,
      sheetElevation = 0.dp,
      sheetPeekHeight = BOTTOM_SHEET_PEEK_HEIGHT + navigationBarPadding,
      sheetShape =
        RoundedCornerShape(topStart = bottomSheetCornerSize, topEnd = bottomSheetCornerSize),
      snackbarHost = {
        SnackbarHost(
          hostState = it,
          modifier =
            Modifier.padding(bottom = 112.dp)
              .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
        )
      }
    )

    /**
     * Since we want the all button to not move when expanding and collapsing bottom bar and
     * transform to add button. We are not placing it inside the bottom sheet content and instead
     * place it above the home screen and bottom bar content essentially.
     *
     * We might have to replace this once bottom sheet exposes height or offset from bottom which
     * would allow us to modify the offset of this item in the sheet itself instead of using
     * workarounds.
     *
     * track: https://issuetracker.google.com/issues/209825720
     */
    val threshold = 5 // (1/0.2) 0.2 is our threshold in the 0..1 range
    val primaryActionStartPadding =
      (24.dp - (4 * (bottomSheetSwipeTransition.currentState * threshold).inverseProgress()).dp)
        .coerceAtLeast(20.dp)
        .coerceAtMost(24.dp)

    val primaryActionBottomPadding =
      navigationBarPadding - (4 * bottomSheetSwipeTransition.currentState).dp

    Box(Modifier.padding(start = primaryActionStartPadding).align(Alignment.BottomStart)) {
      if (state.canShowFeedLinkEntry) {
        FeedLinkInputField(
          modifier =
            Modifier.windowInsetsPadding(
                WindowInsets.navigationBars
                  .only(WindowInsetsSides.Bottom)
                  .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
              )
              .padding(bottom = 24.dp, end = 24.dp),
          isFetchingFeed = state.isFetchingFeed,
          onAddFeed = { viewModel.dispatch(HomeEvent.AddFeed(it)) },
          onCancelFeedEntryClicked = { viewModel.dispatch(HomeEvent.OnCancelAddFeedClicked) }
        )
      } else {
        BottomSheetPrimaryActionButton(
          modifier = Modifier.padding(bottom = primaryActionBottomPadding.coerceAtLeast(0.dp)),
          selected = state.isAllFeedsSelected,
          bottomSheetSwipeProgress =
            (bottomSheetSwipeTransition.currentState * threshold).inverseProgress(),
          bottomSheetCurrentState = bottomSheetState.currentValue,
          bottomSheetTargetState = bottomSheetState.targetValue
        ) {
          viewModel.dispatch(HomeEvent.OnPrimaryActionClicked)
        }
      }
    }
  }
}

@Composable
private fun HomeScreenContent(
  featuredPosts: ImmutableList<PostWithMetadata>,
  posts: ImmutableList<PostWithMetadata>,
  isRefreshing: Boolean,
  onSwipeToRefresh: () -> Unit,
  onPostClicked: (PostWithMetadata) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit,
  onNoFeedsSwipeUp: () -> Unit
) {
  val hasContent = featuredPosts.isNotEmpty() || posts.isNotEmpty()
  if (hasContent) {
    val swipeRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = onSwipeToRefresh)

    Box(Modifier.fillMaxSize().pullRefresh(swipeRefreshState)) {
      PostsList(
        featuredPosts = featuredPosts,
        posts = posts,
        onPostClicked = onPostClicked,
        onFeaturedItemChange = onFeaturedItemChange
      )

      PullRefreshIndicator(
        refreshing = isRefreshing,
        state = swipeRefreshState,
        modifier = Modifier.statusBarsPadding().align(Alignment.TopCenter)
      )
    }
  } else {
    NoFeeds(onNoFeedsSwipeUp)
  }
}

@Composable
private fun NoFeeds(onNoFeedsSwipeUp: () -> Unit) {
  Column(
    modifier =
      Modifier.fillMaxSize().padding(bottom = 136.dp).pointerInput(Unit) {
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
      text = "No feeds present!",
      style = MaterialTheme.typography.headlineMedium,
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    Spacer(Modifier.requiredHeight(8.dp))

    Text(
      text = "Swipe up to get started",
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisMed
    )

    Spacer(Modifier.requiredHeight(12.dp))

    Icon(
      imageVector = Icons.Filled.KeyboardArrowUp,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
  }
}
