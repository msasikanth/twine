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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles

@Composable
fun TextField(
  value: String,
  onValueChange: (String) -> Unit,
  hint: String,
  modifier: Modifier = Modifier,
  keyboardActions: KeyboardActions = KeyboardActions(),
  keyboardOptions: KeyboardOptions = KeyboardOptions(),
  enabled: Boolean = true,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailingIcon: @Composable (() -> Unit)? = null,
  supportingText: @Composable (() -> Unit)? = null,
) {
  val translucentStyles = LocalTranslucentStyles.current
  val containerShape = RoundedCornerShape(50)

  androidx.compose.material3.OutlinedTextField(
    modifier = modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = true,
    textStyle = MaterialTheme.typography.labelLarge,
    shape = containerShape,
    enabled = enabled,
    visualTransformation = visualTransformation,
    colors =
      OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = translucentStyles.default.background,
        focusedContainerColor = translucentStyles.default.background,
        disabledContainerColor = translucentStyles.default.background,
        cursorColor = AppTheme.colorScheme.primary,
        selectionColors =
          TextSelectionColors(
            handleColor = AppTheme.colorScheme.tintedForeground,
            backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f),
          ),
        unfocusedBorderColor = translucentStyles.default.outline,
        focusedBorderColor = translucentStyles.default.outline,
      ),
    placeholder = {
      Text(
        text = hint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f),
      )
    },
    supportingText = supportingText,
    trailingIcon = trailingIcon,
  )
}

@Composable
fun TextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  hint: String,
  modifier: Modifier = Modifier,
  keyboardActions: KeyboardActions = KeyboardActions(),
  keyboardOptions: KeyboardOptions = KeyboardOptions(),
  enabled: Boolean = true,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  trailingIcon: @Composable (() -> Unit)? = null,
) {
  val translucentStyles = LocalTranslucentStyles.current
  val containerShape = RoundedCornerShape(50)

  androidx.compose.material3.TextField(
    modifier =
      modifier
        .requiredHeight(56.dp)
        .fillMaxWidth()
        .border(1.dp, translucentStyles.default.outline, containerShape),
    value = value,
    onValueChange = onValueChange,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = true,
    textStyle = MaterialTheme.typography.labelLarge,
    shape = containerShape,
    enabled = enabled,
    visualTransformation = visualTransformation,
    colors =
      TextFieldDefaults.colors(
        unfocusedContainerColor = translucentStyles.default.background,
        focusedContainerColor = translucentStyles.default.background,
        disabledContainerColor = translucentStyles.default.background,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        cursorColor = AppTheme.colorScheme.primary,
        selectionColors =
          TextSelectionColors(
            handleColor = AppTheme.colorScheme.tintedForeground,
            backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f),
          ),
      ),
    placeholder = {
      Text(
        text = hint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f),
      )
    },
    trailingIcon = trailingIcon,
  )
}
