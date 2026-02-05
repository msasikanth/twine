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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsAmoledSubtitle
import twine.shared.generated.resources.settingsAmoledTitle

@Composable
internal fun AmoledSettingItem(useAmoled: Boolean, onValueChanged: (Boolean) -> Unit) {
  var checked by remember(useAmoled) { mutableStateOf(useAmoled) }
  Box(
    modifier =
      Modifier.clickable {
        checked = !checked
        onValueChanged(!useAmoled)
      }
  ) {
    Row(
      modifier = Modifier.padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsAmoledTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
        Text(
          stringResource(Res.string.settingsAmoledSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(checked = checked, onCheckedChange = { checked -> onValueChanged(checked) })
    }
  }
}
