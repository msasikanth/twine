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

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
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
import dev.sasikanth.rss.reader.feedhealth.FeedHealthViewModel
import dev.sasikanth.rss.reader.feedhealth.ui.FeedHealthScreen
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
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.ui.ImageViewerScreen
import dev.sasikanth.rss.reader.reader.ui.ReaderScreen
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.SettingsAppInfoScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsAppearanceScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsBehaviorScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsDataScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.settings.ui.SettingsServicesScreen
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

fun EntryProviderScope<NavKey>.placeholderScreen(
  modifier: Modifier = Modifier,
  placeholderViewModel: () -> PlaceholderViewModel,
  navigator: AppNavigator,
) {
  entry<Screen.Placeholder> {
    val viewModel = viewModel { placeholderViewModel() }
    PlaceholderScreen(
      modifier = modifier,
      viewModel = viewModel,
      navigateHome = { navigator.navigateToMain() },
      navigateOnboarding = { navigator.navigateToOnboarding() },
    )
  }
}

fun EntryProviderScope<NavKey>.onboardingScreen(
  onboardingViewModel: () -> OnboardingViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.Onboarding> {
    val viewModel = viewModel { onboardingViewModel() }
    OnboardingScreen(
      modifier = modifier,
      viewModel = viewModel,
      onOnboardingDone = { navigator.navigateToMain() },
      onNavigateToDiscovery = { navigator.navigate(Screen.Discovery(isFromOnboarding = true)) },
      onNavigateToAccountSelection = { navigator.navigate(Screen.AccountSelection) },
    )
  }
}

fun EntryProviderScope<NavKey>.accountSelectionScreen(
  accountSelectionViewModel: () -> AccountSelectionViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.AccountSelection> {
    val viewModel = viewModel { accountSelectionViewModel() }

    AccountSelectionScreen(
      modifier = modifier,
      viewModel = viewModel,
      onNavigateToHome = { navigator.navigateToMain(triggerSync = true) },
      onNavigateToDiscovery = { navigator.navigate(Screen.Discovery(isFromOnboarding = true)) },
      openPaywall = { navigator.navigate(Screen.Paywall()) },
      openFreshRssLogin = { navigator.navigate(Screen.FreshRssLogin) },
      openMinifluxLogin = { navigator.navigate(Screen.MinifluxLogin) },
      goBack = { navigator.goBack() },
    )
  }
}

