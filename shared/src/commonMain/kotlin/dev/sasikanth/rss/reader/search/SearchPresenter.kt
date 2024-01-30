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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SearchPresenter(
  rssRepository: RssRepository,
  dispatchersProvider: DispatchersProvider,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
  @Assisted private val openPost: (postLink: String) -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
      )
    }

  internal val state: StateFlow<SearchState> = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()

  internal val searchQuery
    get() = presenterInstance.searchQuery

  internal val searchSortOrder
    get() = presenterInstance.searchSortOrder

  internal fun dispatch(event: SearchEvent) {
    when (event) {
      is SearchEvent.OnPostClicked -> openPost(event.post.link)
      SearchEvent.BackClicked -> goBack()
      else -> {
        /* no-op */
      }
    }
    presenterInstance.dispatch(event)
  }

  @OptIn(FlowPreview::class)
  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    val effects = MutableSharedFlow<SearchEffect>()

    var searchQuery by mutableStateOf(TextFieldValue())
      private set

    var searchSortOrder by mutableStateOf(SearchSortOrder.Newest)
      private set

    private val _state = MutableStateFlow(SearchState.DEFAULT)
    val state: StateFlow<SearchState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchState.DEFAULT
      )

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
        .launchIn(coroutineScope)
    }

    fun dispatch(event: SearchEvent) {
      when (event) {
        is SearchEvent.SearchQueryChanged -> {
          searchQuery = event.query
        }
        is SearchEvent.SearchPosts -> searchPosts(event.query, event.searchSortOrder)
        SearchEvent.BackClicked -> {
          /* no-op */
        }
        SearchEvent.ClearSearchResults -> clearSearchResults()
        is SearchEvent.SearchSortOrderChanged -> {
          searchSortOrder = event.searchSortOrder
        }
        SearchEvent.ClearSearchQuery -> {
          searchQuery = TextFieldValue()
        }
        is SearchEvent.OnPostBookmarkClick -> onPostBookmarkClick(event.post)
        is SearchEvent.OnPostClicked -> {
          // no-op
        }
        is SearchEvent.TogglePostReadStatus -> togglePostReadStatus(event.postLink, event.postRead)
      }
    }

    private fun togglePostReadStatus(postLink: String, postRead: Boolean) {
      coroutineScope.launch {
        rssRepository.updatePostReadStatus(read = !postRead, link = postLink)
      }
    }

    private fun onPostBookmarkClick(post: PostWithMetadata) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, link = post.link)
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
          .cachedIn(coroutineScope)

      _state.update { it.copy(searchResults = searchResults) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
