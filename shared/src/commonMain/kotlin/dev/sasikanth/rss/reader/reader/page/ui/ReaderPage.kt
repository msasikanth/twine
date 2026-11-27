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

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorProducer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.DefaultMarkdownAnimation
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.markdown.CoilMarkdownTransformer
import dev.sasikanth.rss.reader.markdown.LocalMarkdownAnnotatorSettings
import dev.sasikanth.rss.reader.markdown.LocalMarkdownTextColorProducer
import dev.sasikanth.rss.reader.markdown.LocalReaderContentWidth
import dev.sasikanth.rss.reader.markdown.rememberAnnotatorSettings
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderContentType
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.reader.ui.ImageClickEvent
import dev.sasikanth.rss.reader.reader.ui.LocalOnImageClick
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalImageSharedElementScopeKey
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.smoothVerticalFade
import kotlin.time.Instant
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode

private const val TOP_SCRIM_OPAQUE_THRESHOLD = 200f
internal val ReaderHorizontalPadding = 48.dp
internal val ReaderPostMetadataConfig =
  PostMetadataConfig(
    showUnreadIndicator = false,
    showToggleReadUnreadOption = true,
    enablePostSource = false,
  )
private val NoOpAnimateTextSize: Modifier.() -> Modifier = { this }
private val ReaderMarkdownAnimations = DefaultMarkdownAnimation(NoOpAnimateTextSize)

internal class ReaderFadeScrimCache {
  private var backdrop: Color = Color.Unspecified
  private var size: Size = Size.Unspecified
  private var topScrimSolidPx: Float = Float.NaN
  private var topScrimFadePx: Float = Float.NaN
  private var bottomScrimHeightPx: Float = Float.NaN

  var topBrush: Brush? = null
    private set

  var topScrimSize: Size = Size.Zero
    private set

  var bottomBrush: Brush? = null
    private set

  var bottomScrimOffset: Offset = Offset.Zero
    private set

  var bottomScrimSize: Size = Size.Zero
    private set

  fun updateIfNeeded(
    backdrop: Color,
    size: Size,
    topScrimSolidPx: Float,
    topScrimFadePx: Float,
    bottomScrimHeightPx: Float,
  ) {
    if (
      this.backdrop == backdrop &&
        this.size == size &&
        this.topScrimSolidPx == topScrimSolidPx &&
        this.topScrimFadePx == topScrimFadePx &&
        this.bottomScrimHeightPx == bottomScrimHeightPx
    ) {
      return
    }

    this.backdrop = backdrop
    this.size = size
    this.topScrimSolidPx = topScrimSolidPx
    this.topScrimFadePx = topScrimFadePx
    this.bottomScrimHeightPx = bottomScrimHeightPx

    val topScrimHeight = topScrimSolidPx + topScrimFadePx
    topBrush =
      smoothVerticalFade(
        color = backdrop,
        endY = topScrimHeight,
        solidFraction = if (topScrimHeight > 0f) topScrimSolidPx / topScrimHeight else 0f,
      )
    topScrimSize = size.copy(height = topScrimHeight)

    val bottomScrimTop = size.height - bottomScrimHeightPx
    bottomBrush =
      smoothVerticalFade(
        color = backdrop,
        startY = bottomScrimTop,
        endY = size.height,
        reversed = true,
      )
    bottomScrimOffset = Offset(0f, bottomScrimTop)
    bottomScrimSize = size.copy(height = bottomScrimHeightPx)
  }
}

