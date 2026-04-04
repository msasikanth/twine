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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
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

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .fillMaxHeight()
        .background(AppTheme.colorScheme.backdrop)
        .windowInsetsPadding(WindowInsets.systemBars)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      val allSources = state.sources.collectAsLazyPagingItems()
      val searchResults = state.feedsSearchResults.collectAsLazyPagingItems()
      val isInSearchMode by derivedStateOf {
        searchQuery.text.length >= Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
      }

      val imeBottomPadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
      val lazyListState = rememberLazyListState()
      val translucentStyle = LocalTranslucentStyles.current

      LazyColumn(
        modifier =
          Modifier.fillMaxSize()
            .padding(bottom = if (imeBottomPadding > 0.dp) imeBottomPadding else 0.dp),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = 100.dp),
        overscrollEffect = null,
      ) {
        if (showCloseIcon) {
          item {
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
          item { Spacer(Modifier.requiredHeight(16.dp)) }
        }

        items(MainDestination.entries) { destination ->
          val selected =
            if (destination == MainDestination.Home) {
              selectedDestination == destination && state.activeSource == null
            } else {
              selectedDestination == destination
            }
          val backgroundColor by
            animateColorAsState(
              if (selected) {
                translucentStyle.default.background
              } else {
                Color.Transparent
              }
            )

          val navigationItemShape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50)

          NavigationDrawerItem(
            modifier =
              Modifier.requiredHeight(44.dp)
                .padding(end = 16.dp)
                .background(backgroundColor, navigationItemShape),
            label = {
              val style =
                if (selected) {
                  MaterialTheme.typography.titleSmall
                } else {
                  MaterialTheme.typography.bodyMedium
                }
              Text(text = stringResource(destination.label), style = style)
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
            shape = navigationItemShape,
            colors =
              NavigationDrawerItemDefaults.colors(
                selectedIconColor = AppTheme.colorScheme.onSurface,
                unselectedIconColor = AppTheme.colorScheme.onSurface,
                selectedTextColor = AppTheme.colorScheme.onSurface,
                unselectedTextColor = AppTheme.colorScheme.onSurface,
                selectedContainerColor = Color.Transparent,
                unselectedContainerColor = Color.Transparent,
              ),
          )
        }

        item {
          AllFeedsHeader(
            feedsCount = state.numberOfFeeds,
            feedsSortOrder = state.feedsSortOrder,
            onFeedsSortChanged = { dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
            onAddNewFeedClick = { dispatch(FeedsEvent.OnNewFeedClicked) },
          )
        }

        pinnedSources(
          pinnedSources = state.pinnedSources,
          onSourceClick = { dispatch(FeedsEvent.OnSourceClick(it)) },
          onPinClick = { dispatch(FeedsEvent.OnSourcePinClicked(it)) },
          onPinnedSourceOrderChanged = { dispatch(FeedsEvent.OnPinnedSourcePositionChanged(it)) },
        )

        item {
          SearchBar(
            query = searchQuery,
            onQueryChange = { dispatch(FeedsEvent.SearchQueryChanged(it)) },
            onClearClick = { dispatch(FeedsEvent.ClearSearchQuery) },
          )
        }

        if (state.numberOfFeeds == 0 && state.numberOfFeedGroups == 0 && !isInSearchMode) {
          item { NoFeeds() }
        } else {
          if (isInSearchMode) {
            sourcesSearchResults(
              searchResults = searchResults,
              selectedSources = state.selectedSources,
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
            )
          } else {
            allSources(
              numberOfFeedGroups = state.numberOfFeedGroups,
              sources = allSources,
              selectedSources = state.selectedSources,
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

  val navigationRailItemColors =
    NavigationRailItemDefaults.colors(
      selectedIconColor = AppTheme.colorScheme.primary,
      unselectedIconColor = AppTheme.colorScheme.onSurfaceVariant,
      indicatorColor = Color.Transparent,
    )

  LazyColumn(
    modifier =
      Modifier.fillMaxHeight()
        .fillMaxWidth()
        .background(AppTheme.colorScheme.backdrop)
        .windowInsetsPadding(WindowInsets.systemBars)
        .padding(vertical = 16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    items(MainDestination.entries) { destination ->
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
      item {
        Spacer(Modifier.requiredHeight(16.dp))

        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 16.dp),
          color = AppTheme.colorScheme.outlineVariant,
        )

        Spacer(Modifier.requiredHeight(16.dp))
      }

      items(state.pinnedSources) { source ->
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
      item {
        Spacer(Modifier.requiredHeight(16.dp))

        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 16.dp),
          color = AppTheme.colorScheme.outlineVariant,
        )

        Spacer(Modifier.requiredHeight(16.dp))
      }

      items(sources.itemCount) { index ->
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
  val iconSize = 32.dp

  Box(
    modifier =
      Modifier.requiredSize(iconSize + 8.dp)
        .graphicsLayer(
          compositingStrategy = CompositingStrategy.Offscreen,
          alpha = if (selected || !hasActiveSource) 1f else 0.25f,
        ),
    contentAlignment = Alignment.Center,
  ) {
    val selectedColor by
      animateColorAsState(
        if (selected) {
          AppTheme.colorScheme.primaryContainer
        } else {
          Color.Transparent
        }
      )

    Box(modifier = Modifier.matchParentSize().background(selectedColor, RoundedCornerShape(12.dp)))

    val shape = RoundedCornerShape(8.dp)
    val clickableModifier = Modifier.requiredSize(iconSize).clip(shape).clickable { onClick() }

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
        Box(
          modifier =
            Modifier.clip(shape)
              .then(clickableModifier)
              .background(AppTheme.colorScheme.secondary.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          FeedGroupIconGrid(
            feedHomepageLinks = source.feedHomepageLinks,
            feedIconLinks = source.feedIconLinks,
            feedShowFavIconSettings = source.feedShowFavIconSettings,
          )
        }
      }
    }

    Surface(
      modifier =
        Modifier.align(Alignment.TopEnd).requiredSize(14.dp).dropShadow(CircleShape) {
          spread = 1.dp.toPx()
          color = Color.Black
          blendMode = BlendMode.DstOut
        },
      shape = CircleShape,
      color = AppTheme.colorScheme.inverseSurface,
      contentColor = AppTheme.colorScheme.inverseOnSurface,
    ) {
      Icon(
        imageVector = TwineIcons.Pin,
        contentDescription = null,
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
  val iconSize = 32.dp

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier.graphicsLayer { alpha = if (selected || !hasActiveSource) 1f else 0.25f },
  ) {
    val selectedColor by
      animateColorAsState(
        if (selected) {
          AppTheme.colorScheme.primaryContainer
        } else {
          Color.Transparent
        }
      )

    Box(
      modifier =
        Modifier.requiredSize(iconSize + 8.dp).background(selectedColor, RoundedCornerShape(12.dp))
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
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .then(clickableModifier)
              .background(AppTheme.colorScheme.secondary.copy(alpha = 0.16f)),
          contentAlignment = Alignment.Center,
        ) {
          FeedGroupIconGrid(
            feedHomepageLinks = source.feedHomepageLinks,
            feedIconLinks = source.feedIconLinks,
            feedShowFavIconSettings = source.feedShowFavIconSettings,
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
  val translucentStyle = LocalTranslucentStyles.current

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
        color = AppTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
      )
    },
    trailingIcon = {
      if (query.text.isNotBlank()) {
        IconButton(icon = TwineIcons.Close, contentDescription = null, onClick = onClearClick)
      }
    },
    shape = RoundedCornerShape(50),
    singleLine = true,
    textStyle = MaterialTheme.typography.bodyMedium,
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedBorderColor = translucentStyle.default.outline,
        unfocusedBorderColor = translucentStyle.default.outline,
        focusedTextColor = AppTheme.colorScheme.onSurfaceVariant,
        disabledTextColor = Color.Transparent,
        unfocusedContainerColor = translucentStyle.default.background,
        focusedContainerColor = translucentStyle.default.background,
      ),
  )
}

@Composable
private fun NoFeeds() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
        text = stringResource(Res.string.feedsLetsStart),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )
    }
  }
}

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
