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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.SnackbarResult
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsEvent.ChangeHomeViewMode
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.AboutItem
import dev.sasikanth.rss.reader.settings.ui.items.AmoledSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSelectionSheet
import dev.sasikanth.rss.reader.settings.ui.items.AppIconSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.AutoSyncSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.BlockImagesSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.BlockedWordsSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.BrowserTypeSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.CloudSyncSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.DeleteAppDataConfirmationDialog
import dev.sasikanth.rss.reader.settings.ui.items.DeleteAppDataSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.DownloadFullContentSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.DynamicColorSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.HomeLayoutSelector
import dev.sasikanth.rss.reader.settings.ui.items.MarkAsReadOnSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.NotificationsSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OPMLSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.OpmlFeedSelectionSheet
import dev.sasikanth.rss.reader.settings.ui.items.PostsDeletionPeriodSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.ReportIssueSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.ShowFeedFavIconSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.ShowReaderViewSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.StatisticsItem
import dev.sasikanth.rss.reader.settings.ui.items.TwinePremiumBanner
import dev.sasikanth.rss.reader.settings.ui.items.UnreadPostsCountSettingItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.homeViewMode
import twine.shared.generated.resources.settings
import twine.shared.generated.resources.settingsCustomisations
import twine.shared.generated.resources.settingsFreeFeedLimitReached
import twine.shared.generated.resources.settingsHeaderBehaviour
import twine.shared.generated.resources.settingsHeaderData
import twine.shared.generated.resources.settingsHeaderFeedback
import twine.shared.generated.resources.settingsHeaderSync
import twine.shared.generated.resources.settingsHeaderTheme
import twine.shared.generated.resources.settingsThemeAuto
import twine.shared.generated.resources.settingsThemeDark
import twine.shared.generated.resources.settingsThemeLight
import twine.shared.generated.resources.settingsUpgradeToPremium

private val settingsItemPadding: PaddingValues
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(0.dp)
    }
  }

