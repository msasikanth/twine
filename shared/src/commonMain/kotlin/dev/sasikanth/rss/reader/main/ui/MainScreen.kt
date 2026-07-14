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

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.app.AppNavigator
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalRootWindowSizeClass
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed

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
  openFeedHealth: () -> Unit,
  isSideNavigationExpanded: Boolean,
  setSideNavigationExpanded: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  canHandleBack: Boolean = true,
  startTab: String? = null,
) {
  // Navigation (rail/drawer) tiers follow the window, not the pane, so in a list-detail
  // split the side navigation stays inline in a row instead of overlaying the posts list.
  val sizeClass = LocalRootWindowSizeClass.current
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  val config = remember {
    SavedStateConfiguration {
      serializersModule = SerializersModule {
        polymorphic(NavKey::class) { subclassesOfSealed<Screen>() }
      }
    }
  }

  val backStack = rememberNavBackStack(config, Screen.MainHome)
  val navigator = remember { AppNavigator(backStack) }

  val currentDestination = backStack.lastOrNull()

  val selectedDestination =
    when (currentDestination) {
      is Screen.MainHome -> MainDestination.Home
      is Screen.MainSearch -> MainDestination.Search
      is Screen.MainBookmarks -> MainDestination.Bookmarks
      is Screen.MainSettings -> MainDestination.Settings
      is Screen.MainDiscovery -> MainDestination.Discovery
      else -> MainDestination.Home
    }

  LaunchedEffect(startTab) {
    if (startTab == Screen.Main.TAB_BOOKMARKS) {
      navigator.popUpTo(Screen.MainHome::class, inclusive = true)
      navigator.navigate(Screen.MainBookmarks)
    }
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
        setSideNavigationExpanded(false)
      }
      else -> {
        navigator.popUpTo(Screen.MainHome::class, inclusive = false)
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
        setSideNavigationExpanded(!isSideNavigationExpanded)
      }
      else -> {
        scope.launch { drawerState.open() }
      }
    }
    Unit
  }

  val goBackToHome = {
    if (selectedDestination != MainDestination.Home) {
      navigator.popUpTo(Screen.MainHome::class, inclusive = false)
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

            // pop up to start destination, launch single top, restore state
            // in navigation 3 with AppNavigator, we can simulate this:
            navigator.popUpTo(Screen.MainHome::class, inclusive = false)
            if (route != Screen.MainHome) {
              navigator.navigate(route)
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
        openFeedHealth = openFeedHealth,
        closeDrawer = {
          when {
            sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
              setSideNavigationExpanded(false)
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
    sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> {
      Row(modifier = modifier.fillMaxSize()) {
        val sideNavigationWidth by animateDpAsState(if (isSideNavigationExpanded) 360.dp else 80.dp)

        Box(modifier = Modifier.requiredWidth(sideNavigationWidth)) {
          drawerContent(isSideNavigationExpanded)
        }

        Box(modifier = Modifier.weight(1f)) {
          MainScreenContent(
            navigator = navigator,
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
          navigator = navigator,
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
  navigator: AppNavigator,
  homeContent: @Composable () -> Unit,
  searchContent: @Composable () -> Unit,
  bookmarksContent: @Composable () -> Unit,
  settingsContent: @Composable () -> Unit,
  discoveryContent: @Composable () -> Unit,
) {
  val entryProvider =
    entryProvider<NavKey> {
      entry<Screen.MainHome> { homeContent() }
      entry<Screen.MainSearch> { searchContent() }
      entry<Screen.MainBookmarks> { bookmarksContent() }
      entry<Screen.MainSettings> { settingsContent() }
      entry<Screen.MainDiscovery> { discoveryContent() }
    }

  NavDisplay(
    backStack = navigator.backStack,
    entryProvider = entryProvider,
    onBack = { navigator.goBack() },
  )
}
