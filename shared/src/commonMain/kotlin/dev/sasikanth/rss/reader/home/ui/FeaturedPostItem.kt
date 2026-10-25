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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import kotlin.math.roundToInt

private val featuredItemPadding: PaddingValues
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

/**
 * The title reserves three lines worth of height so every page of the pager is the same height. The
 * line count is only known once the title is laid out, so it is kept off the snapshot system and
 * read back in the filler's own measure pass; writing it to state would make the Layout phase
 * invalidate a Composition read and compose every page twice.
 */
private class TitleFiller {
  var heightPx = 0f
}

@Composable
internal fun FeaturedPostItem(
  item: ResolvedPost,
  contentAlphaProvider: () -> Float,
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
        .combinedClickable(onClick = onClick, onLongClick = { showDropdown = true })
        .graphicsLayer { this.alpha = alpha * contentAlphaProvider.invoke() }
  ) {
    val density = LocalDensity.current
    val titleTextStyle = MaterialTheme.typography.headlineMedium
    val titleMaxLines = 3
    val titleFiller = remember(item.link) { TitleFiller() }

    featuredImage()

    Spacer(Modifier.height(16.dp))

    val isDarkTheme = AppTheme.isDark
    Text(
      modifier =
        Modifier.graphicsLayer {
          blendMode =
            if (isDarkTheme) {
              BlendMode.Screen
            } else {
              BlendMode.Multiply
            }
        },
      text = item.title.ifBlank { item.description },
      style = titleTextStyle,
      color = AppTheme.colorScheme.onSurfaceVariant,
      maxLines = titleMaxLines,
      overflow = TextOverflow.Ellipsis,
      onTextLayout = { textLayoutResult ->
        val numberOfLines = textLayoutResult.lineCount
        titleFiller.heightPx =
          if (numberOfLines < titleMaxLines) {
            with(density) { titleTextStyle.lineHeight.toPx() } * (titleMaxLines - numberOfLines)
          } else {
            0f
          }
      },
    )

    Spacer(Modifier.height(8.dp))

    Text(
      text = item.description,
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.outline,
      minLines = 3,
      maxLines = 3,
      overflow = TextOverflow.Ellipsis,
    )

    Spacer(
      Modifier.layout { measurable, _ ->
        val height = titleFiller.heightPx.roundToInt()
        val placeable = measurable.measure(Constraints.fixed(width = 0, height = height))
        layout(0, height) { placeable.place(0, 0) }
      }
    )

    Spacer(modifier = Modifier.requiredHeight(4.dp))

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

    Spacer(Modifier.requiredHeight(16.dp))
  }
}
