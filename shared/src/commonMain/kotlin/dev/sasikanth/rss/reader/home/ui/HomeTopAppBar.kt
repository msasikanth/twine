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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.CircularIconButton
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.MarkAllAsRead
import dev.sasikanth.rss.reader.resources.icons.Menu
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.appBarAllFeeds
import twine.shared.generated.resources.markAllAsRead
import twine.shared.generated.resources.moreMenuOptions
import twine.shared.generated.resources.postsAll
import twine.shared.generated.resources.postsLast24Hours
import twine.shared.generated.resources.postsToday
import twine.shared.generated.resources.postsUnread
import twine.shared.generated.resources.screenHome

private const val APP_BAR_OPAQUE_THRESHOLD = 200f

@Composable
internal fun HomeTopAppBar(
  source: Source?,
  postsType: PostsType,
  listState: LazyListState,
  hasUnreadPosts: Boolean,
  scrollBehavior: TopAppBarScrollBehavior?,
  modifier: Modifier = Modifier,
  onMenuClicked: (() -> Unit)? = null,
  onShowPostsSortFilter: () -> Unit,
  onMarkPostsAsRead: (Source?) -> Unit,
) {
  val backgroundAlphaProvider =
    remember(listState) {
      {
        if (listState.firstVisibleItemIndex == 0) {
          (listState.firstVisibleItemScrollOffset / APP_BAR_OPAQUE_THRESHOLD).coerceIn(0f, 0.9f)
        } else {
          0.9f
        }
      }
    }
  var hasUnreadPosts by remember(hasUnreadPosts) { mutableStateOf(hasUnreadPosts) }

  val backgroundColor = AppTheme.colorScheme.surface
  TopAppBar(
    modifier =
      modifier.drawBehind { drawRect(backgroundColor.copy(alpha = backgroundAlphaProvider())) },
    scrollBehavior = scrollBehavior,
    contentPadding = PaddingValues(start = 0.dp, top = 8.dp, end = 12.dp, bottom = 8.dp),
    title = { SourceInfo(source = source) },
    navigationIcon = {
      if (onMenuClicked != null) {
        CircularIconButton(
          modifier = Modifier.padding(start = 12.dp),
          icon = TwineIcons.Menu,
          label = stringResource(Res.string.moreMenuOptions),
          onClick = onMenuClicked,
        )
      }
    },
    actions = {
      AnimatedContent(hasUnreadPosts, transitionSpec = { fadeIn().togetherWith(fadeOut()) }) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
          Spacer(Modifier.width(12.dp))

          PostTypePill(postsType = postsType, onClick = onShowPostsSortFilter)

          AnimatedVisibility(
            visible = it,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
          ) {
            Row {
              Spacer(Modifier.width(12.dp))

              CircularIconButton(
                icon = TwineIcons.MarkAllAsRead,
                label = stringResource(Res.string.markAllAsRead),
                enabled = it,
                backgroundColor = AppTheme.colorScheme.inverseSurface,
                contentColor = AppTheme.colorScheme.inverseOnSurface,
                borderColor = Color.Transparent,
                onClick = {
                  hasUnreadPosts = false
                  onMarkPostsAsRead(source)
                },
              )
            }
          }
        }
      }
    },
    colors =
      TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
      ),
  )
}

@Composable
private fun SourceInfo(source: Source?, modifier: Modifier = Modifier) {
  Column(modifier = modifier.padding(start = 12.dp)) {
    val title = stringResource(Res.string.screenHome)
    val subtitle =
      when (source) {
        is Feed -> source.name
        is FeedGroup -> source.name
        else -> stringResource(Res.string.appBarAllFeeds)
      }

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.onSurface,
      maxLines = 1,
    )

    Text(
      modifier = Modifier.basicMarquee(),
      text = subtitle,
      style = MaterialTheme.typography.bodySmall,
      color = AppTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
    )
  }
}

@Composable
private fun PostTypePill(postsType: PostsType, onClick: () -> Unit, modifier: Modifier = Modifier) {
  TranslucentButton(
    text = getPostTypeLabel(postsType),
    trailingIcon = TwineIcons.ArrowDown,
    onClick = onClick,
  )
}

@Composable
private fun getPostTypeLabel(type: PostsType) =
  when (type) {
    PostsType.ALL -> stringResource(Res.string.postsAll)
    PostsType.UNREAD -> stringResource(Res.string.postsUnread)
    PostsType.TODAY -> stringResource(Res.string.postsToday)
    PostsType.LAST_24_HOURS -> stringResource(Res.string.postsLast24Hours)
  }
