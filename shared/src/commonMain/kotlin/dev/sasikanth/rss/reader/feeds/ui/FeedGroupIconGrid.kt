/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun FeedGroupIconGrid(
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  iconSize: Dp = 16.dp,
  iconShape: Shape = RoundedCornerShape(6.dp),
  horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(2.dp),
  verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(2.dp),
  modifier: Modifier = Modifier,
) {
  val iconsCount = maxOf(feedHomepageLinks.size, feedIconLinks.size)

  if (iconsCount > 0) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
      Row(horizontalArrangement = horizontalArrangement) {
        FeedIcon(
          index = 0,
          feedHomepageLinks = feedHomepageLinks,
          feedIconLinks = feedIconLinks,
          feedShowFavIconSettings = feedShowFavIconSettings,
          iconSize = iconSize,
          iconShape = iconShape,
        )
        FeedIcon(
          index = 2,
          feedHomepageLinks = feedHomepageLinks,
          feedIconLinks = feedIconLinks,
          feedShowFavIconSettings = feedShowFavIconSettings,
          iconSize = iconSize,
          iconShape = iconShape,
        )
      }

      Row(horizontalArrangement = horizontalArrangement) {
        FeedIcon(
          index = 3,
          feedHomepageLinks = feedHomepageLinks,
          feedIconLinks = feedIconLinks,
          feedShowFavIconSettings = feedShowFavIconSettings,
          iconSize = iconSize,
          iconShape = iconShape,
        )
        FeedIcon(
          index = 1,
          feedHomepageLinks = feedHomepageLinks,
          feedIconLinks = feedIconLinks,
          feedShowFavIconSettings = feedShowFavIconSettings,
          iconSize = iconSize,
          iconShape = iconShape,
        )
      }
    }
  } else {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
      Icon(
        imageVector = Icons.TwoTone.FolderOpen,
        contentDescription = null,
        tint = AppTheme.colorScheme.onSurface,
        modifier = Modifier.requiredSize(36.dp)
      )
    }
  }
}

@Composable
private fun FeedIcon(
  index: Int,
  feedHomepageLinks: List<String>,
  feedIconLinks: List<String>,
  feedShowFavIconSettings: List<Boolean>,
  iconSize: Dp,
  iconShape: Shape,
  modifier: Modifier = Modifier
) {
  val homepageLink = feedHomepageLinks.getOrNull(index)
  val iconLink = feedIconLinks.getOrNull(index)
  val showFavIconSetting = feedShowFavIconSettings.getOrNull(index) ?: true

  if (!homepageLink.isNullOrBlank() || !iconLink.isNullOrBlank()) {
    FeedIcon(
      icon = iconLink.orEmpty(),
      homepageLink = homepageLink.orEmpty(),
      showFeedFavIcon = showFavIconSetting,
      contentDescription = null,
      shape = iconShape,
      modifier = Modifier.requiredSize(iconSize).background(Color.White).then(modifier)
    )
  } else {
    Box(Modifier.requiredSize(iconSize))
  }
}
