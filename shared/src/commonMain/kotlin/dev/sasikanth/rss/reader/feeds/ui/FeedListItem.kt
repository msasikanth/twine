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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.*
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun FeedListItem(
  modifier: Modifier = Modifier,
  feed: Feed,
  selected: Boolean,
  canShowDivider: Boolean,
  feedsSheetMode: FeedsSheetMode,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
) {
  Box(
    modifier =
      modifier.clickable { onFeedSelected(feed) }.fillMaxWidth().padding(start = 20.dp, end = 12.dp)
  ) {
    Row(
      modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(contentAlignment = Alignment.Center) {
        SelectionIndicator(selected = selected && feedsSheetMode != Edit, animationProgress = 1f)

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
              Modifier.requiredSize(48.dp).clip(RoundedCornerShape(12.dp)).align(Alignment.Center),
          )
        }
      }

      Spacer(Modifier.requiredWidth(16.dp))

      FeedLabelInput(
        modifier = Modifier.weight(1f),
        value = feed.name,
        onFeedNameChanged = { newFeedName -> onFeedNameChanged(newFeedName, feed.link) },
        enabled = feedsSheetMode == Edit
      )

      Spacer(Modifier.requiredWidth(16.dp))

      ActionButtons(feed = feed, feedsSheetMode = feedsSheetMode, onDeleteFeed = onDeleteFeed)
    }

    if (canShowDivider) {
      Divider(
        modifier = Modifier.requiredHeight(1.dp).align(Alignment.BottomStart).padding(end = 12.dp),
        color = AppTheme.colorScheme.tintedSurface
      )
    }
  }
}

@Composable
private fun ActionButtons(
  feed: Feed,
  feedsSheetMode: FeedsSheetMode,
  onDeleteFeed: (Feed) -> Unit
) {
  Row {
    when (feedsSheetMode) {
      LinkEntry,
      Default -> {
        ShareIconButton(content = { feed.link })
      }
      Edit -> {
        IconButton(onClick = { onDeleteFeed(feed) }) {
          Icon(
            imageVector = TwineIcons.Delete,
            contentDescription = null,
            tint = AppTheme.colorScheme.tintedForeground
          )
        }
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
  var nameChangeSaved by remember(value) { mutableStateOf(false) }

  val focusManager = LocalFocusManager.current
  val isInputBlank by derivedStateOf { input.isBlank() }
  val interactionSource = remember { MutableInteractionSource() }
  val isFocused by interactionSource.collectIsFocusedAsState()

  LaunchedEffect(isFocused) {
    if (!isFocused && !nameChangeSaved) {
      input = value
    }
  }

  fun onFeedNameChanged() {
    if (!isInputBlank) {
      nameChangeSaved = true
      onFeedNameChanged.invoke(input)
      focusManager.clearFocus()
    }
  }

  TextField(
    modifier = modifier.requiredHeight(56.dp).fillMaxWidth(),
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
            text = LocalStrings.current.buttonChange,
            style = MaterialTheme.typography.labelLarge
          )
        }
      }
    },
    placeholder = {
      Text(
        text = LocalStrings.current.feedNameHint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}

@Composable internal expect fun ShareIconButton(content: () -> String)

@Composable
internal fun ShareIconButtonInternal(onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = TwineIcons.Share,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
  }
}
