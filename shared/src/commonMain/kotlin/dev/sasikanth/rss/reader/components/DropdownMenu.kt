/*
 * Copyright 2023 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun DropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset.Zero,
  content: @Composable ColumnScope.() -> Unit
) {
  MaterialTheme(shapes = MaterialTheme.shapes.copy(extraSmall = MaterialTheme.shapes.large)) {
    androidx.compose.material3.DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
      offset = offset,
      modifier =
        modifier.background(
          color = AppTheme.colorScheme.surface,
          shape = MaterialTheme.shapes.large
        ),
      content = content
    )
  }
}

@Composable
internal fun DropdownMenuItem(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  leadingIcon: (@Composable () -> Unit)? = null,
  contentDescription: String? = null,
  text: @Composable () -> Unit,
) {
  Row(
    modifier =
      modifier
        .clickable(onClickLabel = contentDescription, onClick = onClick)
        .padding(vertical = 12.dp)
        .padding(start = 16.dp, end = 20.dp)
        .fillMaxWidth()
  ) {
    CompositionLocalProvider(LocalContentColor provides AppTheme.colorScheme.onSurface) {
      if (leadingIcon != null) {
        leadingIcon()
        Spacer(Modifier.width(12.dp))
      } else {
        Spacer(Modifier.width(4.dp))
      }
      text()
    }
  }
}
