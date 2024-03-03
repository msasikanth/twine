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

package dev.sasikanth.rss.reader.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun Switch(checked: Boolean, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
  MaterialTheme(
    colorScheme =
      darkColorScheme(
        primary = AppTheme.colorScheme.tintedForeground,
        onPrimary = AppTheme.colorScheme.tintedSurface,
        outline = AppTheme.colorScheme.outline,
        surfaceVariant = AppTheme.colorScheme.surfaceContainerHighest
      )
  ) {
    androidx.compose.material3.Switch(
      modifier = modifier,
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}