fun EntryProviderScope<NavKey>.mainScreen(
  navigator: AppNavigator,
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
  entry<Screen.Main> { mainRoute ->
    val triggerSync = mainRoute.triggerSync
    val startTab = mainRoute.startTab
    val feedsViewModel = viewModel { feedsViewModel() }

    // Navigation 3 has no simple `hasRoute` backstack scanner for inside nested graphs since we
    // replaced it with Navigator
    // But mainScreen is almost always active when it's rendered, so canHandleBack is true.
    val isMainActive = true

    LaunchedEffect(useDarkTheme) {
      toggleLightStatusBar(!useDarkTheme)
      toggleLightNavBar(!useDarkTheme)
    }

    MainScreen(
      feedsViewModel = feedsViewModel,
      startTab = startTab,
      homeContent = { openDrawer ->
        val viewModel = viewModel { homeViewModel() }

        LaunchedEffect(Unit) {
          navigator.results
            .map { it[SELECTED_GROUPS_KEY] as? Set<String> }
            .filterNotNull()
            .onEach { selectedGroupIds ->
              if (selectedGroupIds.isNotEmpty()) {
                feedsViewModel.dispatch(FeedsEvent.OnGroupsSelected(selectedGroupIds))
                navigator.consumeResult(SELECTED_GROUPS_KEY)
              }
            }
            .launchIn(this)
        }

        LaunchedEffect(Unit) {
          feedsViewModel.state
            .map { it.openGroupSelection }
            .filterNotNull()
            .onEach { selectedGroupIds ->
              navigator.navigate(Modals.GroupSelection(selectedGroupIds.toList()))
              feedsViewModel.dispatch(FeedsEvent.MarkOpenGroupSelectionDone)
            }
            .launchIn(this)
        }

        HomeScreen(
          viewModel = viewModel,
          feedsViewModel = feedsViewModel,
          triggerSync = triggerSync,
          openPost = { index, post -> openPost(index, post, FromScreen.Home) },
          onMenuClicked = openDrawer,
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
          navigator.results
            .map { it[FRESH_RSS_LOGIN_SUCCESS_KEY] as? Boolean }
            .filterNotNull()
            .filter { it }
            .onEach {
              viewModel.dispatch(SettingsEvent.TriggerSync)
              navigator.consumeResult(FRESH_RSS_LOGIN_SUCCESS_KEY)
            }
            .launchIn(this)
        }
        LaunchedEffect(Unit) {
          navigator.results
            .map { it[MINIFLUX_LOGIN_SUCCESS_KEY] as? Boolean }
            .filterNotNull()
            .filter { it }
            .onEach {
              viewModel.dispatch(SettingsEvent.TriggerSync)
              navigator.consumeResult(MINIFLUX_LOGIN_SUCCESS_KEY)
            }
            .launchIn(this)
        }

        SettingsScreen(
          viewModel = viewModel,
          goBack = goBack,
          openAppearanceSettings = { navigator.navigate(Screen.SettingsAppearance) },
          openBehaviorSettings = { navigator.navigate(Screen.SettingsBehavior) },
          openServicesSettings = { navigator.navigate(Screen.SettingsServices) },
          openDataSettings = { navigator.navigate(Screen.SettingsData) },
          openAppInfoSettings = { navigator.navigate(Screen.SettingsAppInfo) },
          openPaywall = { navigator.navigate(Screen.Paywall()) },
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
      openFeedInfoSheet = { feedId -> navigator.navigate(Modals.FeedInfo(feedId)) },
      openGroupScreen = { groupId -> navigator.navigate(Screen.FeedGroup(groupId)) },
      openGroupSelectionSheet = { feedsViewModel.dispatch(FeedsEvent.OnAddToGroupClicked) },
      openAddFeedScreen = { navigator.navigate(Screen.AddFeed) },
      openPaywall = { navigator.navigate(Screen.Paywall()) },
      openFeedHealth = { navigator.navigate(Screen.FeedHealth) },
      canHandleBack = isMainActive,
    )
  }
}

fun EntryProviderScope<NavKey>.settingsAppearanceScreen(
  settingsViewModel: () -> SettingsViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.SettingsAppearance> {
    val viewModel = viewModel { settingsViewModel() }
    SettingsAppearanceScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openPaywall = { navigator.navigate(Screen.Paywall()) },
    )
  }
}

fun EntryProviderScope<NavKey>.settingsBehaviorScreen(
  settingsViewModel: () -> SettingsViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.SettingsBehavior> {
    val viewModel = viewModel { settingsViewModel() }
    SettingsBehaviorScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openBlockedWords = { navigator.navigate(Screen.BlockedWords) },
    )
  }
}

fun EntryProviderScope<NavKey>.settingsServicesScreen(
  settingsViewModel: () -> SettingsViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.SettingsServices> {
    val viewModel = viewModel { settingsViewModel() }
    SettingsServicesScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openPaywall = { navigator.navigate(Screen.Paywall()) },
      openFreshRssLogin = { navigator.navigate(Screen.FreshRssLogin) },
      openMinifluxLogin = { navigator.navigate(Screen.MinifluxLogin) },
    )
  }
}

fun EntryProviderScope<NavKey>.settingsDataScreen(
  statisticsViewModel: () -> StatisticsViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.SettingsData> {
    val viewModel = viewModel { statisticsViewModel() }
    SettingsDataScreen(
      modifier = modifier,
      statisticsViewModel = viewModel,
      goBack = { navigator.goBack() },
      openFeedHealth = { navigator.navigate(Screen.FeedHealth) },
    )
  }
}

fun EntryProviderScope<NavKey>.feedHealthScreen(
  feedHealthViewModel: () -> FeedHealthViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.FeedHealth> {
    val viewModel = viewModel { feedHealthViewModel() }
    FeedHealthScreen(viewModel = viewModel, goBack = { navigator.goBack() }, modifier = modifier)
  }
}

fun EntryProviderScope<NavKey>.settingsAppInfoScreen(
  settingsViewModel: () -> SettingsViewModel,
  openChangelog: () -> Unit,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.SettingsAppInfo> {
    val viewModel = viewModel { settingsViewModel() }
    SettingsAppInfoScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openAbout = { navigator.navigate(Screen.About) },
      openChangelog = openChangelog,
    )
  }
}

fun EntryProviderScope<NavKey>.freshRssLoginScreen(
  freshRssLoginViewModel: () -> FreshRssLoginViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.FreshRssLogin> {
    val viewModel = viewModel { freshRssLoginViewModel() }
    FreshRssLoginScreen(
      modifier = modifier,
      viewModel = viewModel,
      onLoginSuccess = {
        navigator.setResult(FRESH_RSS_LOGIN_SUCCESS_KEY, true)
        navigator.goBack()
      },
      goBack = { navigator.goBack() },
    )
  }
}

