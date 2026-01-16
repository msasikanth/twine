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
package dev.sasikanth.rss.reader.settings.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.data.opml.OpmlResult
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.Period.NEVER
import dev.sasikanth.rss.reader.data.repository.Period.ONE_MONTH
import dev.sasikanth.rss.reader.data.repository.Period.ONE_WEEK
import dev.sasikanth.rss.reader.data.repository.Period.ONE_YEAR
import dev.sasikanth.rss.reader.data.repository.Period.SIX_MONTHS
import dev.sasikanth.rss.reader.data.repository.Period.THREE_MONTHS
import dev.sasikanth.rss.reader.data.sync.CloudSyncProvider
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.LayoutCompact
import dev.sasikanth.rss.reader.resources.icons.LayoutDefault
import dev.sasikanth.rss.reader.resources.icons.LayoutSimple
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsEvent.ChangeHomeViewMode
import dev.sasikanth.rss.reader.settings.SettingsState
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.util.relativeDurationString
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.ignoreHorizontalParentPadding
import kotlin.time.Instant
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.blockedWords
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.enableAutoSyncDesc
import twine.shared.generated.resources.enableAutoSyncTitle
import twine.shared.generated.resources.homeViewMode
import twine.shared.generated.resources.homeViewModeCompact
import twine.shared.generated.resources.homeViewModeDefault
import twine.shared.generated.resources.homeViewModeSimple
import twine.shared.generated.resources.ic_launcher_foreground
import twine.shared.generated.resources.markArticleAsRead
import twine.shared.generated.resources.markArticleAsReadOnOpen
import twine.shared.generated.resources.markArticleAsReadOnScroll
import twine.shared.generated.resources.settings
import twine.shared.generated.resources.settingsAboutSubtitle
import twine.shared.generated.resources.settingsAboutTitle
import twine.shared.generated.resources.settingsAmoledSubtitle
import twine.shared.generated.resources.settingsAmoledTitle
import twine.shared.generated.resources.settingsAppIconSubtitle
import twine.shared.generated.resources.settingsAppIconTitle
import twine.shared.generated.resources.settingsBlockImagesSubtitle
import twine.shared.generated.resources.settingsBlockImagesTitle
import twine.shared.generated.resources.settingsBrowserTypeSubtitle
import twine.shared.generated.resources.settingsBrowserTypeTitle
import twine.shared.generated.resources.settingsCustomisations
import twine.shared.generated.resources.settingsDownloadFullContentSubtitle
import twine.shared.generated.resources.settingsDownloadFullContentTitle
import twine.shared.generated.resources.settingsDownloadFullContentWarning
import twine.shared.generated.resources.settingsEnableNotificationsSubtitle
import twine.shared.generated.resources.settingsEnableNotificationsTitle
import twine.shared.generated.resources.settingsHeaderBehaviour
import twine.shared.generated.resources.settingsHeaderFeedback
import twine.shared.generated.resources.settingsHeaderOpml
import twine.shared.generated.resources.settingsHeaderSync
import twine.shared.generated.resources.settingsHeaderTheme
import twine.shared.generated.resources.settingsNotificationWarningAndroid
import twine.shared.generated.resources.settingsOpmlCancel
import twine.shared.generated.resources.settingsOpmlExport
import twine.shared.generated.resources.settingsOpmlExporting
import twine.shared.generated.resources.settingsOpmlImport
import twine.shared.generated.resources.settingsOpmlImporting
import twine.shared.generated.resources.settingsPostsDeletionPeriodNever
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneMonth
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneWeek
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneYear
import twine.shared.generated.resources.settingsPostsDeletionPeriodSixMonths
import twine.shared.generated.resources.settingsPostsDeletionPeriodThreeMonths
import twine.shared.generated.resources.settingsPostsDeletionPeriodTitle
import twine.shared.generated.resources.settingsReportIssue
import twine.shared.generated.resources.settingsShowReaderViewSubtitle
import twine.shared.generated.resources.settingsShowReaderViewTitle
import twine.shared.generated.resources.settingsShowUnreadCountSubtitle
import twine.shared.generated.resources.settingsShowUnreadCountTitle
import twine.shared.generated.resources.settingsSyncDropbox
import twine.shared.generated.resources.settingsSyncSignOut
import twine.shared.generated.resources.settingsSyncStatusFailure
import twine.shared.generated.resources.settingsSyncStatusIdle
import twine.shared.generated.resources.settingsSyncStatusSuccess
import twine.shared.generated.resources.settingsSyncStatusSyncing
import twine.shared.generated.resources.settingsThemeAuto
import twine.shared.generated.resources.settingsThemeDark
import twine.shared.generated.resources.settingsThemeLight
import twine.shared.generated.resources.settingsVersion
import twine.shared.generated.resources.showFeedFavIconDesc
import twine.shared.generated.resources.showFeedFavIconTitle
import twine.shared.generated.resources.twinePremium
import twine.shared.generated.resources.twinePremiumDesc
import twine.shared.generated.resources.twinePremiumSubscribedDesc