@Composable
internal fun ReaderPage(
  pageViewModel: ReaderPageViewModel,
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  markdownComponents: MarkdownComponents,
  playbackStateFlow: StateFlow<PlaybackState>,
  isDarkTheme: Boolean,
  themeVariant: ThemeVariant,
  fromScreen: ReaderScreenArgs.FromScreen,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (ImageClickEvent) -> Unit,
  onWatchVideo: (String) -> Unit,
  onOpenWebsite: () -> Unit,
  onPauseAudio: () -> Unit,
  onResumeAudio: () -> Unit,
  onSeekAudio: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
  onHeroVisibilityChanged: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  val markdownContentState by pageViewModel.contentState.collectAsStateWithLifecycle()
  val excerptState by pageViewModel.excerptState.collectAsStateWithLifecycle()
  val contentParsingProgress by pageViewModel.parsingProgress.collectAsStateWithLifecycle()
  val resolvedCoverArtUrl by pageViewModel.resolvedCoverArtUrl.collectAsStateWithLifecycle()
  val chapters by pageViewModel.chapters.collectAsStateWithLifecycle()
  val skipSponsorChapters by pageViewModel.skipSponsorChapters.collectAsStateWithLifecycle()

  val linkHandler = LocalLinkHandler.current
  val coroutineScope = rememberCoroutineScope()
  val contentType = pageViewModel.contentType
  val onCommentsClick =
    remember(readerPost.commentsLink, linkHandler) {
      {
        val commentsLink = readerPost.commentsLink
        if (!commentsLink.isNullOrBlank()) {
          coroutineScope.launch { linkHandler.openLink(commentsLink) }
        }
        Unit
      }
    }

  LaunchedEffect(contentType, readerPost.id) {
    if (contentType !is ReaderContentType.Podcast) {
      onHeroVisibilityChanged(true)
    }
  }

  ProvideReaderMarkdownLocals(
    markdownComponents = markdownComponents,
    onImageClick = onImageClick,
  ) { updateContentWidth ->
    Box(modifier = modifier.clipToBounds()) {
      when (contentType) {
        ReaderContentType.Article -> {
          ArticleReaderPage(
            modifier = Modifier.fillMaxSize(),
            readerPost = readerPost,
            showFullArticle = showFullArticle,
            page = page,
            pagerState = pagerState,
            excerpt = excerptState,
            markdownContentState = markdownContentState,
            contentParsingProgress = contentParsingProgress,
            markdownComponents = markdownComponents,
            isDarkTheme = isDarkTheme,
            themeVariant = themeVariant,
            fromScreen = fromScreen,
            contentPaddingValues = contentPaddingValues,
            onBookmarkClick = onBookmarkClick,
            onCommentsClick = onCommentsClick,
            onMarkAsUnread = onMarkAsUnread,
            onOpenWebsite = onOpenWebsite,
            onImageClick = onImageClick,
            onContentWidthChanged = updateContentWidth,
          )
        }
        is ReaderContentType.Podcast -> {
          PodcastReaderPage(
            modifier = Modifier.fillMaxSize(),
            readerPost = readerPost,
            contentType = contentType,
            resolvedCoverArtUrl = resolvedCoverArtUrl,
            chapters = chapters,
            skipSponsorChapters = skipSponsorChapters,
            markdownContentState = markdownContentState,
            contentParsingProgress = contentParsingProgress,
            markdownComponents = markdownComponents,
            playbackStateFlow = playbackStateFlow,
            audioPlayerInstallationHint = pageViewModel.audioPlayer.installationHint,
            fromScreen = fromScreen,
            contentPaddingValues = contentPaddingValues,
            onBookmarkClick = onBookmarkClick,
            onCommentsClick = onCommentsClick,
            onMarkAsUnread = onMarkAsUnread,
            onImageClick = onImageClick,
            onPlayClick = pageViewModel::playAudio,
            onPlayAtClick = pageViewModel::playAudioAt,
            onChapterClick = pageViewModel::playChapter,
            onToggleSkipSponsorChapters = pageViewModel::toggleSkipSponsorChapters,
            onPauseClick = onPauseAudio,
            onResumeClick = onResumeAudio,
            onSeek = onSeekAudio,
            onSeekForward = onSeekForward,
            onSeekBackward = onSeekBackward,
            onPlaybackSpeedChange = onPlaybackSpeedChange,
            onSleepTimerOptionSelected = onSleepTimerOptionSelected,
            onOpenWebsite = onOpenWebsite,
            onHeroVisibilityChanged = onHeroVisibilityChanged,
            onContentWidthChanged = updateContentWidth,
          )
        }
        is ReaderContentType.Video -> {
          VideoReaderPage(
            modifier = Modifier.fillMaxSize(),
            readerPost = readerPost,
            contentType = contentType,
            markdownContentState = markdownContentState,
            contentParsingProgress = contentParsingProgress,
            markdownComponents = markdownComponents,
            isDarkTheme = isDarkTheme,
            fromScreen = fromScreen,
            contentPaddingValues = contentPaddingValues,
            onWatchVideo = onWatchVideo,
            onBookmarkClick = onBookmarkClick,
            onCommentsClick = onCommentsClick,
            onMarkAsUnread = onMarkAsUnread,
            onContentWidthChanged = updateContentWidth,
          )
        }
      }
    }
  }
}

