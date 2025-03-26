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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.size.Size
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.processParams
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderEvent
import dev.sasikanth.rss.reader.reader.ReaderPresenter
import dev.sasikanth.rss.reader.reader.ReaderState
import dev.sasikanth.rss.reader.resources.icons.ArticleShortcut
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.OpenBrowser
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.asJSString
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxThemes
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Composable
internal fun ReaderScreen(
  darkTheme: Boolean,
  presenter: ReaderPresenter,
  dispatchersProvider: DispatchersProvider,
  modifier: Modifier = Modifier
) {
  val state by presenter.state.collectAsState()
  val coroutineScope = rememberCoroutineScope()
  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current

  ModalBottomSheet(
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = AppTheme.colorScheme.backdrop,
    dragHandle = null,
    shape = RectangleShape,
    onDismissRequest = { presenter.dispatch(ReaderEvent.BackClicked) },
  ) {
    val topAppBarScrollBehaviour = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
      modifier = modifier.nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
      topBar = {
        CenterAlignedTopAppBar(
          modifier = Modifier.statusBarsPadding(),
          scrollBehavior = topAppBarScrollBehaviour,
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = Color.Transparent,
              scrolledContainerColor = Color.Transparent,
            ),
          navigationIcon = {
            IconButton(
              onClick = {
                // TODO: Go to previous article
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = null,
                tint = AppTheme.colorScheme.onSurface,
              )
            }
          },
          title = {
            Box(
              modifier =
                Modifier.background(
                    color = AppTheme.colorScheme.primary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(50)
                  )
                  .padding(horizontal = 16.dp, vertical = 8.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = LocalStrings.current.pullToClose,
                style = MaterialTheme.typography.labelSmall,
                color = AppTheme.colorScheme.onSurfaceVariant
              )
            }
          },
          actions = {
            IconButton(
              onClick = {
                // TODO: Go to next article
              }
            ) {
              Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colorScheme.onSurface,
              )
            }
          }
        )
      },
      bottomBar = {
        BottomBar(
          openInBrowserClick = { coroutineScope.launch { linkHandler.openLink(state.link) } },
          loadFullArticleClick = { presenter.dispatch(ReaderEvent.ArticleShortcutClicked) }
        )
      },
      containerColor = Color.Unspecified,
      contentColor = Color.Unspecified
    ) { paddingValues ->
      var readerProcessingProgress by remember { mutableStateOf(ReaderProcessingProgress.Loading) }
      var parsedContent by remember { mutableStateOf(ReaderContent("", "")) }

      val layoutDirection = LocalLayoutDirection.current

      val webViewState = rememberWebViewStateWithHTMLData("")
      val navigator = rememberWebViewNavigator()
      val jsBridge = rememberWebViewJsBridge()

      LaunchedEffect(state.content) {
        if (!state.content.isNullOrBlank()) {
          launch(dispatchersProvider.io) {
            navigator.loadHtml(
              html = ReaderHTML.create(),
              baseUrl = state.link,
            )
          }
        }
      }

      LaunchedEffect(webViewState.loadingState, state.fetchFullArticle) {
        if (webViewState.loadingState == LoadingState.Finished) {
          navigator.evaluateJavaScript(
            script =
              """
                parseReaderContent(
                  ${state.link.asJSString}, 
                  ${state.content.asJSString}, 
                  ${state.postImage.orEmpty().asJSString},
                  ${state.fetchFullArticle}
                )
              """
                .trimIndent()
          )
        }
      }

      DisposableEffect(jsBridge) {
        jsBridge.register(
          object : IJsMessageHandler {
            override fun handle(
              message: JsMessage,
              navigator: WebViewNavigator?,
              callback: (String) -> Unit
            ) {
              if (message.params.isNotBlank()) {
                readerProcessingProgress = processParams<ReaderProcessingProgress>(message)
              }
            }

            override fun methodName(): String {
              return "renderProgress"
            }
          }
        )

        jsBridge.register(
          object : IJsMessageHandler {
            override fun handle(
              message: JsMessage,
              navigator: WebViewNavigator?,
              callback: (String) -> Unit
            ) {
              if (message.params.isNotBlank()) {
                parsedContent = processParams(message)
              }
            }

            override fun methodName(): String {
              return "parsedContentCallback"
            }
          }
        )

        onDispose { jsBridge.clear() }
      }

      // Dummy view to parse the reader content using JS
      WebView(
        modifier = Modifier.requiredSize(0.dp),
        state = webViewState,
        navigator = navigator,
        webViewJsBridge = jsBridge,
        captureBackPresses = false,
      )

      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding =
          PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            top = 0.dp,
            end = paddingValues.calculateEndPadding(layoutDirection),
            bottom = paddingValues.calculateBottomPadding()
          )
      ) {
        item(key = "reader-header") {
          val postImage = state.postImage

          Box {
            BannerImageBlurred(postImage = postImage, darkTheme = darkTheme)

            PostInfo(
              paddingValues = paddingValues,
              state = state,
              postImage = postImage,
              parsedContent = parsedContent,
              onCommentsClick = {
                coroutineScope.launch { linkHandler.openLink(state.commentsLink) }
              },
              onShareClick = { sharedHandler.share(state.link) },
              onBookmarkClick = { presenter.dispatch(ReaderEvent.TogglePostBookmark) }
            )
          }
        }

        item(key = "divider") {
          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 32.dp).padding(top = 20.dp, bottom = 24.dp),
            color = AppTheme.colorScheme.outlineVariant
          )
        }

        item(
          key = "reader-content",
        ) {
          val readerLinkHandler = remember {
            object : UriHandler {
              override fun openUri(uri: String) {
                coroutineScope.launch { linkHandler.openLink(uri) }
              }
            }
          }
          CompositionLocalProvider(LocalUriHandler provides readerLinkHandler) {
            val highlightsBuilder =
              remember(darkTheme) {
                Highlights.Builder().theme(SyntaxThemes.atom(darkMode = darkTheme))
              }

            Markdown(
              modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
              content = parsedContent.content,
              typography =
                markdownTypography(
                  link =
                    MaterialTheme.typography.bodyLarge.copy(
                      color = AppTheme.colorScheme.tintedForeground,
                      fontWeight = FontWeight.Bold,
                      textDecoration = TextDecoration.Underline
                    )
                ),
              colors =
                markdownColor(
                  text = AppTheme.colorScheme.onSurface,
                ),
              imageTransformer = Coil3ImageTransformerImpl,
              components =
                markdownComponents(
                  codeBlock = {
                    MarkdownHighlightedCodeBlock(
                      content = it.content,
                      node = it.node,
                      highlights = highlightsBuilder
                    )
                  },
                  codeFence = {
                    MarkdownHighlightedCodeFence(
                      content = it.content,
                      node = it.node,
                      highlights = highlightsBuilder
                    )
                  },
                )
            )
          }

          when {
            readerProcessingProgress == ReaderProcessingProgress.Loading -> {
              Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LinearProgressIndicator(
                  trackColor = AppTheme.colorScheme.tintedSurface,
                  color = AppTheme.colorScheme.tintedForeground,
                )
              }
            }
            state.content.isNullOrBlank() -> {
              Text(LocalStrings.current.noReaderContent)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BottomBar(
  openInBrowserClick: () -> Unit,
  loadFullArticleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val navBarScrimColor = AppTheme.colorScheme.backdrop

  Box(
    modifier =
      Modifier.fillMaxWidth().wrapContentHeight().navigationBarsPadding().drawBehind {
        drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, navBarScrimColor)))
      },
    contentAlignment = Alignment.Center
  ) {
    Row(
      modifier =
        Modifier.padding(bottom = 16.dp, top = 16.dp)
          .clipToBounds()
          .background(color = AppTheme.colorScheme.bottomSheet, shape = RoundedCornerShape(50))
          .border(
            width = 1.dp,
            color = AppTheme.colorScheme.bottomSheetBorder,
            shape = RoundedCornerShape(50)
          )
          .padding(all = 12.dp)
          .then(modifier),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BottomBarIconButton(
        label = LocalStrings.current.openWebsite,
        icon = TwineIcons.OpenBrowser,
        onClick = openInBrowserClick
      )

      BottomBarIconButton(
        label = LocalStrings.current.cdLoadFullArticle,
        icon = TwineIcons.ArticleShortcut,
        onClick = loadFullArticleClick
      )
    }
  }
}

