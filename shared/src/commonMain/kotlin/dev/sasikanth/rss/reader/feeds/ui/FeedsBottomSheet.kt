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
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.FeedsEffect
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Default
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Edit
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.LinkEntry
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
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
        feeds = state.feedsInBottomBar.collectAsLazyPagingItems(),
        selectedFeed = selectedFeed,
        canShowUnreadPostsCount = state.canShowUnreadPostsCount,
        onFeedSelected = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(feed)) }
      )
    } else {
      BottomSheetExpandedContent(
        feedsListItemTypes = state.feedsInExpandedView.collectAsLazyPagingItems(),
        pinnedFeedsListItemTypes = state.pinnedFeeds.collectAsLazyPagingItems(),
        feedsSheetMode = feedsSheetMode,
        searchQuery = feedsPresenter.searchQuery,
        onSearchQueryChanged = { feedsPresenter.dispatch(FeedsEvent.SearchQueryChanged(it)) },
        onClearSearchQuery = { feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery) },
        onFeedInfoClick = { feedsPresenter.dispatch(FeedsEvent.OnFeedInfoClick(it.link)) },
        onFeedSelected = { feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(it)) },
        editFeeds = editFeeds,
        onTogglePinnedSection = { feedsPresenter.dispatch(FeedsEvent.TogglePinnedSection) },
        onFeedsSortChanged = { feedsPresenter.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
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
  pinnedFeedsListItemTypes: LazyPagingItems<PinnedFeedsListItemType>,
  feedsSheetMode: FeedsSheetMode,
  searchQuery: TextFieldValue,
  onSearchQueryChanged: (TextFieldValue) -> Unit,
  onClearSearchQuery: () -> Unit,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  editFeeds: () -> Unit,
  onTogglePinnedSection: () -> Unit,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().consumeWindowInsets(WindowInsets.statusBars).then(modifier),
    topBar = {
      SearchBar(
        query = searchQuery,
        feedsSheetMode = feedsSheetMode,
        onQueryChange = { onSearchQueryChanged(it) },
        onClearClick = onClearSearchQuery
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

    Box {
      LazyColumn(
        modifier =
          Modifier.fillMaxSize()
            .padding(
              bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp,
              // doing this so that the dividers in sticky headers can go below the search bar and
              // not overlap with each other
              top = padding.calculateTopPadding() - 1.dp
            ),
        contentPadding =
          PaddingValues(
            start = padding.calculateStartPadding(layoutDirection),
            end = padding.calculateEndPadding(layoutDirection),
            bottom = padding.calculateBottomPadding() + 64.dp
          )
      ) {
        // Pinned feeds
        if (pinnedFeedsListItemTypes.itemCount > 0) {
          items(
            count = pinnedFeedsListItemTypes.itemCount,
            key = pinnedFeedsListItemTypes.itemKey { it.key },
            contentType = pinnedFeedsListItemTypes.itemContentType { it.contentType }
          ) { index ->
            when (val pinnedFeedsListItemType = pinnedFeedsListItemTypes[index]) {
              is PinnedFeedsListItemType.PinnedFeedListItem -> {
                val feed = pinnedFeedsListItemType.feed
                val listItemTopPadding =
                  if (index > 1) {
                    8.dp
                  } else {
                    0.dp
                  }

                val listItemBottomPadding =
                  if (index != pinnedFeedsListItemTypes.itemCount + 1) {
                    8.dp
                  } else {
                    0.dp
                  }

                FeedListItem(
                  modifier =
                    Modifier.padding(top = listItemTopPadding, bottom = listItemBottomPadding),
                  feed = feed,
                  onFeedInfoClick = onFeedInfoClick,
                  onFeedSelected = onFeedSelected,
                )
              }
              is PinnedFeedsListItemType.PinnedFeedsHeader -> {
                PinnedFeedsHeader(
                  isPinnedSectionExpanded = pinnedFeedsListItemType.isExpanded,
                  onToggleSection = onTogglePinnedSection
                )
              }
              else -> {
                // no-op
              }
            }
          }

          item {
            HorizontalDivider(
              modifier = Modifier.padding(top = 24.dp),
              color = AppTheme.colorScheme.tintedSurface
            )
          }
        }

        // All feeds
        items(
          count = feedsListItemTypes.itemCount,
          key = feedsListItemTypes.itemKey { it.key },
          contentType = feedsListItemTypes.itemContentType { it.contentType }
        ) { index ->
          when (val feedListItemType = feedsListItemTypes[index]) {
            is FeedsListItemType.FeedListItem -> {
              val feed = feedListItemType.feed
              val listItemTopPadding =
                if (index != 0) {
                  8.dp
                } else {
                  0.dp
                }

              val listItemBottomPadding =
                if (index != feedsListItemTypes.itemCount) {
                  8.dp
                } else {
                  0.dp
                }

              FeedListItem(
                modifier =
                  Modifier.padding(top = listItemTopPadding, bottom = listItemBottomPadding),
                feed = feed,
                onFeedInfoClick = onFeedInfoClick,
                onFeedSelected = onFeedSelected,
              )
            }
            is FeedsListItemType.AllFeedsHeader -> {
              AllFeedsHeader(
                feedsCount = feedListItemType.feedsCount,
                feedsSortOrder = feedListItemType.feedsSortOrder,
                onFeedsSortChanged = onFeedsSortChanged
              )
            }
            else -> {
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
private fun AllFeedsHeader(
  feedsCount: Long,
  feedsSortOrder: FeedsOrderBy,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier =
      Modifier.padding(vertical = 12.dp).padding(start = 32.dp, end = 20.dp).then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    var showSortDropdown by remember { mutableStateOf(false) }

    Text(
      text = LocalStrings.current.allFeeds,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    Spacer(Modifier.requiredWidth(8.dp))

    Text(
      modifier = Modifier.weight(1f),
      text = feedsCount.toString(),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.tintedForeground,
    )

    Box {
      TextButton(
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
        onClick = { showSortDropdown = true },
        shape = MaterialTheme.shapes.large
      ) {
        val orderText =
          when (feedsSortOrder) {
            FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
            FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
            FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
          }

        Text(
          text = orderText,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground
        )

        Spacer(Modifier.width(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = LocalStrings.current.editFeeds,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }

      DropdownMenu(
        modifier = Modifier.requiredWidth(132.dp),
        expanded = showSortDropdown,
        onDismissRequest = { showSortDropdown = false }
      ) {
        FeedsOrderBy.entries.forEach { sortOrder ->
          val label =
            when (sortOrder) {
              FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
              FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
              FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
            }

          val color =
            if (feedsSortOrder == sortOrder) {
              AppTheme.colorScheme.tintedSurface
            } else {
              Color.Unspecified
            }
          val labelColor =
            if (feedsSortOrder == sortOrder) {
              AppTheme.colorScheme.onSurface
            } else {
              AppTheme.colorScheme.textEmphasisHigh
            }

          DropdownMenuItem(
            modifier = Modifier.background(color),
            onClick = {
              onFeedsSortChanged(sortOrder)
              showSortDropdown = false
            },
            text = { Text(label, color = labelColor) }
          )
        }
      }
    }
  }
}

@Composable
private fun PinnedFeedsHeader(
  isPinnedSectionExpanded: Boolean,
  modifier: Modifier = Modifier,
  onToggleSection: () -> Unit
) {
  Row(
    modifier =
      Modifier.clickable { onToggleSection() }
        .padding(vertical = 12.dp)
        .padding(start = 32.dp, end = 20.dp)
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = LocalStrings.current.pinnedFeeds,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    val icon =
      if (isPinnedSectionExpanded) {
        Icons.Filled.ExpandMore
      } else {
        Icons.Filled.ExpandLess
      }

    IconButton(onClick = onToggleSection) {
      Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
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

@Composable
private fun BottomSheetCollapsedContent(
  feeds: LazyPagingItems<Feed>,
  selectedFeed: Feed?,
  canShowUnreadPostsCount: Boolean,
  onFeedSelected: (Feed) -> Unit,
  modifier: Modifier = Modifier
) {
  Box {
    LazyRow(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(start = 100.dp, end = 24.dp)
    ) {
      items(feeds.itemCount) { index ->
        val feed = feeds[index]
        if (feed != null) {
          BottomSheetItem(
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

@Composable
private fun SearchBar(
  query: TextFieldValue,
  feedsSheetMode: FeedsSheetMode,
  onQueryChange: (TextFieldValue) -> Unit,
  onClearClick: () -> Unit,
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  MaterialTheme(colorScheme = darkColorScheme(primary = AppTheme.colorScheme.tintedForeground)) {
    OutlinedTextField(
      modifier =
        Modifier.fillMaxWidth()
          .windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          )
          .padding(horizontal = 24.dp, vertical = 8.dp),
      value = query.copy(selection = TextRange(query.text.length)),
      onValueChange = onQueryChange,
      placeholder = {
        Text(
          text = LocalStrings.current.feedsSearchHint,
          color = AppTheme.colorScheme.tintedForeground,
          style = MaterialTheme.typography.bodyLarge
        )
      },
      leadingIcon = {
        Icon(
          imageVector = Icons.Rounded.Search,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
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
        OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AppTheme.colorScheme.tintedHighlight,
          unfocusedBorderColor = AppTheme.colorScheme.tintedHighlight,
          disabledBorderColor = AppTheme.colorScheme.tintedHighlight,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
          disabledTextColor = Color.Transparent,
        )
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
