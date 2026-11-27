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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorProducer
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
import dev.sasikanth.rss.reader.home.ui.featuredImageMemoryCacheKey
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs
import dev.sasikanth.rss.reader.reader.ui.ImageClickEvent
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.ParallaxAlignment
import dev.sasikanth.rss.reader.utils.formatRelativeTime
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage

private val ReaderPostMetadataConfig =
  PostMetadataConfig(
    showUnreadIndicator = false,
    showToggleReadUnreadOption = true,
    enablePostSource = false,
  )

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
  onImageClick: (ImageClickEvent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = Modifier.fillMaxWidth().then(modifier)) {
    val title = readerPost.title
    val description = readerPost.description
    val postImage = readerPost.imageUrl

    if (!postImage.isNullOrBlank()) {
      val unlockAspectRatio = UrlUtils.isUnconstrainedMedia(postImage)
      val sharedElementKey = "header:${readerPost.id}:$postImage"
      val memoryCacheKey = featuredImageMemoryCacheKey(postImage, unlockAspectRatio)
      val sourceCornerRadiusDp = if (unlockAspectRatio) 8f else 28f
      val sourceAspectRatio = if (unlockAspectRatio) 0f else 16f / 9f

      Box(
        modifier =
          Modifier.padding(horizontal = 16.dp).align(Alignment.CenterHorizontally).clickable {
            onImageClick(
              ImageClickEvent(
                imageUrl = postImage,
                sharedElementKey = sharedElementKey,
                memoryCacheKey = memoryCacheKey,
                sourceCornerRadiusDp = sourceCornerRadiusDp,
                sourceAspectRatio = sourceAspectRatio,
              )
            )
          }
      ) {
        FeaturedImage(
          imageUrl = postImage,
          unlockAspectRatio = unlockAspectRatio,
          sharedElementKey = sharedElementKey,
          alignment =
            remember(page, pagerState) {
              ParallaxAlignment(
                horizontalBias = { pagerState.getOffsetFractionForPage(page) },
                multiplier = 2f,
              )
            },
        )
      }

      Spacer(modifier = Modifier.requiredHeight(8.dp))
    }

    val colorScheme = AppTheme.colorScheme
    val secondaryColorProducer = remember(colorScheme) { ColorProducer { colorScheme.secondary } }
    val headlineMedium = MaterialTheme.typography.headlineMedium
    val headlineStyle =
      remember(headlineMedium) { headlineMedium.copy(fontWeight = FontWeight.Bold) }
    val headlineModifier =
      if (themeVariant == ThemeVariant.Dynamic) {
        val isDark = themeVariant.isDark(darkTheme)
        Modifier.padding(top = 12.dp).graphicsLayer {
          blendMode = if (isDark) BlendMode.Screen else BlendMode.Multiply
        }
      } else {
        Modifier.padding(top = 12.dp)
      }

    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
      BasicText(
        modifier = headlineModifier,
        text = title.ifBlank { description },
        style = headlineStyle,
        color = secondaryColorProducer,
        overflow = TextOverflow.Ellipsis,
      )

      if (excerpt.isNotBlank()) {
        Spacer(Modifier.requiredHeight(8.dp))

        BasicText(
          text = excerpt,
          style = MaterialTheme.typography.bodyMedium,
          color = secondaryColorProducer,
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
          config = ReaderPostMetadataConfig,
          onDropdownChange = { showDropdown = it },
          onSourceClick = {
            // no-op
          },
        )
      }
    }
  }
}
