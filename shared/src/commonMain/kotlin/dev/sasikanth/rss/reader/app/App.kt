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

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SheetValue
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavHostController
import androidx.navigation.NavUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import dev.sasikanth.rss.reader.about.ui.AboutScreen
import dev.sasikanth.rss.reader.addfeed.AddFeedEvent
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.addfeed.ui.AddFeedScreen
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsScreen
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsViewModel
import dev.sasikanth.rss.reader.bookmarks.BookmarksViewModel
import dev.sasikanth.rss.reader.bookmarks.ui.BookmarksScreen
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feed.ui.FeedInfoBottomSheet
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.freshrss.ui.FRESH_RSS_LOGIN_SUCCESS_KEY
import dev.sasikanth.rss.reader.freshrss.ui.FreshRssLoginScreen
import dev.sasikanth.rss.reader.group.GroupEvent
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.group.ui.GroupScreen
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.groupselection.ui.GroupSelectionSheet
import dev.sasikanth.rss.reader.groupselection.ui.SELECTED_GROUPS_KEY
import dev.sasikanth.rss.reader.home.HomeEvent
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
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.premium.PremiumPaywallScreen
import dev.sasikanth.rss.reader.premium.PremiumPaywallViewModel
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.ui.ReaderScreen
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.SettingsEvent
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareHandler
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import dev.sasikanth.rss.reader.statistics.ui.StatisticsScreen
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.SeedColorExtractor
import dev.sasikanth.rss.reader.ui.darkAppColorScheme
import dev.sasikanth.rss.reader.ui.lightAppColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalDynamicColorEnabled
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias App =
  @Composable
  (
    onThemeChange: (useDarkTheme: Boolean) -> Unit,
    toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
    toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
  ) -> Unit

