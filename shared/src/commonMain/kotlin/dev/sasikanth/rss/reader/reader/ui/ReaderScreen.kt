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
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
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
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.markdown.SafeMarkdownCheckBox
import dev.sasikanth.rss.reader.markdown.SafeMarkdownHeader
import dev.sasikanth.rss.reader.markdown.SafeMarkdownParagraph
import dev.sasikanth.rss.reader.markdown.SafeMarkdownText
import dev.sasikanth.rss.reader.markdown.safeUnescapedTextInNode
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPanelContent
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ui.MediaControls
import dev.sasikanth.rss.reader.reader.page.ui.ReaderPage
import dev.sasikanth.rss.reader.reader.page.ui.SleepTimerBottomSheet
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.CollapseContent
import dev.sasikanth.rss.reader.resources.icons.ExpandContent
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ComicNeueFontFamily
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.ui.GoogleSansFontFamily
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.LoraFontFamily
import dev.sasikanth.rss.reader.ui.MerriWeatherFontFamily
import dev.sasikanth.rss.reader.ui.OutfitFontFamily
import dev.sasikanth.rss.reader.ui.RobotoSerifFontFamily
import dev.sasikanth.rss.reader.ui.getOverriddenColorScheme
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.ui.systemDynamicColorScheme
import dev.sasikanth.rss.reader.ui.typography
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.iosBottomSafeAreaPadding
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.intellij.markdown.MarkdownTokenTypes
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonClose
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.readerCollapseScreen
import twine.shared.generated.resources.readerExpandScreen
import twine.shared.generated.resources.readerPageCount

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
internal fun ReaderScreen(
  viewModel: ReaderViewModel,
  pageViewModelFactory: @Composable (ResolvedPost) -> ReaderPageViewModel,
  onBack: () -> Unit,
  openPaywall: () -> Unit,
  onImageClick: (String) -> Unit,
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

  val defaultLight = remember { appDynamicColorState.lightAppColorScheme }
  val defaultDark = remember { appDynamicColorState.darkAppColorScheme }
  val articleDynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = defaultLight.toValues(),
      defaultDarkAppColorScheme = defaultDark.toValues(),
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

  val readerFocusRequester = remember { FocusRequester() }
  if (platform is Platform.Desktop) {
    LaunchedEffect(Unit) { readerFocusRequester.requestFocus() }
  }

  // If the pager lays out before paging delivers its first item count, the initial
  // page gets clamped to 0 and the requested index is lost. Restore it exactly once,
  // as soon as the count is available, unless the user has already started swiping.
  val initialPostIndex = remember { state.activePostIndex }
  var initialPageRestored by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(posts.itemCount) {
    if (!initialPageRestored && posts.itemCount > 0) {
      if (
        initialPostIndex in 0 until posts.itemCount &&
          pagerState.currentPage != initialPostIndex &&
          !pagerState.isScrollInProgress
      ) {
        pagerState.scrollToPage(initialPostIndex)
      }
      initialPageRestored = true
    }
  }

  // Publish the active post from the settled page rather than per-page visibility:
  // onVisibilityChanged ignores parent clipping, so in a split layout the pager's
  // beyond-viewport neighbor overlaps the list pane and falsely reports as visible.
  // The debounce keeps the initial page-restore scroll from publishing page 0.
  LaunchedEffect(pagerState, posts) {
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

  // In a split layout the list pane keeps the app theme, so article-based dynamic colors
  // in the reader pane would clash with it.
  val isDynamicColorEnabled =
    state.selectedThemeVariant == ThemeVariant.Dynamic && !showCloseNavIcon

  LaunchedEffect(isDynamicColorEnabled) {
    if (!isDynamicColorEnabled) {
      articleDynamicColorState.reset()
    }
  }

  pagerState.CollectItemTransition(
    posts.itemCount,
    isDynamicColorEnabled,
    itemProvider = { index ->
      if (shouldBlockImage || posts.itemCount == 0) null else posts.peek(index)
    },
  ) { fromItem, toItem, offset ->
    if (isDynamicColorEnabled) {
      val fromSeedColor = seedColorExtractor.calculateSeedColor(url = fromItem?.imageUrl)
      val toSeedColor = seedColorExtractor.calculateSeedColor(url = toItem?.imageUrl)

      articleDynamicColorState.animate(
        fromSeedColor = fromSeedColor,
        toSeedColor = toSeedColor,
        progress = offset,
      )
    }
  }
  LaunchedEffect(state.openPaywall) {
    if (state.openPaywall) {
      openPaywall()
      viewModel.dispatch(ReaderEvent.MarkOpenPaywallDone)
    }
  }

  LaunchedEffect(exitScreen) {
    if (exitScreen) {
      onBack()
    }
  }

  LaunchedEffect(posts.itemCount, posts.loadState.refresh) {
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

  LaunchedEffect(isDarkTheme) {
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
    typography(
      fontFamily = fontFamily,
      fontScalingFactor = state.readerFontScaleFactor,
      lineHeightScalingFactor = state.readerLineHeightScaleFactor,
    )

  CompositionLocalProvider(
    LocalDynamicColorState provides articleDynamicColorState,
    LocalUriHandler provides readerLinkHandler,
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
      Scaffold(
        modifier =
          modifier.fillMaxSize().focusRequester(readerFocusRequester).focusable().onKeyEvent { event
            ->
            if (event.type != KeyEventType.KeyUp) return@onKeyEvent false

            val currentReaderPost = runCatching { posts.peek(pagerState.settledPage) }.getOrNull()

            return@onKeyEvent when (event.key) {
              Key.DirectionRight -> {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }

                true
              }
              Key.DirectionLeft -> {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }

                true
              }
              Key.B if currentReaderPost != null -> {
                viewModel.dispatch(
                  ReaderEvent.TogglePostBookmark(
                    postId = currentReaderPost.id,
                    currentBookmarkStatus = currentReaderPost.bookmarked,
                  )
                )

                true
              }
              Key.U if currentReaderPost != null -> {
                viewModel.dispatch(ReaderEvent.OnMarkAsUnread(postId = currentReaderPost.id))

                true
              }
              Key.V if currentReaderPost != null -> {
                coroutineScope.launch { linkHandler.openLink(currentReaderPost.link) }

                true
              }
              Key.Escape -> {
                onBack()

                true
              }
              else -> false
            }
          },
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
            title = {
              if (pagerState.pageCount > 1) {
                val pageIndicatorState = remember {
                  object : PageIndicatorState {
                    override val pageOffset: Float
                      get() = pagerState.currentPageOffsetFraction

                    override val selectedPage: Int
                      get() = pagerState.currentPage

                    override val pageCount: Int
                      get() = pagerState.pageCount
                  }
                }

                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                  Text(
                    text =
                      stringResource(
                        Res.string.readerPageCount,
                        pagerState.currentPage + 1,
                        pagerState.pageCount,
                      ),
                    color = AppTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                  )

                  HorizontalPageIndicators(pageIndicatorState = pageIndicatorState)
                }
              }
            },
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
        containerColor = AppTheme.colorScheme.backdrop,
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

          val readerContentPadding =
            PaddingValues(
              top = paddingValues.calculateTopPadding(),
              bottom = readerActionsPanelBottomInset() + READER_ACTIONS_PANEL_COLLAPSED_HEIGHT,
            )

          HorizontalPager(
            modifier =
              Modifier.widthIn(max = readerContentMaxWidth)
                .fillMaxSize()
                .align(Alignment.Center)
                .iosBottomSafeAreaPadding(),
            state = pagerState,
            overscrollEffect = null,
            beyondViewportPageCount = 1,
            contentPadding =
              PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
              ),
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
              val highlightsBuilder =
                remember(isDarkTheme) {
                  Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDarkTheme))
                }
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
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h1,
                      )
                    },
                    heading2 = { cm ->
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h2,
                      )
                    },
                    heading3 = { cm ->
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h3,
                      )
                    },
                    heading4 = { cm ->
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h4,
                      )
                    },
                    heading5 = { cm ->
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h5,
                      )
                    },
                    heading6 = { cm ->
                      SafeMarkdownHeader(
                        content = cm.content,
                        node = cm.node,
                        style = cm.typography.h6,
                      )
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
                    codeBlock = { cm ->
                      MarkdownHighlightedCodeBlock(
                        content = cm.content,
                        node = cm.node,
                        highlightsBuilder = highlightsBuilder,
                        showHeader = true,
                      )
                    },
                    codeFence = { cm ->
                      MarkdownHighlightedCodeFence(
                        content = cm.content,
                        node = cm.node,
                        highlightsBuilder = highlightsBuilder,
                        showHeader = true,
                      )
                    },
                  )
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
              )
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

          val settledPost =
            try {
              posts.peek(pagerState.settledPage)
            } catch (_: IndexOutOfBoundsException) {
              null
            }
          if (settledPost != null) {
            val settledPageViewModel = pageViewModelFactory.invoke(settledPost)
            val showFullArticle by
              settledPageViewModel.showFullArticle.collectAsStateWithLifecycle()

            ReaderActionsPanel(
              modifier = Modifier.align(Alignment.BottomCenter),
              isParentThemeDark = isParentThemeDark,
              isDarkTheme = isDarkTheme,
              loadFullArticle = showFullArticle,
              panelContent = state.panelContent,
              isAudioPost = !settledPost.audioUrl.isNullOrBlank(),
              isAudioPlayerAvailable = settledPageViewModel.audioPlayer.isAvailable,
              playbackState = viewModel.playbackState,
              settledPostId = settledPost.id,
              selectedFont = state.selectedReaderFont,
              selectedThemeVariant = state.selectedThemeVariant,
              fontScaleFactor = state.readerFontScaleFactor,
              fontLineHeightFactor = state.readerLineHeightScaleFactor,
              isSubscribed = state.isSubscribed,
              overriddenColorScheme = overriddenDarkColorScheme,
              openInBrowserClick = {
                coroutineScope.launch { linkHandler.openLink(settledPost.link) }
              },
              loadFullArticleClick = { settledPageViewModel.toggleFullArticle() },
              openReaderViewSettings = {
                viewModel.dispatch(ReaderEvent.ShowPanelContent(ReaderPanelContent.Customizations))
              },
              openPlayer = {
                viewModel.dispatch(ReaderEvent.ShowPanelContent(ReaderPanelContent.Player))
              },
              collapsePanel = { viewModel.dispatch(ReaderEvent.CollapsePanel) },
              onPlayClick = { settledPageViewModel.playAudio() },
              onPauseClick = viewModel::pauseAudio,
              onResumeClick = viewModel::resumeAudio,
              onSeek = viewModel::seekAudio,
              onSeekForward = viewModel::seekForward,
              onSeekBackward = viewModel::seekBackward,
              onPlaybackSpeedChange = viewModel::setPlaybackSpeed,
              onSleepTimerOptionSelected = viewModel::setSleepTimer,
              onFontChange = { font -> viewModel.dispatch(ReaderEvent.UpdateReaderFont(font)) },
              onThemeVariantChange = { themeVariant ->
                viewModel.dispatch(ReaderEvent.UpdateThemeVariant(themeVariant))
              },
              onFontScaleFactorChange = { fontScaleFactor ->
                viewModel.dispatch(ReaderEvent.UpdateFontScaleFactor(fontScaleFactor))
              },
              onFontLineHeightFactorChange = { fontLineHeightFactor ->
                viewModel.dispatch(ReaderEvent.UpdateFontLineHeightFactor(fontLineHeightFactor))
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReaderActionsPanel(
  isParentThemeDark: Boolean,
  isDarkTheme: Boolean,
  loadFullArticle: Boolean,
  panelContent: ReaderPanelContent,
  isAudioPost: Boolean,
  isAudioPlayerAvailable: Boolean,
  playbackState: StateFlow<PlaybackState>,
  settledPostId: String,
  selectedFont: ReaderFont,
  selectedThemeVariant: ThemeVariant,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  isSubscribed: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  openPlayer: () -> Unit,
  collapsePanel: () -> Unit,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
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

  Box(
    modifier = Modifier.fillMaxWidth().wrapContentHeight().then(modifier),
    contentAlignment = Alignment.BottomCenter,
  ) {
    val (shadowColor1, shadowColor2) =
      remember(isDarkTheme) {
        if (isDarkTheme) {
          Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
        } else {
          Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
        }
      }
    val backgroundShape = RoundedCornerShape(36.dp)

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
      recoil.animateTo(
        targetValue = 0f,
        animationSpec =
          spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
      )
    }

    Box(
      modifier =
        Modifier.graphicsLayer { translationY = recoil.value }
          .padding(bottom = readerActionsPanelBottomInset())
          .padding(horizontal = 16.dp)
          .widthIn(max = 640.dp)
          .pointerInput(Unit) {}
          .pointerInput(panelContent, isAudioPost) {
            var dragTotal = 0f
            val threshold = 24.dp.toPx()
            detectVerticalDragGestures(
              onDragStart = { dragTotal = 0f },
              onDragEnd = {
                when {
                  dragTotal < -threshold &&
                    panelContent == ReaderPanelContent.Actions &&
                    isAudioPost -> openPlayer()
                  dragTotal > threshold && panelContent != ReaderPanelContent.Actions ->
                    collapsePanel()
                }
              },
            ) { _, dragAmount ->
              dragTotal += dragAmount
            }
          }
          .dropShadow(shape = backgroundShape) {
            offset = Offset(x = 0f, y = 16.dp.toPx())
            radius = 32.dp.toPx()
            color = shadowColor1
          }
          .dropShadow(shape = backgroundShape) {
            offset = Offset(x = 0f, y = 4.dp.toPx())
            radius = 8.dp.toPx()
            color = shadowColor2
          }
          .clip(backgroundShape)
          .drawBehind { drawRect(colorScheme.bottomSheet) }
          .drawWithContent {
            drawContent()
            val outline = backgroundShape.createOutline(size, layoutDirection, this)
            drawOutline(
              outline = outline,
              color = colorScheme.bottomSheetBorder,
              style = Stroke(width = 1.dp.toPx()),
            )
          }
          .graphicsLayer { clip = true }
    ) {
      AppTheme(useDarkTheme = true, overriddenColorScheme = overriddenColorScheme) {
        AnimatedContent(
          modifier = Modifier.requiredHeightIn(min = READER_ACTIONS_PANEL_COLLAPSED_HEIGHT),
          contentAlignment = Alignment.BottomCenter,
          targetState = panelContent,
          transitionSpec = {
            fadeIn(tween(durationMillis = 220, delayMillis = 90)) togetherWith
              fadeOut(tween(durationMillis = 90)) using
              SizeTransform { _, _ ->
                spring(
                  dampingRatio = Spring.DampingRatioLowBouncy,
                  stiffness = Spring.StiffnessMediumLow,
                  visibilityThreshold = IntSize.VisibilityThreshold,
                )
              }
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
            ReaderPanelContent.Player -> {
              ReaderPanelPlayer(
                playbackState = playbackState,
                settledPostId = settledPostId,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
                onSeek = onSeek,
                onSeekForward = onSeekForward,
                onSeekBackward = onSeekBackward,
                onPlaybackSpeedChange = onPlaybackSpeedChange,
                onSleepTimerOptionSelected = onSleepTimerOptionSelected,
              )
            }
            ReaderPanelContent.Actions -> {
              ReaderPanelActions(
                overriddenColorScheme = overriddenColorScheme,
                loadFullArticle = loadFullArticle,
                isAudioPost = isAudioPost && isAudioPlayerAvailable,
                playbackState = playbackState,
                settledPostId = settledPostId,
                openInBrowserClick = openInBrowserClick,
                loadFullArticleClick = loadFullArticleClick,
                openReaderViewSettings = openReaderViewSettings,
                openPlayer = openPlayer,
                onPlayClick = onPlayClick,
                onPauseClick = onPauseClick,
                onResumeClick = onResumeClick,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ReaderPanelActions(
  overriddenColorScheme: AppColorScheme?,
  loadFullArticle: Boolean,
  isAudioPost: Boolean,
  playbackState: StateFlow<PlaybackState>,
  settledPostId: String,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  openPlayer: () -> Unit,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
) {
  val flags by
    remember(playbackState, settledPostId) {
        playbackState
          .map { state ->
            val isSettledPostLoaded = state.playingPostId == settledPostId
            ReaderPlaybackFlags(
              isSettledPostLoaded = isSettledPostLoaded,
              isPlaying = isSettledPostLoaded && state.isPlaying,
              isBuffering = isSettledPostLoaded && state.buffering,
            )
          }
          .distinctUntilChanged()
      }
      .collectAsStateWithLifecycle(ReaderPlaybackFlags())

  if (isAudioPost) {
    ReaderAudioBottomBar(
      selectedAppColorScheme = overriddenColorScheme,
      isPlaying = flags.isPlaying,
      isBuffering = flags.isBuffering,
      openInBrowserClick = openInBrowserClick,
      playPauseClick = {
        if (flags.isPlaying) {
          onPauseClick()
        } else {
          if (flags.isSettledPostLoaded) onResumeClick() else onPlayClick()
          openPlayer()
        }
      },
      openReaderViewSettings = openReaderViewSettings,
    )
  } else {
    ReaderViewBottomBar(
      selectedAppColorScheme = overriddenColorScheme,
      loadFullArticle = loadFullArticle,
      openInBrowserClick = openInBrowserClick,
      loadFullArticleClick = loadFullArticleClick,
      openReaderViewSettings = openReaderViewSettings,
    )
  }
}

@Immutable
private data class ReaderPlaybackFlags(
  val isSettledPostLoaded: Boolean = false,
  val isPlaying: Boolean = false,
  val isBuffering: Boolean = false,
)

@Composable
private fun ReaderPanelPlayer(
  playbackState: StateFlow<PlaybackState>,
  settledPostId: String,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onResumeClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
) {
  val playerState by playbackState.collectAsStateWithLifecycle(PlaybackState.Idle)
  var showSleepTimerSheet by remember { mutableStateOf(false) }
  val isSettledPostLoaded = playerState.playingPostId == settledPostId
  val currentPlaybackState = if (isSettledPostLoaded) playerState else PlaybackState.Idle

  MediaControls(
    playbackState = currentPlaybackState,
    onPlayClick = { if (isSettledPostLoaded) onResumeClick() else onPlayClick() },
    onPauseClick = onPauseClick,
    onSeek = onSeek,
    onSeekForward = onSeekForward,
    onSeekBackward = onSeekBackward,
    onPlaybackSpeedChange = {
      val newSpeed =
        when (currentPlaybackState.playbackSpeed) {
          0.5f -> 1.0f
          1.0f -> 1.5f
          1.5f -> 2.0f
          2.0f -> 0.5f
          else -> 1.0f
        }
      onPlaybackSpeedChange(newSpeed)
    },
    onSleepTimerClick = { showSleepTimerSheet = true },
  )

  if (showSleepTimerSheet) {
    SleepTimerBottomSheet(
      playbackState = currentPlaybackState,
      onOptionSelected = {
        onSleepTimerOptionSelected(it)
        showSleepTimerSheet = false
      },
      onDismiss = { showSleepTimerSheet = false },
    )
  }
}

private val READER_ACTIONS_PANEL_COLLAPSED_HEIGHT = 64.dp
private val READER_ACTIONS_PANEL_RECOIL = 12.dp

@Composable
private fun readerActionsPanelBottomInset(): Dp =
  WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp)