@Composable
private fun BottomBarIconButton(label: String, icon: ImageVector, onClick: () -> Unit) {
  AppTheme(useDarkTheme = true) {
    Box(
      modifier =
        Modifier.requiredSizeIn(minWidth = 64.dp, minHeight = 40.dp)
          .semantics {
            role = Role.Button
            contentDescription = label
          }
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
          ) {
            onClick()
          },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.requiredSize(20.dp),
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun PostInfo(
  paddingValues: PaddingValues,
  state: ReaderState,
  postImage: String?,
  parsedContent: ReaderContent,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth()
        .padding(top = paddingValues.calculateTopPadding())
        .padding(horizontal = 32.dp)
        .then(modifier),
  ) {
    val title = state.title.orEmpty()
    val description = state.description.orEmpty()

    if (!postImage.isNullOrBlank()) {
      AsyncImage(
        url = postImage,
        modifier =
          Modifier.clip(MaterialTheme.shapes.extraLarge)
            .requiredHeightIn(max = 198.dp)
            .aspectRatio(ratio = 16f / 9f)
            .background(AppTheme.colorScheme.surfaceContainerLowest)
            .align(Alignment.CenterHorizontally),
        contentDescription = null,
        contentScale = ContentScale.Crop,
      )

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Text(
      modifier = Modifier.padding(top = 20.dp),
      text = state.publishedAt.orEmpty(),
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

    if (parsedContent.excerpt.isNotBlank()) {
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
      val feedIconUrl = if (showFeedFavIcon) state.feedHomePageLink else state.feedIcon

      PostSourcePill(
        modifier = Modifier.weight(1f).clearAndSetSemantics {},
        feedName = state.feedName,
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
        postBookmarked = state.isBookmarked ?: false,
        commentsLink = state.commentsLink,
        onCommentsClick = onCommentsClick,
        onShareClick = onShareClick,
        onBookmarkClick = onBookmarkClick,
      )
    }
  }
}

@Composable
private fun BannerImageBlurred(postImage: String?, darkTheme: Boolean) {
  if (!postImage.isNullOrBlank()) {
    val gradientOverlayModifier =
      if (darkTheme) {
        Modifier.drawWithCache {
          val gradientColor = Color.Black
          val radialGradient =
            Brush.radialGradient(
              colors =
                listOf(
                  gradientColor,
                  gradientColor.copy(alpha = 0.0f),
                  gradientColor.copy(alpha = 0.0f)
                ),
              center = Offset(x = this.size.width, y = 40f)
            )

          val linearGradient =
            Brush.verticalGradient(
              colors = listOf(gradientColor, gradientColor.copy(alpha = 0.0f)),
            )

          onDrawWithContent {
            drawContent()
            drawRect(radialGradient)
            drawRect(linearGradient)
          }
        }
      } else {
        Modifier
      }

    val overlayColor = AppTheme.colorScheme.inversePrimary
    val colorMatrix = remember {
      ColorMatrix().apply {
        val sat = if (darkTheme) 1f else 5f
        setToSaturation(sat)
      }
    }

    AsyncImage(
      url = postImage,
      modifier =
        Modifier.requiredHeightIn(max = 800.dp)
          .aspectRatio(1f)
          .graphicsLayer {
            val blurRadiusInPx = 100.dp.toPx()
            renderEffect = BlurEffect(blurRadiusInPx, blurRadiusInPx, TileMode.Decal)
            shape = RectangleShape
            clip = false
          }
          .drawWithContent {
            drawContent()

            drawRect(
              color = overlayColor,
              blendMode = BlendMode.Luminosity,
            )
          }
          .then(gradientOverlayModifier),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      size = Size(128, 128),
      backgroundColor = AppTheme.colorScheme.surface,
      colorFilter = ColorFilter.colorMatrix(colorMatrix)
    )
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
  val excerpt: String,
  val content: String,
)