private val settingsItemPadding
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Expanded -> PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(0.dp)
    }

@Composable
internal fun SettingsScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openAbout: () -> Unit,
  openBlockedWords: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val appInfo = viewModel.appInfo
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

  LaunchedEffect(state.authUrlToOpen) {
    state.authUrlToOpen?.let { url ->
      linkHandler.openLink(url)
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
              actionIconContentColor = AppTheme.colorScheme.onSurface
            ),
        )

        HorizontalDivider(
          modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
          color = AppTheme.colorScheme.surfaceContainer
        )
      }
    },
    content = { padding ->
      if (state.showAppIconSelectionSheet) {
        AppIconSelectionSheet(
          currentAppIcon = state.appIcon,
          onAppIconChange = {
            viewModel.dispatch(SettingsEvent.OnAppIconChanged(it))
            viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet)
          },
          onDismiss = { viewModel.dispatch(SettingsEvent.CloseAppIconSelectionSheet) }
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
            bottom = padding.calculateBottomPadding() + 80.dp
          ),
      ) {
        // region Twine Premium banner
        item {
          AnimatedVisibility(
            visible = !state.appInfo.isFoss && state.subscriptionResult != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
          ) {
            Column {
              TwinePremiumBanner(
                modifier = Modifier.animateItem(),
                subscriptionResult = state.subscriptionResult,
                onClick = { openPaywall() }
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
            onClick = { viewModel.dispatch(ChangeHomeViewMode(it)) }
          )
        }

        item { Divider(24.dp) }

        item {
          SubHeader(
            text = stringResource(Res.string.settingsHeaderTheme),
          )
        }

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
                  identifier = AppThemeMode.Light
                ),
                ToggleableButtonItem(
                  label = stringResource(Res.string.settingsThemeDark),
                  isSelected = appThemeMode == AppThemeMode.Dark,
                  identifier = AppThemeMode.Dark
                )
              ),
            onItemSelected = {
              viewModel.dispatch(SettingsEvent.OnAppThemeModeChanged(it.identifier as AppThemeMode))
            }
          )
        }

        item {
          AnimatedVisibility(
            visible = AppTheme.isDark,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
          ) {
            AmoledSettingItem(
              useAmoled = state.useAmoled,
              onValueChanged = { newValue ->
                viewModel.dispatch(SettingsEvent.ToggleAmoled(newValue))
              }
            )
          }
        }

        if (state.canSubscribe) {
          item { Divider(24.dp) }

          item {
            AppIconSettingItem(
              appIcon = state.appIcon,
              isSubscribed = state.isSubscribed,
              onClick = { viewModel.dispatch(SettingsEvent.AppIconClicked) }
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
            availableProviders = viewModel.availableProviders,
            onSyncClicked = { provider -> viewModel.dispatch(SettingsEvent.SyncClicked(provider)) },
            onSignOutClicked = { provider ->
              viewModel.dispatch(SettingsEvent.SignOutClicked(provider))
            }
          )
        }

        item { Divider() }
        // endregion

        // region Behaviour settings
        item {
          SubHeader(
            text = stringResource(Res.string.settingsHeaderBehaviour),
          )
        }

        item {
          ShowReaderViewSettingItem(
            showReaderView = state.showReaderView,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowReaderView(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          BrowserTypeSettingItem(
            browserType = state.browserType,
            onBrowserTypeChanged = { newBrowserType ->
              viewModel.dispatch(SettingsEvent.UpdateBrowserType(newBrowserType))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          UnreadPostsCountSettingItem(
            showUnreadCountEnabled = state.showUnreadPostsCount,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowUnreadPostsCount(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          AutoSyncSettingItem(
            enableAutoSync = state.enableAutoSync,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleAutoSync(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          ShowFeedFavIconSettingItem(
            showFeedFavIcon = state.showFeedFavIcon,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleShowFeedFavIcon(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          BlockImagesSettingItem(
            blockImages = state.blockImages,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleBlockImages(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          NotificationsSettingItem(
            enableNotifications = state.enableNotifications,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleNotifications(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          DownloadFullContentSettingItem(
            downloadFullContent = state.downloadFullContent,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.ToggleDownloadFullContent(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item { BlockedWordsSettingItem { openBlockedWords() } }

        item { Divider(24.dp) }

        item {
          MarkArticleAsReadOnSetting(articleMarkAsReadOn = state.markAsReadOn) {
            viewModel.dispatch(SettingsEvent.MarkAsReadOnChanged(it))
          }
        }

        item { Divider(24.dp) }

        item {
          PostsDeletionPeriodSettingItem(
            postsDeletionPeriod = state.postsDeletionPeriod,
            onValueChanged = { newValue ->
              viewModel.dispatch(SettingsEvent.PostsDeletionPeriodChanged(newValue))
            }
          )
        }

        item { Divider(24.dp) }

        item {
          OPMLSettingItem(
            opmlResult = state.opmlResult,
            hasFeeds = state.hasFeeds,
            onImportClicked = { viewModel.dispatch(SettingsEvent.ImportOpmlClicked) },
            onExportClicked = { viewModel.dispatch(SettingsEvent.ExportOpmlClicked) },
            onCancelClicked = { viewModel.dispatch(SettingsEvent.CancelOpmlImportOrExport) }
          )
        }

        item { Divider() }
        // endregion

        // region Feedback and about
        item {
          SubHeader(
            text = stringResource(Res.string.settingsHeaderFeedback),
          )
        }

        item {
          ReportIssueItem(
            appInfo = state.appInfo,
            onClick = {
              coroutineScope.launch { linkHandler.openLink(Constants.REPORT_ISSUE_LINK) }
            }
          )
        }

        item { Divider() }

        item { AboutItem { openAbout() } }

        item { Divider() }
        // endregion
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  )
}

@Composable
private fun AppIconSettingItem(
  appIcon: AppIcon,
  isSubscribed: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      Modifier.clickable(onClick = onClick)
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .fillMaxWidth()
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = stringResource(Res.string.settingsAppIconTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )

        if (!isSubscribed) {
          Spacer(Modifier.width(8.dp))

          Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = AppTheme.colorScheme.primary
          )
        }
      }

      Text(
        text = stringResource(Res.string.settingsAppIconSubtitle),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisMed
      )
    }

    AppIconPreview(appIcon = appIcon, modifier = Modifier.size(48.dp))
  }
}

@Composable
private fun AppIconSelectionSheet(
  currentAppIcon: AppIcon,
  onAppIconChange: (AppIcon) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    LazyVerticalGrid(
      columns = GridCells.Fixed(3),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 48.dp),
    ) {
      items(AppIcon.entries) { appIcon ->
        val isSelected = appIcon == currentAppIcon
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier =
            Modifier.clip(RoundedCornerShape(12.dp))
              .clickable { onAppIconChange(appIcon) }
              .padding(8.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            val shape = RoundedCornerShape(28.dp)

            AppIconPreview(appIcon = appIcon, shape = shape, modifier = Modifier.size(64.dp))

            if (isSelected) {
              Box(
                Modifier.matchParentSize().clip(shape).background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          Spacer(Modifier.height(8.dp))

          Text(
            text = appIcon.title,
            style = MaterialTheme.typography.labelMedium,
            color =
              if (isSelected) AppTheme.colorScheme.tintedForeground
              else AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1
          )
        }
      }
    }
  }
}

@Composable
private fun AppIconPreview(
  appIcon: AppIcon,
  modifier: Modifier = Modifier,
  shape: Shape = CircleShape,
) {
  val backgroundColor =
    when (appIcon) {
      AppIcon.AntiqueGold -> Color(0xFFC5A059)
      AppIcon.Cranberry -> Color(0xFFF62F27)
      AppIcon.DarkJade -> Color(0xFF006C53)
      AppIcon.DeepIce -> Color(0xFF29B6F6)
      AppIcon.DeepTeal -> Color(0xFF00838F)
      AppIcon.DustyRose -> Color(0xFFC98CA7)
      AppIcon.RoyalPlum -> Color(0xFF693764)
      AppIcon.SlateBlue -> Color(0xFF375069)
      AppIcon.SoftSage -> Color(0xFF9BB49D)
      AppIcon.StormySky -> Color(0xFF607D8B)
    }
  val backgroundBrush =
    Brush.radialGradient(
      0.17f to backgroundColor.copy(alpha = 0.55f).compositeOver(Color.White),
      1f to backgroundColor,
      center = Offset(20f, 24f)
    )

  Box(
    modifier = modifier.clip(shape).background(backgroundBrush),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      painter = painterResource(Res.drawable.ic_launcher_foreground),
      contentDescription = null,
      tint = Color.Unspecified,
      modifier = Modifier.scale(1.2f).fillMaxSize()
    )
  }
}

@Composable
private fun HomeLayoutSelector(
  homeViewMode: HomeViewMode,
  onClick: (HomeViewMode) -> Unit,
) {
  Row(modifier = Modifier.padding(horizontal = 8.dp)) {
    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeDefault),
      icon = TwineIcons.LayoutDefault,
      selected = homeViewMode == HomeViewMode.Default,
      onClick = { onClick(HomeViewMode.Default) }
    )

    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeSimple),
      icon = TwineIcons.LayoutSimple,
      selected = homeViewMode == HomeViewMode.Simple,
      onClick = { onClick(HomeViewMode.Simple) }
    )

    LayoutIconButton(
      modifier = Modifier.weight(1f),
      label = stringResource(Res.string.homeViewModeCompact),
      icon = TwineIcons.LayoutCompact,
      selected = homeViewMode == HomeViewMode.Compact,
      onClick = { onClick(HomeViewMode.Compact) }
    )
  }
}

@Composable
private fun LayoutIconButton(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier =
      Modifier.then(modifier)
        .clip(MaterialTheme.shapes.medium)
        .clickable { onClick() }
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val defaultTranslucentStyle = LocalTranslucentStyles.current.default
    val background =
      if (selected) {
        Color.Transparent
      } else {
        defaultTranslucentStyle.background
      }
    val border =
      if (selected) {
        defaultTranslucentStyle.outline.copy(alpha = 0.48f)
      } else {
        defaultTranslucentStyle.outline
      }
    val shape =
      if (selected) {
        MaterialTheme.shapes.medium
      } else {
        MaterialTheme.shapes.small
      }
    val iconTint =
      if (selected) {
        AppTheme.colorScheme.inverseOnSurface
      } else {
        AppTheme.colorScheme.outline
      }
    val padding by animateDpAsState(if (selected) 0.dp else 4.dp)

    Box(
      modifier =
        Modifier.requiredSize(48.dp)
          .padding(padding)
          .background(background, shape)
          .border(1.dp, border, shape),
      contentAlignment = Alignment.Center
    ) {
      val iconBackground by
        animateColorAsState(
          if (selected) {
            AppTheme.colorScheme.primary
          } else {
            Color.Transparent
          }
        )
      val iconBackgroundSize by animateDpAsState(if (selected) 40.dp else 0.dp)

      Box(
        modifier =
          Modifier.requiredSize(iconBackgroundSize)
            .background(iconBackground, MaterialTheme.shapes.small),
      )

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.requiredSize(20.dp)
      )
    }

    Spacer(Modifier.requiredHeight(4.dp))

    val textStyle =
      if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
    Text(
      text = label,
      style = textStyle,
      color = AppTheme.colorScheme.onSurface,
    )
  }
}

@Composable
fun TwinePremiumBanner(
  subscriptionResult: SubscriptionResult?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Row(
    modifier =
      modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(32.dp),
      imageVector = Icons.Rounded.WorkspacePremium,
      contentDescription = null,
      tint = AppTheme.colorScheme.primary,
    )

    Column {
      if (subscriptionResult != SubscriptionResult.Subscribed) {
        Text(
          text = stringResource(Res.string.twinePremium),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
      }

      val subscriptionDescRes =
        if (subscriptionResult == SubscriptionResult.Subscribed) {
          Res.string.twinePremiumSubscribedDesc
        } else {
          Res.string.twinePremiumDesc
        }
      Text(
        text = stringResource(subscriptionDescRes),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisMed
      )
    }
  }
}

@Composable
private fun MarkArticleAsReadOnSetting(
  articleMarkAsReadOn: MarkAsReadOn,
  onMarkAsReadOnChanged: (MarkAsReadOn) -> Unit
) {
  var showDropdown by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.markArticleAsRead),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    Box {
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

      TextButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
        onClick = { showDropdown = true },
        shape = MaterialTheme.shapes.medium
      ) {
        val markAsReadOnLabel =
          when (articleMarkAsReadOn) {
            MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
            MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
          }

        Text(
          text = markAsReadOnLabel,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground
        )

        Spacer(Modifier.requiredWidth(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }

      DropdownMenu(
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
      ) {
        MarkAsReadOn.entries.forEach { markAsReadOn ->
          val label =
            when (markAsReadOn) {
              MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
              MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
            }

          val backgroundColor =
            if (markAsReadOn == articleMarkAsReadOn) {
              AppTheme.colorScheme.tintedHighlight
            } else {
              Color.Unspecified
            }

          DropdownMenuItem(
            onClick = {
              onMarkAsReadOnChanged(markAsReadOn)
              showDropdown = false
            },
            modifier = Modifier.background(backgroundColor)
          ) {
            val textColor =
              if (markAsReadOn == articleMarkAsReadOn) {
                AppTheme.colorScheme.inverseOnSurface
              } else {
                AppTheme.colorScheme.textEmphasisHigh
              }

            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = textColor)
          }
        }
      }
    }
  }
}

@Composable
private fun BlockedWordsSettingItem(onClick: () -> Unit) {
  Row(
    modifier = Modifier.clickable { onClick() }.padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.blockedWords),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh
    )
  }
}

@Composable
private fun ShowReaderViewSettingItem(showReaderView: Boolean, onValueChanged: (Boolean) -> Unit) {
  var checked by remember(showReaderView) { mutableStateOf(showReaderView) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!showReaderView)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsShowReaderViewTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsShowReaderViewSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun AmoledSettingItem(useAmoled: Boolean, onValueChanged: (Boolean) -> Unit) {
  var checked by remember(useAmoled) { mutableStateOf(useAmoled) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!useAmoled)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsAmoledTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsAmoledSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun PostsDeletionPeriodSettingItem(
  postsDeletionPeriod: Period?,
  onValueChanged: (Period) -> Unit
) {
  var showDropdown by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.settingsPostsDeletionPeriodTitle),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    Box {
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

      TextButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
        onClick = { showDropdown = true },
        shape = MaterialTheme.shapes.medium
      ) {
        val period =
          when (postsDeletionPeriod) {
            ONE_WEEK -> stringResource(Res.string.settingsPostsDeletionPeriodOneWeek)
            ONE_MONTH -> stringResource(Res.string.settingsPostsDeletionPeriodOneMonth)
            THREE_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodThreeMonths)
            SIX_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodSixMonths)
            ONE_YEAR -> stringResource(Res.string.settingsPostsDeletionPeriodOneYear)
            NEVER -> stringResource(Res.string.settingsPostsDeletionPeriodNever)
            null -> ""
          }

        Text(
          text = period,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground
        )

        Spacer(Modifier.requiredWidth(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }

      DropdownMenu(
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
      ) {
        Period.entries.forEach { period ->
          val periodString =
            when (period) {
              ONE_WEEK -> stringResource(Res.string.settingsPostsDeletionPeriodOneWeek)
              ONE_MONTH -> stringResource(Res.string.settingsPostsDeletionPeriodOneMonth)
              THREE_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodThreeMonths)
              SIX_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodSixMonths)
              ONE_YEAR -> stringResource(Res.string.settingsPostsDeletionPeriodOneYear)
              NEVER -> stringResource(Res.string.settingsPostsDeletionPeriodNever)
            }

          val backgroundColor =
            if (period == postsDeletionPeriod) {
              AppTheme.colorScheme.tintedHighlight
            } else {
              Color.Unspecified
            }

          DropdownMenuItem(
            onClick = {
              onValueChanged(period)
              showDropdown = false
            },
            modifier = Modifier.background(backgroundColor)
          ) {
            val textColor =
              if (period == postsDeletionPeriod) {
                AppTheme.colorScheme.inverseOnSurface
              } else {
                AppTheme.colorScheme.textEmphasisHigh
              }

            Text(text = periodString, style = MaterialTheme.typography.bodyLarge, color = textColor)
          }
        }
      }
    }
  }
}

