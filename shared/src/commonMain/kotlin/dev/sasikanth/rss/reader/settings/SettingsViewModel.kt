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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.data.opml.OpmlManager
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.notifications.Notifier
import dev.sasikanth.rss.reader.utils.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class SettingsViewModel(
  rssRepository: RssRepository,
  appInfo: AppInfo,
  private val settingsRepository: SettingsRepository,
  private val opmlManager: OpmlManager,
  private val billingHandler: BillingHandler,
  private val notifier: Notifier,
) : ViewModel() {

  private val _state = MutableStateFlow(SettingsState.default(appInfo))
  val state: StateFlow<SettingsState>
    get() = _state

  init {
    combine(
        settingsRepository.browserType,
        settingsRepository.showUnreadPostsCount,
        settingsRepository.postsDeletionPeriod,
        settingsRepository.showReaderView,
        settingsRepository.appThemeMode,
        settingsRepository.useAmoled,
        settingsRepository.enableAutoSync,
        settingsRepository.showFeedFavIcon,
        settingsRepository.markAsReadOn,
        settingsRepository.homeViewMode,
        settingsRepository.blockImages,
        settingsRepository.enableNotifications,
        settingsRepository.downloadFullContent,
      ) {
        browserType,
        showUnreadPostsCount,
        postsDeletionPeriod,
        showReaderView,
        appThemeMode,
        useAmoled,
        enableAutoSync,
        showFeedFavIcon,
        markAsReadOn,
        homeViewMode,
        blockImages,
        enableNotifications,
        downloadFullContent ->
        Settings(
          browserType = browserType,
          showUnreadPostsCount = showUnreadPostsCount,
          postsDeletionPeriod = postsDeletionPeriod,
          showReaderView = showReaderView,
          appThemeMode = appThemeMode,
          useAmoled = useAmoled,
          enableAutoSync = enableAutoSync,
          showFeedFavIcon = showFeedFavIcon,
          markAsReadOn = markAsReadOn,
          homeViewMode = homeViewMode,
          blockImages = blockImages,
          enableNotifications = enableNotifications,
          downloadFullContent = downloadFullContent,
        )
      }
      .onEach { settings ->
        _state.update {
          it.copy(
            browserType = settings.browserType,
            showUnreadPostsCount = settings.showUnreadPostsCount,
            postsDeletionPeriod = settings.postsDeletionPeriod,
            showReaderView = settings.showReaderView,
            appThemeMode = settings.appThemeMode,
            useAmoled = settings.useAmoled,
            enableAutoSync = settings.enableAutoSync,
            showFeedFavIcon = settings.showFeedFavIcon,
            markAsReadOn = settings.markAsReadOn,
            homeViewMode = settings.homeViewMode,
            blockImages = settings.blockImages,
            enableNotifications = settings.enableNotifications,
            downloadFullContent = settings.downloadFullContent,
          )
        }
      }
      .launchIn(viewModelScope)

    rssRepository
      .hasFeeds()
      .onEach { hasFeeds -> _state.update { it.copy(hasFeeds = hasFeeds) } }
      .launchIn(viewModelScope)

    opmlManager.result
      .onEach { result -> _state.update { it.copy(opmlResult = result) } }
      .launchIn(viewModelScope)
  }

  fun dispatch(event: SettingsEvent) {
    when (event) {
      is SettingsEvent.UpdateBrowserType -> updateBrowserType(event.browserType)
      is SettingsEvent.ToggleShowUnreadPostsCount -> toggleShowUnreadPostsCount(event.value)
      is SettingsEvent.ToggleShowReaderView -> toggleShowReaderView(event.value)
      is SettingsEvent.ToggleAutoSync -> toggleAutoSync(event.value)
      is SettingsEvent.ToggleShowFeedFavIcon -> toggleShowFeedFavIcon(event.value)
      SettingsEvent.ImportOpmlClicked -> importOpmlClicked()
      SettingsEvent.ExportOpmlClicked -> exportOpmlClicked()
      SettingsEvent.CancelOpmlImportOrExport -> cancelOpmlImportOrExport()
      is SettingsEvent.PostsDeletionPeriodChanged -> postsDeletionPeriodChanged(event.newPeriod)
      is SettingsEvent.OnAppThemeModeChanged -> onAppThemeModeChanged(event.appThemeMode)
      is SettingsEvent.ToggleAmoled -> toggleAmoled(event.value)
      is SettingsEvent.MarkAsReadOnChanged -> markAsReadOnChanged(event.newMarkAsReadOn)
      is SettingsEvent.LoadSubscriptionStatus -> loadSubscriptionStatus()
      is SettingsEvent.MarkOpenPaywallAsDone -> {
        _state.update { it.copy(openPaywall = false) }
      }
      is SettingsEvent.ChangeHomeViewMode -> changeHomeViewMode(event.homeViewMode)
      is SettingsEvent.ToggleBlockImages -> toggleBlockImages(event.value)
      is SettingsEvent.ToggleNotifications -> toggleNotifications(event.value)
      is SettingsEvent.ToggleDownloadFullContent -> toggleDownloadFullContent(event.value)
    }
  }

  private fun toggleDownloadFullContent(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleDownloadFullContent(value) }
  }

  private fun toggleNotifications(value: Boolean) {
    viewModelScope.launch {
      if (value) {
        val granted = notifier.requestPermission()
        settingsRepository.toggleNotifications(granted)
      } else {
        settingsRepository.toggleNotifications(false)
      }
    }
  }

  private fun toggleBlockImages(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleBlockImages(value) }
  }

  private fun changeHomeViewMode(homeViewMode: HomeViewMode) {
    viewModelScope.launch { settingsRepository.updateHomeViewMode(homeViewMode) }
  }

  private fun loadSubscriptionStatus() {
    viewModelScope.launch {
      val subscriptionResult = billingHandler.customerResult()
      _state.update { it.copy(subscriptionResult = subscriptionResult) }
    }
  }

  private fun markAsReadOnChanged(markAsReadOn: MarkAsReadOn) {
    viewModelScope.launch { settingsRepository.updateMarkAsReadOn(markAsReadOn) }
  }

  private fun toggleShowFeedFavIcon(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleShowFeedFavIcon(value) }
  }

  private fun toggleAutoSync(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleAutoSync(value) }
  }

  private fun onAppThemeModeChanged(appThemeMode: AppThemeMode) {
    viewModelScope.launch { settingsRepository.updateAppTheme(appThemeMode) }
  }

  private fun toggleAmoled(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleAmoled(value) }
  }

  private fun toggleShowReaderView(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleShowReaderView(value) }
  }

  private fun postsDeletionPeriodChanged(newPeriod: Period) {
    viewModelScope.launch { settingsRepository.updatePostsDeletionPeriod(newPeriod) }
  }

  private fun toggleShowUnreadPostsCount(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleShowUnreadPostsCount(value) }
  }

  private fun cancelOpmlImportOrExport() {
    opmlManager.cancel()
  }

  private fun exportOpmlClicked() {
    viewModelScope.launch { opmlManager.export() }
  }

  private fun importOpmlClicked() {
    viewModelScope.launch {
      if (billingHandler.isSubscribed()) {
        opmlManager.import()
      } else {
        _state.update { it.copy(openPaywall = true) }
      }
    }
  }

  private fun updateBrowserType(browserType: BrowserType) {
    viewModelScope.launch { settingsRepository.updateBrowserType(browserType) }
  }
}

private data class Settings(
  val browserType: BrowserType,
  val showUnreadPostsCount: Boolean,
  val postsDeletionPeriod: Period,
  val showReaderView: Boolean,
  val appThemeMode: AppThemeMode,
  val useAmoled: Boolean,
  val enableAutoSync: Boolean,
  val showFeedFavIcon: Boolean,
  val markAsReadOn: MarkAsReadOn,
  val homeViewMode: HomeViewMode,
  val blockImages: Boolean,
  val enableNotifications: Boolean,
  val downloadFullContent: Boolean,
)
