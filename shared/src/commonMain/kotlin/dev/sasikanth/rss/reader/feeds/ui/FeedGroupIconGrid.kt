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
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun FeedGroupIconGrid(icons: List<String>, modifier: Modifier = Modifier) {
  if (icons.isNotEmpty()) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val icon1 = icons.elementAtOrNull(0)
        if (icon1 != null) {
          AsyncImage(
            url = icon1,
            contentDescription = null,
            modifier = Modifier.requiredSize(18.dp).clip(CircleShape)
          )
        }
        val icon2 = icons.elementAtOrNull(1)
        if (icon2 != null) {
          AsyncImage(
            url = icon2,
            contentDescription = null,
            modifier = Modifier.requiredSize(18.dp).clip(CircleShape)
          )
        }
      }

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val icon3 = icons.elementAtOrNull(2)
        if (icon3 != null) {
          AsyncImage(
            url = icon3,
            contentDescription = null,
            modifier = Modifier.requiredSize(18.dp).clip(CircleShape)
          )
        }
        val icon4 = icons.elementAtOrNull(3)
        if (icon4 != null) {
          AsyncImage(
            url = icon4,
            contentDescription = null,
            modifier = Modifier.requiredSize(18.dp).clip(CircleShape)
          )
        }
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
