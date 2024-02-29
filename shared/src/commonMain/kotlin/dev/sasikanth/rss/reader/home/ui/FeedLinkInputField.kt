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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun FeedLinkInputField(
  modifier: Modifier = Modifier,
  isFetchingFeed: Boolean,
  onAddFeed: (String) -> Unit,
  onCancelFeedEntryClicked: () -> Unit
) {
  var input by remember { mutableStateOf("") }
  val isInputBlank by derivedStateOf { input.isBlank() }

  val focusRequester = remember { FocusRequester() }
  val focusManager = LocalFocusManager.current

  LaunchedEffect(Unit) {
    // Sync between requesting focus and ime padding is broken for some reason after updating
    // to Compose v1.6.0. Adding delay resolves the issue for time being. Will check the cause for
    // this later.
    delay(50)
    focusRequester.requestFocus()
  }

  fun onAddFeed() {
    if (!isInputBlank) {
      onAddFeed.invoke(input)
    }
    focusManager.clearFocus()
  }

  TextField(
    modifier = modifier.requiredHeight(56.dp).fillMaxWidth().focusRequester(focusRequester),
    value = input,
    onValueChange = { input = it },
    keyboardOptions =
      KeyboardOptions(
        imeAction = ImeAction.Done,
        keyboardType = KeyboardType.Uri,
        autoCorrect = false
      ),
    keyboardActions = KeyboardActions(onDone = { onAddFeed() }),
    singleLine = true,
    textStyle = MaterialTheme.typography.labelLarge,
    shape = RoundedCornerShape(16.dp),
    enabled = !isFetchingFeed,
    colors =
      TextFieldDefaults.colors(
        unfocusedContainerColor = AppTheme.colorScheme.tintedSurface,
        focusedContainerColor = AppTheme.colorScheme.tintedSurface,
        disabledContainerColor = AppTheme.colorScheme.tintedSurface,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
      ),
    leadingIcon = {
      IconButton(onClick = onCancelFeedEntryClicked, enabled = !isFetchingFeed) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    },
    trailingIcon = {
      if (isFetchingFeed) {
        CircularProgressIndicator(
          modifier = Modifier.padding(end = 8.dp).requiredSize(24.dp),
          color = AppTheme.colorScheme.tintedForeground,
          strokeWidth = 4.dp
        )
      } else {
        IconButton(
          onClick = { onAddFeed() },
          enabled = !isInputBlank,
          colors =
            IconButtonDefaults.iconButtonColors(
              contentColor = AppTheme.colorScheme.tintedForeground,
              disabledContentColor = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
            ),
        ) {
          Icon(Icons.TwoTone.CheckCircle, contentDescription = LocalStrings.current.buttonAdd)
        }
      }
    },
    placeholder = {
      Text(
        text = LocalStrings.current.feedEntryHint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}
