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
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.resources.icons.RadioSelected
import dev.sasikanth.rss.reader.resources.icons.RadioUnselected
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeedGroupItem(
  feedGroup: FeedGroup,
  isInMultiSelectMode: Boolean,
  selected: Boolean,
  onFeedGroupSelected: (FeedGroup) -> Unit,
  onFeedGroupClick: (FeedGroup) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val backgroundColor =
    if (selected) {
      AppTheme.colorScheme.tintedHighlight
    } else {
      AppTheme.colorScheme.tintedSurface
    }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .then(modifier)
        .clip(MaterialTheme.shapes.large)
        .background(backgroundColor)
        .combinedClickable(
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
      val iconSize =
        if (feedGroup.feedIcons.size > 2) {
          17.dp
        } else {
          19.dp
        }

      val iconSpacing =
        if (feedGroup.feedIcons.size > 2) {
          2.dp
        } else {
          0.dp
        }

      FeedGroupIconGrid(
        modifier = Modifier.requiredSize(36.dp),
        icons = feedGroup.feedIcons,
        iconSize = iconSize,
        iconShape = CircleShape,
        verticalArrangement = Arrangement.spacedBy(iconSpacing),
        horizontalArrangement = Arrangement.spacedBy(iconSpacing),
      )

      Spacer(Modifier.requiredWidth(12.dp))

      Column(Modifier.weight(1f)) {
        Text(
          text = feedGroup.name,
          style = MaterialTheme.typography.labelMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        val text =
          if (feedGroup.feedIds.isEmpty()) {
            LocalStrings.current.feedGroupNoFeeds
          } else {
            LocalStrings.current.feedGroupFeeds(feedGroup.feedIds.size)
          }

        Text(
          text = text,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.textEmphasisMed,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(Modifier.requiredWidth(4.dp))

      if (isInMultiSelectMode) {
        val icon =
          if (selected) {
            TwineIcons.RadioSelected
          } else {
            TwineIcons.RadioUnselected
          }

        val tint =
          if (selected) {
            AppTheme.colorScheme.tintedForeground
          } else {
            AppTheme.colorScheme.onSurface
          }

        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = tint,
          modifier = Modifier.requiredSize(24.dp),
        )
      }
    }
  }
}
