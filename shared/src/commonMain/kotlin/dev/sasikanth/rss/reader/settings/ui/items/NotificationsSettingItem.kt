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

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.Switch
import dev.sasikanth.rss.reader.platform.LocalLinkHandler
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalTranslucentStyles
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.ignoreHorizontalParentPadding
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsEnableNotificationsSubtitle
import twine.shared.generated.resources.settingsEnableNotificationsTitle
import twine.shared.generated.resources.settingsNotificationWarningAndroid

@Composable
internal fun NotificationsSettingItem(
  enableNotifications: Boolean,
  onValueChanged: (Boolean) -> Unit,
) {
  val translucentStyles = LocalTranslucentStyles.current
  val linkHandler = LocalLinkHandler.current
  val coroutineScope = rememberCoroutineScope()

  Column(
    modifier =
      Modifier.clickable { onValueChanged(!enableNotifications) }
        .padding(start = 24.dp, top = 16.dp, end = 24.dp, bottom = 20.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = stringResource(Res.string.settingsEnableNotificationsTitle),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
        Text(
          text = stringResource(Res.string.settingsEnableNotificationsSubtitle),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.textEmphasisMed,
        )
      }

      Spacer(Modifier.width(16.dp))

      Switch(checked = enableNotifications, onCheckedChange = onValueChanged)
    }

    AnimatedVisibility(
      modifier = Modifier.ignoreHorizontalParentPadding(horizontal = 12.dp),
      visible = platform == Platform.Android && enableNotifications,
    ) {
      Column {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
          modifier =
            Modifier.clip(MaterialTheme.shapes.small)
              .clickable {
                coroutineScope.launch { linkHandler.openLink(Constants.DONT_KILL_MY_APP_LINK) }
              }
              .background(translucentStyles.default.background, MaterialTheme.shapes.small)
              .border(1.dp, AppTheme.colorScheme.secondary, MaterialTheme.shapes.small)
              .padding(horizontal = 12.dp, vertical = 8.dp),
          text = stringResource(Res.string.settingsNotificationWarningAndroid),
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.secondary,
        )
      }
    }
  }
}
