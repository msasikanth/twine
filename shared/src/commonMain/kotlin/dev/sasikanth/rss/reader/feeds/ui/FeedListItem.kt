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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.FeedLabelInput
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.ui.FeedsSheetMode.Edit
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.DoneAll
import dev.sasikanth.rss.reader.resources.icons.Pin
import dev.sasikanth.rss.reader.resources.icons.PinFilled
import dev.sasikanth.rss.reader.resources.icons.Share
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.icons.Website
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.share.LocalShareHandler
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.launch

@Composable
internal fun FeedListItem(
  modifier: Modifier = Modifier,
  feed: Feed,
  selected: Boolean,
  canPinFeeds: Boolean,
  canShowUnreadPostsCount: Boolean,
  feedsSheetMode: FeedsSheetMode,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedSelected: (Feed) -> Unit,
  onFeedNameChanged: (newFeedName: String, feedLink: String) -> Unit,
  onFeedPinClick: (Feed) -> Unit,
  onMarkFeedAsRead: (Feed) -> Unit,
) {
  val clickableModifier =
    if (feedsSheetMode != Edit) {
      modifier.clickable { onFeedSelected(feed) }
    } else {
      modifier
    }

  Box(modifier = clickableModifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp)) {
    Row(
      modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(contentAlignment = Alignment.Center) {
        SelectionIndicator(selected = selected && feedsSheetMode != Edit, animationProgress = 1f)

        Box(
          modifier =
            Modifier.requiredSize(56.dp).background(Color.White, RoundedCornerShape(16.dp)),
          contentAlignment = Alignment.Center
        ) {
          BadgedBox(
            badge = {
              val numberOfUnreadPosts = feed.numberOfUnreadPosts
              if (numberOfUnreadPosts > 0 && canShowUnreadPostsCount) {
                Badge(
                  containerColor = AppTheme.colorScheme.tintedForeground,
                  contentColor = AppTheme.colorScheme.tintedBackground,
                  modifier =
                    Modifier.sizeIn(minWidth = 24.dp, minHeight = 16.dp).graphicsLayer {
                      translationX = -8.dp.toPx()
                      translationY = 4.dp.toPx()
                    }
                ) {
                  Text(feed.numberOfUnreadPosts.toString())
                }
              }
            }
          ) {
            AsyncImage(
              url = feed.icon,
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier =
                Modifier.requiredSize(48.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .align(Alignment.Center),
            )
          }
        }
      }

      Spacer(Modifier.requiredWidth(16.dp))

      FeedLabelInput(
        modifier = Modifier.weight(1f),
        value = feed.name,
        onFeedNameChanged = { newFeedName -> onFeedNameChanged(newFeedName, feed.link) },
        enabled = feedsSheetMode == Edit
      )

      Spacer(Modifier.requiredWidth(16.dp))

      AnimatedContent(feedsSheetMode == Edit) {
        ActionButtons(
          feed = feed,
          isInEditMode = it,
          canPinFeed = canPinFeeds,
          onFeedInfoClick = onFeedInfoClick,
          onFeedPinClick = onFeedPinClick,
          onMarkFeedAsRead = onMarkFeedAsRead
        )
      }
    }
  }
}

@Composable
private fun ActionButtons(
  feed: Feed,
  isInEditMode: Boolean,
  canPinFeed: Boolean,
  onFeedInfoClick: (Feed) -> Unit,
  onFeedPinClick: (Feed) -> Unit,
  onMarkFeedAsRead: (Feed) -> Unit,
) {
  Row {
    if (isInEditMode) {
      PinFeedIconButton(feed = feed, canPinFeed = canPinFeed, onFeedPinClick = onFeedPinClick)

      IconButton(onClick = { onFeedInfoClick(feed) }) {
        Icon(
          imageVector = Icons.TwoTone.Info,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground
        )
      }
    } else {
      FeedListItemMenu(feed = feed, onMarkFeedAsRead = onMarkFeedAsRead)
    }
  }
}

@Composable
fun FeedListItemMenu(feed: Feed, onMarkFeedAsRead: (Feed) -> Unit, modifier: Modifier = Modifier) {
  Box(modifier) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val shareHandler = LocalShareHandler.current
    val linkHandler = LocalLinkHandler.current

    IconButton(onClick = { showDropdownMenu = true }) {
      Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = null,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    DropdownMenu(
      expanded = showDropdownMenu,
      onDismissRequest = { showDropdownMenu = false },
      offset = DpOffset(x = 0.dp, y = (-24).dp)
    ) {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.share) },
        leadingIcon = {
          Icon(imageVector = TwineIcons.Share, contentDescription = LocalStrings.current.share)
        },
        onClick = {
          showDropdownMenu = false
          shareHandler.share(feed.link)
        }
      )

      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.openWebsite) },
        leadingIcon = {
          Icon(
            modifier = Modifier.requiredSize(24.dp),
            imageVector = TwineIcons.Website,
            contentDescription = LocalStrings.current.openWebsite
          )
        },
        onClick = {
          showDropdownMenu = false
          coroutineScope.launch { linkHandler.openLink(feed.homepageLink) }
        }
      )

      val hasUnreadPostsInFeed = feed.numberOfUnreadPosts > 0
      if (hasUnreadPostsInFeed) {
        Divider(modifier = Modifier.padding(vertical = 4.dp))

        DropdownMenuItem(
          text = { Text(text = LocalStrings.current.markAllAsRead) },
          leadingIcon = {
            Icon(
              imageVector = TwineIcons.DoneAll,
              contentDescription = LocalStrings.current.markAllAsRead
            )
          },
          onClick = {
            showDropdownMenu = false
            onMarkFeedAsRead(feed)
          },
        )
      }
    }
  }
}

@Composable
private fun PinFeedIconButton(
  feed: Feed,
  canPinFeed: Boolean,
  onFeedPinClick: (Feed) -> Unit,
) {
  val pinnedIconColor =
    if (canPinFeed) {
      AppTheme.colorScheme.tintedForeground
    } else {
      AppTheme.colorScheme.tintedForeground.copy(alpha = 0.4f)
    }

  IconButton(onClick = { onFeedPinClick(feed) }, enabled = canPinFeed) {
    Icon(
      imageVector =
        if (feed.pinnedAt != null) {
          TwineIcons.PinFilled
        } else {
          TwineIcons.Pin
        },
      contentDescription = null,
      tint = pinnedIconColor
    )
  }
}
