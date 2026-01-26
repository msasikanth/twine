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
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.app.AppIconManager
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
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.OAuthManager
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.notifications.Notifier
import dev.sasikanth.rss.reader.utils.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class SettingsViewModel(
  rssRepository: RssRepository,
  val appInfo: AppInfo,
  private val settingsRepository: SettingsRepository,
  private val userRepository: UserRepository,
  private val opmlManager: OpmlManager,
  private val billingHandler: BillingHandler,
  private val notifier: Notifier,
  private val syncCoordinator: SyncCoordinator,
  private val oAuthManager: OAuthManager,
  private val appIconManager: AppIconManager,
  val availableProviders: Set<CloudServiceProvider>
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
        settingsRepository.dynamicColorEnabled,
        settingsRepository.enableAutoSync,
        settingsRepository.showFeedFavIcon,
        settingsRepository.markAsReadOn,
        settingsRepository.homeViewMode,
        settingsRepository.blockImages,
        settingsRepository.enableNotifications,
        settingsRepository.downloadFullContent,
        settingsRepository.lastSyncedAt,
        userRepository.user(),
        settingsRepository.appIcon,
      ) {
        browserType,
        showUnreadPostsCount,
        postsDeletionPeriod,
        showReaderView,
        appThemeMode,
        useAmoled,
        dynamicColorEnabled,
        enableAutoSync,
        showFeedFavIcon,
        markAsReadOn,
        homeViewMode,
        blockImages,
        enableNotifications,
        downloadFullContent,
        lastSyncedAt,
        user,
        appIcon ->
        Settings(
          browserType = browserType,
          showUnreadPostsCount = showUnreadPostsCount,
          postsDeletionPeriod = postsDeletionPeriod,
          showReaderView = showReaderView,
          appThemeMode = appThemeMode,
          useAmoled = useAmoled,
          dynamicColorEnabled = dynamicColorEnabled,
          enableAutoSync = enableAutoSync,
          showFeedFavIcon = showFeedFavIcon,
          markAsReadOn = markAsReadOn,
          homeViewMode = homeViewMode,
          blockImages = blockImages,
          enableNotifications = enableNotifications,
          downloadFullContent = downloadFullContent,
          lastSyncedAt = lastSyncedAt,
          lastSyncStatus =
            when (user?.lastSyncStatus) {
              "SUCCESS" -> SettingsState.SyncProgress.Success
              "FAILURE" -> SettingsState.SyncProgress.Failure
              "SYNCING" -> SettingsState.SyncProgress.Syncing
              else -> SettingsState.SyncProgress.Idle
            },
          hasCloudServiceSignedIn = user != null,
          appIcon = appIcon,
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
            dynamicColorEnabled = settings.dynamicColorEnabled,
            enableAutoSync = settings.enableAutoSync,
            showFeedFavIcon = settings.showFeedFavIcon,
            markAsReadOn = settings.markAsReadOn,
            homeViewMode = settings.homeViewMode,
            blockImages = settings.blockImages,
            enableNotifications = settings.enableNotifications,
            downloadFullContent = settings.downloadFullContent,
            lastSyncedAt = settings.lastSyncedAt,
            hasCloudServiceSignedIn = settings.hasCloudServiceSignedIn,
            appIcon = settings.appIcon,
            syncProgress =
              if (it.syncProgress == SettingsState.SyncProgress.Syncing) {
                SettingsState.SyncProgress.Syncing
              } else {
                settings.lastSyncStatus
              }
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
      is SettingsEvent.ToggleDynamicColor -> toggleDynamicColor(event.value)
      is SettingsEvent.MarkAsReadOnChanged -> markAsReadOnChanged(event.newMarkAsReadOn)
      is SettingsEvent.LoadSubscriptionStatus -> loadSubscriptionStatus()
      is SettingsEvent.MarkOpenPaywallAsDone -> {
        _state.update { it.copy(openPaywall = false) }
      }
      is SettingsEvent.ChangeHomeViewMode -> changeHomeViewMode(event.homeViewMode)
      is SettingsEvent.ToggleBlockImages -> toggleBlockImages(event.value)
      is SettingsEvent.ToggleNotifications -> toggleNotifications(event.value)
      is SettingsEvent.ToggleDownloadFullContent -> toggleDownloadFullContent(event.value)
      is SettingsEvent.SyncClicked -> syncClicked(event.provider)
      is SettingsEvent.SignOutClicked -> signOutClicked()
      SettingsEvent.ClearAuthUrl -> _state.update { it.copy(authUrlToOpen = null) }
      is SettingsEvent.OnAppIconChanged -> onAppIconChanged(event.appIcon)
      SettingsEvent.AppIconClicked -> appIconClicked()
      SettingsEvent.CloseAppIconSelectionSheet -> {
        _state.update { it.copy(showAppIconSelectionSheet = false) }
      }
    }
  }

  private fun syncClicked(provider: CloudServiceProvider) {
    viewModelScope.launch {
      val isSignedIn = provider.isSignedIn().first()
      if (isSignedIn) {
        _state.update { it.copy(syncProgress = SettingsState.SyncProgress.Syncing) }
        val result = syncCoordinator.pull()
        _state.update {
          it.copy(
            syncProgress =
              if (result) SettingsState.SyncProgress.Success else SettingsState.SyncProgress.Failure
          )
        }
      } else {
        oAuthManager.setPendingProvider(provider.cloudService)
        val authUrl = oAuthManager.getAuthUrl(provider.cloudService)
        _state.update { it.copy(authUrlToOpen = authUrl) }
      }
    }
  }

  private fun signOutClicked() {
    viewModelScope.launch {
      availableProviders.forEach {
        if (it.isSignedInImmediate()) {
          it.signOut()
        }
      }
      _state.update { it.copy(syncProgress = SettingsState.SyncProgress.Idle) }
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
      val canSubscribe = billingHandler.canSubscribe()
      _state.update {
        it.copy(subscriptionResult = subscriptionResult, canSubscribe = canSubscribe)
      }
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

  private fun toggleDynamicColor(value: Boolean) {
    viewModelScope.launch { settingsRepository.toggleDynamicColor(value) }
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

  private fun onAppIconChanged(appIcon: AppIcon) {
    viewModelScope.launch {
      settingsRepository.updateAppIcon(appIcon)
      appIconManager.setIcon(appIcon)
    }
  }

  private fun appIconClicked() {
    viewModelScope.launch {
      if (billingHandler.isSubscribed()) {
        _state.update { it.copy(showAppIconSelectionSheet = true) }
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
  val dynamicColorEnabled: Boolean,
  val enableAutoSync: Boolean,
  val showFeedFavIcon: Boolean,
  val markAsReadOn: MarkAsReadOn,
  val homeViewMode: HomeViewMode,
  val blockImages: Boolean,
  val enableNotifications: Boolean,
  val downloadFullContent: Boolean,
  val lastSyncedAt: kotlin.time.Instant?,
  val lastSyncStatus: SettingsState.SyncProgress,
  val hasCloudServiceSignedIn: Boolean,
  val appIcon: AppIcon,
)
