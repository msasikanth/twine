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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun FeedGroupIconGrid(
  icons: List<String>,
  iconSize: Dp = 18.dp,
  iconShape: Shape = CircleShape,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp),
  verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
  modifier: Modifier = Modifier,
) {
  if (icons.isNotEmpty()) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
      val icon2 =
        if (icons.size > 2) {
          icons.elementAtOrNull(1)
        } else {
          null
        }

      val icon4 =
        if (icons.size > 2) {
          icons.elementAtOrNull(3)
        } else {
          icons.elementAtOrNull(1)
        }

      Row(horizontalArrangement = horizontalArrangement) {
        FeedIcon(
          icon = icons.elementAtOrNull(0),
          iconSize = iconSize,
          iconShape = iconShape,
        )
        FeedIcon(
          icon = icon2,
          iconSize = iconSize,
          iconShape = iconShape,
        )
      }

      Row(horizontalArrangement = horizontalArrangement) {
        FeedIcon(
          icon = icons.elementAtOrNull(2),
          iconSize = iconSize,
          iconShape = iconShape,
        )
        FeedIcon(
          icon = icon4,
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
private fun FeedIcon(icon: String?, iconSize: Dp, iconShape: Shape, modifier: Modifier = Modifier) {
  if (!icon.isNullOrBlank()) {
    FeedIcon(
      url = icon,
      contentDescription = null,
      modifier =
        Modifier.requiredSize(iconSize).clip(iconShape).background(Color.White).then(modifier)
    )
  } else {
    Box(Modifier.requiredSize(iconSize))
  }
}
