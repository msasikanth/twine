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

package dev.sasikanth.rss.reader.addfeed

import dev.sasikanth.rss.reader.core.model.local.FeedGroup

sealed interface AddFeedEvent {

  data class AddFeedClicked(val feedLink: String, val name: String?) : AddFeedEvent

  data class OnGroupsSelected(val selectedGroupIds: Set<String>) : AddFeedEvent

  data class OnRemoveGroupClicked(val group: FeedGroup) : AddFeedEvent

  data object MarkGoBackAsDone : AddFeedEvent

  data object MarkErrorAsShown : AddFeedEvent

  data class OnAlwaysFetchSourceArticleChanged(val newValue: Boolean) : AddFeedEvent

  data class OnShowFeedFavIconChanged(val newValue: Boolean) : AddFeedEvent
}
