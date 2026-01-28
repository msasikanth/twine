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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.Check
import dev.sasikanth.rss.reader.resources.icons.Close
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun Switch(checked: Boolean, modifier: Modifier = Modifier, onCheckedChange: (Boolean) -> Unit) {
  MaterialTheme(
    colorScheme =
      MaterialTheme.colorScheme.copy(
        primary = AppTheme.colorScheme.primary,
        onPrimary = AppTheme.colorScheme.onPrimary,
        outline = AppTheme.colorScheme.outline,
        surfaceContainerHighest = AppTheme.colorScheme.surfaceContainerHighest
      )
  ) {
    androidx.compose.material3.Switch(
      modifier = modifier,
      checked = checked,
      thumbContent = {
        val icon = if (checked) TwineIcons.Check else TwineIcons.Close
        Icon(modifier = Modifier.size(16.dp), imageVector = icon, contentDescription = null)
      },
      onCheckedChange = onCheckedChange
    )
  }
}
