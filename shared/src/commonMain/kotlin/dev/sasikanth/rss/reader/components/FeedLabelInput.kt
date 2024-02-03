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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun FeedLabelInput(
  value: String,
  onFeedNameChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  textAlign: TextAlign = TextAlign.Start
) {
  // Maintaining local state so that it updates the text field in the UI
  // instantly and doesn't have any weird UI state issues.
  //
  // I probably can extract this out into the presenter, we would have to
  // maintain a list of text field states that are derived from the feeds list
  // but this seems like a good alternative.
  //
  var input by remember(value) { mutableStateOf(value) }
  var inputModified by remember(value) { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current
  val isInputBlank by derivedStateOf { input.isBlank() }
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  fun onFeedNameChanged(clearFocus: Boolean = true) {
    inputModified = input != value

    if (!isInputBlank && inputModified) {
      onFeedNameChanged.invoke(input)
    }

    if (clearFocus) {
      focusManager.clearFocus()
    }
  }

  LaunchedEffect(isFocused) {
    if (!isFocused && !inputModified) {
      input = value
    }
  }

  LaunchedEffect(input) {
    // Same as setting a debounce
    delay(500)
    onFeedNameChanged(clearFocus = false)
  }

  TextField(
    modifier = modifier.requiredHeight(56.dp).fillMaxWidth(),
    value = input,
    onValueChange = { input = it },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false),
    keyboardActions = KeyboardActions(onDone = { onFeedNameChanged() }),
    singleLine = true,
    textStyle = MaterialTheme.typography.titleMedium.copy(textAlign = textAlign),
    shape = RoundedCornerShape(16.dp),
    enabled = enabled,
    interactionSource = interactionSource,
    colors =
      TextFieldDefaults.colors(
        focusedContainerColor = AppTheme.colorScheme.tintedSurface,
        unfocusedContainerColor = AppTheme.colorScheme.tintedSurface,
        disabledContainerColor = AppTheme.colorScheme.tintedBackground,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        disabledTextColor = AppTheme.colorScheme.textEmphasisHigh,
        focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
        unfocusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
      ),
    placeholder = {
      Text(
        text = LocalStrings.current.feedNameHint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}
