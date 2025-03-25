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
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.home.HomeLoadingState.Loading
import kotlinx.coroutines.flow.Flow

@Immutable
internal data class HomeState(
  val posts: Flow<PagingData<PostWithMetadata>>?,
  val loadingState: HomeLoadingState,
  val feedsSheetState: SheetValue,
  val activeSource: Source?,
  val hasFeeds: Boolean?,
  val postsType: PostsType,
  val hasUnreadPosts: Boolean
) {

  companion object {

    val DEFAULT =
      HomeState(
        posts = null,
        loadingState = HomeLoadingState.Idle,
        feedsSheetState = SheetValue.PartiallyExpanded,
        activeSource = null,
        hasFeeds = null,
        postsType = PostsType.ALL,
        hasUnreadPosts = false,
      )
  }

  val isRefreshing: Boolean
    get() = loadingState == Loading
}

sealed interface HomeLoadingState {
  data object Idle : HomeLoadingState

  data object Loading : HomeLoadingState

  data class Error(val errorMessage: String) : HomeLoadingState
}
