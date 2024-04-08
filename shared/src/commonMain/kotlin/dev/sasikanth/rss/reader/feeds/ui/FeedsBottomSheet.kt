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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
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
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.inverse
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
internal fun FeedsBottomSheet(
  feedsPresenter: FeedsPresenter,
  bottomSheetSwipeTransition: Transition<Float>,
  closeSheet: () -> Unit,
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
        onFeedSelected = { feed -> feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(feed)) },
        onHomeSelected = { feedsPresenter.dispatch(FeedsEvent.OnHomeSelected) }
      )
    } else {
      BottomSheetExpandedContent(
        feeds = state.feedsInExpandedView.collectAsLazyPagingItems(),
        pinnedFeeds = state.pinnedFeeds.collectAsLazyPagingItems(),
        searchResults = state.feedsSearchResults.collectAsLazyPagingItems(),
        searchQuery = feedsPresenter.searchQuery,
        feedsSortOrder = state.feedsSortOrder,
        feedsViewMode = state.feedsViewMode,
        isPinnedSectionExpanded = state.isPinnedSectionExpanded,
        canShowUnreadPostsCount = state.canShowUnreadPostsCount,
        onSearchQueryChanged = { feedsPresenter.dispatch(FeedsEvent.SearchQueryChanged(it)) },
        onClearSearchQuery = { feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery) },
        onFeedInfoClick = { feedsPresenter.dispatch(FeedsEvent.OnFeedInfoClick(it.link)) },
        onFeedSelected = { feedsPresenter.dispatch(FeedsEvent.OnFeedSelected(it)) },
        onTogglePinnedSection = { feedsPresenter.dispatch(FeedsEvent.TogglePinnedSection) },
        onFeedsSortChanged = { feedsPresenter.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
        onChangeFeedsViewModeClick = {
          feedsPresenter.dispatch(FeedsEvent.OnChangeFeedsViewModeClick)
        },
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
  feeds: LazyPagingItems<Feed>,
  pinnedFeeds: LazyPagingItems<Feed>,
  searchResults: LazyPagingItems<Feed>,
  searchQuery: TextFieldValue,
  feedsSortOrder: FeedsOrderBy,
  feedsViewMode: FeedsViewMode,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  onSearchQueryChanged: (TextFieldValue) -> Unit,
  onClearSearchQuery: () -> Unit,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onTogglePinnedSection: () -> Unit,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onChangeFeedsViewModeClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = Modifier.fillMaxSize().consumeWindowInsets(WindowInsets.statusBars).then(modifier),
    topBar = {
      SearchBar(
        query = searchQuery,
        feedsViewMode = feedsViewMode,
        onQueryChange = { onSearchQueryChanged(it) },
        onClearClick = onClearSearchQuery,
        onChangeFeedsViewModeClick = onChangeFeedsViewModeClick
      )
    },
    containerColor = AppTheme.colorScheme.tintedBackground
  ) { padding ->
    val layoutDirection = LocalLayoutDirection.current
    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    val gridItemSpan =
      when (feedsViewMode) {
        FeedsViewMode.Grid -> GridItemSpan(1)
        FeedsViewMode.List -> GridItemSpan(2)
      }

    LazyVerticalGrid(
      modifier =
        Modifier.fillMaxSize()
          .padding(
            bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp,
            // doing this so that the dividers in sticky headers can go below the search bar and
            // not overlap with each other
            top = padding.calculateTopPadding() - 1.dp
          ),
      columns = GridCells.Fixed(2),
      contentPadding =
        PaddingValues(
          start = padding.calculateStartPadding(layoutDirection) + 20.dp,
          end = padding.calculateEndPadding(layoutDirection) + 20.dp,
          bottom = padding.calculateBottomPadding() + 64.dp,
          top = 12.dp
        ),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (
        searchResults.itemCount == 0 && searchQuery.text.length < MINIMUM_REQUIRED_SEARCH_CHARACTERS
      ) {
        pinnedFeeds(
          pinnedFeeds = pinnedFeeds,
          isPinnedSectionExpanded = isPinnedSectionExpanded,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          gridItemSpan = gridItemSpan,
          onTogglePinnedSection = onTogglePinnedSection,
          onFeedInfoClick = onFeedInfoClick,
          onFeedSelected = onFeedSelected
        )

        allFeeds(
          feeds = feeds,
          feedsSortOrder = feedsSortOrder,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          gridItemSpan = gridItemSpan,
          onFeedsSortChanged = onFeedsSortChanged,
          onFeedInfoClick = onFeedInfoClick,
          onFeedSelected = onFeedSelected
        )
      } else {
        feedSearchResults(
          searchResults = searchResults,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          gridItemSpan = gridItemSpan,
          onFeedInfoClick = onFeedInfoClick,
          onFeedSelected = onFeedSelected
        )
      }
    }
  }
}