@Composable
internal fun SettingsScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openAbout: () -> Unit,
  openStatistics: () -> Unit,
  openBlockedWords: () -> Unit,
  openPaywall: () -> Unit,
  openFreshRssLogin: () -> Unit,
  openMinifluxLogin: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val appInfo = viewModel.appInfo
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

  var showDeleteAppDataConfirmation by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val freeFeedLimitReachedString = stringResource(Res.string.settingsFreeFeedLimitReached)
  val upgradeToPremiumString = stringResource(Res.string.settingsUpgradeToPremium)

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

  LaunchedEffect(state.showFreeFeedLimitWarning) {
    if (state.showFreeFeedLimitWarning) {
      launch {
        delay(2.seconds)
        viewModel.dispatch(SettingsEvent.MarkFreeFeedLimitWarningAsDone)
      }

      val result =
        snackbarHostState.showSnackbar(
          message = freeFeedLimitReachedString,
          actionLabel = upgradeToPremiumString,
        )

      if (result == SnackbarResult.ActionPerformed) {
        openPaywall()
      }
    }
  }

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.dispatch(SettingsEvent.LoadSubscriptionStatus)
  }

  if (showDeleteAppDataConfirmation) {
    DeleteAppDataConfirmationDialog(
      onConfirm = {
        showDeleteAppDataConfirmation = false
        viewModel.dispatch(SettingsEvent.DeleteAppData)
      },
      onDismiss = { showDeleteAppDataConfirmation = false },
    )
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = {
            Text(
              stringResource(Res.string.settings),
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
    snackbarHost = {
      SnackbarHost(hostState = snackbarHostState) { snackbarData ->
        Snackbar(
          modifier = Modifier.padding(12.dp),
          content = {
            Text(text = snackbarData.message, maxLines = 4, overflow = TextOverflow.Ellipsis)
          },
          action = {
            snackbarData.actionLabel?.let { actionLabel ->
              TextButton(
                onClick = { snackbarHostState.currentSnackbarData?.performAction() },
                colors =
                  ButtonDefaults.textButtonColors(
                    contentColor = AppTheme.colorScheme.tintedForeground
                  ),
                shape = MaterialTheme.shapes.medium,
              ) {
                Text(text = actionLabel, style = MaterialTheme.typography.labelLarge)
              }
            }
          },
          actionOnNewLine = false,
          shape = SnackbarDefaults.shape,
          backgroundColor = AppTheme.colorScheme.surfaceContainerLow,
          contentColor = AppTheme.colorScheme.onSurface,
          elevation = 0.dp,
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

      if (state.showAppIconSelectionSheet) {
        AppIconSelectionSheet(
          currentAppIcon = state.appIcon,
          onAppIconChange = {
            viewModel.dispatch(SettingsEvent.OnAppIconChanged(it))
            viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet)
          },
          onDismiss = { viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet) },
        )
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start =
              padding.calculateStartPadding(layoutDirection) +
                settingsItemPadding.calculateStartPadding(layoutDirection),
            top = padding.calculateTopPadding() + 8.dp,
            end =
              padding.calculateEndPadding(layoutDirection) +
                settingsItemPadding.calculateEndPadding(layoutDirection),
            bottom = padding.calculateBottomPadding() + 80.dp,
          ),
      ) {
        // region Twine Premium banner
        item {
          AnimatedVisibility(
            visible = !state.appInfo.isFoss && state.subscriptionResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            Column {
              TwinePremiumBanner(
                modifier = Modifier.animateItem(),
                subscriptionResult = state.subscriptionResult,
                onClick = { openPaywall() },
              )

              Divider()
            }
          }
        }
        // endregion

        // region Customisation settings
        item { SubHeader(text = stringResource(Res.string.settingsCustomisations)) }

        item { SubHeader(text = stringResource(Res.string.homeViewMode)) }

        item {
          HomeLayoutSelector(
            homeViewMode = state.homeViewMode,
            onClick = { viewModel.dispatch(ChangeHomeViewMode(it)) },
          )
        }

        item { Divider(24.dp) }

        item { SubHeader(text = stringResource(Res.string.settingsHeaderTheme)) }

        item {
          val appThemeMode = state.appThemeMode
          ToggleableButtonGroup(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            items =
              listOf(
                ToggleableButtonItem(
                  label = stringResource(Res.string.settingsThemeAuto),
                  isSelected = appThemeMode == AppThemeMode.Auto,
                  identifier = AppThemeMode.Auto,
                ),
                ToggleableButtonItem(
                  label = stringResource(Res.string.settingsThemeLight),
                  isSelected = appThemeMode == AppThemeMode.Light,
                  identifier = AppThemeMode.Light,
                ),
                ToggleableButtonItem(
                  label = stringResource(Res.string.settingsThemeDark),
                  isSelected = appThemeMode == AppThemeMode.Dark,
                  identifier = AppThemeMode.Dark,
                ),
              ),
            onItemSelected = {
              viewModel.dispatch(SettingsEvent.OnAppThemeModeChanged(it.identifier as AppThemeMode))
            },
          )
        }

        item {
          AnimatedVisibility(
            visible = AppTheme.isDark,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
          ) {
            AmoledSettingItem(
              useAmoled = state.useAmoled,
              onValueChanged = { newValue ->
                viewModel.dispatch(SettingsEvent.ToggleAmoled(newValue))
              },
            )
          }
        }

        item {
          DynamicColorSettingItem(
            dynamicColorEnabled = state.dynamicColorEnabled,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleDynamicColor(newValue))
            },
          )
        }

        if (state.canSubscribe) {
          item { Divider(24.dp) }

          item {
            AppIconSettingItem(
              appIcon = state.appIcon,
              isSubscribed = state.isSubscribed,
              onClick = { viewModel.dispatch(SettingsEvent.AppIconClicked) },
            )
          }
        }

        item { Divider() }
        // endregion

        // region Sync settings
        item { SubHeader(text = stringResource(Res.string.settingsHeaderSync)) }

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

        item { Divider() }
        // endregion

        // region Behaviour settings
        item { SubHeader(text = stringResource(Res.string.settingsHeaderBehaviour)) }

        item {
          ShowReaderViewSettingItem(
            showReaderView = state.showReaderView,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowReaderView(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          BrowserTypeSettingItem(
            browserType = state.browserType,
            onBrowserTypeChanged = { newBrowserType ->
              viewModel.dispatch(SettingsEvent.UpdateBrowserType(newBrowserType))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          UnreadPostsCountSettingItem(
            showUnreadCountEnabled = state.showUnreadPostsCount,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowUnreadPostsCount(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          AutoSyncSettingItem(
            enableAutoSync = state.enableAutoSync,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleAutoSync(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          ShowFeedFavIconSettingItem(
            showFeedFavIcon = state.showFeedFavIcon,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowFeedFavIcon(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          BlockImagesSettingItem(
            blockImages = state.blockImages,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleBlockImages(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          NotificationsSettingItem(
            enableNotifications = state.enableNotifications,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleNotifications(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          DownloadFullContentSettingItem(
            downloadFullContent = state.downloadFullContent,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleDownloadFullContent(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item { BlockedWordsSettingItem { openBlockedWords() } }

        item { Divider(24.dp) }

        item {
          MarkAsReadOnSettingItem(articleMarkAsReadOn = state.markAsReadOn) {
            viewModel.dispatch(SettingsEvent.MarkAsReadOnChanged(it))
          }
        }

        item { Divider(24.dp) }

        item {
          PostsDeletionPeriodSettingItem(
            postsDeletionPeriod = state.postsDeletionPeriod,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.PostsDeletionPeriodChanged(newValue))
            },
          )
        }

        item { Divider(24.dp) }

        item {
          OPMLSettingItem(
            opmlResult = state.opmlResult,
            hasFeeds = state.hasFeeds,
            onImportClicked = { viewModel.dispatch(SettingsEvent.ImportOpmlClicked) },
            onExportClicked = { viewModel.dispatch(SettingsEvent.ExportOpmlClicked) },
            onCancelClicked = { viewModel.dispatch(SettingsEvent.CancelOpmlImportOrExport) },
          )
        }

        item { Divider() }
        // endregion

        // region Data
        item { SubHeader(text = stringResource(Res.string.settingsHeaderData)) }

        item { StatisticsItem { openStatistics() } }

        item { Divider(24.dp) }

        item { DeleteAppDataSettingItem { showDeleteAppDataConfirmation = true } }

        item { Divider() }
        // endregion

        // region Feedback and about
        item { SubHeader(text = stringResource(Res.string.settingsHeaderFeedback)) }

        item {
          ReportIssueSettingItem(
            appInfo = state.appInfo,
            onClick = {
              coroutineScope.launch { linkHandler.openLink(Constants.REPORT_ISSUE_LINK) }
            },
          )
        }

        item { Divider() }

        item { AboutItem { openAbout() } }

        item { Divider() }
        // endregion
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}

@Composable
private fun Divider(horizontalInsets: Dp = 0.dp) {
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp, horizontal = horizontalInsets),
    color = AppTheme.colorScheme.outlineVariant,
  )
}
