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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ConfirmFeedDeleteDialog
import dev.sasikanth.rss.reader.components.FeedLabelInput
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feed.FeedEffect
import dev.sasikanth.rss.reader.feed.FeedEvent
import dev.sasikanth.rss.reader.feed.FeedPresenter
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.flow.collectLatest

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

  ModalBottomSheet(
    modifier = Modifier.then(modifier),
    onDismissRequest = { feedPresenter.dispatch(FeedEvent.BackClicked) },
    containerColor = AppTheme.colorScheme.tintedBackground,
    contentColor = AppTheme.colorScheme.tintedForeground,
    windowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Vertical).union(WindowInsets.ime),
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)
    ) {
      Spacer(Modifier.requiredHeight(8.dp))

      val feed = state.feed
      if (feed != null) {
        Box(Modifier.requiredSize(64.dp).background(Color.White, RoundedCornerShape(16.dp))) {
          AsyncImage(
            url = feed.icon,
            contentDescription = feed.name,
            modifier =
              Modifier.requiredSize(56.dp).clip(RoundedCornerShape(12.dp)).align(Alignment.Center)
          )
        }

        Spacer(Modifier.requiredHeight(24.dp))

        FeedLabelInput(
          value = feed.name,
          onFeedNameChanged = { newFeedName ->
            feedPresenter.dispatch(FeedEvent.OnFeedNameChanged(newFeedName, feed.link))
          },
          textAlign = TextAlign.Center
        )

        Spacer(Modifier.requiredHeight(8.dp))

        RemoveFeedButton(feed) { feedPresenter.dispatch(FeedEvent.RemoveFeedClicked) }
      } else {
        CircularProgressIndicator(color = AppTheme.colorScheme.tintedForeground)
      }
    }
  }
}

@Composable
private fun RemoveFeedButton(feed: Feed, onRemoveFeedClick: () -> Unit) {
  Box {
    var showConfirmDialog by remember { mutableStateOf(false) }

    TextButton(
      onClick = { showConfirmDialog = true },
      contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 24.dp),
      shape = MaterialTheme.shapes.large
    ) {
      Icon(
        imageVector = Icons.Outlined.Delete,
        contentDescription = LocalStrings.current.editFeeds,
        tint = MaterialTheme.colorScheme.error
      )
      Spacer(Modifier.width(12.dp))
      Text(
        text = LocalStrings.current.removeFeed,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.error
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
}
