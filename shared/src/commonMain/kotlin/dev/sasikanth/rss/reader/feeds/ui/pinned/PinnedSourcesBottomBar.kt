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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.BOTTOM_BAR_MAX_WIDTH
import dev.sasikanth.rss.reader.utils.PINNED_SOURCES_BOTTOM_BAR_HEIGHT
import kotlinx.collections.immutable.ImmutableList
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun NowPlayingBottomBar(
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  val shape = CircleShape
  val colorScheme = AppTheme.colorScheme

  Row(
    modifier =
      modifier
        .navigationBarsPadding()
        .widthIn(max = BOTTOM_BAR_MAX_WIDTH)
        .padding(horizontal = 32.dp)
        .shadow(elevation = 4.dp, shape = shape)
        .clip(shape)
        .drawWithCache {
          val outline = shape.createOutline(size, layoutDirection, this)
          val borderStroke = Stroke(width = 1.dp.toPx())
          onDrawWithContent {
            drawRect(colorScheme.bottomSheet)
            drawContent()
            drawOutline(
              outline = outline,
              color = colorScheme.bottomSheetBorder,
              style = borderStroke,
            )
          }
        }
        .fillMaxWidth()
        .height(PINNED_SOURCES_BOTTOM_BAR_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
    content = content,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PinnedSourcesBottomBar(
  pinnedSources: ImmutableList<Source>,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  onPinnedSourceOrderChanged: (List<Source>) -> Unit,
  modifier: Modifier = Modifier,
  scrollBehavior: PinnedSourcesBottomBarScrollBehavior? = null,
  nowPlayingChip: (@Composable RowScope.() -> Unit)? = null,
) {
  val shape = CircleShape
  val colorScheme = AppTheme.colorScheme
  val haptic = LocalHapticFeedback.current

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

  val startFadeState =
    animateFloatAsState(
      targetValue = if (lazyListState.canScrollBackward) 1f else 0f,
      animationSpec = tween(durationMillis = 200),
    )
  val endFadeState =
    animateFloatAsState(
      targetValue = if (lazyListState.canScrollForward) 1f else 0f,
      animationSpec = tween(durationMillis = 200),
    )

  Row(
    modifier =
      Modifier.onSizeChanged { size ->
          val height = size.height.toFloat()
          if (scrollBehavior != null && scrollBehavior.state.heightOffsetLimit != -height) {
            scrollBehavior.state.heightOffsetLimit = -height
          }
        }
        .graphicsLayer { translationY = -(scrollBehavior?.state?.heightOffset ?: 0f) }
        .then(modifier)
        .navigationBarsPadding()
        .widthIn(max = BOTTOM_BAR_MAX_WIDTH)
        .padding(horizontal = 32.dp)
        .shadow(elevation = 4.dp, shape = shape)
        .clip(shape)
        .drawWithCache {
          val outline = shape.createOutline(size, layoutDirection, this)
          val borderStroke = Stroke(width = 1.dp.toPx())
          onDrawWithContent {
            drawRect(colorScheme.bottomSheet)
            drawContent()
            drawOutline(
              outline = outline,
              color = colorScheme.bottomSheetBorder,
              style = borderStroke,
            )
          }
        }
        .fillMaxWidth()
        .height(PINNED_SOURCES_BOTTOM_BAR_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    nowPlayingChip?.invoke(this)

    LazyRow(
      state = lazyListState,
      modifier =
        Modifier.weight(1f).fillMaxHeight().drawWithCache {
          val fadeWidth = BOTTOM_BAR_FADE_WIDTH.toPx()
          val fadeSize = size.copy(width = fadeWidth)
          val startMask =
            Brush.horizontalGradient(colorStops = StartFadeStops, startX = 0f, endX = fadeWidth)
          val endMask =
            Brush.horizontalGradient(
              colorStops = EndFadeStops,
              startX = size.width - fadeWidth,
              endX = size.width,
            )
          val endTopLeft = Offset(size.width - fadeWidth, 0f)
          var lastBottomSheet = Color.Unspecified
          var cachedFadeFilter: ColorFilter? = null

          onDrawWithContent {
            drawContent()

            val start = startFadeState.value
            val end = endFadeState.value
            if (start > 0f || end > 0f) {
              val bottomSheet = colorScheme.bottomSheet
              val fadeFilter =
                if (bottomSheet == lastBottomSheet) {
                  cachedFadeFilter
                } else {
                  lastBottomSheet = bottomSheet
                  ColorFilter.tint(bottomSheet).also { cachedFadeFilter = it }
                }

              if (start > 0f) {
                drawRect(
                  brush = startMask,
                  size = fadeSize,
                  alpha = start,
                  colorFilter = fadeFilter,
                )
              }

              if (end > 0f) {
                drawRect(
                  brush = endMask,
                  topLeft = endTopLeft,
                  size = fadeSize,
                  alpha = end,
                  colorFilter = fadeFilter,
                )
              }
            }
          }
        },
      verticalAlignment = Alignment.CenterVertically,
      contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
    ) {
      items(
        count = pinnedSources.size,
        key = { pinnedSources[it].id },
        contentType = { pinnedSources[it].sourceType },
      ) { index ->
        val source = pinnedSources[index]
        ReorderableItem(state = reorderableLazyRowState, key = source.id) { isDragging ->
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

private val BOTTOM_BAR_FADE_WIDTH = 40.dp
private val FadeAlphas = floatArrayOf(1f, 0.94f, 0.78f, 0.5f, 0.22f, 0.06f, 0f)
private val StartFadeStops = fadeStops(reversed = false)
private val EndFadeStops = fadeStops(reversed = true)

private fun fadeStops(reversed: Boolean): Array<Pair<Float, Color>> {
  return Array(FadeAlphas.size) { index ->
    val position = index / (FadeAlphas.size - 1).toFloat()
    val alpha = if (reversed) FadeAlphas[FadeAlphas.size - 1 - index] else FadeAlphas[index]
    position to Color.White.copy(alpha = alpha)
  }
}
