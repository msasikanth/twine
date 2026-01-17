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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.relativeDurationString
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

private val featuredItemPadding
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Expanded -> PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(0.dp)
    }

@Immutable data class FeaturedPostItem(val postWithMetadata: PostWithMetadata, val seedColor: Int?)

@Composable
internal fun FeaturedPostItem(
  item: PostWithMetadata,
  onClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onSourceClick: () -> Unit,
  updateReadStatus: (updatedReadStatus: Boolean) -> Unit,
  modifier: Modifier = Modifier,
  featuredImage: @Composable () -> Unit,
) {
  var readStatus by remember(item.read) { mutableStateOf(item.read) }
  val alpha by
    animateFloatAsState(if (readStatus) Constants.ITEM_READ_ALPHA else Constants.ITEM_UNREAD_ALPHA)
  var showDropdown by remember { mutableStateOf(false) }

  Column(
    modifier =
      Modifier.then(modifier)
        .padding(featuredItemPadding)
        .clip(MaterialTheme.shapes.extraLarge)
        .combinedClickable(
          onClick = onClick,
          onLongClick = { showDropdown = true },
        )
        .graphicsLayer { this.alpha = alpha }
  ) {
    val density = LocalDensity.current
    val titleTextStyle = MaterialTheme.typography.headlineMedium
    val titleMaxLines = 3
    var descriptionBottomPadding by remember(item.link) { mutableStateOf(0.dp) }

    featuredImage()

    Spacer(modifier = Modifier.requiredHeight(8.dp))

    val isDarkTheme = AppTheme.isDark
    Text(
      modifier =
        Modifier.padding(all = 8.dp).graphicsLayer {
          blendMode =
            if (isDarkTheme) {
              BlendMode.Screen
            } else {
              BlendMode.Multiply
            }
        },
      text = item.title.ifBlank { item.description },
      style = titleTextStyle,
      fontWeight = FontWeight.Bold,
      color = AppTheme.colorScheme.secondary,
      maxLines = titleMaxLines,
      overflow = TextOverflow.Ellipsis,
      onTextLayout = { textLayoutResult ->
        val numberOfLines = textLayoutResult.lineCount
        if (numberOfLines < titleMaxLines) {
          val lineHeight = with(density) { titleTextStyle.lineHeight.toDp() }
          descriptionBottomPadding = lineHeight * (titleMaxLines - numberOfLines)
        }
      }
    )

    Text(
      modifier = Modifier.padding(horizontal = 8.dp),
      text = item.description,
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.outline,
      minLines = 3,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(Modifier.requiredHeight(descriptionBottomPadding + 4.dp))

    PostActionBar(
      feedName = item.feedName,
      feedIcon = item.feedIcon,
      feedHomepageLink = item.feedHomepageLink,
      showFeedFavIcon = item.showFeedFavIcon,
      postRead = readStatus,
      postRelativeTimestamp = item.date.relativeDurationString(),
      postLink = item.link,
      postBookmarked = item.bookmarked,
      commentsLink = item.commentsLink,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      onTogglePostReadClick = {
        readStatus = !readStatus
        updateReadStatus(readStatus)
      },
      showDropdown = showDropdown,
      onDropdownChange = { showDropdown = it },
      onSourceClick = onSourceClick,
    )

    Spacer(modifier = Modifier.height(8.dp))
  }
}
