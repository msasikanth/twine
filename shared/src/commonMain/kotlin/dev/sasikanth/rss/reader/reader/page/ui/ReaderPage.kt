/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.ReferenceLinkHandlerImpl
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.markdownAnimations
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.model.markdownDimens
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.home.ui.FeaturedImage
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.markdown.CoilMarkdownTransformer
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.reader.page.ReaderPageViewModel
import dev.sasikanth.rss.reader.reader.webview.ReaderWebView
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.readerDateTimestamp
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.snipme.highlights.Highlights
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmark
import twine.shared.generated.resources.comments
import twine.shared.generated.resources.share
import twine.shared.generated.resources.unBookmark

private val json = Json {
  ignoreUnknownKeys = true
  isLenient = true
  explicitNulls = false
}

@Composable
internal fun ReaderPage(
  pageViewModel: @Composable (key: String) -> ReaderPageViewModel,
  readerPost: PostWithMetadata,
  page: Int,
  pagerState: PagerState,
  highlightsBuilder: Highlights.Builder,
  loadFullArticle: Boolean,
  onBookmarkClick: () -> Unit,
  modifier: Modifier = Modifier,
  contentPaddingValues: PaddingValues = PaddingValues(),
) {
  val pageViewModel = pageViewModel(readerPost.id)
  val markdownContentState by pageViewModel.contentState.collectAsStateWithLifecycle()
  val excerptState by pageViewModel.excerptState.collectAsStateWithLifecycle()
  val contentParsingProgress by pageViewModel.parsingProgress.collectAsStateWithLifecycle()

  val linkHandler = LocalLinkHandler.current
  val sharedHandler = LocalShareHandler.current
  val coroutineScope = rememberCoroutineScope()
  val markdownComponents = remember {
    markdownComponents(
      codeBlock = { cm ->
        MarkdownHighlightedCodeBlock(
          content = cm.content,
          node = cm.node,
          highlightsBuilder = highlightsBuilder
        )
      },
      codeFence = { cm ->
        MarkdownHighlightedCodeFence(
          content = cm.content,
          node = cm.node,
          highlightsBuilder = highlightsBuilder
        )
      },
    )
  }

  val textSelectionColors =
    TextSelectionColors(
      handleColor = AppTheme.colorScheme.primary,
      backgroundColor = AppTheme.colorScheme.primary.copy(alpha = 0.4f),
    )
  CompositionLocalProvider(LocalTextSelectionColors provides textSelectionColors) {
    SelectionContainer {
      Box(modifier = modifier) {
        // Dummy view to parse the reader content using JS
        ReaderWebView(
          modifier = Modifier.requiredSize(0.dp),
          link = readerPost.link,
          content = readerPost.rawContent ?: readerPost.description,
          postImage = readerPost.imageUrl,
          fetchFullArticle = loadFullArticle,
          contentLoaded = {
            val readerContent = json.decodeFromString<ReaderContent>(it)
            pageViewModel.onParsingComplete(readerContent)
          },
        )

        CompositionLocalProvider(
          LocalReferenceLinkHandler provides ReferenceLinkHandlerImpl(),
          LocalMarkdownPadding provides
            markdownPadding(
              block = 12.dp,
            ),
          LocalMarkdownDimens provides markdownDimens(),
          LocalImageTransformer provides CoilMarkdownTransformer,
          LocalMarkdownAnnotator provides markdownAnnotator(),
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
                bottom = contentPaddingValues.calculateBottomPadding()
              )
          ) {
            item(key = "reader-header") {
              PostInfo(
                readerPost = readerPost,
                page = page,
                pagerState = pagerState,
                excerpt = excerptState,
                onCommentsClick = {
                  coroutineScope.launch { linkHandler.openLink(readerPost.commentsLink) }
                },
                onShareClick = { sharedHandler.share(readerPost.link) },
                onBookmarkClick = onBookmarkClick
              )
            }

            item(key = "divider") {
              HorizontalDivider(
                modifier =
                  Modifier.padding(horizontal = 32.dp).padding(top = 20.dp, bottom = 24.dp),
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
private fun PostInfo(
  readerPost: PostWithMetadata,
  page: Int,
  pagerState: PagerState,
  excerpt: String,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
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
          modifier =
            Modifier.graphicsLayer {
              translationX =
                if (page in 0..pagerState.pageCount) {
                  pagerState.getOffsetFractionForPage(page) * 350f
                } else {
                  0f
                }
              scaleX = 1.15f
              scaleY = 1.15f
            },
          image = postImage
        )
      }

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Column(modifier = Modifier.padding(horizontal = 32.dp)) {
      DisableSelection {
        Text(
          modifier = Modifier.padding(top = 20.dp),
          text = readerPost.date.readerDateTimestamp(),
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.outline,
          maxLines = 1,
        )
      }

      Text(
        modifier = Modifier.padding(top = 12.dp),
        text = title.ifBlank { description },
        style = MaterialTheme.typography.headlineSmall,
        color = AppTheme.colorScheme.onSurface,
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
        val showFeedFavIcon = LocalShowFeedFavIconSetting.current
        val feedIconUrl = if (showFeedFavIcon) readerPost.feedHomepageLink else readerPost.feedIcon

        DisableSelection {
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
        }

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
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
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
        modifier =
          Modifier.requiredSize(16.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
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
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.semantics { isTraversalGroup = true }) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = stringResource(Res.string.comments)
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

    val sharedLabel = stringResource(Res.string.share)
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
        stringResource(Res.string.unBookmark)
      } else {
        stringResource(Res.string.bookmark)
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
)
