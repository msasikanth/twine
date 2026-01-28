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

package dev.sasikanth.rss.reader.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.repository.RssRepository
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class StatisticsViewModel(private val rssRepository: RssRepository) : ViewModel() {

  private val _state = MutableStateFlow(StatisticsState())
  val state: StateFlow<StatisticsState>
    get() = _state

  init {
    dispatch(StatisticsEvent.LoadStatistics)
  }

  fun dispatch(event: StatisticsEvent) {
    when (event) {
      StatisticsEvent.LoadStatistics -> loadStatistics()
    }
  }

  private fun loadStatistics() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      val startDate = Clock.System.now() - 30.days

      rssRepository
        .getReadingStatistics(startDate)
        .onEach { statistics ->
          _state.update { it.copy(statistics = statistics, isLoading = false) }
        }
        .launchIn(this)
    }
  }
}
