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
package dev.sasikanth.rss.reader.search

import androidx.compose.ui.text.input.TextFieldValue
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source

sealed interface SearchEvent {

  data class SearchQueryChanged(val query: TextFieldValue) : SearchEvent

  data class SearchPosts(
    val query: String,
    val searchSortOrder: SearchSortOrder,
    val sourceIds: List<String> = emptyList(),
    val onlyBookmarked: Boolean = false,
    val onlyUnread: Boolean = false,
  ) : SearchEvent

  data object ClearSearchResults : SearchEvent

  data class SearchSortOrderChanged(val searchSortOrder: SearchSortOrder) : SearchEvent

  data class OnOnlyBookmarkedChanged(val onlyBookmarked: Boolean) : SearchEvent

  data class OnOnlyUnreadChanged(val onlyUnread: Boolean) : SearchEvent

  data object ClearSearchQuery : SearchEvent

  data class OnPostBookmarkClick(val post: ResolvedPost) : SearchEvent

  data class UpdatePostReadStatus(val postId: String, val updatedReadStatus: Boolean) : SearchEvent

  data class OnSourceChanged(val source: Source?) : SearchEvent
}
