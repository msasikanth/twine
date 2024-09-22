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
package dev.sasikanth.rss.reader.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.opml.OpmlManager
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.combine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias SettingsPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
    openAbout: () -> Unit,
  ) -> SettingsPresenter

@Inject
class SettingsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val settingsRepository: SettingsRepository,
  private val rssRepository: RssRepository,
  private val appInfo: AppInfo,
  private val opmlManager: OpmlManager,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
  @Assisted private val openAbout: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        appInfo = appInfo,
        settingsRepository = settingsRepository,
        rssRepository = rssRepository,
        opmlManager = opmlManager,
      )
    }

  internal val state = presenterInstance.state

  fun dispatch(event: SettingsEvent) {
    when (event) {
      SettingsEvent.BackClicked -> goBack()
      SettingsEvent.AboutClicked -> openAbout()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    appInfo: AppInfo,
    rssRepository: RssRepository,
    private val settingsRepository: SettingsRepository,
    private val opmlManager: OpmlManager,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(SettingsState.default(appInfo))
    val state: StateFlow<SettingsState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState.default(appInfo)
      )

    init {
      combine(
          settingsRepository.browserType,
          settingsRepository.showUnreadPostsCount,
          settingsRepository.postsDeletionPeriod,
          settingsRepository.showReaderView,
          settingsRepository.appThemeMode,
          rssRepository.hasFeeds()
        ) {
          browserType,
          showUnreadPostsCount,
          postsDeletionPeriod,
          showReaderView,
          appThemeMode,
          hasFeeds ->
          Settings(
            browserType = browserType,
            showUnreadPostsCount = showUnreadPostsCount,
            hasFeeds = hasFeeds,
            postsDeletionPeriod = postsDeletionPeriod,
            showReaderView = showReaderView,
            appThemeMode = appThemeMode,
          )
        }
        .onEach { settings ->
          _state.update {
            it.copy(
              browserType = settings.browserType,
              showUnreadPostsCount = settings.showUnreadPostsCount,
              hasFeeds = settings.hasFeeds,
              postsDeletionPeriod = settings.postsDeletionPeriod,
              showReaderView = settings.showReaderView,
              appThemeMode = settings.appThemeMode,
            )
          }
        }
        .launchIn(coroutineScope)

      opmlManager.result
        .onEach { result -> _state.update { it.copy(opmlResult = result) } }
        .launchIn(coroutineScope)
    }

    fun dispatch(event: SettingsEvent) {
      when (event) {
        SettingsEvent.BackClicked -> {
          // no-op
        }
        is SettingsEvent.UpdateBrowserType -> updateBrowserType(event.browserType)
        is SettingsEvent.ToggleShowUnreadPostsCount -> toggleShowUnreadPostsCount(event.value)
        is SettingsEvent.ToggleShowReaderView -> toggleShowReaderView(event.value)
        SettingsEvent.AboutClicked -> {
          // no-op
        }
        SettingsEvent.ImportOpmlClicked -> importOpmlClicked()
        SettingsEvent.ExportOpmlClicked -> exportOpmlClicked()
        SettingsEvent.CancelOpmlImportOrExport -> cancelOpmlImportOrExport()
        is SettingsEvent.PostsDeletionPeriodChanged -> postsDeletionPeriodChanged(event.newPeriod)
        is SettingsEvent.OnAppThemeModeChanged -> onAppThemeModeChanged(event.appThemeMode)
      }
    }

    private fun onAppThemeModeChanged(appThemeMode: AppThemeMode) {
      coroutineScope.launch { settingsRepository.updateAppTheme(appThemeMode) }
    }

    private fun toggleShowReaderView(value: Boolean) {
      coroutineScope.launch { settingsRepository.toggleShowReaderView(value) }
    }

    private fun postsDeletionPeriodChanged(newPeriod: Period) {
      coroutineScope.launch { settingsRepository.updatePostsDeletionPeriod(newPeriod) }
    }

    private fun toggleShowUnreadPostsCount(value: Boolean) {
      coroutineScope.launch { settingsRepository.toggleShowUnreadPostsCount(value) }
    }

    private fun cancelOpmlImportOrExport() {
      opmlManager.cancel()
    }

    private fun exportOpmlClicked() {
      coroutineScope.launch { opmlManager.export() }
    }

    private fun importOpmlClicked() {
      coroutineScope.launch { opmlManager.import() }
    }

    private fun updateBrowserType(browserType: BrowserType) {
      coroutineScope.launch { settingsRepository.updateBrowserType(browserType) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}

private data class Settings(
  val browserType: BrowserType,
  val showUnreadPostsCount: Boolean,
  val hasFeeds: Boolean,
  val postsDeletionPeriod: Period,
  val showReaderView: Boolean,
  val appThemeMode: AppThemeMode,
)
