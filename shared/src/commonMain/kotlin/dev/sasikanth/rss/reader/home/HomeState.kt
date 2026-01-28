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
@file:OptIn(ExperimentalMaterialApi::class)

package dev.sasikanth.rss.reader.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Immutable
import app.cash.paging.PagingData
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.sync.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDateTime

@Immutable
data class HomeState(
  val posts: Flow<PagingData<PostWithMetadata>>?,
  val syncState: SyncState,
  val feedsSheetState: SheetValue,
  val activeSource: Source?,
  val hasFeeds: Boolean?,
  val postsType: PostsType,
  val postsSortOrder: PostsSortOrder,
  val hasUnreadPosts: Boolean,
  val currentDateTime: LocalDateTime,
  val homeViewMode: HomeViewMode,
  val unreadSinceLastSync: UnreadSinceLastSync?,
  val prevActiveSource: Source?,
  val activePostIndex: Int,
  val showPostsSortFilter: Boolean,
) {

  companion object {

    fun default(currentDateTime: LocalDateTime) =
      HomeState(
        posts = null,
        syncState = SyncState.Idle,
        feedsSheetState = SheetValue.PartiallyExpanded,
        activeSource = null,
        hasFeeds = null,
        postsType = PostsType.ALL,
        postsSortOrder = PostsSortOrder.Latest,
        hasUnreadPosts = false,
        currentDateTime = currentDateTime,
        homeViewMode = HomeViewMode.Default,
        unreadSinceLastSync = null,
        prevActiveSource = null,
        activePostIndex = 0,
        showPostsSortFilter = false,
      )
  }

  val isSyncing: Boolean
    get() = syncState is SyncState.InProgress
}
