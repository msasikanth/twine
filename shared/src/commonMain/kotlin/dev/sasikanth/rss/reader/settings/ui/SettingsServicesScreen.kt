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
package dev.sasikanth.rss.reader.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.CloudSyncSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OPMLSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OpmlFeedSelectionSheet
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.settingsServicesAndSync

@Composable
internal fun SettingsServicesScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openPaywall: () -> Unit,
  openFreshRssLogin: () -> Unit,
  openMinifluxLogin: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

  LaunchedEffect(state.authUrlToOpen) {
    state.authUrlToOpen?.let { url ->
      linkHandler.openLink(url, useInAppBrowser = true)
      viewModel.dispatch(SettingsEvent.ClearAuthUrl)
    }
  }

  LaunchedEffect(state.openPaywall) {
    if (state.openPaywall) {
      openPaywall()
      viewModel.dispatch(SettingsEvent.MarkOpenPaywallAsDone)
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.dispatch(SettingsEvent.LoadSubscriptionStatus)
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {
            Text(
              stringResource(Res.string.settingsServicesAndSync),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleMedium,
            )
          },
          navigationIcon = {
            CircularIconButton(
              modifier = Modifier.padding(start = 12.dp),
              icon = TwineIcons.ArrowBack,
              label = stringResource(Res.string.buttonGoBack),
              onClick = goBack,
            )
          },
          contentPadding = PaddingValues(vertical = 8.dp),
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surface,
              navigationIconContentColor = AppTheme.colorScheme.onSurface,
              titleContentColor = AppTheme.colorScheme.onSurface,
              actionIconContentColor = AppTheme.colorScheme.onSurface,
            ),
        )

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.outlineVariant,
        )
      }
    },
    content = { padding ->
      if (state.opmlFeedsToSelect != null) {
        OpmlFeedSelectionSheet(
          feeds = state.opmlFeedsToSelect!!,
          onFeedsSelected = { viewModel.dispatch(SettingsEvent.OnOpmlFeedsSelected(it)) },
          onDismiss = { viewModel.dispatch(SettingsEvent.ClearOpmlFeedsToSelect) },
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + settingsItemHorizontalPadding,
            top = padding.calculateTopPadding() + 8.dp,
            end = padding.calculateEndPadding(layoutDirection) + settingsItemHorizontalPadding,
            bottom = padding.calculateBottomPadding() + 80.dp,
          ),
      ) {
        item {
          OPMLSettingItem(
            opmlResult = state.opmlResult,
            hasFeeds = state.hasFeeds,
            onImportClicked = { viewModel.dispatch(SettingsEvent.ImportOpmlClicked) },
            onExportClicked = { viewModel.dispatch(SettingsEvent.ExportOpmlClicked) },
            onCancelClicked = { viewModel.dispatch(SettingsEvent.CancelOpmlImportOrExport) },
          )
        }

        item { SettingsDivider(24.dp) }

        item {
          CloudSyncSettingItem(
            syncProgress = state.syncProgress,
            lastSyncedAt = state.lastSyncedAt,
            hasCloudServiceSignedIn = state.hasCloudServiceSignedIn,
            availableProviders = viewModel.availableProviders,
            isSubscribed = state.isSubscribed,
            onSyncClicked = {
              if (it.isPremium && !state.isSubscribed) {
                openPaywall()
              } else {
                viewModel.dispatch(SettingsEvent.SyncClicked(it))
              }
            },
            onAPIServiceClicked = {
              if (it.isPremium && !state.isSubscribed) {
                openPaywall()
              } else {
                when (it.cloudService) {
                  ServiceType.FRESH_RSS -> openFreshRssLogin()
                  ServiceType.MINIFLUX -> openMinifluxLogin()
                  else -> {
                    throw IllegalStateException("Unknown cloud service type: ${it.cloudService}")
                  }
                }
              }
            },
            onSignOutClicked = { viewModel.dispatch(SettingsEvent.SignOutClicked) },
          )
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}
