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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.OutlinedButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.BrowserType
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.Period.ONE_MONTH
import dev.sasikanth.rss.reader.data.repository.Period.ONE_WEEK
import dev.sasikanth.rss.reader.data.repository.Period.ONE_YEAR
import dev.sasikanth.rss.reader.data.repository.Period.SIX_MONTHS
import dev.sasikanth.rss.reader.data.repository.Period.THREE_MONTHS
import dev.sasikanth.rss.reader.opml.OpmlResult
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.coroutines.launch

@Composable
internal fun SettingsScreen(
  settingsPresenter: SettingsPresenter,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by settingsPresenter.state.collectAsState()
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current
  val isSystemInDarkMode =
    when (state.appThemeMode) {
      AppThemeMode.Dark -> true
      else -> isSystemInDarkTheme()
    }

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(LocalStrings.current.settings) },
          navigationIcon = {
            IconButton(onClick = { settingsPresenter.dispatch(SettingsEvent.BackClicked) }) {
              Icon(TwineIcons.ArrowBack, contentDescription = null)
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
            SubHeader(
              text = LocalStrings.current.settingsHeaderBehaviour,
            )
          }

          item {
            ShowReaderViewSettingItem(
              showReaderView = state.showReaderView,
              onValueChanged = { newValue ->
                settingsPresenter.dispatch(SettingsEvent.ToggleShowReaderView(newValue))
              }
            )
          }

          item { Divider(24.dp) }

          item {
            BrowserTypeSettingItem(
              browserType = state.browserType,
              onBrowserTypeChanged = { newBrowserType ->
                settingsPresenter.dispatch(SettingsEvent.UpdateBrowserType(newBrowserType))
              }
            )
          }

          if (canBlurImage && isSystemInDarkMode) {
            item { Divider(horizontalInsets = 24.dp) }

            item {
              FeaturedItemBlurSettingItem(
                featuredItemBlurEnabled = state.enableHomePageBlur,
                onValueChanged = { newValue ->
                  settingsPresenter.dispatch(SettingsEvent.ToggleFeaturedItemBlur(newValue))
                }
              )
            }
          }

          item { Divider(24.dp) }

          item {
            UnreadPostsCountSettingItem(
              showUnreadCountEnabled = state.showUnreadPostsCount,
              onValueChanged = { newValue ->
                settingsPresenter.dispatch(SettingsEvent.ToggleShowUnreadPostsCount(newValue))
              }
            )
          }

          item { Divider(24.dp) }

          item {
            PostsDeletionPeriodSettingItem(
              postsDeletionPeriod = state.postsDeletionPeriod,
              onValueChanged = { newValue ->
                settingsPresenter.dispatch(SettingsEvent.PostsDeletionPeriodChanged(newValue))
              }
            )
          }

          item { Divider(24.dp) }

          item {
            OPMLSettingItem(
              opmlResult = state.opmlResult,
              hasFeeds = state.hasFeeds,
              onImportClicked = { settingsPresenter.dispatch(SettingsEvent.ImportOpmlClicked) },
              onExportClicked = { settingsPresenter.dispatch(SettingsEvent.ExportOpmlClicked) },
              onCancelClicked = {
                settingsPresenter.dispatch(SettingsEvent.CancelOpmlImportOrExport)
              }
            )
          }

          item { Divider() }

          item {
            SubHeader(
              text = LocalStrings.current.settingsHeaderFeedback,
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

          item { AboutItem { settingsPresenter.dispatch(SettingsEvent.AboutClicked) } }

          item { Divider() }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  )
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
          LocalStrings.current.settingsShowReaderViewTitle,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsShowReaderViewSubtitle,
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
      text = LocalStrings.current.settingsPostsDeletionPeriodTitle,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    Box {
      TextButton(onClick = { showDropdown = true }, shape = MaterialTheme.shapes.medium) {
        val period =
          when (postsDeletionPeriod) {
            ONE_WEEK -> LocalStrings.current.settingsPostsDeletionPeriodOneWeek
            ONE_MONTH -> LocalStrings.current.settingsPostsDeletionPeriodOneMonth
            THREE_MONTHS -> LocalStrings.current.settingsPostsDeletionPeriodThreeMonths
            SIX_MONTHS -> LocalStrings.current.settingsPostsDeletionPeriodSixMonths
            ONE_YEAR -> LocalStrings.current.settingsPostsDeletionPeriodOneYear
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
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
      ) {
        Period.entries.forEach { period ->
          val periodString =
            when (period) {
              ONE_WEEK -> LocalStrings.current.settingsPostsDeletionPeriodOneWeek
              ONE_MONTH -> LocalStrings.current.settingsPostsDeletionPeriodOneMonth
              THREE_MONTHS -> LocalStrings.current.settingsPostsDeletionPeriodThreeMonths
              SIX_MONTHS -> LocalStrings.current.settingsPostsDeletionPeriodSixMonths
              ONE_YEAR -> LocalStrings.current.settingsPostsDeletionPeriodOneYear
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
                AppTheme.colorScheme.onSurface
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
          LocalStrings.current.settingsShowUnreadCountTitle,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsShowUnreadCountSubtitle,
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
private fun FeaturedItemBlurSettingItem(
  featuredItemBlurEnabled: Boolean,
  onValueChanged: (Boolean) -> Unit
) {
  var checked by remember(featuredItemBlurEnabled) { mutableStateOf(featuredItemBlurEnabled) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!featuredItemBlurEnabled)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          LocalStrings.current.settingsEnableBlurTitle,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsEnableBlurSubtitle,
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
          LocalStrings.current.settingsBrowserTypeTitle,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsBrowserTypeSubtitle,
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
    SubHeader(text = LocalStrings.current.settingsHeaderOpml)

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
                  LocalStrings.current.settingsOpmlImporting(opmlResult.progress)
                }
                is OpmlResult.InProgress.Exporting -> {
                  LocalStrings.current.settingsOpmlExporting(opmlResult.progress)
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
            Text(LocalStrings.current.settingsOpmlCancel)
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
            Text(LocalStrings.current.settingsOpmlImport)
          }

          OutlinedButton(
            modifier = Modifier.weight(1f),
            enabled = hasFeeds,
            onClick = onExportClicked,
          ) {
            Text(LocalStrings.current.settingsOpmlExport)
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
          LocalStrings.current.settingsReportIssue,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsVersion(appInfo.versionName, appInfo.versionCode),
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
          LocalStrings.current.settingsAboutTitle,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
        Text(
          LocalStrings.current.settingsAboutSubtitle,
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
