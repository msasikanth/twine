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
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.app.Screen
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
  searchContent: @Composable (openDrawer: () -> Unit) -> Unit,
  bookmarksContent: @Composable (openDrawer: () -> Unit) -> Unit,
  settingsContent: @Composable (openDrawer: () -> Unit) -> Unit,
  discoveryContent: @Composable (openDrawer: () -> Unit) -> Unit,
  openFeedInfoSheet: (id: String) -> Unit,
  openGroupScreen: (id: String) -> Unit,
  openGroupSelectionSheet: () -> Unit,
  openAddFeedScreen: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
  canHandleBack: Boolean = true,
) {
  val sizeClass = LocalWindowSizeClass.current
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  val selectedDestination =
    when {
      currentDestination?.hasRoute(Screen.MainHome::class) == true -> MainDestination.Home
      currentDestination?.hasRoute(Screen.MainSearch::class) == true -> MainDestination.Search
      currentDestination?.hasRoute(Screen.MainBookmarks::class) == true -> MainDestination.Bookmarks
      currentDestination?.hasRoute(Screen.MainSettings::class) == true -> MainDestination.Settings
      currentDestination?.hasRoute(Screen.MainDiscovery::class) == true -> MainDestination.Discovery
      else -> MainDestination.Home
    }

  var isSideNavigationExpanded by rememberSaveable {
    mutableStateOf(sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND))
  }

  NavigationEventHandler(
    state = rememberNavigationEventState(NavigationEventInfo.None),
    isBackEnabled =
      canHandleBack &&
        (selectedDestination != MainDestination.Home ||
          (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
            isSideNavigationExpanded &&
            !sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)) ||
          (drawerState.isOpen)),
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
        navController.popBackStack<Screen.MainHome>(inclusive = false)
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

  val goBackToHome = {
    if (selectedDestination != MainDestination.Home) {
      navController.popBackStack<Screen.MainHome>(inclusive = false)
    }
  }

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
            val route =
              when (it) {
                MainDestination.Home -> Screen.MainHome
                MainDestination.Search -> Screen.MainSearch
                MainDestination.Bookmarks -> Screen.MainBookmarks
                MainDestination.Settings -> Screen.MainSettings
                MainDestination.Discovery -> Screen.MainDiscovery
              }

            navController.navigate(route) {
              popUpTo(navController.graph.findStartDestination().id) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
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
          navController = navController,
          homeContent = { homeContent(openDrawer) },
          searchContent = { searchContent(goBackToHome) },
          bookmarksContent = { bookmarksContent(goBackToHome) },
          settingsContent = { settingsContent(goBackToHome) },
          discoveryContent = { discoveryContent(goBackToHome) },
        )
      }
    }
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
      Row(modifier = modifier.fillMaxSize()) {
        val sideNavigationWidth by animateDpAsState(if (isSideNavigationExpanded) 360.dp else 80.dp)

        Box(modifier = Modifier.requiredWidth(sideNavigationWidth)) {
          drawerContent(isSideNavigationExpanded)
        }

        Box(modifier = Modifier.weight(1f)) {
          MainScreenContent(
            navController = navController,
            homeContent = { homeContent(openDrawer) },
            searchContent = { searchContent(goBackToHome) },
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
            Box(modifier = Modifier.size(0.dp).focusable(true))

            drawerContent(true)
          }
        },
      ) {
        MainScreenContent(
          navController = navController,
          homeContent = { homeContent(openDrawer) },
          searchContent = { searchContent(goBackToHome) },
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
  navController: NavHostController,
  homeContent: @Composable () -> Unit,
  searchContent: @Composable () -> Unit,
  bookmarksContent: @Composable () -> Unit,
  settingsContent: @Composable () -> Unit,
  discoveryContent: @Composable () -> Unit,
) {
  NavHost(navController = navController, startDestination = Screen.MainHome) {
    composable<Screen.MainHome> { homeContent() }
    composable<Screen.MainSearch> { searchContent() }
    composable<Screen.MainBookmarks> { bookmarksContent() }
    composable<Screen.MainSettings> { settingsContent() }
    composable<Screen.MainDiscovery> { discoveryContent() }
  }
}
