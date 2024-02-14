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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.resources.icons.Bookmarks
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.Tune
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.resources.strings.LocalStrings
import dev.sasikanth.rss.reader.ui.AppTheme

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  hasFeeds: Boolean,
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
        .padding(start = 12.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (!hasFeeds) {
      AppName()
    } else {
      PostsTypeSelector(postsType = postsType, onPostTypeChanged = onPostTypeChanged)
    }

    Spacer(Modifier.weight(1f))

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
        imageVector = TwineIcons.Bookmarks,
        contentDescription = LocalStrings.current.bookmarks,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }

    OverflowMenu(onSettingsClicked)
  }
}

@Composable
fun PostsTypeSelector(
  modifier: Modifier = Modifier,
  postsType: PostsType = PostsType.ALL,
  onPostTypeChanged: (PostsType) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }
  val title = getPostTypeLabel(postsType)

  Box {
    TextButton(
      onClick = { showDropdown = true },
      modifier = modifier,
    ) {
      Text(
        modifier = Modifier.align(Alignment.CenterVertically),
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.titleLarge
      )

      Spacer(Modifier.width(4.dp))

      Icon(
        modifier = Modifier.align(Alignment.CenterVertically),
        imageVector = Icons.Filled.ArrowDropDown,
        contentDescription = null,
        tint = Color.White
      )
    }

    DropdownMenu(
      modifier = Modifier.requiredWidth(120.dp),
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
}

@Composable
@ReadOnlyComposable
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> LocalStrings.current.postsAll
    PostsType.UNREAD -> LocalStrings.current.postsUnread
    PostsType.TODAY -> LocalStrings.current.postsToday
  }

@Composable
private fun AppName(modifier: Modifier = Modifier) {
  Row(modifier = modifier.padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
    Text(
      text = LocalStrings.current.appName,
      color = Color.White,
      style = MaterialTheme.typography.titleLarge
    )

    Spacer(Modifier.width(4.dp))

    Icon(imageVector = TwineIcons.RSS, contentDescription = null, tint = Color.White)
  }
}

@Composable
private fun OverflowMenu(onSettingsClicked: () -> Unit) {
  Box {
    var dropdownExpanded by remember { mutableStateOf(false) }

    IconButton(
      onClick = {
        try {
          dropdownExpanded = true
          throw IllegalStateException("Handled exception from common code")
        } catch (e: Exception) {
          BugsnagKotlin.sendHandledException(e)
        }
      },
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
          text = { Text(text = LocalStrings.current.settings) },
          leadingIcon = {
            Icon(imageVector = TwineIcons.Tune, contentDescription = LocalStrings.current.settings)
          },
          onClick = {
            dropdownExpanded = false
            onSettingsClicked()
            throw Exception("Fatal exception from the common code")
          }
        )
      }
    }
  }
}
