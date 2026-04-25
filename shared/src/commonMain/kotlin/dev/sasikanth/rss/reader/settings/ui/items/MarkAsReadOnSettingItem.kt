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
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.components.TranslucentButton
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.resources.icons.ArrowDown
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.markArticleAsRead
import twine.shared.generated.resources.markArticleAsReadOnOpen
import twine.shared.generated.resources.markArticleAsReadOnScroll

@Composable
internal fun MarkAsReadOnSettingItem(
  articleMarkAsReadOn: MarkAsReadOn,
  onMarkAsReadOnChanged: (MarkAsReadOn) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }

  SettingItem(
    title = stringResource(Res.string.markArticleAsRead),
    onClick = { showDropdown = !showDropdown },
    action = {
      Box {
        val markAsReadOnLabel =
          when (articleMarkAsReadOn) {
            MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
            MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
          }

        TranslucentButton(
          text = markAsReadOnLabel,
          trailingIcon = TwineIcons.ArrowDown,
          onClick = { showDropdown = true },
        )

        DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
          MarkAsReadOn.entries.forEach { markAsReadOn ->
            val label =
              when (markAsReadOn) {
                MarkAsReadOn.Open -> stringResource(Res.string.markArticleAsReadOnOpen)
                MarkAsReadOn.Scroll -> stringResource(Res.string.markArticleAsReadOnScroll)
              }

            DropdownMenuItem(
              text = label,
              selected = markAsReadOn == articleMarkAsReadOn,
              onClick = {
                onMarkAsReadOnChanged(markAsReadOn)
                showDropdown = false
              },
            )
          }
        }
      }
    },
  )
}
