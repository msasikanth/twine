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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.paging.LoadState.NotLoading
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import com.mikepenz.markdown.compose.components.markdownComponents
import dev.sasikanth.rss.reader.app.LocalSnackbarHostState
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.markdown.ReaderMarkdownTable
import dev.sasikanth.rss.reader.markdown.SafeMarkdownCheckBox
import dev.sasikanth.rss.reader.markdown.SafeMarkdownHeader
import dev.sasikanth.rss.reader.markdown.SafeMarkdownHighlightedCodeBlock
import dev.sasikanth.rss.reader.markdown.SafeMarkdownHighlightedCodeFence
import dev.sasikanth.rss.reader.markdown.SafeMarkdownParagraph
import dev.sasikanth.rss.reader.markdown.SafeMarkdownText
import dev.sasikanth.rss.reader.markdown.safeUnescapedTextInNode
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderContentType
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPanelContent
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ui.MediaControls
import dev.sasikanth.rss.reader.reader.page.ui.ReaderPage
import dev.sasikanth.rss.reader.reader.page.ui.SleepTimerBottomSheet
import dev.sasikanth.rss.reader.reader.page.ui.nextPlaybackSpeed
import dev.sasikanth.rss.reader.reader.resolveDynamicThemeSeedUrl
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.CollapseContent
import dev.sasikanth.rss.reader.resources.icons.ExpandContent
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ComicNeueFontFamily
import dev.sasikanth.rss.reader.ui.DefaultRippleAlpha
import dev.sasikanth.rss.reader.ui.DefaultTypography
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.ui.GoogleSansFontFamily
import dev.sasikanth.rss.reader.ui.LocalAppColorScheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalIsDarkTheme
import dev.sasikanth.rss.reader.ui.LocalNavAnimatedContentScopeOrNull
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.ui.LoraFontFamily
import dev.sasikanth.rss.reader.ui.MerriWeatherFontFamily
import dev.sasikanth.rss.reader.ui.OutfitFontFamily
import dev.sasikanth.rss.reader.ui.RobotoSerifFontFamily
import dev.sasikanth.rss.reader.ui.TranslucentStyle
import dev.sasikanth.rss.reader.ui.TranslucentStyles
import dev.sasikanth.rss.reader.ui.getOverriddenColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.ui.systemDynamicColorScheme
import dev.sasikanth.rss.reader.ui.typography
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.ShortcutHandler
import dev.sasikanth.rss.reader.utils.iosBottomSafeAreaPadding
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.intellij.markdown.MarkdownTokenTypes
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonClose
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.readerCollapseScreen
import twine.shared.generated.resources.readerExpandScreen
import twine.shared.generated.resources.readerPageCount

