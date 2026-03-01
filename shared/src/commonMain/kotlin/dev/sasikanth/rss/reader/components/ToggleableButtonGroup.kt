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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun ToggleableButtonGroup(
  items: List<ToggleableButtonItem>,
  onItemSelected: (ToggleableButtonItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val containerHeight = 48.dp
  val indicatorHeight = 40.dp
  val horizontalPadding = 4.dp
  val itemSpacing = 1.dp

  Box(
    modifier =
      Modifier.then(modifier)
        .requiredHeightIn(min = containerHeight)
        .border(width = 1.dp, color = AppTheme.colorScheme.outline, shape = CircleShape)
  ) {
    val backgroundColor = AppTheme.colorScheme.inverseSurface
    val selectedItemIndex by
      animateFloatAsState(
        targetValue = items.indexOfFirst { it.isSelected }.coerceAtLeast(0).toFloat(),
        label = "selected_index",
      )

    Row(
      modifier =
        Modifier.padding(horizontal = horizontalPadding).drawBehind {
          val itemSpacingPx = itemSpacing.toPx()
          val itemCount = items.size
          val itemWidth = (size.width - (itemSpacingPx * (itemCount - 1))) / itemCount

          val indicatorHeightPx = indicatorHeight.toPx()
          val yOffset = (size.height - indicatorHeightPx) / 2

          drawRoundRect(
            color = backgroundColor,
            topLeft = Offset(x = (itemWidth + itemSpacingPx) * selectedItemIndex, y = yOffset),
            size = Size(width = itemWidth, height = indicatorHeightPx),
            cornerRadius = CornerRadius(size.height / 2),
          )
        },
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(itemSpacing),
    ) {
      items.forEachIndexed { index, item ->
        val isSelected = item.isSelected
        val isFirst = index == 0
        val isPreviousSelected = if (index > 0) items[index - 1].isSelected else false

        ToggleableButton(
          modifier = Modifier.weight(1f),
          item = item,
          showDivider = !isFirst && !isSelected && !isPreviousSelected,
          onClick = { onItemSelected(item) },
        )
      }
    }
  }
}

@Composable
private fun ToggleableButton(
  item: ToggleableButtonItem,
  showDivider: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
    if (showDivider) {
      VerticalDivider(
        modifier = Modifier.height(20.dp),
        color = AppTheme.colorScheme.outlineVariant,
      )
    }

    Surface(
      modifier = Modifier.fillMaxWidth().requiredHeightIn(min = 40.dp),
      shape = CircleShape,
      color = Color.Transparent,
      onClick = onClick,
    ) {
      val contentColor =
        if (item.isSelected) {
          AppTheme.colorScheme.inverseOnSurface
        } else {
          AppTheme.colorScheme.onSurfaceVariant
        }

      Box(contentAlignment = Alignment.Center) {
        Text(item.label, style = MaterialTheme.typography.labelLarge, color = contentColor)
      }
    }
  }
}

data class ToggleableButtonItem(val label: String, val isSelected: Boolean, val identifier: Any?)