private fun LazyGridScope.feedSearchResults(
  searchResults: LazyPagingItems<Feed>,
  canShowUnreadPostsCount: Boolean,
  gridItemSpan: GridItemSpan,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit
) {
  items(
    count = searchResults.itemCount,
    key = searchResults.itemKey { it.link },
    contentType = searchResults.itemContentType { it.link },
    span = { gridItemSpan }
  ) { index ->
    val feed = searchResults[index]

    if (feed != null) {
      FeedListItem(
        feed = feed,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        onFeedInfoClick = onFeedInfoClick,
        onFeedSelected = onFeedSelected,
      )
    }
  }
}

private fun LazyGridScope.allFeeds(
  feeds: LazyPagingItems<Feed>,
  feedsSortOrder: FeedsOrderBy,
  canShowUnreadPostsCount: Boolean,
  gridItemSpan: GridItemSpan,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit
) {
  if (feeds.itemCount > 0 && feeds.loadState.refresh != LoadState.Loading) {
    item(key = "AllFeedsHeader", span = { GridItemSpan(2) }) {
      AllFeedsHeader(
        feedsCount = feeds.itemCount,
        feedsSortOrder = feedsSortOrder,
        onFeedsSortChanged = onFeedsSortChanged
      )
    }

    items(
      count = feeds.itemCount,
      key = feeds.itemKey { it.link },
      contentType = { "FeedListItem" },
      span = { gridItemSpan }
    ) { index ->
      val feed = feeds[index]
      if (feed != null) {
        FeedListItem(
          feed = feed,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          onFeedInfoClick = onFeedInfoClick,
          onFeedSelected = onFeedSelected,
        )
      }
    }
  }
}

private fun LazyGridScope.pinnedFeeds(
  pinnedFeeds: LazyPagingItems<Feed>,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  gridItemSpan: GridItemSpan,
  onTogglePinnedSection: () -> Unit,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit
) {
  if (pinnedFeeds.itemCount > 0 && pinnedFeeds.loadState.refresh != LoadState.Loading) {
    item(key = "PinnedFeedsHeader", span = { GridItemSpan(2) }) {
      PinnedFeedsHeader(
        isPinnedSectionExpanded = isPinnedSectionExpanded,
        onToggleSection = onTogglePinnedSection
      )
    }

    if (isPinnedSectionExpanded) {
      items(
        count = pinnedFeeds.itemCount,
        key = pinnedFeeds.itemKey { "PinnedFeed:${it.link}" },
        contentType = { "FeedListItem" },
        span = { gridItemSpan }
      ) { index ->
        val feed = pinnedFeeds[index]
        if (feed != null) {
          FeedListItem(
            feed = feed,
            canShowUnreadPostsCount = canShowUnreadPostsCount,
            onFeedInfoClick = onFeedInfoClick,
            onFeedSelected = onFeedSelected,
          )
        }
      }
    }

    item(span = { GridItemSpan(2) }) {
      HorizontalDivider(
        modifier = Modifier.padding(top = 24.dp),
        color = AppTheme.colorScheme.tintedSurface
      )
    }
  }
}

@Composable
private fun AllFeedsHeader(
  feedsCount: Int,
  feedsSortOrder: FeedsOrderBy,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = Modifier.padding(start = 12.dp).then(modifier),
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
    modifier = Modifier.padding(start = 12.dp).then(modifier),
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
        Icons.Filled.ExpandLess
      } else {
        Icons.Filled.ExpandMore
      }

    IconButton(onClick = onToggleSection) {
      Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
    }
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
  feedsViewMode: FeedsViewMode,
  onQueryChange: (TextFieldValue) -> Unit,
  onClearClick: () -> Unit,
  onChangeFeedsViewModeClick: () -> Unit,
) {
  val keyboardState by keyboardVisibilityAsState()
  val focusManager = LocalFocusManager.current

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    MaterialTheme(colorScheme = darkColorScheme(primary = AppTheme.colorScheme.tintedForeground)) {
      OutlinedTextField(
        modifier =
          Modifier.weight(1f)
            .windowInsetsPadding(
              WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
            )
            .padding(vertical = 8.dp)
            .padding(start = 24.dp, end = 12.dp),
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

    IconButton(
      modifier = Modifier.padding(end = 20.dp),
      onClick = onChangeFeedsViewModeClick,
    ) {
      val icon =
        when (feedsViewMode) {
          FeedsViewMode.Grid -> Icons.Outlined.ViewAgenda
          FeedsViewMode.List -> Icons.Filled.GridView
        }

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }
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
