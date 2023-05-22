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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.navigationBars
import com.moriatsushi.insetsx.statusBarsPadding
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetScaffold
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetState
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue.Collapsed
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue.Expanded
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetScaffoldState
import dev.sasikanth.rss.reader.components.bottomsheet.rememberBottomSheetState
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.feeds.ui.FeedsBottomSheet
import dev.sasikanth.rss.reader.home.HomeEffect
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeViewModelFactory
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverseProgress
import dev.sasikanth.rss.reader.utils.openBrowser
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

private const val NUMBER_OF_FEATURED_POSTS = 6
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
  val posts = state.posts

  // TODO: Move this transformation to data layer in background thread
  val featuredPosts =
    posts.filter { !it.imageUrl.isNullOrBlank() }.take(NUMBER_OF_FEATURED_POSTS).toImmutableList()
  val postsList = (posts - featuredPosts).toImmutableList()

  val bottomSheetState =
    rememberBottomSheetState(
      state.feedsSheetState,
      confirmStateChange = {
        viewModel.dispatch(HomeEvent.FeedsSheetStateChanged(it))
        true
      }
    )
  val bottomSheetSwipeProgress by derivedStateOf { calculateBottomSheetProgress(bottomSheetState) }
  val bottomSheetSwipeTransition =
    updateTransition(targetState = bottomSheetSwipeProgress, label = "Bottom Sheet Swipe Progress")
  val bottomSheetCornerSize by
    bottomSheetSwipeTransition.animateDp { BOTTOM_SHEET_CORNER_SIZE * it.inverseProgress() }

  LaunchedEffect(Unit) {
    viewModel.effects.collect { effect ->
      when (effect) {
        is HomeEffect.OpenPost -> {
          openBrowser(effect.post.link)
        }
      }
    }
  }

  BottomSheetScaffold(
    scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = bottomSheetState),
    content = {
      HomeScreenContent(
        featuredPosts = featuredPosts,
        postsList = postsList,
        isRefreshing = state.isRefreshing,
        onSwipeToRefresh = { viewModel.dispatch(HomeEvent.OnSwipeToRefresh) },
        onPostClicked = { viewModel.dispatch(HomeEvent.OnPostClicked(it)) },
        onFeaturedItemChange = onFeaturedItemChange
      )
    },
    backgroundColor = Color.Transparent,
    sheetContent = {
      FeedsBottomSheet(
        feedsViewModel = homeViewModelFactory.feedsViewModel,
        bottomSheetSwipeTransition = bottomSheetSwipeTransition,
        closeSheet = { coroutineScope.launch { bottomSheetState.collapse() } }
      )
    },
    sheetBackgroundColor = AppTheme.colorScheme.tintedBackground,
    sheetContentColor = AppTheme.colorScheme.tintedForeground,
    sheetElevation = 0.dp,
    sheetPeekHeight = BOTTOM_SHEET_PEEK_HEIGHT + navigationBarPadding,
    sheetShape =
      RoundedCornerShape(topStart = bottomSheetCornerSize, topEnd = bottomSheetCornerSize)
  )
}

@Composable
private fun HomeScreenContent(
  featuredPosts: ImmutableList<PostWithMetadata>,
  postsList: ImmutableList<PostWithMetadata>,
  isRefreshing: Boolean,
  onSwipeToRefresh: () -> Unit,
  onPostClicked: (PostWithMetadata) -> Unit,
  onFeaturedItemChange: (imageUrl: String?) -> Unit
) {
  val swipeRefreshState = rememberPullRefreshState(isRefreshing, onRefresh = onSwipeToRefresh)

  Box(Modifier.pullRefresh(swipeRefreshState)) {
    PostsList(
      featuredPosts = featuredPosts,
      posts = postsList,
      onPostClicked = onPostClicked,
      onFeaturedItemChange = onFeaturedItemChange
    )

    PullRefreshIndicator(
      refreshing = isRefreshing,
      state = swipeRefreshState,
      modifier = Modifier.statusBarsPadding().align(Alignment.TopCenter)
    )
  }
}

private fun calculateBottomSheetProgress(bottomSheetState: BottomSheetState): Float {
  return when {
    bottomSheetState.currentValue == Collapsed && bottomSheetState.targetValue == Expanded ->
      bottomSheetState.progress
    bottomSheetState.currentValue == Collapsed && bottomSheetState.targetValue == Collapsed -> 0f
    bottomSheetState.currentValue == Expanded && bottomSheetState.targetValue == Collapsed ->
      1f - bottomSheetState.progress
    bottomSheetState.currentValue == Expanded && bottomSheetState.targetValue == Expanded -> 1f
    else -> 0f
  }
}
