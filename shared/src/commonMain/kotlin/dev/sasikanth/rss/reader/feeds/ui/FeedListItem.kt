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

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.*
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.PinFilled
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.delay

@Composable
internal fun FeedListItem(
  modifier: Modifier = Modifier,
  feed: Feed,
  selected: Boolean,
  canPinFeeds: Boolean,
  feedsSheetMode: FeedsSheetMode,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
  onFeedPinClick: (Feed) -> Unit
) {
  val clickableModifier =
    if (feedsSheetMode != Edit) {
      modifier.clickable { onFeedSelected(feed) }
    } else {
      modifier
    }

  Box(modifier = clickableModifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp)) {
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
          BadgedBox(
            badge = {
              val numberOfUnreadPosts = feed.numberOfUnreadPosts
              if (numberOfUnreadPosts > 0) {
                Badge(
                  containerColor = AppTheme.colorScheme.tintedForeground,
                  contentColor = AppTheme.colorScheme.tintedBackground,
                  modifier =
                    Modifier.graphicsLayer {
                      translationX = -4.dp.toPx()
                      translationY = 4.dp.toPx()
                    }
                ) {
                  Text(feed.numberOfUnreadPosts.toString())
                }
              }
            }
          ) {
            AsyncImage(
              url = feed.icon,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier =
                Modifier.requiredSize(48.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .align(Alignment.Center),
            )
          }
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

      AnimatedContent(feedsSheetMode == Edit) {
        ActionButtons(
          feed = feed,
          isInEditMode = it,
          canPinFeed = canPinFeeds,
          onDeleteFeed = onDeleteFeed,
          onFeedPinClick = onFeedPinClick
        )
      }
    }
  }
}

@Composable
private fun ActionButtons(
  feed: Feed,
  isInEditMode: Boolean,
  canPinFeed: Boolean,
  onDeleteFeed: (Feed) -> Unit,
  onFeedPinClick: (Feed) -> Unit
) {
  Row {
    if (isInEditMode) {
      PinFeedIconButton(feed = feed, canPinFeed = canPinFeed, onFeedPinClick = onFeedPinClick)

      IconButton(onClick = { onDeleteFeed(feed) }) {
        Icon(
          imageVector = TwineIcons.Delete,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    } else {
      val shareHandler = LocalShareHandler.current
      ShareIconButton(onClick = { shareHandler.share(feed.link) })
    }
  }
}

@Composable
private fun PinFeedIconButton(
  feed: Feed,
  canPinFeed: Boolean,
  onFeedPinClick: (Feed) -> Unit,
) {
  val pinnedIconColor =
    if (canPinFeed) {
      AppTheme.colorScheme.tintedForeground
    } else {
      AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
    }

  IconButton(onClick = { onFeedPinClick(feed) }, enabled = canPinFeed) {
    Icon(
      imageVector =
        if (feed.pinnedAt != null) {
          TwineIcons.PinFilled
        } else {
          TwineIcons.Pin
        },
      contentDescription = null,
      tint = pinnedIconColor
    )
  }
}

@Composable
private fun FeedLabelInput(
  value: String,
  onFeedNameChanged: (String) -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true
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
    placeholder = {
      Text(
        text = LocalStrings.current.feedNameHint,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
      )
    }
  )
}

@Composable
internal fun ShareIconButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(
      imageVector = TwineIcons.Share,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
  }
}
