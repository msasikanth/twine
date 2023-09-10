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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Transition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.feeds.FeedsEffect
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.*
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.inverseProgress
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FeedsBottomSheet(
  feedsPresenter: FeedsPresenter,
  bottomSheetSwipeTransition: Transition<Float>,
  feedsSheetMode: FeedsSheetMode,
  closeSheet: () -> Unit,
  editFeeds: () -> Unit,
  exitFeedsEdit: () -> Unit
) {
  val state by feedsPresenter.state.collectAsState()
  val selectedFeed = state.selectedFeed

  LaunchedEffect(Unit) {
    feedsPresenter.effects.collect { effect ->
      when (effect) {
        FeedsEffect.MinimizeSheet -> closeSheet()
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    BottomSheetHandle(bottomSheetSwipeTransition)

    // Transforming the bottom sheet progress from 0-1 to 1-0,
    // since we want to control the alpha of the content as
    // users swipes the sheet up and down
    val bottomSheetExpandingProgress =
      (bottomSheetSwipeTransition.currentState * 5f).inverseProgress()
    val hasBottomSheetExpandedThreshold = bottomSheetExpandingProgress > 1e-6f

    if (hasBottomSheetExpandedThreshold) {
      BottomSheetCollapsedContent(
        modifier = Modifier.graphicsLayer { alpha = bottomSheetExpandingProgress },
        feeds = state.allFeeds,
        selectedFeed = selectedFeed,
        onFeedSelected = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(feed)) }
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
        pinnedFeeds = state.pinnedFeeds,
        feeds = state.feeds,
        selectedFeed = state.selectedFeed,
        feedsSheetMode = feedsSheetMode,
        canPinFeeds = state.canPinFeeds,
        closeSheet = { feedsPresenter.dispatch(FeedsEvent.OnGoBackClicked) },
        onDeleteFeed = { feedsPresenter.dispatch(FeedsEvent.OnDeleteFeed(it)) },
        onFeedSelected = { feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(it)) },
        editFeeds = editFeeds,
        exitFeedsEdit = exitFeedsEdit,
        onFeedNameChanged = { newFeedName, feedLink ->
          feedsPresenter.dispatch(
            FeedsEvent.OnFeedNameUpdated(newFeedName = newFeedName, feedLink = feedLink)
          )
        },
        onFeedPinClick = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedPinClicked(feed)) }
      )
    }
  }
}

