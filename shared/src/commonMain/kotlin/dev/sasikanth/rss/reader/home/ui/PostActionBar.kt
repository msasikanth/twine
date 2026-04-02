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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.IconButton
import dev.sasikanth.rss.reader.components.IconButtonSize
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.Bookmarked
import dev.sasikanth.rss.reader.resources.icons.Comments
import dev.sasikanth.rss.reader.resources.icons.MoreHorizFilled
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Visibility
import dev.sasikanth.rss.reader.resources.icons.VisibilityOff
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants
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
  alwaysShowMarkAsUnread: Boolean = false,
  hideMarkAsOptions: Boolean = false,
  onDropdownChange: (Boolean) -> Unit = {},
  config: PostMetadataConfig = PostMetadataConfig.DEFAULT,
  onSourceClick: () -> Unit,
) {
  Row(
    modifier = Modifier.height(IntrinsicSize.Min).then(modifier),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      modifier = Modifier.fillMaxHeight().weight(1f),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      SourceInfo(
        modifier = Modifier.weight(1f, fill = false).widthIn(max = 120.dp).clearAndSetSemantics {},
        feedName = feedName,
        feedIcon = feedIcon,
        feedHomepageLink = feedHomepageLink,
        showFeedFavIcon = showFeedFavIcon,
        config = config,
        postRead = postRead,
        onSourceClick = onSourceClick,
      )

      Text(
        modifier = Modifier.padding(horizontal = 8.dp).clearAndSetSemantics {},
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        text = Constants.BULLET_POINT,
        color = AppTheme.colorScheme.outline,
      )

      Text(
        modifier = Modifier.clearAndSetSemantics {},
        style = MaterialTheme.typography.labelMedium,
        maxLines = 1,
        text = postRelativeTimestamp,
        color = AppTheme.colorScheme.outline,
        textAlign = TextAlign.Start,
        overflow = TextOverflow.Ellipsis,
      )

      if (postReadingTimeEstimate > 0) {
        Text(
          modifier = Modifier.padding(horizontal = 8.dp).clearAndSetSemantics {},
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1,
          text = Constants.BULLET_POINT,
          color = AppTheme.colorScheme.outline,
        )

        Text(
          modifier = Modifier.clearAndSetSemantics {},
          style = MaterialTheme.typography.labelMedium,
          maxLines = 1,
          text = stringResource(Res.string.readingTimeEstimate, postReadingTimeEstimate),
          color = AppTheme.colorScheme.outline,
          textAlign = TextAlign.Start,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    PostActions(
      postLink = postLink,
      postBookmarked = postBookmarked,
      postRead = postRead,
      config = config,
      commentsLink = commentsLink,
      showDropdown = showDropdown,
      alwaysShowMarkAsUnread = alwaysShowMarkAsUnread,
      hideMarkAsOptions = hideMarkAsOptions,
      onDropdownChange = onDropdownChange,
      onBookmarkClick = onBookmarkClick,
      onCommentsClick = onCommentsClick,
      togglePostReadClick = onTogglePostReadClick,
    )
  }
}

@Composable
private fun SourceInfo(
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
  feedName: String,
  postRead: Boolean,
  config: PostMetadataConfig,
  onSourceClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val postSourceTextColor =
    if (config.enablePostSource) {
      AppTheme.colorScheme.onSurface
    } else {
      AppTheme.colorScheme.onSurfaceVariant
    }

  Row(
    modifier =
      modifier
        .fillMaxHeight()
        .clip(MaterialTheme.shapes.extraSmall)
        .clickable(onClick = onSourceClick, enabled = config.enablePostSource),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.requiredSize(18.dp)
          .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    ) {
      FeedIcon(
        icon = feedIcon,
        homepageLink = feedHomepageLink,
        showFeedFavIcon = showFeedFavIcon,
        contentDescription = null,
        modifier =
          Modifier.requiredSize(16.dp)
            .border(1.dp, AppTheme.colorScheme.outlineVariant, RoundedCornerShape(25))
            .align(Alignment.Center),
      )

      if (!postRead) {
        Box(
          modifier =
            Modifier.align(Alignment.TopEnd)
              .requiredSize(6.dp)
              .dropShadow(CircleShape) {
                color = Color.Black
                spread = 1.dp.toPx()
                blendMode = BlendMode.DstOut
              }
              .background(MaterialTheme.colorScheme.error, CircleShape)
        )
      }
    }

    Spacer(Modifier.requiredWidth(4.dp))

    Text(
      style = MaterialTheme.typography.labelMedium,
      maxLines = 1,
      text = feedName,
      color = postSourceTextColor,
      overflow = TextOverflow.Ellipsis,
    )
  }
}

@Composable
internal fun PostActions(
  postLink: String,
  postBookmarked: Boolean,
  postRead: Boolean,
  config: PostMetadataConfig,
  commentsLink: String?,
  modifier: Modifier = Modifier,
  showDropdown: Boolean = false,
  alwaysShowMarkAsUnread: Boolean = false,
  hideMarkAsOptions: Boolean = false,
  onDropdownChange: (Boolean) -> Unit = {},
  onBookmarkClick: () -> Unit,
  onCommentsClick: () -> Unit,
  togglePostReadClick: () -> Unit,
) {
  Row(
    modifier = modifier.semantics { isTraversalGroup = true },
    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
  ) {
    if (!commentsLink.isNullOrBlank()) {
      val commentsLabel = stringResource(Res.string.comments)
      TooltipBox(
        state = rememberTooltipState(),
        positionProvider =
          TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above
          ),
        tooltip = {
          Box(
            modifier =
              Modifier.background(AppTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                .padding(8.dp)
          ) {
            Text(
              text = commentsLabel,
              style = MaterialTheme.typography.labelMedium,
              color = AppTheme.colorScheme.onSurface,
            )
          }
        },
      ) {
        IconButton(
          icon = TwineIcons.Comments,
          contentDescription = commentsLabel,
          size = IconButtonSize.Small,
          onClick = onCommentsClick,
        )
      }
    }

    val bookmarkLabel =
      if (postBookmarked) {
        stringResource(Res.string.unBookmark)
      } else {
        stringResource(Res.string.bookmark)
      }

    TooltipBox(
      state = rememberTooltipState(),
      positionProvider =
        TooltipDefaults.rememberTooltipPositionProvider(positioning = TooltipAnchorPosition.Above),
      tooltip = {
        Box(
          modifier =
            Modifier.background(AppTheme.colorScheme.surface, RoundedCornerShape(4.dp))
              .padding(8.dp)
        ) {
          Text(
            text = bookmarkLabel,
            style = MaterialTheme.typography.labelMedium,
            color = AppTheme.colorScheme.onSurface,
          )
        }
      },
    ) {
      IconButton(
        icon =
          if (postBookmarked) {
            TwineIcons.Bookmarked
          } else {
            TwineIcons.Bookmark
          },
        contentDescription = bookmarkLabel,
        size = IconButtonSize.Small,
        onClick = onBookmarkClick,
      )
    }

    Box {
      val coroutineScope = rememberCoroutineScope()
      val moreMenuOptionsLabel = stringResource(Res.string.moreMenuOptions)

      TooltipBox(
        state = rememberTooltipState(),
        positionProvider =
          TooltipDefaults.rememberTooltipPositionProvider(
            positioning = TooltipAnchorPosition.Above
          ),
        tooltip = {
          Box(
            modifier =
              Modifier.background(AppTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                .padding(8.dp)
          ) {
            Text(
              text = moreMenuOptionsLabel,
              style = MaterialTheme.typography.labelMedium,
              color = AppTheme.colorScheme.onSurface,
            )
          }
        },
      ) {
        IconButton(
          icon = TwineIcons.MoreHorizFilled,
          contentDescription = moreMenuOptionsLabel,
          size = IconButtonSize.Small,
        ) {
          onDropdownChange(true)
        }
      }

      DropdownMenu(
        modifier = Modifier.width(IntrinsicSize.Min),
        expanded = showDropdown,
        onDismissRequest = { onDropdownChange(false) },
      ) {
        if (config.showToggleReadUnreadOption && !hideMarkAsOptions) {
          val markAsReadLabel =
            if (alwaysShowMarkAsUnread || postRead) {
              stringResource(Res.string.markAsUnRead)
            } else {
              stringResource(Res.string.markAsRead)
            }

          DropdownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            leadingIcon =
              if (alwaysShowMarkAsUnread || postRead) {
                TwineIcons.VisibilityOff
              } else {
                TwineIcons.Visibility
              },
            text = markAsReadLabel,
            contentDescription = markAsReadLabel,
            onClick = {
              coroutineScope.launch {
                onDropdownChange(false)
                togglePostReadClick()
              }
            },
          )
        }

        val linkHandler = LocalLinkHandler.current
        val openWebsiteLabel = stringResource(Res.string.openWebsite)

        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          leadingIcon = TwineIcons.Website,
          text = openWebsiteLabel,
          contentDescription = openWebsiteLabel,
          onClick = {
            coroutineScope.launch {
              onDropdownChange(false)
              delay(150)
              linkHandler.openLink(postLink)
            }
          },
        )

        val shareHandler = LocalShareHandler.current
        val shareLabel = stringResource(Res.string.share)

        DropdownMenuItem(
          modifier = Modifier.fillMaxWidth(),
          leadingIcon = TwineIcons.Share,
          text = shareLabel,
          contentDescription = shareLabel,
          onClick = {
            coroutineScope.launch {
              onDropdownChange(false)
              delay(150)
              shareHandler.share(postLink)
            }
          },
        )
      }
    }
  }
}

@Immutable
data class PostMetadataConfig(
  val showUnreadIndicator: Boolean,
  val showToggleReadUnreadOption: Boolean,
  val enablePostSource: Boolean,
) {

  companion object {

    val DEFAULT =
      PostMetadataConfig(
        showUnreadIndicator = true,
        showToggleReadUnreadOption = true,
        enablePostSource = true,
      )
  }
}
