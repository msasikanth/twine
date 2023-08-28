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
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SearchPresenter(
  rssRepository: RssRepository,
  dispatchersProvider: DispatchersProvider,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
      )
    }

  val state: StateFlow<SearchState> = presenterInstance.state
  val searchQuery
    get() = presenterInstance.searchQuery

  internal fun dispatch(event: SearchEvent) {
    when (event) {
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

    var searchQuery by mutableStateOf("")
      private set

    private val _state = MutableStateFlow(SearchState.DEFAULT)
    val state: StateFlow<SearchState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchState.DEFAULT
      )

    init {
      snapshotFlow { searchQuery }
        .debounce(500.milliseconds)
        .distinctUntilChanged()
        .onEach {
          if (it.isNotBlank()) {
            dispatch(SearchEvent.SearchPosts(it))
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
        is SearchEvent.SearchPosts -> searchPosts(event.query)
        SearchEvent.BackClicked -> {
          /* no-op */
        }
        SearchEvent.ClearSearchResults -> clearSearchResults()
      }
    }

    private fun clearSearchResults() {
      _state.update { it.reset() }
    }

    private fun searchPosts(query: String) {
      rssRepository
        .search(query)
        .onStart { _state.update { it.copy(searchInProgress = true) } }
        .onEach { searchResults ->
          _state.update { it.copy(searchResults = searchResults.toImmutableList()) }
        }
        .onCompletion { _state.update { it.copy(searchInProgress = false) } }
        .launchIn(coroutineScope)
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
