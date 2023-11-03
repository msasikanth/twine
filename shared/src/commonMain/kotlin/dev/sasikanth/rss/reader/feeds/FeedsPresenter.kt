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
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.models.local.Feed
import dev.sasikanth.rss.reader.repository.ObservableSelectedFeed
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
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
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FeedsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val observableSelectedFeed: ObservableSelectedFeed,
  @Assisted componentContext: ComponentContext
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        observableSelectedFeed = observableSelectedFeed
      )
    }

  internal val state: StateFlow<FeedsState> = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()
  internal val searchQuery
    get() = presenterInstance.searchQuery

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(FeedsEvent.Init) }
  }

  fun dispatch(event: FeedsEvent) = presenterInstance.dispatch(event)

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val observableSelectedFeed: ObservableSelectedFeed
  ) : InstanceKeeper.Instance {

    var searchQuery by mutableStateOf(TextFieldValue())
      private set

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(FeedsState.DEFAULT)
    val state: StateFlow<FeedsState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedsState.DEFAULT
      )

    val effects = MutableSharedFlow<FeedsEffect>(extraBufferCapacity = 10)

    fun dispatch(event: FeedsEvent) {
      when (event) {
        FeedsEvent.Init -> init()
        FeedsEvent.OnGoBackClicked -> onGoBackClicked()
        is FeedsEvent.OnDeleteFeed -> onDeleteFeed(event.feed)
        is FeedsEvent.OnFeedSelected -> onFeedSelected(event.feed)
        is FeedsEvent.OnFeedNameUpdated -> onFeedNameUpdated(event.newFeedName, event.feedLink)
        is FeedsEvent.OnFeedPinClicked -> onFeedPinClicked(event.feed)
        FeedsEvent.ClearSearchQuery -> clearSearchQuery()
        is FeedsEvent.SearchQueryChanged -> onSearchQueryChanged(event.searchQuery)
      }
    }

    private fun onSearchQueryChanged(searchQuery: TextFieldValue) {
      this.searchQuery = searchQuery
    }

    private fun clearSearchQuery() {
      searchQuery = TextFieldValue()
    }

    private fun onFeedPinClicked(feed: Feed) {
      coroutineScope.launch { rssRepository.toggleFeedPinStatus(feed) }
    }

    private fun onFeedNameUpdated(newFeedName: String, feedLink: String) {
      coroutineScope.launch { rssRepository.updateFeedName(newFeedName, feedLink) }
    }

    private fun onDeleteFeed(feed: Feed) {
      coroutineScope.launch {
        rssRepository.removeFeed(feed.link)
        if (_state.value.selectedFeed == feed) {
          observableSelectedFeed.clearSelection()
        }
      }
    }

    private fun onFeedSelected(feed: Feed) {
      coroutineScope.launch {
        observableSelectedFeed.selectFeed(feed)
        effects.emit(FeedsEffect.MinimizeSheet)
      }
    }

    private fun onGoBackClicked() {
      coroutineScope.launch { effects.emit(FeedsEffect.MinimizeSheet) }
    }

    @OptIn(FlowPreview::class)
    private fun init() {
      val feeds =
        createPager(config = createPagingConfig(pageSize = 20)) { rssRepository.allFeeds() }
          .flow
          .cachedIn(coroutineScope)

      observableSelectedFeed.selectedFeed
        .flatMapLatest { selectedFeed ->
          rssRepository.numberOfPinnedFeeds().map { numberOfPinnedFeeds ->
            Triple(selectedFeed, feeds, numberOfPinnedFeeds)
          }
        }
        .distinctUntilChanged()
        .onEach { (selectedFeed, feeds, numberOfPinnedFeeds) ->
          _state.update {
            it.copy(
              feeds = feeds,
              numberOfPinnedFeeds = numberOfPinnedFeeds,
              selectedFeed = selectedFeed
            )
          }
        }
        .launchIn(coroutineScope)

      val searchQueryFlow = snapshotFlow { searchQuery }.debounce(500.milliseconds)
      searchQueryFlow
        .distinctUntilChanged()
        .onEach { searchQuery ->
          val searchQueryText = searchQuery.text
          val transformedSearchQuery =
            if (searchQueryText.length >= 3) {
              searchQueryText
            } else {
              ""
            }

          val feedSearchResults =
            createPager(config = createPagingConfig(pageSize = 20)) {
                rssRepository.searchFeed(transformedSearchQuery)
              }
              .flow
              .cachedIn(coroutineScope)

          _state.update { it.copy(feedsSearchResults = feedSearchResults) }
        }
        .launchIn(coroutineScope)
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
