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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.components.AlertDialog
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.sync.APIServiceProvider
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsState
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.CloudSyncSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OPMLSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OpmlFeedSelectionSheet
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.enableAutoSyncDesc
import twine.shared.generated.resources.enableAutoSyncTitle
import twine.shared.generated.resources.freshRssClearDataPositive
import twine.shared.generated.resources.freshRssErrorUnknown
import twine.shared.generated.resources.settingsBlockImagesSubtitle
import twine.shared.generated.resources.settingsBlockImagesTitle
import twine.shared.generated.resources.settingsDownloadFullContentSubtitle
import twine.shared.generated.resources.settingsDownloadFullContentTitle
import twine.shared.generated.resources.settingsEnableNotificationsSubtitle
import twine.shared.generated.resources.settingsEnableNotificationsTitle
import twine.shared.generated.resources.settingsGroupByFeedNotificationsSubtitle
import twine.shared.generated.resources.settingsGroupByFeedNotificationsTitle
import twine.shared.generated.resources.settingsHeaderData
import twine.shared.generated.resources.settingsHeaderSync
import twine.shared.generated.resources.settingsServicesAndSync
import twine.shared.generated.resources.settingsSyncDropbox
import twine.shared.generated.resources.settingsSyncFreshRSS
import twine.shared.generated.resources.settingsSyncMiniflux
import twine.shared.generated.resources.settingsSyncStatusFailure
import twine.shared.generated.resources.switchServiceDescription
import twine.shared.generated.resources.switchServiceTitle

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

  SettingsServicesContent(
    state = state,
    dispatch = viewModel::dispatch,
    availableProviders = viewModel.availableProviders,
    goBack = goBack,
    openPaywall = openPaywall,
    openFreshRssLogin = openFreshRssLogin,
    openMinifluxLogin = openMinifluxLogin,
    modifier = modifier,
  )
}

