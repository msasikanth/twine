@file:Suppress(
  "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
  "INVISIBLE_MEMBER",
  "INVISIBLE_REFERENCE",
)
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

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemKey
import com.adamglin.composeshadow.dropShadow
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownExtendedSpans
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.Input
import com.mikepenz.markdown.model.MarkdownState
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownExtendedSpans
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.home.ui.FeaturedImage
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.markdown.CoilMarkdownTransformer
import dev.sasikanth.rss.reader.markdown.handleElement
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalAppColorScheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.LocalSeedColorExtractor
import dev.sasikanth.rss.reader.ui.rememberDynamicColorState
import dev.sasikanth.rss.reader.util.readerDateTimestamp
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  explicitNulls = false
}

@Composable
internal fun ReaderScreen(
  darkTheme: Boolean,
  presenter: ReaderPresenter,
  modifier: Modifier = Modifier
) {
  val state by presenter.state.collectAsState()
  val posts = state.posts.collectAsLazyPagingItems()
  val pagerState = rememberPagerState { posts.itemCount }
  val coroutineScope = rememberCoroutineScope()
  val linkHandler = LocalLinkHandler.current
  val scrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
  val seedColorExtractor = LocalSeedColorExtractor.current
  // Using theme colors as default from the home screen
  // before we create dynamic content theme
  val dynamicColorState =
    rememberDynamicColorState(
      defaultLightAppColorScheme = LocalAppColorScheme.current,
      defaultDarkAppColorScheme = LocalAppColorScheme.current,
      useTonalSpotScheme = true,
    )
  val defaultSeedColor = AppTheme.colorScheme.tintedForeground
  val readerLinkHandler = remember {
    object : UriHandler {
      override fun openUri(uri: String) {
        coroutineScope.launch { linkHandler.openLink(uri) }
      }
    }
  }

  LaunchedEffect(pagerState, posts.loadState) {
    snapshotFlow { pagerState.settledPage }
      .distinctUntilChanged()
      .collectLatest { page ->
        val readerPost =
          try {
            posts[page]
          } catch (e: IndexOutOfBoundsException) {
            null
          }
        if (readerPost != null) {
          presenter.dispatch(ReaderEvent.PostPageChanged(readerPost))
        }
      }
  }

  LaunchedEffect(pagerState, posts.loadState) {
    snapshotFlow {
        val settledPage = pagerState.settledPage
        try {
          pagerState.getOffsetFractionForPage(settledPage)
        } catch (e: Throwable) {
          0f
        }
      }
      .collect { offset ->
        val readerPost =
          try {
            posts[pagerState.settledPage]
          } catch (e: IndexOutOfBoundsException) {
            null
          }

        if (readerPost != null) {
          // The default snap position of the pager is 0.5f, that means the targetPage
          // state only changes after reaching half way point. We instead want it to scale
          // as we start swiping.
          //
          // Instead of using EPSILON for snap threshold, we are doing that calculation
          // as the page offset changes
          //
          val currentItem = readerPost
          val fromItem =
            if (offset < -EPSILON) {
              posts[pagerState.settledPage - 1]
            } else {
              currentItem
            }

          val toItem =
            if (offset > EPSILON) {
              posts[pagerState.settledPage + 1]
            } else {
              currentItem
            }

          val fromSeedColor =
            seedColorExtractor.calculateSeedColor(fromItem?.imageUrl).run {
              if (this != null) Color(this) else defaultSeedColor
            }
          val toSeedColor =
            seedColorExtractor.calculateSeedColor(toItem?.imageUrl).run {
              if (this != null) Color(this) else defaultSeedColor
            }

          dynamicColorState.animate(
            fromSeedColor = fromSeedColor,
            toSeedColor = toSeedColor,
            progress = offset
          )
        }
      }
  }

  CompositionLocalProvider(
    LocalDynamicColorState provides dynamicColorState,
    LocalUriHandler provides readerLinkHandler
  ) {
    val snackbarHostState = remember { SnackbarHostState() }
    AppTheme(useDarkTheme = darkTheme) {
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
              FilledIconButton(
                modifier = Modifier.padding(start = 24.dp),
                colors =
                  IconButtonDefaults.filledIconButtonColors(
                    containerColor = AppTheme.colorScheme.primary.copy(alpha = 0.08f)
                  ),
                shape = RoundedCornerShape(50),
                onClick = { presenter.dispatch(ReaderEvent.BackClicked) },
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.onSurface,
                )
              }
            },
            title = {
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

              if (pagerState.pageCount > 1) {
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
              posts[pagerState.settledPage]
            } catch (e: IndexOutOfBoundsException) {
              null
            }
          val comingSoonString = LocalStrings.current.comingSoon

          if (readerPost != null) {
            BottomBar(
              darkTheme = darkTheme,
              loadFullArticle = state.canLoadFullPost(readerPost.id),
              openInBrowserClick = {
                coroutineScope.launch { linkHandler.openLink(readerPost.link) }
              },
              loadFullArticleClick = {
                presenter.dispatch(ReaderEvent.LoadFullArticleClicked(readerPost.id))
              },
              openReaderViewSettings = {
                // TODO: Open reader view settings
                coroutineScope.launch {
                  snackbarHostState.showSnackbar(
                    message = comingSoonString,
                    duration = SnackbarDuration.Short,
                  )
                }
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

        HorizontalPager(
          modifier = modifier,
          state = pagerState,
          key = posts.itemKey { it.id },
          beyondViewportPageCount = 1,
          overscrollEffect = null,
          contentPadding =
            PaddingValues(
              start = paddingValues.calculateStartPadding(layoutDirection),
              end = paddingValues.calculateEndPadding(layoutDirection),
            )
        ) { page ->
          val readerPost = posts[page]

          if (readerPost != null) {
            LaunchedEffect(readerPost.id) { presenter.dispatch(ReaderEvent.PostLoaded(readerPost)) }

            ReaderPage(
              readerPost = readerPost,
              page = page,
              pagerState = pagerState,
              highlightsBuilder = highlightsBuilder,
              loadFullArticle = state.canLoadFullPost(readerPost.id),
              onBookmarkClick = {
                presenter.dispatch(
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
      }
    }
  }
}

@Composable
private fun ReaderPage(
  readerPost: PostWithMetadata,
  page: Int,
  pagerState: PagerState,
  highlightsBuilder: Highlights.Builder,
  loadFullArticle: Boolean,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  var readerProcessingProgress by
    remember(readerPost.id) { mutableStateOf(ReaderProcessingProgress.Loading) }
  var parsedContent by remember(readerPost.id) { mutableStateOf(ReaderContent("", "")) }
  val parsedMarkdownState =
    remember(readerPost.id, parsedContent) {
      val flavour = GFMFlavourDescriptor()
      val input =
        Input(
          content = parsedContent.content.orEmpty(),
          lookupLinks = true,
          flavour = flavour,
          parser = MarkdownParser(flavour),
          referenceLinkHandler = ReferenceLinkHandlerImpl(),
        )

      MarkdownState(input)
    }

  LaunchedEffect(parsedMarkdownState) { parsedMarkdownState.parse() }

  val markdownState by parsedMarkdownState.state.collectAsState()
  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current
  val coroutineScope = rememberCoroutineScope()
  val markdownComponents = remember {
    markdownComponents(
      codeBlock = { cm ->
        MarkdownHighlightedCodeBlock(
          content = cm.content,
          node = cm.node,
          highlights = highlightsBuilder
        )
      },
      codeFence = { cm ->
        MarkdownHighlightedCodeFence(
          content = cm.content,
          node = cm.node,
          highlights = highlightsBuilder
        )
      },
    )
  }

  Box(modifier = modifier) {
    // Dummy view to parse the reader content using JS
    ReaderWebView(
      modifier = Modifier.requiredSize(0.dp),
      link = readerPost.link,
      content = readerPost.rawContent ?: readerPost.description,
      postImage = readerPost.imageUrl,
      fetchFullArticle = loadFullArticle,
      contentLoaded = {
        readerProcessingProgress = ReaderProcessingProgress.Idle
        parsedContent = json.decodeFromString(it)
      },
    )

    CompositionLocalProvider(
      LocalReferenceLinkHandler provides markdownState.referenceLinkHandler,
      LocalMarkdownPadding provides markdownPadding(),
      LocalMarkdownDimens provides markdownDimens(),
      LocalImageTransformer provides CoilMarkdownTransformer,
      LocalMarkdownAnnotator provides markdownAnnotator(),
      LocalMarkdownExtendedSpans provides markdownExtendedSpans(),
      LocalMarkdownAnimations provides markdownAnimations(),
      LocalMarkdownColors provides markdownColor(),
      LocalMarkdownTypography provides markdownTypography(),
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        overscrollEffect = null,
        contentPadding =
          PaddingValues(
            top = contentPaddingValues.calculateTopPadding(),
            bottom = contentPaddingValues.calculateBottomPadding()
          )
      ) {
        item(key = "reader-header") {
          PostInfo(
            readerPost = readerPost,
            page = page,
            pagerState = pagerState,
            parsedContent = parsedContent,
            onCommentsClick = {
              coroutineScope.launch { linkHandler.openLink(readerPost.commentsLink) }
            },
            onShareClick = { sharedHandler.share(readerPost.link) },
            onBookmarkClick = onBookmarkClick
          )
        }

        item(key = "divider") {
          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 20.dp, bottom = 24.dp),
            color = AppTheme.colorScheme.outlineVariant
          )
        }

        if (readerProcessingProgress == ReaderProcessingProgress.Loading) {
          item(key = "progress-indicator") { ProgressIndicator() }
        }

        if (readerProcessingProgress == ReaderProcessingProgress.Idle || parsedContent.hasContent) {
          if (parsedContent.hasContent) {
            when (val state = markdownState) {
              is State.Success -> {
                items(items = state.node.children) { node ->
                  Box(modifier = Modifier.padding(horizontal = 32.dp)) {
                    handleElement(
                      node = node,
                      components = markdownComponents,
                      content = state.content,
                      includeSpacer = true,
                      skipLinkDefinition = state.linksLookedUp,
                    )
                  }
                }
              }
              else -> {
                // no-op
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProgressIndicator() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    LinearProgressIndicator(
      trackColor = AppTheme.colorScheme.tintedSurface,
      color = AppTheme.colorScheme.tintedForeground,
    )
  }
}

@Composable
private fun BottomBar(
  darkTheme: Boolean,
  loadFullArticle: Boolean,
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  openReaderViewSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val navBarScrimColor = AppTheme.colorScheme.backdrop

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .wrapContentHeight()
        .drawBehind {
          drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
        }
        .padding(bottom = 16.dp)
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    val (shadowColor1, shadowColor2) =
      if (darkTheme) {
        Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
      } else {
        Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
      }

    Row(
      modifier =
        Modifier.padding(bottom = 16.dp, top = 16.dp)
          .height(IntrinsicSize.Min)
          .background(color = AppTheme.colorScheme.bottomSheet, shape = RoundedCornerShape(50))
          .border(
            width = 1.dp,
            color = AppTheme.colorScheme.bottomSheetBorder,
            shape = RoundedCornerShape(50)
          )
          .dropShadow(
            shape = RoundedCornerShape(50),
            offsetY = 16.dp,
            blur = 32.dp,
            color = shadowColor1
          )
          .dropShadow(
            shape = RoundedCornerShape(50),
            offsetY = 4.dp,
            blur = 8.dp,
            color = shadowColor2
          )
          .padding(horizontal = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AppTheme(useDarkTheme = true) {
        val transition = updateTransition(loadFullArticle)
        val buttonMinWidth by
          transition.animateDp {
            if (it) {
              52.dp
            } else {
              56.dp
            }
          }
        val readerViewToggleWidth by
          transition.animateDp(
            transitionSpec = {
              spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy
              )
            }
          ) {
            if (it) {
              88.dp
            } else {
              72.dp
            }
          }
        val readerViewToggleVerticalPadding by
          transition.animateDp(
            transitionSpec = {
              spring(
                stiffness = Spring.StiffnessMedium,
                dampingRatio = Spring.DampingRatioMediumBouncy
              )
            }
          ) {
            if (it) {
              8.dp
            } else {
              12.dp
            }
          }
        val readerViewToggleBackgroundColor by
          transition.animateColor {
            if (it) {
              AppTheme.colorScheme.primaryContainer
            } else {
              AppTheme.colorScheme.surfaceContainerHighest
            }
          }
        val readerViewToggleContentColor by
          transition.animateColor {
            if (it) {
              AppTheme.colorScheme.onPrimaryContainer
            } else {
              AppTheme.colorScheme.onSurface
            }
          }

        BottomBarIconButton(
          modifier = Modifier.padding(vertical = 12.dp),
          label = LocalStrings.current.openWebsite,
          icon = TwineIcons.OpenBrowser,
          onClick = openInBrowserClick,
          minWidth = buttonMinWidth
        )

        BottomBarToggleIconButton(
          modifier = Modifier.fillMaxHeight().padding(vertical = readerViewToggleVerticalPadding),
          label = LocalStrings.current.cdLoadFullArticle,
          icon = TwineIcons.ArticleShortcut,
          onClick = loadFullArticleClick,
          backgroundColor = readerViewToggleBackgroundColor,
          contentColor = readerViewToggleContentColor,
          minWidth = readerViewToggleWidth
        )

        BottomBarIconButton(
          modifier = Modifier.padding(vertical = 12.dp),
          label = LocalStrings.current.readerSettings,
          icon = TwineIcons.Settings,
          onClick = openReaderViewSettings,
          minWidth = buttonMinWidth
        )
      }
    }
  }
}

@Composable
private fun BottomBarIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentColor: Color = AppTheme.colorScheme.onSurfaceVariant,
  minWidth: Dp = 56.dp,
) {
  Box(
    modifier =
      Modifier.then(modifier)
        .requiredSizeIn(minWidth = minWidth)
        .clip(RoundedCornerShape(50))
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.padding(vertical = 10.dp).requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
    )
  }
}

@Composable
private fun BottomBarToggleIconButton(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  backgroundColor: Color = Color.Transparent,
  contentColor: Color = AppTheme.colorScheme.onSurfaceVariant,
  minWidth: Dp = 72.dp,
) {
  Box(
    modifier =
      Modifier.requiredSizeIn(minHeight = 40.dp, minWidth = minWidth)
        .then(modifier)
        .clip(RoundedCornerShape(50))
        .background(backgroundColor, RoundedCornerShape(50))
        .semantics {
          role = Role.Button
          contentDescription = label
        }
        .clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.requiredSize(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = contentColor,
    )
  }
}

@Composable
private fun PostInfo(
  readerPost: PostWithMetadata,
  page: Int,
  pagerState: PagerState,
  parsedContent: ReaderContent,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp).then(modifier),
  ) {
    val title = readerPost.title
    val description = readerPost.description
    val postImage = readerPost.imageUrl

    if (!postImage.isNullOrBlank()) {
      FeaturedImage(
        modifier =
          Modifier.graphicsLayer {
              translationX =
                if (page in 0..pagerState.pageCount) {
                  pagerState.getOffsetFractionForPage(page) * 250f
                } else {
                  0f
                }
              scaleX = 1.08f
              scaleY = 1.08f
            }
            .align(Alignment.CenterHorizontally),
        image = postImage
      )

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Text(
      modifier = Modifier.padding(top = 20.dp),
      text = readerPost.date.readerDateTimestamp(),
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.outline,
      maxLines = 1,
    )

    Text(
      modifier = Modifier.padding(top = 12.dp),
      text = title.ifBlank { description },
      style = MaterialTheme.typography.headlineSmall,
      color = AppTheme.colorScheme.onSurface,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )

    if (!parsedContent.excerpt.isNullOrBlank()) {
      Spacer(Modifier.requiredHeight(8.dp))

      Text(
        text = parsedContent.excerpt,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.secondary,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )
    }

    Spacer(Modifier.requiredHeight(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      val showFeedFavIcon = LocalShowFeedFavIconSetting.current
      val feedIconUrl = if (showFeedFavIcon) readerPost.feedHomepageLink else readerPost.feedIcon

      PostSourcePill(
        modifier = Modifier.weight(1f).clearAndSetSemantics {},
        feedName = readerPost.feedName,
        feedIcon = feedIconUrl,
        config =
          PostMetadataConfig(
            showUnreadIndicator = false,
            showToggleReadUnreadOption = true,
            enablePostSource = false
          ),
        onSourceClick = {
          // no-op
        },
      )

      PostOptionsButtonRow(
        postBookmarked = readerPost.bookmarked,
        commentsLink = readerPost.commentsLink,
        onCommentsClick = onCommentsClick,
        onShareClick = onShareClick,
        onBookmarkClick = onBookmarkClick,
      )
    }
  }
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
  feedName: String,
  config: PostMetadataConfig,
  onSourceClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    val postSourceTextColor =
      if (config.enablePostSource) {
        AppTheme.colorScheme.onSurface
      } else {
        AppTheme.colorScheme.onSurfaceVariant
      }

    Row(
      modifier =
        Modifier.background(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            RoundedCornerShape(50)
          )
          .border(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            RoundedCornerShape(50)
          )
          .clip(RoundedCornerShape(50))
          .clickable(onClick = onSourceClick, enabled = config.enablePostSource)
          .padding(vertical = 6.dp)
          .padding(start = 8.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FeedIcon(
        modifier = Modifier.requiredSize(16.dp).clip(RoundedCornerShape(4.dp)),
        url = feedIcon,
        contentDescription = null,
      )

      Spacer(Modifier.requiredWidth(6.dp))

      Text(
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        text = feedName,
        color = postSourceTextColor,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
private fun PostOptionsButtonRow(
  postBookmarked: Boolean,
  commentsLink: String?,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
) {
  Row(modifier = Modifier.semantics { isTraversalGroup = true }) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = LocalStrings.current.comments
      PostOptionIconButton(
        modifier =
          Modifier.semantics {
            role = Role.Button
            contentDescription = commentsLabel
          },
        icon = TwineIcons.Comments,
        iconTint = AppTheme.colorScheme.onSurfaceVariant,
        onClick = onCommentsClick
      )
    }

    val sharedLabel = LocalStrings.current.share
    PostOptionIconButton(
      modifier =
        Modifier.semantics {
          role = Role.Button
          contentDescription = sharedLabel
        },
      icon = TwineIcons.Share,
      iconTint = AppTheme.colorScheme.onSurfaceVariant,
      onClick = onShareClick
    )

    val bookmarkLabel =
      if (postBookmarked) {
        LocalStrings.current.unBookmark
      } else {
        LocalStrings.current.bookmark
      }
    PostOptionIconButton(
      modifier =
        Modifier.semantics {
          role = Role.Button
          contentDescription = bookmarkLabel
        },
      icon =
        if (postBookmarked) {
          TwineIcons.Bookmarked
        } else {
          TwineIcons.Bookmark
        },
      iconTint =
        if (postBookmarked) {
          AppTheme.colorScheme.tintedForeground
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        },
      onClick = onBookmarkClick
    )
  }
}

@Composable
private fun PostOptionIconButton(
  icon: ImageVector,
  modifier: Modifier = Modifier,
  iconTint: Color = AppTheme.colorScheme.textEmphasisHigh,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(40.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onClick)
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Serializable
enum class ReaderProcessingProgress {
  Loading,
  Idle
}

@Serializable
data class ReaderContent(
  val excerpt: String?,
  val content: String?,
) {

  val hasContent: Boolean
    get() = !content.isNullOrBlank()
}
