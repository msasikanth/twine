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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.InverseButton
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.sync.APIServiceProvider
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.resources.icons.Dropbox
import dev.sasikanth.rss.reader.resources.icons.Freshrss
import dev.sasikanth.rss.reader.resources.icons.Miniflux
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.Sync
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsState
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingSyncLabel
import twine.shared.generated.resources.settingsSyncDropbox
import twine.shared.generated.resources.settingsSyncFreshRSS
import twine.shared.generated.resources.settingsSyncMiniflux
import twine.shared.generated.resources.settingsSyncSignIn
import twine.shared.generated.resources.settingsSyncSignOut
import twine.shared.generated.resources.settingsSyncStatusFailure
import twine.shared.generated.resources.settingsSyncStatusIdle
import twine.shared.generated.resources.settingsSyncStatusSuccess
import twine.shared.generated.resources.settingsSyncStatusSyncing

@Composable
internal fun CloudSyncSettingItem(
  syncProgress: SettingsState.SyncProgress,
  lastSyncedAt: Instant?,
  availableProviders: Set<CloudServiceProvider>,
  isSubscribed: Boolean,
  onSyncClicked: (CloudServiceProvider) -> Unit,
  onAPIServiceClicked: (APIServiceProvider) -> Unit,
  onSignOutClicked: () -> Unit,
  onSyncErrorClicked: (Exception) -> Unit,
) {
  availableProviders.forEach { provider ->
    val label =
      when (val service = provider.cloudService) {
        ServiceType.DROPBOX -> stringResource(Res.string.settingsSyncDropbox)
        ServiceType.FRESH_RSS -> stringResource(Res.string.settingsSyncFreshRSS)
        ServiceType.MINIFLUX -> stringResource(Res.string.settingsSyncMiniflux)
      }
    val isSignedIn by provider.isSignedIn().collectAsStateWithLifecycle(false)
    val verticalPadding by animateDpAsState(if (isSignedIn) 16.dp else 12.dp)
    val translucentStyle = LocalTranslucentStyles.current

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .then(
            if (syncProgress is SettingsState.SyncProgress.Failure && isSignedIn) {
              Modifier.clickable { onSyncErrorClicked(syncProgress.exception) }
            } else {
              Modifier
            }
          )
          .padding(vertical = verticalPadding)
          .padding(start = 16.dp, end = 24.dp)
          .clip(MaterialTheme.shapes.medium)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        val icon =
          when (provider.cloudService) {
            ServiceType.FRESH_RSS -> TwineIcons.Freshrss
            ServiceType.MINIFLUX -> TwineIcons.Miniflux
            ServiceType.DROPBOX -> TwineIcons.Dropbox
          }

        Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
          Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = AppTheme.colorScheme.onSurface,
          )
        }

        Spacer(Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = label,
              style = MaterialTheme.typography.titleMedium,
              color = AppTheme.colorScheme.onSurface,
            )

            if (provider.isPremium && !isSubscribed) {
              Spacer(Modifier.width(8.dp))

              Icon(
                modifier = Modifier.size(16.dp),
                imageVector = TwineIcons.StarShine,
                contentDescription = null,
                tint = AppTheme.colorScheme.primary,
              )
            }
          }

          Spacer(Modifier.height(4.dp))

          var statusString =
            when (syncProgress) {
              SettingsState.SyncProgress.Idle -> stringResource(Res.string.settingsSyncStatusIdle)
              SettingsState.SyncProgress.Syncing ->
                stringResource(Res.string.settingsSyncStatusSyncing)

              SettingsState.SyncProgress.Success ->
                stringResource(Res.string.settingsSyncStatusSuccess)

              is SettingsState.SyncProgress.Failure ->
                stringResource(Res.string.settingsSyncStatusFailure)
            }

          if (
            syncProgress !is SettingsState.SyncProgress.Idle &&
              syncProgress !is SettingsState.SyncProgress.Syncing &&
              lastSyncedAt != null &&
              isSignedIn
          ) {
            statusString += " ${Constants.BULLET_POINT} ${lastSyncedAt.formatRelativeTime()}"
          }

          AnimatedVisibility(
            visible = syncProgress !is SettingsState.SyncProgress.Idle && isSignedIn
          ) {
            Text(
              text = statusString,
              style = MaterialTheme.typography.bodySmall,
              color = AppTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Spacer(Modifier.width(8.dp))

        if (isSignedIn) {
          CircularIconButton(
            icon = TwineIcons.Sync,
            label = stringResource(Res.string.settingSyncLabel),
            onClick = { onSyncClicked(provider) },
          )

          Spacer(modifier = Modifier.width(12.dp))
        }

        if (isSignedIn) {
          TranslucentButton(
            text = stringResource(Res.string.settingsSyncSignOut),
            onClick = { onSignOutClicked() },
          )
        } else {
          InverseButton(
            text = stringResource(Res.string.settingsSyncSignIn),
            onClick = {
              if (provider is APIServiceProvider && !isSignedIn) {
                onAPIServiceClicked(provider)
              } else {
                onSyncClicked(provider)
              }
            },
          )
        }
      }
    }
  }
}
