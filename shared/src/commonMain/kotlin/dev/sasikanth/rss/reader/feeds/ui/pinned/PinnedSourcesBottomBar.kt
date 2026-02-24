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

package dev.sasikanth.rss.reader.feeds.ui.pinned

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.BOTTOM_BAR_CORNER_RADIUS
import dev.sasikanth.rss.reader.utils.BOTTOM_BAR_MAX_WIDTH
import dev.sasikanth.rss.reader.utils.PINNED_SOURCES_BOTTOM_BAR_HEIGHT

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PinnedSourcesBottomBar(
  pinnedSources: List<Source>,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onSourceClick: (Source) -> Unit,
  onHomeSelected: () -> Unit,
  modifier: Modifier = Modifier,
  scrollBehavior: PinnedSourcesBottomBarScrollBehavior? = null,
) {
  val shape = RoundedCornerShape(BOTTOM_BAR_CORNER_RADIUS)

  val translationY = scrollBehavior?.state?.heightOffset ?: 0f

  LazyRow(
    modifier =
      Modifier.onGloballyPositioned { coordinates ->
          val height = coordinates.size.height.toFloat()
          if (scrollBehavior != null && scrollBehavior.state.heightOffsetLimit != -height) {
            scrollBehavior.state.heightOffsetLimit = -height
          }
        }
        .graphicsLayer { this.translationY = -translationY }
        .then(modifier)
        .navigationBarsPadding()
        .widthIn(max = BOTTOM_BAR_MAX_WIDTH)
        .padding(horizontal = 32.dp)
        .shadow(elevation = 4.dp, shape = shape)
        .clip(shape)
        .background(AppTheme.colorScheme.bottomSheet)
        .fillMaxWidth()
        .height(PINNED_SOURCES_BOTTOM_BAR_HEIGHT),
    verticalAlignment = Alignment.CenterVertically,
    contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
  ) {
    items(pinnedSources.size) { index ->
      SourceItem(
        source = pinnedSources[index],
        activeSource = activeSource,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        onHomeSelected = onHomeSelected,
        onSourceClick = onSourceClick,
      )
    }
  }
}

@Composable
private fun SourceItem(
  source: Source,
  activeSource: Source?,
  canShowUnreadPostsCount: Boolean,
  onHomeSelected: () -> Unit,
  onSourceClick: (Source) -> Unit,
) {
  when (val source = source) {
    is FeedGroup -> {
      PinnedFeedGroupItem(
        feedGroup = source,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
    is Feed -> {
      PinnedFeedItem(
        badgeCount = source.numberOfUnreadPosts,
        homePageUrl = source.homepageLink,
        feedIconUrl = source.icon,
        showFeedFavIcon = source.showFeedFavIcon,
        canShowUnreadPostsCount = canShowUnreadPostsCount,
        hasActiveSource = activeSource != null,
        selected = activeSource?.id == source.id,
        onClick = {
          if (activeSource?.id == source.id) {
            onHomeSelected()
          } else {
            onSourceClick(source)
          }
        },
      )
    }
  }
}
