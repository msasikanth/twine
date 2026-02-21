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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import dev.sasikanth.rss.reader.accountselection.AccountSelectionViewModel
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsViewModel
import dev.sasikanth.rss.reader.bookmarks.BookmarksViewModel
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.discovery.DiscoveryViewModel
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.miniflux.MinifluxLoginViewModel
import dev.sasikanth.rss.reader.onboarding.OnboardingViewModel
import dev.sasikanth.rss.reader.placeholder.PlaceholderViewModel
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.premium.PremiumPaywallViewModel
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.search.SearchViewModel
import dev.sasikanth.rss.reader.settings.SettingsViewModel
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.share.ShareHandler
import dev.sasikanth.rss.reader.statistics.StatisticsViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.SeedColorExtractor
import dev.sasikanth.rss.reader.ui.darkAppColorScheme
import dev.sasikanth.rss.reader.ui.getOverriddenColorScheme
import dev.sasikanth.rss.reader.ui.lightAppColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.utils.ExternalUriHandler
import dev.sasikanth.rss.reader.utils.InAppRating
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalInAppRating
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
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
  audioPlayer: AudioPlayer,
  shareHandler: ShareHandler,
  linkHandler: LinkHandler,
  inAppRating: InAppRating,
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
  accountSelectionViewModel: () -> AccountSelectionViewModel,
  freshRssLoginViewModel: () -> dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel,
  minifluxLoginViewModel: () -> MinifluxLoginViewModel,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  statisticsViewModel: () -> StatisticsViewModel,
  discoveryViewModel: () -> DiscoveryViewModel,
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
  val windowInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true)

  CompositionLocalProvider(
    LocalWindowSizeClass provides windowInfo.windowSizeClass,
    LocalShareHandler provides shareHandler,
    LocalLinkHandler provides linkHandler,
    LocalInAppRating provides inAppRating,
    LocalDynamicColorState provides dynamicColorState,
    LocalShowFeedFavIconSetting provides appState.showFeedFavIcon,
    LocalSeedColorExtractor provides seedColorExtractor,
    LocalBlockImage provides appState.blockImages,
    LocalAmoledSetting provides appState.useAmoled,
  ) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme =
      remember(isSystemInDarkTheme, appState.appThemeMode, appState.themeVariant) {
        appState.themeVariant.isDark(
          when (appState.appThemeMode) {
            AppThemeMode.Light -> false
            AppThemeMode.Dark -> true
            AppThemeMode.Auto -> isSystemInDarkTheme
          }
        )
      }
    val overriddenColorScheme =
      remember(appState.themeVariant, useDarkTheme) {
        appState.themeVariant.getOverriddenColorScheme(useDarkTheme)
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
                appViewModel.onPostOpened(post.id)
              }
            },
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

    LaunchedEffect(appState.homeViewMode, appState.themeVariant) {
      if (
        appState.homeViewMode == HomeViewMode.Default &&
          appState.themeVariant == ThemeVariant.Dynamic
      ) {
        dynamicColorState.refresh()
      } else {
        dynamicColorState.reset()
      }
    }

    LaunchedEffect(Unit) {
      appViewModel.navigateToReader
        .onEach { args ->
          val route = Screen.Reader(args)
          if (!navController.popBackStack(Screen.Main(), inclusive = false)) {
            navController.navigate(Screen.Main()) {
              popUpTo<Screen.Placeholder> { inclusive = true }
              launchSingleTop = true
            }
          }
          navController.navigate(route)
        }
        .launchIn(this)
    }

    LaunchedEffect(Unit) {
      ExternalUriHandler.uri.collect { uri ->
        if (uri != null) {
          if (uri.startsWith("twine://oauth")) {
            appViewModel.onOAuthRedirect(uri, linkHandler)
          } else if (uri == "twine://reader/currently-playing") {
            val playingPostId = audioPlayer.playbackState.value.playingPostId
            if (playingPostId != null) {
              appViewModel.onCurrentlyPlayingDeepLink(playingPostId)
            }
          } else {
            val deepLinkRequest =
              NavDeepLinkRequest(uri = NavUri(uri), action = null, mimeType = null)
            if (navController.graph.hasDeepLink(deepLinkRequest)) {
              if (!navController.popBackStack(Screen.Main(), inclusive = false)) {
                navController.navigate(Screen.Main()) {
                  popUpTo<Screen.Placeholder> { inclusive = true }
                  launchSingleTop = true
                }
              }

              navController.navigate(NavUri(uri))
            }
          }
          ExternalUriHandler.consume()
        }
      }
    }

    AppTheme(useDarkTheme = useDarkTheme, overriddenColorScheme = overriddenColorScheme) {
      NavHost(
        navController = navController,
        startDestination = Screen.Placeholder,
        popEnterTransition = {
          fadeIn(
            animationSpec =
              spring(
                dampingRatio = 1.0f, // reflects material3 motionScheme.defaultEffectsSpec()
                stiffness = 1600.0f, // reflects material3 motionScheme.defaultEffectsSpec()
              )
          )
        },
        popExitTransition = {
          if (platform == Platform.Apple) {
            slideOutOfContainer(
              towards = AnimatedContentTransitionScope.SlideDirection.End,
              animationSpec = tween(durationMillis = 200, easing = LinearEasing),
              targetOffset = { fullOffset -> (fullOffset * 0.3f).toInt() },
            )
          } else {
            scaleOut(
              targetScale = 0.7f,
              transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 0.5f),
            ) + fadeOut()
          }
        },
      ) {
        placeholderScreen(
          modifier = roundedCornerScreenModifier,
          placeholderViewModel = placeholderViewModel,
          navController = navController,
        )

        onboardingScreen(onboardingViewModel = onboardingViewModel, navController = navController)

        accountSelectionScreen(
          accountSelectionViewModel = accountSelectionViewModel,
          navController = navController,
        )

        mainScreen(
          navController = navController,
          useDarkTheme = useDarkTheme,
          toggleLightStatusBar = toggleLightStatusBar,
          toggleLightNavBar = toggleLightNavBar,
          homeViewModel = homeViewModel,
          feedsViewModel = feedsViewModel,
          searchViewModel = searchViewModel,
          bookmarksViewModel = bookmarksViewModel,
          settingsViewModel = settingsViewModel,
          discoveryViewModel = discoveryViewModel,
          openPost = openPost,
          screenModifier = screenModifier,
        )

        freshRssLoginScreen(
          freshRssLoginViewModel = freshRssLoginViewModel,
          navController = navController,
        )

        minifluxLoginScreen(
          minifluxLoginViewModel = minifluxLoginViewModel,
          navController = navController,
        )

        readerScreen(
          readerViewModel = readerViewModel,
          readerPageViewModel = readerPageViewModel,
          navController = navController,
          toggleLightStatusBar = toggleLightStatusBar,
          toggleLightNavBar = toggleLightNavBar,
          modifier = roundedCornerScreenModifier,
        )

        addFeedScreen(
          addFeedViewModel = addFeedViewModel,
          navController = navController,
          useDarkTheme = useDarkTheme,
          toggleLightStatusBar = toggleLightStatusBar,
          toggleLightNavBar = toggleLightNavBar,
        )

        discoveryScreen(
          discoveryViewModel = discoveryViewModel,
          navController = navController,
          screenModifier = screenModifier,
        )

        aboutScreen(modifier = roundedCornerScreenModifier, navController = navController)

        statisticsScreen(
          modifier = roundedCornerScreenModifier,
          statisticsViewModel = statisticsViewModel,
          navController = navController,
        )

        feedGroupScreen(
          modifier = roundedCornerScreenModifier,
          groupViewModel = groupViewModel,
          navController = navController,
        )

        blockedWordsScreen(
          modifier = roundedCornerScreenModifier,
          blockedWordsViewModel = blockedWordsViewModel,
          navController = navController,
        )

        paywallScreen(
          modifier = roundedCornerScreenModifier,
          premiumPaywallViewModel = premiumPaywallViewModel,
          navController = navController,
        )

        feedInfoDialog(feedViewModel = feedViewModel, navController = navController)

        groupSelectionDialog(
          groupSelectionViewModel = groupSelectionViewModel,
          navController = navController,
        )
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
      Screen.Reader(ReaderScreenArgs(postIndex = index, postId = post.id, fromScreen = fromScreen))

    navigate(route)
  } else {
    openLink()
  }
}
