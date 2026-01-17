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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dev.sasikanth.rss.reader.app.Modals
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.time.LastRefreshedAt
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FeedViewModel(
  private val dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val lastRefreshedAt: LastRefreshedAt,
  @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val feedId = savedStateHandle.toRoute<Modals.FeedInfo>().feedId
  private val _state = MutableStateFlow(FeedState.DEFAULT)
  val state: StateFlow<FeedState>
    get() = _state

  init {
    init()
  }

  fun dispatch(event: FeedEvent) {
    when (event) {
      FeedEvent.RemoveFeedClicked -> removeFeed()
      is FeedEvent.OnFeedNameChanged -> onFeedNameUpdated(event.newFeedName, event.feedId)
      is FeedEvent.OnAlwaysFetchSourceArticleChanged ->
        onAlwaysFetchSourceArticleChanged(event.newValue, event.feedId)
      is FeedEvent.OnShowFeedFavIconChanged ->
        onShowFeedFavIconChanged(event.newValue, event.feedId)
      is FeedEvent.OnMarkPostsAsRead -> onMarkPostsAsRead(event.feedId)
    }
  }

  private fun onMarkPostsAsRead(feedId: String) {
    viewModelScope.launch {
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
    viewModelScope.launch { rssRepository.updateFeedAlwaysFetchSource(feedId, newValue) }
  }

  private fun onShowFeedFavIconChanged(newValue: Boolean, feedId: String) {
    viewModelScope.launch { rssRepository.updateFeedShowFavIcon(feedId, newValue) }
  }

  private fun onFeedNameUpdated(newFeedName: String, feedId: String) {
    viewModelScope.launch { rssRepository.updateFeedName(newFeedName, feedId) }
  }

  private fun removeFeed() {
    viewModelScope.launch {
      rssRepository.removeFeed(feedId)
      observableActiveSource.clearSelection()
      _state.update { it.copy(dismissSheet = true) }
    }
  }

  private fun init() {
    viewModelScope.launch {
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
        .launchIn(viewModelScope)
    }
  }
}
