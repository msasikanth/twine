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
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.time.LastRefreshedAt
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias FeedPresenterFactory =
  (
    feedId: String,
    ComponentContext,
    dismiss: () -> Unit,
  ) -> FeedPresenter

@Inject
class FeedPresenter(
  dispatchersProvider: DispatchersProvider,
  rssRepository: RssRepository,
  settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val lastRefreshedAt: LastRefreshedAt,
  @Assisted feedId: String,
  @Assisted componentContext: ComponentContext,
  @Assisted private val dismiss: () -> Unit
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        settingsRepository = settingsRepository,
        feedId = feedId,
        observableActiveSource = observableActiveSource,
        lastRefreshedAt = lastRefreshedAt,
      )
    }

  internal val state: StateFlow<FeedState> = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()

  init {
    lifecycle.doOnCreate { dispatch(FeedEvent.Init) }
  }

  fun dispatch(event: FeedEvent) {
    when (event) {
      FeedEvent.BackClicked,
      FeedEvent.DismissSheet -> dismiss()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    private val dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val settingsRepository: SettingsRepository,
    private val feedId: String,
    private val observableActiveSource: ObservableActiveSource,
    private val lastRefreshedAt: LastRefreshedAt,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(FeedState.DEFAULT)
    val state: StateFlow<FeedState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedState.DEFAULT
      )

    val effects = MutableSharedFlow<FeedEffect>()

    fun dispatch(event: FeedEvent) {
      when (event) {
        FeedEvent.Init -> init()
        FeedEvent.BackClicked,
        FeedEvent.DismissSheet -> {
          // no-op
        }
        FeedEvent.RemoveFeedClicked -> removeFeed()
        is FeedEvent.OnFeedNameChanged -> onFeedNameUpdated(event.newFeedName, event.feedId)
        is FeedEvent.OnAlwaysFetchSourceArticleChanged ->
          onAlwaysFetchSourceArticleChanged(event.newValue, event.feedId)
        is FeedEvent.OnMarkPostsAsRead -> onMarkPostsAsRead(event.feedId)
      }
    }

    private fun onMarkPostsAsRead(feedId: String) {
      coroutineScope.launch {
        val (postsType, dateTime) =
          withContext(dispatchersProvider.io) {
            val postsType = settingsRepository.postsType.first()
            val dateTime = lastRefreshedAt.dateTimeFlow.first()

            Pair(postsType, dateTime)
          }
        val postsAfter =
          PostsFilterUtils.postsThresholdTime(postsType = postsType, dateTime = dateTime)

        rssRepository.markPostsInFeedAsRead(feedIds = listOf(feedId), postsAfter = postsAfter)
      }
    }

    private fun onAlwaysFetchSourceArticleChanged(newValue: Boolean, feedId: String) {
      coroutineScope.launch { rssRepository.updateFeedAlwaysFetchSource(feedId, newValue) }
    }

    private fun onFeedNameUpdated(newFeedName: String, feedId: String) {
      coroutineScope.launch { rssRepository.updateFeedName(newFeedName, feedId) }
    }

    private fun removeFeed() {
      coroutineScope.launch {
        rssRepository.removeFeed(feedId)
        observableActiveSource.clearSelection()
        effects.emit(FeedEffect.DismissSheet)
      }
    }

    private fun init() {
      coroutineScope.launch {
        val (postsType, dateTime) =
          withContext(dispatchersProvider.io) {
            val postsType = settingsRepository.postsType.first()
            val dateTime = lastRefreshedAt.dateTimeFlow.first()

            Pair(postsType, dateTime)
          }
        val postsAfter =
          PostsFilterUtils.postsThresholdTime(postsType = postsType, dateTime = dateTime)

        rssRepository
          .feed(
            feedId = feedId,
            postsAfter = postsAfter,
            lastSyncedAt = dateTime.toInstant(TimeZone.currentSystemDefault())
          )
          .onEach { feed -> _state.update { it.copy(feed = feed) } }
          .catch {
            // no-op
            // When we delete a feed, this flow crashes because, that feed is no longer available
          }
          .launchIn(coroutineScope)
      }
    }
  }
}
