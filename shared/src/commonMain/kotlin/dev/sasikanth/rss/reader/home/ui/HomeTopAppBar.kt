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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.resources.icons.Bookmark
import dev.sasikanth.rss.reader.resources.icons.MarkAllAsRead
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.Sort
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmarks
import twine.shared.generated.resources.markAllAsRead
import twine.shared.generated.resources.moreMenuOptions
import twine.shared.generated.resources.postsAll
import twine.shared.generated.resources.postsFilter
import twine.shared.generated.resources.postsLast24Hours
import twine.shared.generated.resources.postsSearchHint
import twine.shared.generated.resources.postsToday
import twine.shared.generated.resources.postsUnread
import twine.shared.generated.resources.screenHome
import twine.shared.generated.resources.settings

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  source: Source?,
  postsType: PostsType,
  listState: LazyListState,
  hasUnreadPosts: Boolean,
  scrollBehavior: TopAppBarScrollBehavior,
  modifier: Modifier = Modifier,
  onMenuClicked: (() -> Unit)? = null,
  onShowPostsSortFilter: () -> Unit,
  onMarkPostsAsRead: (Source?) -> Unit,
) {
  val backgroundAlpha by
    remember(listState) {
      derivedStateOf {
        if (listState.firstVisibleItemIndex == 0) {
          (listState.firstVisibleItemScrollOffset / APP_BAR_OPAQUE_THRESHOLD).coerceIn(0f, 0.85f)
        } else {
          0.85f
        }
      }
    }
  var hasUnreadPosts by remember(hasUnreadPosts) { mutableStateOf(hasUnreadPosts) }

  CenterAlignedTopAppBar(
    modifier = modifier.background(AppTheme.colorScheme.surface.copy(alpha = backgroundAlpha)),
    scrollBehavior = scrollBehavior,
    contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
    title = { SourceInfo(source = source, postsType = postsType) },
    navigationIcon = {
      if (onMenuClicked != null) {
        CircularIconButton(
          modifier = Modifier.padding(start = 12.dp),
          icon = Icons.Rounded.Menu,
          label = stringResource(Res.string.moreMenuOptions),
          onClick = onMenuClicked
        )
      }
    },
    actions = {
      AnimatedVisibility(
        visible = hasUnreadPosts,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
      ) {
        CircularIconButton(
          icon = TwineIcons.MarkAllAsRead,
          label = stringResource(Res.string.markAllAsRead),
          enabled = hasUnreadPosts,
          onClick = {
            hasUnreadPosts = false
            onMarkPostsAsRead(source)
          }
        )
      }

      Spacer(Modifier.width(8.dp))

      CircularIconButton(
        icon = TwineIcons.Sort,
        label = stringResource(Res.string.postsFilter),
        onClick = onShowPostsSortFilter
      )
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent
      )
  )
}

@Composable
private fun SourceInfo(
  source: Source?,
  postsType: PostsType,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    Row(
      modifier = Modifier.clip(MaterialTheme.shapes.small),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        val title =
          when (source) {
            is Feed -> source.name
            is FeedGroup -> source.name
            else -> stringResource(Res.string.screenHome)
          }

        Text(
          modifier = Modifier.basicMarquee(),
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.onSurface,
          maxLines = 1,
        )

        Text(
          text = getPostTypeLabel(postsType),
          style = MaterialTheme.typography.labelMedium,
          color = AppTheme.colorScheme.secondary,
        )
      }
    }
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
          shape = MaterialTheme.shapes.small,
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
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> stringResource(Res.string.postsAll)
    PostsType.UNREAD -> stringResource(Res.string.postsUnread)
    PostsType.TODAY -> stringResource(Res.string.postsToday)
    PostsType.LAST_24_HOURS -> stringResource(Res.string.postsLast24Hours)
  }

