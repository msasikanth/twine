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

package dev.sasikanth.rss.reader.feeds.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.resources.icons.Remove
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val SelectedPinnedSourceShape = RoundedCornerShape(16.dp)
private val PinnedSourceIconShape = RoundedCornerShape(25)

internal fun LazyListScope.pinnedSources(
  pinnedSources: List<Source>,
  activeSource: Source?,
  onSourceClick: (Source) -> Unit,
  onPinClick: (Source) -> Unit,
  onPinnedSourceOrderChanged: (List<Source>) -> Unit,
) {
  if (pinnedSources.isNotEmpty()) {
    item(key = "PinnedSourcesRow", contentType = "PinnedSourcesRow") {
      val lazyListState = rememberLazyListState()
      val haptic = LocalHapticFeedback.current
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
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 16.dp).animateItem(),
      ) {
        items(
          items = pinnedSources,
          key = { "PinnedSource: ${it.id}" },
          contentType = { it.sourceType },
        ) { source ->
          ReorderableItem(state = reorderableLazyRowState, key = "PinnedSource: ${source.id}") {
            isDragging ->
            val interactionSource = remember { MutableInteractionSource() }

            PinnedSourceItem(
              source = source,
              selected = activeSource?.id == source.id,
              onSourceClick = onSourceClick,
              onRemoveClick = onPinClick,
              isDragging = isDragging,
              modifier =
                Modifier.longPressDraggableHandle(
                  onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                  interactionSource = interactionSource,
                ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PinnedSourceItem(
  source: Source,
  selected: Boolean,
  onSourceClick: (Source) -> Unit,
  onRemoveClick: (Source) -> Unit,
  isDragging: Boolean,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val targetSelectionAlpha = if (selected) 1f else 0f
  val initialSelectionAlpha = remember { targetSelectionAlpha }
  var hasSelectionTransitioned by remember { mutableStateOf(false) }
  val selectionAlpha = remember { Animatable(initialSelectionAlpha) }
  if (hasSelectionTransitioned || targetSelectionAlpha != initialSelectionAlpha) {
    LaunchedEffect(targetSelectionAlpha) {
      hasSelectionTransitioned = true
      selectionAlpha.animateTo(targetSelectionAlpha)
    }
  }

  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(modifier = modifier.requiredSize(64.dp), contentAlignment = Alignment.Center) {
      Box(
        modifier =
          Modifier.requiredSize(56.dp).drawWithCache {
            val outline = SelectedPinnedSourceShape.createOutline(size, layoutDirection, this)
            onDrawBehind {
              val alpha = selectionAlpha.value
              if (alpha > 0f) {
                drawOutline(outline, colorScheme.primaryContainer.copy(alpha = alpha))
              }
            }
          }
      )

      Box(
        modifier =
          Modifier.requiredSize(48.dp)
            .drawWithCache {
              val outline = PinnedSourceIconShape.createOutline(size, layoutDirection, this)
              val strokeWidth = 1.dp.toPx()
              val halfStroke = strokeWidth / 2f
              val borderStroke = Stroke(width = strokeWidth)
              val borderTopLeft = Offset(halfStroke, halfStroke)
              val borderSize = Size(size.width - strokeWidth, size.height - strokeWidth)
              val radius = (minOf(size.width, size.height) * 0.25f - halfStroke).coerceAtLeast(0f)
              val borderCornerRadius = CornerRadius(radius, radius)
              onDrawWithContent {
                drawOutline(outline, colorScheme.backdrop)
                drawContent()
                drawRoundRect(
                  color = colorScheme.outlineVariant,
                  topLeft = borderTopLeft,
                  size = borderSize,
                  cornerRadius = borderCornerRadius,
                  style = borderStroke,
                )
              }
            }
            .clip(PinnedSourceIconShape)
            .clickable(enabled = !isDragging) { onSourceClick(source) },
        contentAlignment = Alignment.Center,
      ) {
        when (source) {
          is Feed -> {
            FeedIcon(
              icon = source.icon,
              homepageLink = source.homepageLink,
              showFeedFavIcon = source.showFeedFavIcon,
              contentDescription = null,
              modifier = Modifier.requiredSize(48.dp),
            )
          }
          is FeedGroup -> {
            FeedGroupIconGrid(
              feedHomepageLinks = source.feedHomepageLinks,
              feedIconLinks = source.feedIconLinks,
              feedShowFavIconSettings = source.feedShowFavIconSettings,
              modifier = Modifier.requiredSize(48.dp),
            )
          }
        }
      }

      if (!isDragging) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .requiredSize(20.dp)
              .drawBehind {
                drawCircle(color = colorScheme.backdrop, radius = 11.dp.toPx())
                drawCircle(color = colorScheme.inverseSurface)
              }
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onRemoveClick(source) },
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = TwineIcons.Remove,
            contentDescription = null,
            tint = colorScheme.settledInverseOnSurface,
            modifier = Modifier.padding(4.dp),
          )
        }
      }
    }

    Text(
      modifier = Modifier.widthIn(max = 64.dp),
      text = source.name,
      style = MaterialTheme.typography.labelMedium,
      color = { colorScheme.onSurfaceVariant },
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