@Composable
internal fun ProvideReaderMarkdownLocals(
  markdownComponents: MarkdownComponents,
  onImageClick: (ImageClickEvent) -> Unit,
  content: @Composable (updateContentWidth: (Dp) -> Unit) -> Unit,
) {
  val shouldBlockImage = LocalBlockImage.current
  val colorScheme = AppTheme.colorScheme
  val settledPrimary = colorScheme.settledPrimary
  val settledOnSurface = colorScheme.settledOnSurface
  val settledOutlineVariant = colorScheme.settledOutlineVariant

  val textSelectionColors =
    remember(settledPrimary) {
      TextSelectionColors(
        handleColor = settledPrimary,
        backgroundColor = settledPrimary.copy(alpha = 0.4f),
      )
    }
  val referenceLinkHandler = remember { ReferenceLinkHandlerImpl() }
  val padding = markdownPadding(block = 12.dp)
  val dimens = markdownDimens(tableCellWidth = 140.dp, tableCellPadding = 12.dp)
  val annotator =
    remember(shouldBlockImage) {
      markdownAnnotator(
        annotate = { _, node -> node.type == MarkdownElementTypes.IMAGE && shouldBlockImage }
      )
    }
  val colors =
    markdownColor(
      text = settledOnSurface,
      codeBackground = settledOnSurface.copy(alpha = 0.1f),
      dividerColor = settledOutlineVariant,
      tableBackground = settledOnSurface.copy(alpha = 0.02f),
    )
  val materialTypography = MaterialTheme.typography
  val textLinkStyles =
    remember(materialTypography.bodyLarge, settledPrimary) {
      TextLinkStyles(
        materialTypography.bodyLarge
          .copy(
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.Underline,
            color = settledPrimary,
          )
          .toSpanStyle()
      )
    }
  val typography =
    markdownTypography(
      h1 = materialTypography.displaySmall,
      h2 = materialTypography.headlineLarge,
      h3 = materialTypography.headlineMedium,
      h4 = materialTypography.headlineSmall,
      h5 = materialTypography.titleLarge,
      h6 = materialTypography.titleMedium,
      textLink = textLinkStyles,
    )
  val codeSpanStyle =
    remember(typography.inlineCode, colors.inlineCodeBackground) {
      typography.inlineCode.copy(background = colors.inlineCodeBackground).toSpanStyle()
    }
  val highlightSpanStyle =
    remember(colors.highlightText, colors.highlightBackground) {
      SpanStyle(color = colors.highlightText, background = colors.highlightBackground)
    }
  val annotatorSettings =
    rememberAnnotatorSettings(
      linkTextSpanStyle = typography.textLink,
      codeSpanStyle = codeSpanStyle,
      annotator = annotator,
      referenceLinkHandler = referenceLinkHandler,
      highlightSpanStyle = highlightSpanStyle,
    )
  val markdownTextColorProducer = remember(colorScheme) { ColorProducer { colorScheme.onSurface } }
  val readerContentWidth = remember { mutableStateOf(Dp.Unspecified) }
  val readerContentWidthProvider = remember(readerContentWidth) { { readerContentWidth.value } }
  val updateContentWidth =
    remember(readerContentWidth) {
      { widthDp: Dp ->
        if (readerContentWidth.value != widthDp) {
          readerContentWidth.value = widthDp
        }
      }
    }

  CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
    SelectionContainer {
      CompositionLocalProvider(
        LocalOnImageClick provides onImageClick,
        LocalMarkdownComponents provides markdownComponents,
        LocalReferenceLinkHandler provides referenceLinkHandler,
        LocalMarkdownPadding provides padding,
        LocalMarkdownDimens provides dimens,
        LocalImageTransformer provides CoilMarkdownTransformer,
        LocalMarkdownAnnotator provides annotator,
        LocalMarkdownAnimations provides ReaderMarkdownAnimations,
        LocalMarkdownColors provides colors,
        LocalMarkdownTypography provides typography,
        LocalMarkdownAnnotatorSettings provides annotatorSettings,
        LocalMarkdownTextColorProducer provides markdownTextColorProducer,
        LocalReaderContentWidth provides readerContentWidthProvider,
      ) {
        content(updateContentWidth)
      }
    }
  }
}

