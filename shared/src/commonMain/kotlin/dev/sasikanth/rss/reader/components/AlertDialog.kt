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

package dev.sasikanth.rss.reader.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun AlertDialog(
  title: String,
  text: String,
  confirmText: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  dismissText: String? = null,
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
    text = { Text(text = text, style = MaterialTheme.typography.bodyMedium) },
    confirmButton = { TranslucentButton(text = confirmText, onClick = onConfirm) },
    dismissButton = {
      if (dismissText != null) {
        InverseButton(text = dismissText, onClick = onDismiss)
      }
    },
    containerColor = AppTheme.colorScheme.surfaceContainerHigh,
    titleContentColor = AppTheme.colorScheme.onSurface,
    textContentColor = AppTheme.colorScheme.onSurfaceVariant,
  )
}
