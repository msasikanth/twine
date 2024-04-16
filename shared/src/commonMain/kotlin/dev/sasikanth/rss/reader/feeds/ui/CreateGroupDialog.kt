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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun CreateGroupDialog(
  onCreateGroup: (String) -> Unit,
  modifier: Modifier = Modifier,
  onDismiss: () -> Unit
) {
  var groupName by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  AlertDialog(
    modifier = modifier,
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onCreateGroup(groupName)
          onDismiss()
        },
        shape = MaterialTheme.shapes.large,
        enabled = groupName.isNotBlank(),
        colors =
          ButtonDefaults.textButtonColors(
            contentColor = AppTheme.colorScheme.tintedForeground,
            disabledContentColor = AppTheme.colorScheme.onSurface.copy(alpha = 0.38f)
          )
      ) {
        Text(text = LocalStrings.current.buttonAdd, style = MaterialTheme.typography.labelLarge)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, shape = MaterialTheme.shapes.large) {
        Text(
          text = LocalStrings.current.buttonCancel,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisHigh
        )
      }
    },
    title = {
      Text(text = LocalStrings.current.createGroup, color = AppTheme.colorScheme.textEmphasisHigh)
    },
    text = {
      TextField(
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        value = groupName,
        onValueChange = { groupName = it },
        maxLines = 1,
        keyboardOptions =
          KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
          ),
        keyboardActions = KeyboardActions(onDone = { onCreateGroup(groupName) }),
        placeholder = {
          Text(
            text = LocalStrings.current.groupNameHint,
            color = AppTheme.colorScheme.textEmphasisMed,
            style = MaterialTheme.typography.bodyLarge
          )
        },
        colors =
          TextFieldDefaults.colors(
            unfocusedContainerColor = AppTheme.colorScheme.tintedBackground,
            focusedContainerColor = AppTheme.colorScheme.tintedBackground,
            unfocusedIndicatorColor = Color.Unspecified,
            focusedIndicatorColor = AppTheme.colorScheme.tintedForeground,
            cursorColor = AppTheme.colorScheme.tintedForeground,
            selectionColors =
              TextSelectionColors(
                handleColor = AppTheme.colorScheme.tintedForeground,
                backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f)
              )
          )
      )
    },
    containerColor = AppTheme.colorScheme.tintedSurface,
    titleContentColor = AppTheme.colorScheme.onSurface,
    textContentColor = AppTheme.colorScheme.onSurface,
  )
}