@Composable
private fun OverflowMenu(
  onSearchClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
) {
  BoxWithConstraints {
    val density = LocalDensity.current
    var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    CircularIconButton(
      modifier =
        Modifier.onGloballyPositioned { coordinates ->
          buttonHeight = with(density) { coordinates.size.height.toDp() }
        },
      icon = Icons.Rounded.MoreVert,
      label = stringResource(Res.string.moreMenuOptions),
      onClick = { dropdownExpanded = true }
    )

    if (dropdownExpanded) {
      DropdownMenu(
        modifier = Modifier.requiredWidth(240.dp),
        offset = DpOffset(x = 0.dp, y = buttonHeight.unaryMinus()),
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false }
      ) {
        OverflowMenuItem(
          icon = Icons.Rounded.Search,
          label = stringResource(Res.string.postsSearchHint),
          onClick = {
            dropdownExpanded = false
            onSearchClicked()
          }
        )

        OverflowMenuItem(
          label = stringResource(Res.string.bookmarks),
          icon = TwineIcons.Bookmark,
          onClick = {
            dropdownExpanded = false
            onBookmarksClicked()
          }
        )

        OverflowMenuItem(
          label = stringResource(Res.string.settings),
          icon = TwineIcons.Settings,
          onClick = {
            dropdownExpanded = false
            onSettingsClicked()
          }
        )
      }
    }
  }
}

@Composable
private fun OverflowMenuItem(
  label: String,
  icon: ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  selected: Boolean = false,
  enabled: Boolean = true,
  padding: PaddingValues = PaddingValues(0.dp),
) {
  DropdownMenuItem(
    modifier =
      Modifier.then(modifier)
        .clearAndSetSemantics {
          role = Role.Button
          contentDescription = label
        }
        .padding(padding)
        .background(
          color = if (selected) AppTheme.colorScheme.primaryContainer else Color.Unspecified,
          shape = MaterialTheme.shapes.large
        ),
    text = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
    enabled = enabled,
    onClick = onClick,
    leadingIcon = {
      Icon(
        modifier = Modifier.requiredSize(20.dp),
        imageVector = icon,
        contentDescription = null,
      )
    },
  )
}

@Composable
private fun LayoutIconButton(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Column(
    modifier =
      Modifier.then(modifier)
        .clip(MaterialTheme.shapes.medium)
        .clickable { onClick() }
        .padding(vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    val defaultTranslucentStyle = LocalTranslucentStyles.current.default
    val background =
      if (selected) {
        Color.Transparent
      } else {
        defaultTranslucentStyle.background
      }
    val border =
      if (selected) {
        defaultTranslucentStyle.outline.copy(alpha = 0.48f)
      } else {
        defaultTranslucentStyle.outline
      }
    val shape =
      if (selected) {
        MaterialTheme.shapes.medium
      } else {
        MaterialTheme.shapes.small
      }
    val iconTint =
      if (selected) {
        AppTheme.colorScheme.inverseOnSurface
      } else {
        AppTheme.colorScheme.outline
      }
    val padding by animateDpAsState(if (selected) 0.dp else 4.dp)

    Box(
      modifier =
        Modifier.requiredSize(48.dp)
          .padding(padding)
          .background(background, shape)
          .border(1.dp, border, shape),
      contentAlignment = Alignment.Center
    ) {
      val iconBackground by
        animateColorAsState(
          if (selected) {
            AppTheme.colorScheme.inverseSurface
          } else {
            Color.Transparent
          }
        )
      val iconBackgroundSize by animateDpAsState(if (selected) 40.dp else 0.dp)

      Box(
        modifier =
          Modifier.requiredSize(iconBackgroundSize)
            .background(iconBackground, MaterialTheme.shapes.small),
      )

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.requiredSize(20.dp)
      )
    }

    Spacer(Modifier.requiredHeight(4.dp))

    val textStyle =
      if (selected) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
    Text(
      text = label,
      style = textStyle,
      color = AppTheme.colorScheme.onSurface,
    )
  }
}
