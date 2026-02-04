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

import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.data.opml.OpmlFeed
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider

sealed interface SettingsEvent {

  data class UpdateBrowserType(val browserType: BrowserType) : SettingsEvent

  data class ToggleShowUnreadPostsCount(val value: Boolean) : SettingsEvent

  data class ToggleShowReaderView(val value: Boolean) : SettingsEvent

  data class ToggleAutoSync(val value: Boolean) : SettingsEvent

  data class ToggleShowFeedFavIcon(val value: Boolean) : SettingsEvent

  data object ImportOpmlClicked : SettingsEvent

  data object ExportOpmlClicked : SettingsEvent

  data object CancelOpmlImportOrExport : SettingsEvent

  data class PostsDeletionPeriodChanged(val newPeriod: Period) : SettingsEvent

  data class OnAppThemeModeChanged(val appThemeMode: AppThemeMode) : SettingsEvent

  data class ToggleAmoled(val value: Boolean) : SettingsEvent

  data class ToggleDynamicColor(val value: Boolean) : SettingsEvent

  data class MarkAsReadOnChanged(val newMarkAsReadOn: MarkAsReadOn) : SettingsEvent

  data object LoadSubscriptionStatus : SettingsEvent

  data object MarkOpenPaywallAsDone : SettingsEvent

  data class ChangeHomeViewMode(val homeViewMode: HomeViewMode) : SettingsEvent

  data class ToggleBlockImages(val value: Boolean) : SettingsEvent

  data class ToggleNotifications(val value: Boolean) : SettingsEvent

  data class ToggleDownloadFullContent(val value: Boolean) : SettingsEvent

  data class SyncClicked(val provider: CloudServiceProvider) : SettingsEvent

  data object TriggerSync : SettingsEvent

  data object SignOutClicked : SettingsEvent

  data object ClearAuthUrl : SettingsEvent

  data object AppIconClicked : SettingsEvent

  data object CloseAppIconSelectionSheet : SettingsEvent

  data class OnAppIconChanged(val appIcon: AppIcon) : SettingsEvent

  data object DeleteAppData : SettingsEvent

  data class OnOpmlFeedsSelected(val feeds: List<OpmlFeed>) : SettingsEvent

  data object ClearOpmlFeedsToSelect : SettingsEvent

  data object MarkFreeFeedLimitWarningAsDone : SettingsEvent
}
