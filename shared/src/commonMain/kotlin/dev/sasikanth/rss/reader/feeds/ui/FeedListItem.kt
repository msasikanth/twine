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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import dev.sasikanth.rss.reader.CommonRes
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.components.DropdownMenuShareItem
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import dev.sasikanth.rss.reader.utils.pressInteraction
import dev.sasikanth.rss.reader.utils.toDp

@Composable
internal fun FeedListItem(
  modifier: Modifier = Modifier,
  feed: Feed,
  selected: Boolean,
  canShowDivider: Boolean,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
) {
  val hapticFeedback = LocalHapticFeedback.current
  val coroutineScope = rememberCoroutineScope()
  val interactionSource = remember { MutableInteractionSource() }
  val keyboardState by keyboardVisibilityAsState()

  var dropdownOffset by remember(feed) { mutableStateOf(Offset.Zero) }
  var dropdownMenuExpanded by remember(feed) { mutableStateOf(false) }
  var feedNameEditable by remember(feed) { mutableStateOf(false) }

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      feedNameEditable = false
    }
  }

  Box(
    modifier =
      modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(feed) {
          detectTapGestures(
            onTap = {
              pressInteraction(
                coroutineScope = coroutineScope,
                interactionSource = interactionSource,
                offset = it,
              ) {
                onFeedSelected(feed)
              }
            },
            onLongPress = {
              pressInteraction(
                coroutineScope = coroutineScope,
                interactionSource = interactionSource,
                offset = it,
              ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                dropdownOffset = it
                dropdownMenuExpanded = true
              }
            }
          )
        }
        .fillMaxWidth()
        .padding(start = 20.dp, end = 12.dp)
  ) {
    Row(
      modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(contentAlignment = Alignment.Center) {
        SelectionIndicator(selected = selected, animationProgress = 1f)
        Box(
          modifier =
            Modifier.requiredSize(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            url = feed.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.requiredSize(48.dp).clip(RoundedCornerShape(16.dp)).align(Alignment.Center),
          )
        }
      }
      Spacer(Modifier.requiredWidth(16.dp))

      FeedLabelInput(
        modifier = Modifier.weight(1f),
        value = feed.name,
        onFeedNameChanged = { newFeedName ->
          feedNameEditable = false
          onFeedNameChanged(newFeedName, feed.link)
        },
        enabled = feedNameEditable
      )

      Spacer(Modifier.requiredWidth(16.dp))
      IconButton(onClick = { onDeleteFeed(feed) }) {
        Icon(
          painter = painterResource(CommonRes.images.ic_delete),
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    }

    if (canShowDivider) {
      Divider(
        modifier = Modifier.requiredHeight(1.dp).align(Alignment.BottomStart).padding(end = 12.dp),
        color = AppTheme.colorScheme.tintedSurface
      )
    }

    Box {
      DropdownMenu(
        expanded = dropdownMenuExpanded,
        onDismissRequest = { dropdownMenuExpanded = false },
        offset = DpOffset(dropdownOffset.x.toDp(), dropdownOffset.y.toDp())
      ) {
        DropdownMenuItem(
          text = { Text(stringResource(CommonRes.strings.edit_feed_name)) },
          leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
          onClick = {
            dropdownMenuExpanded = false
            feedNameEditable = true
          }
        )

        DropdownMenuShareItem(
          contentToShare = feed.link,
          onShareMenuOpened = { dropdownMenuExpanded = false }
        )
      }
    }
  }
}

@Composable
private fun FeedLabelInput(
  value: String,
  onFeedNameChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
) {
  var input by remember(value) { mutableStateOf(value) }

  val focusManager = LocalFocusManager.current
  val focusRequester = remember { FocusRequester() }
  val isInputBlank by derivedStateOf { input.isBlank() }
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  LaunchedEffect(enabled) {
    if (enabled) {
      focusRequester.requestFocus()
    } else {
      focusRequester.freeFocus()
    }
  }

  fun onFeedNameChanged() {
    if (!isInputBlank) {
      onFeedNameChanged.invoke(input)
      focusManager.clearFocus()
    }
  }

  TextField(
    modifier = modifier.requiredHeight(56.dp).fillMaxWidth().focusRequester(focusRequester),
    value = input,
    onValueChange = { input = it },
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrect = false),
    keyboardActions = KeyboardActions(onDone = { onFeedNameChanged() }),
    singleLine = true,
    textStyle = MaterialTheme.typography.titleMedium,
    shape = RoundedCornerShape(16.dp),
    enabled = enabled,
    interactionSource = interactionSource,
    colors =
      TextFieldDefaults.colors(
        focusedContainerColor = AppTheme.colorScheme.tintedSurface,
        disabledContainerColor = AppTheme.colorScheme.tintedBackground,
        unfocusedContainerColor = AppTheme.colorScheme.tintedBackground,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
        disabledTextColor = AppTheme.colorScheme.tintedForeground,
        focusedTextColor = AppTheme.colorScheme.tintedForeground,
        unfocusedTextColor = AppTheme.colorScheme.tintedForeground,
      ),
    trailingIcon = {
      if (isFocused) {
        TextButton(
          modifier = Modifier.padding(end = 8.dp),
          enabled = !isInputBlank,
          onClick = { onFeedNameChanged() },
          colors =
            ButtonDefaults.textButtonColors(
              contentColor = AppTheme.colorScheme.tintedForeground,
              disabledContentColor = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
            )
        ) {
          Text(
            text = stringResource(CommonRes.strings.button_change),
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    },
    placeholder = {
      Text(
        text = stringResource(CommonRes.strings.feed_name_hint),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}
