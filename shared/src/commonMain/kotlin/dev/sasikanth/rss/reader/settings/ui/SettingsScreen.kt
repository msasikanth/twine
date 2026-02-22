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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.SubHeader
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Account
import dev.sasikanth.rss.reader.resources.icons.Appearance
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Behaviors
import dev.sasikanth.rss.reader.resources.icons.Sync
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.AboutItem
import dev.sasikanth.rss.reader.settings.ui.items.ReportIssueSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.SettingsNavigationItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.settings
import twine.shared.generated.resources.settingsAppearanceAndLayout
import twine.shared.generated.resources.settingsAppearanceAndLayoutSubtitle
import twine.shared.generated.resources.settingsFeaturesAndBehaviors
import twine.shared.generated.resources.settingsFeaturesAndBehaviorsSubtitle
import twine.shared.generated.resources.settingsHeaderFeedback
import twine.shared.generated.resources.settingsServicesAndSync
import twine.shared.generated.resources.settingsServicesAndSyncSubtitle
import twine.shared.generated.resources.settingsYourInsights
import twine.shared.generated.resources.settingsYourInsightsSubtitle

@Composable
internal fun SettingsScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openAppearanceSettings: () -> Unit,
  openBehaviorSettings: () -> Unit,
  openServicesSettings: () -> Unit,
  openDataSettings: () -> Unit,
  openAbout: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val layoutDirection = LocalLayoutDirection.current
  val linkHandler = LocalLinkHandler.current

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        TopAppBar(
          title = {
            Text(
              modifier = Modifier.padding(start = 12.dp),
              text = stringResource(Res.string.settings),
              color = AppTheme.colorScheme.onSurface,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
            )
          },
          navigationIcon = {
            CircularIconButton(
              modifier = Modifier.padding(start = 12.dp),
              icon = TwineIcons.ArrowBack,
              label = stringResource(Res.string.buttonGoBack),
              backgroundColor = AppTheme.colorScheme.inverseSurface,
              borderColor = AppTheme.colorScheme.inverseSurface,
              contentColor = AppTheme.colorScheme.inverseOnSurface,
              onClick = goBack,
            )
          },
          contentPadding = PaddingValues(vertical = 8.dp),
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = AppTheme.colorScheme.surfaceContainerHigh,
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
          SettingsNavigationItem(
            title = stringResource(Res.string.settingsAppearanceAndLayout),
            subtitle = stringResource(Res.string.settingsAppearanceAndLayoutSubtitle),
            icon = TwineIcons.Appearance,
            onClick = openAppearanceSettings,
          )
        }

        item {
          SettingsNavigationItem(
            title = stringResource(Res.string.settingsFeaturesAndBehaviors),
            subtitle = stringResource(Res.string.settingsFeaturesAndBehaviorsSubtitle),
            icon = TwineIcons.Behaviors,
            onClick = openBehaviorSettings,
          )
        }

        item {
          SettingsNavigationItem(
            title = stringResource(Res.string.settingsServicesAndSync),
            subtitle = stringResource(Res.string.settingsServicesAndSyncSubtitle),
            icon = TwineIcons.Sync,
            onClick = openServicesSettings,
          )
        }

        item {
          SettingsNavigationItem(
            title = stringResource(Res.string.settingsYourInsights),
            subtitle = stringResource(Res.string.settingsYourInsightsSubtitle),
            icon = TwineIcons.Account,
            onClick = openDataSettings,
          )
        }

        item { SettingsDivider() }

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

        item { SettingsDivider() }

        item { AboutItem { openAbout() } }

        item { SettingsDivider() }
        // endregion
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}
