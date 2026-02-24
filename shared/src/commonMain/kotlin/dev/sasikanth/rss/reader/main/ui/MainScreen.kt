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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.resources.icons.Home
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlinx.coroutines.launch

@Composable
internal fun MainScreen(
  feedsViewModel: FeedsViewModel,
  homeContent: @Composable (openDrawer: () -> Unit) -> Unit,
  bookmarksContent: @Composable (openDrawer: () -> Unit) -> Unit,
  settingsContent: @Composable (openDrawer: () -> Unit) -> Unit,
  discoveryContent: @Composable (openDrawer: () -> Unit) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val sizeClass = LocalWindowSizeClass.current
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.Home) }
  var isSideNavigationExpanded by rememberSaveable {
    mutableStateOf(sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND))
  }

  NavigationEventHandler(
    state = rememberNavigationEventState(NavigationEventInfo.None),
    isBackEnabled =
      selectedDestination != MainDestination.Home ||
        (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
          isSideNavigationExpanded &&
          !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)) ||
        (drawerState.isOpen),
  ) {
    when {
      drawerState.isOpen -> {
        scope.launch { drawerState.close() }
      }
      isSideNavigationExpanded &&
        !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> {
        isSideNavigationExpanded = false
      }
      else -> {
        selectedDestination = MainDestination.Home
      }
    }
  }

  val focusManager = LocalFocusManager.current

  LaunchedEffect(drawerState.currentValue) {
    if (drawerState.currentValue == DrawerValue.Closed) {
      feedsViewModel.dispatch(FeedsEvent.ClearSearchQuery)
      feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection)
    }
  }

  LaunchedEffect(isSideNavigationExpanded) {
    if (!isSideNavigationExpanded) {
      feedsViewModel.dispatch(FeedsEvent.ClearSearchQuery)
      feedsViewModel.dispatch(FeedsEvent.CancelSourcesSelection)
    }
  }

  LaunchedEffect(drawerState.isOpen, isSideNavigationExpanded) {
    if (drawerState.isOpen || isSideNavigationExpanded) {
      focusManager.clearFocus()
    }
  }

  val openDrawer = {
    focusManager.clearFocus()
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
        isSideNavigationExpanded = !isSideNavigationExpanded
      }
      else -> {
        scope.launch { drawerState.open() }
      }
    }
    Unit
  }

  val goBackToHome = { selectedDestination = MainDestination.Home }

  val drawerContent =
    @Composable { isExpanded: Boolean ->
      val showCloseIcon =
        !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
      val dismissOnSelection =
        !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

      NavigationDrawerContent(
        feedsViewModel = feedsViewModel,
        selectedDestination = selectedDestination,
        onDestinationSelected = {
          if (it == MainDestination.Home && selectedDestination == MainDestination.Home) {
            feedsViewModel.dispatch(FeedsEvent.OnHomeSelected)
          } else {
            selectedDestination = it
          }

          if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
          }
        },
        openFeedInfoSheet = openFeedInfoSheet,
        openGroupScreen = openGroupScreen,
        openGroupSelectionSheet = openGroupSelectionSheet,
        openAddFeedScreen = openAddFeedScreen,
        openPaywall = openPaywall,
        closeDrawer = {
          when {
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
              isSideNavigationExpanded = false
            }
            else -> {
              scope.launch { drawerState.close() }
            }
          }
        },
        expanded = isExpanded,
        showCloseIcon = showCloseIcon,
        dismissOnSelection = dismissOnSelection,
      )
    }

  when {
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> {
      PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
          AnimatedVisibility(
            visible = isSideNavigationExpanded,
            enter = slideInHorizontally(initialOffsetX = { -it }) + expandHorizontally(),
            exit = slideOutHorizontally(targetOffsetX = { -it }) + shrinkHorizontally(),
          ) {
            Box(modifier = Modifier.requiredWidth(360.dp)) { drawerContent(true) }
          }
        },
      ) {
        MainScreenContent(
          selectedDestination = selectedDestination,
          homeContent = { homeContent(openDrawer) },
          bookmarksContent = { bookmarksContent(goBackToHome) },
          settingsContent = { settingsContent(goBackToHome) },
          discoveryContent = { discoveryContent(goBackToHome) },
        )
      }
    }
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
      Row(modifier = modifier.fillMaxSize()) {
        val sideNavigationWidth by
          androidx.compose.animation.core.animateDpAsState(
            if (isSideNavigationExpanded) 360.dp else 80.dp
          )

        Box(modifier = Modifier.requiredWidth(sideNavigationWidth)) {
          drawerContent(isSideNavigationExpanded)
        }

        Box(modifier = Modifier.weight(1f)) {
          MainScreenContent(
            selectedDestination = selectedDestination,
            homeContent = { homeContent(openDrawer) },
            bookmarksContent = { bookmarksContent(goBackToHome) },
            settingsContent = { settingsContent(goBackToHome) },
            discoveryContent = { discoveryContent(goBackToHome) },
          )
        }
      }
    }
    else -> {
      ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet(
            modifier = Modifier.fillMaxWidth(),
            drawerContainerColor = AppTheme.colorScheme.backdrop,
            drawerContentColor = AppTheme.colorScheme.onSurface,
            drawerShape = RectangleShape,
          ) {
            drawerContent(true)
          }
        },
      ) {
        MainScreenContent(
          selectedDestination = selectedDestination,
          homeContent = { homeContent(openDrawer) },
          bookmarksContent = { bookmarksContent(goBackToHome) },
          settingsContent = { settingsContent(goBackToHome) },
          discoveryContent = { discoveryContent(goBackToHome) },
        )
      }
    }
  }
}

@Composable
private fun MainScreenContent(
  selectedDestination: MainDestination,
  homeContent: @Composable () -> Unit,
  bookmarksContent: @Composable () -> Unit,
  settingsContent: @Composable () -> Unit,
  discoveryContent: @Composable () -> Unit,
) {
  when (selectedDestination) {
    MainDestination.Home -> homeContent()
    MainDestination.Bookmarks -> bookmarksContent()
    MainDestination.Settings -> settingsContent()
    MainDestination.Discovery -> discoveryContent()
  }
}
