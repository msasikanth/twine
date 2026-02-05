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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.sync.APIServiceProvider
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.resources.icons.Dropbox
import dev.sasikanth.rss.reader.resources.icons.Freshrss
import dev.sasikanth.rss.reader.resources.icons.Miniflux
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsState
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.relativeDurationString
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsSyncDropbox
import twine.shared.generated.resources.settingsSyncFreshRSS
import twine.shared.generated.resources.settingsSyncMiniflux
import twine.shared.generated.resources.settingsSyncSignOut
import twine.shared.generated.resources.settingsSyncStatusFailure
import twine.shared.generated.resources.settingsSyncStatusIdle
import twine.shared.generated.resources.settingsSyncStatusSuccess
import twine.shared.generated.resources.settingsSyncStatusSyncing

@Composable
internal fun CloudSyncSettingItem(
  syncProgress: SettingsState.SyncProgress,
  lastSyncedAt: Instant?,
  hasCloudServiceSignedIn: Boolean,
  availableProviders: Set<CloudServiceProvider>,
  isSubscribed: Boolean,
  onSyncClicked: (CloudServiceProvider) -> Unit,
  onAPIServiceClicked: (APIServiceProvider) -> Unit,
  onSignOutClicked: () -> Unit,
) {
  availableProviders.forEach { provider ->
    val label =
      when (val service = provider.cloudService) {
        ServiceType.DROPBOX -> stringResource(Res.string.settingsSyncDropbox)
        ServiceType.FRESH_RSS -> stringResource(Res.string.settingsSyncFreshRSS)
        ServiceType.MINIFLUX -> stringResource(Res.string.settingsSyncMiniflux)
      }
    val isSignedIn by provider.isSignedIn().collectAsStateWithLifecycle(false)
    val canInteract = !hasCloudServiceSignedIn || isSignedIn
    val verticalPadding by animateDpAsState(if (isSignedIn) 12.dp else 4.dp)

    Box(
      modifier =
        Modifier.clickable(enabled = canInteract) {
            if (provider is APIServiceProvider && !isSignedIn) {
              onAPIServiceClicked(provider)
            } else {
              onSyncClicked(provider)
            }
          }
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = verticalPadding)
          .alpha(if (canInteract) 1f else 0.38f)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        val icon =
          when (provider.cloudService) {
            ServiceType.FRESH_RSS -> TwineIcons.Freshrss
            ServiceType.MINIFLUX -> TwineIcons.Miniflux
            ServiceType.DROPBOX -> TwineIcons.Dropbox
          }

        Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = label,
              style = MaterialTheme.typography.titleMedium,
              color = AppTheme.colorScheme.textEmphasisHigh,
            )

            if (provider.isPremium && !isSubscribed) {
              Spacer(Modifier.width(8.dp))

              Icon(
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Rounded.WorkspacePremium,
                contentDescription = null,
                tint = AppTheme.colorScheme.primary,
              )
            }
          }

          var statusString =
            when (syncProgress) {
              SettingsState.SyncProgress.Idle -> stringResource(Res.string.settingsSyncStatusIdle)
              SettingsState.SyncProgress.Syncing ->
                stringResource(Res.string.settingsSyncStatusSyncing)

              SettingsState.SyncProgress.Success ->
                stringResource(Res.string.settingsSyncStatusSuccess)

              SettingsState.SyncProgress.Failure ->
                stringResource(Res.string.settingsSyncStatusFailure)
            }

          if (
            syncProgress != SettingsState.SyncProgress.Idle &&
              syncProgress != SettingsState.SyncProgress.Syncing &&
              lastSyncedAt != null &&
              isSignedIn
          ) {
            statusString += " \u2022 ${lastSyncedAt.relativeDurationString()}"
          }

          AnimatedVisibility(
            visible = syncProgress != SettingsState.SyncProgress.Idle && isSignedIn
          ) {
            Text(
              text = statusString,
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.textEmphasisMed,
            )
          }
        }

        if (isSignedIn) {
          val actionLabel = stringResource(Res.string.settingsSyncSignOut)

          TextButton(enabled = canInteract, onClick = { onSignOutClicked() }) {
            Text(
              text = actionLabel,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              color = AppTheme.colorScheme.primary,
            )
          }
        } else {
          Box(Modifier.minimumInteractiveComponentSize())
        }
      }
    }
  }
}
