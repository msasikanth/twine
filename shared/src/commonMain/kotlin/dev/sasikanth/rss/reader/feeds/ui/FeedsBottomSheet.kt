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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.feeds.FeedsEffect
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.utils.inverse

@Composable
internal fun FeedsBottomSheet(
  feedsPresenter: FeedsPresenter,
  bottomSheetSwipeTransition: Transition<Float>,
  closeSheet: () -> Unit,
  selectedFeedChanged: () -> Unit
) {
  val state by feedsPresenter.state.collectAsState()

  LaunchedEffect(Unit) {
    feedsPresenter.effects.collect { effect ->
      when (effect) {
        FeedsEffect.MinimizeSheet -> closeSheet()
        FeedsEffect.SelectedFeedChanged -> selectedFeedChanged()
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    BottomSheetHandle(bottomSheetSwipeTransition)

    // Transforming the bottom sheet progress from 0-1 to 1-0,
    // since we want to control the alpha of the content as
    // users swipes the sheet up and down
    val bottomSheetExpandingProgress = (bottomSheetSwipeTransition.currentState * 5f).inverse()
    val hasBottomSheetExpandedThreshold = bottomSheetExpandingProgress > 1e-6f

    if (hasBottomSheetExpandedThreshold) {
      BottomSheetCollapsedContent(
        modifier = Modifier.graphicsLayer { alpha = bottomSheetExpandingProgress },
        pinnedSources = state.pinnedSources.collectAsLazyPagingItems(),
        activeSource = state.activeSource,
        canShowUnreadPostsCount = state.canShowUnreadPostsCount,
        onSourceClick = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedClick(feed)) },
        onHomeSelected = { feedsPresenter.dispatch(FeedsEvent.OnHomeSelected) }
      )
    } else {
      BottomSheetExpandedContent(
        numberOfFeeds = state.numberOfFeeds,
        pinnedSources = state.pinnedSources.collectAsLazyPagingItems(),
        sources = state.sources.collectAsLazyPagingItems(),
        searchResults = state.feedsSearchResults.collectAsLazyPagingItems(),
        selectedSources = state.selectedSources,
        searchQuery = feedsPresenter.searchQuery,
        feedsSortOrder = state.feedsSortOrder,
        feedsViewMode = state.feedsViewMode,
        isPinnedSectionExpanded = state.isPinnedSectionExpanded,
        isInMultiSelectMode = state.isInMultiSelectMode,
        canShowUnreadPostsCount = state.canShowUnreadPostsCount,
        onSearchQueryChanged = { feedsPresenter.dispatch(FeedsEvent.SearchQueryChanged(it)) },
        onClearSearchQuery = { feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery) },
        onFeedClick = { feedsPresenter.dispatch(FeedsEvent.OnFeedClick(it)) },
        onToggleSourceSelection = { feedsPresenter.dispatch(FeedsEvent.OnToggleFeedSelection(it)) },
        onTogglePinnedSection = { feedsPresenter.dispatch(FeedsEvent.TogglePinnedSection) },
        onFeedsSortChanged = { feedsPresenter.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
        onChangeFeedsViewModeClick = {
          feedsPresenter.dispatch(FeedsEvent.OnChangeFeedsViewModeClick)
        },
        onCancelFeedsSelection = { feedsPresenter.dispatch(FeedsEvent.CancelSourcesSelection) },
        onPinSelectedFeeds = { feedsPresenter.dispatch(FeedsEvent.PinSelectedSources) },
        onUnPinSelectedFeeds = { feedsPresenter.dispatch(FeedsEvent.UnPinSelectedSources) },
        onDeleteSelectedFeeds = { feedsPresenter.dispatch(FeedsEvent.DeleteSelectedSources) },
        onCreateGroup = { feedsPresenter.dispatch(FeedsEvent.OnCreateGroup(it)) },
        modifier =
          Modifier.graphicsLayer {
            val threshold = 0.3
            val scaleFactor = 1 / (1 - threshold)
            val targetAlpha =
              if (bottomSheetSwipeTransition.currentState > threshold) {
                  (bottomSheetSwipeTransition.currentState - threshold) * scaleFactor
                } else {
                  0f
                }
                .toFloat()
            alpha = targetAlpha
          }
      )
    }
  }
}
