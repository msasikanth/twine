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

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.home.ui.FeaturedImage
import dev.sasikanth.rss.reader.home.ui.PostActionBar
import dev.sasikanth.rss.reader.home.ui.PostMetadataConfig
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.ParallaxAlignment
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage

@Composable
internal fun PostHeader(
  readerPost: ResolvedPost,
  showFullArticle: Boolean,
  page: Int,
  pagerState: PagerState,
  excerpt: String,
  darkTheme: Boolean,
  themeVariant: ThemeVariant,
  fromScreen: ReaderScreenArgs.FromScreen,
  onCommentsClick: () -> Unit,
  onShareClick: () -> Unit,
  onBookmarkClick: () -> Unit,
  onMarkAsUnread: () -> Unit,
  onImageClick: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = Modifier.fillMaxWidth().then(modifier)) {
    val title = readerPost.title
    val description = readerPost.description
    val postImage = readerPost.imageUrl

    if (!postImage.isNullOrBlank()) {
      Box(
        modifier =
          Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally).clickable {
            onImageClick(postImage)
          }
      ) {
        FeaturedImage(
          imageUrl = postImage,
          unlockAspectRatio = UrlUtils.isUnconstrainedMedia(postImage),
          alignment =
            remember(page) {
              ParallaxAlignment(
                horizontalBias = { pagerState.getOffsetFractionForPage(page) },
                multiplier = 2f,
              )
            },
        )
      }

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      Text(
        modifier =
          Modifier.padding(top = 12.dp).graphicsLayer {
            if (themeVariant == ThemeVariant.Dynamic) {
              blendMode =
                if (themeVariant.isDark(darkTheme)) {
                  BlendMode.Screen
                } else {
                  BlendMode.Multiply
                }
            }
          },
        text = title.ifBlank { description },
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colorScheme.secondary,
        overflow = TextOverflow.Ellipsis,
      )

      if (excerpt.isNotBlank()) {
        Spacer(Modifier.requiredHeight(8.dp))

        Text(
          text = excerpt,
          style = MaterialTheme.typography.bodyMedium,
          color = AppTheme.colorScheme.secondary,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(Modifier.requiredHeight(12.dp))

      DisableSelection {
        var showDropdown by remember { mutableStateOf(false) }

        PostActionBar(
          feedName = readerPost.feedName,
          feedIcon = readerPost.feedIcon,
          feedHomepageLink = readerPost.feedHomepageLink,
          showFeedFavIcon = readerPost.showFeedFavIcon,
          postRead = readerPost.read,
          postRelativeTimestamp = readerPost.date.formatRelativeTime(),
          postLink = readerPost.link,
          postBookmarked = readerPost.bookmarked,
          commentsLink = readerPost.commentsLink,
          postReadingTimeEstimate = readerPost.feedContentReadingTime ?: 0,
          onBookmarkClick = onBookmarkClick,
          onCommentsClick = onCommentsClick,
          onTogglePostReadClick = onMarkAsUnread,
          showDropdown = showDropdown,
          alwaysShowMarkAsUnread = true,
          hideMarkAsOptions = fromScreen == ReaderScreenArgs.FromScreen.Bookmarks,
          config =
            PostMetadataConfig(
              showUnreadIndicator = false,
              showToggleReadUnreadOption = true,
              enablePostSource = false,
            ),
          onDropdownChange = { showDropdown = it },
          onSourceClick = {
            // no-op
          },
        )
      }
    }
  }
}
