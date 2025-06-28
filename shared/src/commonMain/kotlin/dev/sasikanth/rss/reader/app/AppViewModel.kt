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
package dev.sasikanth.rss.reader.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.time.LastRefreshedAt
import dev.sasikanth.rss.reader.di.scopes.ActivityScope
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.NTuple4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
@ActivityScope
class AppViewModel(
  private val dispatchersProvider: DispatchersProvider,
  private val lastRefreshedAt: LastRefreshedAt,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val linkHandler: LinkHandler,
  private val syncCoordinator: SyncCoordinator,
) : ViewModel() {

  private val _state = MutableStateFlow(AppState.DEFAULT)
  val state: StateFlow<AppState>
    get() = _state

  init {
    refreshFeedsIfExpired()
    combine(
        settingsRepository.appThemeMode,
        settingsRepository.showFeedFavIcon,
        settingsRepository.homeViewMode,
        settingsRepository.showReaderView,
      ) { appThemeMode, showFeedFavIcon, homeViewMode, showReaderView ->
        NTuple4(appThemeMode, showFeedFavIcon, homeViewMode, showReaderView)
      }
      .onEach { (appThemeMode, showFeedFavIcon, homeViewMode, showReaderView) ->
        _state.update {
          it.copy(
            appThemeMode = appThemeMode,
            showFeedFavIcon = showFeedFavIcon,
            homeViewMode = homeViewMode,
            showReaderView = showReaderView,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  fun markPostAsRead(id: String) {
    viewModelScope.launch { rssRepository.updatePostReadStatus(read = true, id = id) }
  }

  fun updateActivePostIndex(index: Int) {
    _state.update { it.copy(activePostIndex = index) }
  }

  private fun refreshFeedsIfExpired() {
    viewModelScope.launch {
      if (lastRefreshedAt.hasExpired()) {
        syncCoordinator.refreshFeeds()
      }
    }
  }
}
