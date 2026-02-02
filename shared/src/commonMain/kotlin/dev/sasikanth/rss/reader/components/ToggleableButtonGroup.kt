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

package dev.sasikanth.rss.reader.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun ToggleableButtonGroup(
  items: List<ToggleableButtonItem>,
  onItemSelected: (ToggleableButtonItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      Modifier.then(modifier)
        .requiredHeightIn(min = 56.dp)
        .border(
          width = 1.dp,
          color = AppTheme.colorScheme.outlineVariant,
          shape = MaterialTheme.shapes.large,
        )
  ) {
    val backgroundColor = AppTheme.colorScheme.tintedForeground
    val selectedItemIndex by
      animateFloatAsState(items.indexOfFirst { it.isSelected }.toFloat(), label = "selected_index")

    Row(
      modifier =
        Modifier.padding(4.dp).drawBehind {
          val spacingBetween = 4.dp.toPx()
          val itemWidth = (size.width - spacingBetween * (items.size - 1)) / items.size

          drawRoundRect(
            color = backgroundColor,
            topLeft =
              Offset(
                x = itemWidth * selectedItemIndex + spacingBetween * selectedItemIndex,
                y = 0f,
              ),
            size = Size(width = itemWidth, height = size.height),
            cornerRadius = CornerRadius(12.dp.toPx()),
          )
        },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      items.forEach { toggleableButtonItem ->
        TextButton(
          modifier = Modifier.weight(1f).requiredHeightIn(min = 48.dp),
          content = { Text(toggleableButtonItem.label) },
          shape = MaterialTheme.shapes.medium,
          colors =
            ButtonDefaults.textButtonColors(
              contentColor =
                if (toggleableButtonItem.isSelected) {
                  AppTheme.colorScheme.tintedSurface
                } else {
                  AppTheme.colorScheme.tintedForeground
                }
            ),
          onClick = { onItemSelected(toggleableButtonItem) },
        )
      }
    }
  }
}

data class ToggleableButtonItem(val label: String, val isSelected: Boolean, val identifier: Any?)
