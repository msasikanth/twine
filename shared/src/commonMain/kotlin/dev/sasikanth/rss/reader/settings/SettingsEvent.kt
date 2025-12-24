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

import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period

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

  data class MarkAsReadOnChanged(val newMarkAsReadOn: MarkAsReadOn) : SettingsEvent

  data object LoadSubscriptionStatus : SettingsEvent

  data object MarkOpenPaywallAsDone : SettingsEvent

  data class ChangeHomeViewMode(val homeViewMode: HomeViewMode) : SettingsEvent
}
