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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState

@Composable
internal fun BottomSheetExpandedContent(
  feeds: LazyPagingItems<Feed>,
  pinnedFeeds: LazyPagingItems<Feed>,
  searchResults: LazyPagingItems<Feed>,
  selectedSources: Set<Source>,
  feedGroups: LazyPagingItems<FeedGroup>,
  pinnedFeedGroups: LazyPagingItems<FeedGroup>,
  searchQuery: TextFieldValue,
  feedsSortOrder: FeedsOrderBy,
  feedsViewMode: FeedsViewMode,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  onSearchQueryChanged: (TextFieldValue) -> Unit,
  onClearSearchQuery: () -> Unit,
  onFeedClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
  onTogglePinnedSection: () -> Unit,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onChangeFeedsViewModeClick: () -> Unit,
  onCancelFeedsSelection: () -> Unit,
  onPinSelectedFeeds: () -> Unit,
  onUnPinSelectedFeeds: () -> Unit,
  onDeleteSelectedFeeds: () -> Unit,
  onCreateGroup: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var showNewGroupDialog by remember { mutableStateOf(false) }

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
    bottomBar = {
      Box(contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
          visible = !isInMultiSelectMode,
          enter = slideInVertically { it },
          exit = slideOutVertically { it }
        ) {
          BottomSheetExpandedBottomBar(
            onNewGroupClick = { showNewGroupDialog = true },
            onNewFeedClick = {
              // TODO: Open feed creation dialog/sheet/screen
            }
          )
        }

        AnimatedVisibility(
          visible = isInMultiSelectMode,
          enter = slideInVertically { it },
          exit = slideOutVertically { it }
        ) {
          ContextActionsBottomBar(onCancel = onCancelFeedsSelection) {
            val areSelectedFeedsPinned = selectedSources.all { it.pinnedAt != null }

            val label =
              if (areSelectedFeedsPinned) LocalStrings.current.actionUnpin
              else LocalStrings.current.actionPin

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Pin,
              label = label,
              onClick = {
                if (areSelectedFeedsPinned) {
                  onUnPinSelectedFeeds()
                } else {
                  onPinSelectedFeeds()
                }
              }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Delete,
              label = LocalStrings.current.actionDelete,
              onClick = onDeleteSelectedFeeds
            )
          }
        }
      }
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
          start = padding.calculateStartPadding(layoutDirection),
          end = padding.calculateEndPadding(layoutDirection),
          bottom = padding.calculateBottomPadding() + 64.dp,
          top = 8.dp
        ),
    ) {
      if (
        searchResults.itemCount == 0 &&
          searchQuery.text.length < Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
      ) {
        pinnedFeeds(
          pinnedFeeds = pinnedFeeds,
          pinnedFeedGroups = pinnedFeedGroups,
          selectedSources = selectedSources,
          isPinnedSectionExpanded = isPinnedSectionExpanded,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          isInMultiSelectMode = isInMultiSelectMode,
          gridItemSpan = gridItemSpan,
          onTogglePinnedSection = onTogglePinnedSection,
          onSourceClick = onFeedClick,
          onToggleSourceSelection = onToggleSourceSelection,
        )

        allFeeds(
          feeds = feeds,
          feedGroups = feedGroups,
          selectedSources = selectedSources,
          feedsSortOrder = feedsSortOrder,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          isInMultiSelectMode = isInMultiSelectMode,
          gridItemSpan = gridItemSpan,
          onFeedsSortChanged = onFeedsSortChanged,
          onSourceClick = onFeedClick,
          onToggleSourceSelection = onToggleSourceSelection
        )
      } else {
        feedSearchResults(
          searchResults = searchResults,
          selectedSources = selectedSources,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          isInMultiSelectMode = isInMultiSelectMode,
          gridItemSpan = gridItemSpan,
          onSourceClick = onFeedClick,
          onToggleSourceSelection = onToggleSourceSelection
        )
      }
    }
  }

  if (showNewGroupDialog) {
    CreateGroupDialog(onCreateGroup = onCreateGroup, onDismiss = { showNewGroupDialog = false })
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
            IconButton(onClick = onClearClick) {
              Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                tint = AppTheme.colorScheme.tintedForeground
              )
            }
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

    Spacer(Modifier.requiredWidth(20.dp))
  }
}

