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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.Constants.BADGE_COUNT_TRIM_LIMIT

@Composable
fun UnreadBadge(unreadCount: Long, modifier: Modifier = Modifier) {
  val translucentStyles = LocalTranslucentStyles.current
  if (unreadCount < 10) {
    Badge(
      containerColor = translucentStyles.default.background,
      contentColor = AppTheme.colorScheme.onSurface,
      modifier = modifier.size(24.dp),
    ) {
      val badgeText = unreadCount.toString()

      Text(
        text = badgeText,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(vertical = 4.dp).align(Alignment.CenterVertically),
      )
    }
  } else {
    Surface(
      color = translucentStyles.default.background,
      contentColor = AppTheme.colorScheme.onSurface,
      shape = CircleShape,
      modifier = modifier,
    ) {
      val badgeText =
        if (unreadCount > BADGE_COUNT_TRIM_LIMIT) {
          "+$BADGE_COUNT_TRIM_LIMIT"
        } else {
          unreadCount.toString()
        }

      Box(contentAlignment = Alignment.Center) {
        Text(
          text = badgeText,
          style = MaterialTheme.typography.labelMedium,
          modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
        )
      }
    }
  }
}
