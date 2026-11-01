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

package dev.sasikanth.rss.reader.placeholder

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class PlaceholderViewModel(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val refreshPolicy: RefreshPolicy,
) : ViewModel() {

  private val _navigateToHome = MutableStateFlow(false)
  val navigateToHome: StateFlow<Boolean>
    get() = _navigateToHome

  private val _navigateToOnboarding = MutableStateFlow(false)
  val navigateToOnboarding: StateFlow<Boolean>
    get() = _navigateToOnboarding

  init {
    viewModelScope.launch {
      val isOnboardingDone = settingsRepository.isOnboardingDone.first()
      if (isOnboardingDone) {
        setDefaultActiveSource()
        _navigateToHome.value = true
      } else {
        val numberOfFeeds = numberOfFeeds() ?: 0
        if (numberOfFeeds > 0) {
          settingsRepository.completeOnboarding()
          setDefaultActiveSource()
          _navigateToHome.value = true
        } else {
          _navigateToOnboarding.value = true
        }
      }
    }
  }

  private suspend fun setDefaultActiveSource() {
    if (observableActiveSource.currentActiveSource != null) return

    val defaultSourceId = settingsRepository.defaultSourceId.first() ?: return
    val postsType = settingsRepository.postsType.first()
    val lastRefreshedAt = refreshPolicy.lastRefreshedAtFlow.first()
    val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, lastRefreshedAt)

    val source =
      rssRepository
        .source(
          id = defaultSourceId,
          postsAfter = postsAfter,
          postsUpperBound = lastRefreshedAt.toInstant(TimeZone.currentSystemDefault()),
        )
        .firstOrNull()

    if (source != null) {
      observableActiveSource.changeActiveSource(source)
    } else {
      settingsRepository.updateDefaultSourceId(null)
    }
  }

  private suspend fun numberOfFeeds(): Long? {
    return rssRepository.numberOfFeeds().firstOrNull()
  }

  fun markNavigateToHomeAsDone() {
    _navigateToHome.value = false
  }

  fun markNavigateToOnboardingAsDone() {
    _navigateToOnboarding.value = false
  }
}
