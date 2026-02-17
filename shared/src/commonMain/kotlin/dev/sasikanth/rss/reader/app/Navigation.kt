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

package dev.sasikanth.rss.reader.app

import androidx.compose.material3.SheetValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import dev.sasikanth.rss.reader.about.ui.AboutScreen
import dev.sasikanth.rss.reader.accountselection.AccountSelectionViewModel
import dev.sasikanth.rss.reader.accountselection.ui.AccountSelectionScreen
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.addfeed.ui.AddFeedScreen
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsScreen
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsViewModel
import dev.sasikanth.rss.reader.bookmarks.BookmarksViewModel
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.discovery.DiscoveryViewModel
import dev.sasikanth.rss.reader.discovery.ui.DiscoveryScreen
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feed.ui.FeedInfoBottomSheet
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel
import dev.sasikanth.rss.reader.freshrss.ui.FRESH_RSS_LOGIN_SUCCESS_KEY
import dev.sasikanth.rss.reader.freshrss.ui.FreshRssLoginScreen
import dev.sasikanth.rss.reader.group.GroupEvent
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.group.ui.GroupScreen
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.groupselection.ui.GroupSelectionSheet
import dev.sasikanth.rss.reader.groupselection.ui.SELECTED_GROUPS_KEY
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.home.ui.HomeScreen
import dev.sasikanth.rss.reader.main.ui.MainScreen
import dev.sasikanth.rss.reader.miniflux.MinifluxLoginViewModel
import dev.sasikanth.rss.reader.miniflux.ui.MINIFLUX_LOGIN_SUCCESS_KEY
import dev.sasikanth.rss.reader.miniflux.ui.MinifluxLoginScreen
import dev.sasikanth.rss.reader.onboarding.OnboardingViewModel
import dev.sasikanth.rss.reader.onboarding.ui.OnboardingScreen
import dev.sasikanth.rss.reader.placeholder.PlaceholderScreen
import dev.sasikanth.rss.reader.placeholder.PlaceholderViewModel
import dev.sasikanth.rss.reader.premium.PremiumPaywallScreen
import dev.sasikanth.rss.reader.premium.PremiumPaywallViewModel
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.ui.ReaderScreen
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import dev.sasikanth.rss.reader.statistics.ui.StatisticsScreen
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

fun NavGraphBuilder.placeholderScreen(
  modifier: Modifier = Modifier,
  placeholderViewModel: () -> PlaceholderViewModel,
  navController: NavHostController,
) {
  composable<Screen.Placeholder> {
    val viewModel = viewModel { placeholderViewModel() }
    PlaceholderScreen(
      modifier = modifier,
      viewModel = viewModel,
      navigateHome = {
        navController.navigate(Screen.Main()) { popUpTo<Screen.Placeholder> { inclusive = true } }
      },
      navigateOnboarding = {
        navController.navigate(Screen.Onboarding) {
          popUpTo<Screen.Placeholder> { inclusive = true }
        }
      },
    )
  }
}

fun NavGraphBuilder.onboardingScreen(
  onboardingViewModel: () -> OnboardingViewModel,
  navController: NavHostController,
) {
  composable<Screen.Onboarding> {
    val viewModel = viewModel { onboardingViewModel() }
    OnboardingScreen(
      viewModel = viewModel,
      onOnboardingDone = {
        navController.navigate(Screen.Main()) { popUpTo<Screen.Onboarding> { inclusive = true } }
      },
      onNavigateToDiscovery = {
        navController.navigate(Screen.Discovery(isFromOnboarding = true)) {
          popUpTo<Screen.Onboarding> { inclusive = true }
        }
      },
      onNavigateToAccountSelection = {
        navController.navigate(Screen.AccountSelection) {
          popUpTo<Screen.Onboarding> { inclusive = true }
        }
      },
    )
  }
}

fun NavGraphBuilder.accountSelectionScreen(
  accountSelectionViewModel: () -> AccountSelectionViewModel,
  navController: NavHostController,
) {
  composable<Screen.AccountSelection> {
    val viewModel = viewModel { accountSelectionViewModel() }

    AccountSelectionScreen(
      viewModel = viewModel,
      onNavigateToHome = {
        navController.navigate(Screen.Main(triggerSync = true)) {
          popUpTo<Screen.AccountSelection> { inclusive = true }
        }
      },
      onNavigateToDiscovery = {
        navController.navigate(Screen.Discovery(isFromOnboarding = true)) {
          popUpTo<Screen.AccountSelection> { inclusive = true }
        }
      },
      openPaywall = { navController.navigate(Screen.Paywall) },
      openFreshRssLogin = { navController.navigate(Screen.FreshRssLogin) },
      openMinifluxLogin = { navController.navigate(Screen.MinifluxLogin) },
    )
  }
}

