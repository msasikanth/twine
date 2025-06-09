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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.rounded.DoneOutline
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.resources.icons.DropdownIcon
import dev.sasikanth.rss.reader.resources.icons.LayoutCompact
import dev.sasikanth.rss.reader.resources.icons.LayoutDefault
import dev.sasikanth.rss.reader.resources.icons.LayoutSimple
import dev.sasikanth.rss.reader.resources.icons.Settings
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.util.homeAppBarTimestamp
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.bookmarks
import twine.shared.generated.resources.homeViewMode
import twine.shared.generated.resources.homeViewModeCompact
import twine.shared.generated.resources.homeViewModeDefault
import twine.shared.generated.resources.homeViewModeSimple
import twine.shared.generated.resources.markAllAsRead
import twine.shared.generated.resources.moreMenuOptions
import twine.shared.generated.resources.postsAll
import twine.shared.generated.resources.postsLast24Hours
import twine.shared.generated.resources.postsSearchHint
import twine.shared.generated.resources.postsToday
import twine.shared.generated.resources.postsUnread
import twine.shared.generated.resources.settings

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  source: Source?,
  currentDateTime: LocalDateTime,
  postsType: PostsType,
  listState: LazyListState,
  hasFeeds: Boolean?,
  hasUnreadPosts: Boolean,
  homeViewMode: HomeViewMode,
  modifier: Modifier = Modifier,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onPostTypeChanged: (PostsType) -> Unit,
  onMarkPostsAsRead: (Source?) -> Unit,
  onChangeHomeViewMode: (HomeViewMode) -> Unit,
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
      currentDateTime = currentDateTime,
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
        contentDescription = stringResource(Res.string.postsSearchHint),
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    IconButton(
      onClick = onBookmarksClicked,
    ) {
      Icon(
        imageVector = Icons.Outlined.BookmarkBorder,
        contentDescription = stringResource(Res.string.bookmarks),
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    OverflowMenu(
      hasUnreadPosts = hasUnreadPosts,
      homeViewMode = homeViewMode,
      onSettingsClicked = onSettingsClicked,
      onMarkAllAsRead = { onMarkPostsAsRead(source) },
      onChangeHomeViewMode = onChangeHomeViewMode
    )
  }
}

@Composable
fun SourceInfo(
  source: Source?,
  currentDateTime: LocalDateTime,
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
        Modifier.clip(MaterialTheme.shapes.small)
          .clickable(enabled = hasFeeds == true) { showPostsTypeDropDown = true }
          .onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
      verticalAlignment = Alignment.CenterVertically
    ) {
      SourceIcon(source)

      val sourceLabel =
        when (source) {
          is FeedGroup -> source.name
          is Feed -> source.name
          else -> currentDateTime.homeAppBarTimestamp()
        }

      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(
          modifier = Modifier.basicMarquee(),
          text = sourceLabel,
          style = MaterialTheme.typography.labelSmall,
          color = AppTheme.colorScheme.onSurface,
          maxLines = 1,
        )

        AnimatedVisibility(visible = hasFeeds == true) {
          val postsTypeLabel = getPostTypeLabel(postsType)

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = postsTypeLabel,
              style = MaterialTheme.typography.titleMedium,
              color = AppTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.requiredWidth(4.dp))

            Icon(
              imageVector = TwineIcons.DropdownIcon,
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
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> stringResource(Res.string.postsAll)
    PostsType.UNREAD -> stringResource(Res.string.postsUnread)
    PostsType.TODAY -> stringResource(Res.string.postsToday)
    PostsType.LAST_24_HOURS -> stringResource(Res.string.postsLast24Hours)
  }

@Composable
private fun OverflowMenu(
  hasUnreadPosts: Boolean,
  homeViewMode: HomeViewMode,
  onSettingsClicked: () -> Unit,
  onMarkAllAsRead: () -> Unit,
  onChangeHomeViewMode: (HomeViewMode) -> Unit,
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
        contentDescription = stringResource(Res.string.moreMenuOptions),
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    if (dropdownExpanded) {
      DropdownMenu(
        modifier = Modifier.requiredWidth(240.dp),
        offset = DpOffset(x = 0.dp, y = buttonHeight.unaryMinus()),
        expanded = dropdownExpanded,
        onDismissRequest = { dropdownExpanded = false }
      ) {
        Text(
          modifier = Modifier.padding(horizontal = 20.dp).padding(top = 20.dp, bottom = 12.dp),
          text = stringResource(Res.string.homeViewMode),
          style = MaterialTheme.typography.labelMedium,
          color = AppTheme.colorScheme.onSurfaceVariant,
        )

        Row(modifier = Modifier.padding(horizontal = 8.dp)) {
          LayoutIconButton(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.homeViewModeDefault),
            icon = TwineIcons.LayoutDefault,
            selected = homeViewMode == HomeViewMode.Default,
            onClick = { onChangeHomeViewMode(HomeViewMode.Default) }
          )

          LayoutIconButton(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.homeViewModeSimple),
            icon = TwineIcons.LayoutSimple,
            selected = homeViewMode == HomeViewMode.Simple,
            onClick = { onChangeHomeViewMode(HomeViewMode.Simple) }
          )

          LayoutIconButton(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.homeViewModeCompact),
            icon = TwineIcons.LayoutCompact,
            selected = homeViewMode == HomeViewMode.Compact,
            onClick = { onChangeHomeViewMode(HomeViewMode.Compact) }
          )
        }

        HorizontalDivider(
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
          thickness = 2.dp,
          color = AppTheme.colorScheme.surfaceContainerHigh
        )

        OverflowMenuItem(
          label = stringResource(Res.string.markAllAsRead),
          icon = Icons.Rounded.DoneOutline,
          enabled = hasUnreadPosts,
          onClick = {
            dropdownExpanded = false
            onMarkAllAsRead()
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
    text = {
      Text(
        text = label,
        color = AppTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium
      )
    },
    enabled = enabled,
    onClick = onClick,
    leadingIcon = {
      Icon(
        modifier = Modifier.requiredSize(20.dp),
        imageVector = icon,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurfaceVariant
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
      Modifier.then(modifier).padding(vertical = 8.dp).clip(MaterialTheme.shapes.medium).clickable {
        onClick()
      },
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

    Spacer(Modifier.requiredHeight(8.dp))
  }
}
