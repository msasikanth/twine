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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Visibility
import dev.sasikanth.rss.reader.resources.icons.VisibilityOff
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmark
import twine.shared.generated.resources.comments
import twine.shared.generated.resources.markAsRead
import twine.shared.generated.resources.markAsUnRead
import twine.shared.generated.resources.moreMenuOptions
import twine.shared.generated.resources.openWebsite
import twine.shared.generated.resources.readingTimeEstimate
import twine.shared.generated.resources.share
import twine.shared.generated.resources.unBookmark

@Composable
internal fun PostActionBar(
  feedName: String,
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
  postRead: Boolean,
  postRelativeTimestamp: String,
  postLink: String,
  postBookmarked: Boolean,
  commentsLink: String?,
  postReadingTimeEstimate: Int,
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  onTogglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
  showDropdown: Boolean = false,
  onDropdownChange: (Boolean) -> Unit = {},
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
      modifier = Modifier.weight(1f).padding(end = 8.dp).clearAndSetSemantics {},
      feedName = feedName,
      feedIcon = feedIcon,
      feedHomepageLink = feedHomepageLink,
      showFeedFavIcon = showFeedFavIcon,
      config = config,
      onSourceClick = onSourceClick,
    )

    Text(
      modifier = Modifier.clearAndSetSemantics {},
      style = MaterialTheme.typography.labelSmall,
      maxLines = 1,
      text = postRelativeTimestamp.uppercase(),
      color = AppTheme.colorScheme.outline,
      textAlign = TextAlign.Start,
      overflow = TextOverflow.Ellipsis,
    )

    if (postReadingTimeEstimate > 0) {
      Text(
        modifier = Modifier.padding(horizontal = 4.dp).clearAndSetSemantics {},
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        text = "\u2022",
        color = AppTheme.colorScheme.outline,
      )

      Text(
        modifier = Modifier.clearAndSetSemantics {},
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        text = stringResource(Res.string.readingTimeEstimate, postReadingTimeEstimate).uppercase(),
        color = AppTheme.colorScheme.outline,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
      )
    }

    PostActions(
      postLink = postLink,
      postBookmarked = postBookmarked,
      postRead = postRead,
      config = config,
      commentsLink = commentsLink,
      showDropdown = showDropdown,
      onDropdownChange = onDropdownChange,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      togglePostReadClick = onTogglePostReadClick
    )
  }
}

@Composable
private fun PostSourcePill(
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
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
        modifier = Modifier.requiredSize(16.dp),
        icon = feedIcon,
        homepageLink = feedHomepageLink,
        showFeedFavIcon = showFeedFavIcon,
        shape = MaterialTheme.shapes.extraSmall,
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
internal fun PostActions(
  postLink: String,
  postBookmarked: Boolean,
  postRead: Boolean,
  config: PostMetadataConfig,
  commentsLink: String?,
  showDropdown: Boolean = false,
  onDropdownChange: (Boolean) -> Unit = {},
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  togglePostReadClick: () -> Unit
) {
  Row(modifier = Modifier.semantics { isTraversalGroup = true }) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = stringResource(Res.string.comments)
      IconButton(
        icon = TwineIcons.Comments,
        contentDescription = commentsLabel,
        onClick = onCommentsClick
      )
    }

    val bookmarkLabel =
      if (postBookmarked) {
        stringResource(Res.string.unBookmark)
      } else {
        stringResource(Res.string.bookmark)
      }
    IconButton(
      icon =
        if (postBookmarked) {
          TwineIcons.Bookmarked
        } else {
          TwineIcons.Bookmark
        },
      contentDescription = bookmarkLabel,
      onClick = onBookmarkClick
    )

    Box {
      val coroutineScope = rememberCoroutineScope()
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
      val moreMenuOptionsLabel = stringResource(Res.string.moreMenuOptions)

      IconButton(
        icon = Icons.Filled.MoreVert,
        contentDescription = moreMenuOptionsLabel,
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          }
      ) {
        onDropdownChange(true)
      }

      DropdownMenu(
        modifier = Modifier.width(IntrinsicSize.Min),
        expanded = showDropdown,
        onDismissRequest = { onDropdownChange(false) },
        offset = DpOffset(x = 0.dp, y = buttonHeight.unaryMinus()),
      ) {
        if (config.showToggleReadUnreadOption) {
          val markAsReadLabel =
            if (postRead) {
              stringResource(Res.string.markAsUnRead)
            } else {
              stringResource(Res.string.markAsRead)
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
                  TwineIcons.VisibilityOff
                } else {
                  TwineIcons.Visibility
                }

              Icon(
                icon,
                contentDescription = null,
                tint = AppTheme.colorScheme.onSurface,
              )
            },
            onClick = {
              coroutineScope.launch {
                onDropdownChange(false)
                togglePostReadClick()
              }
            }
          )
        }

        val linkHandler = LocalLinkHandler.current
        val openWebsiteLabel = stringResource(Res.string.openWebsite)

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
            coroutineScope.launch {
              onDropdownChange(false)
              delay(150)
              linkHandler.openLink(postLink)
            }
          }
        )

        val shareHandler = LocalShareHandler.current
        val shareLabel = stringResource(Res.string.share)

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
            coroutineScope.launch {
              onDropdownChange(false)
              delay(150)
              shareHandler.share(postLink)
            }
          }
        )
      }
    }
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