fun NavGraphBuilder.mainScreen(
  navController: NavHostController,
  useDarkTheme: Boolean,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  homeViewModel: () -> HomeViewModel,
  feedsViewModel: () -> FeedsViewModel,
  searchViewModel: () -> SearchViewModel,
  bookmarksViewModel: () -> BookmarksViewModel,
  settingsViewModel: () -> SettingsViewModel,
  discoveryViewModel: () -> DiscoveryViewModel,
  openPost: (Int, ResolvedPost, FromScreen) -> Unit,
  screenModifier: Modifier,
) {
  composable<Screen.Main> {
    val triggerSync = it.toRoute<Screen.Main>().triggerSync

    LaunchedEffect(useDarkTheme) {
      toggleLightStatusBar(!useDarkTheme)
      toggleLightNavBar(!useDarkTheme)
    }

    MainScreen(
      homeContent = { openDrawer ->
        val viewModel = viewModel { homeViewModel() }
        val feedsViewModel = viewModel { feedsViewModel() }

        LaunchedEffect(Unit) {
          it.savedStateHandle
            .getStateFlow<Set<String>>(SELECTED_GROUPS_KEY, emptySet())
            .filterNotNull()
            .onEach { selectedGroupIds ->
              if (selectedGroupIds.isNotEmpty()) {
                feedsViewModel.dispatch(FeedsEvent.OnGroupsSelected(selectedGroupIds))
                it.savedStateHandle[SELECTED_GROUPS_KEY] = emptySet<String>()
              }
            }
            .launchIn(this)
        }

        LaunchedEffect(Unit) {
          feedsViewModel.state
            .map { it.openGroupSelection }
            .filterNotNull()
            .onEach { selectedGroupIds ->
              navController.navigate(Modals.GroupSelection(selectedGroupIds.toList()))
              feedsViewModel.dispatch(FeedsEvent.MarkOpenGroupSelectionDone)
            }
            .launchIn(this)
        }

        HomeScreen(
          viewModel = viewModel,
          feedsViewModel = feedsViewModel,
          triggerSync = triggerSync,
          openPost = { index, post -> openPost(index, post, FromScreen.Home) },
          openGroupSelectionSheet = { feedsViewModel.dispatch(FeedsEvent.OnAddToGroupClicked) },
          openFeedInfoSheet = { feedId -> navController.navigate(Modals.FeedInfo(feedId)) },
          openAddFeedScreen = { navController.navigate(Screen.AddFeed) },
          openGroupScreen = { groupId -> navController.navigate(Screen.FeedGroup(groupId)) },
          openPaywall = { navController.navigate(Screen.Paywall) },
          onMenuClicked = openDrawer,
          onBottomSheetStateChanged = { sheetValue ->
            val showDarkSystemBars =
              if (sheetValue == SheetValue.Expanded) {
                true
              } else {
                useDarkTheme
              }

            toggleLightStatusBar(!showDarkSystemBars)
            toggleLightNavBar(!showDarkSystemBars)
          },
          modifier = screenModifier,
        )
      },
      searchContent = { goBack ->
        val viewModel = viewModel { searchViewModel() }

        SearchScreen(
          searchViewModel = viewModel,
          goBack = goBack,
          openPost = { searchQuery, sortOrder, index, post ->
            openPost(index, post, FromScreen.Search(searchQuery, sortOrder))
          },
          modifier = screenModifier,
        )
      },
      bookmarksContent = { goBack ->
        val viewModel = viewModel { bookmarksViewModel() }

        BookmarksScreen(
          bookmarksViewModel = viewModel,
          goBack = goBack,
          openPost = { index, post -> openPost(index, post, FromScreen.Bookmarks) },
          modifier = screenModifier,
        )
      },
      settingsContent = { goBack ->
        val viewModel = viewModel { settingsViewModel() }

        LaunchedEffect(Unit) {
          merge(
              navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow(FRESH_RSS_LOGIN_SUCCESS_KEY, false)
                ?.filter { it }
                ?.onEach {
                  navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(FRESH_RSS_LOGIN_SUCCESS_KEY, false)
                } ?: emptyFlow(),
              navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getStateFlow(MINIFLUX_LOGIN_SUCCESS_KEY, false)
                ?.filter { it }
                ?.onEach {
                  navController.currentBackStackEntry
                    ?.savedStateHandle
                    ?.set(MINIFLUX_LOGIN_SUCCESS_KEY, false)
                } ?: emptyFlow(),
            )
            .onEach { viewModel.dispatch(SettingsEvent.TriggerSync) }
            .launchIn(this)
        }

        SettingsScreen(
          viewModel = viewModel,
          goBack = goBack,
          openAbout = { navController.navigate(Screen.About) },
          openStatistics = { navController.navigate(Screen.Statistics) },
          openBlockedWords = { navController.navigate(Screen.BlockedWords) },
          openPaywall = { navController.navigate(Screen.Paywall) },
          openFreshRssLogin = { navController.navigate(Screen.FreshRssLogin) },
          openMinifluxLogin = { navController.navigate(Screen.MinifluxLogin) },
          modifier = screenModifier,
        )
      },
      discoveryContent = { goBack ->
        val viewModel = viewModel { discoveryViewModel() }

        DiscoveryScreen(
          viewModel = viewModel,
          showDoneButton = false,
          goBack = goBack,
          modifier = screenModifier,
        )
      },
    )
  }
}

