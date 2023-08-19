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

import androidx.compose.animation.core.Transition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.moriatsushi.insetsx.imePadding
import com.moriatsushi.insetsx.navigationBars
import dev.icerock.moko.resources.compose.stringResource
import dev.sasikanth.rss.reader.CommonRes
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.feeds.FeedsEffect
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.inverseProgress
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FeedsBottomSheet(
  feedsViewModel: FeedsViewModel,
  bottomSheetSwipeTransition: Transition<Float>,
  showingFeedLinkEntry: Boolean,
  closeSheet: () -> Unit
) {
  val state by feedsViewModel.state.collectAsState()
  val selectedFeed = state.selectedFeed

  LaunchedEffect(Unit) {
    feedsViewModel.effects.collect { effect ->
      when (effect) {
        FeedsEffect.MinimizeSheet -> closeSheet()
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    BottomSheetHandle(bottomSheetSwipeTransition)

    Box {
      // Transforming the bottom sheet progress from 0-1 to 1-0,
      // since we want to control the alpha of the content as
      // users swipes the sheet up and down
      val bottomSheetExpandingProgress =
        (bottomSheetSwipeTransition.currentState * 5f).inverseProgress()
      val hasBottomSheetExpandedThreshold = bottomSheetExpandingProgress > 1e-6f

      if (hasBottomSheetExpandedThreshold) {
        BottomSheetCollapsedContent(
          modifier = Modifier.graphicsLayer { alpha = bottomSheetExpandingProgress },
          feeds = state.feeds,
          selectedFeed = selectedFeed,
          onFeedSelected = { feed -> feedsViewModel.dispatch(FeedsEvent.OnFeedSelected(feed)) }
        )
      } else {
        BottomSheetExpandedContent(
          modifier =
            Modifier.graphicsLayer {
              val threshold = 0.3
              val scaleFactor = 1 / (1 - threshold)
              val targetAlpha =
                if (bottomSheetSwipeTransition.currentState > threshold) {
                    (bottomSheetSwipeTransition.currentState - threshold) * scaleFactor
                  } else {
                    0f
                  }
                  .toFloat()
              alpha = targetAlpha
            },
          feeds = state.feeds,
          selectedFeed = state.selectedFeed,
          showingFeedLinkEntry = showingFeedLinkEntry,
          closeSheet = { feedsViewModel.dispatch(FeedsEvent.OnGoBackClicked) },
          onDeleteFeed = { feedsViewModel.dispatch(FeedsEvent.OnDeleteFeed(it)) },
          onFeedSelected = { feedsViewModel.dispatch(FeedsEvent.OnFeedSelected(it)) },
          onFeedNameChanged = { newFeedName, feedLink ->
            feedsViewModel.dispatch(
              FeedsEvent.OnFeedNameUpdated(newFeedName = newFeedName, feedLink = feedLink)
            )
          }
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetExpandedContent(
  feeds: ImmutableList<Feed>,
  selectedFeed: Feed?,
  showingFeedLinkEntry: Boolean,
  closeSheet: () -> Unit,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = Modifier.fillMaxSize().imePadding().then(modifier)) {
    Toolbar(onCloseClicked = closeSheet)

    LazyColumn(contentPadding = PaddingValues(bottom = 112.dp), modifier = Modifier.weight(1f)) {
      itemsIndexed(feeds) { index, feed ->
        FeedListItem(
          feed = feed,
          selected = selectedFeed == feed,
          canShowDivider = index != feeds.lastIndex,
          onDeleteFeed = onDeleteFeed,
          onFeedSelected = onFeedSelected,
          onFeedNameChanged = onFeedNameChanged
        )
      }
    }

    FeedsSheetBottomBar(showingFeedLinkEntry = showingFeedLinkEntry, closeSheet = closeSheet)
  }
}

@Composable
private fun FeedsSheetBottomBar(showingFeedLinkEntry: Boolean, closeSheet: () -> Unit) {
  Box(
    Modifier.background(AppTheme.colorScheme.tintedBackground)
      .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
  ) {
    Divider(Modifier.align(Alignment.TopStart), color = AppTheme.colorScheme.tintedSurface)
    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
      // Placeholder view with similar height of primary action button and input field
      // from the home screen
      Box(Modifier.requiredHeight(56.dp))
      if (!showingFeedLinkEntry) {
        TextButton(
          modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
          onClick = closeSheet
        ) {
          Text(
            text = stringResource(CommonRes.strings.button_go_back),
            style = MaterialTheme.typography.labelLarge,
            color = AppTheme.colorScheme.tintedForeground
          )
        }
      }
    }
  }
}

@Composable
private fun BottomSheetCollapsedContent(
  feeds: ImmutableList<Feed>,
  selectedFeed: Feed?,
  onFeedSelected: (Feed) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.padding(start = 100.dp).fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(end = 24.dp)
  ) {
    items(feeds) { feed ->
      BottomSheetItem(
        text = feed.name.uppercase(),
        iconUrl = feed.icon,
        selected = selectedFeed == feed,
        onClick = { onFeedSelected(feed) }
      )
    }
  }
}
