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

package dev.sasikanth.rss.reader.settings.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.settings.ui.items.SettingItem
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

internal val settingsItemHorizontalPadding: Dp
  @Composable
  @ReadOnlyComposable
  get() {
    val sizeClass = LocalWindowSizeClass.current
    return when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 128.dp
      else -> 0.dp
    }
  }

@Composable
internal fun SettingsDivider(horizontalInsets: Dp = 0.dp) {
  HorizontalDivider(
    modifier = Modifier.padding(vertical = 8.dp, horizontal = horizontalInsets),
    color = dev.sasikanth.rss.reader.ui.AppTheme.colorScheme.outlineVariant,
  )
}

@Composable
internal fun SettingsSwitchItem(
  title: String,
  subtitle: String? = null,
  checked: Boolean,
  onValueChanged: (Boolean) -> Unit,
) {
  SettingItem(
    title = title,
    subtitle = subtitle,
    action = { Switch(checked = checked, onCheckedChange = onValueChanged) },
    onClick = { onValueChanged(!checked) },
  )
}