private val ReaderActionsPanelShape = RoundedCornerShape(36.dp)
private val PanelDarkMaterialColorScheme = darkColorScheme()
private val RecoilSpringSpec =
  spring<Float>(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
private val PanelSizeTransformSpec =
  spring<IntSize>(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow,
    visibilityThreshold = IntSize.VisibilityThreshold,
  )

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
internal fun ReaderScreen(
  viewModel: ReaderViewModel,
  pageViewModelFactory: @Composable (ResolvedPost) -> ReaderPageViewModel,
  onBack: () -> Unit,
  openPaywall: () -> Unit,
  onImageClick: (ImageClickEvent) -> Unit,
  toggleLightStatusBar: (Boolean) -> Unit,
  toggleLightNavBar: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  showCloseNavIcon: Boolean = false,
  isReaderPaneExpanded: Boolean = false,
  toggleReaderPaneExpanded: (() -> Unit)? = null,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val posts = state.posts.collectAsLazyPagingItems()
  val linkHandler = LocalLinkHandler.current
  val seedColorExtractor = LocalSeedColorExtractor.current
  val appDynamicColorState = LocalDynamicColorState.current
  val shouldBlockImage = LocalBlockImage.current
  val podcastHeroVisibility = remember { mutableStateMapOf<String, Boolean>() }
  val pageBackdropVisibilityProviders = remember { mutableStateMapOf<String, () -> Float>() }
  val enrichedPodcastArtworkUrls = remember { mutableStateMapOf<String, String>() }

  val defaultLightValues =
    remember(appDynamicColorState) { appDynamicColorState.lightAppColorScheme.peekValues() }
  val defaultDarkValues =
    remember(appDynamicColorState) { appDynamicColorState.darkAppColorScheme.peekValues() }
  val articleDynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = defaultLightValues,
      defaultDarkAppColorScheme = defaultDarkValues,
    )

  val readerLinkHandler = remember {
    object : UriHandler {
      override fun openUri(uri: String) {
        coroutineScope.launch { linkHandler.openLink(uri) }
      }
    }
  }
  val pagerState = rememberPagerState(initialPage = state.activePostIndex) { posts.itemCount }
  val exitScreen by viewModel.exitScreen.collectAsStateWithLifecycle(false)

  // If the pager lays out before paging delivers its first item count, the initial
  // page gets clamped to 0 and the requested index is lost. Restore it exactly once,
  // as soon as the count is available, unless the user has already started swiping.
  val initialPostIndex = remember { state.activePostIndex }
  val initialPostId = remember { state.activePostId }
  var initialPageRestored by rememberSaveable { mutableStateOf(false) }
  var initialTargetPage by rememberSaveable { mutableStateOf<Int?>(null) }
  val navTransition = LocalNavAnimatedContentScopeOrNull.current?.transition
  var hasInitialTransitionSettled by remember { mutableStateOf(false) }
  var anchoredPostId by rememberSaveable {
    mutableStateOf(initialPostId.takeIf { it.isNotBlank() })
  }
  LaunchedEffect(posts.itemCount) {
    if (!initialPageRestored && posts.itemCount > 0) {
      if (!pagerState.isScrollInProgress) {
        val targetPage =
          resolveInitialReaderPage(
            initialPostIndex = initialPostIndex,
            initialPostId = initialPostId,
            itemCount = posts.itemCount,
            peekPostId = { index -> runCatching { posts.peek(index) }.getOrNull()?.id },
          )
        initialTargetPage = targetPage
        if (pagerState.currentPage != targetPage) {
          pagerState.scrollToPage(targetPage)
        }
      } else {
        anchoredPostId = runCatching { posts.peek(pagerState.currentPage) }.getOrNull()?.id
      }
      initialPageRestored = true
    }
  }

  LaunchedEffect(navTransition, showCloseNavIcon, pagerState) {
    if (!hasInitialTransitionSettled) {
      if (showCloseNavIcon) {
        withTimeoutOrNull(300.milliseconds) {
          snapshotFlow { pagerState.isScrollInProgress }.first { it }
        }
      } else if (navTransition != null && navTransition.isTransitioning) {
        snapshotFlow { !navTransition.isTransitioning || pagerState.isScrollInProgress }
          .first { it }
      } else {
        withTimeoutOrNull(300.milliseconds) {
          snapshotFlow {
              (navTransition != null && navTransition.isTransitioning) ||
                pagerState.isScrollInProgress
            }
            .first { it }
        }
        if (
          navTransition != null && navTransition.isTransitioning && !pagerState.isScrollInProgress
        ) {
          snapshotFlow { !navTransition.isTransitioning || pagerState.isScrollInProgress }
            .first { it }
        }
      }
      hasInitialTransitionSettled = true
    }
  }

  SideEffect(hasInitialTransitionSettled) {
    if (hasInitialTransitionSettled) {
      viewModel.dispatch(ReaderEvent.OnInitialTransitionSettled)
    }
  }

  LaunchedEffect(pagerState, posts) {
    launch {
      snapshotFlow { pagerState.settledPage }
        .collect { page ->
          if (!initialPageRestored) return@collect
          val postAtPage = runCatching { posts.peek(page) }.getOrNull() ?: return@collect
          if (
            anchoredPostId == initialPostId &&
              page == initialTargetPage &&
              postAtPage.id != initialPostId
          ) {
            return@collect
          }
          anchoredPostId = postAtPage.id
        }
    }

    launch {
      snapshotFlow {
          val page = pagerState.currentPage
          Triple(page, posts.itemCount, runCatching { posts.peek(page) }.getOrNull()?.id)
        }
        .collect { (page, itemCount, postIdAtPage) ->
          val anchor = anchoredPostId
          if (
            !initialPageRestored ||
              anchor == null ||
              postIdAtPage == null ||
              postIdAtPage == anchor ||
              pagerState.isScrollInProgress
          ) {
            return@collect
          }

          val anchorIndex =
            posts.indexOfPostNear(postId = anchor, page = page, itemCount = itemCount)
              ?: posts.indexOfPost(postId = anchor, itemCount = itemCount)
          if (anchorIndex != null && anchorIndex != page) {
            pagerState.scrollToPage(anchorIndex)
          }
        }
    }

    // Publish the active post from the settled page rather than per-page visibility:
    // onVisibilityChanged ignores parent clipping, so in a split layout the pager's
    // beyond-viewport neighbor overlaps the list pane and falsely reports as visible.
    // The debounce keeps the initial page-restore scroll from publishing page 0.
    launch {
      snapshotFlow { pagerState.settledPage to posts.itemCount }
        .debounce(250.milliseconds)
        .collect { (page, itemCount) ->
          if (page in 0 until itemCount) {
            val readerPost = runCatching { posts.peek(page) }.getOrNull()
            if (readerPost != null) {
              viewModel.dispatch(ReaderEvent.PostPageChanged(page, readerPost))
            }
          }
        }
    }
  }

  // In a split layout the list pane keeps the app theme, so article-based dynamic colors
  // in the reader pane would clash with it.
  val isDynamicColorEnabled =
    state.selectedThemeVariant == ThemeVariant.Dynamic && !showCloseNavIcon
  val isSettledPostLoaded by
    remember(pagerState, posts) {
      derivedStateOf { runCatching { posts.peek(pagerState.settledPage) }.getOrNull() != null }
    }

  LaunchedEffect(isDynamicColorEnabled, shouldBlockImage, articleDynamicColorState) {
    if (!isDynamicColorEnabled || shouldBlockImage) {
      articleDynamicColorState.reset()
    }
  }

  LaunchedEffect(
    pagerState,
    posts,
    isDynamicColorEnabled,
    shouldBlockImage,
    hasInitialTransitionSettled,
  ) {
    if (!isDynamicColorEnabled || shouldBlockImage) return@LaunchedEffect

    snapshotFlow {
        val itemCount = posts.itemCount
        val settledPage = pagerState.settledPage
        val loaded = runCatching { posts.peek(settledPage) }.getOrNull() != null
        Triple(settledPage, itemCount, loaded)
      }
      .distinctUntilChanged()
      .collect { (settledPage, itemCount, _) ->
        if (itemCount == 0) return@collect
        val pageRange =
          if (hasInitialTransitionSettled) {
            (settledPage - 1).coerceAtLeast(0)..(settledPage + 1).coerceAtMost(itemCount - 1)
          } else {
            settledPage.coerceIn(0, itemCount - 1)..settledPage.coerceIn(0, itemCount - 1)
          }

        for (page in pageRange) {
          val post = runCatching { posts.peek(page) }.getOrNull()
          if (post?.seedColor == null) {
            val seedUrl = post?.resolveDynamicThemeSeedUrl()
            if (!seedUrl.isNullOrBlank()) {
              launch { seedColorExtractor.calculateSeedColor(url = seedUrl) }
            }
          }
        }
      }
  }

  pagerState.CollectItemTransition(
    posts.itemCount,
    isSettledPostLoaded,
    isDynamicColorEnabled,
    shouldBlockImage,
    articleDynamicColorState,
    itemProvider = { index ->
      if (shouldBlockImage || index !in 0 until posts.itemCount) {
        null
      } else {
        runCatching { posts.peek(index) }.getOrNull()
      }
    },
  ) { fromItem, toItem, offset ->
    if (isDynamicColorEnabled) {
      val fromUrl = fromItem?.resolveDynamicThemeSeedUrl()
      val toUrl = toItem?.resolveDynamicThemeSeedUrl()
      val fromSeedColor =
        fromItem?.seedColor?.let { Color(it) }
          ?: seedColorExtractor.cachedSeedColor(url = fromUrl)
          ?: seedColorExtractor.calculateSeedColor(url = fromUrl)
      val toSeedColor =
        toItem?.seedColor?.let { Color(it) }
          ?: seedColorExtractor.cachedSeedColor(url = toUrl)
          ?: seedColorExtractor.calculateSeedColor(url = toUrl)

      articleDynamicColorState.animate(
        fromSeedColor = fromSeedColor,
        toSeedColor = toSeedColor,
        progress = offset,
      )
    }
  }

  SideEffect(state.openPaywall) {
    if (state.openPaywall) {
      openPaywall()
      viewModel.dispatch(ReaderEvent.MarkOpenPaywallDone)
    }
  }

  SideEffect(exitScreen) {
    if (exitScreen) {
      onBack()
    }
  }

  SideEffect(posts.itemCount, posts.loadState.refresh) {
    if (posts.itemCount == 0 && posts.loadState.refresh is NotLoading) {
      onBack()
    }
  }

  NavigationBackHandler(
    state = rememberNavigationEventState(NavigationEventInfo.None),
    isBackEnabled = state.isPanelExpanded,
  ) {
    viewModel.dispatch(ReaderEvent.CollapsePanel)
  }

  val isParentThemeDark = AppTheme.isDark
  val isDarkTheme =
    remember(state.selectedThemeVariant, isParentThemeDark) {
      state.selectedThemeVariant.isDark(isParentThemeDark)
    }

  SideEffect(isDarkTheme) {
    toggleLightStatusBar(!isDarkTheme)
    toggleLightNavBar(!isDarkTheme)
  }

  val snackbarHostState = remember { SnackbarHostState() }
  val fontFamily =
    when (state.selectedReaderFont) {
      ReaderFont.ComicNeue -> ComicNeueFontFamily
      ReaderFont.GoogleSans -> GoogleSansFontFamily
      ReaderFont.Golos -> GolosFontFamily
      ReaderFont.Lora -> LoraFontFamily
      ReaderFont.Merriweather -> MerriWeatherFontFamily
      ReaderFont.RobotoSerif -> RobotoSerifFontFamily
      ReaderFont.Outfit -> OutfitFontFamily
    }
  val typography =
    remember(fontFamily, state.readerFontScaleFactor, state.readerLineHeightScaleFactor) {
      typography(
        fontFamily = fontFamily,
        fontScalingFactor = state.readerFontScaleFactor,
        lineHeightScalingFactor = state.readerLineHeightScaleFactor,
      )
    }

  CompositionLocalProvider(
    LocalDynamicColorState provides articleDynamicColorState,
    LocalUriHandler provides readerLinkHandler,
    LocalSnackbarHostState provides snackbarHostState,
  ) {
    val sourceColorScheme = AppTheme.colorScheme
    val systemDynamicColors =
      if (state.selectedThemeVariant == ThemeVariant.SystemDynamic) {
        systemDynamicColorScheme(isDarkTheme)
      } else {
        null
      }
    val overriddenColorScheme =
      remember(state.selectedThemeVariant, isDarkTheme, sourceColorScheme, systemDynamicColors) {
        systemDynamicColors ?: state.selectedThemeVariant.getOverriddenColorScheme(isDarkTheme)
      }

    val darkAppColorScheme = appDynamicColorState.darkAppColorScheme
    val systemDynamicDarkColors =
      if (state.selectedThemeVariant == ThemeVariant.SystemDynamic) {
        systemDynamicColorScheme(true)
      } else {
        null
      }
    val overriddenDarkColorScheme =
      remember(state.selectedThemeVariant, darkAppColorScheme, systemDynamicDarkColors) {
        systemDynamicDarkColors ?: state.selectedThemeVariant.getOverriddenColorScheme(true)
      }

    AppTheme(
      useDarkTheme = isDarkTheme,
      typography = typography,
      overriddenColorScheme = overriddenColorScheme,
    ) {
      val settledPost =
        if (posts.itemCount > 0 && pagerState.settledPage in 0 until posts.itemCount) {
          runCatching { posts.peek(pagerState.settledPage) }.getOrNull()
        } else {
          null
        }
      val settledPageViewModel = settledPost?.let { pageViewModelFactory.invoke(it) }

      ShortcutHandler { event ->
        if (event.type != KeyEventType.KeyUp) return@ShortcutHandler false

        return@ShortcutHandler when (event.key) {
          Key.DirectionRight -> {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }

            true
          }
          Key.DirectionLeft -> {
            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }

            true
          }
          Key.B if settledPost != null -> {
            viewModel.dispatch(
              ReaderEvent.TogglePostBookmark(
                postId = settledPost.id,
                currentBookmarkStatus = settledPost.bookmarked,
              )
            )

            true
          }
          Key.U if settledPost != null -> {
            viewModel.dispatch(ReaderEvent.OnMarkAsUnread(postId = settledPost.id))

            true
          }
          Key.V if settledPost != null -> {
            coroutineScope.launch { linkHandler.openLink(settledPost.link) }

            true
          }
          Key.S -> {
            val nextPanel =
              if (state.panelContent == ReaderPanelContent.Customizations) {
                ReaderPanelContent.Actions
              } else {
                ReaderPanelContent.Customizations
              }
            viewModel.dispatch(ReaderEvent.ShowPanelContent(nextPanel))

            true
          }
          Key.Spacebar -> {
            val playback = viewModel.playbackState.value
            when {
              settledPost != null &&
                settledPageViewModel != null &&
                settledPageViewModel.contentType is ReaderContentType.Podcast &&
                settledPageViewModel.audioPlayer.isAvailable -> {
                if (playback.playingPostId == settledPost.id) {
                  if (playback.isPlaying || playback.buffering) {
                    viewModel.pauseAudio()
                  } else {
                    viewModel.resumeAudio()
                  }
                } else {
                  settledPageViewModel.playAudio()
                }
                true
              }
              playback.playingPostId != null -> {
                if (playback.isPlaying || playback.buffering) {
                  viewModel.pauseAudio()
                } else {
                  viewModel.resumeAudio()
                }
                true
              }
              else -> false
            }
          }
          Key.LeftBracket -> {
            if (viewModel.playbackState.value.playingPostId != null) {
              viewModel.seekBackward()
              true
            } else {
              false
            }
          }
          Key.RightBracket -> {
            if (viewModel.playbackState.value.playingPostId != null) {
              viewModel.seekForward()
              true
            } else {
              false
            }
          }
          Key.Escape -> {
            onBack()

            true
          }
          else -> false
        }
      }

      val colorScheme = AppTheme.colorScheme
      Scaffold(
        modifier = modifier.fillMaxSize().drawBehind { drawRect(colorScheme.backdrop) },
        topBar = {
          CenterAlignedTopAppBar(
            expandedHeight = 72.dp,
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
              ),
            navigationIcon = {
              CircularIconButton(
                modifier = Modifier.padding(start = 12.dp),
                icon = if (showCloseNavIcon) TwineIcons.Close else TwineIcons.ArrowBack,
                label =
                  stringResource(
                    if (showCloseNavIcon) Res.string.buttonClose else Res.string.buttonGoBack
                  ),
                onClick = onBack,
              )
            },
            title = { ReaderTopBarTitle(pagerState = pagerState) },
            actions = {
              if (toggleReaderPaneExpanded != null) {
                CircularIconButton(
                  modifier = Modifier.padding(end = 12.dp),
                  icon =
                    if (isReaderPaneExpanded) {
                      TwineIcons.CollapseContent
                    } else {
                      TwineIcons.ExpandContent
                    },
                  label =
                    stringResource(
                      if (isReaderPaneExpanded) {
                        Res.string.readerCollapseScreen
                      } else {
                        Res.string.readerExpandScreen
                      }
                    ),
                  onClick = toggleReaderPaneExpanded,
                )
              }
            },
          )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = Color.Unspecified,
      ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
          val layoutDirection = LocalLayoutDirection.current
          val sizeClass = LocalWindowSizeClass.current
          val readerContentMaxWidth =
            if (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
              960.dp
            } else {
              700.dp
            }

          val topInset = paddingValues.calculateTopPadding()
          val panelBottomInset = readerActionsPanelBottomInset()
          val readerContentPadding =
            remember(topInset, panelBottomInset) {
              PaddingValues(
                top = topInset,
                bottom = panelBottomInset + READER_ACTIONS_PANEL_COLLAPSED_HEIGHT,
              )
            }
          val startInset = paddingValues.calculateStartPadding(layoutDirection)
          val endInset = paddingValues.calculateEndPadding(layoutDirection)
          val pagerContentPadding =
            remember(startInset, endInset) { PaddingValues(start = startInset, end = endInset) }

          val markdownComponents =
            remember(isDarkTheme) {
              markdownComponents(
                text = { cm ->
                  SafeMarkdownText(
                    content = cm.node.safeUnescapedTextInNode(cm.content),
                    node = cm.node,
                    style = cm.typography.text,
                  )
                },
                heading1 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h1)
                },
                heading2 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h2)
                },
                heading3 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h3)
                },
                heading4 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h4)
                },
                heading5 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h5)
                },
                heading6 = { cm ->
                  SafeMarkdownHeader(content = cm.content, node = cm.node, style = cm.typography.h6)
                },
                setextHeading1 = { cm ->
                  SafeMarkdownHeader(
                    content = cm.content,
                    node = cm.node,
                    style = cm.typography.h1,
                    contentChildType = MarkdownTokenTypes.SETEXT_CONTENT,
                  )
                },
                setextHeading2 = { cm ->
                  SafeMarkdownHeader(
                    content = cm.content,
                    node = cm.node,
                    style = cm.typography.h2,
                    contentChildType = MarkdownTokenTypes.SETEXT_CONTENT,
                  )
                },
                paragraph = { cm ->
                  SafeMarkdownParagraph(
                    content = cm.content,
                    node = cm.node,
                    style = cm.typography.paragraph,
                  )
                },
                checkbox = { cm ->
                  SafeMarkdownCheckBox(
                    content = cm.content,
                    node = cm.node,
                    style = cm.typography.text,
                  )
                },
                table = { cm -> ReaderMarkdownTable(cm) },
                codeBlock = { cm ->
                  SafeMarkdownHighlightedCodeBlock(
                    content = cm.content,
                    node = cm.node,
                    isDarkTheme = isDarkTheme,
                    showHeader = true,
                  )
                },
                codeFence = { cm ->
                  SafeMarkdownHighlightedCodeFence(
                    content = cm.content,
                    node = cm.node,
                    isDarkTheme = isDarkTheme,
                    showHeader = true,
                  )
                },
              )
            }

          if (posts.itemCount > 0) {
            val backdropHeight =
              topInset +
                if (
                  sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)
                ) {
                  360.dp
                } else {
                  320.dp
                }

            ReaderAmbientBackdrop(
              posts = posts,
              pagerState = pagerState,
              isDarkTheme = isDarkTheme,
              enrichedPodcastArtworkUrls = enrichedPodcastArtworkUrls,
              scrollVisibilityProviders = pageBackdropVisibilityProviders,
              modifier = Modifier.fillMaxWidth().height(backdropHeight).align(Alignment.TopCenter),
            )

            HorizontalPager(
              modifier =
                Modifier.widthIn(max = readerContentMaxWidth)
                  .fillMaxSize()
                  .align(Alignment.Center)
                  .iosBottomSafeAreaPadding(),
              state = pagerState,
              overscrollEffect = null,
              beyondViewportPageCount = if (hasInitialTransitionSettled) 1 else 0,
              contentPadding = pagerContentPadding,
              key = {
                runCatching {
                    val post = posts.peek(it)
                    post?.let { post.id + post.sourceId }
                  }
                  .getOrNull() ?: it
              },
            ) { page ->
              val readerPost = posts[page]

              if (readerPost != null) {
                val pageViewModel = pageViewModelFactory.invoke(readerPost)
                val showFullArticle by pageViewModel.showFullArticle.collectAsStateWithLifecycle()

                DisposableEffect(readerPost.id) {
                  onDispose {
                    podcastHeroVisibility.remove(readerPost.id)
                    pageBackdropVisibilityProviders.remove(readerPost.id)
                    enrichedPodcastArtworkUrls.remove(readerPost.id)
                  }
                }

                ReaderPage(
                  modifier = Modifier.fillMaxSize(),
                  contentPaddingValues = readerContentPadding,
                  pageViewModel = pageViewModel,
                  readerPost = readerPost,
                  showFullArticle = showFullArticle,
                  page = page,
                  pagerState = pagerState,
                  markdownComponents = markdownComponents,
                  playbackStateFlow = viewModel.playbackState,
                  isDarkTheme = isDarkTheme,
                  themeVariant = state.selectedThemeVariant,
                  fromScreen = state.fromScreen,
                  onBookmarkClick = {
                    viewModel.dispatch(
                      ReaderEvent.TogglePostBookmark(
                        postId = readerPost.id,
                        currentBookmarkStatus = readerPost.bookmarked,
                      )
                    )
                  },
                  onMarkAsUnread = {
                    viewModel.dispatch(ReaderEvent.OnMarkAsUnread(postId = readerPost.id))
                  },
                  onImageClick = onImageClick,
                  onWatchVideo = { url -> coroutineScope.launch { linkHandler.openLink(url) } },
                  onOpenWebsite = {
                    coroutineScope.launch { linkHandler.openLink(readerPost.link) }
                  },
                  onPauseAudio = viewModel::pauseAudio,
                  onResumeAudio = viewModel::resumeAudio,
                  onSeekAudio = viewModel::seekAudio,
                  onSeekForward = viewModel::seekForward,
                  onSeekBackward = viewModel::seekBackward,
                  onPlaybackSpeedChange = viewModel::setPlaybackSpeed,
                  onSleepTimerOptionSelected = viewModel::setSleepTimer,
                  onHeroVisibilityChanged = { visible ->
                    podcastHeroVisibility[readerPost.id] = visible
                  },
                  onBackdropVisibilityProviderChanged = { provider ->
                    if (provider != null) {
                      pageBackdropVisibilityProviders[readerPost.id] = provider
                    } else {
                      pageBackdropVisibilityProviders.remove(readerPost.id)
                    }
                  },
                  onResolvedCoverArtUrlChanged = { url ->
                    if (!url.isNullOrBlank()) {
                      enrichedPodcastArtworkUrls[readerPost.id] = url
                    } else {
                      enrichedPodcastArtworkUrls.remove(readerPost.id)
                    }
                  },
                )
              }
            }
          }

          if (state.isPanelExpanded) {
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .pointerInput(Unit) {
                    detectTapGestures { viewModel.dispatch(ReaderEvent.CollapsePanel) }
                  }
                  .align(Alignment.BottomCenter)
            )
          }

          if (settledPost != null && settledPageViewModel != null) {
            ReaderSettledActionsPanel(
              modifier = Modifier.align(Alignment.BottomCenter),
              settledPost = settledPost,
              settledPageViewModel = settledPageViewModel,
              viewModel = viewModel,
              isHeroVisible = podcastHeroVisibility[settledPost.id] ?: true,
              isParentThemeDark = isParentThemeDark,
              isDarkTheme = isDarkTheme,
              panelContent = state.panelContent,
              selectedFont = state.selectedReaderFont,
              selectedThemeVariant = state.selectedThemeVariant,
              fontScaleFactor = state.readerFontScaleFactor,
              fontLineHeightFactor = state.readerLineHeightScaleFactor,
              isSubscribed = state.isSubscribed,
              overriddenDarkColorScheme = overriddenDarkColorScheme,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReaderTopBarTitle(pagerState: PagerState) {
  if (pagerState.pageCount <= 1) return

  val pageIndicatorState =
    remember(pagerState) {
      object : PageIndicatorState {
        override val pageOffset: Float
          get() = pagerState.currentPageOffsetFraction

        override val selectedPage: Int
          get() = pagerState.currentPage

        override val pageCount: Int
          get() = pagerState.pageCount
      }
    }
  val colorScheme = AppTheme.colorScheme
  val textColorProducer = remember(colorScheme) { ColorProducer { colorScheme.onSurfaceVariant } }

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ReaderPageCountLabel(pagerState = pagerState, colorProducer = textColorProducer)

    HorizontalPageIndicators(pageIndicatorState = pageIndicatorState)
  }
}

@Composable
private fun ReaderPageCountLabel(pagerState: PagerState, colorProducer: ColorProducer) {
  BasicText(
    text =
      stringResource(Res.string.readerPageCount, pagerState.currentPage + 1, pagerState.pageCount),
    style = MaterialTheme.typography.labelSmall,
    color = colorProducer,
  )
}

@Composable
private fun ReaderSettledActionsPanel(
  settledPost: ResolvedPost,
  settledPageViewModel: ReaderPageViewModel,
  viewModel: ReaderViewModel,
  isHeroVisible: Boolean,
  isParentThemeDark: Boolean,
  isDarkTheme: Boolean,
  panelContent: ReaderPanelContent,
  selectedFont: ReaderFont,
  selectedThemeVariant: ThemeVariant,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  isSubscribed: Boolean,
  overriddenDarkColorScheme: AppColorScheme?,
  modifier: Modifier = Modifier,
) {
  val showFullArticle by settledPageViewModel.showFullArticle.collectAsStateWithLifecycle()
  val resolvedCoverArtUrl by settledPageViewModel.resolvedCoverArtUrl.collectAsStateWithLifecycle()
  val linkHandler = LocalLinkHandler.current
  val coroutineScope = rememberCoroutineScope()

  ReaderActionsPanel(
    modifier = modifier,
    settledPost = settledPost,
    contentType = settledPageViewModel.contentType,
    resolvedCoverArtUrl = resolvedCoverArtUrl,
    isHeroVisible = isHeroVisible,
    isParentThemeDark = isParentThemeDark,
    isDarkTheme = isDarkTheme,
    showFullArticle = showFullArticle,
    panelContent = panelContent,
    playbackState = viewModel.playbackState,
    selectedFont = selectedFont,
    selectedThemeVariant = selectedThemeVariant,
    fontScaleFactor = fontScaleFactor,
    fontLineHeightFactor = fontLineHeightFactor,
    isSubscribed = isSubscribed,
    overriddenColorScheme = overriddenDarkColorScheme,
    openInBrowserClick = { coroutineScope.launch { linkHandler.openLink(settledPost.link) } },
    loadFullArticleClick = { settledPageViewModel.toggleFullArticle() },
    watchVideoClick = {
      val videoType = settledPageViewModel.contentType as? ReaderContentType.Video
      if (videoType != null) {
        coroutineScope.launch { linkHandler.openLink(videoType.videoUrl) }
      }
    },
    openReaderViewSettings = {
      viewModel.dispatch(ReaderEvent.ShowPanelContent(ReaderPanelContent.Customizations))
    },
    openPlayer = { targetPostId ->
      viewModel.dispatch(ReaderEvent.ShowPanelContent(ReaderPanelContent.Player(targetPostId)))
    },
    collapsePanel = { viewModel.dispatch(ReaderEvent.CollapsePanel) },
    onPlayClick = { settledPageViewModel.playAudio() },
    onPauseClick = viewModel::pauseAudio,
    onResumeClick = viewModel::resumeAudio,
    onStopClick = viewModel::stopAudio,
    onSeek = viewModel::seekAudio,
    onSeekForward = viewModel::seekForward,
    onSeekBackward = viewModel::seekBackward,
    onPlaybackSpeedChange = viewModel::setPlaybackSpeed,
    onSleepTimerOptionSelected = viewModel::setSleepTimer,
    onFontChange = { font -> viewModel.dispatch(ReaderEvent.UpdateReaderFont(font)) },
    onThemeVariantChange = { themeVariant ->
      viewModel.dispatch(ReaderEvent.UpdateThemeVariant(themeVariant))
    },
    onFontScaleFactorChange = { factor ->
      viewModel.dispatch(ReaderEvent.UpdateFontScaleFactor(factor))
    },
    onFontLineHeightFactorChange = { factor ->
      viewModel.dispatch(ReaderEvent.UpdateFontLineHeightFactor(factor))
    },
  )
}

@Composable
private fun ReaderActionsPanel(
  settledPost: ResolvedPost,
  contentType: ReaderContentType,
  resolvedCoverArtUrl: String?,
  isHeroVisible: Boolean,
  isParentThemeDark: Boolean,
  isDarkTheme: Boolean,
  showFullArticle: Boolean,
  panelContent: ReaderPanelContent,
  playbackState: StateFlow<PlaybackState>,
  selectedFont: ReaderFont,
  selectedThemeVariant: ThemeVariant,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  isSubscribed: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  watchVideoClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  openPlayer: (targetPostId: String) -> Unit,
  collapsePanel: () -> Unit,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
  onStopClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
  onFontChange: (ReaderFont) -> Unit,
  onThemeVariantChange: (ThemeVariant) -> Unit,
  onFontScaleFactorChange: (Float) -> Unit,
  onFontLineHeightFactorChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  overriddenColorScheme: AppColorScheme? = null,
) {
  val colorScheme = AppTheme.colorScheme
  val settledPostId = settledPost.id
  val isPodcastAvailable = contentType is ReaderContentType.Podcast && contentType.isPlayerAvailable

  val initialActiveInfo = remember(playbackState) { playbackState.value.toActiveAudioSnapshot() }
  val activeAudio by
    remember(playbackState) {
        playbackState.map { it.toActiveAudioSnapshot() }.distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(initialActiveInfo)

  val initialProgress = remember(playbackState) { playbackState.value.toPlaybackFraction() }
  val activeProgress by
    remember(playbackState) { playbackState.map { it.toPlaybackFraction() }.distinctUntilChanged() }
      .collectAsStateWithLifecycle(initialProgress)

  Box(
    modifier = Modifier.fillMaxWidth().wrapContentHeight().then(modifier),
    contentAlignment = Alignment.BottomCenter,
  ) {
    val (spotShadowColor, ambientShadowColor) =
      remember(isDarkTheme) {
        if (isDarkTheme) {
          Pair(Color.Black.copy(alpha = 0.85f), Color.Black.copy(alpha = 0.4f))
        } else {
          Pair(Color.Black.copy(alpha = 0.65f), Color.Black.copy(alpha = 0.28f))
        }
      }

    val density = LocalDensity.current
    val recoil = remember { Animatable(0f) }
    var hasSettledOnce by remember { mutableStateOf(false) }
    val isPanelExpanded = panelContent != ReaderPanelContent.Actions
    LaunchedEffect(isPanelExpanded) {
      if (!hasSettledOnce) {
        hasSettledOnce = true
        return@LaunchedEffect
      }

      val impulse = with(density) { READER_ACTIONS_PANEL_RECOIL.toPx() }
      recoil.snapTo(if (isPanelExpanded) impulse else -impulse)
      recoil.animateTo(targetValue = 0f, animationSpec = RecoilSpringSpec)
    }

    val playingPostId = activeAudio.playingPostId
    val showDockedStrip =
      playingPostId != null &&
        playingPostId != settledPostId &&
        panelContent == ReaderPanelContent.Actions

    Box(
      modifier =
        Modifier.padding(bottom = readerActionsPanelBottomInset())
          .padding(horizontal = 16.dp)
          .widthIn(max = 640.dp)
          .graphicsLayer {
            translationY = recoil.value
            shadowElevation = 16.dp.toPx()
            shape = ReaderActionsPanelShape
            clip = true
            this.ambientShadowColor = ambientShadowColor
            this.spotShadowColor = spotShadowColor
          }
          .pointerInput(Unit) {}
          .pointerInput(panelContent, isPodcastAvailable, settledPostId, playingPostId) {
            var dragTotal = 0f
            val threshold = 24.dp.toPx()
            detectVerticalDragGestures(
              onDragStart = { dragTotal = 0f },
              onDragEnd = {
                when {
                  dragTotal < -threshold && panelContent == ReaderPanelContent.Actions -> {
                    when {
                      isPodcastAvailable -> openPlayer(settledPostId)
                      playingPostId != null -> openPlayer(playingPostId)
                    }
                  }
                  dragTotal > threshold && panelContent != ReaderPanelContent.Actions ->
                    collapsePanel()
                }
              },
            ) { _, dragAmount ->
              dragTotal += dragAmount
            }
          }
          .drawWithCache {
            val outline = ReaderActionsPanelShape.createOutline(size, layoutDirection, this)
            val borderStroke = Stroke(width = 1.dp.toPx())
            onDrawWithContent {
              drawRect(colorScheme.bottomSheet)
              drawContent()
              drawOutline(
                outline = outline,
                color = colorScheme.bottomSheetBorder,
                style = borderStroke,
              )
            }
          }
    ) {
      ReaderActionsPanelTheme(overriddenColorScheme = overriddenColorScheme) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          AnimatedVisibility(
            visible = showDockedStrip,
            enter =
              fadeIn(tween(durationMillis = 180, delayMillis = 60)) +
                expandIn(
                  animationSpec = PanelSizeTransformSpec,
                  expandFrom = Alignment.BottomCenter,
                  initialSize = { fullSize -> IntSize((fullSize.width * 0.6f).roundToInt(), 0) },
                ),
            exit =
              fadeOut(tween(durationMillis = 100)) +
                shrinkOut(
                  animationSpec = PanelSizeTransformSpec,
                  shrinkTowards = Alignment.BottomCenter,
                  targetSize = { fullSize -> IntSize((fullSize.width * 0.6f).roundToInt(), 0) },
                ),
          ) {
            DockedNowPlayingStrip(
              coverUrl = activeAudio.coverUrl,
              title = activeAudio.title,
              artist = activeAudio.artist,
              isPlaying = activeAudio.isPlaying,
              isBuffering = activeAudio.isBuffering,
              progress = activeProgress,
              onOpenClick = { playingPostId?.let { openPlayer(it) } },
              onPlayPauseClick = {
                if (activeAudio.isPlaying) {
                  onPauseClick()
                } else {
                  onResumeClick()
                }
              },
              onStopClick = onStopClick,
            )
          }

          AnimatedContent(
            modifier = Modifier.requiredHeightIn(min = READER_ACTIONS_PANEL_COLLAPSED_HEIGHT),
            contentAlignment = Alignment.BottomCenter,
            targetState = panelContent,
            transitionSpec = {
              fadeIn(tween(durationMillis = 220, delayMillis = 90)) togetherWith
                fadeOut(tween(durationMillis = 90)) using
                SizeTransform { _, _ -> PanelSizeTransformSpec }
            },
          ) { targetState ->
            when (targetState) {
              ReaderPanelContent.Customizations -> {
                ReaderCustomizationsContent(
                  selectedFont = selectedFont,
                  selectedThemeVariant = selectedThemeVariant,
                  fontScaleFactor = fontScaleFactor,
                  fontLineHeightFactor = fontLineHeightFactor,
                  isSubscribed = isSubscribed,
                  isParentThemeDark = isParentThemeDark,
                  onFontChange = onFontChange,
                  onThemeVariantChange = onThemeVariantChange,
                  onFontScaleFactorChange = onFontScaleFactorChange,
                  onFontLineHeightFactorChange = onFontLineHeightFactorChange,
                )
              }
              is ReaderPanelContent.Player -> {
                ReaderPanelPlayer(
                  playbackState = playbackState,
                  targetPostId = targetState.targetPostId,
                  settledPostId = settledPostId,
                  onPlayClick = onPlayClick,
                  onPauseClick = onPauseClick,
                  onResumeClick = onResumeClick,
                  onSeek = onSeek,
                  onSeekForward = onSeekForward,
                  onSeekBackward = onSeekBackward,
                  onPlaybackSpeedChange = onPlaybackSpeedChange,
                  onSleepTimerOptionSelected = onSleepTimerOptionSelected,
                  onCollapsePanel = collapsePanel,
                )
              }
              ReaderPanelContent.Actions -> {
                val isSettledLoaded = activeAudio.playingPostId == settledPostId
                val pillMode =
                  when (contentType) {
                    ReaderContentType.Article ->
                      ReaderPillMode.Article(loadFullArticle = showFullArticle)
                    is ReaderContentType.Podcast -> {
                      if (contentType.isPlayerAvailable && !isHeroVisible) {
                        val fallbackProgress =
                          if (settledPost.audioDuration > 0L) {
                            (settledPost.audioProgress.toFloat() / settledPost.audioDuration)
                              .coerceIn(0f, 1f)
                          } else {
                            0f
                          }
                        ReaderPillMode.PodcastMiniPlayer(
                          coverUrl = resolvedCoverArtUrl,
                          isPlaying = isSettledLoaded && activeAudio.isPlaying,
                          isBuffering = isSettledLoaded && activeAudio.isBuffering,
                          progress = if (isSettledLoaded) activeProgress else fallbackProgress,
                        )
                      } else {
                        ReaderPillMode.PodcastUtility(showFullArticle = showFullArticle)
                      }
                    }
                    is ReaderContentType.Video -> ReaderPillMode.Video
                  }

                ReaderActionPill(
                  mode = pillMode,
                  postLink = settledPost.link,
                  selectedAppColorScheme = overriddenColorScheme,
                  openInBrowserClick = openInBrowserClick,
                  loadFullArticleClick = loadFullArticleClick,
                  watchVideoClick = watchVideoClick,
                  openReaderViewSettings = openReaderViewSettings,
                  onMiniPlayPauseClick = {
                    if (isSettledLoaded) {
                      if (activeAudio.isPlaying) {
                        onPauseClick()
                      } else {
                        onResumeClick()
                      }
                    } else {
                      onPlayClick()
                    }
                  },
                  onExpandPlayerClick = { openPlayer(settledPostId) },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Immutable
private data class ActiveAudioSnapshot(
  val playingPostId: String? = null,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
  val coverUrl: String? = null,
  val title: String? = null,
  val artist: String? = null,
)

private fun PlaybackState.toActiveAudioSnapshot(): ActiveAudioSnapshot =
  ActiveAudioSnapshot(
    playingPostId = playingPostId,
    isPlaying = isPlaying,
    isBuffering = buffering,
    coverUrl = coverUrl,
    title = title,
    artist = artist,
  )

private fun PlaybackState.toPlaybackFraction(): Float =
  if (duration > 0L) {
    (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
  } else {
    0f
  }

@Composable
private fun ReaderActionsPanelTheme(
  overriddenColorScheme: AppColorScheme?,
  content: @Composable () -> Unit,
) {
  val useAmoled = LocalAmoledSetting.current
  val dynamicColorState = LocalDynamicColorState.current
  val sourceColorScheme = overriddenColorScheme ?: dynamicColorState.darkAppColorScheme
  val colorScheme =
    if (!useAmoled) {
      sourceColorScheme
    } else {
      val amoledColorScheme =
        remember(sourceColorScheme) {
          val initialValues = sourceColorScheme.peekValues()
          AppColorScheme(initialValues).apply {
            updateFromWithAnimationState(
              values = initialValues,
              amoled = true,
              isAnimating = sourceColorScheme.isAnimating,
            )
          }
        }

      SideEffect {
        amoledColorScheme.updateFromWithAnimationState(
          values = sourceColorScheme.peekValues(),
          amoled = true,
          isAnimating = sourceColorScheme.isAnimating,
        )
      }

      DisposableEffect(sourceColorScheme, amoledColorScheme) {
        val disposeObserver =
          sourceColorScheme.observeValues { values, isAnimating ->
            amoledColorScheme.updateFromWithAnimationState(
              values = values,
              amoled = true,
              isAnimating = isAnimating,
            )
          }
        onDispose { disposeObserver() }
      }

      amoledColorScheme
    }

  val localTranslucentStyles =
    remember(colorScheme) {
      TranslucentStyles(
        default =
          TranslucentStyle(
            backgroundGetter = { colorScheme.onSurface.copy(alpha = 0.08f) },
            foregroundGetter = { colorScheme.onSurface },
            outlineGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
          ),
        prominent =
          TranslucentStyle(
            backgroundGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
            foregroundGetter = { colorScheme.onSurface },
            outlineGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
          ),
      )
    }

  @Suppress("DEPRECATION")
  val rippleConfiguration =
    remember(colorScheme.settledSecondary) {
      RippleConfiguration(color = colorScheme.settledSecondary, rippleAlpha = DefaultRippleAlpha)
    }

  CompositionLocalProvider(LocalIsDarkTheme provides true) {
    MaterialTheme(colorScheme = PanelDarkMaterialColorScheme, typography = DefaultTypography) {
      CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalRippleConfiguration provides rippleConfiguration,
        LocalTranslucentStyles provides localTranslucentStyles,
      ) {
        content()
      }
    }
  }
}

private fun PlaybackState.toSelectedSleepTimerOption(targetPostId: String): SleepTimerOption =
  if (playingPostId == targetPostId) selectedSleepTimerOption else SleepTimerOption.None

@Composable
private fun ReaderPanelPlayer(
  playbackState: StateFlow<PlaybackState>,
  targetPostId: String,
  settledPostId: String,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
  onCollapsePanel: () -> Unit,
) {
  var showSleepTimerSheet by remember { mutableStateOf(false) }

  LaunchedEffect(playbackState, targetPostId, settledPostId) {
    if (targetPostId != settledPostId) {
      playbackState
        .map { it.playingPostId }
        .distinctUntilChanged()
        .collect { playingId ->
          if (playingId == null) {
            onCollapsePanel()
          }
        }
    }
  }

  MediaControls(
    playbackStateFlow = playbackState,
    settledPostId = targetPostId,
    onPlayClick = {
      if (playbackState.value.playingPostId == targetPostId) {
        onResumeClick()
      } else if (targetPostId == settledPostId) {
        onPlayClick()
      }
    },
    onPauseClick = onPauseClick,
    onSeek = onSeek,
    onSeekForward = onSeekForward,
    onSeekBackward = onSeekBackward,
    onPlaybackSpeedChange = { currentSpeed ->
      onPlaybackSpeedChange(nextPlaybackSpeed(currentSpeed))
    },
    onSleepTimerClick = { showSleepTimerSheet = true },
  )

  if (showSleepTimerSheet) {
    val initialSelectedOption =
      remember(playbackState, targetPostId) {
        playbackState.value.toSelectedSleepTimerOption(targetPostId)
      }
    val selectedOption by
      remember(playbackState, targetPostId) {
          playbackState
            .map { state -> state.toSelectedSleepTimerOption(targetPostId) }
            .distinctUntilChanged()
        }
        .collectAsStateWithLifecycle(initialSelectedOption)

    SleepTimerBottomSheet(
      selectedOption = selectedOption,
      onOptionSelected = {
        onSleepTimerOptionSelected(it)
        showSleepTimerSheet = false
      },
      onDismiss = { showSleepTimerSheet = false },
    )
  }
}

private val Transition<*>.isTransitioning: Boolean
  get() = isRunning || currentState != targetState

internal fun resolveInitialReaderPage(
  initialPostIndex: Int,
  initialPostId: String,
  itemCount: Int,
  peekPostId: (Int) -> String?,
): Int {
  if (itemCount <= 0) return 0
  val clampedIndex = initialPostIndex.coerceIn(0, itemCount - 1)
  if (initialPostId.isBlank()) return clampedIndex

  if (peekPostId(clampedIndex) == initialPostId) {
    return clampedIndex
  }

  for (index in 0 until itemCount) {
    if (peekPostId(index) == initialPostId) {
      return index
    }
  }

  return clampedIndex
}

private const val ANCHOR_SEARCH_RADIUS = 25

private fun LazyPagingItems<ResolvedPost>.indexOfPostNear(
  postId: String,
  page: Int,
  itemCount: Int,
): Int? {
  for (offset in 0..ANCHOR_SEARCH_RADIUS) {
    for (index in intArrayOf(page - offset, page + offset)) {
      if (index < 0 || index >= itemCount) continue
      if (runCatching { peek(index) }.getOrNull()?.id == postId) return index
    }
  }
  return null
}

private fun LazyPagingItems<ResolvedPost>.indexOfPost(postId: String, itemCount: Int): Int? {
  for (index in 0 until itemCount) {
    if (runCatching { peek(index) }.getOrNull()?.id == postId) return index
  }
  return null
}

private val READER_ACTIONS_PANEL_COLLAPSED_HEIGHT = 64.dp
private val READER_ACTIONS_PANEL_RECOIL = 12.dp

@Composable
private fun readerActionsPanelBottomInset(): Dp =
  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp)
