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
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source

sealed interface HomeEvent {

  data object Init : HomeEvent

  data object OnSwipeToRefresh : HomeEvent

  data class OnPostClicked(val post: PostWithMetadata) : HomeEvent

  data class OnPostSourceClicked(val feedId: String) : HomeEvent

  data class FeedsSheetStateChanged(val feedsSheetState: SheetValue) : HomeEvent

  data object OnHomeSelected : HomeEvent

  data object BackClicked : HomeEvent

  data object SearchClicked : HomeEvent

  data class OnPostBookmarkClick(val post: PostWithMetadata) : HomeEvent

  data class OnPostsTypeChanged(val postsType: PostsType) : HomeEvent

  data object BookmarksClicked : HomeEvent

  data object SettingsClicked : HomeEvent

  data class TogglePostReadStatus(val postId: String, val postRead: Boolean) : HomeEvent

  data class MarkPostsAsRead(val source: Source?) : HomeEvent

  data class OnPostItemsScrolled(val postIds: List<String>) : HomeEvent

  data object MarkScrolledPostsAsRead : HomeEvent

  data class MarkFeaturedPostsAsRead(val postId: String) : HomeEvent
}
