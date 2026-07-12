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
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import dev.sasikanth.rss.reader.accountselection.AccountSelectionViewModel
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsViewModel
import dev.sasikanth.rss.reader.bookmarks.BookmarksViewModel
import dev.sasikanth.rss.reader.changelog.ui.ChangelogSheet
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.discovery.DiscoveryViewModel
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feedhealth.FeedHealthViewModel
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel
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
import dev.sasikanth.rss.reader.ui.systemDynamicColorScheme
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclassesOfSealed
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
  freshRssLoginViewModel: () -> FreshRssLoginViewModel,
  minifluxLoginViewModel: () -> MinifluxLoginViewModel,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  statisticsViewModel: () -> StatisticsViewModel,
  feedHealthViewModel: () -> FeedHealthViewModel,
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
    val systemDynamicColors =
      if (appState.themeVariant == ThemeVariant.SystemDynamic) {
        systemDynamicColorScheme(useDarkTheme)
      } else {
        null
      }
    val overriddenColorScheme =
      remember(appState.themeVariant, useDarkTheme, systemDynamicColors) {
        systemDynamicColors ?: appState.themeVariant.getOverriddenColorScheme(useDarkTheme)
      }

    val config = remember {
      SavedStateConfiguration {
        serializersModule = SerializersModule {
          polymorphic(NavKey::class) {
            subclassesOfSealed<Screen>()
            subclassesOfSealed<Modals>()
          }
        }
      }
    }

    val backStack = rememberNavBackStack(config, Screen.Placeholder)
    val navigator = remember { AppNavigator(backStack) }

    val openPost: (Int, ResolvedPost, FromScreen) -> Unit =
      remember(navigator, linkHandler, appViewModel, coroutineScope) {
        { index, post, fromScreen ->
          navigator.navigateToReaderOrOpenLink(
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
    val screenModifier =
      Modifier.fillMaxSize().let {
        if (platform == Platform.Apple) {
          it
            .dropShadow(shape = RoundedCornerShape(screenCornerRadius)) {
              color = Color.Black.copy(alpha = 0.1f)
              radius = 32.dp.toPx()
            }
            .clip(RoundedCornerShape(screenCornerRadius))
        } else {
          it
        }
      }

    LaunchedEffect(useDarkTheme) { onThemeChange(useDarkTheme) }

    LaunchedEffect(Unit) {
      appViewModel.navigateToReader
        .onEach { args -> navigator.navigateToReader(args) }
        .launchIn(this)
    }

    LaunchedEffect(Unit) {
      ExternalUriHandler.uri.collect { uri ->
        if (uri != null) {
          if (uri.startsWith("twine://oauth")) {
            appViewModel.onOAuthRedirect(uri, linkHandler)
          } else if (uri == "twine://bookmarks") {
            navigator.navigateToMain(startTab = Screen.Main.TAB_BOOKMARKS)
          } else if (uri == "twine://reader/currently-playing") {
            val playingPostId = audioPlayer.playbackState.value.playingPostId
            if (playingPostId != null) {
              appViewModel.onCurrentlyPlayingDeepLink(playingPostId)
            }
          } else {
            val screen = DeepLinkParser.parse(uri)
            if (screen != null) {
              if (!navigator.popUpTo(Screen.Main::class, inclusive = false)) {
                navigator.navigateToMain()
              }
              navigator.navigate(screen)
            }
          }
          ExternalUriHandler.consume()
        }
      }
    }

    AppTheme(useDarkTheme = useDarkTheme, overriddenColorScheme = overriddenColorScheme) {
      val entryProvider =
        entryProvider<NavKey> {
          placeholderScreen(
            modifier = screenModifier,
            placeholderViewModel = placeholderViewModel,
            navigator = navigator,
          )

          onboardingScreen(
            modifier = screenModifier,
            onboardingViewModel = onboardingViewModel,
            navigator = navigator,
          )

          accountSelectionScreen(
            modifier = screenModifier,
            accountSelectionViewModel = accountSelectionViewModel,
            navigator = navigator,
          )

          mainScreen(
            navigator = navigator,
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
            modifier = screenModifier,
            freshRssLoginViewModel = freshRssLoginViewModel,
            navigator = navigator,
          )

          minifluxLoginScreen(
            modifier = screenModifier,
            minifluxLoginViewModel = minifluxLoginViewModel,
            navigator = navigator,
          )

          readerScreen(
            readerViewModel = readerViewModel,
            readerPageViewModel = readerPageViewModel,
            navigator = navigator,
            toggleLightStatusBar = toggleLightStatusBar,
            toggleLightNavBar = toggleLightNavBar,
            modifier = screenModifier,
          )

          addFeedScreen(
            modifier = screenModifier,
            addFeedViewModel = addFeedViewModel,
            navigator = navigator,
            useDarkTheme = useDarkTheme,
            toggleLightStatusBar = toggleLightStatusBar,
            toggleLightNavBar = toggleLightNavBar,
          )

          discoveryScreen(
            discoveryViewModel = discoveryViewModel,
            navigator = navigator,
            screenModifier = screenModifier,
          )

          aboutScreen(modifier = screenModifier, navigator = navigator)

          feedGroupScreen(
            modifier = screenModifier,
            groupViewModel = groupViewModel,
            navigator = navigator,
          )

          blockedWordsScreen(
            modifier = screenModifier,
            blockedWordsViewModel = blockedWordsViewModel,
            navigator = navigator,
          )

          paywallScreen(
            modifier = screenModifier,
            premiumPaywallViewModel = premiumPaywallViewModel,
            navigator = navigator,
          )

          imageViewerScreen(
            modifier = screenModifier,
            navigator = navigator,
            toggleLightStatusBar = toggleLightStatusBar,
            toggleLightNavBar = toggleLightNavBar,
          )

          settingsAppearanceScreen(
            modifier = screenModifier,
            settingsViewModel = settingsViewModel,
            navigator = navigator,
          )

          settingsBehaviorScreen(
            modifier = screenModifier,
            settingsViewModel = settingsViewModel,
            navigator = navigator,
          )

          settingsServicesScreen(
            modifier = screenModifier,
            settingsViewModel = settingsViewModel,
            navigator = navigator,
          )

          settingsDataScreen(
            statisticsViewModel = statisticsViewModel,
            navigator = navigator,
            modifier = screenModifier,
          )

          feedHealthScreen(
            feedHealthViewModel = feedHealthViewModel,
            navigator = navigator,
            modifier = screenModifier,
          )

          settingsAppInfoScreen(
            modifier = screenModifier,
            settingsViewModel = settingsViewModel,
            openChangelog = { appViewModel.openChangelog() },
            navigator = navigator,
          )

          feedInfoDialog(feedViewModel = feedViewModel, navigator = navigator)

          groupSelectionDialog(
            groupSelectionViewModel = groupSelectionViewModel,
            navigator = navigator,
          )
        }

      NavDisplay(
        backStack = backStack,
        entryProvider = entryProvider,
        onBack = { navigator.goBack() },
      )

      if (appState.showChangelog) {
        ChangelogSheet(
          versionName = appState.versionName,
          onDismiss = appViewModel::onChangelogDismissed,
        )
      }
    }
  }
}

private fun AppNavigator.navigateToReaderOrOpenLink(
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
