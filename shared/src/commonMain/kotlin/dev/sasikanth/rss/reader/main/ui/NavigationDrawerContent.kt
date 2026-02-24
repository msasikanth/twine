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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.ContextActionItem
import dev.sasikanth.rss.reader.components.ContextActionsBottomBar
import dev.sasikanth.rss.reader.components.FilledIconButton
import dev.sasikanth.rss.reader.components.IconButtonSize
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.feeds.SourceListItem
import dev.sasikanth.rss.reader.feeds.ui.CreateGroupDialog
import dev.sasikanth.rss.reader.feeds.ui.DeleteConfirmationDialog
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.feeds.ui.common.AllFeedsHeader
import dev.sasikanth.rss.reader.feeds.ui.common.allSources
import dev.sasikanth.rss.reader.feeds.ui.common.sourcesSearchResults
import dev.sasikanth.rss.reader.resources.icons.Add
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.Delete
import dev.sasikanth.rss.reader.resources.icons.NewGroup
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.KeyboardState
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.keyboardVisibilityAsState
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.actionAddTo
import twine.shared.generated.resources.actionDelete
import twine.shared.generated.resources.actionGroupsTooltip
import twine.shared.generated.resources.actionPin
import twine.shared.generated.resources.actionUnpin
import twine.shared.generated.resources.buttonAddFeed
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
  val focusManager = LocalFocusManager.current
  val closeDrawerWithFocusClear = {
    focusManager.clearFocus()
    closeDrawer()
  }

  LaunchedEffect(expanded) { focusManager.clearFocus() }

  Crossfade(targetState = expanded, modifier = modifier) { isExpanded ->
    if (isExpanded) {
      ExpandedDrawerContent(
        feedsViewModel = feedsViewModel,
        selectedDestination = selectedDestination,
        onDestinationSelected = onDestinationSelected,
        openFeedInfoSheet = openFeedInfoSheet,
        openGroupScreen = openGroupScreen,
        openGroupSelectionSheet = openGroupSelectionSheet,
        openAddFeedScreen = openAddFeedScreen,
        openPaywall = openPaywall,
        closeDrawer = closeDrawerWithFocusClear,
        showCloseIcon = showCloseIcon,
        dismissOnSelection = dismissOnSelection,
      )
    } else {
      CollapsedDrawerContent(
        feedsViewModel = feedsViewModel,
        selectedDestination = selectedDestination,
        onDestinationSelected = onDestinationSelected,
        closeDrawer = closeDrawerWithFocusClear,
        dismissOnSelection = dismissOnSelection,
      )
    }
  }
}

