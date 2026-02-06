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

package dev.sasikanth.rss.reader.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class DiscoveryViewModel(
  private val discoveryRepository: DiscoveryRepository,
  private val rssRepository: RssRepository,
  private val feedFetcher: FeedFetcher,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(DiscoveryState.DEFAULT)
  val state: StateFlow<DiscoveryState> = _state.asStateFlow()

  init {
    dispatch(DiscoveryEvent.LoadDiscoveryGroups)

    rssRepository
      .allFeeds()
      .onEach { feeds ->
        val addedFeedLinks = feeds.map { it.link }.toSet()
        _state.update { it.copy(addedFeedLinks = addedFeedLinks) }
      }
      .launchIn(viewModelScope)
  }

  fun dispatch(event: DiscoveryEvent) {
    when (event) {
      DiscoveryEvent.LoadDiscoveryGroups -> loadDiscoveryGroups()
      is DiscoveryEvent.SearchQueryChanged -> {
        _state.update { it.copy(searchQuery = event.query) }
      }
      is DiscoveryEvent.AddFeedClicked -> addFeed(event.link)
    }
  }

  private fun loadDiscoveryGroups() {
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true) }
      val groups = discoveryRepository.groups()
      _state.update { it.copy(groups = groups.toImmutableList(), isLoading = false) }
    }
  }

  private fun addFeed(link: String) {
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(inProgressFeedLinks = it.inProgressFeedLinks + link) }
      try {
        when (val result = feedFetcher.fetch(link)) {
          is FeedFetchResult.Success -> {
            rssRepository.upsertFeedWithPosts(feedPayload = result.feedPayload)
          }
          else -> {
            // Handle error or just ignore for now in discovery
          }
        }
      } catch (e: Exception) {
        // Ignore
      } finally {
        _state.update { it.copy(inProgressFeedLinks = it.inProgressFeedLinks - link) }
      }
    }
  }
}
