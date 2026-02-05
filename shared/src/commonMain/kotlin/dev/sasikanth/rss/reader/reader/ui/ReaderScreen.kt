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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import app.cash.paging.compose.collectAsLazyPagingItems
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ui.ReaderPage
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ComicNeueFontFamily
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.ui.GoogleSansFontFamily
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.LoraFontFamily
import dev.sasikanth.rss.reader.ui.MerriWeatherFontFamily
import dev.sasikanth.rss.reader.ui.RethinkSansFontFamily
import dev.sasikanth.rss.reader.ui.RobotoSerifFontFamily
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.ui.typography
import dev.sasikanth.rss.reader.utils.CollectItemTransition
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalDynamicColorEnabled
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack
import twine.shared.generated.resources.readerPageCount

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
internal fun ReaderScreen(
  viewModel: ReaderViewModel,
  pageViewModelFactory: @Composable (ResolvedPost) -> ReaderPageViewModel,
  onPostChanged: (Int, String) -> Unit,
  onBack: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val posts = state.posts.collectAsLazyPagingItems()
  val linkHandler = LocalLinkHandler.current
  val seedColorExtractor = LocalSeedColorExtractor.current
  val appDynamicColorState = LocalDynamicColorState.current
  val dynamicColorEnabled = LocalDynamicColorEnabled.current
  val shouldBlockImage = LocalBlockImage.current

  // Using theme colors as default from the home screen
  // before we create dynamic content theme
  val dynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = appDynamicColorState.lightAppColorScheme,
      defaultDarkAppColorScheme = appDynamicColorState.darkAppColorScheme,
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

  pagerState.CollectItemTransition(
    key = posts.itemCount,
    itemProvider = { index ->
      if (shouldBlockImage || posts.itemCount == 0) null else posts.peek(index)
    },
  ) { fromItem, toItem, offset ->
    val fromSeedColor = seedColorExtractor.calculateSeedColor(url = fromItem?.imageUrl)
    val toSeedColor = seedColorExtractor.calculateSeedColor(url = toItem?.imageUrl)

    if (dynamicColorEnabled) {
      dynamicColorState.animate(
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

  BackHandler(enabled = state.showReaderCustomisations) {
    viewModel.dispatch(ReaderEvent.HideReaderCustomisations)
  }

  CompositionLocalProvider(
    LocalDynamicColorState provides dynamicColorState,
    LocalUriHandler provides readerLinkHandler,
  ) {
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
        ReaderFont.RethinkSans -> RethinkSansFontFamily
      }
    val typography =
      typography(
        fontFamily = fontFamily,
        fontScalingFactor = state.readerFontScaleFactor,
        lineHeightScalingFactor = state.readerLineHeightScaleFactor,
      )

    val isParentThemeDark = AppTheme.isDark
    AppTheme(useDarkTheme = isParentThemeDark, typography = typography) {
      val isDarkTheme = AppTheme.isDark
      val nestedScrollModifier =
        if (platform !is Platform.Desktop) {
          Modifier.nestedScroll(scrollBehaviour.nestedScrollConnection)
        } else {
          Modifier
        }

      Scaffold(
        modifier =
          modifier.fillMaxSize().then(nestedScrollModifier).onKeyEvent { event ->
            return@onKeyEvent when {
              event.key == Key.DirectionRight && event.type == KeyEventType.KeyUp -> {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }

                true
              }
              event.key == Key.DirectionLeft && event.type == KeyEventType.KeyUp -> {
                coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }

                true
              }
              else -> false
            }
          },
        topBar = {
          val topBarScrimColor = AppTheme.colorScheme.backdrop
          val scrollBehavior =
            if (platform !is Platform.Desktop) {
              scrollBehaviour
            } else {
              null
            }

          CenterAlignedTopAppBar(
            modifier =
              Modifier.drawBehind {
                drawRect(
                  brush = Brush.verticalGradient(listOf(topBarScrimColor, Color.Transparent))
                )
              },
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
                icon = TwineIcons.ArrowBack,
                label = stringResource(Res.string.buttonGoBack),
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
              isDarkTheme = isDarkTheme,
              loadFullArticle = showFullArticle,
              showReaderCustomisations = state.showReaderCustomisations,
              selectedFont = state.selectedReaderFont,
              fontScaleFactor = state.readerFontScaleFactor,
              fontLineHeightFactor = state.readerLineHeightScaleFactor,
              openInBrowserClick = {
                coroutineScope.launch { linkHandler.openLink(readerPost.link) }
              },
              loadFullArticleClick = { pageViewModel.toggleFullArticle() },
              openReaderViewSettings = { viewModel.dispatch(ReaderEvent.ShowReaderCustomisations) },
              onFontChange = { font -> viewModel.dispatch(ReaderEvent.UpdateReaderFont(font)) },
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
        Box(modifier = Modifier.fillMaxSize()) {
          val layoutDirection = LocalLayoutDirection.current
          val sizeClass = LocalWindowSizeClass.current
          val readerContentMaxWidth =
            if (sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
              960.dp
            } else {
              700.dp
            }

          HorizontalPager(
            modifier =
              Modifier.widthIn(max = readerContentMaxWidth).fillMaxSize().align(Alignment.Center),
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
              val markdownComponents = remember {
                markdownComponents(
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
                modifier =
                  Modifier.fillMaxSize().onVisibilityChanged(minDurationMs = 200L) {
                    if (it) {
                      onPostChanged(page, readerPost.id)
                      viewModel.dispatch(ReaderEvent.PostPageChanged(page, readerPost))
                    }
                  },
                contentPaddingValues = paddingValues,
                pageViewModel = pageViewModel,
                readerPost = readerPost,
                showFullArticle = showFullArticle,
                page = page,
                pagerState = pagerState,
                markdownComponents = markdownComponents,
                isDarkTheme = isDarkTheme,
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
  isDarkTheme: Boolean,
  loadFullArticle: Boolean,
  showReaderCustomisations: Boolean,
  selectedFont: ReaderFont,
  fontScaleFactor: Float,
  fontLineHeightFactor: Float,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  onFontChange: (ReaderFont) -> Unit,
  onFontScaleFactorChange: (Float) -> Unit,
  onFontLineHeightFactorChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val navBarScrimColor = AppTheme.colorScheme.backdrop

  LookaheadScope {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .wrapContentHeight()
          .drawBehind {
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
          }
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
        if (isDarkTheme) {
          Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
        } else {
          Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
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
            .background(color = AppTheme.colorScheme.bottomSheet, shape = backgroundShape)
            .border(
              width = 1.dp,
              color = AppTheme.colorScheme.bottomSheetBorder,
              shape = backgroundShape,
            )
            .graphicsLayer { clip = true }
      ) {
        AppTheme(useDarkTheme = true) {
          AnimatedContent(
            modifier = Modifier.requiredHeightIn(min = 64.dp),
            contentAlignment = Alignment.BottomCenter,
            targetState = showReaderCustomisations,
          ) { targetState ->
            if (targetState) {
              ReaderCustomizationsContent(
                selectedFont = selectedFont,
                fontScaleFactor = fontScaleFactor,
                fontLineHeightFactor = fontLineHeightFactor,
                onFontChange = onFontChange,
                onFontScaleFactorChange = onFontScaleFactorChange,
                onFontLineHeightFactorChange = onFontLineHeightFactorChange,
              )
            } else {
              ReaderViewBottomBar(
                loadFullArticle,
                openInBrowserClick,
                loadFullArticleClick,
                openReaderViewSettings,
              )
            }
          }
        }
      }
    }
  }
}
