/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.main.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsState
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.feeds.ui.DeleteConfirmationDialog
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.feeds.ui.common.AllFeedsHeader
import dev.sasikanth.rss.reader.feeds.ui.common.allSources
import dev.sasikanth.rss.reader.feeds.ui.common.pinnedSources
import dev.sasikanth.rss.reader.feeds.ui.common.sourcesSearchResults
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.Edit
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.Tune
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.actionAddTo
import twine.shared.generated.resources.actionDelete
import twine.shared.generated.resources.actionGroupsTooltip
import twine.shared.generated.resources.actionPin
import twine.shared.generated.resources.actionUnpin
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.edit
import twine.shared.generated.resources.feedsLetsStart
import twine.shared.generated.resources.feedsSearchHint

private val NavigationItemShape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)
private val SelectedSourceColorShape = RoundedCornerShape(12.dp)
private val SourceIconShape = RoundedCornerShape(8.dp)
private val SearchBarShape = RoundedCornerShape(50)

@Composable
internal fun NavigationDrawerContent(
  feedsViewModel: FeedsViewModel,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  closeDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  expanded: Boolean = true,
  showCloseIcon: Boolean = true,
  dismissOnSelection: Boolean = true,
) {
  val state by feedsViewModel.state.collectAsStateWithLifecycle()
  val focusManager = LocalFocusManager.current
  val closeDrawerWithFocusClear = {
    focusManager.clearFocus()
    closeDrawer()
  }

  LaunchedEffect(expanded) { focusManager.clearFocus() }

  LaunchedEffect(state.openPaywall, state.openAddFeedScreen) {
    when {
      state.openPaywall -> {
        openPaywall()
        feedsViewModel.dispatch(FeedsEvent.MarkOpenPaywallDone)
      }
      state.openAddFeedScreen -> {
        openAddFeedScreen()
        feedsViewModel.dispatch(FeedsEvent.MarkOpenAddFeedDone)
      }
    }
  }

  NavigationDrawerContent(
    state = state,
    searchQuery = feedsViewModel.searchQuery,
    selectedDestination = selectedDestination,
    onDestinationSelected = onDestinationSelected,
    dispatch = feedsViewModel::dispatch,
    openFeedInfoSheet = openFeedInfoSheet,
    openGroupScreen = openGroupScreen,
    openGroupSelectionSheet = openGroupSelectionSheet,
    closeDrawer = closeDrawerWithFocusClear,
    modifier = modifier,
    expanded = expanded,
    showCloseIcon = showCloseIcon,
    dismissOnSelection = dismissOnSelection,
    scrollIndex = feedsViewModel.drawerListScrollIndex,
    scrollOffset = feedsViewModel.drawerListScrollOffset,
    onScrollPositionChanged = { index, offset ->
      feedsViewModel.drawerListScrollIndex = index
      feedsViewModel.drawerListScrollOffset = offset
    },
  )
}

@Composable
internal fun NavigationDrawerContent(
  state: FeedsState,
  searchQuery: TextFieldValue,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  dispatch: (FeedsEvent) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  closeDrawer: () -> Unit,
  modifier: Modifier = Modifier,
  expanded: Boolean = true,
  showCloseIcon: Boolean = true,
  dismissOnSelection: Boolean = true,
  scrollIndex: Int = 0,
  scrollOffset: Int = 0,
  onScrollPositionChanged: (index: Int, offset: Int) -> Unit = { _, _ -> },
) {
  Crossfade(targetState = expanded, modifier = modifier) { isExpanded ->
    if (isExpanded) {
      ExpandedDrawerContent(
        state = state,
        searchQuery = searchQuery,
        selectedDestination = selectedDestination,
        onDestinationSelected = onDestinationSelected,
        dispatch = dispatch,
        openFeedInfoSheet = openFeedInfoSheet,
        openGroupScreen = openGroupScreen,
        openGroupSelectionSheet = openGroupSelectionSheet,
        closeDrawer = closeDrawer,
        showCloseIcon = showCloseIcon,
        dismissOnSelection = dismissOnSelection,
        scrollIndex = scrollIndex,
        scrollOffset = scrollOffset,
        onScrollPositionChanged = onScrollPositionChanged,
      )
    } else {
      CollapsedDrawerContent(
        state = state,
        selectedDestination = selectedDestination,
        onDestinationSelected = onDestinationSelected,
        dispatch = dispatch,
        closeDrawer = closeDrawer,
        dismissOnSelection = dismissOnSelection,
      )
    }
  }
}

