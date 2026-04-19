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

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.data.repository.AudioMarkAsReadThreshold
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.audioMarkAsReadThreshold100
import twine.shared.generated.resources.audioMarkAsReadThreshold25
import twine.shared.generated.resources.audioMarkAsReadThreshold50
import twine.shared.generated.resources.audioMarkAsReadThreshold75
import twine.shared.generated.resources.audioMarkAsReadThreshold90
import twine.shared.generated.resources.audioMarkAsReadThresholdSubtitle
import twine.shared.generated.resources.audioMarkAsReadThresholdTitle

@Composable
internal fun AudioMarkAsReadThresholdSettingItem(
  threshold: AudioMarkAsReadThreshold,
  onThresholdChanged: (AudioMarkAsReadThreshold) -> Unit,
  modifier: Modifier = Modifier,
) {
  var showDropdown by remember { mutableStateOf(false) }

  SettingItem(
    modifier = modifier,
    title = stringResource(Res.string.audioMarkAsReadThresholdTitle),
    subtitle = stringResource(Res.string.audioMarkAsReadThresholdSubtitle),
    onClick = { showDropdown = !showDropdown },
    action = {
      Box {
        TranslucentButton(
          text = threshold.label(),
          trailingIcon = TwineIcons.ArrowDown,
          onClick = { showDropdown = true },
        )

        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
          AudioMarkAsReadThreshold.entries.forEach { thresholdOption ->
            DropdownMenuItem(
              text = thresholdOption.label(),
              selected = thresholdOption == threshold,
              onClick = {
                onThresholdChanged(thresholdOption)
                showDropdown = false
              },
            )
          }
        }
      }
    },
  )
}

@Composable
private fun AudioMarkAsReadThreshold.label(): String {
  return when (this) {
    AudioMarkAsReadThreshold.TwentyFive -> stringResource(Res.string.audioMarkAsReadThreshold25)
    AudioMarkAsReadThreshold.Fifty -> stringResource(Res.string.audioMarkAsReadThreshold50)
    AudioMarkAsReadThreshold.SeventyFive -> stringResource(Res.string.audioMarkAsReadThreshold75)
    AudioMarkAsReadThreshold.Ninety -> stringResource(Res.string.audioMarkAsReadThreshold90)
    AudioMarkAsReadThreshold.Hundred -> stringResource(Res.string.audioMarkAsReadThreshold100)
  }
}
