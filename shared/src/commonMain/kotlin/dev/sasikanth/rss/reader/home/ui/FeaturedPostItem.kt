/*
 * Copyright 2023 Sasikanth Miriyampalli
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
@file:OptIn(ExperimentalFoundationApi::class)

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.relativeDurationString
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage

private val featuredImageAspectRatio: Float
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.77f
      WindowWidthSizeClass.Medium -> 2.5f
      WindowWidthSizeClass.Expanded -> 1.9f
      else -> 1.77f
    }

@Immutable data class FeaturedPostItem(val postWithMetadata: PostWithMetadata, val seedColor: Int?)

@Composable
internal fun FeaturedPostItem(
  item: PostWithMetadata,
  page: Int,
  pagerState: PagerState,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onSourceClick: () -> Unit,
  onTogglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      Modifier.then(modifier)
        .clip(MaterialTheme.shapes.extraLarge)
        .clickable(onClick = onClick)
        .alpha(if (item.read) Constants.ITEM_READ_ALPHA else Constants.ITEM_UNREAD_ALPHA)
  ) {
    val density = LocalDensity.current
    var descriptionBottomPadding by remember(item.link) { mutableStateOf(0.dp) }

    AsyncImage(
      url = item.imageUrl!!,
      modifier =
        Modifier.clip(MaterialTheme.shapes.extraLarge)
          .aspectRatio(featuredImageAspectRatio)
          .background(AppTheme.colorScheme.surfaceContainerLowest)
          .graphicsLayer {
            translationX =
              if (page in 0..pagerState.pageCount) {
                pagerState.getOffsetFractionForPage(page) * 250f
              } else {
                0f
              }
            scaleX = 1.08f
            scaleY = 1.08f
          },
      contentDescription = null,
      contentScale = ContentScale.Crop,
    )

    Spacer(modifier = Modifier.requiredHeight(8.dp))

    Text(
      modifier = Modifier.padding(horizontal = 16.dp),
      text = item.title.ifBlank { item.description },
      style = MaterialTheme.typography.titleLarge,
      color = AppTheme.colorScheme.textEmphasisHigh,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
      onTextLayout = { textLayoutResult ->
        val numberOfLines = textLayoutResult.lineCount
        if (numberOfLines < 3) {
          val lineTop = textLayoutResult.getLineTop(0)
          val lineBottom = textLayoutResult.getLineBottom(0)
          val lineHeight = with(density) { (lineTop + lineBottom).toDp() }

          descriptionBottomPadding = lineHeight * (3 - numberOfLines)
        }
      }
    )

    if (item.title.isNotBlank() && item.description.isNotBlank()) {
      Spacer(modifier = Modifier.requiredHeight(8.dp))

      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = item.description,
        style = MaterialTheme.typography.bodySmall,
        color = AppTheme.colorScheme.textEmphasisHigh,
        minLines = 3,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
      )

      Spacer(Modifier.requiredHeight(descriptionBottomPadding))
    }

    PostMetadata(
      feedName = item.feedName,
      postPublishedAt = item.date.relativeDurationString(),
      postLink = item.link,
      postRead = item.read,
      postBookmarked = item.bookmarked,
      commentsLink = item.commentsLink,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      onSourceClick = onSourceClick,
      onTogglePostReadClick = onTogglePostReadClick,
      modifier = Modifier.padding(start = 16.dp, end = 0.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}