@Composable
private fun BlockImagesSettingItem(blockImages: Boolean, onValueChanged: (Boolean) -> Unit) {
  var checked by remember(blockImages) { mutableStateOf(blockImages) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!blockImages)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsBlockImagesTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsBlockImagesSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun ShowFeedFavIconSettingItem(
  showFeedFavIcon: Boolean,
  onValueChanged: (Boolean) -> Unit
) {
  var checked by remember(showFeedFavIcon) { mutableStateOf(showFeedFavIcon) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!showFeedFavIcon)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.showFeedFavIconTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.showFeedFavIconDesc),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun NotificationsSettingItem(
  enableNotifications: Boolean,
  onValueChanged: (Boolean) -> Unit
) {
  val translucentStyles = LocalTranslucentStyles.current
  val linkHandler = LocalLinkHandler.current
  val coroutineScope = rememberCoroutineScope()

  Column(
    modifier =
      Modifier.clickable { onValueChanged(!enableNotifications) }
        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(Res.string.settingsEnableNotificationsTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          text = stringResource(Res.string.settingsEnableNotificationsSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = enableNotifications,
        onCheckedChange = onValueChanged,
      )
    }

    AnimatedVisibility(
      modifier = Modifier.ignoreHorizontalParentPadding(horizontal = 12.dp),
      visible = platform == Platform.Android && enableNotifications
    ) {
      Column {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          modifier =
            Modifier.clickable {
                coroutineScope.launch { linkHandler.openLink(Constants.DONT_KILL_MY_APP_LINK) }
              }
              .background(translucentStyles.default.background, MaterialTheme.shapes.small)
              .border(1.dp, AppTheme.colorScheme.secondary, MaterialTheme.shapes.small)
              .padding(horizontal = 12.dp, vertical = 8.dp),
          text = stringResource(Res.string.settingsNotificationWarningAndroid),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.secondary
        )
      }
    }
  }
}