@Composable
private fun ExpandedDrawerContent(
  feedsViewModel: FeedsViewModel,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  closeDrawer: () -> Unit,
  showCloseIcon: Boolean,
  dismissOnSelection: Boolean,
) {
  val state by feedsViewModel.state.collectAsStateWithLifecycle()
  val searchQuery = feedsViewModel.searchQuery

  var showNewGroupDialog by remember { mutableStateOf(false) }

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

  if (state.showDeleteConfirmation) {
    DeleteConfirmationDialog(
      onDelete = { feedsViewModel.dispatch(FeedsEvent.DeleteSelectedSources) },
      dismiss = { feedsViewModel.dispatch(FeedsEvent.DismissDeleteConfirmation) },
    )
  }

  if (showNewGroupDialog) {
    CreateGroupDialog(
      onCreateGroup = { feedsViewModel.dispatch(FeedsEvent.OnCreateGroup(it)) },
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

          NavigationDrawerItem(
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
                color = AppTheme.colorScheme.onSurface,
              )
            },
            selected = selected,
            modifier =
              Modifier.then(
                if (selected) {
                  Modifier.background(
                    brush =
                      Brush.horizontalGradient(
                        0.0f to AppTheme.colorScheme.primaryContainer,
                        0.6f to AppTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        0.85f to Color.Transparent,
                        1.0f to Color.Transparent,
                      )
                  )
                } else {
                  Modifier
                }
              ),
            onClick = {
              if (
                destination == MainDestination.Home && selectedDestination == MainDestination.Home
              ) {
                feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
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
                modifier = Modifier.sizeIn(20.dp).padding(start = 4.dp),
                imageVector = icon,
                contentDescription = stringResource(destination.label),
              )
            },
            colors =
              NavigationDrawerItemDefaults.colors(
                selectedIconColor = AppTheme.colorScheme.primary,
                unselectedIconColor = AppTheme.colorScheme.onSurfaceVariant,
                selectedTextColor = AppTheme.colorScheme.primary,
                unselectedTextColor = AppTheme.colorScheme.onSurfaceVariant,
                selectedContainerColor = Color.Transparent,
                unselectedContainerColor = Color.Transparent,
              ),
          )
        }

        item {
          AllFeedsHeader(
            feedsCount = state.numberOfFeeds,
            feedsSortOrder = state.feedsSortOrder,
            onFeedsSortChanged = { feedsViewModel.dispatch(FeedsEvent.OnFeedSortOrderChanged(it)) },
            onAddNewFeedClick = { feedsViewModel.dispatch(FeedsEvent.OnNewFeedClicked) },
          )
        }

        item {
          SearchBar(
            query = searchQuery,
            onQueryChange = { feedsViewModel.dispatch(FeedsEvent.SearchQueryChanged(it)) },
            onClearClick = { feedsViewModel.dispatch(FeedsEvent.ClearSearchQuery) },
          )
        }

        if (state.numberOfFeeds == 0 && state.numberOfFeedGroups == 0 && !isInSearchMode) {
          item {
            NoFeeds(onAddNewFeedClick = { feedsViewModel.dispatch(FeedsEvent.OnNewFeedClicked) })
          }
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
                  feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  feedsViewModel.dispatch(FeedsEvent.OnSourceClick(it))
                }

                if (dismissOnSelection) {
                  closeDrawer()
                }
              },
              onToggleSourceSelection = {
                feedsViewModel.dispatch(FeedsEvent.OnToggleFeedSelection(it))
              },
              onPinClick = { feedsViewModel.dispatch(FeedsEvent.OnSourcePinClicked(it)) },
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
                  feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  feedsViewModel.dispatch(FeedsEvent.OnSourceClick(it))
                }

                if (dismissOnSelection) {
                  closeDrawer()
                }
              },
              onToggleSourceSelection = {
                feedsViewModel.dispatch(FeedsEvent.OnToggleFeedSelection(it))
              },
              onPinClick = { feedsViewModel.dispatch(FeedsEvent.OnSourcePinClicked(it)) },
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
            onCancel = { feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection) },
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
                  feedsViewModel.dispatch(FeedsEvent.UnPinSelectedSources)
                } else {
                  feedsViewModel.dispatch(FeedsEvent.PinSelectedSources)
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
              onClick = { feedsViewModel.dispatch(FeedsEvent.DeleteSelectedSourcesClicked) },
            )

            if (state.selectedSources.size == 1) {
              val editIcon =
                if (state.selectedSources.first().sourceType == SourceType.FeedGroup) {
                  Icons.Filled.Edit
                } else {
                  Icons.Filled.Tune
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

                  feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection)
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
  feedsViewModel: FeedsViewModel,
  selectedDestination: MainDestination,
  onDestinationSelected: (MainDestination) -> Unit,
  closeDrawer: () -> Unit,
  dismissOnSelection: Boolean,
) {
  val state by feedsViewModel.state.collectAsStateWithLifecycle()
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
            feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
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
                  feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
                } else {
                  feedsViewModel.dispatch(FeedsEvent.OnSourceClick(source))
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
private fun SourceIcon(
  source: Source,
  selected: Boolean,
  hasActiveSource: Boolean,
  onClick: () -> Unit,
) {
  val showFeedFavIcon = LocalShowFeedFavIconSetting.current
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
          showFeedFavIcon = showFeedFavIcon,
          contentDescription = null,
          shape = RoundedCornerShape(8.dp),
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
            iconSize = 12.dp,
            iconShape = RoundedCornerShape(4.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp),
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
        IconButton(onClick = onClearClick) {
          Icon(
            Icons.Rounded.Close,
            contentDescription = null,
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
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
private fun NoFeeds(onAddNewFeedClick: () -> Unit) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = stringResource(Res.string.feedsLetsStart),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
      )

      Spacer(Modifier.requiredHeight(48.dp))

      FilledIconButton(
        icon = TwineIcons.Add,
        contentDescription = stringResource(Res.string.buttonAddFeed),
        containerColor = AppTheme.colorScheme.inverseSurface,
        iconTint = AppTheme.colorScheme.inverseOnSurface,
        size = IconButtonSize.Large,
        onClick = onAddNewFeedClick,
      )
    }
  }
}
