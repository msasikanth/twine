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
import dev.sasikanth.rss.reader.opml.OpmlManager
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

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
          settingsRepository.enableFeaturedItemBlur,
          rssRepository.numberOfFeeds()
        ) { browserType, featuredItemBlurEnabled, numberOfFeeds ->
          val hasFeeds = numberOfFeeds > 0
          Triple(browserType, featuredItemBlurEnabled, hasFeeds)
        }
        .onEach { (browserType, featuredItemBlurEnabled, hasFeeds) ->
          _state.update {
            it.copy(
              browserType = browserType,
              enableHomePageBlur = featuredItemBlurEnabled,
              hasFeeds = hasFeeds
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
        is SettingsEvent.ToggleFeaturedItemBlur -> toggleFeaturedItemBlur(event.value)
        SettingsEvent.AboutClicked -> {
          // no-op
        }
        SettingsEvent.ImportOpmlClicked -> importOpmlClicked()
        SettingsEvent.ExportOpmlClicked -> exportOpmlClicked()
        SettingsEvent.CancelOpmlImportOrExport -> cancelOpmlImportOrExport()
      }
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

    private fun toggleFeaturedItemBlur(value: Boolean) {
      coroutineScope.launch { settingsRepository.toggleFeaturedItemBlur(value) }
    }

    private fun updateBrowserType(browserType: BrowserType) {
      coroutineScope.launch { settingsRepository.updateBrowserType(browserType) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
