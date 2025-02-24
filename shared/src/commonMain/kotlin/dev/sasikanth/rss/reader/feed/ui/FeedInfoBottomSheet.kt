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

package dev.sasikanth.rss.reader.feed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ConfirmFeedDeleteDialog
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feed.FeedEffect
import dev.sasikanth.rss.reader.feed.FeedEvent
import dev.sasikanth.rss.reader.feed.FeedPresenter
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.DeleteOutline
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.SYSTEM_SCRIM
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

private val HORIZONTAL_PADDING = 24.dp

@Composable
fun FeedInfoBottomSheet(
  feedPresenter: FeedPresenter,
  modifier: Modifier = Modifier,
) {
  val state by feedPresenter.state.collectAsState()

  LaunchedEffect(Unit) {
    feedPresenter.effects.collectLatest { effect ->
      when (effect) {
        FeedEffect.DismissSheet -> feedPresenter.dispatch(FeedEvent.DismissSheet)
      }
    }
  }

  AppTheme(useDarkTheme = true) {
    ModalBottomSheet(
      modifier = Modifier.then(modifier),
      onDismissRequest = { feedPresenter.dispatch(FeedEvent.BackClicked) },
      containerColor = AppTheme.colorScheme.tintedBackground,
      contentColor = Color.Unspecified,
      contentWindowInsets = {
        WindowInsets.systemBars
          .only(WindowInsetsSides.Bottom)
          .union(WindowInsets.ime.only(WindowInsetsSides.Bottom))
      },
      sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
      scrimColor = SYSTEM_SCRIM
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
      ) {
        val feed = state.feed
        if (feed != null) {
          FeedLabelInput(
            modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING),
            feed = feed,
            onFeedNameChange = { newFeedName ->
              feedPresenter.dispatch(
                FeedEvent.OnFeedNameChanged(newFeedName = newFeedName, feedId = feed.id)
              )
            }
          )

          Spacer(Modifier.requiredHeight(8.dp))

          FeedUnreadCount(
            modifier = Modifier.fillMaxWidth().padding(horizontal = HORIZONTAL_PADDING),
            numberOfUnreadPosts = feed.numberOfUnreadPosts,
            onMarkPostsAsRead = { feedPresenter.dispatch(FeedEvent.OnMarkPostsAsRead(feed.id)) }
          )

          Divider(horizontalInsets = HORIZONTAL_PADDING)

          AlwaysFetchSourceArticleSwitch(
            feed = feed,
            onValueChanged = { newValue, feedId ->
              feedPresenter.dispatch(FeedEvent.OnAlwaysFetchSourceArticleChanged(newValue, feedId))
            }
          )

          Divider(horizontalInsets = HORIZONTAL_PADDING)

          FeedOptions(
            modifier = Modifier.padding(horizontal = HORIZONTAL_PADDING),
            feed = feed,
            onRemoveFeedClick = { feedPresenter.dispatch(FeedEvent.RemoveFeedClicked) }
          )

          Spacer(Modifier.requiredHeight(8.dp))
        } else {
          CircularProgressIndicator(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = AppTheme.colorScheme.tintedForeground
          )
        }
      }
    }
  }
}