fun NavGraphBuilder.freshRssLoginScreen(
  freshRssLoginViewModel: () -> FreshRssLoginViewModel,
  navController: NavHostController,
) {
  composable<Screen.FreshRssLogin> {
    val viewModel = viewModel { freshRssLoginViewModel() }
    FreshRssLoginScreen(
      viewModel = viewModel,
      onLoginSuccess = {
        navController.previousBackStackEntry
          ?.savedStateHandle
          ?.set(FRESH_RSS_LOGIN_SUCCESS_KEY, true)
        navController.popBackStack()
      },
      goBack = { navController.popBackStack() },
    )
  }
}

fun NavGraphBuilder.minifluxLoginScreen(
  minifluxLoginViewModel: () -> MinifluxLoginViewModel,
  navController: NavHostController,
) {
  composable<Screen.MinifluxLogin> {
    val viewModel = viewModel { minifluxLoginViewModel() }
    MinifluxLoginScreen(
      viewModel = viewModel,
      onLoginSuccess = {
        navController.previousBackStackEntry
          ?.savedStateHandle
          ?.set(MINIFLUX_LOGIN_SUCCESS_KEY, true)
        navController.popBackStack()
      },
      goBack = { navController.popBackStack() },
    )
  }
}

fun NavGraphBuilder.readerScreen(
  readerViewModel: (SavedStateHandle) -> ReaderViewModel,
  readerPageViewModel: (ResolvedPost) -> ReaderPageViewModel,
  navController: NavHostController,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  composable<Screen.Reader>(
    typeMap = mapOf(typeOf<ReaderScreenArgs>() to ReaderScreenArgs.navTypeMap),
    deepLinks =
      listOf(
        navDeepLink<Screen.Reader>(
          basePath = Screen.Reader.ROUTE,
          typeMap = mapOf(typeOf<ReaderScreenArgs>() to ReaderScreenArgs.navTypeMap),
        )
      ),
  ) {
    val viewModel = viewModel { readerViewModel(it.savedStateHandle) }

    ReaderScreen(
      viewModel = viewModel,
      pageViewModelFactory = { post -> viewModel(key = post.id) { readerPageViewModel(post) } },
      onBack = { navController.popBackStack() },
      openPaywall = { navController.navigate(Screen.Paywall) },
      toggleLightStatusBar = toggleLightStatusBar,
      toggleLightNavBar = toggleLightNavBar,
      modifier = modifier,
    )
  }
}

fun NavGraphBuilder.addFeedScreen(
  addFeedViewModel: () -> AddFeedViewModel,
  navController: NavHostController,
  useDarkTheme: Boolean,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
) {
  composable<Screen.AddFeed>(
    deepLinks = listOf(navDeepLink<Screen.AddFeed>(basePath = Screen.AddFeed.ROUTE))
  ) {
    val viewModel = viewModel { addFeedViewModel() }

    LaunchedEffect(Unit) {
      it.savedStateHandle
        .getStateFlow<Set<String>>(SELECTED_GROUPS_KEY, emptySet())
        .filterNotNull()
        .onEach { selectedGroupIds ->
          if (selectedGroupIds.isNotEmpty()) {
            viewModel.dispatch(AddFeedEvent.OnGroupsSelected(selectedGroupIds))
            it.savedStateHandle[SELECTED_GROUPS_KEY] = emptySet<String>()
          }
        }
        .launchIn(this)
    }

    LaunchedEffect(useDarkTheme) {
      toggleLightStatusBar(!useDarkTheme)
      toggleLightNavBar(!useDarkTheme)
    }

    AddFeedScreen(
      viewModel = viewModel,
      goBack = { navController.popBackStack() },
      openGroupSelection = { selectedGroupIds ->
        navController.navigate(Modals.GroupSelection(selectedGroupIds.toList()))
      },
      openDiscovery = { navController.navigate(Screen.Discovery()) },
    )
  }
}

