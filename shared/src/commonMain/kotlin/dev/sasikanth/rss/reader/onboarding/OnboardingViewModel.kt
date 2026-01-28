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

package dev.sasikanth.rss.reader.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.database.FeedPrePopulator
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class OnboardingViewModel(
  private val feedPrePopulator: FeedPrePopulator,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: SyncCoordinator,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(OnboardingState.DEFAULT)
  val state: StateFlow<OnboardingState> = _state.asStateFlow()

  private val _effects = MutableSharedFlow<OnboardingEffect>()
  val effects = _effects.asSharedFlow()

  fun dispatch(event: OnboardingEvent) {
    when (event) {
      OnboardingEvent.GetStartedClicked -> getStartedClicked()
    }
  }

  private fun getStartedClicked() {
    viewModelScope.launch {
      _state.update { it.copy(isPrePopulating = true) }
      withContext(dispatchersProvider.io) {
        val feedsPrePopulated = feedPrePopulator.prePopulate()
        if (feedsPrePopulated) {
          syncCoordinator.pull()
        }
        settingsRepository.completeOnboarding()
      }
      _effects.emit(OnboardingEffect.NavigateToHome)
      _state.update { it.copy(isPrePopulating = false) }
    }
  }
}
