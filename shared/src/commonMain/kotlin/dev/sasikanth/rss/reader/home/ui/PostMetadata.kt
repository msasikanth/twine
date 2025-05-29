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
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun PostMetadata(
  feedName: String,
  feedIcon: String,
  postRead: Boolean,
  postPublishedAt: String,
  postLink: String,
  postBookmarked: Boolean,
  commentsLink: String?,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onTogglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
  config: PostMetadataConfig = PostMetadataConfig.DEFAULT,
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
    PostSourcePill(
      modifier = Modifier.weight(1f).clearAndSetSemantics {},
      feedName = feedName,
      feedIcon = feedIcon,
      config = config,
      onSourceClick = onSourceClick,
    )

    Text(
      modifier = Modifier.padding(horizontal = 8.dp).clearAndSetSemantics {},
      style = MaterialTheme.typography.bodySmall,
      maxLines = 1,
      text = postPublishedAt,
      color = AppTheme.colorScheme.outline,
      textAlign = TextAlign.Start
    )

    PostOptionsButtonRow(
      postLink = postLink,
      postBookmarked = postBookmarked,
      postRead = postRead,
      config = config,
      commentsLink = commentsLink,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      togglePostReadClick = onTogglePostReadClick
    )
  }
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
  feedName: String,
  config: PostMetadataConfig,
  onSourceClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    val postSourceTextColor =
      if (config.enablePostSource) {
        AppTheme.colorScheme.onSurface
      } else {
        AppTheme.colorScheme.onSurfaceVariant
      }

    Row(
      modifier =
        Modifier.background(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
            RoundedCornerShape(50)
          )
          .border(
            1.dp,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            RoundedCornerShape(50)
          )
          .clip(RoundedCornerShape(50))
          .clickable(onClick = onSourceClick, enabled = config.enablePostSource)
          .padding(vertical = 6.dp)
          .padding(start = 8.dp, end = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FeedIcon(
        modifier = Modifier.requiredSize(16.dp).clip(RoundedCornerShape(4.dp)),
        url = feedIcon,
        contentDescription = null,
      )

      Spacer(Modifier.requiredWidth(6.dp))

      Text(
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        text = feedName,
        color = postSourceTextColor,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
internal fun PostOptionsButtonRow(
  postLink: String,
  postBookmarked: Boolean,
  postRead: Boolean,
  config: PostMetadataConfig,
  commentsLink: String?,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  togglePostReadClick: () -> Unit
) {
  Row(modifier = Modifier.semantics { isTraversalGroup = true }) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = LocalStrings.current.comments
      PostOptionIconButton(
        modifier =
          Modifier.semantics {
            role = Role.Button
            contentDescription = commentsLabel
          },
        icon = TwineIcons.Comments,
        iconTint = AppTheme.colorScheme.onSurfaceVariant,
        onClick = onCommentsClick
      )
    }

    val bookmarkLabel =
      if (postBookmarked) {
        LocalStrings.current.unBookmark
      } else {
        LocalStrings.current.bookmark
      }
    PostOptionIconButton(
      modifier =
        Modifier.semantics {
          role = Role.Button
          contentDescription = bookmarkLabel
        },
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
          AppTheme.colorScheme.onSurfaceVariant
        },
      onClick = onBookmarkClick
    )

    var showDropdown by remember { mutableStateOf(false) }
    Box {
      val coroutineScope = rememberCoroutineScope()
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
      val moreMenuOptionsLabel = LocalStrings.current.moreMenuOptions

      PostOptionIconButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
              buttonHeight = with(density) { coordinates.size.height.toDp() }
            }
            .semantics {
              role = Role.Button
              contentDescription = moreMenuOptionsLabel
            },
        icon = Icons.Filled.MoreVert,
        iconTint = AppTheme.colorScheme.onSurfaceVariant,
        onClick = { showDropdown = true }
      )

      DropdownMenu(
        modifier = Modifier.width(IntrinsicSize.Min),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
        offset = DpOffset(x = 0.dp, y = buttonHeight.unaryMinus()),
      ) {
        if (config.showToggleReadUnreadOption) {
          val markAsReadLabel =
            if (postRead) {
              LocalStrings.current.markAsUnRead
            } else {
              LocalStrings.current.markAsRead
            }

          DropdownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            contentDescription = markAsReadLabel,
            text = {
              Text(
                text = markAsReadLabel,
                color = AppTheme.colorScheme.onSurface,
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
                tint = AppTheme.colorScheme.onSurface,
              )
            },
            onClick = {
              togglePostReadClick()
              showDropdown = false
            }
          )
        }

        val linkHandler = LocalLinkHandler.current
        val openWebsiteLabel = LocalStrings.current.openWebsite

        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          contentDescription = openWebsiteLabel,
          text = {
            Text(
              text = openWebsiteLabel,
              color = AppTheme.colorScheme.onSurface,
              textAlign = TextAlign.Start
            )
          },
          leadingIcon = {
            Icon(
              modifier = Modifier.requiredSize(24.dp),
              imageVector = TwineIcons.Website,
              contentDescription = null,
              tint = AppTheme.colorScheme.onSurface,
            )
          },
          onClick = {
            coroutineScope.launch { linkHandler.openLink(postLink) }
            showDropdown = false
          }
        )

        val shareHandler = LocalShareHandler.current
        val shareLabel = LocalStrings.current.share

        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          contentDescription = shareLabel,
          text = {
            Text(
              text = shareLabel,
              color = AppTheme.colorScheme.onSurface,
              textAlign = TextAlign.Start
            )
          },
          leadingIcon = {
            Icon(
              TwineIcons.Share,
              contentDescription = null,
              tint = AppTheme.colorScheme.onSurface,
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
  modifier: Modifier = Modifier,
  iconTint: Color = AppTheme.colorScheme.textEmphasisHigh,
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
      contentDescription = null,
      tint = iconTint,
      modifier = Modifier.size(20.dp)
    )
  }
}

@Immutable
data class PostMetadataConfig(
  val showUnreadIndicator: Boolean,
  val showToggleReadUnreadOption: Boolean,
  val enablePostSource: Boolean
) {

  companion object {

    val DEFAULT =
      PostMetadataConfig(
        showUnreadIndicator = true,
        showToggleReadUnreadOption = true,
        enablePostSource = true
      )
  }
}
