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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.markArticleAsRead
import twine.shared.generated.resources.markArticleAsReadOnOpen
import twine.shared.generated.resources.markArticleAsReadOnScroll

@Composable
internal fun MarkAsReadOnSettingItem(
  articleMarkAsReadOn: MarkAsReadOn,
  onMarkAsReadOnChanged: (MarkAsReadOn) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.markArticleAsRead),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    Box {
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

      TextButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
        onClick = { showDropdown = true },
        shape = MaterialTheme.shapes.medium,
      ) {
        val markAsReadOnLabel =
          when (articleMarkAsReadOn) {
            MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
            MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
          }

        Text(
          text = markAsReadOnLabel,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground,
        )

        Spacer(Modifier.requiredWidth(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground,
        )
      }

      DropdownMenu(
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
      ) {
        MarkAsReadOn.entries.forEach { markAsReadOn ->
          val label =
            when (markAsReadOn) {
              MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
              MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
            }

          val backgroundColor =
            if (markAsReadOn == articleMarkAsReadOn) {
              AppTheme.colorScheme.tintedHighlight
            } else {
              Color.Unspecified
            }

          DropdownMenuItem(
            onClick = {
              onMarkAsReadOnChanged(markAsReadOn)
              showDropdown = false
            },
            modifier = Modifier.background(backgroundColor),
          ) {
            val textColor =
              if (markAsReadOn == articleMarkAsReadOn) {
                AppTheme.colorScheme.inverseOnSurface
              } else {
                AppTheme.colorScheme.textEmphasisHigh
              }

            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = textColor)
          }
        }
      }
    }
  }
}
