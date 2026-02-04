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
package dev.sasikanth.rss.reader.settings

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.data.opml.OpmlFeed
import dev.sasikanth.rss.reader.data.opml.OpmlResult
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period
import kotlin.time.Instant

@Immutable
data class SettingsState(
  val browserType: BrowserType,
  val showUnreadPostsCount: Boolean,
  val hasFeeds: Boolean,
  val appInfo: AppInfo,
  val opmlResult: OpmlResult?,
  val postsDeletionPeriod: Period?,
  val showReaderView: Boolean,
  val appThemeMode: AppThemeMode,
  val useAmoled: Boolean,
  val dynamicColorEnabled: Boolean,
  val enableAutoSync: Boolean,
  val showFeedFavIcon: Boolean,
  val markAsReadOn: MarkAsReadOn,
  val subscriptionResult: SubscriptionResult?,
  val openPaywall: Boolean,
  val homeViewMode: HomeViewMode,
  val blockImages: Boolean,
  val enableNotifications: Boolean,
  val downloadFullContent: Boolean,
  val syncProgress: SyncProgress,
  val lastSyncedAt: Instant?,
  val hasCloudServiceSignedIn: Boolean,
  val authUrlToOpen: String?,
  val appIcon: AppIcon,
  val showAppIconSelectionSheet: Boolean,
  val canSubscribe: Boolean,
  val opmlFeedsToSelect: List<OpmlFeed>?,
  val showFreeFeedLimitWarning: Boolean,
) {

  val isSubscribed: Boolean
    get() = subscriptionResult == SubscriptionResult.Subscribed

  enum class SyncProgress {
    Idle,
    Syncing,
    Success,
    Failure,
  }

  companion object {

    fun default(appInfo: AppInfo) =
      SettingsState(
        browserType = BrowserType.Default,
        showUnreadPostsCount = false,
        hasFeeds = false,
        appInfo = appInfo,
        opmlResult = null,
        postsDeletionPeriod = null,
        showReaderView = false,
        appThemeMode = AppThemeMode.Auto,
        useAmoled = false,
        dynamicColorEnabled = true,
        enableAutoSync = true,
        showFeedFavIcon = true,
        markAsReadOn = MarkAsReadOn.Open,
        subscriptionResult = null,
        openPaywall = false,
        homeViewMode = HomeViewMode.Default,
        blockImages = false,
        enableNotifications = false,
        downloadFullContent = false,
        syncProgress = SyncProgress.Idle,
        lastSyncedAt = null,
        hasCloudServiceSignedIn = false,
        authUrlToOpen = null,
        appIcon = AppIcon.DarkJade,
        showAppIconSelectionSheet = false,
        canSubscribe = true,
        opmlFeedsToSelect = null,
        showFreeFeedLimitWarning = false,
      )
  }
}
