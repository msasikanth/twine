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

package dev.sasikanth.rss.reader.feeds.ui.expanded

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.FeedsViewMode
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.feeds.ui.BottomSheetExpandedBottomBar
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import sh.calvin.reorderable.rememberReorderableLazyGridState

@Composable
internal fun BottomSheetExpandedContent(
  feedsPresenter: FeedsPresenter,
  modifier: Modifier = Modifier
) {
  val state by feedsPresenter.state.collectAsState()
  val searchQuery = feedsPresenter.searchQuery
  val feedsViewMode = state.feedsViewMode

  var showNewGroupDialog by remember { mutableStateOf(false) }

  if (state.showDeleteConfirmation) {
    DeleteConfirmationDialog(
      onDelete = { feedsPresenter.dispatch(FeedsEvent.DeleteSelectedSources) },
      dismiss = { feedsPresenter.dispatch(FeedsEvent.DismissDeleteConfirmation) }
    )
  }

  Scaffold(
    modifier = Modifier.fillMaxSize().consumeWindowInsets(WindowInsets.statusBars).then(modifier),
    topBar = {
      SearchBar(
        query = searchQuery,
        feedsViewMode = feedsViewMode,
        onQueryChange = { feedsPresenter.dispatch(FeedsEvent.SearchQueryChanged(it)) },
        onClearClick = { feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery) },
        onChangeFeedsViewModeClick = {
          feedsPresenter.dispatch(FeedsEvent.OnChangeFeedsViewModeClick)
        }
      )
    },
    bottomBar = {
      Box(contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
          visible = !state.isInMultiSelectMode,
          enter = slideInVertically { it },
          exit = slideOutVertically { it }
        ) {
          BottomSheetExpandedBottomBar(
            onNewGroupClick = { showNewGroupDialog = true },
            onNewFeedClick = { feedsPresenter.dispatch(FeedsEvent.OnNewFeedClicked) }
          )
        }

        AnimatedVisibility(
          visible = state.isInMultiSelectMode,
          enter = slideInVertically { it },
          exit = slideOutVertically { it }
        ) {
          val areGroupsSelected = state.selectedSources.any { it is FeedGroup }
          val tooltip: @Composable (() -> Unit)? =
            if (areGroupsSelected) {
              { Text(text = LocalStrings.current.actionGroupsTooltip) }
            } else {
              null
            }

          ContextActionsBottomBar(
            tooltip = tooltip,
            onCancel = { feedsPresenter.dispatch(FeedsEvent.CancelSourcesSelection) }
          ) {
            val areSelectedFeedsPinned = state.selectedSources.all { it.pinnedAt != null }

            val label =
              if (areSelectedFeedsPinned) LocalStrings.current.actionUnpin
              else LocalStrings.current.actionPin

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Pin,
              label = label,
              onClick = {
                if (areSelectedFeedsPinned) {
                  feedsPresenter.dispatch(FeedsEvent.UnPinSelectedSources)
                } else {
                  feedsPresenter.dispatch(FeedsEvent.PinSelectedSources)
                }
              }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.NewGroup,
              label = LocalStrings.current.actionAddTo,
              enabled = !areGroupsSelected,
              onClick = { feedsPresenter.dispatch(FeedsEvent.OnAddToGroupClicked) }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Delete,
              label = LocalStrings.current.actionDelete,
              onClick = { feedsPresenter.dispatch(FeedsEvent.DeleteSelectedSourcesClicked) }
            )

            if (state.selectedSources.size == 1) {
              ContextActionItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Filled.Edit,
                label = LocalStrings.current.edit,
                onClick = {
                  feedsPresenter.dispatch(
                    FeedsEvent.OnEditSourceClicked(state.selectedSources.first())
                  )
                }
              )
            }
          }
        }
      }
    },
    containerColor = AppTheme.colorScheme.tintedBackground
  ) { padding ->
    val allSources = state.sources.collectAsLazyPagingItems()
    val searchResults = state.feedsSearchResults.collectAsLazyPagingItems()

    var pinnedSources by remember(state.pinnedSources) { mutableStateOf(state.pinnedSources) }
    val isInSearchMode by derivedStateOf {
      searchQuery.text.length < Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
    }

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val gridItemSpan =
      when (state.feedsViewMode) {
        FeedsViewMode.Grid -> GridItemSpan(1)
        FeedsViewMode.List -> GridItemSpan(2)
      }
    val lazyGridState = rememberLazyGridState()
    val reorderableLazyGridState =
      rememberReorderableLazyGridState(lazyGridState) { from, to ->
        // We are doing this here instead of the presenter is to avoid
        // items blinking or having janky frames while the presenter updates happen and
        // state changes.
        pinnedSources =
          pinnedSources.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }

        feedsPresenter.dispatch(FeedsEvent.OnPinnedSourcePositionChanged(pinnedSources))
      }

    SourcesGrid(
      modifier =
        Modifier.padding(
          bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp,
          // doing this so that the dividers in sticky headers can go below the search bar and
          // not overlap with each other
          top = padding.calculateTopPadding() - 1.dp
        ),
      state = lazyGridState,
      pinnedSources = {
        pinnedSources(
          reorderableLazyGridState = reorderableLazyGridState,
          pinnedSources = pinnedSources,
          selectedSources = state.selectedSources,
          isPinnedSectionExpanded = state.isPinnedSectionExpanded,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          gridItemSpan = gridItemSpan,
          isInMultiSelectMode = state.isInMultiSelectMode,
          onTogglePinnedSection = { feedsPresenter.dispatch(FeedsEvent.TogglePinnedSection) },
          onSourceClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = {
            feedsPresenter.dispatch(FeedsEvent.OnToggleFeedSelection(it))
          }
        )
      },
      allSources = {
        allSources(
          numberOfFeeds = state.numberOfFeeds,
          numberOfFeedGroups = state.numberOfFeedGroups,
          sources = allSources,
          selectedSources = state.selectedSources,
          feedsSortOrder = state.feedsSortOrder,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          isInMultiSelectMode = state.isInMultiSelectMode,
          gridItemSpan = gridItemSpan,
          onFeedsSortChanged = { feedsPresenter.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
          onSourceClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = {
            feedsPresenter.dispatch(FeedsEvent.OnToggleFeedSelection(it))
          }
        )
      },
      searchResults = {
        sourcesSearchResults(
          searchResults = searchResults,
          selectedSources = state.selectedSources,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          isInMultiSelectMode = state.isInMultiSelectMode,
          gridItemSpan = gridItemSpan,
          onSourceClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = {
            feedsPresenter.dispatch(FeedsEvent.OnToggleFeedSelection(it))
          }
        )
      },
      isInSearchMode = isInSearchMode,
      padding = padding
    )

    if (showNewGroupDialog) {
      CreateGroupDialog(
        onCreateGroup = { feedsPresenter.dispatch(FeedsEvent.OnCreateGroup(it)) },
        onDismiss = { showNewGroupDialog = false }
      )
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
    MaterialTheme(
      colorScheme = MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
    ) {
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

@Composable
fun DeleteConfirmationDialog(
  onDelete: () -> Unit,
  dismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    modifier = modifier,
    onDismissRequest = dismiss,
    confirmButton = {
      TextButton(
        onClick = {
          onDelete()
          dismiss()
        },
        shape = MaterialTheme.shapes.large
      ) {
        Text(
          text = LocalStrings.current.delete,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    dismissButton = {
      TextButton(onClick = dismiss, shape = MaterialTheme.shapes.large) {
        Text(
          text = LocalStrings.current.buttonCancel,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }
    },
    title = {
      Text(text = LocalStrings.current.removeSources, color = AppTheme.colorScheme.textEmphasisMed)
    },
    text = {
      Text(
        text = LocalStrings.current.removeSourcesDesc,
        color = AppTheme.colorScheme.textEmphasisMed
      )
    },
    containerColor = AppTheme.colorScheme.tintedSurface,
    titleContentColor = AppTheme.colorScheme.onSurface,
    textContentColor = AppTheme.colorScheme.onSurface,
  )
}
