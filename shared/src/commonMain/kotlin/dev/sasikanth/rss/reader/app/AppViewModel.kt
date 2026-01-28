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
package dev.sasikanth.rss.reader.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.auth.OAuthManager
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.utils.NTuple7
import dev.sasikanth.rss.reader.utils.combine
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AppViewModel(
  private val refreshPolicy: RefreshPolicy,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: SyncCoordinator,
  private val oAuthManager: OAuthManager,
  private val linkHandler: LinkHandler,
) : ViewModel() {

  private val _state = MutableStateFlow(AppState.DEFAULT)
  val state: StateFlow<AppState>
    get() = _state

  init {
    refreshFeedsIfExpired()
    setupSessionTracking()

    combine(
        settingsRepository.appThemeMode,
        settingsRepository.useAmoled,
        settingsRepository.dynamicColorEnabled,
        settingsRepository.showFeedFavIcon,
        settingsRepository.homeViewMode,
        settingsRepository.showReaderView,
        settingsRepository.blockImages,
      ) {
        appThemeMode,
        useAmoled,
        dynamicColorEnabled,
        showFeedFavIcon,
        homeViewMode,
        showReaderView,
        blockImages ->
        NTuple7(
          appThemeMode,
          useAmoled,
          dynamicColorEnabled,
          showFeedFavIcon,
          homeViewMode,
          showReaderView,
          blockImages
        )
      }
      .onEach {
        (
          appThemeMode,
          useAmoled,
          dynamicColorEnabled,
          showFeedFavIcon,
          homeViewMode,
          showReaderView,
          blockImages) ->
        _state.update {
          it.copy(
            appThemeMode = appThemeMode,
            useAmoled = useAmoled,
            dynamicColorEnabled = dynamicColorEnabled,
            showFeedFavIcon = showFeedFavIcon,
            homeViewMode = homeViewMode,
            showReaderView = showReaderView,
            blockImages = blockImages,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  fun onPostOpened(postId: String, index: Int) {
    updateActivePostIndex(index)
    markPostAsRead(postId)
  }

  fun markPostAsRead(id: String) {
    viewModelScope.launch { rssRepository.updatePostReadStatus(read = true, id = id) }
  }

  fun updateActivePostIndex(index: Int) {
    _state.update { it.copy(activePostIndex = index) }
  }

  fun onOAuthRedirect(uri: String) {
    viewModelScope.launch {
      val signInSucceeded = oAuthManager.handleRedirect(uri)
      if (signInSucceeded) {
        syncCoordinator.push()
      }
      linkHandler.close()
    }
  }

  private fun refreshFeedsIfExpired() {
    viewModelScope.launch {
      if (refreshPolicy.hasExpired()) {
        syncCoordinator.pull()
      }
    }
  }

  private fun setupSessionTracking() {
    viewModelScope.launch {
      val currentTime = Clock.System.now()
      val installDate = settingsRepository.installDate.first()

      if (installDate == null) {
        settingsRepository.updateInstallDate(currentTime)
      }

      settingsRepository.incrementUserSessionCount()
    }
  }
}
