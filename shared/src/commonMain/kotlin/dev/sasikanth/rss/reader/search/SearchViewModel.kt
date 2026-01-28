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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.data.repository.RssRepository
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@OptIn(FlowPreview::class)
@Inject
class SearchViewModel(
  private val rssRepository: RssRepository,
) : ViewModel() {

  var searchQuery by mutableStateOf(TextFieldValue())
    private set

  var searchSortOrder by mutableStateOf(SearchSortOrder.Newest)
    private set

  private val _state = MutableStateFlow(SearchState.DEFAULT)
  val state: StateFlow<SearchState>
    get() = _state

  init {
    val searchQueryFlow = snapshotFlow { searchQuery }.debounce(500.milliseconds)
    val searchSortOrderFlow = snapshotFlow { searchSortOrder }

    searchQueryFlow
      .combine(searchSortOrderFlow) { searchQuery, sortOrder -> searchQuery to sortOrder }
      .distinctUntilChanged()
      .onEach { (searchQuery, sortOrder) ->
        if (searchQuery.text.isNotBlank()) {
          dispatch(SearchEvent.SearchPosts(searchQuery.text, sortOrder))
        } else {
          dispatch(SearchEvent.ClearSearchResults)
        }
      }
      .launchIn(viewModelScope)
  }

  fun dispatch(event: SearchEvent) {
    when (event) {
      is SearchEvent.SearchQueryChanged -> {
        searchQuery = event.query
      }
      is SearchEvent.SearchPosts -> searchPosts(event.query, event.searchSortOrder)
      SearchEvent.ClearSearchResults -> clearSearchResults()
      is SearchEvent.SearchSortOrderChanged -> {
        searchSortOrder = event.searchSortOrder
      }
      SearchEvent.ClearSearchQuery -> {
        searchQuery = TextFieldValue()
      }
      is SearchEvent.OnPostBookmarkClick -> onPostBookmarkClick(event.post)
      is SearchEvent.UpdatePostReadStatus ->
        updatePostReadStatus(event.postId, event.updatedReadStatus)
    }
  }

  private fun updatePostReadStatus(postId: String, updatedReadStatus: Boolean) {
    viewModelScope.launch {
      rssRepository.updatePostReadStatus(read = updatedReadStatus, id = postId)
    }
  }

  private fun onPostBookmarkClick(post: PostWithMetadata) {
    viewModelScope.launch {
      rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
    }
  }

  private fun clearSearchResults() {
    _state.update { it.reset() }
  }

  private fun searchPosts(query: String, sortOrder: SearchSortOrder) {
    val searchResults =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.search(query, sortOrder)
        }
        .flow
        .cachedIn(viewModelScope)

    _state.update { it.copy(searchResults = searchResults) }
  }
}
