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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.compose.LocalImageTransformer
import com.mikepenz.markdown.compose.LocalMarkdownAnimations
import com.mikepenz.markdown.compose.LocalMarkdownAnnotator
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.LocalReferenceLinkHandler
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.markdown.CoilMarkdownTransformer
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.media.SleepTimerOption
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.reader.ui.LocalOnImageClick
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import kotlin.time.Instant
import kotlinx.coroutines.launch
import org.intellij.markdown.MarkdownElementTypes

@Composable
internal fun ReaderPage(
  pageViewModel: ReaderPageViewModel,
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  markdownComponents: MarkdownComponents,
  isDarkTheme: Boolean,
  themeVariant: ThemeVariant,
  fromScreen: ReaderScreenArgs.FromScreen,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  val markdownContentState by pageViewModel.contentState.collectAsStateWithLifecycle()
  val excerptState by pageViewModel.excerptState.collectAsStateWithLifecycle()
  val contentParsingProgress by pageViewModel.parsingProgress.collectAsStateWithLifecycle()
  val playbackState by pageViewModel.audioPlayer.playbackState.collectAsStateWithLifecycle()

  ReaderPageContent(
    markdownContentState = markdownContentState,
    excerptState = excerptState,
    contentParsingProgress = contentParsingProgress,
    playbackState = playbackState,
    readerPost = readerPost,
    showFullArticle = showFullArticle,
    page = page,
    pagerState = pagerState,
    markdownComponents = markdownComponents,
    isDarkTheme = isDarkTheme,
    themeVariant = themeVariant,
    fromScreen = fromScreen,
    onBookmarkClick = onBookmarkClick,
    onMarkAsUnread = onMarkAsUnread,
    onImageClick = onImageClick,
    onPlayClick = pageViewModel::playAudio,
    onPauseClick = pageViewModel::pauseAudio,
    onSeek = pageViewModel::seekAudio,
    onSeekForward = pageViewModel::seekForward,
    onSeekBackward = pageViewModel::seekBackward,
    onPlaybackSpeedChange = { speed -> pageViewModel.setPlaybackSpeed(speed) },
    onSleepTimerOptionSelected = { pageViewModel.setSleepTimer(it) },
    modifier = modifier,
    contentPaddingValues = contentPaddingValues,
  )
}

