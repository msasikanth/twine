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

package dev.sasikanth.rss.reader.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue.Collapsed
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode
import dev.sasikanth.rss.reader.home.HomeLoadingState.Loading
import dev.sasikanth.rss.reader.models.local.Feed
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Immutable
internal data class HomeState(
  val featuredPosts: ImmutableList<PostWithMetadata>,
  val posts: Flow<PagingData<PostWithMetadata>>,
  val loadingState: HomeLoadingState,
  val feedsSheetState: BottomSheetValue,
  val selectedFeed: Feed?,
  val feedsSheetMode: FeedsSheetMode,
  val feedFetchingState: FeedFetchingState
) {

  companion object {

    val DEFAULT =
      HomeState(
        featuredPosts = persistentListOf(),
        posts = emptyFlow(),
        loadingState = HomeLoadingState.Idle,
        feedsSheetState = Collapsed,
        selectedFeed = null,
        feedsSheetMode = FeedsSheetMode.Default,
        feedFetchingState = FeedFetchingState.Idle
      )
  }

  val isAllFeedsSelected: Boolean
    get() = selectedFeed == null

  val isRefreshing: Boolean
    get() = loadingState == Loading

  val isFetchingFeed: Boolean
    get() = feedFetchingState == FeedFetchingState.Loading
}

sealed interface HomeLoadingState {
  object Idle : HomeLoadingState

  object Loading : HomeLoadingState

  data class Error(val errorMessage: String) : HomeLoadingState
}

enum class FeedFetchingState {
  Idle,
  Loading
}