private fun LazyGridScope.feedSearchResults(
  searchResults: LazyPagingItems<Feed>,
  selectedSources: Set<Source>,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  gridItemSpan: GridItemSpan,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
) {
  items(
    count = searchResults.itemCount,
    key = searchResults.itemKey { "SearchResult:${it.id}" },
    contentType = { "FeedListItem" },
    span = { gridItemSpan }
  ) { index ->
    val feed = searchResults[index]
    val startPadding = startPaddingOfFeedListItem(gridItemSpan, index)
    val endPadding = endPaddingOfFeedListItem(gridItemSpan, index)
    val topPadding = topPaddingOfFeedListItem(gridItemSpan, index)
    val bottomPadding = bottomPaddingOfFeedListItem(index, searchResults.itemCount)

    if (feed != null) {
      FeedListItem(
        feed = feed,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        isInMultiSelectMode = isInMultiSelectMode,
        isFeedSelected = selectedSources.contains(feed),
        onFeedClick = onSourceClick,
        onFeedSelected = onToggleSourceSelection,
        modifier =
          Modifier.padding(
            start = startPadding,
            top = topPadding,
            end = endPadding,
            bottom = bottomPadding
          )
      )
    }
  }
}

private fun LazyGridScope.allFeeds(
  feeds: LazyPagingItems<Feed>,
  feedGroups: LazyPagingItems<FeedGroup>,
  selectedSources: Set<Source>,
  feedsSortOrder: FeedsOrderBy,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  gridItemSpan: GridItemSpan,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit
) {
  if (feeds.itemCount > 0) {
    item(key = "AllFeedsHeader", span = { GridItemSpan(2) }) {
      AllFeedsHeader(
        feedsCount = feeds.itemCount,
        feedsSortOrder = feedsSortOrder,
        onFeedsSortChanged = onFeedsSortChanged
      )
    }

    if (feedGroups.itemCount > 0) {
      val feedGroupGridItemSpan = GridItemSpan(1)

      items(
        count = feedGroups.itemCount,
        key = feedGroups.itemKey { it.id },
        contentType = { "FeedGroupItem" },
        span = { feedGroupGridItemSpan }
      ) { index ->
        val feedGroup = feedGroups[index]
        if (feedGroup != null) {
          val startPadding = startPaddingOfFeedListItem(feedGroupGridItemSpan, index)
          val endPadding = endPaddingOfFeedListItem(feedGroupGridItemSpan, index)
          val topPadding = topPaddingOfFeedListItem(feedGroupGridItemSpan, index)
          val bottomPadding = bottomPaddingOfFeedListItem(index, feedGroups.itemCount)

          FeedGroupItem(
            modifier =
              Modifier.padding(
                start = startPadding,
                top = topPadding,
                end = endPadding,
                bottom = bottomPadding
              ),
            feedGroup = feedGroup,
            isInMultiSelectMode = isInMultiSelectMode,
            selected = selectedSources.contains(feedGroup),
            onFeedGroupSelected = onToggleSourceSelection,
            onFeedGroupClick = onSourceClick
          )
        }
      }

      item(span = { GridItemSpan(2) }) { Spacer(Modifier.requiredHeight(40.dp)) }
    }

    items(
      count = feeds.itemCount,
      key = feeds.itemKey { it.id },
      contentType = { "FeedListItem" },
      span = { gridItemSpan }
    ) { index ->
      val feed = feeds[index]
      val startPadding = startPaddingOfFeedListItem(gridItemSpan, index)
      val endPadding = endPaddingOfFeedListItem(gridItemSpan, index)
      val topPadding = topPaddingOfFeedListItem(gridItemSpan, index)
      val bottomPadding = bottomPaddingOfFeedListItem(index, feeds.itemCount)

      if (feed != null) {
        FeedListItem(
          feed = feed,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          isInMultiSelectMode = isInMultiSelectMode,
          isFeedSelected = selectedSources.contains(feed),
          onFeedClick = onSourceClick,
          onFeedSelected = onToggleSourceSelection,
          modifier =
            Modifier.padding(
              start = startPadding,
              top = topPadding,
              end = endPadding,
              bottom = bottomPadding
            )
        )
      }
    }
  }
}