@Composable
private fun DownloadFullContentSettingItem(
  downloadFullContent: Boolean,
  onValueChanged: (Boolean) -> Unit
) {
  val translucentStyles = LocalTranslucentStyles.current
  Column(
    modifier =
      Modifier.clickable { onValueChanged(!downloadFullContent) }
        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsDownloadFullContentTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsDownloadFullContentSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = downloadFullContent,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      modifier =
        Modifier.ignoreHorizontalParentPadding(horizontal = 12.dp)
          .background(
            AppTheme.colorScheme.error.copy(alpha = translucentStyles.default.background.alpha),
            MaterialTheme.shapes.small
          )
          .border(1.dp, AppTheme.colorScheme.error, MaterialTheme.shapes.small)
          .padding(horizontal = 12.dp, vertical = 8.dp),
      text = stringResource(Res.string.settingsDownloadFullContentWarning),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.error
    )
  }
}

@Composable
private fun AutoSyncSettingItem(enableAutoSync: Boolean, onValueChanged: (Boolean) -> Unit) {
  var checked by remember(enableAutoSync) { mutableStateOf(enableAutoSync) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!enableAutoSync)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.enableAutoSyncTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.enableAutoSyncDesc),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun UnreadPostsCountSettingItem(
  showUnreadCountEnabled: Boolean,
  onValueChanged: (Boolean) -> Unit
) {
  var checked by remember(showUnreadCountEnabled) { mutableStateOf(showUnreadCountEnabled) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!showUnreadCountEnabled)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsShowUnreadCountTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsShowUnreadCountSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }
  }
}