@Inject
@Composable
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
fun App(
  shareHandler: ShareHandler,
  linkHandler: LinkHandler,
  imageLoader: ImageLoader,
  seedColorExtractor: SeedColorExtractor,
  appViewModel: () -> AppViewModel,
  placeholderViewModel: () -> PlaceholderViewModel,
  onboardingViewModel: () -> OnboardingViewModel,
  homeViewModel: () -> HomeViewModel,
  feedsViewModel: () -> FeedsViewModel,
  readerViewModel: (SavedStateHandle) -> ReaderViewModel,
  readerPageViewModel: (ResolvedPost) -> ReaderPageViewModel,
  addFeedViewModel: () -> AddFeedViewModel,
  feedViewModel: (SavedStateHandle) -> FeedViewModel,
  groupSelectionViewModel: (SavedStateHandle) -> GroupSelectionViewModel,
  searchViewModel: () -> SearchViewModel,
  bookmarksViewModel: () -> BookmarksViewModel,
  settingsViewModel: () -> SettingsViewModel,
  freshRssLoginViewModel: () -> dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel,
  minifluxLoginViewModel: () -> MinifluxLoginViewModel,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  premiumPaywallViewModel: () -> PremiumPaywallViewModel,
  statisticsViewModel: () -> StatisticsViewModel,
  @Assisted onThemeChange: (useDarkTheme: Boolean) -> Unit,
  @Assisted toggleLightStatusBar: (isLightStatusBar: Boolean) -> Unit,
  @Assisted toggleLightNavBar: (isLightNavBar: Boolean) -> Unit,
) {
  setSingletonImageLoaderFactory { imageLoader }

  val appViewModel = viewModel { appViewModel() }
  val appState by appViewModel.state.collectAsStateWithLifecycle()
  val dynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = lightAppColorScheme(),
      defaultDarkAppColorScheme = darkAppColorScheme(),
    )
  val coroutineScope = rememberCoroutineScope()
  val windowInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)

  CompositionLocalProvider(
    LocalWindowSizeClass provides windowInfo.windowSizeClass,
    LocalShareHandler provides shareHandler,
    LocalLinkHandler provides linkHandler,
    LocalDynamicColorState provides dynamicColorState,
    LocalDynamicColorEnabled provides appState.dynamicColorEnabled,
    LocalShowFeedFavIconSetting provides appState.showFeedFavIcon,
    LocalSeedColorExtractor provides seedColorExtractor,
    LocalBlockImage provides appState.blockImages,
    LocalAmoledSetting provides appState.useAmoled
  ) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme =
      remember(isSystemInDarkTheme, appState.appThemeMode) {
        when (appState.appThemeMode) {
          AppThemeMode.Light -> false
          AppThemeMode.Dark -> true
          AppThemeMode.Auto -> isSystemInDarkTheme
        }
      }
    val navController = rememberNavController()
    val openPost: (Int, ResolvedPost, FromScreen) -> Unit =
      remember(navController, linkHandler, appViewModel, coroutineScope) {
        { index, post, fromScreen ->
          navController.navigateToReaderOrOpenLink(
            showReader = appState.showReaderView,
            index = index,
            post = post,
            fromScreen = fromScreen,
            openLink = {
              coroutineScope.launch {
                linkHandler.openLink(post.link)
                appViewModel.onPostOpened(post.id, index)
              }
            }
          )
        }
      }
    val screenCornerRadius =
      if (platform == Platform.Apple) {
        38.dp
      } else {
        24.dp
      }
    val screenModifier = Modifier.fillMaxSize()
    val roundedCornerScreenModifier = screenModifier.clip(RoundedCornerShape(screenCornerRadius))

    LaunchedEffect(useDarkTheme) { onThemeChange(useDarkTheme) }

    LaunchedEffect(appState.homeViewMode, appState.dynamicColorEnabled) {
      if (appState.homeViewMode == HomeViewMode.Default && appState.dynamicColorEnabled) {
        dynamicColorState.refresh()
      } else {
        dynamicColorState.reset()
      }
    }

    DisposableEffect(Unit) {
      ExternalUriHandler.listener = { uri ->
        if (uri.startsWith("twine://oauth")) {
          appViewModel.onOAuthRedirect(uri)
        } else {
          val deepLinkRequest =
            NavDeepLinkRequest(uri = NavUri(uri), action = null, mimeType = null)
          if (navController.graph.hasDeepLink(deepLinkRequest)) {
            if (!navController.popBackStack(Screen.Main, inclusive = false)) {
              navController.navigate(Screen.Main) {
                popUpTo(Screen.Placeholder) { inclusive = true }
                launchSingleTop = true
              }
            }

            navController.navigate(NavUri(uri))
          }
        }
      }

      onDispose { ExternalUriHandler.listener = null }
    }

    AppTheme(useDarkTheme = useDarkTheme) {
      NavHost(
        navController = navController,
        startDestination = Screen.Placeholder,
        popEnterTransition = { EnterTransition.None },
        popExitTransition = {
          scaleOut(
            targetScale = 0.9f,
            transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f)
          )
        }
      ) {
        composable<Screen.Placeholder> {
          val viewModel = viewModel { placeholderViewModel() }
          PlaceholderScreen(
            modifier = roundedCornerScreenModifier,
            viewModel = viewModel,
            navigateHome = {
              navController.navigate(Screen.Main) {
                popUpTo(Screen.Placeholder) { inclusive = true }
              }
            },
            navigateOnboarding = {
              navController.navigate(Screen.Onboarding) {
                popUpTo(Screen.Placeholder) { inclusive = true }
              }
            }
          )
        }

        composable<Screen.Onboarding> {
          val viewModel = viewModel { onboardingViewModel() }
          OnboardingScreen(
            viewModel = viewModel,
            onOnboardingDone = {
              navController.navigate(Screen.Main) {
                popUpTo(Screen.Onboarding) { inclusive = true }
              }
            }
          )
        }

        composable<Screen.Main> {
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

              LaunchedEffect(Unit) {
                viewModel.dispatch(HomeEvent.UpdateVisibleItemIndex(appState.activePostIndex))
              }

              HomeScreen(
                viewModel = viewModel,
                feedsViewModel = feedsViewModel,
                onVisiblePostChanged = { index -> appViewModel.updateActivePostIndex(index) },
                openPost = { index, post -> openPost(index, post, FromScreen.Home) },
                openGroupSelectionSheet = {
                  feedsViewModel.dispatch(FeedsEvent.OnAddToGroupClicked)
                },
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
                onScrolledToTop = { appViewModel.updateActivePostIndex(0) },
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
                modifier = screenModifier
              )
            },
            bookmarksContent = { goBack ->
              val viewModel = viewModel { bookmarksViewModel() }

              BookmarksScreen(
                bookmarksViewModel = viewModel,
                goBack = goBack,
                openPost = { index, post -> openPost(index, post, FromScreen.Bookmarks) },
                modifier = screenModifier
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
                      }
                      ?: emptyFlow(),
                    navController.currentBackStackEntry
                      ?.savedStateHandle
                      ?.getStateFlow(MINIFLUX_LOGIN_SUCCESS_KEY, false)
                      ?.filter { it }
                      ?.onEach {
                        navController.currentBackStackEntry
                          ?.savedStateHandle
                          ?.set(MINIFLUX_LOGIN_SUCCESS_KEY, false)
                      }
                      ?: emptyFlow()
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
                modifier = screenModifier
              )
            }
          )
        }

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
            goBack = { navController.popBackStack() }
          )
        }

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
            goBack = { navController.popBackStack() }
          )
        }

        composable<Screen.Reader>(
          typeMap = mapOf(typeOf<ReaderScreenArgs>() to ReaderScreenArgs.navTypeMap),
          deepLinks =
            listOf(
              navDeepLink<Screen.Reader>(
                basePath = Screen.Reader.ROUTE,
                typeMap = mapOf(typeOf<ReaderScreenArgs>() to ReaderScreenArgs.navTypeMap)
              )
            )
        ) {
          val viewModel = viewModel { readerViewModel(it.savedStateHandle) }
          val fromScreen = it.toRoute<Screen.Reader>().readerScreenArgs.fromScreen

          ReaderScreen(
            viewModel = viewModel,
            pageViewModelFactory = { post ->
              viewModel(key = post.id) { readerPageViewModel(post) }
            },
            onPostChanged = { activePostIndex ->
              if (fromScreen !is FromScreen.UnreadWidget) {
                appViewModel.updateActivePostIndex(activePostIndex)
              }
            },
            onBack = { navController.popBackStack() },
            openPaywall = { navController.navigate(Screen.Paywall) },
            modifier = roundedCornerScreenModifier
          )
        }

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

          LaunchedEffect(Unit) {
            toggleLightStatusBar(!useDarkTheme)
            toggleLightNavBar(!useDarkTheme)
          }

          AddFeedScreen(
            viewModel = viewModel,
            goBack = { navController.popBackStack() },
            openGroupSelection = { selectedGroupIds ->
              navController.navigate(Modals.GroupSelection(selectedGroupIds.toList()))
            }
          )
        }

        composable<Screen.About> {
          AboutScreen(
            modifier = roundedCornerScreenModifier,
            goBack = { navController.popBackStack() }
          )
        }

        composable<Screen.Statistics> {
          val viewModel = viewModel { statisticsViewModel() }
          StatisticsScreen(
            modifier = roundedCornerScreenModifier,
            viewModel = viewModel,
            goBack = { navController.popBackStack() }
          )
        }

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
            modifier = roundedCornerScreenModifier,
            viewModel = viewModel,
            goBack = { navController.popBackStack() },
            openGroupSelection = { navController.navigate(Modals.GroupSelection()) }
          )
        }

        composable<Screen.BlockedWords> {
          val viewModel = viewModel { blockedWordsViewModel() }
          BlockedWordsScreen(
            modifier = roundedCornerScreenModifier,
            viewModel = viewModel,
            goBack = { navController.popBackStack() }
          )
        }

        composable<Screen.Paywall> {
          val viewModel = viewModel { premiumPaywallViewModel() }
          val hasPremium by viewModel.hasPremium.collectAsStateWithLifecycle()

          PremiumPaywallScreen(
            modifier = roundedCornerScreenModifier,
            hasPremium = hasPremium,
            goBack = { navController.popBackStack() }
          )
        }

        dialog<Modals.FeedInfo> {
          val viewModel = viewModel { feedViewModel(it.savedStateHandle) }
          FeedInfoBottomSheet(feedViewModel = viewModel, dismiss = { navController.popBackStack() })
        }

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
            }
          )
        }
      }
    }
  }
}

private fun NavHostController.navigateToReaderOrOpenLink(
  showReader: Boolean,
  index: Int,
  post: ResolvedPost,
  fromScreen: FromScreen,
  openLink: () -> Unit,
) {
  if (showReader) {
    val route =
      Screen.Reader(
        ReaderScreenArgs(
          postIndex = index,
          postId = post.id,
          fromScreen = fromScreen,
        )
      )

    navigate(route)
  } else {
    openLink()
  }
}
