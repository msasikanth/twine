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

package dev.sasikanth.rss.reader.main.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.keyboardShortcutsAddFeed
import twine.shared.generated.resources.keyboardShortcutsClose
import twine.shared.generated.resources.keyboardShortcutsMarkAsUnread
import twine.shared.generated.resources.keyboardShortcutsNextPost
import twine.shared.generated.resources.keyboardShortcutsOpenInBrowser
import twine.shared.generated.resources.keyboardShortcutsPreviousPost
import twine.shared.generated.resources.keyboardShortcutsRefresh
import twine.shared.generated.resources.keyboardShortcutsSearch
import twine.shared.generated.resources.keyboardShortcutsSectionGlobal
import twine.shared.generated.resources.keyboardShortcutsSectionReader
import twine.shared.generated.resources.keyboardShortcutsSettings
import twine.shared.generated.resources.keyboardShortcutsShowShortcuts
import twine.shared.generated.resources.keyboardShortcutsTitle
import twine.shared.generated.resources.keyboardShortcutsToggleBookmark

@Composable
internal fun KeyboardShortcutsSheet(onDismiss: () -> Unit) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerLowest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 8.dp, bottom = 48.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
      Text(
        text = stringResource(Res.string.keyboardShortcutsTitle),
        style = MaterialTheme.typography.titleLarge,
      )

      ShortcutSection(
        title = stringResource(Res.string.keyboardShortcutsSectionGlobal),
        shortcuts =
          listOf(
            "⌘R" to stringResource(Res.string.keyboardShortcutsRefresh),
            "⌘F" to stringResource(Res.string.keyboardShortcutsSearch),
            "⌘N" to stringResource(Res.string.keyboardShortcutsAddFeed),
            "⌘," to stringResource(Res.string.keyboardShortcutsSettings),
            "⌘/" to stringResource(Res.string.keyboardShortcutsShowShortcuts),
          ),
      )

      ShortcutSection(
        title = stringResource(Res.string.keyboardShortcutsSectionReader),
        shortcuts =
          listOf(
            "→" to stringResource(Res.string.keyboardShortcutsNextPost),
            "←" to stringResource(Res.string.keyboardShortcutsPreviousPost),
            "B" to stringResource(Res.string.keyboardShortcutsToggleBookmark),
            "U" to stringResource(Res.string.keyboardShortcutsMarkAsUnread),
            "V" to stringResource(Res.string.keyboardShortcutsOpenInBrowser),
            "Esc" to stringResource(Res.string.keyboardShortcutsClose),
          ),
      )
    }
  }
}

@Composable
private fun ShortcutSection(title: String, shortcuts: List<Pair<String, String>>) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelLarge,
      color = AppTheme.colorScheme.onSurfaceVariant,
    )

    shortcuts.forEach { (keys, label) -> ShortcutRow(keys = keys, label = label) }
  }
}

@Composable
private fun ShortcutRow(keys: String, label: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyLarge)

    Text(
      text = keys,
      style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontFamily.Monospace),
      color = AppTheme.colorScheme.onSurface,
      modifier =
        Modifier.clip(RoundedCornerShape(6.dp))
          .background(AppTheme.colorScheme.surfaceContainerHigh)
          .padding(vertical = 4.dp, horizontal = 8.dp),
    )
  }
}