fun NavGraphBuilder.discoveryScreen(
  discoveryViewModel: () -> DiscoveryViewModel,
  navController: NavHostController,
  screenModifier: Modifier = Modifier,
) {
  composable<Screen.Discovery> {
    val viewModel = viewModel { discoveryViewModel() }
    val isFromOnboarding = it.toRoute<Screen.Discovery>().isFromOnboarding

    DiscoveryScreen(
      viewModel = viewModel,
      showDoneButton = isFromOnboarding,
      onDone = {
        if (isFromOnboarding) {
          navController.navigate(Screen.Main()) { popUpTo<Screen.Discovery> { inclusive = true } }
        } else {
          navController.popBackStack()
        }
      },
      goBack = { navController.popBackStack() },
      modifier = screenModifier,
    )
  }
}

fun NavGraphBuilder.aboutScreen(modifier: Modifier = Modifier, navController: NavHostController) {
  composable<Screen.About> {
    AboutScreen(modifier = modifier, goBack = { navController.popBackStack() })
  }
}

fun NavGraphBuilder.statisticsScreen(
  modifier: Modifier = Modifier,
  statisticsViewModel: () -> StatisticsViewModel,
  navController: NavHostController,
) {
  composable<Screen.Statistics> {
    val viewModel = viewModel { statisticsViewModel() }
    StatisticsScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navController.popBackStack() },
    )
  }
}

fun NavGraphBuilder.feedGroupScreen(
  modifier: Modifier = Modifier,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  navController: NavHostController,
) {
  composable<Screen.FeedGroup> {
    val viewModel = viewModel { groupViewModel(it.savedStateHandle) }

    LaunchedEffect(Unit) {
      it.savedStateHandle
        .getStateFlow<Set<String>>(SELECTED_GROUPS_KEY, emptySet())
        .filterNotNull()
        .onEach { selectedGroupIds ->
          if (selectedGroupIds.isNotEmpty()) {
            viewModel.dispatch(GroupEvent.OnGroupsSelected(selectedGroupIds))
            it.savedStateHandle[SELECTED_GROUPS_KEY] = emptySet<String>()
          }
        }
        .launchIn(this)
    }

    GroupScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navController.popBackStack() },
      openGroupSelection = { navController.navigate(Modals.GroupSelection()) },
    )
  }
}

fun NavGraphBuilder.blockedWordsScreen(
  modifier: Modifier = Modifier,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  navController: NavHostController,
) {
  composable<Screen.BlockedWords> {
    val viewModel = viewModel { blockedWordsViewModel() }
    BlockedWordsScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navController.popBackStack() },
    )
  }
}

fun NavGraphBuilder.paywallScreen(
  modifier: Modifier = Modifier,
  premiumPaywallViewModel: () -> PremiumPaywallViewModel,
  navController: NavHostController,
) {
  composable<Screen.Paywall> {
    val viewModel = viewModel { premiumPaywallViewModel() }
    val hasPremium by viewModel.hasPremium.collectAsStateWithLifecycle()

    PremiumPaywallScreen(
      modifier = modifier,
      hasPremium = hasPremium,
      goBack = { navController.popBackStack() },
    )
  }
}

fun NavGraphBuilder.feedInfoDialog(
  feedViewModel: (SavedStateHandle) -> FeedViewModel,
  navController: NavHostController,
) {
  dialog<Modals.FeedInfo> {
    val viewModel = viewModel { feedViewModel(it.savedStateHandle) }
    FeedInfoBottomSheet(feedViewModel = viewModel, dismiss = { navController.popBackStack() })
  }
}

fun NavGraphBuilder.groupSelectionDialog(
  groupSelectionViewModel: (SavedStateHandle) -> GroupSelectionViewModel,
  navController: NavHostController,
) {
  dialog<Modals.GroupSelection> {
    val viewModel = viewModel { groupSelectionViewModel(it.savedStateHandle) }
    GroupSelectionSheet(
      viewModel = viewModel,
      dismiss = { navController.popBackStack() },
      onGroupsSelected = { selectedGroupIds ->
        navController.previousBackStackEntry
          ?.savedStateHandle
          ?.set(SELECTED_GROUPS_KEY, selectedGroupIds)

        navController.popBackStack()
      },
    )
  }
}
