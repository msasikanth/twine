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

@file:OptIn(ExperimentalFoundationApi::class)

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size as ComposeSize
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.size.Size
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import dev.sasikanth.rss.reader.utils.onDesktopContextMenu
import kotlin.math.roundToInt

private val PostListExpandedPadding = PaddingValues(horizontal = 64.dp)
private val PostListCompactPadding = PaddingValues(0.dp)
private val CompactPostListExpandedPadding = PaddingValues(horizontal = 128.dp, vertical = 12.dp)
private val CompactPostListDefaultPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
private val PostItemCornerRadius = 24.dp
private val PostItemShape = RoundedCornerShape(PostItemCornerRadius)
private val FeedIconShape = RoundedCornerShape(25)
private val PostListImageSize = Size(256, 256)

internal val postListPadding: PaddingValues
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        PostListExpandedPadding
      else -> PostListCompactPadding
    }
  }

internal val compactPostListPadding: PaddingValues
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        CompactPostListExpandedPadding
      else -> CompactPostListDefaultPadding
    }
  }

private class LazyFloatAnimTracker(
  var lastTarget: Float,
  var hasChanged: Boolean = false,
  var animatable: Animatable<Float, AnimationVector1D>? = null,
)

@Composable
private fun rememberLazyAnimatedFloat(key: Any, targetValue: Float): State<Float> {
  val state = remember(key) { mutableFloatStateOf(targetValue) }
  val tracker = remember(key) { LazyFloatAnimTracker(targetValue) }
  if (tracker.lastTarget != targetValue) {
    tracker.hasChanged = true
    tracker.lastTarget = targetValue
  }
  if (tracker.hasChanged) {
    LaunchedEffect(key, targetValue) {
      val anim = tracker.animatable ?: Animatable(state.floatValue).also { tracker.animatable = it }
      anim.animateTo(targetValue) { state.floatValue = value }
    }
  }
  return state
}

@Composable
private inline fun DesktopContextMenuHost(
  isDesktop: Boolean,
  modifier: Modifier,
  contextMenuOffset: Offset?,
  postLink: String,
  postRead: Boolean,
  config: PostMetadataConfig,
  showDropdown: Boolean,
  noinline onDismissRequest: () -> Unit,
  noinline onTogglePostRead: () -> Unit,
  content: @Composable (Modifier) -> Unit,
) {
  if (isDesktop) {
    Box(modifier = Modifier.then(modifier)) {
      content(Modifier)

      contextMenuOffset?.let { offset ->
        Box(
          modifier = Modifier.offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
        ) {
          PostActionsContextMenu(
            postLink = postLink,
            postRead = postRead,
            config = config,
            expanded = showDropdown,
            onDismissRequest = onDismissRequest,
            togglePostReadClick = onTogglePostRead,
          )
        }
      }
    }
  } else {
    content(modifier)
  }
}