@Composable
private fun BottomSheetExpandedContent(
  pinnedFeeds: ImmutableList<Feed>,
  feeds: ImmutableList<Feed>,
  selectedFeed: Feed?,
  feedsSheetMode: FeedsSheetMode,
  canPinFeeds: Boolean,
  closeSheet: () -> Unit,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
  editFeeds: () -> Unit,
  exitFeedsEdit: () -> Unit,
  onFeedPinClick: (Feed) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().consumeWindowInsets(WindowInsets.statusBars).then(modifier),
    topBar = {
      CenterAlignedTopAppBar(
        modifier = Modifier.background(AppTheme.colorScheme.tintedBackground),
        title = { Text(LocalStrings.current.feeds) },
        navigationIcon = {
          when (feedsSheetMode) {
            Default,
            LinkEntry -> {
              IconButton(modifier = Modifier.padding(start = 4.dp), onClick = closeSheet) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
              }
            }
            Edit -> {
              IconButton(modifier = Modifier.padding(start = 4.dp), onClick = exitFeedsEdit) {
                Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = null)
              }
            }
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = AppTheme.colorScheme.onSurface,
            titleContentColor = AppTheme.colorScheme.onSurface,
            actionIconContentColor = AppTheme.colorScheme.onSurface
          )
      )
    },
    bottomBar = {
      FeedsSheetBottomBar(
        feedsSheetMode = feedsSheetMode,
        editFeeds = editFeeds,
      )
    },
    containerColor = AppTheme.colorScheme.tintedBackground
  ) { padding ->
    val layoutDirection = LocalLayoutDirection.current
    val focusManager = LocalFocusManager.current
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val keyboardState by keyboardVisibilityAsState()

    LaunchedEffect(keyboardState) {
      if (keyboardState == KeyboardState.Closed) {
        focusManager.clearFocus()
      }
    }

    LazyColumn(
      modifier =
        Modifier.fillMaxSize()
          .padding(bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp),
      contentPadding =
        PaddingValues(
          start = padding.calculateStartPadding(layoutDirection),
          top = padding.calculateTopPadding(),
          end = padding.calculateEndPadding(layoutDirection),
          bottom = padding.calculateBottomPadding() + 64.dp
        )
    ) {
      itemsIndexed(pinnedFeeds) { index, feed ->
        FeedListItem(
          feed = feed,
          selected = selectedFeed == feed,
          canShowDivider = index != pinnedFeeds.lastIndex,
          canPinFeeds = true,
          feedsSheetMode = feedsSheetMode,
          onDeleteFeed = onDeleteFeed,
          onFeedSelected = onFeedSelected,
          onFeedNameChanged = onFeedNameChanged,
          onFeedPinClick = onFeedPinClick
        )
      }

      if (pinnedFeeds.isNotEmpty() && feeds.isNotEmpty()) {
        item {
          Divider(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            color = AppTheme.colorScheme.tintedSurface
          )
        }
      }

      itemsIndexed(feeds) { index, feed ->
        FeedListItem(
          feed = feed,
          selected = selectedFeed == feed,
          canShowDivider = index != feeds.lastIndex,
          canPinFeeds = canPinFeeds,
          feedsSheetMode = feedsSheetMode,
          onDeleteFeed = onDeleteFeed,
          onFeedSelected = onFeedSelected,
          onFeedNameChanged = onFeedNameChanged,
          onFeedPinClick = onFeedPinClick
        )
      }
    }
  }
}

@Composable
private fun FeedsSheetBottomBar(
  feedsSheetMode: FeedsSheetMode,
  modifier: Modifier = Modifier,
  editFeeds: () -> Unit
) {
  val imeModifier =
    if (feedsSheetMode == LinkEntry) {
      Modifier.windowInsetsPadding(WindowInsets.ime)
    } else {
      Modifier
    }

  AnimatedVisibility(
    visible = feedsSheetMode != Edit,
    enter = slideInVertically { it },
    exit = slideOutVertically { it }
  ) {
    Box(
      imeModifier
        .background(AppTheme.colorScheme.tintedBackground)
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
        .then(modifier)
    ) {
      Divider(Modifier.align(Alignment.TopStart), color = AppTheme.colorScheme.tintedSurface)
      Box(Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        // Placeholder view with similar height of primary action button and input field
        // from the home screen
        Box(Modifier.requiredHeight(56.dp))
        when (feedsSheetMode) {
          Default,
          Edit -> {
            EditFeeds(editFeeds)
          }
          LinkEntry -> {
            // no-op
          }
        }
      }
    }
  }
}

@Composable
private fun BoxScope.EditFeeds(onClick: () -> Unit) {
  TextButton(
    modifier = Modifier.Companion.align(Alignment.CenterEnd).padding(end = 24.dp),
    onClick = onClick
  ) {
    Icon(imageVector = Icons.Filled.Edit, contentDescription = LocalStrings.current.editFeeds)
    Spacer(Modifier.width(12.dp))
    Text(
      text = LocalStrings.current.editFeeds,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.tintedForeground
    )
  }
}

@Composable
private fun BottomSheetCollapsedContent(
  feeds: ImmutableList<Feed>,
  selectedFeed: Feed?,
  onFeedSelected: (Feed) -> Unit,
  modifier: Modifier = Modifier
) {
  Box {
    LazyRow(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(start = 100.dp, end = 24.dp)
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

    Box(
      modifier =
        Modifier.requiredSize(100.dp)
          .background(
            Brush.horizontalGradient(
              colorStops =
                arrayOf(
                  0.7f to AppTheme.colorScheme.tintedBackground,
                  0.8f to AppTheme.colorScheme.tintedBackground.copy(alpha = 0.4f),
                  1f to Color.Transparent
                )
            )
          )
    )
  }
}
