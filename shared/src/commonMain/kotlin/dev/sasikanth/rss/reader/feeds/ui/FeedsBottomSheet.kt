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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.FeedsEffect
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Default
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Edit
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.LinkEntry
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.inverse
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
internal fun FeedsBottomSheet(
  feedsPresenter: FeedsPresenter,
  bottomSheetSwipeTransition: Transition<Float>,
  feedsSheetMode: FeedsSheetMode,
  closeSheet: () -> Unit,
  editFeeds: () -> Unit,
  exitFeedsEdit: () -> Unit,
  selectedFeedChanged: () -> Unit
) {
  val state by feedsPresenter.state.collectAsState()
  val selectedFeed = state.selectedFeed

  LaunchedEffect(Unit) {
    feedsPresenter.effects.collect { effect ->
      when (effect) {
        FeedsEffect.MinimizeSheet -> closeSheet()
        FeedsEffect.SelectedFeedChanged -> selectedFeedChanged()
      }
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    BottomSheetHandle(bottomSheetSwipeTransition)

    // Transforming the bottom sheet progress from 0-1 to 1-0,
    // since we want to control the alpha of the content as
    // users swipes the sheet up and down
    val bottomSheetExpandingProgress = (bottomSheetSwipeTransition.currentState * 5f).inverse()
    val hasBottomSheetExpandedThreshold = bottomSheetExpandingProgress > 1e-6f

    if (hasBottomSheetExpandedThreshold) {
      BottomSheetCollapsedContent(
        modifier = Modifier.graphicsLayer { alpha = bottomSheetExpandingProgress },
        feeds = state.feeds.collectAsLazyPagingItems(),
        selectedFeed = selectedFeed,
        canShowUnreadPostsCount = state.canShowUnreadPostsCount,
        onFeedSelected = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(feed)) },
        onHomeSelected = { feedsPresenter.dispatch(FeedsEvent.OnHomeSelected) }
      )
    } else {
      BottomSheetExpandedContent(
        feedsListItemTypes = state.feedsInExpandedMode.collectAsLazyPagingItems(),
        selectedFeed = state.selectedFeed,
        feedsSheetMode = feedsSheetMode,
        onFeedInfoClick = { feedsPresenter.dispatch(FeedsEvent.OnFeedInfoClick(it.link)) },
        onFeedSelected = { feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(it)) },
        editFeeds = editFeeds,
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
          }
      )
    }
  }
}