@Composable
internal fun PostListItem(
  item: ResolvedPost,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  onPostSourceClick: () -> Unit,
  updatePostReadStatus: (updatedReadStatus: Boolean) -> Unit,
  modifier: Modifier = Modifier,
  reduceReadItemAlpha: Boolean = false,
  highlighted: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
  contentPadding: PaddingValues = postListPadding,
  blockImages: Boolean = LocalBlockImage.current,
  applyHorizontalSystemBarInsets: Boolean = true,
  actionLabels: PostActionLabels? = null,
) {
  var readStatus by remember(item.id, item.read) { mutableStateOf(item.read) }
  val readAlpha =
    rememberLazyAnimatedFloat(
      key = item.id,
      targetValue =
        if (readStatus && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
        else Constants.ITEM_UNREAD_ALPHA,
    )
  var showDropdown by remember(item.id) { mutableStateOf(false) }
  var contextMenuOffset by remember(item.id) { mutableStateOf<Offset?>(null) }
  val showImage = !(item.imageUrl.isNullOrBlank())
  val highlightAlpha =
    rememberLazyAnimatedFloat(key = item.id, targetValue = if (highlighted) 1f else 0f)
  val colorScheme = AppTheme.colorScheme
  val unreadCutoutColor =
    remember(colorScheme, highlightAlpha) {
      {
        val currentHighlightAlpha = highlightAlpha.value
        if (currentHighlightAlpha > 0f) {
          colorScheme.surfaceContainerHighest
            .copy(alpha = currentHighlightAlpha)
            .compositeOver(colorScheme.backdrop)
        } else {
          colorScheme.backdrop
        }
      }
    }
  val horizontalInsetsModifier =
    if (applyHorizontalSystemBarInsets) {
      val systemBars = WindowInsets.systemBars
      val horizontalInsets = remember(systemBars) { systemBars.only(WindowInsetsSides.Horizontal) }
      Modifier.windowInsetsPadding(horizontalInsets)
    } else {
      Modifier
    }

  DesktopContextMenuHost(
    isDesktop = platform is Platform.Desktop,
    modifier = modifier,
    contextMenuOffset = contextMenuOffset,
    postLink = item.link,
    postRead = readStatus,
    config = postMetadataConfig,
    showDropdown = showDropdown,
    onDismissRequest = {
      showDropdown = false
      contextMenuOffset = null
    },
    onTogglePostRead = {
      readStatus = !readStatus
      updatePostReadStatus(readStatus)
    },
  ) { itemModifier ->
    Column(
      modifier =
        itemModifier
          .then(horizontalInsetsModifier)
          .padding(contentPadding)
          .clip(PostItemShape)
          .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
          .onDesktopContextMenu { offset ->
            contextMenuOffset = offset
            showDropdown = true
          }
          .drawWithCache {
            val cornerRadius = CornerRadius(PostItemCornerRadius.toPx())
            onDrawBehind {
              val currentHighlightAlpha = highlightAlpha.value
              if (currentHighlightAlpha > 0f) {
                drawRoundRect(
                  color = colorScheme.surfaceContainerHighest.copy(alpha = currentHighlightAlpha),
                  cornerRadius = cornerRadius,
                )
              }
            }
          }
          .then(
            if (reduceReadItemAlpha) {
              Modifier.graphicsLayer {
                this.alpha = readAlpha.value
                compositingStrategy = CompositingStrategy.ModulateAlpha
              }
            } else {
              Modifier
            }
          )
          .semantics { contentDescription = item.title.ifBlank { item.description } }
          .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
      Row(modifier = Modifier.padding(top = 8.dp)) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            modifier = Modifier.padding(end = if (showImage) 16.dp else 0.dp),
            style = MaterialTheme.typography.titleMedium,
            text = item.title.ifBlank { item.description },
            color = { colorScheme.onSurface },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )

          if (item.description.isNotBlank()) {
            Spacer(Modifier.requiredHeight(4.dp))

            Text(
              modifier = Modifier.padding(end = if (showImage) 16.dp else 0.dp),
              style = MaterialTheme.typography.bodyMedium,
              text = item.description,
              color = { colorScheme.onSurfaceVariant },
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        if (!blockImages) {
          item.imageUrl?.let { url ->
            Box(modifier = Modifier.requiredSize(64.dp), contentAlignment = Alignment.Center) {
              AsyncImage(
                url = url,
                modifier = Modifier.aspectRatio(1f).clip(FeedIconShape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                size = PostListImageSize,
              )
            }
          }
        }
      }

      Spacer(Modifier.height(4.dp))

      val relativeTimestamp = item.date.formatRelativeTime()
      PostActionBar(
        feedName = item.feedName,
        feedIcon = item.feedIcon,
        feedHomepageLink = item.feedHomepageLink,
        showFeedFavIcon = item.showFeedFavIcon,
        postRead = readStatus,
        postRelativeTimestamp = relativeTimestamp,
        postLink = item.link,
        postBookmarked = item.bookmarked,
        commentsLink = item.commentsLink,
        postReadingTimeEstimate = item.feedContentReadingTime ?: 0,
        onBookmarkClick = onPostBookmarkClick,
        onCommentsClick = onPostCommentsClick,
        onTogglePostReadClick = {
          readStatus = !readStatus
          updatePostReadStatus(readStatus)
        },
        showDropdown = contextMenuOffset == null && showDropdown,
        onDropdownChange = { showDropdown = it },
        config = postMetadataConfig,
        onSourceClick = onPostSourceClick,
        actionLabels = actionLabels,
        unreadIndicatorCutoutColor = unreadCutoutColor,
      )
    }
  }
}

@Composable
internal fun SimplePostListItem(
  item: ResolvedPost,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  onPostSourceClick: () -> Unit,
  updatePostReadStatus: (updatedReadStatus: Boolean) -> Unit,
  modifier: Modifier = Modifier,
  reduceReadItemAlpha: Boolean = false,
  highlighted: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
  contentPadding: PaddingValues = postListPadding,
  blockImages: Boolean = LocalBlockImage.current,
  applyHorizontalSystemBarInsets: Boolean = true,
  actionLabels: PostActionLabels? = null,
) {
  var readStatus by remember(item.id, item.read) { mutableStateOf(item.read) }
  val readAlpha =
    rememberLazyAnimatedFloat(
      key = item.id,
      targetValue =
        if (readStatus && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
        else Constants.ITEM_UNREAD_ALPHA,
    )
  var showDropdown by remember(item.id) { mutableStateOf(false) }
  var contextMenuOffset by remember(item.id) { mutableStateOf<Offset?>(null) }
  val showImage = !(item.imageUrl.isNullOrBlank())
  val highlightAlpha =
    rememberLazyAnimatedFloat(key = item.id, targetValue = if (highlighted) 1f else 0f)
  val colorScheme = AppTheme.colorScheme
  val unreadCutoutColor =
    remember(colorScheme, highlightAlpha) {
      {
        val currentHighlightAlpha = highlightAlpha.value
        if (currentHighlightAlpha > 0f) {
          colorScheme.surfaceContainerHighest
            .copy(alpha = currentHighlightAlpha)
            .compositeOver(colorScheme.backdrop)
        } else {
          colorScheme.backdrop
        }
      }
    }
  val horizontalInsetsModifier =
    if (applyHorizontalSystemBarInsets) {
      val systemBars = WindowInsets.systemBars
      val horizontalInsets = remember(systemBars) { systemBars.only(WindowInsetsSides.Horizontal) }
      Modifier.windowInsetsPadding(horizontalInsets)
    } else {
      Modifier
    }

  DesktopContextMenuHost(
    isDesktop = platform is Platform.Desktop,
    modifier = modifier,
    contextMenuOffset = contextMenuOffset,
    postLink = item.link,
    postRead = readStatus,
    config = postMetadataConfig,
    showDropdown = showDropdown,
    onDismissRequest = {
      showDropdown = false
      contextMenuOffset = null
    },
    onTogglePostRead = {
      readStatus = !readStatus
      updatePostReadStatus(readStatus)
    },
  ) { itemModifier ->
    Column(
      modifier =
        itemModifier
          .then(horizontalInsetsModifier)
          .padding(contentPadding)
          .clip(PostItemShape)
          .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
          .onDesktopContextMenu { offset ->
            contextMenuOffset = offset
            showDropdown = true
          }
          .drawWithCache {
            val cornerRadius = CornerRadius(PostItemCornerRadius.toPx())
            onDrawBehind {
              val currentHighlightAlpha = highlightAlpha.value
              if (currentHighlightAlpha > 0f) {
                drawRoundRect(
                  color = colorScheme.surfaceContainerHighest.copy(alpha = currentHighlightAlpha),
                  cornerRadius = cornerRadius,
                )
              }
            }
          }
          .then(
            if (reduceReadItemAlpha) {
              Modifier.graphicsLayer {
                this.alpha = readAlpha.value
                compositingStrategy = CompositingStrategy.ModulateAlpha
              }
            } else {
              Modifier
            }
          )
          .semantics { contentDescription = item.title.ifBlank { item.description } }
          .padding(horizontal = 24.dp, vertical = 4.dp)
    ) {
      Row(modifier = Modifier.padding(top = 8.dp)) {
        Column(modifier = Modifier.padding(vertical = 4.dp).weight(1f)) {
          Text(
            modifier = Modifier.padding(end = if (showImage) 16.dp else 0.dp),
            style = MaterialTheme.typography.titleMedium,
            text = item.title.ifBlank { item.description },
            color = { colorScheme.onSurface },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }

        if (!blockImages) {
          item.imageUrl?.let { url ->
            Box(modifier = Modifier.requiredSize(48.dp), contentAlignment = Alignment.Center) {
              AsyncImage(
                url = url,
                modifier = Modifier.aspectRatio(1f).clip(FeedIconShape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                size = PostListImageSize,
              )
            }
          }
        }
      }

      Spacer(Modifier.height(4.dp))

      val relativeTimestamp = item.date.formatRelativeTime()
      PostActionBar(
        feedName = item.feedName,
        feedIcon = item.feedIcon,
        feedHomepageLink = item.feedHomepageLink,
        showFeedFavIcon = item.showFeedFavIcon,
        postRead = readStatus,
        postRelativeTimestamp = relativeTimestamp,
        postLink = item.link,
        postBookmarked = item.bookmarked,
        commentsLink = item.commentsLink,
        postReadingTimeEstimate = item.feedContentReadingTime ?: 0,
        onBookmarkClick = onPostBookmarkClick,
        onCommentsClick = onPostCommentsClick,
        onTogglePostReadClick = {
          readStatus = !readStatus
          updatePostReadStatus(readStatus)
        },
        showDropdown = contextMenuOffset == null && showDropdown,
        onDropdownChange = { showDropdown = it },
        config = postMetadataConfig,
        onSourceClick = onPostSourceClick,
        actionLabels = actionLabels,
        unreadIndicatorCutoutColor = unreadCutoutColor,
      )
    }
  }
}

@Composable
internal fun CompactPostListItem(
  item: ResolvedPost,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  updatePostReadStatus: (updatedReadStatus: Boolean) -> Unit,
  modifier: Modifier = Modifier,
  reduceReadItemAlpha: Boolean = false,
  highlighted: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
  contentPadding: PaddingValues = compactPostListPadding,
  actionLabels: PostActionLabels? = null,
) {
  var readStatus by remember(item.id, item.read) { mutableStateOf(item.read) }
  val readAlpha =
    rememberLazyAnimatedFloat(
      key = item.id,
      targetValue =
        if (readStatus && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
        else Constants.ITEM_UNREAD_ALPHA,
    )
  var showDropdown by remember(item.id) { mutableStateOf(false) }
  var contextMenuOffset by remember(item.id) { mutableStateOf<Offset?>(null) }
  val highlightAlpha =
    rememberLazyAnimatedFloat(key = item.id, targetValue = if (highlighted) 1f else 0f)
  val colorScheme = AppTheme.colorScheme
  val errorColor = MaterialTheme.colorScheme.error
  val unreadCutoutColor =
    remember(colorScheme, highlightAlpha) {
      {
        val currentHighlightAlpha = highlightAlpha.value
        if (currentHighlightAlpha > 0f) {
          colorScheme.surfaceContainerHighest
            .copy(alpha = currentHighlightAlpha)
            .compositeOver(colorScheme.backdrop)
        } else {
          colorScheme.backdrop
        }
      }
    }

  DesktopContextMenuHost(
    isDesktop = platform is Platform.Desktop,
    modifier = modifier,
    contextMenuOffset = contextMenuOffset,
    postLink = item.link,
    postRead = readStatus,
    config = postMetadataConfig,
    showDropdown = showDropdown,
    onDismissRequest = {
      showDropdown = false
      contextMenuOffset = null
    },
    onTogglePostRead = {
      readStatus = !readStatus
      updatePostReadStatus(readStatus)
    },
  ) { itemModifier ->
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        itemModifier
          .clip(PostItemShape)
          .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
          .onDesktopContextMenu { offset ->
            contextMenuOffset = offset
            showDropdown = true
          }
          .drawWithCache {
            val cornerRadius = CornerRadius(PostItemCornerRadius.toPx())
            onDrawBehind {
              val currentHighlightAlpha = highlightAlpha.value
              if (currentHighlightAlpha > 0f) {
                drawRoundRect(
                  color = colorScheme.surfaceContainerHighest.copy(alpha = currentHighlightAlpha),
                  cornerRadius = cornerRadius,
                )
              }
            }
          }
          .padding(contentPadding)
          .then(
            if (reduceReadItemAlpha) {
              Modifier.graphicsLayer {
                this.alpha = readAlpha.value
                compositingStrategy = CompositingStrategy.ModulateAlpha
              }
            } else {
              Modifier
            }
          ),
    ) {
      val showUnreadIndicator = postMetadataConfig.showUnreadIndicator && !readStatus
      Box(
        modifier =
          Modifier.requiredSize(24.dp).drawWithCache {
            val dotRadius = 4.dp.toPx()
            val haloRadius = 5.dp.toPx()
            val dotCenterX =
              if (layoutDirection == LayoutDirection.Ltr) size.width - dotRadius else dotRadius
            val dotCenter = Offset(dotCenterX, dotRadius)
            onDrawWithContent {
              drawContent()
              if (showUnreadIndicator) {
                drawCircle(color = unreadCutoutColor(), radius = haloRadius, center = dotCenter)
                drawCircle(color = errorColor, radius = dotRadius, center = dotCenter)
              }
            }
          }
      ) {
        FeedIcon(
          icon = item.feedIcon,
          homepageLink = item.feedHomepageLink,
          showFeedFavIcon = item.showFeedFavIcon,
          contentDescription = null,
          modifier =
            Modifier.requiredSize(20.dp)
              .drawWithCache {
                val strokeWidth = 1.dp.toPx()
                val halfStroke = strokeWidth / 2f
                val borderTopLeft = Offset(halfStroke, halfStroke)
                val borderSize = ComposeSize(size.width - strokeWidth, size.height - strokeWidth)
                val borderCornerRadius =
                  CornerRadius((size.minDimension * 0.25f - halfStroke).coerceAtLeast(0f))
                val borderStyle = Stroke(width = strokeWidth)
                onDrawWithContent {
                  drawContent()
                  drawRoundRect(
                    color = colorScheme.outlineVariant,
                    topLeft = borderTopLeft,
                    size = borderSize,
                    cornerRadius = borderCornerRadius,
                    style = borderStyle,
                  )
                }
              }
              .align(Alignment.Center),
        )
      }

      Spacer(Modifier.requiredWidth(16.dp))

      Text(
        text = item.title.ifBlank { item.description },
        style = MaterialTheme.typography.titleMedium,
        color = { colorScheme.onSurface },
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      Spacer(Modifier.requiredWidth(16.dp))

      PostActions(
        postLink = item.link,
        postBookmarked = item.bookmarked,
        postRead = readStatus,
        config = postMetadataConfig,
        commentsLink = item.commentsLink,
        showDropdown = contextMenuOffset == null && showDropdown,
        onDropdownChange = { showDropdown = it },
        onBookmarkClick = onPostBookmarkClick,
        onCommentsClick = onPostCommentsClick,
        actionLabels = actionLabels,
      ) {
        readStatus = !readStatus
        updatePostReadStatus(readStatus)
      }
    }
  }
}
