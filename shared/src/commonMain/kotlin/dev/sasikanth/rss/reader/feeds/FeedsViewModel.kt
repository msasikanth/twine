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

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import dev.sasikanth.rss.reader.utils.ObservableSelectedFeed
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedsViewModel(
  lifecycle: Lifecycle,
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val observableSelectedFeed: ObservableSelectedFeed
) : InstanceKeeper.Instance {

  private val viewModelScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  private val _state = MutableStateFlow(FeedsState.DEFAULT)
  val state: StateFlow<FeedsState> =
    _state.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = FeedsState.DEFAULT
    )

  private val _effects = MutableSharedFlow<FeedsEffect>(extraBufferCapacity = 10)
  val effects = _effects.asSharedFlow()

  init {
    lifecycle.doOnCreate { dispatch(FeedsEvent.Init) }
  }

  fun dispatch(event: FeedsEvent) {
    when (event) {
      FeedsEvent.Init -> init()
      FeedsEvent.OnAddFeedClicked -> onAddFeedClicked()
      FeedsEvent.OnCancelAddFeedClicked -> onCancelAddFeedClicked()
      is FeedsEvent.AddFeed -> addFeed(event.feedLink)
      FeedsEvent.OnGoBackClicked -> onGoBackClicked()
      is FeedsEvent.OnFeedSelected -> onFeedSelected(event.feed)
      FeedsEvent.OnHomeSelected -> onHomeSelected()
    }
  }

  private fun onHomeSelected() {
    viewModelScope.launch { observableSelectedFeed.clearSelection() }
  }

  private fun onFeedSelected(feed: Feed) {
    viewModelScope.launch { observableSelectedFeed.selectFeed(feed) }
  }

  private fun onGoBackClicked() {
    viewModelScope.launch { _effects.emit(FeedsEffect.MinimizeAddSheet) }
  }

  private fun addFeed(feedLink: String) {
    viewModelScope.launch { rssRepository.addFeed(feedLink) }
  }

  private fun onCancelAddFeedClicked() {
    _state.update { it.copy(canShowFeedLinkEntry = false) }
  }

  private fun onAddFeedClicked() {
    _state.update { it.copy(canShowFeedLinkEntry = false) }
  }

  private fun init() {
    rssRepository
      .allFeeds()
      .onEach { feeds -> _state.update { it.copy(feeds = feeds.toImmutableList()) } }
      .launchIn(viewModelScope)

    observableSelectedFeed.selectedFeed
      .onEach { selectedFeed -> _state.update { it.copy(selectedFeed = selectedFeed) } }
      .launchIn(viewModelScope)
  }

  override fun onDestroy() {
    viewModelScope.cancel()
  }
}
