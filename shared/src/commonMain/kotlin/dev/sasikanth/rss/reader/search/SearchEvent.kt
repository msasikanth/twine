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
package dev.sasikanth.rss.reader.search

import androidx.compose.ui.text.input.TextFieldValue
import dev.sasikanth.rss.reader.database.PostWithMetadata

internal sealed interface SearchEvent {

  data class SearchQueryChanged(val query: TextFieldValue) : SearchEvent

  data class SearchPosts(val query: String, val searchSortOrder: SearchSortOrder) : SearchEvent

  object BackClicked : SearchEvent

  object ClearSearchResults : SearchEvent

  data class SearchSortOrderChanged(val searchSortOrder: SearchSortOrder) : SearchEvent

  object ClearSearchQuery : SearchEvent

  data class OnPostBookmarkClick(val post: PostWithMetadata) : SearchEvent
}
