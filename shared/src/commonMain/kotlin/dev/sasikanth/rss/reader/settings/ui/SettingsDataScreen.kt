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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.SimpleTopAppBar
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.items.DeleteAppDataConfirmationDialog
import dev.sasikanth.rss.reader.settings.ui.items.DeleteAppDataSettingItem
import dev.sasikanth.rss.reader.settings.ui.items.StatisticsItem
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsYourInsights

@Composable
internal fun SettingsDataScreen(
  viewModel: SettingsViewModel,
  goBack: () -> Unit,
  openStatistics: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val layoutDirection = LocalLayoutDirection.current
  var showDeleteAppDataConfirmation by remember { mutableStateOf(false) }

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
      SimpleTopAppBar(title = stringResource(Res.string.settingsYourInsights), onBackClick = goBack)
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
        item { StatisticsItem { openStatistics() } }

        item { SettingsDivider(24.dp) }

        item { DeleteAppDataSettingItem { showDeleteAppDataConfirmation = true } }
      }
    },
    containerColor = AppTheme.colorScheme.backdrop,
    contentColor = Color.Unspecified,
  )
}
