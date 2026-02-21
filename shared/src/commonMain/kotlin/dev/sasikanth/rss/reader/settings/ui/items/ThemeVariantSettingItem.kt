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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.ThemeVariantIconButton
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant

@Composable
internal fun ThemeVariantSettingItem(
  selectedThemeVariant: ThemeVariant,
  isSubscribed: Boolean,
  useDarkTheme: Boolean,
  onThemeVariantChanged: (ThemeVariant) -> Unit,
  modifier: Modifier = Modifier,
) {
  val themeVariantListState =
    rememberLazyListState(
      initialFirstVisibleItemIndex = ThemeVariant.entries.indexOf(selectedThemeVariant)
    )

  LazyRow(
    state = themeVariantListState,
    modifier = modifier.fillMaxWidth(),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    items(ThemeVariant.entries) { themeVariant ->
      ThemeVariantIconButton(
        themeVariant = themeVariant,
        selected = themeVariant == selectedThemeVariant,
        isSubscribed = isSubscribed,
        useDarkTheme = useDarkTheme,
        onClick = { onThemeVariantChanged(themeVariant) },
      )
    }
  }
}
