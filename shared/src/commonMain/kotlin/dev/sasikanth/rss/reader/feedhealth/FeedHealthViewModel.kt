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

package dev.sasikanth.rss.reader.feedhealth

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.core.model.local.FeedSubscriptionHealth
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.utils.calculateInstantBeforePeriod
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class FeedHealthViewModel(
  private val rssRepository: RssRepository,
  private val syncCoordinator: SyncCoordinator,
) : ViewModel() {

  private companion object {
    private const val HIGH_VOLUME_POSTS_THRESHOLD = 150L
    private const val BROKEN_FEED_THRESHOLD = 3L
    private const val STALE_FEED_LIMIT = 25L
    private const val HEALTH_FEED_LIMIT = 10L
    private val STALE_GRACE_PERIOD = 14.days
    private val UNSUBSCRIBE_UNDO_DELAY_MS = 5_000L
  }

  private val _state = MutableStateFlow(FeedHealthState.Default)
  val state: StateFlow<FeedHealthState> = _state

  // Tracks pending delayed unsubscribe jobs, keyed by feedId
  private val pendingUnsubscribeJobs = mutableMapOf<String, Job>()

  init {
    dispatch(FeedHealthEvent.LoadHealthData)
  }

  fun dispatch(event: FeedHealthEvent) {
    when (event) {
      FeedHealthEvent.LoadHealthData -> loadHealthData()
      is FeedHealthEvent.UnsubscribeFeed -> scheduleDeferredUnsubscribe(event.feedId)
      is FeedHealthEvent.RetryFeed -> retryFeed(event.feedId)
      FeedHealthEvent.ClearRetryMessage -> _state.update { it.copy(retryFeedName = null) }
      FeedHealthEvent.UndoUnsubscribe -> undoUnsubscribe()
      is FeedHealthEvent.ToggleFeedSelection -> toggleFeedSelection(event.feedId)
      FeedHealthEvent.ClearSelection -> clearSelection()
      FeedHealthEvent.UnsubscribeSelectedFeeds -> unsubscribeSelectedFeeds()
    }
  }

  private fun loadHealthData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      val timeZone = TimeZone.currentSystemDefault()
      val now = Clock.System.now()
      val sixMonthsAgo = Period.SIX_MONTHS.calculateInstantBeforePeriod()
      val threeMonthsAgo = Period.THREE_MONTHS.calculateInstantBeforePeriod()
      val twoWeeksAgo = now.minus(STALE_GRACE_PERIOD)

      combine(
          rssRepository.staleFeeds(
            sixMonthsAgo = sixMonthsAgo,
            createdBefore = twoWeeksAgo,
            limit = STALE_FEED_LIMIT,
          ),
          rssRepository.highVolumeFeeds(
            after = threeMonthsAgo,
            limit = HEALTH_FEED_LIMIT,
            postsThreshold = HIGH_VOLUME_POSTS_THRESHOLD,
          ),
          rssRepository.leastReadFeeds(threeMonthsAgo, limit = HEALTH_FEED_LIMIT),
          rssRepository.brokenFeeds(threshold = BROKEN_FEED_THRESHOLD, limit = HEALTH_FEED_LIMIT),
        ) { stale, highVolume, leastRead, broken ->
          FeedSubscriptionHealth(
            staleFeeds = stale,
            highVolumeFeeds = highVolume,
            leastReadFeeds = leastRead,
            brokenFeeds = broken,
          )
        }
        .onEach { healthData ->
          _state.update { it.copy(healthData = healthData, isLoading = false) }
        }
        .launchIn(this)
    }
  }

  private fun findFeedName(feedId: String): String =
    _state.value.healthData?.let { data ->
      (data.staleFeeds + data.highVolumeFeeds + data.leastReadFeeds + data.brokenFeeds)
        .find { it.id == feedId }
        ?.name
    } ?: ""

  private fun retryFeed(feedId: String) {
    syncCoordinator.triggerPull(feedId)
    _state.update { it.copy(retryFeedName = findFeedName(feedId)) }
  }

  private fun scheduleDeferredUnsubscribe(feedId: String) {
    // If there's already a pending unsubscribe for a different feed, flush it immediately
    flushAllPendingUnsubscribes(except = feedId)

    val feedName = findFeedName(feedId)

    _state.update { it.copy(pendingUnsubscribe = PendingUnsubscribe(feedId, feedName)) }

    val job =
      viewModelScope.launch {
        delay(UNSUBSCRIBE_UNDO_DELAY_MS)
        rssRepository.removeFeed(feedId)
        _state.update { current ->
          if (current.pendingUnsubscribe?.feedId == feedId) {
            current.copy(pendingUnsubscribe = null)
          } else {
            current
          }
        }
        pendingUnsubscribeJobs.remove(feedId)
      }

    pendingUnsubscribeJobs[feedId] = job
  }

  private fun undoUnsubscribe() {
    val pendingId = _state.value.pendingUnsubscribe?.feedId ?: return
    pendingUnsubscribeJobs.remove(pendingId)?.cancel()
    _state.update { it.copy(pendingUnsubscribe = null) }
  }

  private fun flushAllPendingUnsubscribes(except: String? = null) {
    val toFlush = pendingUnsubscribeJobs.keys.filter { it != except }
    toFlush.forEach { feedId ->
      pendingUnsubscribeJobs.remove(feedId)
      // Let the already-running coroutine finish — it will call removeFeed
    }
    if (toFlush.isNotEmpty()) {
      _state.update { it.copy(pendingUnsubscribe = null) }
    }
  }

  private fun toggleFeedSelection(feedId: String) {
    _state.update { state ->
      val newSelectedIds =
        if (feedId in state.selectedFeedIds) {
          state.selectedFeedIds - feedId
        } else {
          state.selectedFeedIds + feedId
        }
      state.copy(selectedFeedIds = newSelectedIds, isSelectionMode = newSelectedIds.isNotEmpty())
    }
  }

  private fun clearSelection() {
    _state.update { it.copy(selectedFeedIds = emptySet(), isSelectionMode = false) }
  }

  private fun unsubscribeSelectedFeeds() {
    val toRemove = _state.value.selectedFeedIds.toSet()
    clearSelection()
    viewModelScope.launch { toRemove.forEach { feedId -> rssRepository.removeFeed(feedId) } }
  }

  override fun onCleared() {
    super.onCleared()
    // Flush any pending unsubscribes when ViewModel is cleared (user navigated away)
    pendingUnsubscribeJobs.keys.toList().forEach { feedId ->
      pendingUnsubscribeJobs.remove(feedId)
      viewModelScope.launch { rssRepository.removeFeed(feedId) }
    }
  }
}
