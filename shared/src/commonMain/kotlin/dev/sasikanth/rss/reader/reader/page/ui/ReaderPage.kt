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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.home.ui.FeaturedImage
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.markdown.CoilMarkdownTransformer
import dev.sasikanth.rss.reader.media.PlaybackState
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.Forward30
import dev.sasikanth.rss.reader.resources.icons.Pause
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.Replay30
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.VisibilityOff
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.GolosFontFamily
import dev.sasikanth.rss.reader.util.readerDateTimestamp
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.ParallaxAlignment
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.intellij.markdown.MarkdownElementTypes
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmark
import twine.shared.generated.resources.comments
import twine.shared.generated.resources.markAsUnRead
import twine.shared.generated.resources.pause
import twine.shared.generated.resources.play
import twine.shared.generated.resources.readingTimeEstimate
import twine.shared.generated.resources.seek_backward
import twine.shared.generated.resources.seek_forward
import twine.shared.generated.resources.share
import twine.shared.generated.resources.unBookmark

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  explicitNulls = false
}

@Composable
internal fun ReaderPage(
  pageViewModel: ReaderPageViewModel,
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  markdownComponents: MarkdownComponents,
  isDarkTheme: Boolean,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  val markdownContentState by pageViewModel.contentState.collectAsStateWithLifecycle()
  val excerptState by pageViewModel.excerptState.collectAsStateWithLifecycle()
  val contentParsingProgress by pageViewModel.parsingProgress.collectAsStateWithLifecycle()
  val playbackState by pageViewModel.audioPlayer.playbackState.collectAsStateWithLifecycle()

  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current
  val shouldBlockImage = LocalBlockImage.current

  val coroutineScope = rememberCoroutineScope()

  val textSelectionColors =
    TextSelectionColors(
      handleColor = AppTheme.colorScheme.primary,
      backgroundColor = AppTheme.colorScheme.primary.copy(alpha = 0.4f),
    )

  CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
    SelectionContainer {
      Box(modifier = modifier) {
        CompositionLocalProvider(
          LocalReferenceLinkHandler provides ReferenceLinkHandlerImpl(),
          LocalMarkdownPadding provides
            markdownPadding(
              block = 12.dp,
            ),
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
                bottom = contentPaddingValues.calculateBottomPadding() + 24.dp
              )
          ) {
            item(key = "reader-header") {
              PostHeader(
                readerPost = readerPost,
                showFullArticle = showFullArticle,
                page = page,
                pagerState = pagerState,
                excerpt = excerptState,
                darkTheme = isDarkTheme,
                onCommentsClick = {
                  coroutineScope.launch { linkHandler.openLink(readerPost.commentsLink) }
                },
                onShareClick = { sharedHandler.share(readerPost.link) },
                onBookmarkClick = onBookmarkClick,
                onMarkAsUnread = onMarkAsUnread,
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

                MediaControls(
                  playbackState = postPlaybackState,
                  onPlayClick = pageViewModel::playAudio,
                  onPauseClick = pageViewModel::pauseAudio,
                  onSeek = pageViewModel::seekAudio,
                  onSeekForward = pageViewModel::seekForward,
                  onSeekBackward = pageViewModel::seekBackward,
                  onPlaybackSpeedChange = {
                    val newSpeed =
                      when (postPlaybackState.playbackSpeed) {
                        0.5f -> 1.0f
                        1.0f -> 1.5f
                        1.5f -> 2.0f
                        2.0f -> 0.5f
                        else -> 1.0f
                      }
                    pageViewModel.setPlaybackSpeed(newSpeed)
                  },
                  modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp)
                )
              }
            }

            item(key = "divider") {
              HorizontalDivider(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp),
                color = AppTheme.colorScheme.outlineVariant
              )
            }

            if (contentParsingProgress == ReaderProcessingProgress.Loading) {
              item(key = "progress-indicator") { ProgressIndicator() }
            }

            when (val state = markdownContentState) {
              is State.Success -> {
                items(items = state.node.children) { node ->
                  Box(modifier = Modifier.padding(horizontal = 32.dp)) {
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
      trackColor = AppTheme.colorScheme.tintedSurface,
      color = AppTheme.colorScheme.tintedForeground,
    )
  }
}

@Composable
private fun PostHeader(
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  excerpt: String,
  darkTheme: Boolean,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = Modifier.fillMaxWidth().then(modifier),
  ) {
    val title = readerPost.title
    val description = readerPost.description
    val postImage = readerPost.imageUrl

    if (!postImage.isNullOrBlank()) {
      Box(modifier = Modifier.padding(horizontal = 24.dp).align(Alignment.CenterHorizontally)) {
        FeaturedImage(
          imageUrl = postImage,
          unlockAspectRatio = UrlUtils.isUnconstrainedMedia(postImage),
          alignment =
            remember(pagerState) {
              ParallaxAlignment(
                horizontalBias = { pagerState.getOffsetFractionForPage(page) },
                multiplier = 2f,
              )
            }
        )
      }

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Column(modifier = Modifier.padding(horizontal = 32.dp)) {
      DisableSelection {
        Row(
          modifier = Modifier.padding(top = 20.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = readerPost.date.readerDateTimestamp(),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.outline,
            maxLines = 1,
          )

          val readingTimeEstimate =
            readerPost.articleContentReadingTime?.takeIf { showFullArticle }
              ?: readerPost.feedContentReadingTime ?: 0

          if (readingTimeEstimate > 0) {
            Text(
              modifier = Modifier.padding(horizontal = 4.dp).clearAndSetSemantics {},
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              text = "\u2022",
              color = AppTheme.colorScheme.outline,
            )

            Text(
              text = stringResource(Res.string.readingTimeEstimate, readingTimeEstimate),
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.outline,
              maxLines = 1,
            )
          }
        }
      }

      Text(
        modifier =
          Modifier.padding(top = 12.dp).graphicsLayer {
            blendMode =
              if (darkTheme) {
                BlendMode.Screen
              } else {
                BlendMode.Multiply
              }
          },
        text = title.ifBlank { description },
        style = MaterialTheme.typography.headlineMedium,
        fontFamily = GolosFontFamily,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colorScheme.secondary,
        overflow = TextOverflow.Ellipsis,
      )

      if (excerpt.isNotBlank()) {
        Spacer(Modifier.requiredHeight(8.dp))

        Text(
          text = excerpt,
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.secondary,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.requiredHeight(12.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        DisableSelection {
          PostSourcePill(
            modifier = Modifier.weight(1f).clearAndSetSemantics {},
            feedName = readerPost.feedName,
            feedIcon = readerPost.feedIcon,
            feedHomepageLink = readerPost.feedHomepageLink,
            showFeedFavIcon = readerPost.showFeedFavIcon,
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
        }

        PostActions(
          postBookmarked = readerPost.bookmarked,
          commentsLink = readerPost.commentsLink,
          onCommentsClick = onCommentsClick,
          onShareClick = onShareClick,
          onBookmarkClick = onBookmarkClick,
          onMarkAsUnread = onMarkAsUnread,
        )
      }
    }
  }
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
  feedName: String,
  config: PostMetadataConfig,
  onSourceClick: () -> Unit,
  modifier: Modifier = Modifier,
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
            androidx.compose.foundation.shape.RoundedCornerShape(50)
          )
          .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
          .clickable(onClick = onSourceClick, enabled = config.enablePostSource)
          .padding(vertical = 6.dp)
          .padding(start = 8.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FeedIcon(
        modifier = Modifier.requiredSize(16.dp),
        icon = feedIcon,
        homepageLink = feedHomepageLink,
        showFeedFavIcon = showFeedFavIcon,
        shape = MaterialTheme.shapes.extraSmall,
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
private fun PostActions(
  postBookmarked: Boolean,
  commentsLink: String?,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.semantics { isTraversalGroup = true }) {
    val markAsUnreadLabel = stringResource(Res.string.markAsUnRead)
    val markAsUnreadIcon = TwineIcons.VisibilityOff

    PostActionButton(
      label = markAsUnreadLabel,
      icon = markAsUnreadIcon,
      iconTint = AppTheme.colorScheme.onSurfaceVariant,
      onClick = onMarkAsUnread
    )

    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = stringResource(Res.string.comments)
      PostActionButton(
        label = commentsLabel,
        icon = TwineIcons.Comments,
        iconTint = AppTheme.colorScheme.onSurfaceVariant,
        onClick = onCommentsClick
      )
    }

    val sharedLabel = stringResource(Res.string.share)
    PostActionButton(
      label = sharedLabel,
      icon = TwineIcons.Share,
      iconTint = AppTheme.colorScheme.onSurfaceVariant,
      onClick = onShareClick
    )

    val bookmarkLabel =
      if (postBookmarked) {
        stringResource(Res.string.unBookmark)
      } else {
        stringResource(Res.string.bookmark)
      }
    PostActionButton(
      label = bookmarkLabel,
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
private fun PostActionButton(
  icon: ImageVector,
  label: String,
  modifier: Modifier = Modifier,
  iconTint: Color = AppTheme.colorScheme.textEmphasisHigh,
  onClick: () -> Unit,
) {
  TooltipBox(
    positionProvider =
      TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
    tooltip = {
      Box(
        modifier =
          Modifier.background(AppTheme.colorScheme.surface, RoundedCornerShape(4.dp)).padding(8.dp),
      ) {
        Text(text = label)
      }
    },
    state = rememberTooltipState()
  ) {
    Box(
      modifier =
        Modifier.requiredSize(40.dp)
          .clip(MaterialTheme.shapes.small)
          .clickable(onClick = onClick)
          .semantics {
            role = Role.Button
            contentDescription = label
          }
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
}

@Composable
private fun MediaControls(
  playbackState: PlaybackState,
  onPlayClick: () -> Unit,
  onPauseClick: () -> Unit,
  onSeek: (Long) -> Unit,
  onSeekForward: () -> Unit,
  onSeekBackward: () -> Unit,
  onPlaybackSpeedChange: (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  val isPlaying = playbackState.isPlaying
  val progress =
    if (playbackState.duration > 0) {
      (playbackState.currentPosition.toFloat() / playbackState.duration.toFloat()).coerceIn(0f, 1f)
    } else {
      0f
    }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface, RoundedCornerShape(16.dp))
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column {
      Slider(
        modifier = Modifier.padding(top = 8.dp),
        value = progress,
        onValueChange = { onSeek((it * playbackState.duration).toLong()) },
        colors =
          SliderDefaults.colors(
            thumbColor = AppTheme.colorScheme.primary,
            activeTrackColor = AppTheme.colorScheme.primary,
            inactiveTrackColor = AppTheme.colorScheme.primary.copy(alpha = 0.24f)
          ),
      )

      Row(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = formatDuration(playbackState.currentPosition),
          style = MaterialTheme.typography.labelSmall,
          color = AppTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
          text = formatDuration(playbackState.duration),
          style = MaterialTheme.typography.labelSmall,
          color = AppTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceEvenly
    ) {
      TextButton(onClick = { onPlaybackSpeedChange(playbackState.playbackSpeed) }) {
        Text(
          text = "${playbackState.playbackSpeed}x",
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.onSurfaceVariant
        )
      }

      IconButton(onClick = onSeekBackward) {
        Icon(
          imageVector = TwineIcons.Replay30,
          contentDescription = stringResource(Res.string.seek_backward),
          tint = AppTheme.colorScheme.onSurfaceVariant
        )
      }

      Box(
        modifier =
          Modifier.requiredSize(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colorScheme.primaryContainer)
            .clickable(onClick = if (isPlaying) onPauseClick else onPlayClick),
        contentAlignment = Alignment.Center
      ) {
        if (playbackState.buffering) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = AppTheme.colorScheme.onPrimaryContainer,
            strokeWidth = 2.dp
          )
        } else {
          Icon(
            imageVector = if (isPlaying) TwineIcons.Pause else TwineIcons.Play,
            contentDescription =
              if (isPlaying) stringResource(Res.string.pause) else stringResource(Res.string.play),
            tint = AppTheme.colorScheme.onPrimaryContainer,
          )
        }
      }

      IconButton(onClick = onSeekForward) {
        Icon(
          imageVector = TwineIcons.Forward30,
          contentDescription = stringResource(Res.string.seek_forward),
          tint = AppTheme.colorScheme.onSurfaceVariant
        )
      }

      // Spacer to balance the layout, matching TextButton width roughly
      Spacer(Modifier.requiredWidth(48.dp))
    }
  }
}

private fun formatDuration(duration: Long): String {
  val seconds = (duration / 1000) % 60
  val minutes = (duration / (1000 * 60)) % 60
  val hours = (duration / (1000 * 60 * 60))

  return if (hours > 0) {
    "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  } else {
    "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
  }
}
