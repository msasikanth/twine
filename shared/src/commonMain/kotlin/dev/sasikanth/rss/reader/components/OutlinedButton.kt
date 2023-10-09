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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun OutlinedButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: ButtonColors =
    ButtonDefaults.outlinedButtonColors(
      containerColor = AppTheme.colorScheme.surfaceContainerLow,
      contentColor = AppTheme.colorScheme.tintedForeground
    ),
  border: BorderStroke = BorderStroke(1.dp, AppTheme.colorScheme.surfaceContainerHigh),
  content: @Composable RowScope.() -> Unit
) {
  androidx.compose.material3.OutlinedButton(
    modifier = modifier,
    onClick = onClick,
    border = border,
    colors = colors,
    shape = MaterialTheme.shapes.medium,
    content = content,
    enabled = enabled
  )
}
