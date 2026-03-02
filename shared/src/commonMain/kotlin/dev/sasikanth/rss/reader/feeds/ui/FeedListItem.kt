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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.UnreadBadge
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.PinFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedListItem(
  feed: Feed,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  isFeedSelected: Boolean,
  onFeedClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onOptionsClick: () -> Unit,
  onPinClick: ((Feed) -> Unit)? = null,
  modifier: Modifier = Modifier,
  dragHandle: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val haptic = LocalHapticFeedback.current
  val backgroundColor by
    animateColorAsState(
      if (isFeedSelected) {
        AppTheme.colorScheme.primaryContainer
      } else {
        Color.Transparent
      }
    )
  val translucentStyle = LocalTranslucentStyles.current

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .then(modifier)
        .clip(RoundedCornerShape(16.dp))
        .background(backgroundColor)
        .combinedClickable(
          interactionSource = interactionSource ?: remember { MutableInteractionSource() },
          indication = LocalIndication.current,
          onClick = {
            if (isInMultiSelectMode) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onFeedSelected(feed)
            } else {
              onFeedClick(feed)
            }
          },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFeedSelected(feed)
          },
        )
  ) {
    Row(
      modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      FeedIcon(
        icon = feed.icon,
        homepageLink = feed.homepageLink,
        showFeedFavIcon = feed.showFeedFavIcon,
        contentDescription = null,
        modifier = Modifier.requiredSize(24.dp),
        contentScale = ContentScale.Crop,
      )

      Spacer(Modifier.requiredWidth(12.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Clip,
      )

      Spacer(Modifier.requiredWidth(12.dp))

      val numberOfUnreadPosts = feed.numberOfUnreadPosts
      if (canShowUnreadPostsCount && numberOfUnreadPosts > 0 && !isInMultiSelectMode) {
        UnreadBadge(numberOfUnreadPosts)

        Spacer(Modifier.width(16.dp))
      }

      if (isInMultiSelectMode) {
        SelectedCheckIndicator(selected = isFeedSelected)
      }

      if (!isInMultiSelectMode) {
        if (onPinClick != null) {
          val pinIcon = if (feed.pinnedAt != null) TwineIcons.PinFilled else TwineIcons.Pin
          IconButton(modifier = Modifier.requiredSize(40.dp), onClick = { onPinClick(feed) }) {
            Icon(
              modifier = Modifier.requiredSize(20.dp),
              imageVector = pinIcon,
              contentDescription = null,
              tint = AppTheme.colorScheme.secondary,
            )
          }
        }

        dragHandle?.invoke()
      }

      if (!isInMultiSelectMode && dragHandle == null) {
        IconButton(modifier = Modifier.requiredSize(40.dp), onClick = onOptionsClick) {
          Icon(
            modifier = Modifier.requiredSize(20.dp),
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null,
            tint = AppTheme.colorScheme.secondary,
          )
        }
      }
    }
  }
}