@Composable
private fun BrowserTypeSettingItem(
  browserType: BrowserType,
  onBrowserTypeChanged: (BrowserType) -> Unit
) {
  var checked by remember(browserType) { mutableStateOf(browserType == BrowserType.InApp) }

  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        val newBrowserType =
          if (checked) {
            BrowserType.InApp
          } else {
            BrowserType.Default
          }

        onBrowserTypeChanged(newBrowserType)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsBrowserTypeTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsBrowserTypeSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = checked,
        onCheckedChange = { checked ->
          val newBrowserType =
            if (checked) {
              BrowserType.InApp
            } else {
              BrowserType.Default
            }

          onBrowserTypeChanged(newBrowserType)
        },
      )
    }
  }
}

@Composable
private fun OPMLSettingItem(
  opmlResult: OpmlResult?,
  hasFeeds: Boolean,
  onImportClicked: () -> Unit,
  onExportClicked: () -> Unit,
  onCancelClicked: () -> Unit
) {
  Column {
    SubHeader(text = stringResource(Res.string.settingsHeaderOpml))

    when (opmlResult) {
      is OpmlResult.InProgress.Importing,
      is OpmlResult.InProgress.Exporting -> {
        Row(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = {
              // no-op
            },
            enabled = false,
            colors =
              ButtonDefaults.outlinedButtonColors(
                containerColor = AppTheme.colorScheme.tintedSurface,
                disabledContainerColor = AppTheme.colorScheme.tintedSurface,
                contentColor = AppTheme.colorScheme.tintedForeground,
                disabledContentColor = AppTheme.colorScheme.tintedForeground,
              ),
            border = null
          ) {
            val string =
              when (opmlResult) {
                is OpmlResult.InProgress.Importing -> {
                  stringResource(Res.string.settingsOpmlImporting, opmlResult.progress)
                }
                is OpmlResult.InProgress.Exporting -> {
                  stringResource(Res.string.settingsOpmlExporting, opmlResult.progress)
                }
                else -> {
                  ""
                }
              }

            Text(string)
          }

          OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onCancelClicked,
            colors =
              ButtonDefaults.outlinedButtonColors(
                containerColor = Color.Unspecified,
                contentColor = AppTheme.colorScheme.tintedForeground,
              ),
          ) {
            Text(stringResource(Res.string.settingsOpmlCancel))
          }
        }
      }

      // TODO: Handle error states
      OpmlResult.Idle,
      OpmlResult.Error.NoContentInOpmlFile,
      is OpmlResult.Error.UnknownFailure, -> {
        Row(
          modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onImportClicked,
          ) {
            Text(stringResource(Res.string.settingsOpmlImport))
          }

          OutlinedButton(
            modifier = Modifier.weight(1f),
            enabled = hasFeeds,
            onClick = onExportClicked,
          ) {
            Text(stringResource(Res.string.settingsOpmlExport))
          }
        }
      }
      null -> {
        Box(Modifier.requiredHeight(64.dp))
      }
    }
  }
}