@Composable
private fun ReaderPageContent(
  markdownContentState: State,
  excerptState: String?,
  contentParsingProgress: ReaderProcessingProgress,
  playbackState: PlaybackState,
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  markdownComponents: MarkdownComponents,
  isDarkTheme: Boolean,
  themeVariant: ThemeVariant,
  fromScreen: ReaderScreenArgs.FromScreen,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (String) -> Unit,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  onSleepTimerOptionSelected: (SleepTimerOption) -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current
  val shouldBlockImage = LocalBlockImage.current

  val coroutineScope = rememberCoroutineScope()
  var showSleepTimerSheet by remember { mutableStateOf(false) }

  val textSelectionColors =
    TextSelectionColors(
      handleColor = AppTheme.colorScheme.primary,
      backgroundColor = AppTheme.colorScheme.primary.copy(alpha = 0.4f),
    )

  CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
    SelectionContainer {
      Box(modifier = modifier) {
        CompositionLocalProvider(
          LocalOnImageClick provides onImageClick,
          LocalReferenceLinkHandler provides ReferenceLinkHandlerImpl(),
          LocalMarkdownPadding provides markdownPadding(block = 12.dp),
          LocalMarkdownDimens provides markdownDimens(),
          LocalImageTransformer provides CoilMarkdownTransformer,
          LocalMarkdownAnnotator provides
            markdownAnnotator(
              annotate = { _, node ->
                // skipping images when "block images" is enabled
                node.type == MarkdownElementTypes.IMAGE && shouldBlockImage
              }
            ),
          LocalMarkdownAnimations provides markdownAnimations(animateTextSize = { this }),
          LocalMarkdownColors provides
            markdownColor(
              text = AppTheme.colorScheme.onSurface,
              codeBackground = AppTheme.colorScheme.onSurface.copy(alpha = 0.1f),
              dividerColor = AppTheme.colorScheme.outlineVariant,
              tableBackground = AppTheme.colorScheme.onSurface.copy(alpha = 0.02f),
            ),
          LocalMarkdownTypography provides
            markdownTypography(
              h1 = MaterialTheme.typography.displaySmall,
              h2 = MaterialTheme.typography.headlineLarge,
              h3 = MaterialTheme.typography.headlineMedium,
              h4 = MaterialTheme.typography.headlineSmall,
              h5 = MaterialTheme.typography.titleLarge,
              h6 = MaterialTheme.typography.titleMedium,
              textLink =
                TextLinkStyles(
                  MaterialTheme.typography.bodyLarge
                    .copy(
                      fontWeight = FontWeight.Bold,
                      textDecoration = TextDecoration.Underline,
                      color = AppTheme.colorScheme.primary,
                    )
                    .toSpanStyle()
                ),
            ),
        ) {
          LazyColumn(
            modifier = Modifier.fillMaxSize(),
            overscrollEffect = null,
            contentPadding =
              PaddingValues(
                top = contentPaddingValues.calculateTopPadding(),
                bottom = contentPaddingValues.calculateBottomPadding() + 24.dp,
              ),
          ) {
            item(key = "reader-header") {
              PostHeader(
                readerPost = readerPost,
                showFullArticle = showFullArticle,
                page = page,
                pagerState = pagerState,
                excerpt = excerptState ?: "",
                darkTheme = isDarkTheme,
                themeVariant = themeVariant,
                fromScreen = fromScreen,
                onCommentsClick = {
                  val commentsLink = readerPost.commentsLink
                  if (commentsLink != null) {
                    coroutineScope.launch { linkHandler.openLink(commentsLink) }
                  }
                },
                onShareClick = { sharedHandler.share(readerPost.link) },
                onBookmarkClick = onBookmarkClick,
                onMarkAsUnread = onMarkAsUnread,
                onImageClick = onImageClick,
              )
            }

            if (!readerPost.audioUrl.isNullOrBlank()) {
              item(key = "podcast-player") {
                val isPostAudioPlaying = playbackState.playingUrl == readerPost.audioUrl
                val postPlaybackState =
                  if (isPostAudioPlaying) {
                    playbackState
                  } else {
                    PlaybackState.Idle
                  }

                DisableSelection {
                  MediaControls(
                    playbackState = postPlaybackState,
                    onPlayClick = onPlayClick,
                    onPauseClick = onPauseClick,
                    onSeek = onSeek,
                    onSeekForward = onSeekForward,
                    onSeekBackward = onSeekBackward,
                    onPlaybackSpeedChange = {
                      val newSpeed =
                        when (postPlaybackState.playbackSpeed) {
                          0.5f -> 1.0f
                          1.0f -> 1.5f
                          1.5f -> 2.0f
                          2.0f -> 0.5f
                          else -> 1.0f
                        }
                      onPlaybackSpeedChange(newSpeed)
                    },
                    onSleepTimerClick = { showSleepTimerSheet = true },
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp),
                  )
                }
              }
            }

            item(key = "divider") {
              HorizontalDivider(
                modifier = Modifier.padding(24.dp),
                color = AppTheme.colorScheme.outlineVariant,
              )
            }

            if (contentParsingProgress == ReaderProcessingProgress.Loading) {
              item(key = "progress-indicator") { ProgressIndicator() }
            }

            when (val state = markdownContentState) {
              is State.Success -> {
                items(items = state.node.children) { node ->
                  Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    MarkdownElement(
                      node = node,
                      components = markdownComponents,
                      content = state.content,
                      includeSpacer = true,
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

        if (showSleepTimerSheet) {
          SleepTimerBottomSheet(
            playbackState = playbackState,
            onOptionSelected = {
              onSleepTimerOptionSelected(it)
              showSleepTimerSheet = false
            },
            onDismiss = { showSleepTimerSheet = false },
          )
        }
      }
    }
  }
}

@Composable
private fun ProgressIndicator() {
  Box(
    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    LinearProgressIndicator(
      trackColor = AppTheme.colorScheme.surfaceContainerLow,
      color = AppTheme.colorScheme.primary,
    )
  }
}

@Preview(locale = "en")
@Composable
private fun ReaderPagePreview() {
  AppTheme {
    ReaderPageContent(
      markdownContentState = State.Loading(),
      excerptState = null,
      contentParsingProgress = ReaderProcessingProgress.Idle,
      playbackState = PlaybackState.Idle,
      readerPost =
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
        ),
      showFullArticle = false,
      page = 0,
      pagerState = rememberPagerState { 1 },
      markdownComponents = markdownComponents(),
      isDarkTheme = false,
      themeVariant = ThemeVariant.Dynamic,
      fromScreen = ReaderScreenArgs.FromScreen.Home,
      onBookmarkClick = {},
      onMarkAsUnread = {},
      onImageClick = {},
      onPlayClick = {},
      onPauseClick = {},
      onSeek = {},
      onSeekForward = {},
      onSeekBackward = {},
      onPlaybackSpeedChange = {},
      onSleepTimerOptionSelected = {},
    )
  }
}