@Composable
private fun SettingsServicesContent(
  state: SettingsState,
  dispatch: (SettingsEvent) -> Unit,
  availableProviders: Set<CloudServiceProvider>,
  goBack: () -> Unit,
  openPaywall: () -> Unit,
  openFreshRssLogin: () -> Unit,
  openMinifluxLogin: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current

  var showSwitchServiceDialog by remember { mutableStateOf<CloudServiceProvider?>(null) }
  var showSyncErrorDialog by remember { mutableStateOf<Exception?>(null) }

  if (showSyncErrorDialog != null) {
    AlertDialog(
      title = stringResource(Res.string.settingsSyncStatusFailure),
      text = showSyncErrorDialog!!.message ?: stringResource(Res.string.freshRssErrorUnknown),
      confirmText = stringResource(Res.string.buttonCancel),
      onConfirm = { showSyncErrorDialog = null },
      onDismiss = { showSyncErrorDialog = null },
    )
  }

  if (showSwitchServiceDialog != null) {
    val toProvider = showSwitchServiceDialog!!
    val signedInService = state.signedInService

    if (signedInService != null) {
      AlertDialog(
        title = stringResource(Res.string.switchServiceTitle, serviceName(toProvider.cloudService)),
        text = stringResource(Res.string.switchServiceDescription, serviceName(signedInService)),
        confirmText = stringResource(Res.string.freshRssClearDataPositive),
        dismissText = stringResource(Res.string.buttonCancel),
        onConfirm = {
          showSwitchServiceDialog = null
          dispatch(SettingsEvent.SignOutClicked)

          if (toProvider is APIServiceProvider) {
            when (toProvider.cloudService) {
              ServiceType.FRESH_RSS -> openFreshRssLogin()
              ServiceType.MINIFLUX -> openMinifluxLogin()
              else -> {
                // Unknown service type
              }
            }
          } else {
            dispatch(SettingsEvent.SyncClicked(toProvider))
          }
        },
        onDismiss = { showSwitchServiceDialog = null },
      )
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      SimpleTopAppBar(
        title = stringResource(Res.string.settingsServicesAndSync),
        onBackClick = goBack,
      )
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
    content = { padding ->
      if (state.opmlFeedsToSelect != null) {
        OpmlFeedSelectionSheet(
          feeds = state.opmlFeedsToSelect,
          onFeedsSelected = { dispatch(SettingsEvent.OnOpmlFeedsSelected(it)) },
          onDismiss = { dispatch(SettingsEvent.ClearOpmlFeedsToSelect) },
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
        item { SubHeader(text = stringResource(Res.string.settingsHeaderSync)) }

        item {
          CloudSyncSettingItem(
            syncProgress = state.syncProgress,
            lastSyncedAt = state.lastSyncedAt,
            availableProviders = availableProviders,
            isSubscribed = state.isSubscribed,
            onSyncClicked = { provider ->
              if (provider.isPremium && !state.isSubscribed) {
                openPaywall()
              } else {
                if (
                  state.hasCloudServiceSignedIn && state.signedInService != provider.cloudService
                ) {
                  showSwitchServiceDialog = provider
                } else {
                  dispatch(SettingsEvent.SyncClicked(provider))
                }
              }
            },
            onAPIServiceClicked = { provider ->
              if (provider.isPremium && !state.isSubscribed) {
                openPaywall()
              } else {
                if (
                  state.hasCloudServiceSignedIn && state.signedInService != provider.cloudService
                ) {
                  showSwitchServiceDialog = provider
                } else {
                  when (provider.cloudService) {
                    ServiceType.FRESH_RSS -> openFreshRssLogin()
                    ServiceType.MINIFLUX -> openMinifluxLogin()
                    else -> {
                      throw IllegalStateException(
                        "Unknown cloud service type: ${provider.cloudService}"
                      )
                    }
                  }
                }
              }
            },
            onSignOutClicked = { dispatch(SettingsEvent.SignOutClicked) },
            onSyncErrorClicked = { showSyncErrorDialog = it },
          )
        }

        item { SettingsDivider(24.dp) }

        item {
          OPMLSettingItem(
            opmlResult = state.opmlResult,
            onImportClicked = { dispatch(SettingsEvent.ImportOpmlClicked) },
            onExportClicked = { dispatch(SettingsEvent.ExportOpmlClicked) },
            onCancelClicked = { dispatch(SettingsEvent.CancelOpmlImportOrExport) },
          )
        }

        item { SubHeader(text = stringResource(Res.string.settingsHeaderData)) }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.enableAutoSyncTitle),
            subtitle = stringResource(Res.string.enableAutoSyncDesc),
            checked = state.enableAutoSync,
            onValueChanged = { newValue -> dispatch(SettingsEvent.ToggleAutoSync(newValue)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsEnableNotificationsTitle),
            subtitle = stringResource(Res.string.settingsEnableNotificationsSubtitle),
            checked = state.enableNotifications,
            onValueChanged = { newValue -> dispatch(SettingsEvent.ToggleNotifications(newValue)) },
          )
        }

        if (state.enableNotifications) {
          item {
            SettingsSwitchItem(
              title = stringResource(Res.string.settingsGroupByFeedNotificationsTitle),
              subtitle = stringResource(Res.string.settingsGroupByFeedNotificationsSubtitle),
              checked = state.groupByFeedNotifications,
              onValueChanged = { newValue ->
                dispatch(SettingsEvent.ToggleGroupByFeedNotifications(newValue))
              },
            )
          }
        }

        item { SettingsDivider(horizontalInsets = 24.dp) }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsBlockImagesTitle),
            subtitle = stringResource(Res.string.settingsBlockImagesSubtitle),
            checked = state.blockImages,
            onValueChanged = { newValue -> dispatch(SettingsEvent.ToggleBlockImages(newValue)) },
          )
        }

        item {
          SettingsSwitchItem(
            title = stringResource(Res.string.settingsDownloadFullContentTitle),
            subtitle = stringResource(Res.string.settingsDownloadFullContentSubtitle),
            checked = state.downloadFullContent,
            onValueChanged = { newValue ->
              dispatch(SettingsEvent.ToggleDownloadFullContent(newValue))
            },
          )
        }
      }
    },
  )
}

@Composable
private fun serviceName(serviceType: ServiceType?): String {
  return when (serviceType) {
    ServiceType.DROPBOX -> stringResource(Res.string.settingsSyncDropbox)
    ServiceType.FRESH_RSS -> stringResource(Res.string.settingsSyncFreshRSS)
    ServiceType.MINIFLUX -> stringResource(Res.string.settingsSyncMiniflux)
    null -> ""
  }
}

@Preview(locale = "en")
@Composable
private fun SettingsServicesPreview() {
  AppTheme {
    SettingsServicesContent(
      state =
        SettingsState.default(
          appInfo =
            AppInfo(
              versionCode = 1,
              versionName = "1.0.0",
              isDebugBuild = true,
              isFoss = false,
              cachePath = { "" },
            )
        ),
      dispatch = {},
      availableProviders = emptySet(),
      goBack = {},
      openPaywall = {},
      openFreshRssLogin = {},
      openMinifluxLogin = {},
    )
  }
}
