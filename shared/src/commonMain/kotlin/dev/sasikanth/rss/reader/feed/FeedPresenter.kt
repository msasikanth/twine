/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.feed

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FeedPresenter(
  dispatchersProvider: DispatchersProvider,
  rssRepository: RssRepository,
  @Assisted feedLink: String,
  @Assisted componentContext: ComponentContext,
  @Assisted private val dismiss: () -> Unit
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        feedLink = feedLink
      )
    }

  internal val state: StateFlow<FeedState> = presenterInstance.state

  fun dispatch(event: FeedEvent) {
    when (event) {
      FeedEvent.BackClicked -> dismiss()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val feedLink: String
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(FeedState.DEFAULT)
    val state: StateFlow<FeedState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedState.DEFAULT
      )

    fun dispatch(event: FeedEvent) {
      when (event) {
        FeedEvent.Init -> init()
        FeedEvent.BackClicked -> {
          // no-op
        }
        FeedEvent.RemoveFeedClicked -> removeFeed()
        is FeedEvent.OnFeedNameChanged -> onFeedNameUpdated(event.newFeedName, event.feedLink)
      }
    }

    private fun onFeedNameUpdated(newFeedName: String, feedLink: String) {
      coroutineScope.launch { rssRepository.updateFeedName(newFeedName, feedLink) }
    }

    private fun removeFeed() {
      coroutineScope.launch { rssRepository.removeFeed(feedLink) }
    }

    private fun init() {
      coroutineScope.launch {
        val feed = rssRepository.feed(feedLink)
        _state.update { it.copy(feed = feed) }
      }
    }
  }
}
