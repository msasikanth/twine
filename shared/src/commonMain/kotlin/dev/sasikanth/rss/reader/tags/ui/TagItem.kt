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

package dev.sasikanth.rss.reader.tags.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.benasher44.uuid.Uuid
import dev.sasikanth.rss.reader.core.model.local.Tag
import dev.sasikanth.rss.reader.resources.icons.Tag
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
fun TagItem(
  tag: Tag,
  modifier: Modifier = Modifier,
  onTagNameChanged: (id: Uuid, newName: String) -> Unit
) {
  Row(
    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp).then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    var input by remember(tag.label) { mutableStateOf(TextFieldValue(text = tag.label)) }
    val focusManager = LocalFocusManager.current
    val keyboardState by keyboardVisibilityAsState()

    LaunchedEffect(keyboardState) {
      if (keyboardState == KeyboardState.Closed) {
        focusManager.clearFocus()
      }
    }

    TextField(
      modifier = Modifier.fillMaxWidth(),
      value = input,
      onValueChange = { input = it },
      keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false),
      keyboardActions = KeyboardActions(onDone = { onTagNameChanged(tag.id, input.text) }),
      singleLine = true,
      shape = RoundedCornerShape(8.dp),
      leadingIcon = {
        Icon(
          imageVector = TwineIcons.Tag,
          contentDescription = null,
          tint = AppTheme.colorScheme.textEmphasisHigh
        )
      },
      colors =
        TextFieldDefaults.colors(
          focusedContainerColor = AppTheme.colorScheme.surfaceContainer,
          unfocusedContainerColor = AppTheme.colorScheme.surfaceContainerLowest,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent,
          disabledIndicatorColor = Color.Transparent,
          errorIndicatorColor = Color.Transparent,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
          unfocusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
        ),
      placeholder = {
        Text(
          text = LocalStrings.current.tagNameHint,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      },
    )
  }
}