@Composable
internal fun Modifier.readerContentWidthAndScrims(
  listState: LazyListState,
  topPadding: Dp,
  onContentWidthChanged: (Dp) -> Unit,
  horizontalPadding: Dp = ReaderHorizontalPadding,
): Modifier {
  val colorScheme = AppTheme.colorScheme
  val density = LocalDensity.current
  val topScrimSolidPx = with(density) { topPadding.toPx() }
  val topScrimFadePx = with(density) { 72.dp.toPx() }
  val bottomScrimHeightPx = with(density) { 96.dp.toPx() }
  val scrimCache = remember { ReaderFadeScrimCache() }
  val topScrimAlphaProvider =
    remember(listState) {
      {
        if (listState.firstVisibleItemIndex == 0) {
          (listState.firstVisibleItemScrollOffset / TOP_SCRIM_OPAQUE_THRESHOLD).coerceIn(0f, 1f)
        } else {
          1f
        }
      }
    }

  return this.onSizeChanged { intSize ->
      val widthDp = with(density) { (intSize.width.toDp() - horizontalPadding).coerceAtLeast(0.dp) }
      onContentWidthChanged(widthDp)
    }
    .drawWithContent {
      drawContent()
      scrimCache.updateIfNeeded(
        backdrop = colorScheme.backdrop,
        size = size,
        topScrimSolidPx = topScrimSolidPx,
        topScrimFadePx = topScrimFadePx,
        bottomScrimHeightPx = bottomScrimHeightPx,
      )
      scrimCache.topBrush?.let { brush ->
        val alpha = topScrimAlphaProvider()
        if (alpha > 0f) {
          drawRect(brush = brush, size = scrimCache.topScrimSize, alpha = alpha)
        }
      }
      scrimCache.bottomBrush?.let { brush ->
        drawRect(
          brush = brush,
          topLeft = scrimCache.bottomScrimOffset,
          size = scrimCache.bottomScrimSize,
        )
      }
    }
}

internal fun LazyListScope.readerMarkdownItems(
  postId: String,
  markdownContentState: State.Success,
  markdownComponents: MarkdownComponents,
  horizontalPadding: Dp = 24.dp,
) {
  items(
    items = markdownContentState.node.children,
    key = { it.markdownNodeKey() },
    contentType = { it.type.name },
  ) { node ->
    CompositionLocalProvider(
      LocalImageSharedElementScopeKey provides "$postId:${node.markdownNodeKey()}"
    ) {
      Box(modifier = Modifier.padding(horizontal = horizontalPadding)) {
        MarkdownElement(
          node = node,
          components = markdownComponents,
          content = markdownContentState.content,
          includeSpacer = true,
        )
      }
    }
  }
}

@Composable
internal fun ReaderProgressIndicator(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxWidth().padding(vertical = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    LinearProgressIndicator(
      trackColor = AppTheme.colorScheme.surfaceContainerLow,
      color = AppTheme.colorScheme.primary,
    )
  }
}

internal fun ASTNode.markdownNodeKey(): String {
  return "${type.name}:$startOffset:$endOffset"
}

internal fun nextPlaybackSpeed(currentSpeed: Float): Float =
  when (currentSpeed) {
    0.5f -> 1.0f
    1.0f -> 1.5f
    1.5f -> 2.0f
    2.0f -> 0.5f
    else -> 1.0f
  }

@Preview(locale = "en")
@Composable
private fun ReaderPagePreview() {
  val samplePost =
    ResolvedPost(
      id = "",
      sourceId = "",
      title = "Post Title",
      description = "Post Description",
      link = "",
      imageUrl = null,
      audioUrl = null,
      date = Instant.fromEpochMilliseconds(0),
      createdAt = Instant.fromEpochMilliseconds(0),
      commentsLink = null,
      flags = emptySet(),
      feedName = "Feed Name",
      feedIcon = "",
      feedHomepageLink = "",
      alwaysFetchFullArticle = false,
      showFeedFavIcon = true,
      feedContentReadingTime = 0,
    )
  val components = markdownComponents()

  AppTheme {
    ProvideReaderMarkdownLocals(markdownComponents = components, onImageClick = {}) { updateWidth ->
      ArticleReaderPage(
        readerPost = samplePost,
        showFullArticle = false,
        page = 0,
        pagerState = rememberPagerState { 1 },
        excerpt = "",
        markdownContentState = State.Loading(),
        contentParsingProgress = ReaderProcessingProgress.Idle,
        markdownComponents = components,
        isDarkTheme = false,
        themeVariant = ThemeVariant.Dynamic,
        fromScreen = ReaderScreenArgs.FromScreen.Home,
        contentPaddingValues = PaddingValues(),
        onBookmarkClick = {},
        onCommentsClick = {},
        onMarkAsUnread = {},
        onOpenWebsite = {},
        onImageClick = {},
        onContentWidthChanged = updateWidth,
      )
    }
  }
}
