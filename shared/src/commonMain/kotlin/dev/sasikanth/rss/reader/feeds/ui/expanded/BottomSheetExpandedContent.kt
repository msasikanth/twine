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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.ui.BottomSheetExpandedBottomBar
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.rememberReorderableLazyListState
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.actionAddTo
import twine.shared.generated.resources.actionDelete
import twine.shared.generated.resources.actionGroupsTooltip
import twine.shared.generated.resources.actionPin
import twine.shared.generated.resources.actionUnpin
import twine.shared.generated.resources.buttonCancel
import twine.shared.generated.resources.delete
import twine.shared.generated.resources.edit
import twine.shared.generated.resources.feedsSearchHint
import twine.shared.generated.resources.removeSources
import twine.shared.generated.resources.removeSourcesDesc
import twine.shared.generated.resources.settings

@Composable
internal fun BottomSheetExpandedContent(
  viewModel: FeedsViewModel,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val searchQuery = viewModel.searchQuery

  LaunchedEffect(state.openPaywall, state.openAddFeedScreen) {
    when {
      state.openPaywall -> {
        openPaywall()
        viewModel.dispatch(FeedsEvent.MarkOpenPaywallDone)
      }
      state.openAddFeedScreen -> {
        openAddFeedScreen()
        viewModel.dispatch(FeedsEvent.MarkOpenAddFeedDone)
      }
    }
  }

  var showNewGroupDialog by remember { mutableStateOf(false) }

  if (state.showDeleteConfirmation) {
    DeleteConfirmationDialog(
      onDelete = { viewModel.dispatch(FeedsEvent.DeleteSelectedSources) },
      dismiss = { viewModel.dispatch(FeedsEvent.DismissDeleteConfirmation) }
    )
  }

  Scaffold(
    modifier = Modifier.consumeWindowInsets(WindowInsets.statusBars).then(modifier),
    topBar = {
      SearchBar(
        query = searchQuery,
        onQueryChange = { viewModel.dispatch(FeedsEvent.SearchQueryChanged(it)) },
        onClearClick = { viewModel.dispatch(FeedsEvent.ClearSearchQuery) },
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
            onNewFeedClick = { viewModel.dispatch(FeedsEvent.OnNewFeedClicked) }
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
              { Text(text = stringResource(Res.string.actionGroupsTooltip)) }
            } else {
              null
            }

          ContextActionsBottomBar(
            tooltip = tooltip,
            onCancel = { viewModel.dispatch(FeedsEvent.CancelSourcesSelection) }
          ) {
            val areSelectedFeedsPinned = state.selectedSources.all { it.pinnedAt != null }

            val pinActionLabel =
              if (areSelectedFeedsPinned) stringResource(Res.string.actionUnpin)
              else stringResource(Res.string.actionPin)

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Pin,
              label = pinActionLabel,
              onClick = {
                if (areSelectedFeedsPinned) {
                  viewModel.dispatch(FeedsEvent.UnPinSelectedSources)
                } else {
                  viewModel.dispatch(FeedsEvent.PinSelectedSources)
                }
              }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.NewGroup,
              label = stringResource(Res.string.actionAddTo),
              enabled = !areGroupsSelected,
              onClick = { openGroupSelectionSheet() }
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Delete,
              label = stringResource(Res.string.actionDelete),
              onClick = { viewModel.dispatch(FeedsEvent.DeleteSelectedSourcesClicked) }
            )

            if (state.selectedSources.size == 1) {
              val editIcon =
                if (state.selectedSources.first().sourceType == SourceType.FeedGroup) {
                  Icons.Filled.Edit
                } else {
                  Icons.Filled.Tune
                }
              val editLabel =
                if (state.selectedSources.first().sourceType == SourceType.FeedGroup) {
                  stringResource(Res.string.edit)
                } else {
                  stringResource(Res.string.settings)
                }

              ContextActionItem(
                modifier = Modifier.weight(1f),
                icon = editIcon,
                label = editLabel,
                onClick = {
                  val selectedSource = state.selectedSources.first()
                  when (selectedSource.sourceType) {
                    SourceType.Feed -> {
                      openFeedInfoSheet(selectedSource.id)
                    }
                    SourceType.FeedGroup -> {
                      openGroupScreen(selectedSource.id)
                    }
                  }
                }
              )
            }
          }
        }
      }
    },
    containerColor = Color.Transparent,
  ) { padding ->
    val allSources = state.sources.collectAsLazyPagingItems()
    val searchResults = state.feedsSearchResults.collectAsLazyPagingItems()

    var pinnedSources by remember(state.pinnedSources) { mutableStateOf(state.pinnedSources) }
    val isInSearchMode by derivedStateOf {
      searchQuery.text.length >= Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
    }

    val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val lazyListState = rememberLazyListState()
    val reorderableLazyGridState =
      rememberReorderableLazyListState(lazyListState) { from, to ->
        // We are doing this here instead of the presenter is to avoid
        // items blinking or having janky frames while the presenter updates happen and
        // state changes.
        pinnedSources =
          pinnedSources.toMutableList().apply { add(to.index - 1, removeAt(from.index - 1)) }

        viewModel.dispatch(FeedsEvent.OnPinnedSourcePositionChanged(pinnedSources))
      }

    SourcesList(
      modifier =
        Modifier.padding(
          bottom = if (imeBottomPadding > 0.dp) imeBottomPadding + 16.dp else 0.dp,
          // doing this so that the dividers in sticky headers can go below the search bar and
          // not overlap with each other
          top = padding.calculateTopPadding()
        ),
      state = lazyListState,
      pinnedSources = {
        pinnedSources(
          reorderableLazyListState = reorderableLazyGridState,
          pinnedSources = pinnedSources,
          selectedSources = state.selectedSources,
          isPinnedSectionExpanded = state.isPinnedSectionExpanded,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          isInMultiSelectMode = state.isInMultiSelectMode,
          onTogglePinnedSection = { viewModel.dispatch(FeedsEvent.TogglePinnedSection) },
          onSourceClick = { viewModel.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = { viewModel.dispatch(FeedsEvent.OnToggleFeedSelection(it)) }
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
          onFeedsSortChanged = { viewModel.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
          onSourceClick = { viewModel.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = { viewModel.dispatch(FeedsEvent.OnToggleFeedSelection(it)) }
        )
      },
      searchResults = {
        sourcesSearchResults(
          searchResults = searchResults,
          selectedSources = state.selectedSources,
          canShowUnreadPostsCount = state.canShowUnreadPostsCount,
          isInMultiSelectMode = state.isInMultiSelectMode,
          onSourceClick = { viewModel.dispatch(FeedsEvent.OnSourceClick(it)) },
          onToggleSourceSelection = { viewModel.dispatch(FeedsEvent.OnToggleFeedSelection(it)) }
        )
      },
      isInSearchMode = isInSearchMode,
      padding = padding
    )

    if (showNewGroupDialog) {
      CreateGroupDialog(
        onCreateGroup = { viewModel.dispatch(FeedsEvent.OnCreateGroup(it)) },
        onDismiss = { showNewGroupDialog = false }
      )
    }
  }
}

@Composable
private fun SearchBar(
  query: TextFieldValue,
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

  MaterialTheme(
    colorScheme = MaterialTheme.colorScheme.copy(primary = AppTheme.colorScheme.tintedForeground)
  ) {
    OutlinedTextField(
      modifier =
        Modifier.fillMaxWidth()
          .windowInsetsPadding(
            WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
          )
          .padding(vertical = 8.dp)
          .padding(start = 24.dp, end = 24.dp),
      value = query.copy(selection = TextRange(query.text.length)),
      onValueChange = onQueryChange,
      placeholder = {
        Text(
          text = stringResource(Res.string.feedsSearchHint),
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
          focusedBorderColor = AppTheme.colorScheme.primary,
          unfocusedBorderColor = AppTheme.colorScheme.tintedHighlight,
          disabledBorderColor = AppTheme.colorScheme.tintedHighlight,
          focusedTextColor = AppTheme.colorScheme.textEmphasisHigh,
          disabledTextColor = Color.Transparent,
        )
    )
  }
}

@Composable
fun DeleteConfirmationDialog(
  onDelete: () -> Unit,
  dismiss: () -> Unit,
  modifier: Modifier = Modifier,
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
          text = stringResource(Res.string.delete),
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.error
        )
      }
    },
    dismissButton = {
      TextButton(onClick = dismiss, shape = MaterialTheme.shapes.large) {
        Text(
          text = stringResource(Res.string.buttonCancel),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed
        )
      }
    },
    title = {
      Text(
        text = stringResource(Res.string.removeSources),
        color = AppTheme.colorScheme.textEmphasisMed
      )
    },
    text = {
      Text(
        text = stringResource(Res.string.removeSourcesDesc),
        color = AppTheme.colorScheme.textEmphasisMed
      )
    },
    containerColor = AppTheme.colorScheme.tintedSurface,
    titleContentColor = AppTheme.colorScheme.onSurface,
    textContentColor = AppTheme.colorScheme.onSurface,
  )
}
