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
package dev.sasikanth.rss.reader.debug

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.PlatformContext
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.DebugLogSeverity
import dev.sasikanth.rss.reader.data.repository.DebugSettingsRepository
import dev.sasikanth.rss.reader.data.repository.DebugSubscriptionOverride
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.favicons.FavIconImageLoader
import dev.sasikanth.rss.reader.notifications.Notifier
import dev.sasikanth.rss.reader.ui.SeedColorExtractor
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class DebugSettingsViewModel(
  val appInfo: AppInfo,
  private val debugSettingsRepository: DebugSettingsRepository,
  private val settingsRepository: SettingsRepository,
  private val rssRepository: RssRepository,
  private val postContentRepository: PostContentRepository,
  private val userRepository: UserRepository,
  private val billingHandler: BillingHandler,
  private val syncCoordinator: SyncCoordinator,
  private val refreshPolicy: RefreshPolicy,
  private val widgetUpdater: WidgetUpdater,
  private val notifier: Notifier,
  private val imageLoader: ImageLoader,
  private val platformContext: PlatformContext,
  private val seedColorExtractor: SeedColorExtractor,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(DebugSettingsState.default(appInfo))
  val state: StateFlow<DebugSettingsState>
    get() = _state

  private val _effects = MutableSharedFlow<DebugSettingsEffect>()
  val effects = _effects.asSharedFlow()

  init {
    combine(
        debugSettingsRepository.showFpsOverlay,
        debugSettingsRepository.showWindowSizeOverlay,
        debugSettingsRepository.subscriptionOverride,
        debugSettingsRepository.logSeverity,
      ) { showFpsOverlay, showWindowSizeOverlay, subscriptionOverride, logSeverity ->
        DebugPreferenceValues(
          showFpsOverlay = showFpsOverlay,
          showWindowSizeOverlay = showWindowSizeOverlay,
          subscriptionOverride = subscriptionOverride,
          logSeverity = logSeverity,
        )
      }
      .onEach { prefs ->
        _state.update {
          it.copy(
            showFpsOverlay = prefs.showFpsOverlay,
            showWindowSizeOverlay = prefs.showWindowSizeOverlay,
            subscriptionOverride = prefs.subscriptionOverride,
            logSeverity = prefs.logSeverity,
          )
        }
      }
      .launchIn(viewModelScope)

    syncCoordinator.syncState
      .onEach { syncState ->
        _state.update { it.copy(syncState = syncState) }
        if (syncState is SyncState.Complete) {
          refreshDiagnostics()
        }
      }
      .launchIn(viewModelScope)

    refreshPolicy.lastSyncedAtFlow
      .onEach { lastSyncedAt -> _state.update { it.copy(lastSyncedAt = lastSyncedAt) } }
      .launchIn(viewModelScope)

    userRepository
      .user()
      .onEach { user -> _state.update { it.copy(signedInService = user?.serviceType) } }
      .launchIn(viewModelScope)

    combine(
        settingsRepository.installDate,
        settingsRepository.lastReviewPromptDate,
        settingsRepository.userSessionCount,
      ) { installDate, lastReviewPromptDate, userSessionCount ->
        Triple(installDate, lastReviewPromptDate, userSessionCount)
      }
      .onEach { (installDate, lastReviewPromptDate, userSessionCount) ->
        _state.update {
          it.copy(
            installDate = installDate,
            lastReviewPromptDate = lastReviewPromptDate,
            userSessionCount = userSessionCount,
          )
        }
      }
      .launchIn(viewModelScope)

    refreshDiagnostics()
  }

  fun dispatch(event: DebugSettingsEvent) {
    when (event) {
      is DebugSettingsEvent.ToggleFpsOverlay -> toggleFpsOverlay(event.value)
      is DebugSettingsEvent.ToggleWindowSizeOverlay -> toggleWindowSizeOverlay(event.value)
      is DebugSettingsEvent.UpdateSubscriptionOverride -> updateSubscriptionOverride(event.override)
      is DebugSettingsEvent.UpdateLogSeverity -> updateLogSeverity(event.severity)
      DebugSettingsEvent.ClearImageAndFavIconCaches -> clearImageAndFavIconCaches()
      DebugSettingsEvent.ClearSeedColorCache -> clearSeedColorCache()
      DebugSettingsEvent.ClearCachedArticleContent -> clearCachedArticleContent()
      DebugSettingsEvent.ClearDiscoveryCache -> clearDiscoveryCache()
      DebugSettingsEvent.ForceFullSync -> forceFullSync()
      DebugSettingsEvent.ResetRefreshPolicy -> resetRefreshPolicy()
      DebugSettingsEvent.RefreshWidgets -> refreshWidgets()
      DebugSettingsEvent.ResetOnboarding -> resetOnboarding()
      DebugSettingsEvent.ResetChangelogState -> resetChangelogState()
      DebugSettingsEvent.SendTestNotification -> sendTestNotification()
      DebugSettingsEvent.ResetInAppRatingState -> resetInAppRatingState()
      DebugSettingsEvent.RefreshDiagnostics -> refreshDiagnostics()
    }
  }

  fun buildDiagnosticsSummary(): String {
    val current = _state.value
    val db = current.databaseStats
    val subscriptionLabel =
      when (val result = current.effectiveSubscriptionResult) {
        SubscriptionResult.Subscribed -> "Subscribed"
        SubscriptionResult.NotSubscribed -> "NotSubscribed"
        is SubscriptionResult.Error -> "Error (${result.e.message ?: "unknown"})"
        null -> "Unknown"
      }

    return buildString {
      appendLine("Twine Debug Diagnostics")
      appendLine("Version: ${current.appInfo.versionName} (${current.appInfo.versionCode})")
      appendLine("Platform: ${current.appInfo.platform}")
      appendLine("Flavor: ${if (current.appInfo.isFoss) "FOSS" else "Full"}")
      appendLine("Debug build: ${current.appInfo.isDebugBuild}")
      appendLine("Subscription override: ${current.subscriptionOverride}")
      appendLine("Effective subscription: $subscriptionLabel")
      appendLine("Log severity: ${current.logSeverity}")
      appendLine("Sync account: ${current.signedInService?.name ?: "Local"}")
      appendLine(
        "DB: feeds=${db?.feedsCount ?: "-"}, groups=${db?.groupsCount ?: "-"}, posts=${db?.postsCount ?: "-"}, unread=${db?.unreadPostsCount ?: "-"}, bookmarks=${db?.bookmarksCount ?: "-"}, cachedArticles=${db?.cachedArticlesCount ?: "-"}"
      )
      appendLine("Last synced: ${formatTimestamp(current.lastSyncedAt)}")
      appendLine("Last refreshed: ${formatTimestamp(current.lastRefreshedAt)}")
      appendLine("User sessions: ${current.userSessionCount}")
      appendLine("Install date: ${formatTimestamp(current.installDate)}")
      appendLine("Last review prompt: ${formatTimestamp(current.lastReviewPromptDate)}")
      append("Cache path: ${current.appInfo.cachePath()}")
    }
  }

  private fun toggleFpsOverlay(value: Boolean) {
    viewModelScope.launch { debugSettingsRepository.toggleShowFpsOverlay(value) }
  }

  private fun toggleWindowSizeOverlay(value: Boolean) {
    viewModelScope.launch { debugSettingsRepository.toggleShowWindowSizeOverlay(value) }
  }

  private fun updateSubscriptionOverride(override: DebugSubscriptionOverride) {
    viewModelScope.launch {
      debugSettingsRepository.updateSubscriptionOverride(override)
      loadEffectiveSubscription()
    }
  }

  private fun updateLogSeverity(severity: DebugLogSeverity) {
    viewModelScope.launch {
      debugSettingsRepository.updateLogSeverity(severity)
      Logger.setMinSeverity(severity.toKermitSeverity())
    }
  }

  private fun clearImageAndFavIconCaches() {
    viewModelScope.launch {
      imageLoader.memoryCache?.clear()
      withContext(dispatchersProvider.io) {
        imageLoader.diskCache?.clear()
        FavIconImageLoader.clearCache(platformContext)
      }
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Image & favicon caches cleared"))
    }
  }

  private fun clearSeedColorCache() {
    viewModelScope.launch {
      seedColorExtractor.clearCache()
      rssRepository.clearSeedColors()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Seed color cache cleared"))
    }
  }

  private fun clearCachedArticleContent() {
    viewModelScope.launch {
      postContentRepository.clearAllArticleContent()
      refreshDiagnosticsInternal()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Cached article content cleared"))
    }
  }

  private fun clearDiscoveryCache() {
    viewModelScope.launch {
      settingsRepository.clearDiscoveryFeedsCache()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Discovery feeds cache cleared"))
    }
  }

  private fun forceFullSync() {
    viewModelScope.launch {
      rssRepository.resetFeedsRefreshMetadata()
      syncCoordinator.triggerPull()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Full sync triggered"))
    }
  }

  private fun resetRefreshPolicy() {
    viewModelScope.launch {
      refreshPolicy.clear()
      refreshDiagnosticsInternal()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Refresh policy reset"))
    }
  }

  private fun refreshWidgets() {
    viewModelScope.launch {
      widgetUpdater.updateUnreadWidget()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Widget update triggered"))
    }
  }

  private fun resetOnboarding() {
    viewModelScope.launch {
      settingsRepository.resetOnboarding()
      _effects.emit(DebugSettingsEffect.NavigateToOnboarding)
    }
  }

  private fun resetChangelogState() {
    viewModelScope.launch {
      settingsRepository.clearLastSeenChangelogVersion()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("Changelog state reset for next launch"))
    }
  }

  private fun sendTestNotification() {
    viewModelScope.launch {
      val granted = notifier.requestPermission()
      if (granted) {
        notifier.show(
          title = "Twine Debug Notification",
          content = "Test notification from Developer Settings",
          notificationId = TEST_NOTIFICATION_ID,
        )
        _effects.emit(DebugSettingsEffect.ShowSnackbar("Test notification sent"))
      } else {
        _effects.emit(DebugSettingsEffect.ShowSnackbar("Notification permission denied"))
      }
    }
  }

  private fun resetInAppRatingState() {
    viewModelScope.launch {
      settingsRepository.resetInAppRatingState()
      _effects.emit(DebugSettingsEffect.ShowSnackbar("In-app rating eligibility reset"))
    }
  }

  private fun refreshDiagnostics() {
    viewModelScope.launch {
      refreshDiagnosticsInternal()
      loadEffectiveSubscription()
    }
  }

  private suspend fun refreshDiagnosticsInternal() {
    val stats = rssRepository.databaseStats()
    val lastRefreshedAt = refreshPolicy.fetchLastRefreshedAt()
    _state.update { it.copy(databaseStats = stats, lastRefreshedAt = lastRefreshedAt) }
  }

  private suspend fun loadEffectiveSubscription() {
    val result = billingHandler.customerResult()
    _state.update { it.copy(effectiveSubscriptionResult = result) }
  }

  private fun formatTimestamp(instant: Instant?): String {
    return instant?.toLocalDateTime(TimeZone.currentSystemDefault())?.toString() ?: "Never"
  }

  private companion object {
    const val TEST_NOTIFICATION_ID = 9999
  }
}

private data class DebugPreferenceValues(
  val showFpsOverlay: Boolean,
  val showWindowSizeOverlay: Boolean,
  val subscriptionOverride: DebugSubscriptionOverride,
  val logSeverity: DebugLogSeverity,
)
