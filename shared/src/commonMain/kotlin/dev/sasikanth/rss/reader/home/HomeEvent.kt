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
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.home.ui.PostsType

sealed interface HomeEvent {

  data object Init : HomeEvent

  data object OnSwipeToRefresh : HomeEvent

  data class OnPostClicked(val post: PostWithMetadata) : HomeEvent

  data class OnPostSourceClicked(val feedLink: String) : HomeEvent

  data class FeedsSheetStateChanged(val feedsSheetState: BottomSheetValue) : HomeEvent

  data object OnHomeSelected : HomeEvent

  data object OnAddFeedClicked : HomeEvent

  data object OnPrimaryActionClicked : HomeEvent

  data object OnCancelAddFeedClicked : HomeEvent

  data class AddFeed(val feedLink: String) : HomeEvent

  data object BackClicked : HomeEvent

  data object SearchClicked : HomeEvent

  data class OnPostBookmarkClick(val post: PostWithMetadata) : HomeEvent

  data class OnPostsTypeChanged(val postsType: PostsType) : HomeEvent

  data object BookmarksClicked : HomeEvent

  data object SettingsClicked : HomeEvent

  data object EditFeedsClicked : HomeEvent

  data object ExitFeedsEdit : HomeEvent

  data class TogglePostReadStatus(val postLink: String, val postRead: Boolean) : HomeEvent
}
