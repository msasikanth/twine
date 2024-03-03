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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ConfirmFeedDeleteDialog
import dev.sasikanth.rss.reader.components.FeedLabelInput
import dev.sasikanth.rss.reader.components.image.AsyncImage
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
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

  // Doing this before `ModalBottomSheet` as this value is not available
  // after `ModalSheetConsumes` insets, I think? It's returning 0 when
  // I do this inside `ModalBottomSheet`.
  val systemBarsBottomPadding =
    WindowInsets.systemBars
      .only(WindowInsetsSides.Bottom)
      .asPaddingValues()
      .calculateBottomPadding()

  val keyboardState by keyboardVisibilityAsState()

  ModalBottomSheet(
    modifier = Modifier.then(modifier),
    onDismissRequest = { feedPresenter.dispatch(FeedEvent.BackClicked) },
    containerColor = AppTheme.colorScheme.tintedBackground,
    contentColor = Color.Unspecified,
    windowInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom),
    sheetState = SheetState(skipPartiallyExpanded = true, density = LocalDensity.current)
  ) {
    val bottomPadding =
      if (keyboardState == KeyboardState.Closed) {
        systemBarsBottomPadding
      } else {
        0.dp
      }

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(bottom = bottomPadding)
          .padding(horizontal = 24.dp)
          .verticalScroll(rememberScrollState()),
    ) {
      Spacer(Modifier.requiredHeight(8.dp))

      val feed = state.feed
      if (feed != null) {
        Box(
          Modifier.requiredSize(64.dp)
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 24.dp)
            .align(Alignment.CenterHorizontally)
        ) {
          AsyncImage(
            url = feed.icon,
            contentDescription = feed.name,
            modifier =
              Modifier.requiredSize(56.dp).clip(RoundedCornerShape(12.dp)).align(Alignment.Center)
          )
        }

        Spacer(Modifier.requiredHeight(24.dp))

        FeedLabelInput(
          modifier = Modifier.padding(horizontal = 24.dp),
          value = feed.name,
          onFeedNameChanged = { newFeedName ->
            feedPresenter.dispatch(FeedEvent.OnFeedNameChanged(newFeedName, feed.link))
          },
          textAlign = TextAlign.Center
        )

        Spacer(Modifier.requiredHeight(16.dp))

        Divider()

        AlwaysFetchSourceArticleSwitch(
          feed = feed,
          onValueChanged = { newValue, feedLink ->
            feedPresenter.dispatch(FeedEvent.OnAlwaysFetchSourceArticleChanged(newValue, feedLink))
          }
        )

        Divider()

        FeedOptions(
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
      text = LocalStrings.current.feedOptionRemove,
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
  onValueChanged: (newValue: Boolean, feedLink: String) -> Unit
) {
  var checked by
    remember(feed.alwaysFetchSourceArticle) { mutableStateOf(feed.alwaysFetchSourceArticle) }

  Row(
    modifier =
      Modifier.clickable {
          checked = !checked
          onValueChanged(checked, feed.link)
        }
        .padding(vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = LocalStrings.current.alwaysFetchSourceArticle,
      color = AppTheme.colorScheme.textEmphasisHigh,
      style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.width(16.dp))

    MaterialTheme(
      colorScheme =
        darkColorScheme(
          primary = AppTheme.colorScheme.tintedHighlight,
          onPrimary = AppTheme.colorScheme.tintedForeground,
          outline = AppTheme.colorScheme.outline,
          surfaceVariant = AppTheme.colorScheme.surfaceContainerLowest
        )
    ) {
      Switch(
        modifier = modifier,
        checked = checked,
        onCheckedChange = { newValue -> onValueChanged(newValue, feed.link) }
      )
    }
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
    color = AppTheme.colorScheme.outlineVariant
  )
}