@Composable
private fun BottomSheetExpandedContent(
  feedsListItemTypes: LazyPagingItems<FeedsListItemType>,
  selectedFeed: Feed?,
  feedsSheetMode: FeedsSheetMode,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  editFeeds: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().consumeWindowInsets(WindowInsets.statusBars).then(modifier),
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

    Box {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier =
          Modifier.fillMaxSize()
            .padding(
              bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp,
              top = 4.dp
            ),
        contentPadding =
          PaddingValues(
            start = padding.calculateStartPadding(layoutDirection) + 24.dp,
            end = padding.calculateEndPadding(layoutDirection) + 24.dp,
            bottom = padding.calculateBottomPadding() + 64.dp
          ),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        for (index in 0 until feedsListItemTypes.itemCount) {
          when (val feedListItemType = feedsListItemTypes[index]) {
            is FeedsListItemType.FeedListItem -> {
              item {
                val feed = feedListItemType.feed
                FeedListItem(
                  feed = feed,
                  selected = selectedFeed?.link == feed.link,
                  onFeedInfoClick = onFeedInfoClick,
                  onFeedSelected = onFeedSelected,
                )
              }
            }
            FeedsListItemType.AllFeedsHeader -> {
              item(span = { GridItemSpan(2) }) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                  Text(
                    text = LocalStrings.current.feeds,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colorScheme.textEmphasisHigh
                  )
                  // TODO: Add number of feeds
                }
              }
            }
            FeedsListItemType.PinnedFeedsHeader -> {
              item(span = { GridItemSpan(2) }) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp)
                ) {
                  Text(
                    modifier = Modifier.weight(1f),
                    text = LocalStrings.current.pinnedFeeds,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colorScheme.textEmphasisHigh
                  )
                  // TODO: Add sort button
                }
              }
            }
            null -> {
              // no-op
            }
          }
        }
      }

      if (keyboardState == KeyboardState.Opened && feedsSheetMode == LinkEntry) {
        // Scrim when keyboard is open
        Box(
          Modifier.fillMaxSize()
            .padding(padding)
            .background(AppTheme.colorScheme.tintedBackground.copy(alpha = 0.8f))
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
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
        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
          // Only to prevent clicks from passing through. Not sure what's happening
        }
        .then(modifier)
    ) {
      HorizontalDivider(
        Modifier.align(Alignment.TopStart),
        color = AppTheme.colorScheme.tintedSurface
      )
      Box(Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 20.dp)) {
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
    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp),
    onClick = onClick,
    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 24.dp),
    shape = MaterialTheme.shapes.large
  ) {
    Icon(
      imageVector = Icons.Outlined.Edit,
      contentDescription = LocalStrings.current.editFeeds,
      tint = AppTheme.colorScheme.tintedForeground
    )
    Spacer(Modifier.width(12.dp))
    Text(
      text = LocalStrings.current.editFeeds,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.tintedForeground
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BottomSheetCollapsedContent(
  feeds: LazyPagingItems<Feed>,
  selectedFeed: Feed?,
  canShowUnreadPostsCount: Boolean,
  onFeedSelected: (Feed) -> Unit,
  onHomeSelected: () -> Unit,
  modifier: Modifier = Modifier
) {
  LazyRow(
    modifier = modifier.fillMaxWidth().padding(start = 20.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(end = 24.dp)
  ) {
    stickyHeader {
      val shadowColors =
        arrayOf(
          0.85f to AppTheme.colorScheme.tintedBackground,
          0.9f to AppTheme.colorScheme.tintedBackground.copy(alpha = 0.4f),
          1f to Color.Transparent
        )

      HomeBottomBarItem(
        selected = selectedFeed == null,
        onClick = onHomeSelected,
        modifier =
          Modifier.drawWithCache {
              onDrawBehind {
                val brush =
                  Brush.horizontalGradient(
                    colorStops = shadowColors,
                  )
                drawRect(
                  brush = brush,
                )
              }
            }
            .padding(end = 4.dp)
      )
    }

    items(feeds.itemCount) { index ->
      val feed = feeds[index]
      if (feed != null) {
        FeedBottomBarItem(
          text = feed.name.uppercase(),
          badgeCount = feed.numberOfUnreadPosts,
          iconUrl = feed.icon,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          selected = selectedFeed?.link == feed.link,
          onClick = { onFeedSelected(feed) }
        )
      }
    }
  }
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
  feedsSheetMode: FeedsSheetMode,
  onQueryChange: (TextFieldValue) -> Unit,
  onNavigationIconClick: () -> Unit,
  onClearClick: () -> Unit,
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(AppTheme.colorScheme.tintedBackground)
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
  ) {
    val background =
      when (feedsSheetMode) {
        Default,
        Edit -> AppTheme.colorScheme.tintedSurface
        LinkEntry -> AppTheme.colorScheme.tintedSurface.copy(alpha = 0.6f)
      }

    Box(
      modifier =
        Modifier.padding(all = 16.dp)
          .background(color = background, shape = RoundedCornerShape(16.dp))
          .padding(horizontal = 4.dp)
    ) {
      MaterialTheme(
        colorScheme = darkColorScheme(primary = AppTheme.colorScheme.tintedForeground)
      ) {
        TextField(
          modifier = Modifier.fillMaxWidth(),
          value = query.copy(selection = TextRange(query.text.length)),
          onValueChange = onQueryChange,
          placeholder = {
            val hintColor =
              when (feedsSheetMode) {
                Default,
                Edit -> AppTheme.colorScheme.textEmphasisHigh
                LinkEntry -> AppTheme.colorScheme.textEmphasisMed
              }
            Text(
              text = LocalStrings.current.feedsSearchHint,
              color = hintColor,
              style = MaterialTheme.typography.bodyLarge
            )
          },
          leadingIcon = {
            val icon =
              when (feedsSheetMode) {
                Default,
                LinkEntry -> Icons.Rounded.KeyboardArrowDown
                Edit -> TwineIcons.ArrowBack
              }
            IconButton(onClick = onNavigationIconClick) {
              Icon(icon, contentDescription = null, tint = AppTheme.colorScheme.tintedForeground)
            }
          },
          trailingIcon = {
            if (query.text.isNotBlank()) {
              ClearSearchQueryButton { onClearClick() }
            }
          },
          shape = RoundedCornerShape(16.dp),
          singleLine = true,
          textStyle = MaterialTheme.typography.bodyLarge,
          enabled = feedsSheetMode != LinkEntry,
          colors =
            TextFieldDefaults.colors(
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent,
              disabledContainerColor = Color.Transparent,
              focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
              disabledTextColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              focusedIndicatorColor = Color.Transparent,
              disabledIndicatorColor = Color.Transparent,
              errorIndicatorColor = Color.Transparent
            )
        )
      }
    }

    HorizontalDivider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
      color = AppTheme.colorScheme.tintedSurface
    )
  }
}

@Composable
private fun ClearSearchQueryButton(onClearClick: () -> Unit) {
  IconButton(onClick = onClearClick) {
    Icon(
      Icons.Rounded.Close,
      contentDescription = null,
      tint = AppTheme.colorScheme.tintedForeground
    )
  }
}
