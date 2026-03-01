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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.ui.AppTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

internal fun LazyListScope.pinnedSources(
  pinnedSources: List<Source>,
  onSourceClick: (Source) -> Unit,
  onPinClick: (Source) -> Unit,
  onPinnedSourceOrderChanged: (List<Source>) -> Unit,
) {
  if (pinnedSources.isNotEmpty()) {
    item(key = "PinnedSourcesRow") {
      val lazyListState = androidx.compose.foundation.lazy.rememberLazyListState()
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
        items(items = pinnedSources, key = { "PinnedSource: ${it.id}" }) { source ->
          ReorderableItem(state = reorderableLazyRowState, key = "PinnedSource: ${source.id}") {
            isDragging ->
            val haptic = LocalHapticFeedback.current
            val interactionSource = remember { MutableInteractionSource() }

            PinnedSourceItem(
              source = source,
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
  onSourceClick: (Source) -> Unit,
  onRemoveClick: (Source) -> Unit,
  isDragging: Boolean,
  modifier: Modifier = Modifier,
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier =
        modifier
          .requiredSize(64.dp)
          .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen),
      contentAlignment = Alignment.Center,
    ) {
      val shape = RoundedCornerShape(16.dp)
      val borderColor = AppTheme.colorScheme.outlineVariant

      Box(
        modifier =
          Modifier.clip(shape)
            .background(AppTheme.colorScheme.backdrop)
            .border(1.dp, borderColor, shape)
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
        Surface(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .requiredSize(20.dp)
              .dropShadow(shape) {
                spread = 1.dp.toPx()
                color = Color.Black
                blendMode = BlendMode.DstOut
              }
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onRemoveClick(source) },
              ),
          shape = CircleShape,
          color = AppTheme.colorScheme.inverseSurface,
          contentColor = AppTheme.colorScheme.inverseOnSurface,
        ) {
          Icon(
            imageVector = Icons.Rounded.Remove,
            contentDescription = null,
            modifier = Modifier.padding(4.dp),
          )
        }
      }
    }

    Text(
      modifier = Modifier.widthIn(max = 64.dp),
      text = source.name,
      style = MaterialTheme.typography.labelMedium,
      color = AppTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )
  }
}