fun EntryProviderScope<NavKey>.minifluxLoginScreen(
  minifluxLoginViewModel: () -> MinifluxLoginViewModel,
  navigator: AppNavigator,
  modifier: Modifier = Modifier,
) {
  entry<Screen.MinifluxLogin> {
    val viewModel = viewModel { minifluxLoginViewModel() }
    MinifluxLoginScreen(
      modifier = modifier,
      viewModel = viewModel,
      onLoginSuccess = {
        navigator.setResult(MINIFLUX_LOGIN_SUCCESS_KEY, true)
        navigator.goBack()
      },
      goBack = { navigator.goBack() },
    )
  }
}

fun EntryProviderScope<NavKey>.readerScreen(
  readerViewModel: (SavedStateHandle) -> ReaderViewModel,
  readerPageViewModel: (ResolvedPost) -> ReaderPageViewModel,
  navigator: AppNavigator,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  entry<Screen.Reader> { route ->
    // Since we don't have NavBackStackEntry savedStateHandle easily,
    // ReaderViewModel doesn't receive args automatically via savedStateHandle.
    // We can manually pass it via factory.
    // However, `feedViewModel(it.savedStateHandle)` relies on it.
    // A quick hack is to inject ReaderScreenArgs or just let KMP viewModel provide an empty handle
    // and rely on a workaround, or better:
    // With entry<Screen.Reader>, `route` is already the decoded Screen.Reader object!
    // Since twine expects ReaderScreenArgs in the ViewModel from SavedStateHandle... wait!
    // `val viewModel = viewModel { readerViewModel(it.savedStateHandle) }`
    // We can create a fake SavedStateHandle containing ReaderScreenArgs:
    val savedStateHandle = SavedStateHandle(mapOf("readerScreenArgs" to route.readerScreenArgs))
    val viewModel = viewModel { readerViewModel(savedStateHandle) }

    ReaderScreen(
      viewModel = viewModel,
      pageViewModelFactory = { post -> viewModel(key = post.id) { readerPageViewModel(post) } },
      onBack = { navigator.goBack() },
      openPaywall = { navigator.navigate(Screen.Paywall()) },
      onImageClick = { imageUrl -> navigator.navigate(Screen.ImageViewer(imageUrl)) },
      toggleLightStatusBar = toggleLightStatusBar,
      toggleLightNavBar = toggleLightNavBar,
      modifier = modifier,
    )
  }
}

fun EntryProviderScope<NavKey>.addFeedScreen(
  addFeedViewModel: () -> AddFeedViewModel,
  navigator: AppNavigator,
  useDarkTheme: Boolean,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  entry<Screen.AddFeed> {
    val viewModel = viewModel { addFeedViewModel() }

    LaunchedEffect(Unit) {
      navigator.results
        .map { it[SELECTED_GROUPS_KEY] as? Set<String> }
        .filterNotNull()
        .onEach { selectedGroupIds ->
          if (selectedGroupIds.isNotEmpty()) {
            viewModel.dispatch(AddFeedEvent.OnGroupsSelected(selectedGroupIds))
            navigator.consumeResult(SELECTED_GROUPS_KEY)
          }
        }
        .launchIn(this)
    }

    LaunchedEffect(useDarkTheme) {
      toggleLightStatusBar(!useDarkTheme)
      toggleLightNavBar(!useDarkTheme)
    }

    AddFeedScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openGroupSelection = { selectedGroupIds ->
        navigator.navigate(Modals.GroupSelection(selectedGroupIds.toList()))
      },
      openDiscovery = { navigator.navigate(Screen.Discovery()) },
    )
  }
}

fun EntryProviderScope<NavKey>.discoveryScreen(
  discoveryViewModel: () -> DiscoveryViewModel,
  navigator: AppNavigator,
  screenModifier: Modifier = Modifier,
) {
  entry<Screen.Discovery> { discoveryRoute ->
    val viewModel = viewModel { discoveryViewModel() }
    val isFromOnboarding = discoveryRoute.isFromOnboarding
    val state by viewModel.state.collectAsStateWithLifecycle()

    DiscoveryScreen(
      viewModel = viewModel,
      showDoneButton = isFromOnboarding,
      onDone = {
        if (isFromOnboarding) {
          viewModel.completeOnboarding()
          navigator.navigateToMain()
          if (!state.isSubscribed) {
            navigator.navigate(Screen.Paywall(isFromOnboarding = true))
          }
        } else {
          navigator.goBack()
        }
      },
      goBack = { navigator.goBack() },
      modifier = screenModifier,
    )
  }
}

