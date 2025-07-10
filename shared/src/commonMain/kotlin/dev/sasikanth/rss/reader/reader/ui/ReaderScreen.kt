/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import com.adamglin.composeshadow.dropShadow
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.data.repository.ReaderFont
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderViewModel
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ComicNeueFontFamily
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.ui.LocalAppColorScheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.LoraFontFamily
import dev.sasikanth.rss.reader.ui.MerriWeatherFontFamily
import dev.sasikanth.rss.reader.ui.RobotoSerifFontFamily
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.ui.typography
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class, FlowPreview::class)
@Composable
internal fun ReaderScreen(
  darkTheme: Boolean,
  viewModel: ReaderViewModel,
  onPostChanged: (Int) -> Unit,
  onBack: () -> Unit,
  openPaywall: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val state by viewModel.state.collectAsStateWithLifecycle()
  val posts = state.posts.collectAsLazyPagingItems()
  val linkHandler = LocalLinkHandler.current
  val seedColorExtractor = LocalSeedColorExtractor.current
  // Using theme colors as default from the home screen
  // before we create dynamic content theme
  val dynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = LocalAppColorScheme.current,
      defaultDarkAppColorScheme = LocalAppColorScheme.current,
      useTonalSpotScheme = true,
    )
  val readerLinkHandler = remember {
    object : UriHandler {
      override fun openUri(uri: String) {
        coroutineScope.launch { linkHandler.openLink(uri) }
      }
    }
  }
  val pagerState = rememberPagerState(initialPage = state.activePostIndex) { posts.itemCount }

  LaunchedEffect(pagerState, posts.loadState) {
    if (posts.itemCount == 0) return@LaunchedEffect

    snapshotFlow {
        runCatching {
            val settledPage = pagerState.settledPage
            pagerState.getOffsetFractionForPage(settledPage)
          }
          .getOrNull()
          ?: 0f
      }
      .collect { offset ->
        val settledPage = pagerState.settledPage
        val activePost = runCatching { posts.peek(settledPage) }.getOrNull()

        if (activePost == null) return@collect

        // The default snap position of the pager is 0.5f, that means the targetPage
        // state only changes after reaching half way point. We instead want it to scale
        // as we start swiping.
        //
        // Instead of using EPSILON for snap threshold, we are doing that calculation
        // as the page offset changes
        //
        val fromItem =
          if (offset < -EPSILON) {
            runCatching { posts.peek(settledPage - 1) }.getOrNull() ?: activePost
          } else {
            activePost
          }

        val toItem =
          if (offset > EPSILON) {
            runCatching { posts.peek(settledPage + 1) }.getOrNull() ?: activePost
          } else {
            activePost
          }

        val fromSeedColor =
          seedColorExtractor.calculateSeedColor(fromItem.imageUrl)?.let { Color(it) }
        val toSeedColor = seedColorExtractor.calculateSeedColor(toItem.imageUrl)?.let { Color(it) }

        dynamicColorState.animate(
          fromSeedColor = fromSeedColor,
          toSeedColor = toSeedColor,
          progress = offset
        )
      }
  }

  LaunchedEffect(state.openPaywall) {
    if (state.openPaywall) {
      openPaywall()
      viewModel.dispatch(ReaderEvent.MarkOpenPaywallDone)
    }
  }

  BackHandler(enabled = state.showReaderCustomisations) {
    viewModel.dispatch(ReaderEvent.HideReaderCustomisations)
  }

  CompositionLocalProvider(
    LocalDynamicColorState provides dynamicColorState,
    LocalUriHandler provides readerLinkHandler
  ) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val fontFamily =
      when (state.selectedReaderFont) {
        ReaderFont.ComicNeue -> ComicNeueFontFamily
        ReaderFont.Golos -> GolosFontFamily
        ReaderFont.Lora -> LoraFontFamily
        ReaderFont.Merriweather -> MerriWeatherFontFamily
        ReaderFont.RobotoSerif -> RobotoSerifFontFamily
      }
    val typography =
      typography(
        fontFamily = fontFamily,
        fontScalingFactor = state.readerFontScaleFactor,
        lineHeightScalingFactor = state.readerLineHeightScaleFactor,
      )

    AppTheme(useDarkTheme = darkTheme, typography = typography) {
      Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehaviour.nestedScrollConnection),
        topBar = {
          CenterAlignedTopAppBar(
            expandedHeight = 72.dp,
            scrollBehavior = scrollBehaviour,
            colors =
              TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
              ),
            navigationIcon = {
              Box(
                modifier =
                  Modifier.padding(start = 20.dp)
                    .requiredSize(40.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
                    .background(AppTheme.colorScheme.secondary.copy(0.08f), CircleShape)
                    .border(
                      width = 1.dp,
                      color = AppTheme.colorScheme.secondary.copy(alpha = 0.16f),
                      shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  modifier = Modifier.requiredSize(20.dp),
                  imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.onSurface,
                )
              }
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

                HorizontalPageIndicators(
                  pageIndicatorState = pageIndicatorState,
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
            ReaderActionsPanel(
              darkTheme = darkTheme,
              loadFullArticle = state.canLoadFullPost(readerPost.id),
              showReaderCustomisations = state.showReaderCustomisations,
              selectedFont = state.selectedReaderFont,
              fontScaleFactor = state.readerFontScaleFactor,
              fontLineHeightFactor = state.readerLineHeightScaleFactor,
              openInBrowserClick = {
                coroutineScope.launch { linkHandler.openLink(readerPost.link) }
              },
              loadFullArticleClick = {
                viewModel.dispatch(ReaderEvent.LoadFullArticleClicked(readerPost.id))
              },
              openReaderViewSettings = { viewModel.dispatch(ReaderEvent.ShowReaderCustomisations) },
              onFontChange = { font -> viewModel.dispatch(ReaderEvent.UpdateReaderFont(font)) },
              onFontScaleFactorChange = { fontScaleFactor ->
                viewModel.dispatch(ReaderEvent.UpdateFontScaleFactor(fontScaleFactor))
              },
              onFontLineHeightFactorChange = { fontLineHeightFactor ->
                viewModel.dispatch(ReaderEvent.UpdateFontLineHeightFactor(fontLineHeightFactor))
              }
            )
          }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppTheme.colorScheme.backdrop,
        contentColor = Color.Unspecified
      ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val highlightsBuilder =
          remember(darkTheme) {
            Highlights.Builder().theme(SyntaxThemes.atom(darkMode = darkTheme))
          }

        LaunchedEffect(pagerState, posts.loadState) {
          snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collectLatest { page ->
              val readerPost = runCatching { posts.peek(page) }.getOrNull()

              if (readerPost != null) {
                onPostChanged(page)
                viewModel.dispatch(ReaderEvent.PostPageChanged(page, readerPost))
              }
            }
        }

        HorizontalPager(
          modifier = modifier,
          state = pagerState,
          overscrollEffect = null,
          beyondViewportPageCount = 1,
          contentPadding =
            PaddingValues(
              start = paddingValues.calculateStartPadding(layoutDirection),
              end = paddingValues.calculateEndPadding(layoutDirection),
            )
        ) { page ->
          val readerPost = posts[page]

          if (readerPost != null) {
            LaunchedEffect(readerPost.id) { viewModel.dispatch(ReaderEvent.PostLoaded(readerPost)) }

            ReaderPage(
              readerPost = readerPost,
              page = page,
              pagerState = pagerState,
              highlightsBuilder = highlightsBuilder,
              loadFullArticle = state.canLoadFullPost(readerPost.id),
              onBookmarkClick = {
                viewModel.dispatch(
                  ReaderEvent.TogglePostBookmark(
                    postId = readerPost.id,
                    currentBookmarkStatus = readerPost.bookmarked
                  )
                )
              },
              modifier = Modifier.fillMaxSize(),
              contentPaddingValues = paddingValues
            )
          }
        }

        if (state.showReaderCustomisations) {
          Box(
            modifier =
              Modifier.fillMaxSize().pointerInput(Unit) {
                detectTapGestures { viewModel.dispatch(ReaderEvent.HideReaderCustomisations) }
              }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun ReaderActionsPanel(
  darkTheme: Boolean,
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
          .navigationBarsPadding()
          .then(modifier),
      contentAlignment = Alignment.Center
    ) {
      val (shadowColor1, shadowColor2) =
        if (darkTheme) {
          Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
        } else {
          Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
        }
      val backgroundShape = RoundedCornerShape(36.dp)

      Box(
        modifier =
          Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            .pointerInput(Unit) {}
            .dropShadow(
              shape = backgroundShape,
              offsetY = 16.dp,
              blur = 32.dp,
              color = shadowColor1
            )
            .dropShadow(shape = backgroundShape, offsetY = 4.dp, blur = 8.dp, color = shadowColor2)
            .clip(backgroundShape)
            .background(color = AppTheme.colorScheme.bottomSheet, shape = backgroundShape)
            .border(
              width = 1.dp,
              color = AppTheme.colorScheme.bottomSheetBorder,
              shape = backgroundShape
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
                openReaderViewSettings
              )
            }
          }
        }
      }
    }
  }
}
