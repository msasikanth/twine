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

package dev.sasikanth.rss.reader.tags.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.Tag
import dev.sasikanth.rss.reader.resources.icons.Tag
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun TagItem(
  tag: Tag,
  isSelected: Boolean,
  modifier: Modifier = Modifier,
  onTagClicked: () -> Unit
) {
  Row(
    Modifier.fillMaxWidth()
      .clickable(onClick = onTagClicked)
      .padding(vertical = 16.dp, horizontal = 16.dp)
      .then(modifier),
    horizontalArrangement = Arrangement.spacedBy(24.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = TwineIcons.Tag,
      contentDescription = null,
      tint = AppTheme.colorScheme.textEmphasisHigh
    )

    Text(
      modifier = Modifier.weight(1f),
      text = tag.label,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.textEmphasisHigh
    )

    if (isSelected) {
      Icon(
        imageVector = Icons.TwoTone.CheckCircle,
        contentDescription = null,
        tint = AppTheme.colorScheme.tintedForeground
      )
    }
  }
}
