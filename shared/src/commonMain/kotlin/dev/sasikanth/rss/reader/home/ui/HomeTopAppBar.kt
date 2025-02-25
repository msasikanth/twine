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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  source: Source?,
  postsType: PostsType,
  listState: LazyListState,
  hasFeeds: Boolean?,
  hasUnreadPosts: Boolean,
  modifier: Modifier = Modifier,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onPostTypeChanged: (PostsType) -> Unit,
  onMarkPostsAsRead: (Source?) -> Unit,
) {
  val backgroundAlpha by
    remember(listState) {
      derivedStateOf {
        if (listState.firstVisibleItemIndex == 0) {
          (listState.firstVisibleItemScrollOffset / APP_BAR_OPAQUE_THRESHOLD).coerceIn(0f, 1f)
        } else {
          1f
        }
      }
    }

  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .pointerInput(Unit) {}
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface.copy(alpha = backgroundAlpha))
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
        .padding(horizontal = 8.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    SourceInfo(
      modifier = Modifier.weight(1f),
      source = source,
      postsType = postsType,
      hasFeeds = hasFeeds,
      onPostTypeChanged = onPostTypeChanged
    )

    Spacer(Modifier.requiredWidth(16.dp))

    IconButton(
      onClick = onSearchClicked,
    ) {
      Icon(
        imageVector = Icons.Rounded.Search,
        contentDescription = LocalStrings.current.postsSearchHint,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    IconButton(
      onClick = onBookmarksClicked,
    ) {
      Icon(
        imageVector = Icons.Outlined.BookmarkBorder,
        contentDescription = LocalStrings.current.bookmarks,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    OverflowMenu(
      hasUnreadPosts = hasUnreadPosts,
      onSettingsClicked = onSettingsClicked,
      onMarkAllAsRead = { onMarkPostsAsRead(source) }
    )
  }
}

@Composable
fun SourceInfo(
  source: Source?,
  postsType: PostsType,
  hasFeeds: Boolean?,
  modifier: Modifier = Modifier,
  onPostTypeChanged: (PostsType) -> Unit,
) {
  val density = LocalDensity.current
  var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
  var showPostsTypeDropDown by remember { mutableStateOf(false) }

  Box(modifier) {
    Row(
      modifier =
        Modifier.clip(MaterialTheme.shapes.large).onGloballyPositioned { coordinates ->
          buttonHeight = with(density) { coordinates.size.height.toDp() }
        },
      verticalAlignment = Alignment.CenterVertically
    ) {
      SourceIcon(source)

      val sourceLabel =
        when (source) {
          is FeedGroup -> source.name
          is Feed -> source.name
          else -> LocalStrings.current.appName
        }

      Column(
        modifier =
          Modifier.padding(start = 16.dp, end = 8.dp).clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            enabled = hasFeeds == true
          ) {
            showPostsTypeDropDown = true
          }
      ) {
        Text(
          text = sourceLabel,
          style = MaterialTheme.typography.titleLarge,
          color = AppTheme.colorScheme.textEmphasisHigh,
          maxLines = 1,
        )

        AnimatedVisibility(visible = hasFeeds == true) {
          Spacer(Modifier.requiredHeight(8.dp))

          val postsTypeLabel = getPostTypeLabel(postsType)

          Row {
            Text(
              text = postsTypeLabel,
              style = MaterialTheme.typography.bodyMedium,
              color = AppTheme.colorScheme.textEmphasisHigh,
            )

            Spacer(Modifier.requiredWidth(4.dp))

            Icon(
              imageVector = Icons.Filled.ExpandMore,
              contentDescription = null,
              modifier = Modifier.requiredSize(20.dp),
              tint = AppTheme.colorScheme.textEmphasisHigh,
            )
          }
        }
      }
    }

    PostsFilterDropdown(
      showDropdown = showPostsTypeDropDown,
      postsType = postsType,
      offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
      onPostTypeChanged = onPostTypeChanged,
      onDismiss = { showPostsTypeDropDown = false }
    )
  }
}

@Composable
private fun SourceIcon(source: Source?, modifier: Modifier = Modifier) {
  Row(modifier) {
    if (source != null) {
      Spacer(Modifier.requiredWidth(16.dp))
    }

    val showFeedFavIcon = LocalShowFeedFavIconSetting.current
    when (source) {
      is FeedGroup -> {
        val icons = if (showFeedFavIcon) source.feedHomepageLinks else source.feedIconLinks
        val iconSize =
          if (icons.size > 2) {
            18.dp
          } else {
            20.dp
          }

        val iconSpacing =
          if (icons.size > 2) {
            4.dp
          } else {
            0.dp
          }

        FeedGroupIconGrid(
          icons = icons,
          iconSize = iconSize,
          iconShape = RoundedCornerShape(percent = 30),
          horizontalArrangement = Arrangement.spacedBy(iconSpacing),
          verticalArrangement = Arrangement.spacedBy(iconSpacing)
        )
      }
      is Feed -> {
        val icon = if (showFeedFavIcon) source.homepageLink else source.icon
        FeedIcon(
          url = icon,
          contentDescription = null,
          modifier = Modifier.clip(MaterialTheme.shapes.small).requiredSize(24.dp)
        )
      }
      else -> {
        // no-op
      }
    }
  }
}

@Composable
private fun PostsFilterDropdown(
  showDropdown: Boolean,
  postsType: PostsType,
  onPostTypeChanged: (PostsType) -> Unit,
  onDismiss: () -> Unit,
  offset: DpOffset = DpOffset.Zero,
) {
  DropdownMenu(
    modifier = Modifier.requiredWidth(158.dp),
    expanded = showDropdown,
    offset = offset,
    onDismissRequest = onDismiss,
  ) {
    PostsType.entries.forEach { type ->
      val label = getPostTypeLabel(type)
      val color =
        if (postsType == type) {
          AppTheme.colorScheme.tintedHighlight
        } else {
          Color.Unspecified
        }
      val labelColor =
        if (postsType == type) {
          AppTheme.colorScheme.onSurface
        } else {
          AppTheme.colorScheme.textEmphasisHigh
        }

      DropdownMenuItem(
        onClick = {
          onPostTypeChanged(type)
          onDismiss()
        },
        modifier = Modifier.background(color)
      ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = labelColor)
      }
    }
  }
}

@Composable
@ReadOnlyComposable
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> LocalStrings.current.postsAll
    PostsType.UNREAD -> LocalStrings.current.postsUnread
    PostsType.TODAY -> LocalStrings.current.postsToday
    PostsType.LAST_24_HOURS -> LocalStrings.current.postsLast24Hours
  }

