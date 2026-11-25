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

import dev.sasikanth.rss.reader.data.repository.DebugLogSeverity
import dev.sasikanth.rss.reader.data.repository.DebugSubscriptionOverride

sealed interface DebugSettingsEvent {
  data class ToggleFpsOverlay(val value: Boolean) : DebugSettingsEvent

  data class ToggleWindowSizeOverlay(val value: Boolean) : DebugSettingsEvent

  data class UpdateSubscriptionOverride(val override: DebugSubscriptionOverride) :
    DebugSettingsEvent

  data class UpdateLogSeverity(val severity: DebugLogSeverity) : DebugSettingsEvent

  data object ClearImageAndFavIconCaches : DebugSettingsEvent

  data object ClearSeedColorCache : DebugSettingsEvent

  data object ClearCachedArticleContent : DebugSettingsEvent

  data object ClearDiscoveryCache : DebugSettingsEvent

  data object ForceFullSync : DebugSettingsEvent

  data object ResetRefreshPolicy : DebugSettingsEvent

  data object RefreshWidgets : DebugSettingsEvent

  data object ResetOnboarding : DebugSettingsEvent

  data object ResetChangelogState : DebugSettingsEvent

  data object SendTestNotification : DebugSettingsEvent

  data object ResetInAppRatingState : DebugSettingsEvent

  data object RefreshDiagnostics : DebugSettingsEvent
}

sealed interface DebugSettingsEffect {
  data class ShowSnackbar(val message: String) : DebugSettingsEffect

  data object NavigateToOnboarding : DebugSettingsEffect
}
