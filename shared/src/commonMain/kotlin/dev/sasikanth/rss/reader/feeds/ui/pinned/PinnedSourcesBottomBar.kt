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

package dev.sasikanth.rss.reader.feeds.ui.pinned

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.BOTTOM_BAR_MAX_WIDTH
import dev.sasikanth.rss.reader.utils.PINNED_SOURCES_BOTTOM_BAR_HEIGHT
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PinnedSourcesBottomBar(
  pinnedSources: List<Source>,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  onPinnedSourceOrderChanged: (List<Source>) -> Unit,
  modifier: Modifier = Modifier,
  scrollBehavior: PinnedSourcesBottomBarScrollBehavior? = null,
) {
  val shape = CircleShape

  val translationY = scrollBehavior?.state?.heightOffset ?: 0f
  val lazyListState = rememberLazyListState()
  val reorderableLazyRowState =
    rememberReorderableLazyListState(
      lazyListState = lazyListState,
      onMove = { from, to ->
        onPinnedSourceOrderChanged(
          pinnedSources.toMutableList().apply { add(to.index, removeAt(from.index)) }
        )
      },
    )

  LazyRow(
    state = lazyListState,
    modifier =
      Modifier.onGloballyPositioned { coordinates ->
          val height = coordinates.size.height.toFloat()
          if (scrollBehavior != null && scrollBehavior.state.heightOffsetLimit != -height) {
            scrollBehavior.state.heightOffsetLimit = -height
          }
        }
        .graphicsLayer { this.translationY = -translationY }
        .then(modifier)
        .navigationBarsPadding()
        .widthIn(max = BOTTOM_BAR_MAX_WIDTH)
        .padding(horizontal = 32.dp)
        .shadow(elevation = 4.dp, shape = shape)
        .clip(shape)
        .background(AppTheme.colorScheme.bottomSheet)
        .border(width = 1.dp, color = AppTheme.colorScheme.bottomSheetBorder, shape = shape)
        .fillMaxWidth()
        .height(PINNED_SOURCES_BOTTOM_BAR_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
    contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
  ) {
    items(count = pinnedSources.size, key = { pinnedSources[it].id }) { index ->
      val source = pinnedSources[index]
      ReorderableItem(state = reorderableLazyRowState, key = source.id) { isDragging ->
        val haptic = LocalHapticFeedback.current

        SourceItem(
          source = source,
          activeSource = activeSource,
          canShowUnreadPostsCount = canShowUnreadPostsCount,
          onHomeSelected = onHomeSelected,
          onSourceClick = onSourceClick,
          isDragging = isDragging,
          modifier =
            Modifier.longPressDraggableHandle(
              onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
            ),
        )
      }
    }
  }
}

@Composable
private fun SourceItem(
  source: Source,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onHomeSelected: () -> Unit,
  onSourceClick: (Source) -> Unit,
  isDragging: Boolean,
  modifier: Modifier = Modifier,
) {
  when (val source = source) {
    is FeedGroup -> {
      PinnedFeedGroupItem(
        feedGroup = source,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        isDragging = isDragging,
        modifier = modifier,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
    is Feed -> {
      PinnedFeedItem(
        badgeCount = source.numberOfUnreadPosts,
        homePageUrl = source.homepageLink,
        feedIconUrl = source.icon,
        showFeedFavIcon = source.showFeedFavIcon,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        isDragging = isDragging,
        modifier = modifier,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
  }
}