@Composable
private fun OverflowMenu(
  hasUnreadPosts: Boolean,
  onSettingsClicked: () -> Unit,
  onMarkAllAsRead: () -> Unit,
) {
  BoxWithConstraints {
    val density = LocalDensity.current
    var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    IconButton(
      modifier =
        Modifier.onGloballyPositioned { coordinates ->
          buttonHeight = with(density) { coordinates.size.height.toDp() }
        },
      onClick = { dropdownExpanded = true },
    ) {
      Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = LocalStrings.current.moreMenuOptions,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    if (dropdownExpanded) {
      DropdownMenu(
        offset = DpOffset(x = 0.dp, y = buttonHeight.unaryMinus()),
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false }
      ) {
        if (hasUnreadPosts) {
          DropdownMenuItem(
            text = { Text(text = LocalStrings.current.markAllAsRead) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Filled.DoneAll,
                contentDescription = LocalStrings.current.markAllAsRead
              )
            },
            onClick = {
              dropdownExpanded = false
              onMarkAllAsRead()
            }
          )

          HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            thickness = 2.dp,
            color = AppTheme.colorScheme.surfaceContainerHighest
          )
        }

        DropdownMenuItem(
          text = { Text(text = LocalStrings.current.settings) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Rounded.Settings,
              contentDescription = LocalStrings.current.settings
            )
          },
          onClick = {
            dropdownExpanded = false
            onSettingsClicked()
          }
        )
      }
    }
  }
}
