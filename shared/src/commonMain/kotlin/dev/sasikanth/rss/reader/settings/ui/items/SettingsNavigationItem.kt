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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.RSS
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun SettingsNavigationItem(
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector = TwineIcons.RSS,
) {
  Box(modifier = modifier.clickable(onClick = onClick)) {
    Row(modifier = Modifier.padding(all = 16.dp), verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier.size(40.dp)
            .background(AppTheme.colorScheme.onSurface.copy(alpha = 0.08f), shape = CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          modifier = Modifier.size(20.dp),
          imageVector = icon,
          contentDescription = null,
          tint = AppTheme.colorScheme.onSurface,
        )
      }

      Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
        Text(
          title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Medium,
          color = AppTheme.colorScheme.onSurface,
        )
        Text(
          subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}