@Composable
private fun FeedUnreadCount(
  numberOfUnreadPosts: Long,
  modifier: Modifier = Modifier,
  onMarkPostsAsRead: () -> Unit
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Spacer(Modifier.requiredWidth(8.dp))

    val hasUnreadPosts = numberOfUnreadPosts > 0
    val text =
      if (hasUnreadPosts) {
        LocalStrings.current.numberOfUnreadPostsInFeed(numberOfUnreadPosts)
      } else {
        LocalStrings.current.noUnreadPostsInFeed
      }

    Text(
      modifier = Modifier.weight(1f),
      text = text,
      color = AppTheme.colorScheme.textEmphasisHigh,
      style = MaterialTheme.typography.labelLarge,
      textAlign = TextAlign.Start,
    )

    TextButton(
      enabled = hasUnreadPosts,
      onClick = onMarkPostsAsRead,
      shape = MaterialTheme.shapes.medium,
      colors =
        ButtonDefaults.textButtonColors(
          disabledContentColor = AppTheme.colorScheme.onSurface.copy(alpha = 0.38f),
          contentColor = AppTheme.colorScheme.tintedForeground
        )
    ) {
      Icon(
        modifier = Modifier.requiredSize(18.dp),
        imageVector = Icons.Rounded.Check,
        contentDescription = null,
      )

      Spacer(Modifier.requiredWidth(8.dp))

      Text(text = LocalStrings.current.markAsRead, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun FeedLabelInput(
  feed: Feed,
  modifier: Modifier = Modifier,
  onFeedNameChange: (String) -> Unit
) {
  Row(
    Modifier.then(modifier)
      .clip(RoundedCornerShape(24.dp))
      .background(AppTheme.colorScheme.tintedSurface)
      .padding(8.dp)
      .fillMaxWidth()
  ) {
    val showFeedFavIcon = LocalShowFeedFavIconSetting.current
    val feedIcon = if (showFeedFavIcon) feed.homepageLink else feed.icon

    FeedIcon(
      url = feedIcon,
      contentDescription = feed.name,
      modifier = Modifier.requiredSize(56.dp).clip(RoundedCornerShape(16.dp)),
    )

    Spacer(Modifier.requiredWidth(16.dp))

    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = LocalStrings.current.feedTitleHint,
        style = MaterialTheme.typography.labelSmall,
        color = AppTheme.colorScheme.textEmphasisMed
      )

      var input by remember(feed.name) { mutableStateOf(feed.name) }
      val focusManager = LocalFocusManager.current
      val keyboardState by keyboardVisibilityAsState()

      LaunchedEffect(keyboardState) {
        if (keyboardState == KeyboardState.Closed) focusManager.clearFocus()
      }

      val textSelectionColors =
        TextSelectionColors(
          handleColor = AppTheme.colorScheme.tintedForeground,
          backgroundColor = AppTheme.colorScheme.tintedForeground.copy(0.4f)
        )

      // Debounce input changes
      LaunchedEffect(input) {
        if (input.isBlank()) return@LaunchedEffect
        delay(500.milliseconds)
        onFeedNameChange(input)
      }

      CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
        BasicTextField(
          modifier = Modifier.fillMaxWidth(),
          value = input,
          onValueChange = { input = it },
          keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Done),
          keyboardActions =
            KeyboardActions(
              onDone = {
                focusManager.clearFocus()
                onFeedNameChange(input)
              }
            ),
          singleLine = true,
          textStyle =
            MaterialTheme.typography.titleMedium.copy(
              color = AppTheme.colorScheme.textEmphasisHigh
            ),
          cursorBrush = SolidColor(AppTheme.colorScheme.textEmphasisHigh),
        )
      }

      HorizontalDivider(
        color = AppTheme.colorScheme.tintedHighlight,
        modifier = Modifier.padding(end = 32.dp)
      )
    }
  }
}

@Composable
private fun FeedOptions(feed: Feed, onRemoveFeedClick: () -> Unit, modifier: Modifier = Modifier) {
  val coroutineScope = rememberCoroutineScope()
  val linkHandler = LocalLinkHandler.current
  val shareHandler = LocalShareHandler.current
  var showConfirmDialog by remember { mutableStateOf(false) }

  Row(modifier = modifier) {
    FeedOptionItem(
      icon = TwineIcons.Share,
      text = LocalStrings.current.feedOptionShare,
      modifier = Modifier.weight(1f),
      onOptionClick = { shareHandler.share(feed.link) }
    )

    FeedOptionItem(
      icon = TwineIcons.Website,
      text = LocalStrings.current.feedOptionWebsite,
      modifier = Modifier.weight(1f),
      onOptionClick = { coroutineScope.launch { linkHandler.openLink(feed.link) } }
    )

    FeedOptionItem(
      icon = TwineIcons.DeleteOutline,
      text = LocalStrings.current.actionDelete,
      modifier = Modifier.weight(1f),
      onOptionClick = { showConfirmDialog = true }
    )
  }

  if (showConfirmDialog) {
    ConfirmFeedDeleteDialog(
      feedName = feed.name,
      onRemoveFeed = onRemoveFeedClick,
      dismiss = { showConfirmDialog = false },
    )
  }
}

@Composable
private fun AlwaysFetchSourceArticleSwitch(
  feed: Feed,
  modifier: Modifier = Modifier,
  onValueChanged: (newValue: Boolean, feedId: String) -> Unit
) {
  var checked by
    remember(feed.alwaysFetchSourceArticle) { mutableStateOf(feed.alwaysFetchSourceArticle) }

  Row(
    modifier =
      Modifier.clickable {
          checked = !checked
          onValueChanged(checked, feed.id)
        }
        .padding(vertical = 16.dp, horizontal = HORIZONTAL_PADDING),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = LocalStrings.current.alwaysFetchSourceArticle,
      color = AppTheme.colorScheme.textEmphasisHigh,
      style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.width(16.dp))

    Switch(
      modifier = modifier,
      checked = checked,
      onCheckedChange = { newValue -> onValueChanged(newValue, feed.id) }
    )
  }
}

@Composable
private fun FeedOptionItem(
  icon: ImageVector,
  text: String,
  modifier: Modifier = Modifier,
  onOptionClick: () -> Unit
) {
  Column(
    modifier =
      Modifier.clip(RoundedCornerShape(8.dp))
        .clickable { onOptionClick() }
        .padding(vertical = 12.dp)
        .then(modifier),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground,
      modifier = Modifier.size(24.dp)
    )

    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium,
      color = AppTheme.colorScheme.tintedForeground
    )
  }
}

@Composable
private fun Divider(horizontalInsets: Dp = 0.dp) {
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp, horizontal = horizontalInsets),
    color = AppTheme.colorScheme.tintedSurface
  )
}
