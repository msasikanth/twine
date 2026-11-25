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

import androidx.compose.runtime.Immutable
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.DatabaseStats
import dev.sasikanth.rss.reader.data.repository.DebugLogSeverity
import dev.sasikanth.rss.reader.data.repository.DebugSubscriptionOverride
import dev.sasikanth.rss.reader.data.sync.SyncState
import kotlin.time.Instant

@Immutable
data class DebugSettingsState(
  val appInfo: AppInfo,
  val showFpsOverlay: Boolean = false,
  val showWindowSizeOverlay: Boolean = false,
  val subscriptionOverride: DebugSubscriptionOverride = DebugSubscriptionOverride.Default,
  val effectiveSubscriptionResult: SubscriptionResult? = null,
  val logSeverity: DebugLogSeverity = DebugLogSeverity.Info,
  val syncState: SyncState = SyncState.Idle,
  val databaseStats: DatabaseStats? = null,
  val lastSyncedAt: Instant? = null,
  val lastRefreshedAt: Instant? = null,
  val signedInService: ServiceType? = null,
  val installDate: Instant? = null,
  val lastReviewPromptDate: Instant? = null,
  val userSessionCount: Int = 0,
) {
  companion object {
    fun default(appInfo: AppInfo) = DebugSettingsState(appInfo = appInfo)
  }
}
