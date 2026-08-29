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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.app.AppIcon
import dev.sasikanth.rss.reader.components.AppIconButton
import dev.sasikanth.rss.reader.utils.rememberSelectionListState
import dev.sasikanth.rss.reader.utils.scrollOnMouseWheel

@Composable
internal fun AppIconSettingItem(
  selectedAppIcon: AppIcon,
  isSubscribed: Boolean,
  onAppIconChanged: (AppIcon) -> Unit,
  modifier: Modifier = Modifier,
) {
  val appIconListState = rememberSelectionListState(AppIcon.entries.indexOf(selectedAppIcon))

  LazyRow(
    state = appIconListState,
    modifier = modifier.fillMaxWidth().scrollOnMouseWheel(appIconListState),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.Top,
  ) {
    items(AppIcon.entries) { appIcon ->
      AppIconButton(
        appIcon = appIcon,
        selected = appIcon == selectedAppIcon,
        isSubscribed = isSubscribed,
        showLabel = true,
        onClick = { onAppIconChanged(appIcon) },
      )
    }
  }
}
