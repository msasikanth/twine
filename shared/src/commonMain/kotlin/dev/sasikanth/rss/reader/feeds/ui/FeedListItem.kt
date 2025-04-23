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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.size.Size
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.resources.icons.Overflow
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.PinFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting

@Composable
internal fun FeedListItem(
  feed: Feed,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  isFeedSelected: Boolean,
  onFeedClick: () -> Unit,
  onFeedSelected: () -> Unit,
  toggleFeedPin: () -> Unit,
  modifier: Modifier = Modifier,
  dragHandle: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val haptic = LocalHapticFeedback.current
  val selectedBorderColor =
    if (isFeedSelected) {
      AppTheme.colorScheme.primary
    } else {
      Color.Transparent
    }

  Row(
    modifier =
      Modifier.then(modifier)
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.large)
        .border(2.dp, selectedBorderColor, MaterialTheme.shapes.large)
        .combinedClickable(
          interactionSource = interactionSource ?: remember { MutableInteractionSource() },
          indication = LocalIndication.current,
          onClick = {
            if (isInMultiSelectMode) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onFeedSelected()
            } else {
              onFeedClick()
            }
          },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFeedSelected()
          }
        )
        .padding(start = 20.dp, end = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val showFeedFavIcon = LocalShowFeedFavIconSetting.current
    val icon = if (showFeedFavIcon) feed.homepageLink else feed.icon
    val feedIconShape = RoundedCornerShape(6.dp)

    FeedIcon(
      modifier =
        Modifier.requiredSize(24.dp)
          .border(1.dp, AppTheme.colorScheme.outlineVariant, feedIconShape)
          .clip(feedIconShape),
      url = icon,
      contentDescription = null,
      size = Size(56, 56),
    )

    Spacer(Modifier.requiredWidth(12.dp))

    Text(
      modifier = Modifier.weight(1f),
      text = feed.name,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = MaterialTheme.typography.bodyMedium,
      color = AppTheme.colorScheme.onSurface,
    )

    Row(modifier = Modifier.padding(vertical = 4.dp)) {
      val pinIcon =
        if (feed.pinnedAt != null) {
          TwineIcons.PinFilled
        } else {
          TwineIcons.Pin
        }
      IconButton(icon = pinIcon, onClick = { toggleFeedPin() })

      IconButton(
        icon = TwineIcons.Overflow,
        onClick = {
          // TODO: Open feed menu
        }
      )
    }
  }
}

@Composable
private fun IconButton(
  icon: ImageVector,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(40.dp).clip(MaterialTheme.shapes.small).clickable { onClick() },
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      modifier = Modifier.size(20.dp),
      imageVector = icon,
      contentDescription = null,
      tint = AppTheme.colorScheme.onSurfaceVariant,
    )
  }
}
