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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

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
      modifier.background(color = AppTheme.colorScheme.surface, shape = MaterialTheme.shapes.large),
    content = content,
  )
}

@Composable
internal fun DropdownMenuItem(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: (@Composable () -> Unit)? = null,
  contentDescription: String? = null,
  enabled: Boolean = true,
  text: @Composable () -> Unit,
) {
  Row(
    modifier =
      modifier
        .clickable(onClickLabel = contentDescription, enabled = enabled, onClick = onClick)
        .padding(vertical = 12.dp, horizontal = 20.dp)
        .fillMaxWidth()
  ) {
    val contentColor =
      if (enabled) {
        AppTheme.colorScheme.onSurface
      } else {
        AppTheme.colorScheme.onSurface.copy(alpha = 0.38f)
      }
    val iconColor =
      if (enabled) {
        AppTheme.colorScheme.onSurfaceVariant
      } else {
        AppTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
      }

    CompositionLocalProvider(LocalContentColor provides iconColor) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(Modifier.width(16.dp))
      } else {
        Spacer(Modifier.width(4.dp))
      }
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) { text() }
  }
}
