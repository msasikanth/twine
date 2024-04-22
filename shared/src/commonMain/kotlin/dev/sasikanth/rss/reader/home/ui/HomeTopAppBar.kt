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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.feeds.ui.FeedGroupIconGrid
import dev.sasikanth.rss.reader.resources.icons.Bookmarks
import dev.sasikanth.rss.reader.resources.icons.Tune
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  source: Source?,
  postsType: PostsType,
  listState: LazyListState,
  modifier: Modifier = Modifier,
  onSearchClicked: () -> Unit,
  onBookmarksClicked: () -> Unit,
  onSettingsClicked: () -> Unit,
  onPostTypeChanged: (PostsType) -> Unit
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
        .pointerInput(Unit) {}
        .fillMaxWidth()
        .background(AppTheme.colorScheme.surface.copy(alpha = backgroundAlpha))
        .windowInsetsPadding(
          WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
        )
        .padding(horizontal = 8.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    PostsFilter(
      modifier = Modifier.weight(1f),
      source = source,
      postsType = postsType,
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

    OverflowMenu(
      onSettingsClicked = onSettingsClicked,
      onBookmarksClicked = onBookmarksClicked,
    )
  }
}

@Composable
fun PostsFilter(
  modifier: Modifier = Modifier,
  source: Source?,
  postsType: PostsType = PostsType.ALL,
  onPostTypeChanged: (PostsType) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }
  val postsTypeLabel = getPostTypeLabel(postsType)

  Row(
    modifier = modifier.clip(MaterialTheme.shapes.large).clickable { showDropdown = true },
    verticalAlignment = Alignment.CenterVertically
  ) {
    SourceIcon(source)

    Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp)) {
      val sourceLabel =
        when (source) {
          is FeedGroup -> source.name
          is Feed -> source.name
          else -> LocalStrings.current.appBarAllFeeds
        }

      Text(
        text = sourceLabel,
        style = MaterialTheme.typography.titleLarge,
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 1,
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = postsTypeLabel,
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = null,
          modifier = Modifier.requiredSize(20.dp),
          tint = AppTheme.colorScheme.tintedForeground,
        )
      }
    }
  }

  DropdownMenu(
    modifier = Modifier.requiredWidth(158.dp),
    expanded = showDropdown,
    onDismissRequest = { showDropdown = false },
  ) {
    PostsType.entries.forEach { type ->
      val label = getPostTypeLabel(type)
      val color =
        if (postsType == type) {
          AppTheme.colorScheme.tintedSurface
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
          showDropdown = false
        },
        modifier = Modifier.background(color)
      ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = labelColor)
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

    when (source) {
      is FeedGroup -> {
        val iconSize =
          if (source.feedIcons.size > 2) {
            18.dp
          } else {
            20.dp
          }

        val iconSpacing =
          if (source.feedIcons.size > 2) {
            4.dp
          } else {
            0.dp
          }

        FeedGroupIconGrid(
          icons = source.feedIcons,
          iconSize = iconSize,
          iconShape = RoundedCornerShape(percent = 30),
          horizontalArrangement = Arrangement.spacedBy(iconSpacing),
          verticalArrangement = Arrangement.spacedBy(iconSpacing)
        )
      }
      is Feed -> {
        AsyncImage(
          url = source.icon,
          contentDescription = null,
          backgroundColor = Color.White,
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
@ReadOnlyComposable
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> LocalStrings.current.postsAll
    PostsType.UNREAD -> LocalStrings.current.postsUnread
    PostsType.TODAY -> LocalStrings.current.postsToday
    PostsType.LAST_24_HOURS -> LocalStrings.current.postsLast24Hours
  }

@Composable
private fun OverflowMenu(onBookmarksClicked: () -> Unit, onSettingsClicked: () -> Unit) {
  Box {
    var dropdownExpanded by remember { mutableStateOf(false) }

    IconButton(
      onClick = { dropdownExpanded = true },
    ) {
      Icon(
        imageVector = Icons.Rounded.MoreVert,
        contentDescription = LocalStrings.current.moreMenuOptions,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    if (dropdownExpanded) {
      DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
        DropdownMenuItem(
          text = { Text(text = LocalStrings.current.bookmarks) },
          leadingIcon = {
            Icon(
              imageVector = TwineIcons.Bookmarks,
              contentDescription = LocalStrings.current.bookmarks
            )
          },
          onClick = {
            dropdownExpanded = false
            onBookmarksClicked()
          }
        )

        DropdownMenuItem(
          text = { Text(text = LocalStrings.current.settings) },
          leadingIcon = {
            Icon(imageVector = TwineIcons.Tune, contentDescription = LocalStrings.current.settings)
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
