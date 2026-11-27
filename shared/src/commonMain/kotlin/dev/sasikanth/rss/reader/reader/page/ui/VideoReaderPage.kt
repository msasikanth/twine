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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.components.MarkdownComponents
import com.mikepenz.markdown.model.State
import dev.sasikanth.rss.reader.components.AmbientImageBackdrop
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.home.ui.PostActions
import dev.sasikanth.rss.reader.reader.ReaderContentType
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.page.ReaderProcessingProgress
import dev.sasikanth.rss.reader.resources.icons.Play
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalImageSharedElementScopeKey
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.cdWatchVideo
import twine.shared.generated.resources.readLess
import twine.shared.generated.resources.readMore

private val VideoHeroShape = RoundedCornerShape(20.dp)
private val DescriptionCardShape = RoundedCornerShape(20.dp)
private val VideoHeroScrim = Color.Black.copy(alpha = 0.18f)
private val VideoPlayButtonBackground = Color.Black.copy(alpha = 0.62f)
private val VideoPlayButtonBorder = Color.White.copy(alpha = 0.24f)
private val VideoProviderBadgeBackground = Color.Black.copy(alpha = 0.72f)
private val CollapsedDescriptionMaxHeight = 220.dp
private const val LONG_DESCRIPTION_CHAR_THRESHOLD = 420
private const val LONG_DESCRIPTION_BLOCK_THRESHOLD = 4

