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

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.repository.BrowserType
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsPresenter
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun SettingsScreen(settingsPresenter: SettingsPresenter, modifier: Modifier = Modifier) {
  val state by settingsPresenter.state.collectAsState()
  val layoutDirection = LocalLayoutDirection.current

  Scaffold(
    modifier = modifier,
    topBar = {
      Box {
        CenterAlignedTopAppBar(
          title = { Text(LocalStrings.current.settings) },
          navigationIcon = {
            IconButton(onClick = { settingsPresenter.dispatch(SettingsEvent.BackClicked) }) {
              Icon(Icons.Rounded.ArrowBack, contentDescription = null)
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

        Divider(
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
              top = padding.calculateTopPadding(),
              end = padding.calculateEndPadding(layoutDirection),
              bottom = padding.calculateBottomPadding() + 80.dp
            ),
        ) {
          item {
            BrowserTypeSettingItem(
              browserType = state.browserType,
              onBrowserTypeChanged = { newBrowserType ->
                settingsPresenter.dispatch(SettingsEvent.UpdateBrowserType(newBrowserType))
              }
            )
          }

          // TODO: Enable it once featured item blur is handled
          if (false) {
            item {
              FeaturedItemBlurSettingItem(
                featuredItemBlurEnabled = state.enableHomePageBlur,
                onValueChanged = { newValue ->
                  settingsPresenter.dispatch(SettingsEvent.ToggleFeaturedItemBlur(newValue))
                }
              )
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = Color.Unspecified,
  )
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
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
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

      Spacer(Modifier.width(8.dp))

      MaterialTheme(
        colorScheme =
          darkColorScheme(
            primary = AppTheme.colorScheme.tintedSurface,
            onPrimary = AppTheme.colorScheme.tintedForeground,
            outline = AppTheme.colorScheme.outline,
            surfaceVariant = AppTheme.colorScheme.surfaceContainer
          )
      ) {
        Switch(
          checked = checked,
          onCheckedChange = { checked -> onValueChanged(checked) },
        )
      }
    }

    Divider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(horizontal = 24.dp),
      color = AppTheme.colorScheme.surfaceContainer
    )
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
      modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
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

      Spacer(Modifier.width(8.dp))

      MaterialTheme(
        colorScheme =
          darkColorScheme(
            primary = AppTheme.colorScheme.tintedSurface,
            onPrimary = AppTheme.colorScheme.tintedForeground,
            outline = AppTheme.colorScheme.outline,
            surfaceVariant = AppTheme.colorScheme.surfaceContainer
          )
      ) {
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

    Divider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(horizontal = 24.dp),
      color = AppTheme.colorScheme.surfaceContainer
    )
  }
}
