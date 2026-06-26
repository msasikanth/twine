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
import dev.sasikanth.rss.reader.utils.calculateInstantBeforePeriod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class FeedHealthViewModel(private val rssRepository: RssRepository) : ViewModel() {

  private val _state = MutableStateFlow(FeedHealthState.Default)
  val state: StateFlow<FeedHealthState> = _state

  init {
    dispatch(FeedHealthEvent.LoadHealthData)
  }

  fun dispatch(event: FeedHealthEvent) {
    when (event) {
      FeedHealthEvent.LoadHealthData -> loadHealthData()
      is FeedHealthEvent.UnsubscribeFeed -> unsubscribeFeed(event.feedId)
    }
  }

  private fun loadHealthData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      val sixMonthsAgo = Period.SIX_MONTHS.calculateInstantBeforePeriod()
      val threeMonthsAgo = Period.THREE_MONTHS.calculateInstantBeforePeriod()

      combine(
          rssRepository.staleFeeds(sixMonthsAgo),
          rssRepository.highVolumeFeeds(threeMonthsAgo, limit = 10),
          rssRepository.leastReadFeeds(threeMonthsAgo, limit = 10),
        ) { stale, highVolume, leastRead ->
          FeedSubscriptionHealth(
            staleFeeds = stale,
            highVolumeFeeds = highVolume,
            leastReadFeeds = leastRead,
          )
        }
        .onEach { healthData ->
          _state.update { it.copy(healthData = healthData, isLoading = false) }
        }
        .launchIn(this)
    }
  }

  private fun unsubscribeFeed(feedId: String) {
    viewModelScope.launch { rssRepository.removeFeed(feedId) }
  }
}