private fun LazyGridScope.pinnedFeeds(
  pinnedFeeds: LazyPagingItems<Feed>,
  pinnedFeedGroups: LazyPagingItems<FeedGroup>,
  selectedSources: Set<Source>,
  isPinnedSectionExpanded: Boolean,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  gridItemSpan: GridItemSpan,
  onTogglePinnedSection: () -> Unit,
  onSourceClick: (Source) -> Unit,
  onToggleSourceSelection: (Source) -> Unit,
) {
  if (pinnedFeeds.itemCount > 0) {
    item(key = "PinnedFeedsHeader", span = { GridItemSpan(2) }) {
      PinnedFeedsHeader(
        isPinnedSectionExpanded = isPinnedSectionExpanded,
        onToggleSection = onTogglePinnedSection
      )
    }

    if (isPinnedSectionExpanded) {

      items(
        count = pinnedFeedGroups.itemCount,
        key = pinnedFeedGroups.itemKey { "PinnedFeedGroup:${it.id}" },
        contentType = { "FeedGroupItem" },
        span = { gridItemSpan }
      ) { index ->
        val feedGroup = pinnedFeedGroups[index]
        if (feedGroup != null) {
          val startPadding = startPaddingOfFeedListItem(gridItemSpan, index)
          val endPadding = endPaddingOfFeedListItem(gridItemSpan, index)
          val topPadding = topPaddingOfFeedListItem(gridItemSpan, index)
          val bottomPadding = bottomPaddingOfFeedListItem(index, pinnedFeedGroups.itemCount)

          FeedGroupItem(
            modifier =
              Modifier.padding(
                start = startPadding,
                top = topPadding,
                end = endPadding,
                bottom = bottomPadding
              ),
            feedGroup = feedGroup,
            isInMultiSelectMode = isInMultiSelectMode,
            selected = selectedSources.contains(feedGroup),
            onFeedGroupSelected = onToggleSourceSelection,
            onFeedGroupClick = onSourceClick
          )
        }
      }

      item(span = { GridItemSpan(2) }) { Box(Modifier.fillMaxWidth().requiredHeight(8.dp)) }

      items(
        count = pinnedFeeds.itemCount,
        key = pinnedFeeds.itemKey { "PinnedFeed:${it.id}" },
        contentType = { "FeedListItem" },
        span = { gridItemSpan }
      ) { index ->
        val feed = pinnedFeeds[index]
        val startPadding = startPaddingOfFeedListItem(gridItemSpan, index)
        val endPadding = endPaddingOfFeedListItem(gridItemSpan, index)
        val topPadding = topPaddingOfFeedListItem(gridItemSpan, index)
        val bottomPadding = bottomPaddingOfFeedListItem(index, pinnedFeeds.itemCount)

        if (feed != null) {
          FeedListItem(
            feed = feed,
            canShowUnreadPostsCount = canShowUnreadPostsCount,
            isInMultiSelectMode = isInMultiSelectMode,
            isFeedSelected = selectedSources.contains(feed),
            onFeedClick = onSourceClick,
            onFeedSelected = onToggleSourceSelection,
            modifier =
              Modifier.padding(
                start = startPadding,
                top = topPadding,
                end = endPadding,
                bottom = bottomPadding
              ),
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

private fun bottomPaddingOfFeedListItem(index: Int, itemCount: Int) =
  when {
    index < itemCount -> 8.dp
    else -> 0.dp
  }

private fun topPaddingOfFeedListItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 && index > 0 -> 8.dp
    gridItemSpan.currentLineSpan == 1 && index > 1 -> 8.dp
    else -> 0.dp
  }

private fun endPaddingOfFeedListItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 || (gridItemSpan.currentLineSpan == 1 && index % 2 == 1) ->
      24.dp
    else -> 8.dp
  }

private fun startPaddingOfFeedListItem(gridItemSpan: GridItemSpan, index: Int) =
  when {
    gridItemSpan.currentLineSpan == 2 || (gridItemSpan.currentLineSpan == 1 && index % 2 == 0) ->
      24.dp
    else -> 8.dp
  }

@Composable
private fun AllFeedsHeader(
  feedsCount: Int,
  feedsSortOrder: FeedsOrderBy,
  onFeedsSortChanged: (FeedsOrderBy) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier =
      Modifier.padding(start = 32.dp, end = 20.dp).padding(vertical = 12.dp).then(modifier),
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

    Spacer(Modifier.requiredWidth(12.dp))

    Box {
      TextButton(onClick = { showSortDropdown = true }, shape = MaterialTheme.shapes.large) {
        val orderText =
          when (feedsSortOrder) {
            FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
            FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
            FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
            FeedsOrderBy.Pinned -> {
              throw IllegalStateException(
                "Cannot use the following feed sort order here: $feedsSortOrder"
              )
            }
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
        FeedsOrderBy.entries
          .filter { it != FeedsOrderBy.Pinned }
          .forEach { sortOrder ->
            val label =
              when (sortOrder) {
                FeedsOrderBy.Latest -> LocalStrings.current.feedsSortLatest
                FeedsOrderBy.Oldest -> LocalStrings.current.feedsSortOldest
                FeedsOrderBy.Alphabetical -> LocalStrings.current.feedsSortAlphabetical
                FeedsOrderBy.Pinned -> {
                  throw IllegalStateException(
                    "Cannot use the following feed sort order here: $feedsSortOrder"
                  )
                }
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
      Modifier.padding(start = 32.dp, end = 20.dp).padding(vertical = 12.dp).then(modifier),
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

    Spacer(Modifier.requiredWidth(12.dp))

    IconButton(onClick = onToggleSection) {
      Icon(imageVector = icon, contentDescription = null, tint = AppTheme.colorScheme.onSurface)
    }
  }
}
