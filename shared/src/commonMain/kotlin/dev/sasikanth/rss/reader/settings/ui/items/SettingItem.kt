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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
fun SettingItem(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  action: (@Composable () -> Unit)? = null,
  onClick: () -> Unit,
) {
  Box(modifier = modifier.clickable { onClick() }) {
    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.onSurface,
        )

        if (subtitle != null) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      if (action != null) {
        Spacer(Modifier.width(16.dp))
        action()
      }
    }
  }
}