fun EntryProviderScope<NavKey>.aboutScreen(modifier: Modifier = Modifier, navigator: AppNavigator) {
  entry<Screen.About> { AboutScreen(modifier = modifier, goBack = { navigator.goBack() }) }
}

fun EntryProviderScope<NavKey>.feedGroupScreen(
  modifier: Modifier = Modifier,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  navigator: AppNavigator,
) {
  entry<Screen.FeedGroup> { feedGroupRoute ->
    val savedStateHandle = SavedStateHandle(mapOf("id" to feedGroupRoute.groupId))
    val viewModel = viewModel { groupViewModel(savedStateHandle) }

    LaunchedEffect(Unit) {
      navigator.results
        .map { it[SELECTED_GROUPS_KEY] as? Set<String> }
        .filterNotNull()
        .onEach { selectedGroupIds ->
          if (selectedGroupIds.isNotEmpty()) {
            viewModel.dispatch(GroupEvent.OnGroupsSelected(selectedGroupIds))
            navigator.consumeResult(SELECTED_GROUPS_KEY)
          }
        }
        .launchIn(this)
    }

    GroupScreen(
      modifier = modifier,
      viewModel = viewModel,
      goBack = { navigator.goBack() },
      openGroupSelection = { navigator.navigate(Modals.GroupSelection()) },
    )
  }
}

fun EntryProviderScope<NavKey>.blockedWordsScreen(
  modifier: Modifier = Modifier,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  navigator: AppNavigator,
) {
  entry<Screen.BlockedWords> {
    val viewModel = viewModel { blockedWordsViewModel() }
    BlockedWordsScreen(modifier = modifier, viewModel = viewModel, goBack = { navigator.goBack() })
  }
}

fun EntryProviderScope<NavKey>.paywallScreen(
  modifier: Modifier = Modifier,
  premiumPaywallViewModel: () -> PremiumPaywallViewModel,
  navigator: AppNavigator,
) {
  entry<Screen.Paywall> { paywallRoute ->
    val viewModel = viewModel { premiumPaywallViewModel() }
    val hasPremium by viewModel.hasPremium.collectAsStateWithLifecycle()
    val packages by viewModel.packages.collectAsStateWithLifecycle()
    val inProgress by viewModel.inProgress.collectAsStateWithLifecycle()
    val isFromOnboarding = paywallRoute.isFromOnboarding

    LaunchedEffect(hasPremium) {
      if (hasPremium) {
        delay(1000.milliseconds)
        navigator.goBack()
      }
    }

    PremiumPaywallScreen(
      modifier = modifier,
      packages = packages,
      inProgress = inProgress,
      hasPremium = hasPremium,
      isFromOnboarding = isFromOnboarding,
      onPurchase = viewModel::purchasePackage,
      onRestore = viewModel::restorePurchases,
      goBack = { navigator.goBack() },
    )
  }
}

fun EntryProviderScope<NavKey>.imageViewerScreen(
  navigator: AppNavigator,
  toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  entry<Screen.ImageViewer> { imageViewerRoute ->
    val imageUrl = imageViewerRoute.imageUrl
    ImageViewerScreen(
      modifier = modifier,
      imageUrl = imageUrl,
      onBack = { navigator.goBack() },
      toggleLightStatusBar = toggleLightStatusBar,
      toggleLightNavBar = toggleLightNavBar,
    )
  }
}

fun EntryProviderScope<NavKey>.feedInfoDialog(
  feedViewModel: (SavedStateHandle) -> FeedViewModel,
  navigator: AppNavigator,
) {
  entry<Modals.FeedInfo> { modalRoute ->
    val savedStateHandle = SavedStateHandle(mapOf("feedId" to modalRoute.feedId))
    val viewModel = viewModel { feedViewModel(savedStateHandle) }
    FeedInfoBottomSheet(feedViewModel = viewModel, dismiss = { navigator.goBack() })
  }
}

fun EntryProviderScope<NavKey>.groupSelectionDialog(
  groupSelectionViewModel: (SavedStateHandle) -> GroupSelectionViewModel,
  navigator: AppNavigator,
) {
  entry<Modals.GroupSelection> { modalRoute ->
    val savedStateHandle =
      SavedStateHandle(mapOf("selectedGroupIds" to modalRoute.selectedGroupIds))
    val viewModel = viewModel { groupSelectionViewModel(savedStateHandle) }
    GroupSelectionSheet(
      viewModel = viewModel,
      dismiss = { navigator.goBack() },
      onGroupsSelected = { selectedGroupIds ->
        navigator.setResult(SELECTED_GROUPS_KEY, selectedGroupIds)
        navigator.goBack()
      },
    )
  }
}
