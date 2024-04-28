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
package dev.sasikanth.rss.reader.feeds

import androidx.compose.ui.text.input.TextFieldValue
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.repository.FeedsOrderBy

sealed interface FeedsEvent {

  data object Init : FeedsEvent

  data object OnGoBackClicked : FeedsEvent

  data class OnDeleteFeed(val feed: Feed) : FeedsEvent

  data class OnToggleFeedSelection(val source: Source) : FeedsEvent

  data class OnFeedNameUpdated(val newFeedName: String, val feedId: String) : FeedsEvent

  data class OnFeedPinClicked(val feed: Feed) : FeedsEvent

  data class SearchQueryChanged(val searchQuery: TextFieldValue) : FeedsEvent

  data object ClearSearchQuery : FeedsEvent

  data class OnSourceClick(val source: Source) : FeedsEvent

  data class OnFeedSortOrderChanged(val feedsOrderBy: FeedsOrderBy) : FeedsEvent

  data object OnChangeFeedsViewModeClick : FeedsEvent

  data object TogglePinnedSection : FeedsEvent

  data object OnHomeSelected : FeedsEvent

  data object CancelSourcesSelection : FeedsEvent

  data object PinSelectedSources : FeedsEvent

  data object UnPinSelectedSources : FeedsEvent

  data object DeleteSelectedSources : FeedsEvent

  data class OnCreateGroup(val name: String) : FeedsEvent

  data class OnGroupsSelected(val groupIds: Set<String>) : FeedsEvent

  data class OnEditSourceClicked(val source: Source) : FeedsEvent

  data object OnAddToGroupClicked : FeedsEvent

  data object OnNewFeedClicked : FeedsEvent
}
