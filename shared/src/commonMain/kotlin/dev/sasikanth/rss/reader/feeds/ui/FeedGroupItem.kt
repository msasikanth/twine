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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.UnreadBadge
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.resources.icons.MoreVert
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.PinFilled
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
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
  onPinClick: ((FeedGroup) -> Unit)? = null,
  modifier: Modifier = Modifier,
  dragHandle: (@Composable () -> Unit)? = null,
  interactionSource: MutableInteractionSource? = null,
) {
  val haptic = LocalHapticFeedback.current
  val translucentStyle = LocalTranslucentStyles.current
  val backgroundColor by
    animateColorAsState(
      if (selected) {
        translucentStyle.default.background
      } else {
        Color.Transparent
      }
    )

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
          },
        )
        .padding(8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      val iconSize = 16.dp
      val iconSpacing = 2.dp

      FeedGroupIconGrid(
        feedHomepageLinks = feedGroup.feedHomepageLinks,
        feedIconLinks = feedGroup.feedIconLinks,
        feedShowFavIconSettings = feedGroup.feedShowFavIconSettings,
        iconSize = iconSize,
        modifier = Modifier.requiredSize(36.dp),
      )

      Spacer(Modifier.requiredWidth(16.dp))

      Column(Modifier.weight(1f)) {
        Text(
          text = feedGroup.name,
          style = MaterialTheme.typography.titleSmall,
          color = AppTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        val text =
          if (feedGroup.feedIds.isEmpty()) {
            stringResource(Res.string.feedGroupNoFeeds)
          } else {
            pluralStringResource(
              Res.plurals.feedGroupFeeds,
              feedGroup.feedIds.size,
              feedGroup.feedIds.size,
            )
          }

        Text(
          text = text,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.requiredWidth(4.dp))

      val numberOfUnreadPosts = feedGroup.numberOfUnreadPosts
      if (canShowUnreadPostsCount && numberOfUnreadPosts > 0 && !isInMultiSelectMode) {
        UnreadBadge(feedGroup.numberOfUnreadPosts)
        Spacer(Modifier.width(16.dp))
      }

      if (isInMultiSelectMode) {
        SelectedCheckIndicator(selected = selected)
      }

      if (!isInMultiSelectMode) {
        if (onPinClick != null) {
          val pinIcon = if (feedGroup.pinnedAt != null) TwineIcons.PinFilled else TwineIcons.Pin
          IconButton(icon = pinIcon, contentDescription = null) { onPinClick(feedGroup) }
        }

        dragHandle?.invoke()
      }

      if (!isInMultiSelectMode && dragHandle == null) {
        IconButton(icon = TwineIcons.MoreVert, contentDescription = null, onClick = onOptionsClick)
      }
    }
  }
}
