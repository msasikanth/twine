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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.resources.icons.Account
import dev.sasikanth.rss.reader.resources.icons.Appearance
import dev.sasikanth.rss.reader.resources.icons.Behaviors
import dev.sasikanth.rss.reader.resources.icons.Changelog
import dev.sasikanth.rss.reader.resources.icons.Sync
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.settings.ui.items.SettingsNavigationItem
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settings
import twine.shared.generated.resources.settingsAppInfoAndFeedback
import twine.shared.generated.resources.settingsAppearanceAndLayout
import twine.shared.generated.resources.settingsAppearanceAndLayoutSubtitle
import twine.shared.generated.resources.settingsFeaturesAndBehaviors
import twine.shared.generated.resources.settingsFeaturesAndBehaviorsSubtitle
import twine.shared.generated.resources.settingsServicesAndSync
import twine.shared.generated.resources.settingsServicesAndSyncSubtitle
import twine.shared.generated.resources.settingsYourInsights
import twine.shared.generated.resources.settingsYourInsightsSubtitle

@Composable
internal fun SettingsScreen(
  goBack: () -> Unit,
  openAppearanceSettings: () -> Unit,
  openBehaviorSettings: () -> Unit,
  openServicesSettings: () -> Unit,
  openDataSettings: () -> Unit,
  openAppInfoSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = { SimpleTopAppBar(title = stringResource(Res.string.settings), onBackClick = goBack) },
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

        item {
          SettingsNavigationItem(
            title = stringResource(Res.string.settingsAppInfoAndFeedback),
            subtitle = null,
            icon = TwineIcons.Changelog,
            onClick = openAppInfoSettings,
          )
        }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}
