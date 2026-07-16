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
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
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
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ui.ReaderPage
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
import kotlinx.coroutines.flow.debounce
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
    isBackEnabled = state.showReaderCustomisations,
  ) {
    viewModel.dispatch(ReaderEvent.HideReaderCustomisations)
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
  val scrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
      val nestedScrollModifier =
        if (platform !is Platform.Desktop) {
          Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection)
        } else {
          Modifier
        }

      Scaffold(
        modifier =
          modifier
            .fillMaxSize()
            .then(nestedScrollModifier)
            .focusRequester(readerFocusRequester)
            .focusable()
            .onKeyEvent { event ->
              if (event.type != KeyEventType.KeyUp) return@onKeyEvent false

              val currentReaderPost = runCatching { posts.peek(pagerState.settledPage) }.getOrNull()

              return@onKeyEvent when (event.key) {
                Key.DirectionRight -> {
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                  }

                  true
                }
                Key.DirectionLeft -> {
                  coroutineScope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                  }

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
          val scrollBehavior =
            if (platform !is Platform.Desktop) {
              scrollBehaviour
            } else {
              null
            }

          CenterAlignedTopAppBar(
            expandedHeight = 72.dp,
            scrollBehavior = scrollBehavior,
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
        bottomBar = {
          val readerPost =
            try {
              posts.peek(pagerState.settledPage)
            } catch (_: IndexOutOfBoundsException) {
              null
            }
          if (readerPost != null) {
            val pageViewModel = pageViewModelFactory.invoke(readerPost)
            val showFullArticle by pageViewModel.showFullArticle.collectAsStateWithLifecycle()

            ReaderActionsPanel(
              isParentThemeDark = isParentThemeDark,
              isDarkTheme = isDarkTheme,
              loadFullArticle = showFullArticle,
              showReaderCustomisations = state.showReaderCustomisations,
              selectedFont = state.selectedReaderFont,
              selectedThemeVariant = state.selectedThemeVariant,
              fontScaleFactor = state.readerFontScaleFactor,
              fontLineHeightFactor = state.readerLineHeightScaleFactor,
              isSubscribed = state.isSubscribed,
              overriddenColorScheme = overriddenDarkColorScheme,
              openInBrowserClick = {
                coroutineScope.launch { linkHandler.openLink(readerPost.link) }
              },
              loadFullArticleClick = { pageViewModel.toggleFullArticle() },
              openReaderViewSettings = { viewModel.dispatch(ReaderEvent.ShowReaderCustomisations) },
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppTheme.colorScheme.backdrop,
        contentColor = Color.Unspecified,
      ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().iosBottomSafeAreaPadding()) {
          val layoutDirection = LocalLayoutDirection.current
          val sizeClass = LocalWindowSizeClass.current
          val readerContentMaxWidth =
            if (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
              960.dp
            } else {
              700.dp
            }

          val backdropColor = AppTheme.colorScheme.backdrop
          val scrimHeightPx = with(LocalDensity.current) { 96.dp.toPx() }
          HorizontalPager(
            modifier =
              Modifier.widthIn(max = readerContentMaxWidth)
                .fillMaxSize()
                .align(Alignment.Center)
                .drawWithContent {
                  drawContent()
                  drawRect(
                    brush =
                      Brush.verticalGradient(
                        colors = listOf(backdropColor, Color.Transparent),
                        startY = 0f,
                        endY = scrimHeightPx,
                      )
                  )
                  drawRect(
                    brush =
                      Brush.verticalGradient(
                        colors = listOf(Color.Transparent, backdropColor),
                        startY = size.height - scrimHeightPx,
                        endY = size.height,
                      )
                  )
                },
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
                contentPaddingValues = paddingValues,
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

          if (state.showReaderCustomisations) {
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .pointerInput(Unit) {
                    detectTapGestures { viewModel.dispatch(ReaderEvent.HideReaderCustomisations) }
                  }
                  .align(Alignment.BottomCenter)
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ReaderActionsPanel(
  isParentThemeDark: Boolean,
  isDarkTheme: Boolean,
  loadFullArticle: Boolean,
  showReaderCustomisations: Boolean,
  selectedFont: ReaderFont,
  selectedThemeVariant: ThemeVariant,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  isSubscribed: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  onFontChange: (ReaderFont) -> Unit,
  onThemeVariantChange: (ThemeVariant) -> Unit,
  onFontScaleFactorChange: (Float) -> Unit,
  onFontLineHeightFactorChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
  overriddenColorScheme: AppColorScheme? = null,
) {
  val colorScheme = AppTheme.colorScheme

  LookaheadScope {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .wrapContentHeight()
          .animateBounds(
            lookaheadScope = this,
            boundsTransform = { _, _ ->
              spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
              )
            },
          )
          .then(modifier),
      contentAlignment = Alignment.Center,
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

      Box(
        modifier =
          Modifier.padding(
              bottom =
                WindowInsets.navigationBars
                  .asPaddingValues()
                  .calculateBottomPadding()
                  .coerceAtLeast(16.dp)
            )
            .padding(horizontal = 16.dp)
            .widthIn(max = 640.dp)
            .pointerInput(Unit) {}
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
            modifier = Modifier.requiredHeightIn(min = 64.dp),
            contentAlignment = Alignment.BottomCenter,
            targetState = showReaderCustomisations,
          ) { targetState ->
            if (targetState) {
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
        }
      }
    }
  }
}
