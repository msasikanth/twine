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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.settingsDeleteAppDataButton
import twine.shared.generated.resources.settingsDeleteAppDataDialogDesc
import twine.shared.generated.resources.settingsDeleteAppDataDialogTitle
import twine.shared.generated.resources.settingsDeleteAppDataSubtitle
import twine.shared.generated.resources.settingsDeleteAppDataTitle

@Composable
internal fun DeleteAppDataSettingItem(onClick: () -> Unit) {
  Row(
    modifier =
      Modifier.clickable(onClick = onClick)
        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        stringResource(Res.string.settingsDeleteAppDataTitle),
        style = MaterialTheme.typography.titleMedium,
        color = AppTheme.colorScheme.error,
      )
      Text(
        stringResource(Res.string.settingsDeleteAppDataSubtitle),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisMed,
      )
    }
  }
}

@Composable
internal fun DeleteAppDataConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(
          text = stringResource(Res.string.settingsDeleteAppDataButton),
          color = AppTheme.colorScheme.error,
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(
          text = stringResource(Res.string.buttonCancel),
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }
    },
    title = {
      Text(
        text = stringResource(Res.string.settingsDeleteAppDataDialogTitle),
        color = AppTheme.colorScheme.textEmphasisHigh,
      )
    },
    text = {
      Text(
        text = stringResource(Res.string.settingsDeleteAppDataDialogDesc),
        color = AppTheme.colorScheme.textEmphasisMed,
      )
    },
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    titleContentColor = AppTheme.colorScheme.textEmphasisHigh,
    textContentColor = AppTheme.colorScheme.textEmphasisMed,
  )
}
