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

import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import dev.sasikanth.rss.reader.database.PostWithMetadata

sealed interface HomeEvent {

  object Init : HomeEvent

  object OnSwipeToRefresh : HomeEvent

  data class OnPostClicked(val post: PostWithMetadata) : HomeEvent

  data class FeedsSheetStateChanged(val feedsSheetState: BottomSheetValue) : HomeEvent

  object OnHomeSelected : HomeEvent
}