@Composable
private fun ExpandedDrawerContent(
  state: FeedsState,
  searchQuery: TextFieldValue,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  dispatch: (FeedsEvent) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  closeDrawer: () -> Unit,
  showCloseIcon: Boolean,
  dismissOnSelection: Boolean,
  scrollIndex: Int,
  scrollOffset: Int,
  onScrollPositionChanged: (index: Int, offset: Int) -> Unit,
) {
  var showNewGroupDialog by remember { mutableStateOf(false) }

  if (state.showDeleteConfirmation) {
    DeleteConfirmationDialog(
      onDelete = { dispatch(FeedsEvent.DeleteSelectedSources) },
      dismiss = { dispatch(FeedsEvent.DismissDeleteConfirmation) },
    )
  }

  if (showNewGroupDialog) {
    CreateGroupDialog(
      onCreateGroup = { dispatch(FeedsEvent.OnCreateGroup(it)) },
      onDismiss = { showNewGroupDialog = false },
    )
  }

  val colorScheme = AppTheme.colorScheme

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .fillMaxHeight()
        .drawBehind { drawRect(colorScheme.backdrop) }
        .windowInsetsPadding(WindowInsets.systemBars)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val allSources = state.sources.collectAsLazyPagingItems()
      val isInSearchMode = searchQuery.text.length >= Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
      val searchResults =
        if (isInSearchMode) {
          state.feedsSearchResults.collectAsLazyPagingItems()
        } else {
          null
        }
      val selectedSourceIds =
        remember(state.selectedSources) { state.selectedSources.mapTo(mutableSetOf()) { it.id } }

      val lazyListState =
        rememberLazyListState(
          initialFirstVisibleItemIndex = scrollIndex,
          initialFirstVisibleItemScrollOffset = scrollOffset,
        )
      val translucentStyle = LocalTranslucentStyles.current
      val haptic = LocalHapticFeedback.current
      val currentScrollIndex by rememberUpdatedState(scrollIndex)
      val currentScrollOffset by rememberUpdatedState(scrollOffset)
      val currentIsInSearchMode by rememberUpdatedState(isInSearchMode)
      val currentOnScrollPositionChanged by rememberUpdatedState(onScrollPositionChanged)
      var pendingRestoreScroll by remember {
        mutableStateOf(
          if (scrollIndex > 0 || scrollOffset > 0) scrollIndex to scrollOffset else null
        )
      }

      LaunchedEffect(lazyListState, allSources) {
        snapshotFlow {
            DrawerRestoreSnapshot(
              itemCount = allSources.itemCount,
              refreshState = allSources.loadState.refresh,
              isInSearchMode = currentIsInSearchMode,
            )
          }
          .distinctUntilChanged()
          .collect { snapshot ->
            if (snapshot.isInSearchMode) {
              if (pendingRestoreScroll == null) {
                val index = lazyListState.firstVisibleItemIndex
                val offset = lazyListState.firstVisibleItemScrollOffset
                currentOnScrollPositionChanged(index, offset)
                pendingRestoreScroll = index to offset
              }
            } else if (snapshot.itemCount > 0) {
              val savedScroll = pendingRestoreScroll
              if (savedScroll != null) {
                pendingRestoreScroll = null
                if (
                  lazyListState.firstVisibleItemIndex != savedScroll.first ||
                    lazyListState.firstVisibleItemScrollOffset != savedScroll.second
                ) {
                  lazyListState.scrollToItem(savedScroll.first, savedScroll.second)
                  return@collect
                }
              }
              currentOnScrollPositionChanged(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
              )
            } else if (snapshot.refreshState is LoadState.Loading) {
              if (
                pendingRestoreScroll == null && (currentScrollIndex > 0 || currentScrollOffset > 0)
              ) {
                pendingRestoreScroll = currentScrollIndex to currentScrollOffset
              }
            } else if (snapshot.refreshState is LoadState.NotLoading) {
              pendingRestoreScroll = null
              currentOnScrollPositionChanged(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
              )
            }
          }
      }

      LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }
          .distinctUntilChanged()
          .collect { isScrolling ->
            if (!isScrolling && !currentIsInSearchMode && pendingRestoreScroll == null) {
              currentOnScrollPositionChanged(
                lazyListState.firstVisibleItemIndex,
                lazyListState.firstVisibleItemScrollOffset,
              )
            }
          }
      }

      DisposableEffect(lazyListState) {
        onDispose {
          if (!currentIsInSearchMode && pendingRestoreScroll == null) {
            currentOnScrollPositionChanged(
              lazyListState.firstVisibleItemIndex,
              lazyListState.firstVisibleItemScrollOffset,
            )
          }
        }
      }

      LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 100.dp),
        overscrollEffect = null,
      ) {
        if (showCloseIcon) {
          item(key = "CloseButtonHeader", contentType = "Header") {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
              CircularIconButton(
                modifier = Modifier.align(Alignment.CenterStart),
                icon = TwineIcons.Close,
                label = stringResource(Res.string.buttonGoBack),
                onClick = closeDrawer,
              )
            }

            Spacer(Modifier.requiredHeight(16.dp))
          }
        } else {
          item(key = "TopSpacerHeader", contentType = "Header") {
            Spacer(Modifier.requiredHeight(16.dp))
          }
        }

        items(
          items = MainDestination.entries,
          key = { it.name },
          contentType = { "MainDestination" },
        ) { destination ->
          val selected =
            if (destination == MainDestination.Home) {
              selectedDestination == destination && state.activeSource == null
            } else {
              selectedDestination == destination
            }
          val targetSelectionAlpha = if (selected) 1f else 0f
          val initialSelectionAlpha = remember { targetSelectionAlpha }
          var hasSelectionTransitioned by remember { mutableStateOf(false) }
          val selectionAlpha = remember { Animatable(initialSelectionAlpha) }
          if (hasSelectionTransitioned || targetSelectionAlpha != initialSelectionAlpha) {
            LaunchedEffect(targetSelectionAlpha) {
              hasSelectionTransitioned = true
              selectionAlpha.animateTo(targetSelectionAlpha)
            }
          }

          NavigationDrawerItem(
            modifier =
              Modifier.requiredHeight(44.dp).padding(end = 16.dp).drawWithCache {
                val outline = NavigationItemShape.createOutline(size, layoutDirection, this)
                onDrawBehind {
                  val alpha = selectionAlpha.value
                  if (alpha > 0f) {
                    val color = translucentStyle.default.background
                    drawOutline(outline, color.copy(alpha = color.alpha * alpha))
                  }
                }
              },
            label = {
              val style =
                if (selected) {
                  MaterialTheme.typography.titleSmall
                } else {
                  MaterialTheme.typography.bodyMedium
                }
              Text(
                text = stringResource(destination.label),
                style = style,
                color = { colorScheme.onSurface },
              )
            },
            onClick = {
              if (
                destination == MainDestination.Home && selectedDestination == MainDestination.Home
              ) {
                dispatch(FeedsEvent.OnHomeSelected)
              } else {
                onDestinationSelected(destination)
              }

              if (dismissOnSelection) {
                closeDrawer()
              }
            },
            icon = {
              val icon = if (selected) destination.selectedIcon else destination.icon
              Icon(
                modifier = Modifier.padding(start = 4.dp).requiredSize(20.dp),
                imageVector = icon,
                contentDescription = stringResource(destination.label),
              )
            },
            selected = selected,
            shape = NavigationItemShape,
            colors =
              NavigationDrawerItemDefaults.colors(
                selectedIconColor = colorScheme.settledOnSurface,
                unselectedIconColor = colorScheme.settledOnSurface,
                selectedTextColor = colorScheme.settledOnSurface,
                unselectedTextColor = colorScheme.settledOnSurface,
                selectedContainerColor = Color.Transparent,
                unselectedContainerColor = Color.Transparent,
              ),
          )
        }

        item(key = "DestinationsDivider", contentType = "Divider") {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .height(DividerDefaults.Thickness)
                .drawBehind { drawRect(colorScheme.outlineVariant) }
          )
        }

        item(key = "AllFeedsHeader", contentType = "AllFeedsHeader") {
          AllFeedsHeader(
            feedsCount = state.numberOfFeeds,
            visibleFeedsCount = state.visibleFeedsCount,
            feedsSortOrder = state.feedsSortOrder,
            hideFeedsWithoutUnreadPosts = state.hideFeedsWithoutUnreadPosts,
            hideGroupedFeeds = state.hideGroupedFeeds,
            onFeedsSortChanged = { dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
            onHideFeedsWithoutUnreadPostsChanged = {
              dispatch(FeedsEvent.OnHideFeedsWithoutUnreadPostsChanged(it))
            },
            onHideGroupedFeedsChanged = { dispatch(FeedsEvent.OnHideGroupedFeedsChanged(it)) },
            onAddNewFeedClick = { dispatch(FeedsEvent.OnNewFeedClicked) },
          )
        }

        pinnedSources(
          pinnedSources = state.pinnedSources,
          activeSource = state.activeSource,
          onSourceClick = { dispatch(FeedsEvent.OnSourceClick(it)) },
          onPinClick = { dispatch(FeedsEvent.OnSourcePinClicked(it)) },
          onPinnedSourceOrderChanged = { dispatch(FeedsEvent.OnPinnedSourcePositionChanged(it)) },
        )

        val hasSources = state.numberOfFeeds > 0 || state.numberOfFeedGroups > 0
        item(key = "FeedsSearchBar", contentType = "SearchBar") {
          androidx.compose.animation.AnimatedVisibility(
            visible = hasSources,
            enter = fadeIn(),
            exit = fadeOut(),
          ) {
            SearchBar(
              query = searchQuery,
              onQueryChange = { dispatch(FeedsEvent.SearchQueryChanged(it)) },
              onClearClick = { dispatch(FeedsEvent.ClearSearchQuery) },
            )
          }
        }

        if (state.numberOfFeeds == 0 && state.numberOfFeedGroups == 0 && !isInSearchMode) {
          item(key = "NoFeeds", contentType = "NoFeeds") { NoFeeds() }
        } else {
          if (isInSearchMode && searchResults != null) {
            sourcesSearchResults(
              searchResults = searchResults,
              selectedSourceIds = selectedSourceIds,
              activeSource = state.activeSource,
              canShowUnreadPostsCount = state.canShowUnreadPostsCount,
              isInMultiSelectMode = state.isInMultiSelectMode,
              onSourceClick = {
                val isSourceActive = state.activeSource?.id == it.id
                if (isSourceActive) {
                  dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  dispatch(FeedsEvent.OnSourceClick(it))
                }

                if (dismissOnSelection) {
                  closeDrawer()
                }
              },
              onToggleSourceSelection = { dispatch(FeedsEvent.OnToggleFeedSelection(it)) },
              onPinClick = { dispatch(FeedsEvent.OnSourcePinClicked(it)) },
              onSourceEditClick = {
                when (it.sourceType) {
                  SourceType.Feed -> openFeedInfoSheet(it.id)
                  SourceType.FeedGroup -> openGroupScreen(it.id)
                }
              },
              onAddToGroupClick = { dispatch(FeedsEvent.OnSourceAddToGroupClicked(it)) },
              onRemoveSourceClick = { dispatch(FeedsEvent.OnDeleteSourceClicked(it)) },
              onMarkAsReadClick = { dispatch(FeedsEvent.OnMarkSourceAsReadClicked(it)) },
            )
          } else {
            allSources(
              numberOfFeedGroups = state.numberOfFeedGroups,
              sources = allSources,
              hideFeedsWithoutUnreadPosts = state.hideFeedsWithoutUnreadPosts,
              selectedSourceIds = selectedSourceIds,
              activeSource = state.activeSource,
              canShowUnreadPostsCount = state.canShowUnreadPostsCount,
              isInMultiSelectMode = state.isInMultiSelectMode,
              haptic = haptic,
              onSourceClick = {
                val isSourceActive = state.activeSource?.id == it.id
                if (isSourceActive) {
                  dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  dispatch(FeedsEvent.OnSourceClick(it))
                }

                if (dismissOnSelection) {
                  closeDrawer()
                }
              },
              onToggleSourceSelection = { dispatch(FeedsEvent.OnToggleFeedSelection(it)) },
              onPinClick = { dispatch(FeedsEvent.OnSourcePinClicked(it)) },
              onSourceEditClick = {
                when (it.sourceType) {
                  SourceType.Feed -> openFeedInfoSheet(it.id)
                  SourceType.FeedGroup -> openGroupScreen(it.id)
                }
              },
              onAddToGroupClick = { dispatch(FeedsEvent.OnSourceAddToGroupClicked(it)) },
              onRemoveSourceClick = { dispatch(FeedsEvent.OnDeleteSourceClicked(it)) },
              onMarkAsReadClick = { dispatch(FeedsEvent.OnMarkSourceAsReadClicked(it)) },
            )
          }
        }
      }

      Box(modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
        androidx.compose.animation.AnimatedVisibility(
          visible = state.isInMultiSelectMode,
          enter = slideInVertically { it },
          exit = slideOutVertically { it },
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
            onCancel = { dispatch(FeedsEvent.CancelSourcesSelection) },
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
                  dispatch(FeedsEvent.UnPinSelectedSources)
                } else {
                  dispatch(FeedsEvent.PinSelectedSources)
                }
              },
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.NewGroup,
              label = stringResource(Res.string.actionAddTo),
              enabled = !areGroupsSelected,
              onClick = { openGroupSelectionSheet() },
            )

            ContextActionItem(
              modifier = Modifier.weight(1f),
              icon = TwineIcons.Delete,
              label = stringResource(Res.string.actionDelete),
              onClick = { dispatch(FeedsEvent.DeleteSelectedSourcesClicked) },
            )

            if (state.selectedSources.size == 1) {
              val editIcon =
                if (state.selectedSources.first().sourceType == SourceType.FeedGroup) {
                  TwineIcons.Edit
                } else {
                  TwineIcons.Tune
                }
              val editLabel = stringResource(Res.string.edit)

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

                  dispatch(FeedsEvent.CancelSourcesSelection)
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CollapsedDrawerContent(
  state: FeedsState,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  dispatch: (FeedsEvent) -> Unit,
  closeDrawer: () -> Unit,
  dismissOnSelection: Boolean,
) {
  val sources = state.sources.collectAsLazyPagingItems()
  val colorScheme = AppTheme.colorScheme

  val navigationRailItemColors =
    NavigationRailItemDefaults.colors(
      selectedIconColor = colorScheme.settledPrimary,
      unselectedIconColor = colorScheme.settledOnSurfaceVariant,
      indicatorColor = Color.Transparent,
    )

  LazyColumn(
    modifier =
      Modifier.fillMaxHeight()
        .fillMaxWidth()
        .drawBehind { drawRect(colorScheme.backdrop) }
        .windowInsetsPadding(WindowInsets.systemBars)
        .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    items(
      items = MainDestination.entries,
      key = { it.name },
      contentType = { "MainDestination" },
    ) { destination ->
      val selected =
        if (destination == MainDestination.Home) {
          selectedDestination == destination && state.activeSource == null
        } else {
          selectedDestination == destination
        }

      NavigationRailItem(
        selected = selected,
        onClick = {
          if (destination == MainDestination.Home && selectedDestination == MainDestination.Home) {
            dispatch(FeedsEvent.OnHomeSelected)
          } else {
            onDestinationSelected(destination)
          }

          if (dismissOnSelection) {
            closeDrawer()
          }
        },
        icon = {
          val icon = if (selected) destination.selectedIcon else destination.icon
          Icon(
            modifier = Modifier.requiredSize(20.dp),
            imageVector = icon,
            contentDescription = stringResource(destination.label),
          )
        },
        label = null,
        colors = navigationRailItemColors,
      )
    }

    if (state.pinnedSources.isNotEmpty()) {
      item(key = "PinnedSourcesDivider", contentType = "Divider") {
        Spacer(Modifier.requiredHeight(16.dp))

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .height(DividerDefaults.Thickness)
              .drawBehind { drawRect(colorScheme.outlineVariant) }
        )

        Spacer(Modifier.requiredHeight(16.dp))
      }

      items(
        items = state.pinnedSources,
        key = { "PinnedSource:${it.id}" },
        contentType = { it.sourceType },
      ) { source ->
        val selected = state.activeSource?.id == source.id
        Box(modifier = Modifier.padding(bottom = 8.dp)) {
          PinnedSourceIcon(
            source = source,
            selected = selected,
            hasActiveSource = state.activeSource != null,
            onClick = {
              if (selected) {
                dispatch(FeedsEvent.OnHomeSelected)
              } else {
                dispatch(FeedsEvent.OnSourceClick(source))
              }

              if (dismissOnSelection) {
                closeDrawer()
              }
            },
          )
        }
      }
    }

    if (sources.itemCount > 0) {
      item(key = "AllSourcesDivider", contentType = "Divider") {
        Spacer(Modifier.requiredHeight(16.dp))

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .height(DividerDefaults.Thickness)
              .drawBehind { drawRect(colorScheme.outlineVariant) }
        )

        Spacer(Modifier.requiredHeight(16.dp))
      }

      items(
        count = sources.itemCount,
        key =
          sources.itemKey {
            when (it) {
              SourceListItem.Separator -> "Separator"
              is SourceListItem.SourceItem -> it.source.id
            }
          },
        contentType = { index ->
          when (val item = sources.peek(index)) {
            SourceListItem.Separator -> "Separator"
            is SourceListItem.SourceItem -> item.source.sourceType
            null -> null
          }
        },
      ) { index ->
        val sourceItem = sources[index]
        if (sourceItem is SourceListItem.SourceItem) {
          val source = sourceItem.source
          val selected = state.activeSource?.id == source.id
          Box(modifier = Modifier.padding(bottom = 8.dp)) {
            SourceIcon(
              source = source,
              selected = selected,
              hasActiveSource = state.activeSource != null,
              onClick = {
                if (selected) {
                  dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  dispatch(FeedsEvent.OnSourceClick(source))
                }

                if (dismissOnSelection) {
                  closeDrawer()
                }
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PinnedSourceIcon(
  source: Source,
  selected: Boolean,
  hasActiveSource: Boolean,
  onClick: () -> Unit,
) {
  val colorScheme = AppTheme.colorScheme
  val iconSize = 32.dp
  val targetSelectionAlpha = if (selected) 1f else 0f
  val initialSelectionAlpha = remember { targetSelectionAlpha }
  var hasSelectionTransitioned by remember { mutableStateOf(false) }
  val selectionAlpha = remember { Animatable(initialSelectionAlpha) }
  if (hasSelectionTransitioned || targetSelectionAlpha != initialSelectionAlpha) {
    LaunchedEffect(targetSelectionAlpha) {
      hasSelectionTransitioned = true
      selectionAlpha.animateTo(targetSelectionAlpha)
    }
  }

  val iconAlpha = if (selected || !hasActiveSource) 1f else 0.25f

  Box(modifier = Modifier.requiredSize(iconSize + 8.dp), contentAlignment = Alignment.Center) {
    Box(
      modifier =
        Modifier.matchParentSize().drawWithCache {
          val outline = SelectedSourceColorShape.createOutline(size, layoutDirection, this)
          onDrawBehind {
            val alpha = selectionAlpha.value * iconAlpha
            if (alpha > 0f) {
              drawOutline(outline, colorScheme.primaryContainer.copy(alpha = alpha))
            }
          }
        }
    )

    val clickableModifier =
      Modifier.requiredSize(iconSize).clip(SourceIconShape).clickable { onClick() }

    when (source) {
      is Feed -> {
        FeedIcon(
          icon = source.icon,
          homepageLink = source.homepageLink,
          showFeedFavIcon = source.showFeedFavIcon,
          contentDescription = null,
          modifier =
            Modifier.graphicsLayer {
                compositingStrategy = CompositingStrategy.ModulateAlpha
                alpha = iconAlpha
              }
              .then(clickableModifier),
        )
      }
      is FeedGroup -> {
        val groupCutoutColor =
          remember(colorScheme) {
            { colorScheme.secondary.copy(alpha = 0.16f).compositeOver(colorScheme.backdrop) }
          }
        Box(
          modifier =
            Modifier.graphicsLayer { alpha = iconAlpha }
              .then(clickableModifier)
              .drawWithCache {
                val outline = SourceIconShape.createOutline(size, layoutDirection, this)
                onDrawBehind { drawOutline(outline, colorScheme.secondary.copy(alpha = 0.16f)) }
              },
          contentAlignment = Alignment.Center,
        ) {
          FeedGroupIconGrid(
            feedHomepageLinks = source.feedHomepageLinks,
            feedIconLinks = source.feedIconLinks,
            feedShowFavIconSettings = source.feedShowFavIconSettings,
            cutoutColor = groupCutoutColor,
          )
        }
      }
    }

    Box(
      modifier =
        Modifier.align(Alignment.TopEnd).requiredSize(14.dp).drawBehind {
          drawCircle(color = colorScheme.backdrop, radius = 8.dp.toPx())
          drawCircle(color = lerp(colorScheme.backdrop, colorScheme.inverseSurface, iconAlpha))
        },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = TwineIcons.Pin,
        contentDescription = null,
        tint = colorScheme.settledInverseOnSurface.copy(alpha = iconAlpha),
        modifier = Modifier.padding(2.dp),
      )
    }
  }
}

@Composable
private fun SourceIcon(
  source: Source,
  selected: Boolean,
  hasActiveSource: Boolean,
  onClick: () -> Unit,
) {
  val colorScheme = AppTheme.colorScheme
  val iconSize = 32.dp
  val targetSelectionAlpha = if (selected) 1f else 0f
  val initialSelectionAlpha = remember { targetSelectionAlpha }
  var hasSelectionTransitioned by remember { mutableStateOf(false) }
  val selectionAlpha = remember { Animatable(initialSelectionAlpha) }
  if (hasSelectionTransitioned || targetSelectionAlpha != initialSelectionAlpha) {
    LaunchedEffect(targetSelectionAlpha) {
      hasSelectionTransitioned = true
      selectionAlpha.animateTo(targetSelectionAlpha)
    }
  }

  val iconAlpha = if (selected || !hasActiveSource) 1f else 0.25f

  Box(
    contentAlignment = Alignment.Center,
    modifier =
      Modifier.graphicsLayer {
        compositingStrategy =
          if (source is Feed) CompositingStrategy.ModulateAlpha else CompositingStrategy.Auto
        alpha = iconAlpha
      },
  ) {
    Box(
      modifier =
        Modifier.requiredSize(iconSize + 8.dp).drawWithCache {
          val outline = SelectedSourceColorShape.createOutline(size, layoutDirection, this)
          onDrawBehind {
            val alpha = selectionAlpha.value
            if (alpha > 0f) {
              drawOutline(outline, colorScheme.primaryContainer.copy(alpha = alpha))
            }
          }
        }
    )

    val clickableModifier = Modifier.requiredSize(iconSize).clickable { onClick() }

    when (source) {
      is Feed -> {
        FeedIcon(
          icon = source.icon,
          homepageLink = source.homepageLink,
          showFeedFavIcon = source.showFeedFavIcon,
          contentDescription = null,
          modifier = clickableModifier,
        )
      }
      is FeedGroup -> {
        val groupCutoutColor =
          remember(colorScheme) {
            { colorScheme.secondary.copy(alpha = 0.16f).compositeOver(colorScheme.backdrop) }
          }
        Box(
          modifier =
            Modifier.clip(SourceIconShape).then(clickableModifier).drawWithCache {
              val outline = SourceIconShape.createOutline(size, layoutDirection, this)
              onDrawBehind { drawOutline(outline, colorScheme.secondary.copy(alpha = 0.16f)) }
            },
          contentAlignment = Alignment.Center,
        ) {
          FeedGroupIconGrid(
            feedHomepageLinks = source.feedHomepageLinks,
            feedIconLinks = source.feedIconLinks,
            feedShowFavIconSettings = source.feedShowFavIconSettings,
            cutoutColor = groupCutoutColor,
          )
        }
      }
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
  val colorScheme = AppTheme.colorScheme
  val settledOnSurface = colorScheme.settledOnSurface
  val outlineColor = settledOnSurface.copy(alpha = 0.16f)
  val containerColor = settledOnSurface.copy(alpha = 0.08f)

  LaunchedEffect(keyboardState) {
    if (keyboardState == KeyboardState.Closed) {
      focusManager.clearFocus()
    }
  }

  OutlinedTextField(
    modifier =
      Modifier.fillMaxWidth()
        .focusable(false)
        .padding(horizontal = 16.dp)
        .padding(top = 8.dp, bottom = 16.dp),
    value = query,
    onValueChange = onQueryChange,
    placeholder = {
      Text(
        text = stringResource(Res.string.feedsSearchHint),
        color = { colorScheme.onSurfaceVariant },
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    trailingIcon = {
      if (query.text.isNotBlank()) {
        IconButton(icon = TwineIcons.Close, contentDescription = null, onClick = onClearClick)
      }
    },
    shape = SearchBarShape,
    singleLine = true,
    textStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedBorderColor = outlineColor,
        unfocusedBorderColor = outlineColor,
        focusedTextColor = settledOnSurface,
        disabledTextColor = Color.Transparent,
        unfocusedContainerColor = containerColor,
        focusedContainerColor = containerColor,
      ),
  )
}

@Composable
private fun NoFeeds() {
  val colorScheme = AppTheme.colorScheme
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        text = stringResource(Res.string.feedsLetsStart),
        style = MaterialTheme.typography.labelLarge,
        color = { colorScheme.onSurfaceVariant },
        textAlign = TextAlign.Center,
      )
    }
  }
}

private data class DrawerRestoreSnapshot(
  val itemCount: Int,
  val refreshState: LoadState,
  val isInSearchMode: Boolean,
)

@Preview(locale = "en")
@Composable
private fun NavigationDrawerPreview() {
  AppTheme {
    NavigationDrawerContent(
      state = FeedsState.DEFAULT,
      searchQuery = TextFieldValue(),
      selectedDestination = MainDestination.Home,
      onDestinationSelected = {},
      dispatch = {},
      openFeedInfoSheet = {},
      openGroupScreen = {},
      openGroupSelectionSheet = {},
      closeDrawer = {},
    )
  }
}
