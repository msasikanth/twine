/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.app

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
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
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feed.ui.FeedInfoBottomSheet
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.group.GroupEvent
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.group.ui.GroupScreen
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.groupselection.ui.GroupSelectionSheet
import dev.sasikanth.rss.reader.groupselection.ui.SELECTED_GROUPS_KEY
import dev.sasikanth.rss.reader.home.HomeEvent
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.home.ui.HomeScreen
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
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.search.ui.SearchScreen
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.settings.ui.SettingsScreen
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.SeedColorExtractor
import dev.sasikanth.rss.reader.ui.darkAppColorScheme
import dev.sasikanth.rss.reader.ui.lightAppColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import kotlin.reflect.typeOf
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
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
  homeViewModel: () -> HomeViewModel,
  feedsViewModel: () -> FeedsViewModel,
  readerViewModel: (SavedStateHandle) -> ReaderViewModel,
  readerPageViewModel: () -> ReaderPageViewModel,
  addFeedViewModel: () -> AddFeedViewModel,
  feedViewModel: (SavedStateHandle) -> FeedViewModel,
  groupSelectionViewModel: () -> GroupSelectionViewModel,
  searchViewModel: () -> SearchViewModel,
  bookmarksViewModel: () -> BookmarksViewModel,
  settingsViewModel: () -> SettingsViewModel,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  premiumPaywallViewModel: () -> PremiumPaywallViewModel,
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

  CompositionLocalProvider(
    LocalWindowSizeClass provides calculateWindowSizeClass(),
    LocalShareHandler provides shareHandler,
    LocalLinkHandler provides linkHandler,
    LocalDynamicColorState provides dynamicColorState,
    LocalShowFeedFavIconSetting provides appState.showFeedFavIcon,
    LocalSeedColorExtractor provides seedColorExtractor,
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

    LaunchedEffect(useDarkTheme) { onThemeChange(useDarkTheme) }

    LaunchedEffect(appState.homeViewMode) {
      if (appState.homeViewMode != HomeViewMode.Default) {
        dynamicColorState.reset()
      }
    }

    AppTheme(useDarkTheme = useDarkTheme) {
      val navController = rememberNavController()
      val fillMaxSizeModifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.extraLarge)

      DisposableEffect(Unit) {
        ExternalUriHandler.listener = { uri ->
          navController.handleDeepLink(
            NavDeepLinkRequest(uri = NavUri(uri), action = null, mimeType = null)
          )
        }

        onDispose { ExternalUriHandler.listener = null }
      }

      NavHost(
        navController = navController,
        startDestination = Screen.Home,
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
            modifier = fillMaxSizeModifier,
            viewModel = viewModel,
            navigateHome = {
              navController.navigate(Screen.Home) {
                popUpTo(Screen.Placeholder) { inclusive = true }
              }
            }
          )
        }

        composable<Screen.Home> {
          val viewModel = viewModel { homeViewModel() }
          val feedsViewModel = viewModel { feedsViewModel() }

          LaunchedEffect(Unit) {
            it.savedStateHandle
              .getStateFlow<Set<String>>(SELECTED_GROUPS_KEY, emptySet())
              .filterNotNull()
              .onEach { selectedGroupIds ->
                feedsViewModel.dispatch(FeedsEvent.OnGroupsSelected(selectedGroupIds))
              }
              .launchIn(this)
          }

          LaunchedEffect(Unit) {
            viewModel.dispatch(HomeEvent.UpdateVisibleItemIndex(appState.activePostIndex))
          }

          HomeScreen(
            modifier = Modifier.fillMaxSize(),
            useDarkTheme = useDarkTheme,
            viewModel = viewModel,
            feedsViewModel = feedsViewModel,
            onVisiblePostChanged = { index -> appViewModel.updateActivePostIndex(index) },
            openAddFeedScreen = { navController.navigate(Screen.AddFeed) },
            openFeedInfoSheet = { feedId -> navController.navigate(Modals.FeedInfo(feedId)) },
            openSearch = { navController.navigate(Screen.Search) },
            openBookmarks = { navController.navigate(Screen.Bookmarks) },
            openSettings = { navController.navigate(Screen.Settings) },
            openPost = { index, post ->
              coroutineScope.launch {
                openPost(
                  state = appState,
                  navController = navController,
                  index = index,
                  post = post,
                  linkHandler = linkHandler,
                  appViewModel = appViewModel
                )
              }
            },
            openGroupSelectionSheet = { navController.navigate(Modals.GroupSelection) },
            openGroupScreen = { groupId -> navController.navigate(Screen.FeedGroup(groupId)) },
            openPaywall = { navController.navigate(Screen.Paywall) },
            onBottomSheetStateChanged = { sheetValue ->
              val showDarkStatusBar =
                if (sheetValue == SheetValue.Expanded) {
                  true
                } else {
                  useDarkTheme
                }

              toggleLightStatusBar(showDarkStatusBar.not())
            },
            onBottomSheetHidden = { isHidden -> toggleLightNavBar(isHidden) },
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
            modifier = fillMaxSizeModifier,
            darkTheme = useDarkTheme,
            viewModel = viewModel,
            pageViewModel = { key -> viewModel(key = key) { readerPageViewModel() } },
            onPostChanged = { activePostIndex ->
              if (fromScreen !is FromScreen.UnreadWidget) {
                appViewModel.updateActivePostIndex(activePostIndex)
              }
            },
            onBack = { navController.popBackStack() },
            openPaywall = { navController.navigate(Screen.Paywall) }
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
                viewModel.dispatch(AddFeedEvent.OnGroupsSelected(selectedGroupIds))
              }
              .launchIn(this)
          }

          AddFeedScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            goBack = { navController.popBackStack() },
            openGroupSelection = { navController.navigate(Modals.GroupSelection) }
          )
        }

        composable<Screen.Search> {
          val viewModel = viewModel { searchViewModel() }
          SearchScreen(
            modifier = fillMaxSizeModifier,
            searchViewModel = viewModel,
            goBack = { navController.popBackStack() },
            openPost = { searchQuery, sortOrder, index, post ->
              coroutineScope.launch {
                if (appState.showReaderView) {
                  navController.navigate(
                    Screen.Reader(
                      readerScreenArgs =
                        ReaderScreenArgs(
                          postIndex = index,
                          postId = post.id,
                          fromScreen = FromScreen.Search(searchQuery, sortOrder)
                        )
                    )
                  )
                } else {
                  linkHandler.openLink(post.link)
                  appViewModel.markPostAsRead(post.id)
                }
              }
            }
          )
        }

        composable<Screen.Bookmarks> {
          val viewModel = viewModel { bookmarksViewModel() }

          BookmarksScreen(
            modifier = fillMaxSizeModifier,
            bookmarksViewModel = viewModel,
            goBack = { navController.popBackStack() },
            openPost = { index, post ->
              coroutineScope.launch {
                if (appState.showReaderView) {
                  navController.navigate(
                    Screen.Reader(
                      readerScreenArgs =
                        ReaderScreenArgs(
                          postIndex = index,
                          postId = post.id,
                          fromScreen = FromScreen.Bookmarks
                        )
                    )
                  )
                } else {
                  linkHandler.openLink(post.link)
                  appViewModel.markPostAsRead(post.id)
                }
              }
            }
          )
        }

        composable<Screen.Settings> {
          val viewModel = viewModel { settingsViewModel() }

          SettingsScreen(
            modifier = fillMaxSizeModifier,
            viewModel = viewModel,
            goBack = { navController.popBackStack() },
            openBlockedWords = { navController.navigate(Screen.BlockedWords) },
            openPaywall = { navController.navigate(Screen.Paywall) },
            openAbout = { navController.navigate(Screen.About) }
          )
        }

        composable<Screen.About> {
          AboutScreen(modifier = fillMaxSizeModifier, goBack = { navController.popBackStack() })
        }

        composable<Screen.FeedGroup> {
          val viewModel = viewModel { groupViewModel(it.savedStateHandle) }

          LaunchedEffect(Unit) {
            it.savedStateHandle
              .getStateFlow<Set<String>>(SELECTED_GROUPS_KEY, emptySet())
              .filterNotNull()
              .onEach { selectedGroupIds ->
                viewModel.dispatch(GroupEvent.OnGroupsSelected(selectedGroupIds))
              }
              .launchIn(this)
          }

          GroupScreen(
            modifier = Modifier.fillMaxSize(),
            viewModel = viewModel,
            goBack = { navController.popBackStack() },
            openGroupSelection = { navController.navigate(Modals.GroupSelection) }
          )
        }

        composable<Screen.BlockedWords> {
          val viewModel = viewModel { blockedWordsViewModel() }
          BlockedWordsScreen(
            modifier = fillMaxSizeModifier,
            viewModel = viewModel,
            goBack = { navController.popBackStack() }
          )
        }

        composable<Screen.Paywall> {
          val viewModel = viewModel { premiumPaywallViewModel() }
          val hasPremium by viewModel.hasPremium.collectAsStateWithLifecycle()

          PremiumPaywallScreen(
            modifier = fillMaxSizeModifier,
            hasPremium = hasPremium,
            goBack = { navController.popBackStack() }
          )
        }

        dialog<Modals.FeedInfo> {
          val viewModel = viewModel { feedViewModel(it.savedStateHandle) }
          FeedInfoBottomSheet(feedViewModel = viewModel, dismiss = { navController.popBackStack() })
        }

        dialog<Modals.GroupSelection> {
          val viewModel = viewModel { groupSelectionViewModel() }
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

// TODO: Move this to individual screens
private suspend fun openPost(
  state: AppState,
  navController: NavHostController,
  index: Int,
  post: PostWithMetadata,
  linkHandler: LinkHandler,
  appViewModel: AppViewModel,
) {
  if (state.showReaderView) {
    navController.navigate(
      Screen.Reader(
        ReaderScreenArgs(
          postIndex = index,
          postId = post.id,
          fromScreen = FromScreen.Home,
        )
      )
    )
  } else {
    linkHandler.openLink(post.link)
    appViewModel.run {
      updateActivePostIndex(index)
      markPostAsRead(post.id)
    }
  }
}
