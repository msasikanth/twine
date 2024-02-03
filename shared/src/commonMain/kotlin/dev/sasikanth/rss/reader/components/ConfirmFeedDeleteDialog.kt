/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun ConfirmFeedDeleteDialog(
  feedName: String,
  onRemoveFeed: () -> Unit,
  dismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = dismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onRemoveFeed()
          dismiss()
        },
        shape = MaterialTheme.shapes.large
      ) {
        Text(
          text = LocalStrings.current.delete,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    dismissButton = {
      TextButton(onClick = dismiss, shape = MaterialTheme.shapes.large) {
        Text(
          text = LocalStrings.current.buttonCancel,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }
    },
    title = {
      Text(text = LocalStrings.current.removeFeed, color = AppTheme.colorScheme.textEmphasisMed)
    },
    text = {
      Text(
        text = LocalStrings.current.removeFeedDesc(feedName),
        color = AppTheme.colorScheme.textEmphasisMed
      )
    },
    containerColor = AppTheme.colorScheme.tintedSurface,
    titleContentColor = AppTheme.colorScheme.onSurface,
    textContentColor = AppTheme.colorScheme.onSurface,
  )
}
