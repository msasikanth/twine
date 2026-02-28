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

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.formatRelativeTime

private val postListPadding: PaddingValues
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        PaddingValues(horizontal = 64.dp)
      else -> PaddingValues(0.dp)
    }
  }

private val compactPostListPadding: PaddingValues
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        PaddingValues(horizontal = 128.dp, vertical = 12.dp)
      else -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
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
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
) {
  var readStatus by remember(item.read) { mutableStateOf(item.read) }
  val alpha by
    animateFloatAsState(
      if (readStatus && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
      else Constants.ITEM_UNREAD_ALPHA
    )
  var showDropdown by remember { mutableStateOf(false) }
  val showImage = !(item.imageUrl.isNullOrBlank())

  Column(
    modifier =
      Modifier.then(modifier)
        .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        .padding(postListPadding)
        .graphicsLayer { this.alpha = alpha }
        .semantics { contentDescription = item.title.ifBlank { item.description } }
        .padding(horizontal = 24.dp, vertical = 8.dp)
  ) {
    Row {
      Column(modifier = Modifier.weight(1f)) {
        Spacer(Modifier.height(8.dp))

        Text(
          modifier = Modifier.padding(end = if (showImage) 16.dp else 0.dp),
          style = MaterialTheme.typography.titleSmall,
          text = item.title.ifBlank { item.description },
          color = AppTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        if (item.description.isNotBlank()) {
          Spacer(Modifier.requiredHeight(4.dp))

          Text(
            modifier = Modifier.padding(end = if (showImage) 16.dp else 0.dp),
            style = MaterialTheme.typography.bodySmall,
            text = item.description,
            color = AppTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      item.imageUrl?.let { url ->
        Box(
          modifier =
            Modifier.requiredSizeIn(
              minHeight = 64.dp,
              minWidth = 64.dp,
              maxHeight = 96.dp,
              maxWidth = 96.dp,
            ),
          contentAlignment = Alignment.Center,
        ) {
          AsyncImage(
            url = url,
            modifier =
              Modifier.padding(vertical = 8.dp).aspectRatio(1f).clip(RoundedCornerShape(25)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
          )
        }
      }
    }

    PostActionBar(
      modifier = Modifier.padding(end = if (showImage) 12.dp else 0.dp),
      feedName = item.feedName,
      feedIcon = item.feedIcon,
      feedHomepageLink = item.feedHomepageLink,
      showFeedFavIcon = item.showFeedFavIcon,
      postRead = readStatus,
      postRelativeTimestamp = item.date.formatRelativeTime(),
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
      showDropdown = showDropdown,
      onDropdownChange = { showDropdown = it },
      config = postMetadataConfig,
      onSourceClick = onPostSourceClick,
    )
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
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
) {
  var readStatus by remember(item.read) { mutableStateOf(item.read) }
  val alpha by
    animateFloatAsState(
      if (readStatus && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
      else Constants.ITEM_UNREAD_ALPHA
    )
  var showDropdown by remember { mutableStateOf(false) }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier =
      Modifier.then(modifier)
        .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
        .padding(compactPostListPadding)
        .graphicsLayer { this.alpha = alpha },
  ) {
    FeedIcon(
      icon = item.feedIcon,
      homepageLink = item.feedHomepageLink,
      showFeedFavIcon = item.showFeedFavIcon,
      contentDescription = null,
      shape = MaterialTheme.shapes.extraSmall,
      modifier = Modifier.requiredSize(20.dp),
    )

    Spacer(Modifier.requiredWidth(16.dp))

    Text(
      text = item.title.ifBlank { item.description },
      style = MaterialTheme.typography.titleSmall,
      color = AppTheme.colorScheme.onSurface,
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
      showDropdown = showDropdown,
      onDropdownChange = { showDropdown = it },
      onBookmarkClick = onPostBookmarkClick,
      onCommentsClick = onPostCommentsClick,
    ) {
      readStatus = !readStatus
      updatePostReadStatus(readStatus)
    }
  }
}
