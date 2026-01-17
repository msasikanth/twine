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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
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
  modifier: Modifier = Modifier,
  dragHandle: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val haptic = LocalHapticFeedback.current
  val backgroundColor =
    if (isFeedSelected) {
      AppTheme.colorScheme.primaryContainer
    } else {
      Color.Transparent
    }
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
          }
        )
  ) {
    Row(modifier = Modifier.padding(all = 8.dp), verticalAlignment = Alignment.CenterVertically) {
      FeedIcon(
        icon = feed.icon,
        homepageLink = feed.homepageLink,
        showFeedFavIcon = feed.showFeedFavIcon,
        contentDescription = null,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.requiredSize(36.dp),
        contentScale = ContentScale.Crop,
      )

      Spacer(Modifier.requiredWidth(12.dp))

      Text(
        modifier = Modifier.weight(1f),
        text = feed.name,
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 1,
        overflow = TextOverflow.Clip
      )

      Spacer(Modifier.requiredWidth(12.dp))

      val numberOfUnreadPosts = feed.numberOfUnreadPosts
      if (canShowUnreadPostsCount && numberOfUnreadPosts > 0 && !isInMultiSelectMode) {
        Badge(
          containerColor = translucentStyle.prominent.background,
          contentColor = AppTheme.colorScheme.secondary,
          modifier = Modifier.sizeIn(minWidth = 36.dp, minHeight = 24.dp)
        ) {
          Text(
            text = feed.numberOfUnreadPosts.toString(),
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.align(Alignment.CenterVertically)
          )
        }
      }

      if (isInMultiSelectMode) {
        SelectedCheckIndicator(selected = isFeedSelected)
      }

      if (!isInMultiSelectMode) {
        dragHandle?.invoke()
      }

      if (!isInMultiSelectMode && dragHandle == null) {
        IconButton(
          modifier = Modifier.requiredSize(40.dp),
          onClick = onOptionsClick,
        ) {
          Icon(
            modifier = Modifier.requiredSize(20.dp),
            imageVector = Icons.Filled.MoreVert,
            contentDescription = null,
            tint = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}