@Composable
private fun ReportIssueItem(appInfo: AppInfo, onClick: () -> Unit) {
  Box(modifier = Modifier.clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsReportIssue),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsVersion, appInfo.versionName, appInfo.versionCode),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }
    }
  }
}

@Composable
private fun AboutItem(onClick: () -> Unit) {
  Box(modifier = Modifier.clickable(onClick = onClick)) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsAboutTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          stringResource(Res.string.settingsAboutSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }

      AboutProfileImages()
    }
  }
}

@Composable
private fun AboutProfileImages() {
  Box(contentAlignment = Alignment.Center) {
    val backgroundColor = AppTheme.colorScheme.surfaceContainerLowest

    AsyncImage(
      model = Constants.ABOUT_ED_PIC,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.padding(start = 72.dp)
          .requiredSize(62.dp)
          .drawWithCache {
            onDrawBehind {
              drawCircle(
                color = backgroundColor,
              )
            }
          }
          .padding(8.dp)
          .clip(CircleShape)
    )

    AsyncImage(
      model = Constants.ABOUT_SASI_PIC,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.requiredSize(62.dp)
          .drawWithCache {
            onDrawBehind {
              drawCircle(
                color = backgroundColor,
              )
            }
          }
          .padding(8.dp)
          .clip(CircleShape)
    )
  }
}

