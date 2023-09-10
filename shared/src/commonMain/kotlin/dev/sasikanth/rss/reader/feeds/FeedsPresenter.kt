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

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
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

  val state: StateFlow<FeedsState> = presenterInstance.state
  val effects = presenterInstance.effects.asSharedFlow()

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(FeedsEvent.Init) }
  }

  fun dispatch(event: FeedsEvent) = presenterInstance.dispatch(event)

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val observableSelectedFeed: ObservableSelectedFeed
  ) : InstanceKeeper.Instance {

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
      }
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
      coroutineScope.launch { observableSelectedFeed.selectFeed(feed) }
    }

    private fun onGoBackClicked() {
      coroutineScope.launch { effects.emit(FeedsEffect.MinimizeSheet) }
    }

    private fun init() {
      rssRepository
        .allFeeds()
        .onEach { feeds ->
          val feedsGroup = feeds.groupBy { it.pinnedAt != null }.entries
          val pinnedFeeds = feedsGroup.firstOrNull()?.value.orEmpty().sortedBy { it.pinnedAt }
          val unPinnedFeeds = feedsGroup.elementAtOrNull(1)?.value.orEmpty()

          _state.update {
            it.copy(
              pinnedFeeds = pinnedFeeds.toImmutableList(),
              feeds = unPinnedFeeds.toImmutableList()
            )
          }
        }
        .launchIn(coroutineScope)

      rssRepository
        .numberOfPinnedFeeds()
        .onEach { numberOfPinnedFeeds ->
          _state.update { it.copy(numberOfPinnedFeeds = numberOfPinnedFeeds) }
        }
        .launchIn(coroutineScope)

      observableSelectedFeed.selectedFeed
        .onEach { selectedFeed -> _state.update { it.copy(selectedFeed = selectedFeed) } }
        .launchIn(coroutineScope)
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
