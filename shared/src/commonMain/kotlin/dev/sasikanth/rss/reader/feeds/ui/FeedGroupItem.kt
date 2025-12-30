/*
 * Copyright 2024 Sasikanth Miriyampalli
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.feedGroupFeeds
import twine.shared.generated.resources.feedGroupNoFeeds

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedGroupItem(
  feedGroup: FeedGroup,
  canShowUnreadPostsCount: Boolean,
  isInMultiSelectMode: Boolean,
  selected: Boolean,
  onFeedGroupSelected: (FeedGroup) -> Unit,
  onFeedGroupClick: (FeedGroup) -> Unit,
  onOptionsClick: () -> Unit,
  modifier: Modifier = Modifier,
  dragHandle: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val haptic = LocalHapticFeedback.current
  val backgroundColor =
    if (selected) {
      AppTheme.colorScheme.primaryContainer
    } else {
      Color.Transparent
    }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .then(modifier)
        .clip(MaterialTheme.shapes.large)
        .background(backgroundColor)
        .combinedClickable(
          interactionSource = interactionSource ?: remember { MutableInteractionSource() },
          indication = LocalIndication.current,
          onClick = {
            if (isInMultiSelectMode) {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onFeedGroupSelected(feedGroup)
            } else {
              onFeedGroupClick(feedGroup)
            }
          },
          onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onFeedGroupSelected(feedGroup)
          }
        )
        .padding(8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      val showFeedFavIcon = LocalShowFeedFavIconSetting.current
      val icons = if (showFeedFavIcon) feedGroup.feedHomepageLinks else feedGroup.feedIconLinks

      val iconSize =
        if (icons.size > 2) {
          17.dp
        } else {
          19.dp
        }

      val iconSpacing =
        if (icons.size > 2) {
          2.dp
        } else {
          0.dp
        }

      FeedGroupIconGrid(
        modifier = Modifier.requiredSize(36.dp),
        icons = icons,
        iconSize = iconSize,
        iconShape = CircleShape,
        verticalArrangement = Arrangement.spacedBy(iconSpacing),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
      )

      Spacer(Modifier.requiredWidth(12.dp))

      Column(Modifier.weight(1f)) {
        Text(
          text = feedGroup.name,
          style = MaterialTheme.typography.titleSmall,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        val text =
          if (feedGroup.feedIds.isEmpty()) {
            stringResource(Res.string.feedGroupNoFeeds)
          } else {
            pluralStringResource(
              Res.plurals.feedGroupFeeds,
              feedGroup.feedIds.size,
              feedGroup.feedIds.size
            )
          }

        Text(
          text = text,
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.textEmphasisMed,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(Modifier.requiredWidth(4.dp))

      val numberOfUnreadPosts = feedGroup.numberOfUnreadPosts
      if (canShowUnreadPostsCount && numberOfUnreadPosts > 0 && !isInMultiSelectMode) {
        Badge(
          containerColor = AppTheme.colorScheme.tintedForeground,
          contentColor = AppTheme.colorScheme.tintedBackground,
          modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 16.dp)
        ) {
          Text(
            text = feedGroup.numberOfUnreadPosts.toString(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterVertically)
          )
        }
      }

      if (isInMultiSelectMode) {
        SelectedCheckIndicator(selected = selected)
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
