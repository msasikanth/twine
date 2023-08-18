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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.sasikanth.rss.reader.CommonRes
import dev.sasikanth.rss.reader.components.AsyncImage
import dev.sasikanth.rss.reader.components.DropdownMenuShareItem
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.pressInteraction
import dev.sasikanth.rss.reader.utils.toDp

@Composable
internal fun FeedListItem(
  modifier: Modifier = Modifier,
  feed: Feed,
  selected: Boolean,
  canShowDivider: Boolean,
  onDeleteFeed: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit
) {
  val hapticFeedback = LocalHapticFeedback.current
  var dropdownMenuExpanded by remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val interactionSource = remember { MutableInteractionSource() }
  var dropdownOffset by remember(feed) { mutableStateOf(Offset.Zero) }

  Box(
    modifier =
      modifier
        .indication(interactionSource, LocalIndication.current)
        .pointerInput(Unit) {
          detectTapGestures(
            onTap = {
              pressInteraction(
                coroutineScope = coroutineScope,
                interactionSource = interactionSource,
                offset = it,
              ) {
                onFeedSelected(feed)
              }
            },
            onLongPress = {
              pressInteraction(
                coroutineScope = coroutineScope,
                interactionSource = interactionSource,
                offset = it,
              ) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                dropdownOffset = it
                dropdownMenuExpanded = true
              }
            }
          )
        }
        .fillMaxWidth()
        .padding(start = 20.dp, end = 12.dp)
  ) {
    Row(
      modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(contentAlignment = Alignment.Center) {
        SelectionIndicator(selected = selected, animationProgress = 1f)
        Box(
          modifier =
            Modifier.requiredSize(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          AsyncImage(
            url = feed.icon,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.requiredSize(48.dp).clip(RoundedCornerShape(16.dp)).align(Alignment.Center),
          )
        }
      }
      Spacer(Modifier.requiredWidth(16.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        maxLines = 1,
        color = AppTheme.colorScheme.textEmphasisHigh,
        style = MaterialTheme.typography.titleMedium,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(Modifier.requiredWidth(16.dp))
      IconButton(onClick = { onDeleteFeed(feed) }) {
        Icon(
          painter = painterResource(CommonRes.images.ic_delete),
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    }

    if (canShowDivider) {
      Divider(
        modifier = Modifier.requiredHeight(1.dp).align(Alignment.BottomStart).padding(end = 12.dp),
        color = AppTheme.colorScheme.tintedSurface
      )
    }

    Box {
      DropdownMenu(
        expanded = dropdownMenuExpanded,
        onDismissRequest = { dropdownMenuExpanded = false },
        offset = DpOffset(dropdownOffset.x.toDp(), dropdownOffset.y.toDp())
      ) {
        DropdownMenuShareItem(
          contentToShare = feed.link,
          onShareMenuOpened = { dropdownMenuExpanded = false }
        )
      }
    }
  }
}
