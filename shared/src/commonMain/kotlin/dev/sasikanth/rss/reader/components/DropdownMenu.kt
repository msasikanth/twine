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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset.Zero,
  content: @Composable ColumnScope.() -> Unit,
) {
  androidx.compose.material3.DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    offset = offset,
    shape = MaterialTheme.shapes.largeIncreased,
    modifier =
      modifier
        .sizeIn(minWidth = 216.dp)
        .background(color = AppTheme.colorScheme.surface, shape = MaterialTheme.shapes.large),
    content = content,
  )
}

@Composable
internal fun DropdownMenuItem(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: ImageVector? = null,
  contentDescription: String? = null,
  enabled: Boolean = true,
  selected: Boolean = false,
) {
  Row(
    modifier =
      modifier
        .clickable(onClickLabel = contentDescription, enabled = enabled, onClick = onClick)
        .padding(vertical = 12.dp, horizontal = 20.dp)
        .fillMaxWidth()
  ) {
    val translucentStyle = LocalTranslucentStyles.current
    val contentColor =
      when {
        !enabled -> translucentStyle.default.outline
        selected -> AppTheme.colorScheme.primary
        else -> AppTheme.colorScheme.onSurface
      }
    val iconColor =
      if (enabled) {
        AppTheme.colorScheme.onSurfaceVariant
      } else {
        translucentStyle.default.outline
      }

    CompositionLocalProvider(LocalContentColor provides iconColor) {
      if (leadingIcon != null) {
        Icon(modifier = Modifier.size(20.dp), imageVector = leadingIcon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
      }
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
      Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
fun DropdownMenuDivider() {
  HorizontalDivider(
    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
    color = AppTheme.colorScheme.outlineVariant,
  )
}
