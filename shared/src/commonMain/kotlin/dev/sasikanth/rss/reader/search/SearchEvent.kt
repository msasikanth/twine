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
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder

sealed interface SearchEvent {

  data class SearchQueryChanged(val query: TextFieldValue) : SearchEvent

  data class SearchPosts(val query: String, val searchSortOrder: SearchSortOrder) : SearchEvent

  data object ClearSearchResults : SearchEvent

  data class SearchSortOrderChanged(val searchSortOrder: SearchSortOrder) : SearchEvent

  data object ClearSearchQuery : SearchEvent

  data class OnPostBookmarkClick(val post: PostWithMetadata) : SearchEvent

  data class UpdatePostReadStatus(val postId: String, val updatedReadStatus: Boolean) : SearchEvent
}
