/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.addfeed

import dev.sasikanth.rss.reader.core.model.local.FeedGroup

data class AddFeedState(
  val feedFetchingState: FeedFetchingState,
  val selectedFeedGroups: Set<FeedGroup>,
  val alwaysFetchSourceArticle: Boolean,
  val showFeedFavIcon: Boolean,
  val error: AddFeedErrorType?,
  val goBack: Boolean,
) {

  companion object {

    val DEFAULT =
      AddFeedState(
        feedFetchingState = FeedFetchingState.Idle,
        selectedFeedGroups = emptySet(),
        alwaysFetchSourceArticle = false,
        showFeedFavIcon = true,
        error = null,
        goBack = false,
      )
  }
}

enum class FeedFetchingState {
  Idle,
  Loading
}
