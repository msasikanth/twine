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

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.ToggleableButtonGroup
import dev.sasikanth.rss.reader.components.ToggleableButtonItem
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.Period.NEVER
import dev.sasikanth.rss.reader.data.repository.Period.ONE_MONTH
import dev.sasikanth.rss.reader.data.repository.Period.ONE_WEEK
import dev.sasikanth.rss.reader.data.repository.Period.ONE_YEAR
import dev.sasikanth.rss.reader.data.repository.Period.SIX_MONTHS
import dev.sasikanth.rss.reader.data.repository.Period.THREE_MONTHS
import dev.sasikanth.rss.reader.opml.OpmlResult
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.blockedWords
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.enableAutoSyncDesc
import twine.shared.generated.resources.enableAutoSyncTitle
import twine.shared.generated.resources.markArticleAsRead
import twine.shared.generated.resources.markArticleAsReadOnOpen
import twine.shared.generated.resources.markArticleAsReadOnScroll
import twine.shared.generated.resources.settings
import twine.shared.generated.resources.settingsAboutSubtitle
import twine.shared.generated.resources.settingsAboutTitle
import twine.shared.generated.resources.settingsBrowserTypeSubtitle
import twine.shared.generated.resources.settingsBrowserTypeTitle
import twine.shared.generated.resources.settingsHeaderBehaviour
import twine.shared.generated.resources.settingsHeaderFeedback
import twine.shared.generated.resources.settingsHeaderOpml
import twine.shared.generated.resources.settingsHeaderTheme
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
import twine.shared.generated.resources.settingsThemeAuto
import twine.shared.generated.resources.settingsThemeDark
import twine.shared.generated.resources.settingsThemeLight
import twine.shared.generated.resources.settingsVersion
import twine.shared.generated.resources.showFeedFavIconDesc
import twine.shared.generated.resources.showFeedFavIconTitle
import twine.shared.generated.resources.twinePremium
import twine.shared.generated.resources.twinePremiumDesc
import twine.shared.generated.resources.twinePremiumSubscribedDesc

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
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

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
          title = { Text(stringResource(Res.string.settings)) },
          navigationIcon = {
            IconButton(onClick = { goBack() }) {
              Icon(
                TwineIcons.ArrowBack,
                contentDescription = stringResource(Res.string.buttonGoBack)
              )
            }
          },
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
      Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
          contentPadding =
            PaddingValues(
              start = padding.calculateStartPadding(layoutDirection),
              top = padding.calculateTopPadding() + 8.dp,
              end = padding.calculateEndPadding(layoutDirection),
              bottom = padding.calculateBottomPadding() + 80.dp
            ),
        ) {
          item {
            TwinePremium(subscriptionResult = state.subscriptionResult, onClick = { openPaywall() })
          }

          item { Divider() }

          item {
            SubHeader(
              text = stringResource(Res.string.settingsHeaderTheme),
            )
          }

          item {
            val appThemeMode = state.appThemeMode
            ToggleableButtonGroup(
              modifier =
                Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
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
                viewModel.dispatch(
                  SettingsEvent.OnAppThemeModeChanged(it.identifier as AppThemeMode)
                )
              }
            )
          }

          item { Divider() }

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
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  )
}

@Composable
fun TwinePremium(
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
      url = Constants.ABOUT_ED_PIC,
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
      url = Constants.ABOUT_SASI_PIC,
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
private fun Divider(horizontalInsets: Dp = 0.dp) {
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp, horizontal = horizontalInsets),
    color = AppTheme.colorScheme.surfaceContainer
  )
}
