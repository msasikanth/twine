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

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DragHandleColors
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.savedstate.serialization.SavedStateConfiguration
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.computeWindowSizeClass
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import dev.sasikanth.rss.reader.accountselection.AccountSelectionViewModel
import dev.sasikanth.rss.reader.addfeed.AddFeedViewModel
import dev.sasikanth.rss.reader.bazqux.BazQuxLoginViewModel
import dev.sasikanth.rss.reader.blockedwords.BlockedWordsViewModel
import dev.sasikanth.rss.reader.bookmarks.BookmarksViewModel
import dev.sasikanth.rss.reader.changelog.ui.ChangelogSheet
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.AppThemeMode
import dev.sasikanth.rss.reader.discovery.DiscoveryViewModel
import dev.sasikanth.rss.reader.feed.FeedViewModel
import dev.sasikanth.rss.reader.feeds.FeedsViewModel
import dev.sasikanth.rss.reader.freshrss.FreshRssLoginViewModel
import dev.sasikanth.rss.reader.group.GroupViewModel
import dev.sasikanth.rss.reader.groupselection.GroupSelectionViewModel
import dev.sasikanth.rss.reader.home.HomeViewModel
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.miniflux.MinifluxLoginViewModel
import dev.sasikanth.rss.reader.onboarding.OnboardingViewModel
import dev.sasikanth.rss.reader.opmlimport.OpmlImportViewModel
import dev.sasikanth.rss.reader.placeholder.PlaceholderViewModel
import dev.sasikanth.rss.reader.platform.ImageDownloader
import dev.sasikanth.rss.reader.platform.LinkHandler
import dev.sasikanth.rss.reader.platform.LocalImageDownloader
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
import dev.sasikanth.rss.reader.utils.LocalRootWindowSizeClass
import dev.sasikanth.rss.reader.utils.LocalShortcutDispatcher
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.ShortcutDispatcher
import dev.sasikanth.rss.reader.utils.horizontalResizePointerIcon
import dev.sasikanth.rss.reader.utils.updateWindowBackdropColor
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
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
@OptIn(
  ExperimentalMaterial3WindowSizeClassApi::class,
  ExperimentalMaterial3AdaptiveApi::class,
  ExperimentalSerializationApi::class,
  FlowPreview::class,
)
fun App(
  audioPlayer: AudioPlayer,
  shareHandler: ShareHandler,
  linkHandler: LinkHandler,
  imageDownloader: ImageDownloader,
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
  bazQuxLoginViewModel: () -> BazQuxLoginViewModel,
  groupViewModel: (SavedStateHandle) -> GroupViewModel,
  blockedWordsViewModel: () -> BlockedWordsViewModel,
  statisticsViewModel: () -> StatisticsViewModel,
  discoveryViewModel: () -> DiscoveryViewModel,
  opmlImportViewModel: () -> OpmlImportViewModel,
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
  val windowInfo = currentWindowAdaptiveInfoV2()
  val snackbarHostState = remember { SnackbarHostState() }

  // Desktop installs a dispatcher at the window so shortcuts survive whatever holds focus.
  // Other platforms have no such hook, so the root gets one that key events bubble up into.
  val windowShortcutDispatcher = LocalShortcutDispatcher.current
  val shortcutDispatcher = windowShortcutDispatcher ?: remember { ShortcutDispatcher() }

  CompositionLocalProvider(
    LocalShortcutDispatcher provides shortcutDispatcher,
    LocalWindowSizeClass provides windowInfo.windowSizeClass,
    LocalRootWindowSizeClass provides windowInfo.windowSizeClass,
    LocalShareHandler provides shareHandler,
    LocalSnackbarHostState provides snackbarHostState,
    LocalLinkHandler provides linkHandler,
    LocalImageDownloader provides imageDownloader,
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

    // Read as MutableState inside entry content; NavDisplay caches entry composables, so
    // plain value captures go stale until the back stack changes.
    val isReaderPaneExpanded = rememberSaveable { mutableStateOf(false) }
    val isSideNavigationExpanded = rememberSaveable {
      mutableStateOf(
        windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(
          WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
        )
      )
    }

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
    val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
    val maxScreenCornerRadius =
      if (platform == Platform.Apple) {
        38.dp
      } else {
        36.dp
      }
    val screenCornerRadius = remember { Animatable(0.dp, Dp.VectorConverter) }
    LaunchedEffect(navigationEventDispatcherOwner, maxScreenCornerRadius) {
      val transitionFlow =
        navigationEventDispatcherOwner?.navigationEventDispatcher?.transitionState ?: emptyFlow()
      transitionFlow.collectLatest { state ->
        when (state) {
          is NavigationEventTransitionState.InProgress -> {
            val targetRadius =
              maxScreenCornerRadius * LinearOutSlowInEasing.transform(state.latestEvent.progress)
            screenCornerRadius.snapTo(targetRadius)
          }
          is NavigationEventTransitionState.Idle -> {
            if (screenCornerRadius.value > 0.dp) {
              screenCornerRadius.animateTo(0.dp)
            }
          }
        }
      }
    }
    val screenModifier =
      remember(screenCornerRadius) {
        Modifier.fillMaxSize()
          .then(
            ScreenTransitionModifierElement(
              screenCornerRadius = { screenCornerRadius.value },
              drawEdgeShadow = platform == Platform.Apple,
            )
          )
      }

    SideEffect(useDarkTheme, onThemeChange) { onThemeChange(useDarkTheme) }

    LaunchedEffect(Unit) {
      launch {
        snapshotFlow { backStack.none { it is Screen.Reader } }
          .collect { noReader ->
            if (noReader) {
              isReaderPaneExpanded.value = false
            }
          }
      }

      appViewModel.navigateToReader
        .onEach { args -> navigator.navigateToReader(args) }
        .launchIn(this)

      launch {
        ExternalUriHandler.uri.collect { uri ->
          if (uri != null) {
            if (uri.startsWith("twine://oauth") || uri.startsWith("com.googleusercontent.apps.")) {
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
    }

    AppTheme(useDarkTheme = useDarkTheme, overriddenColorScheme = overriddenColorScheme) {
      val appColorScheme = AppTheme.colorScheme
      LaunchedEffect(appColorScheme) {
        updateWindowBackdropColor(appColorScheme.backdrop)
        snapshotFlow { appColorScheme.backdrop }
          .debounce(100.milliseconds)
          .distinctUntilChanged()
          .collect { color -> updateWindowBackdropColor(color) }
      }

      // Inline side navigation (rail or expanded drawer) only exists at large widths;
      // below that the navigation is a modal drawer overlaying content, so the list
      // pane only needs the posts list's own width.
      val hasInlineNavigation =
        windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(
          WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND
        )
      val navigationWidth = if (isSideNavigationExpanded.value) 360.dp else 80.dp
      val listPaneWidth = if (hasInlineNavigation) navigationWidth + 420.dp else 420.dp

      val density = LocalDensity.current
      val containerSize = LocalWindowInfo.current.containerSize
      val windowWidth = with(density) { containerSize.width.toDp() }
      val windowHeight = with(density) { containerSize.height.toDp() }
      val minReaderPaneWidth = 360.dp
      val minListPaneContentWidth = 320.dp
      val isSplitCapableWindow =
        windowInfo.windowSizeClass.isWidthAtLeastBreakpoint(
          WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
        )
      val hasReaderOnBackStack by remember {
        derivedStateOf { backStack.any { it is Screen.Reader } }
      }
      val isReaderExpanded =
        isSplitCapableWindow && isReaderPaneExpanded.value && hasReaderOnBackStack
      val baseDirective = remember(windowInfo) { calculatePaneScaffoldDirective(windowInfo) }
      val paneScaffoldDirective =
        remember(baseDirective, windowWidth, listPaneWidth, isReaderExpanded) {
          calculateReaderPaneScaffoldDirective(
            baseDirective = baseDirective,
            windowWidth = windowWidth,
            listPaneWidth = listPaneWidth,
            minReaderPaneWidth = minReaderPaneWidth,
            isReaderExpanded = isReaderExpanded,
          )
        }
      var hasEnteredListDetailScaffold by remember {
        mutableStateOf(
          shouldUseListDetailForSinglePane(
            isDesktop = platform == Platform.Desktop,
            isSplitCapableWindow = isSplitCapableWindow,
            hasEnteredListDetailScaffold = false,
            isReaderExpanded = isReaderExpanded,
          )
        )
      }
      SideEffect {
        if (paneScaffoldDirective.maxHorizontalPartitions > 1 && !hasEnteredListDetailScaffold) {
          hasEnteredListDetailScaffold = true
        }
      }
      val shouldHandleSinglePaneLayout =
        shouldUseListDetailForSinglePane(
          isDesktop = platform == Platform.Desktop,
          isSplitCapableWindow = isSplitCapableWindow,
          hasEnteredListDetailScaffold = hasEnteredListDetailScaffold,
          isReaderExpanded = isReaderExpanded,
        )

      val listPaneContentWidth = rememberSaveable { mutableFloatStateOf(420f) }
      val navigationPaneWidth = if (hasInlineNavigation) navigationWidth else 0.dp
      val listPaneWidthLimits =
        remember(
          density,
          navigationPaneWidth,
          windowWidth,
          baseDirective.horizontalPartitionSpacerSize,
        ) {
          with(density) {
            val min = (navigationPaneWidth + minListPaneContentWidth).roundToPx()
            val max =
              (windowWidth - baseDirective.horizontalPartitionSpacerSize - minReaderPaneWidth)
                .roundToPx()
                .coerceAtLeast(min)
            min..max
          }
        }
      val isTwoPane = paneScaffoldDirective.maxHorizontalPartitions > 1
      val listPaneSizeClass =
        remember(
          isTwoPane,
          windowInfo.windowSizeClass,
          density,
          navigationPaneWidth,
          listPaneContentWidth.floatValue,
          listPaneWidthLimits,
          windowHeight,
        ) {
          if (!isTwoPane) {
            windowInfo.windowSizeClass
          } else {
            val paneWidthPx =
              with(density) {
                  (navigationPaneWidth + listPaneContentWidth.floatValue.dp).roundToPx()
                }
                .coerceIn(listPaneWidthLimits)
            val paneWidthDp = with(density) { paneWidthPx.toDp().value }
            WindowSizeClass.BREAKPOINTS_V2.computeWindowSizeClass(
              widthDp = paneWidthDp,
              heightDp = windowHeight.value,
            )
          }
        }
      val listPaneSizeClassState = rememberUpdatedState(listPaneSizeClass)

      val openCurrentlyPlaying =
        remember(audioPlayer, appViewModel) {
          {
            val playingPostId = audioPlayer.playbackState.value.playingPostId
            if (playingPostId != null) {
              appViewModel.onCurrentlyPlayingDeepLink(playingPostId)
            }
          }
        }
      val openChangelog = remember(appViewModel) { { appViewModel.openChangelog() } }

      val entryProvider =
        remember(
          navigator,
          screenModifier,
          useDarkTheme,
          toggleLightStatusBar,
          toggleLightNavBar,
          openPost,
          openCurrentlyPlaying,
          openChangelog,
          isSideNavigationExpanded,
          isReaderPaneExpanded,
          listPaneSizeClassState,
        ) {
          entryProvider {
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
              openCurrentlyPlaying = openCurrentlyPlaying,
              screenModifier = screenModifier,
              isSideNavigationExpanded = isSideNavigationExpanded,
              listPaneSizeClass = listPaneSizeClassState,
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

            bazQuxLoginScreen(
              modifier = screenModifier,
              bazQuxLoginViewModel = bazQuxLoginViewModel,
              navigator = navigator,
            )

            readerScreen(
              readerViewModel = readerViewModel,
              readerPageViewModel = readerPageViewModel,
              navigator = navigator,
              toggleLightStatusBar = toggleLightStatusBar,
              toggleLightNavBar = toggleLightNavBar,
              modifier = screenModifier,
              isReaderPaneExpanded = isReaderPaneExpanded,
            )

            addFeedScreen(addFeedViewModel = addFeedViewModel, navigator = navigator)

            discoveryScreen(
              discoveryViewModel = discoveryViewModel,
              navigator = navigator,
              screenModifier = screenModifier,
            )

            opmlImportScreen(
              opmlImportViewModel = opmlImportViewModel,
              navigator = navigator,
              modifier = screenModifier,
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

            settingsAppInfoScreen(
              modifier = screenModifier,
              settingsViewModel = settingsViewModel,
              openChangelog = openChangelog,
              navigator = navigator,
            )

            feedInfoDialog(feedViewModel = feedViewModel, navigator = navigator)

            groupSelectionDialog(
              groupSelectionViewModel = groupSelectionViewModel,
              navigator = navigator,
            )
          }
        }

      val halfSpacerPx =
        with(density) { baseDirective.horizontalPartitionSpacerSize.roundToPx() } / 2
      val dividerCenterRef = remember { FloatRef(Float.NaN) }
      val paneExpansionState =
        rememberPaneExpansionState(
          consumeDragDelta = { delta ->
            val currentOffset = dividerCenterRef.value
            if (currentOffset.isNaN()) {
              delta
            } else {
              val target =
                (currentOffset + delta).coerceIn(
                  (listPaneWidthLimits.first + halfSpacerPx).toFloat(),
                  (listPaneWidthLimits.last + halfSpacerPx).toFloat(),
                )
              target - currentOffset
            }
          }
        )
      LaunchedEffect(navigationPaneWidth, listPaneWidthLimits, density) {
        val paneWidthPx =
          with(density) { (navigationPaneWidth + listPaneContentWidth.floatValue.dp).roundToPx() }
        paneExpansionState.setFirstPaneWidth(paneWidthPx.coerceIn(listPaneWidthLimits))
      }
      val listDetailSceneStrategy =
        rememberListDetailSceneStrategy<NavKey>(
          shouldHandleSinglePaneLayout = shouldHandleSinglePaneLayout,
          directive = paneScaffoldDirective,
          paneExpansionState = paneExpansionState,
          paneExpansionDragHandle = { state ->
            val interactionSource = remember { MutableInteractionSource() }
            val isDragged by interactionSource.collectIsDraggedAsState()
            var wasDragged by remember { mutableStateOf(false) }

            LaunchedEffect(isDragged) {
              if (isDragged) {
                wasDragged = true
              } else if (wasDragged) {
                wasDragged = false
                val center = dividerCenterRef.value
                if (!center.isNaN()) {
                  val paneWidth = with(density) { (center - halfSpacerPx).toDp() }
                  listPaneContentWidth.floatValue = (paneWidth - navigationPaneWidth).value
                }
              }
            }

            VerticalDragHandle(
              modifier =
                Modifier.pointerHoverIcon(horizontalResizePointerIcon)
                  .onPlaced { coordinates ->
                    dividerCenterRef.value =
                      coordinates.positionInParent().x + coordinates.size.width / 2f
                  }
                  .paneExpansionDraggable(
                    state = state,
                    minTouchTargetSize = LocalMinimumInteractiveComponentSize.current,
                    interactionSource = interactionSource,
                  ),
              colors =
                DragHandleColors(
                  color = AppTheme.colorScheme.outlineVariant,
                  pressedColor = AppTheme.colorScheme.primary,
                  draggedColor = AppTheme.colorScheme.primary,
                ),
              interactionSource = interactionSource,
            )
          },
        )
      val bottomSheetSceneStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

      val rootShortcutModifier =
        if (windowShortcutDispatcher == null) {
          Modifier.onKeyEvent { shortcutDispatcher.dispatch(it) }
        } else {
          Modifier
        }

      val saveableStateHolderDecorator = rememberSaveableStateHolderNavEntryDecorator<NavKey>()
      val viewModelStoreDecorator = rememberViewModelStoreNavEntryDecorator<NavKey>()
      val rootNavTransitionDecorator = remember {
        NavEntryDecorator<NavKey> { entry ->
          val rootTransition = LocalNavAnimatedContentScope.current.transition
          CompositionLocalProvider(LocalRootNavTransition provides rootTransition) {
            entry.Content()
          }
        }
      }
      val entryDecorators =
        remember(
          saveableStateHolderDecorator,
          viewModelStoreDecorator,
          rootNavTransitionDecorator,
        ) {
          if (platform == Platform.Apple) {
            listOf(
              saveableStateHolderDecorator,
              viewModelStoreDecorator,
              rootNavTransitionDecorator,
            )
          } else {
            listOf(saveableStateHolderDecorator, viewModelStoreDecorator)
          }
        }
      val sceneStrategies =
        remember(bottomSheetSceneStrategy, listDetailSceneStrategy) {
          listOf(bottomSheetSceneStrategy, listDetailSceneStrategy)
        }

      Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
          modifier =
            Modifier.fillMaxSize()
              .drawBehind { drawRect(appColorScheme.backdrop) }
              .then(rootShortcutModifier),
          backStack = backStack,
          entryProvider = entryProvider,
          entryDecorators = entryDecorators,
          sceneStrategies = sceneStrategies,
          transitionSpec = {
            if (initialState.entries.any { it.contentKey == Screen.Placeholder.toString() }) {
              fadeIn() togetherWith fadeOut()
            } else if (platform == Platform.Android) {
              AndroidNavEnterTransition togetherWith ExitTransition.KeepUntilTransitionsFinished
            } else {
              defaultTransitionSpec<NavKey>().invoke(this)
            }
          },
          popTransitionSpec = {
            if (platform == Platform.Android) {
              EnterTransition.None togetherWith AndroidNavPopExitTransition
            } else {
              defaultPopTransitionSpec<NavKey>().invoke(this)
            }
          },
          onBack = { navigator.goBack() },
        )

        SnackbarHost(
          hostState = snackbarHostState,
          modifier =
            Modifier.align(Alignment.BottomCenter).windowInsetsPadding(WindowInsets.safeDrawing),
        )
      }

      if (appState.showChangelog) {
        ChangelogSheet(
          versionName = appState.versionName,
          onDismiss = appViewModel::onChangelogDismissed,
        )
      }
    }
  }
}

private val LocalRootNavTransition = compositionLocalOf<Transition<EnterExitState>?> { null }

private val AndroidNavEnterTransition = fadeIn(animationSpec = tween(durationMillis = 250))
private val AndroidNavPopExitTransition = fadeOut(animationSpec = tween(durationMillis = 220))
private val APPLE_TRANSITION_SHADOW_WIDTH = 32.dp
private val AppleTransitionShadowColor = Color.Black.copy(alpha = 0.1f)

private class FloatRef(var value: Float)

private data class ScreenTransitionModifierElement(
  val screenCornerRadius: () -> Dp,
  val drawEdgeShadow: Boolean,
) : ModifierNodeElement<ScreenTransitionModifierNode>() {

  override fun create(): ScreenTransitionModifierNode {
    return ScreenTransitionModifierNode(
      screenCornerRadius = screenCornerRadius,
      drawEdgeShadow = drawEdgeShadow,
    )
  }

  override fun update(node: ScreenTransitionModifierNode) {
    node.screenCornerRadius = screenCornerRadius
    node.drawEdgeShadow = drawEdgeShadow
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "screenTransition"
    properties["drawEdgeShadow"] = drawEdgeShadow
  }
}

private class ScreenTransitionModifierNode(
  var screenCornerRadius: () -> Dp,
  var drawEdgeShadow: Boolean,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

  private val clipPath = Path()
  private var cachedClipSize = Size.Unspecified
  private var cachedClipRadiusPx = Float.NaN

  private var cachedShadowBrush: Brush? = null
  private var cachedShadowWidthPx = Float.NaN
  private var cachedShadowLayoutDirection: LayoutDirection? = null
  private var cachedShadowCanvasWidth = Float.NaN

  override fun ContentDrawScope.draw() {
    val cornerRadiusPx = screenCornerRadius().toPx()
    if (!drawEdgeShadow && cornerRadiusPx <= 0f) {
      drawContent()
      return
    }

    if (drawEdgeShadow) {
      val rootTransition = currentValueOf(LocalRootNavTransition)
      val localTransition =
        if (rootTransition != null) {
          currentValueOf(LocalNavAnimatedContentScope).transition
        } else {
          null
        }
      val isTransitionActive =
        cornerRadiusPx > 0f ||
          rootTransition.isTransitionActive() ||
          localTransition.isTransitionActive()

      if (!isTransitionActive) {
        drawContent()
        return
      }

      val shadowWidthPx = APPLE_TRANSITION_SHADOW_WIDTH.toPx()
      if (shadowWidthPx > 0f && size.width > 0f && size.height > 0f) {
        val brush = getOrCreateShadowBrush(shadowWidthPx, layoutDirection, size.width)
        val shadowTop = cornerRadiusPx.coerceAtMost(size.height / 2f)
        val shadowHeight = (size.height - shadowTop * 2f).coerceAtLeast(0f)
        val shadowLeft = if (layoutDirection == LayoutDirection.Ltr) -shadowWidthPx else size.width
        drawRect(
          brush = brush,
          topLeft = Offset(shadowLeft, shadowTop),
          size = Size(shadowWidthPx, shadowHeight),
        )
      }
    }

    if (cornerRadiusPx > 0f) {
      updateClipPathIfNeeded(size, cornerRadiusPx)
      clipPath(clipPath) { this@draw.drawContent() }
    } else {
      drawContent()
    }
  }

  private fun getOrCreateShadowBrush(
    shadowWidthPx: Float,
    layoutDirection: LayoutDirection,
    canvasWidth: Float,
  ): Brush {
    val existing = cachedShadowBrush
    if (
      existing != null &&
        cachedShadowWidthPx == shadowWidthPx &&
        cachedShadowLayoutDirection == layoutDirection &&
        cachedShadowCanvasWidth == canvasWidth
    ) {
      return existing
    }

    val brush =
      if (layoutDirection == LayoutDirection.Ltr) {
        Brush.horizontalGradient(
          colors = listOf(Color.Transparent, AppleTransitionShadowColor),
          startX = -shadowWidthPx,
          endX = 0f,
        )
      } else {
        Brush.horizontalGradient(
          colors = listOf(AppleTransitionShadowColor, Color.Transparent),
          startX = canvasWidth,
          endX = canvasWidth + shadowWidthPx,
        )
      }
    cachedShadowBrush = brush
    cachedShadowWidthPx = shadowWidthPx
    cachedShadowLayoutDirection = layoutDirection
    cachedShadowCanvasWidth = canvasWidth
    return brush
  }

  private fun updateClipPathIfNeeded(size: Size, cornerRadiusPx: Float) {
    if (cachedClipSize == size && cachedClipRadiusPx == cornerRadiusPx) return
    clipPath.rewind()
    clipPath.addRoundRect(
      RoundRect(
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
        cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
      )
    )
    cachedClipSize = size
    cachedClipRadiusPx = cornerRadiusPx
  }
}

private fun Transition<EnterExitState>?.isTransitionActive(): Boolean {
  return this != null && (isRunning || currentState != targetState)
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

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun calculateReaderPaneScaffoldDirective(
  baseDirective: PaneScaffoldDirective,
  windowWidth: Dp,
  listPaneWidth: Dp,
  minReaderPaneWidth: Dp,
  isReaderExpanded: Boolean,
): PaneScaffoldDirective {
  val requiredTwoPaneWidth =
    listPaneWidth + baseDirective.horizontalPartitionSpacerSize + minReaderPaneWidth
  return when {
    isReaderExpanded ->
      baseDirective.copy(
        maxHorizontalPartitions = 1,
        horizontalPartitionSpacerSize = 0.dp,
        excludedBounds = emptyList(),
      )
    windowWidth < requiredTwoPaneWidth -> baseDirective.copy(maxHorizontalPartitions = 1)
    else -> baseDirective
  }
}

internal fun shouldUseListDetailForSinglePane(
  isDesktop: Boolean,
  isSplitCapableWindow: Boolean,
  hasEnteredListDetailScaffold: Boolean,
  isReaderExpanded: Boolean,
): Boolean {
  return isDesktop || isSplitCapableWindow || hasEnteredListDetailScaffold || isReaderExpanded
}
