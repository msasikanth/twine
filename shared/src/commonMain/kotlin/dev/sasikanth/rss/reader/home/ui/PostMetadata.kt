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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun PostMetadata(
  feedName: String,
  postRead: Boolean,
  postPublishedAt: String,
  postLink: String,
  postBookmarked: Boolean,
  commentsLink: String?,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onTogglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
  enablePostSource: Boolean,
  onSourceClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier.padding(
          top = 8.dp,
          bottom = 8.dp,
        )
        .then(modifier),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(modifier = Modifier.weight(1f)) {
      val postSourceClickableModifier =
        if (enablePostSource) {
          Modifier.clip(RoundedCornerShape(50))
            .clickable(onClick = onSourceClick)
            .background(color = Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
        } else {
          Modifier
        }

      val postSourceTextColor =
        if (enablePostSource) {
          Color.White
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        }

      Row(modifier = postSourceClickableModifier) {
        if (!postRead) {
          Box(
            Modifier.requiredSize(6.dp)
              .background(AppTheme.colorScheme.tintedForeground, CircleShape)
              .align(Alignment.CenterVertically)
          )
          Spacer(Modifier.requiredWidth(4.dp))
        }

        Text(
          style = MaterialTheme.typography.bodySmall,
          maxLines = 1,
          text = feedName.capitalize(Locale.current),
          color = postSourceTextColor,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    Text(
      modifier = Modifier.padding(horizontal = 8.dp),
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = postPublishedAt,
      color = AppTheme.colorScheme.textEmphasisMed,
      textAlign = TextAlign.Start
    )

    PostOptionsButtonRow(
      postLink = postLink,
      postBookmarked = postBookmarked,
      postRead = postRead,
      commentsLink = commentsLink,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      togglePostReadClick = onTogglePostReadClick
    )
  }
}

@Composable
private fun PostOptionsButtonRow(
  postLink: String,
  postBookmarked: Boolean,
  postRead: Boolean,
  commentsLink: String?,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  togglePostReadClick: () -> Unit
) {
  Row {
    if (!commentsLink.isNullOrBlank()) {
      PostOptionIconButton(
        icon = TwineIcons.Comments,
        iconTint = Color.White,
        contentDescription = LocalStrings.current.comments,
        onClick = onCommentsClick
      )
    }

    PostOptionIconButton(
      icon =
        if (postBookmarked) {
          TwineIcons.Bookmarked
        } else {
          TwineIcons.Bookmark
        },
      iconTint =
        if (postBookmarked) {
          AppTheme.colorScheme.tintedForeground
        } else {
          Color.White
        },
      contentDescription = LocalStrings.current.bookmark,
      onClick = onBookmarkClick
    )

    var showDropdown by remember { mutableStateOf(false) }
    Box {
      PostOptionIconButton(
        icon = Icons.Filled.MoreVert,
        contentDescription = LocalStrings.current.moreMenuOptions,
        onClick = { showDropdown = true }
      )

      DropdownMenu(
        modifier = Modifier.width(IntrinsicSize.Min),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
        offset = DpOffset(x = 0.dp, y = (-48).dp),
      ) {
        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          text = {
            val label =
              if (postRead) {
                LocalStrings.current.markAsUnRead
              } else {
                LocalStrings.current.markAsRead
              }

            Text(
              text = label,
              color = AppTheme.colorScheme.tintedForeground,
              textAlign = TextAlign.Start
            )
          },
          leadingIcon = {
            val icon =
              if (postRead) {
                Icons.Outlined.CheckCircle
              } else {
                Icons.Filled.CheckCircle
              }

            Icon(
              icon,
              contentDescription = null,
              tint = AppTheme.colorScheme.tintedForeground,
            )
          },
          onClick = {
            togglePostReadClick()
            showDropdown = false
          }
        )

        Divider(modifier = Modifier.padding(vertical = 4.dp))

        val shareHandler = LocalShareHandler.current
        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          text = {
            Text(
              text = LocalStrings.current.share,
              color = AppTheme.colorScheme.tintedForeground,
              textAlign = TextAlign.Start
            )
          },
          leadingIcon = {
            Icon(
              Icons.Filled.Share,
              contentDescription = null,
              tint = AppTheme.colorScheme.tintedForeground,
            )
          },
          onClick = {
            shareHandler.share(postLink)
            showDropdown = false
          }
        )
      }
    }
  }
}

@Composable
private fun PostOptionIconButton(
  icon: ImageVector,
  contentDescription: String,
  modifier: Modifier = Modifier,
  iconTint: Color = Color.White,
  onClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.requiredSize(40.dp)
        .clip(MaterialTheme.shapes.small)
        .clickable(onClick = onClick)
        .then(modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}
