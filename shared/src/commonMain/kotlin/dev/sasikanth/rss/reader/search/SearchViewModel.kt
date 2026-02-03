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
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
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
class SearchViewModel(private val rssRepository: RssRepository) : ViewModel() {

  val sources =
    createPager(config = createPagingConfig(pageSize = 20)) { rssRepository.sources() }
      .flow
      .cachedIn(viewModelScope)

  var searchQuery by mutableStateOf(TextFieldValue())
    private set

  var searchSortOrder by mutableStateOf(SearchSortOrder.Newest)
    private set

  var selectedSource by mutableStateOf<Source?>(null)
    private set

  var onlyBookmarked by mutableStateOf(false)
    private set

  var onlyUnread by mutableStateOf(false)
    private set

  private val _state = MutableStateFlow(SearchState.DEFAULT)
  val state: StateFlow<SearchState>
    get() = _state

  init {
    val searchQueryFlow = snapshotFlow { searchQuery }.debounce(500.milliseconds)
    val searchSortOrderFlow = snapshotFlow { searchSortOrder }
    val selectedSourceFlow = snapshotFlow { selectedSource }
    val onlyBookmarkedFlow = snapshotFlow { onlyBookmarked }
    val onlyUnreadFlow = snapshotFlow { onlyUnread }

    combine(
        searchQueryFlow,
        searchSortOrderFlow,
        selectedSourceFlow,
        onlyBookmarkedFlow,
        onlyUnreadFlow,
      ) { searchQuery, sortOrder, selectedSource, onlyBookmarked, onlyUnread ->
        SearchParameters(searchQuery, sortOrder, selectedSource, onlyBookmarked, onlyUnread)
      }
      .distinctUntilChanged()
      .onEach { (searchQuery, sortOrder, selectedSource, onlyBookmarked, onlyUnread) ->
        if (searchQuery.text.isNotBlank()) {
          val sourceIds =
            when (selectedSource) {
              is Feed -> listOf(selectedSource.id)
              is FeedGroup -> selectedSource.feedIds
              else -> emptyList()
            }
          dispatch(
            SearchEvent.SearchPosts(
              query = searchQuery.text,
              searchSortOrder = sortOrder,
              sourceIds = sourceIds,
              onlyBookmarked = onlyBookmarked,
              onlyUnread = onlyUnread,
            )
          )
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
      is SearchEvent.SearchPosts ->
        searchPosts(
          event.query,
          event.searchSortOrder,
          event.sourceIds,
          event.onlyBookmarked,
          event.onlyUnread,
        )
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
      is SearchEvent.OnSourceChanged -> {
        selectedSource = event.source
        _state.update { it.copy(selectedSource = event.source) }
      }
      is SearchEvent.OnOnlyBookmarkedChanged -> {
        onlyBookmarked = event.onlyBookmarked
        _state.update { it.copy(onlyBookmarked = event.onlyBookmarked) }
      }
      is SearchEvent.OnOnlyUnreadChanged -> {
        onlyUnread = event.onlyUnread
        _state.update { it.copy(onlyUnread = event.onlyUnread) }
      }
    }
  }

  private fun updatePostReadStatus(postId: String, updatedReadStatus: Boolean) {
    viewModelScope.launch {
      rssRepository.updatePostReadStatus(read = updatedReadStatus, id = postId)
    }
  }

  private fun onPostBookmarkClick(post: ResolvedPost) {
    viewModelScope.launch {
      rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
    }
  }

  private fun clearSearchResults() {
    _state.update { it.reset() }
  }

  private fun searchPosts(
    query: String,
    sortOrder: SearchSortOrder,
    sourceIds: List<String>,
    onlyBookmarked: Boolean,
    onlyUnread: Boolean,
  ) {
    val searchResults =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.search(
            searchQuery = query,
            sortOrder = sortOrder,
            sourceIds = sourceIds,
            onlyBookmarked = onlyBookmarked,
            onlyUnread = onlyUnread,
          )
        }
        .flow
        .cachedIn(viewModelScope)

    _state.update { it.copy(searchResults = searchResults) }
  }

  private data class SearchParameters(
    val searchQuery: TextFieldValue,
    val sortOrder: SearchSortOrder,
    val selectedSource: Source?,
    val onlyBookmarked: Boolean,
    val onlyUnread: Boolean,
  )
}
