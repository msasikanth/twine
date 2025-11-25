/*
 * Copyright 2025 Sasikanth Miriyampalli
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.relativeDurationString
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.LocalShowFeedFavIconSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

private val postListPadding
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Expanded -> PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(0.dp)
    }

private val compactPostListPadding
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Expanded -> PaddingValues(horizontal = 128.dp)
      else -> PaddingValues(horizontal = 24.dp)
    }

@Composable
internal fun PostListItem(
  item: PostWithMetadata,
  darkTheme: Boolean,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  onPostSourceClick: () -> Unit,
  togglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
  reduceReadItemAlpha: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
) {
  Column(
    modifier =
      Modifier.then(modifier)
        .clickable(onClick = onClick)
        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
        .padding(postListPadding)
        .alpha(
          if (item.read && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
          else Constants.ITEM_UNREAD_ALPHA
        )
        .semantics { contentDescription = item.title.ifBlank { item.description } }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        modifier =
          Modifier.weight(1f).align(Alignment.Top).padding(horizontal = 8.dp).padding(top = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        text = item.title.ifBlank { item.description },
        color = AppTheme.colorScheme.textEmphasisHigh,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      item.imageUrl?.let { url ->
        AsyncImage(
          url = url,
          modifier =
            Modifier.requiredSize(width = 120.dp, height = 68.dp)
              .clip(RoundedCornerShape(16.dp))
              .align(Alignment.CenterVertically),
          contentDescription = null,
          contentScale = ContentScale.Crop
        )
      }
    }

    val showFeedFavIcon = LocalShowFeedFavIconSetting.current
    val feedIconUrl = if (showFeedFavIcon) item.feedHomepageLink else item.feedIcon

    PostActionBar(
      feedName = item.feedName,
      feedIcon = feedIconUrl,
      postRelativeTimestamp = item.date.relativeDurationString(),
      config = postMetadataConfig,
      postLink = item.link,
      postRead = item.read,
      postBookmarked = item.bookmarked,
      commentsLink = item.commentsLink,
      darkTheme = darkTheme,
      onBookmarkClick = onPostBookmarkClick,
      onCommentsClick = onPostCommentsClick,
      onSourceClick = onPostSourceClick,
      onTogglePostReadClick = togglePostReadClick,
      modifier = Modifier.padding(horizontal = 24.dp)
    )
  }
}

@Composable
internal fun CompactPostListItem(
  item: PostWithMetadata,
  showDivider: Boolean,
  darkTheme: Boolean,
  onClick: () -> Unit,
  onPostBookmarkClick: () -> Unit,
  onPostCommentsClick: () -> Unit,
  togglePostReadClick: () -> Unit,
  modifier: Modifier = Modifier,
  reduceReadItemAlpha: Boolean = false,
  postMetadataConfig: PostMetadataConfig = PostMetadataConfig.DEFAULT,
) {
  val showFeedFavIcon = LocalShowFeedFavIconSetting.current
  val feedIconUrl = if (showFeedFavIcon) item.feedHomepageLink else item.feedIcon

  Box {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
        Modifier.then(modifier)
          .clickable { onClick() }
          .padding(vertical = 12.dp)
          .padding(compactPostListPadding)
          .alpha(
            if (item.read && reduceReadItemAlpha) Constants.ITEM_READ_ALPHA
            else Constants.ITEM_UNREAD_ALPHA
          )
    ) {
      FeedIcon(
        url = feedIconUrl,
        contentDescription = null,
        modifier = Modifier.requiredSize(16.dp).clip(RoundedCornerShape(4.dp)),
      )

      Spacer(Modifier.requiredWidth(16.dp))

      Text(
        text = item.title.ifBlank { item.description },
        style = MaterialTheme.typography.titleSmall,
        color = AppTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f)
      )

      Spacer(Modifier.requiredWidth(16.dp))

      PostActions(
        postLink = item.link,
        postBookmarked = item.bookmarked,
        postRead = item.read,
        config = postMetadataConfig,
        commentsLink = item.commentsLink,
        darkTheme = darkTheme,
        onBookmarkClick = onPostBookmarkClick,
        onCommentsClick = onPostCommentsClick,
        togglePostReadClick = togglePostReadClick,
      )
    }

    if (showDivider) {
      HorizontalDivider(
        modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart).padding(postListPadding),
        color = AppTheme.colorScheme.outlineVariant
      )
    }
  }
}
