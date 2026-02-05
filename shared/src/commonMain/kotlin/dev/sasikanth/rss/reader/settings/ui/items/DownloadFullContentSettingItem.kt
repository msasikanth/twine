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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.ignoreHorizontalParentPadding
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsDownloadFullContentSubtitle
import twine.shared.generated.resources.settingsDownloadFullContentTitle
import twine.shared.generated.resources.settingsDownloadFullContentWarning

@Composable
internal fun DownloadFullContentSettingItem(
  downloadFullContent: Boolean,
  onValueChanged: (Boolean) -> Unit,
) {
  val translucentStyles = LocalTranslucentStyles.current
  Column(
    modifier =
      Modifier.clickable { onValueChanged(!downloadFullContent) }
        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          stringResource(Res.string.settingsDownloadFullContentTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
        Text(
          stringResource(Res.string.settingsDownloadFullContentSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(
        checked = downloadFullContent,
        onCheckedChange = { checked -> onValueChanged(checked) },
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      modifier =
        Modifier.ignoreHorizontalParentPadding(horizontal = 12.dp)
          .background(
            AppTheme.colorScheme.error.copy(alpha = translucentStyles.default.background.alpha),
            MaterialTheme.shapes.small,
          )
          .border(1.dp, AppTheme.colorScheme.error, MaterialTheme.shapes.small)
          .padding(horizontal = 12.dp, vertical = 8.dp),
      text = stringResource(Res.string.settingsDownloadFullContentWarning),
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.error,
    )
  }
}