@Composable
internal fun VideoReaderPage(
  readerPost: ResolvedPost,
  contentType: ReaderContentType.Video,
  markdownContentState: State,
  contentParsingProgress: ReaderProcessingProgress,
  markdownComponents: MarkdownComponents,
  isDarkTheme: Boolean,
  fromScreen: ReaderScreenArgs.FromScreen,
  contentPaddingValues: PaddingValues,
  onWatchVideo: (String) -> Unit,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onContentWidthChanged: (Dp) -> Unit,
  modifier: Modifier = Modifier,
) {
  val listState = rememberLazyListState()
  val topPadding = contentPaddingValues.calculateTopPadding()
  val bottomPadding = contentPaddingValues.calculateBottomPadding()
  val lazyColumnContentPadding =
    remember(bottomPadding) { PaddingValues(bottom = bottomPadding + 24.dp) }

  val thumbnailUrl =
    remember(readerPost.imageUrl, contentType.videoUrl) {
      UrlUtils.youTubeThumbnail(contentType.videoUrl)
        ?: UrlUtils.upgradeYouTubeThumbnail(readerPost.imageUrl)
    }

  val colorScheme = AppTheme.colorScheme
  var isDescriptionExpanded by rememberSaveable(readerPost.id) { mutableStateOf(false) }

  LazyColumn(
    modifier =
      modifier
        .fillMaxSize()
        .readerContentWidthAndScrims(
          listState = listState,
          topPadding = topPadding,
          onContentWidthChanged = onContentWidthChanged,
          horizontalPadding = 64.dp,
        ),
    state = listState,
    overscrollEffect = null,
    contentPadding = lazyColumnContentPadding,
  ) {
    item(key = "video-hero", contentType = "video-hero") {
      Box(modifier = Modifier.fillMaxWidth()) {
        AmbientImageBackdrop(
          imageUrl = thumbnailUrl,
          maxAlpha = if (isDarkTheme) 0.45f else 0.25f,
          modifier = Modifier.matchParentSize(),
        )

        Column(
          modifier = Modifier.fillMaxWidth().padding(top = topPadding + 8.dp, bottom = 16.dp)
        ) {
          DisableSelection {
            Box(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              VideoHeroSurface(
                thumbnailUrl = thumbnailUrl,
                providerLabel = contentType.providerLabel,
                onClick = { onWatchVideo(contentType.videoUrl) },
              )
            }
          }
        }
      }
    }

    item(key = "video-metadata", contentType = "video-metadata") {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        DisableSelection {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            FeedIcon(
              icon = readerPost.feedIcon,
              homepageLink = readerPost.feedHomepageLink,
              showFeedFavIcon = readerPost.showFeedFavIcon,
              contentDescription = null,
              modifier = Modifier.size(36.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = readerPost.feedName,
                style = MaterialTheme.typography.titleSmall,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )

              Text(
                text = readerPost.date.formatRelativeTime(),
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.outline,
                maxLines = 1,
              )
            }

            var showDropdown by remember { mutableStateOf(false) }

            PostActions(
              postLink = readerPost.link,
              postBookmarked = readerPost.bookmarked,
              postRead = readerPost.read,
              config = ReaderPostMetadataConfig,
              commentsLink = readerPost.commentsLink,
              showDropdown = showDropdown,
              alwaysShowMarkAsUnread = true,
              hideMarkAsOptions = fromScreen == ReaderScreenArgs.FromScreen.Bookmarks,
              onDropdownChange = { showDropdown = it },
              onBookmarkClick = onBookmarkClick,
              onCommentsClick = onCommentsClick,
              togglePostReadClick = onMarkAsUnread,
            )
          }
        }

        Text(
          text = readerPost.title.ifBlank { readerPost.description },
          style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
          color = colorScheme.onSurface,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    if (contentParsingProgress == ReaderProcessingProgress.Loading) {
      item(key = "progress-indicator", contentType = "progress-indicator") {
        ReaderProgressIndicator(modifier = Modifier.padding(top = 16.dp))
      }
    }

    if (markdownContentState is State.Success && markdownContentState.node.children.isNotEmpty()) {
      item(key = "video-description-card", contentType = "video-description-card") {
        Spacer(Modifier.requiredHeight(16.dp))
        VideoDescriptionCard(
          postId = readerPost.id,
          markdownContentState = markdownContentState,
          markdownComponents = markdownComponents,
          isExpanded = isDescriptionExpanded,
          onToggleExpanded = { isDescriptionExpanded = !isDescriptionExpanded },
          modifier = Modifier.padding(horizontal = 16.dp),
        )
      }
    }
  }
}

@Composable
private fun VideoHeroSurface(
  thumbnailUrl: String?,
  providerLabel: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val watchLabel = stringResource(Res.string.cdWatchVideo)
  val shouldBlockImage = LocalBlockImage.current

  Box(
    modifier =
      modifier
        .widthIn(max = 840.dp)
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .clip(VideoHeroShape)
        .background(Color.Black)
        .semantics { role = Role.Button }
        .clickable(onClickLabel = watchLabel, onClick = onClick)
  ) {
    if (!thumbnailUrl.isNullOrBlank() && !shouldBlockImage) {
      AsyncImage(
        url = thumbnailUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
      )
    }

    Box(modifier = Modifier.fillMaxSize().background(VideoHeroScrim))

    Box(
      modifier =
        Modifier.align(Alignment.Center)
          .size(64.dp)
          .clip(CircleShape)
          .background(VideoPlayButtonBackground)
          .border(width = 1.dp, color = VideoPlayButtonBorder, shape = CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(32.dp),
        imageVector = TwineIcons.Play,
        contentDescription = watchLabel,
        tint = Color.White,
      )
    }

    Box(
      modifier =
        Modifier.align(Alignment.BottomEnd)
          .padding(12.dp)
          .clip(CircleShape)
          .background(VideoProviderBadgeBackground)
          .padding(horizontal = 10.dp, vertical = 4.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = providerLabel,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White,
        maxLines = 1,
      )
    }
  }
}

@Composable
private fun VideoDescriptionCard(
  postId: String,
  markdownContentState: State.Success,
  markdownComponents: MarkdownComponents,
  isExpanded: Boolean,
  onToggleExpanded: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val isLong =
    remember(markdownContentState.content, markdownContentState.node.children.size) {
      markdownContentState.content.length > LONG_DESCRIPTION_CHAR_THRESHOLD ||
        markdownContentState.node.children.size > LONG_DESCRIPTION_BLOCK_THRESHOLD
    }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(DescriptionCardShape)
        .background(colorScheme.surfaceContainerLow)
        .animateContentSize()
        .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    val contentModifier =
      if (isLong && !isExpanded) {
        Modifier.fillMaxWidth().heightIn(max = CollapsedDescriptionMaxHeight).clipToBounds()
      } else {
        Modifier.fillMaxWidth()
      }

    Column(modifier = contentModifier) {
      markdownContentState.node.children.forEach { node ->
        CompositionLocalProvider(
          LocalImageSharedElementScopeKey provides "$postId:${node.markdownNodeKey()}"
        ) {
          MarkdownElement(
            node = node,
            components = markdownComponents,
            content = markdownContentState.content,
            includeSpacer = true,
          )
        }
      }
    }

    if (isLong) {
      DisableSelection {
        TranslucentButton(
          text =
            stringResource(
              if (isExpanded) {
                Res.string.readLess
              } else {
                Res.string.readMore
              }
            ),
          onClick = onToggleExpanded,
        )
      }
    }
  }
}
