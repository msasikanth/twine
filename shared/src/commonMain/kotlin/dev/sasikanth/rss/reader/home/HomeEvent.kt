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
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.HomeViewMode

sealed interface HomeEvent {

  data object OnSwipeToRefresh : HomeEvent

  data class OnPostSourceClicked(val feedId: String) : HomeEvent

  data class FeedsSheetStateChanged(val feedsSheetState: SheetValue) : HomeEvent

  data object OnHomeSelected : HomeEvent

  data class OnPostBookmarkClick(val post: ResolvedPost) : HomeEvent

  data class OnPostsTypeChanged(val postsType: PostsType) : HomeEvent

  data class UpdatePostReadStatus(val postId: String, val updatedReadStatus: Boolean) : HomeEvent

  data class MarkPostsAsRead(val source: Source?) : HomeEvent

  data class OnPostItemsScrolled(val postIds: List<String>) : HomeEvent

  data object MarkScrolledPostsAsRead : HomeEvent

  data class MarkFeaturedPostsAsRead(val postId: String) : HomeEvent

  data class ChangeHomeViewMode(val homeViewMode: HomeViewMode) : HomeEvent

  data class UpdateVisibleItemIndex(val index: Int) : HomeEvent

  data object LoadNewArticlesClick : HomeEvent

  data object UpdateDate : HomeEvent

  data class UpdatePrevActiveSource(val source: Source?) : HomeEvent

  data class ShowPostsSortFilter(val show: Boolean) : HomeEvent

  data class OnPostsSortFilterApplied(
    val postsType: PostsType,
    val postsSortOrder: PostsSortOrder,
  ) : HomeEvent
}