@Composable
private fun CloudSyncSettingItem(
  syncProgress: SettingsState.SyncProgress,
  lastSyncedAt: Instant?,
  availableProviders: List<CloudSyncProvider>,
  onSyncClicked: (CloudSyncProvider) -> Unit,
  onSignOutClicked: (CloudSyncProvider) -> Unit
) {
  availableProviders.forEach { provider ->
    val label =
      when (provider.name) {
        "Dropbox" -> stringResource(Res.string.settingsSyncDropbox)
        else -> provider.name
      }
    val isSignedIn by provider.isSignedIn().collectAsStateWithLifecycle(false)

    Box(
      modifier =
        Modifier.clickable(enabled = provider.isSupported) { onSyncClicked(provider) }
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          val alpha = if (provider.isSupported) 1f else 0.38f
          Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = AppTheme.colorScheme.textEmphasisHigh.copy(alpha = alpha)
          )

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
              lastSyncedAt != null
          ) {
            statusString += " \u2022 ${lastSyncedAt.relativeDurationString()}"
          }

          AnimatedVisibility(visible = syncProgress != SettingsState.SyncProgress.Idle) {
            Text(
              text = statusString,
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.textEmphasisMed
            )
          }
        }

        if (provider.isSupported) {
          if (isSignedIn) {
            TextButton(
              onClick = { onSignOutClicked(provider) },
            ) {
              Text(
                text = stringResource(Res.string.settingsSyncSignOut),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun Divider(horizontalInsets: Dp = 0.dp) {
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp, horizontal = horizontalInsets),
    color = AppTheme.colorScheme.surfaceContainer
  )
}
