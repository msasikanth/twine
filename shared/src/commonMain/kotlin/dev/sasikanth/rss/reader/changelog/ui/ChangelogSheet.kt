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

package dev.sasikanth.rss.reader.changelog.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.LocalMarkdownTypography
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.markdownPadding
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.changelogTitle
import twine.shared.generated.resources.changelogVersion

@Composable
internal fun ChangelogSheet(
  versionName: String,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var changelogContent by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) { changelogContent = Res.readBytes("files/changelog.md").decodeToString() }

  ModalBottomSheet(
    modifier = modifier,
    onDismissRequest = onDismiss,
    containerColor = AppTheme.colorScheme.surfaceContainerHighest,
    contentColor = AppTheme.colorScheme.onSurface,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .padding(top = 24.dp, bottom = 24.dp)
          .navigationBarsPadding()
    ) {
      Text(
        text = stringResource(Res.string.changelogTitle),
        style = MaterialTheme.typography.headlineMedium,
        color = AppTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
      )

      Text(
        text = stringResource(Res.string.changelogVersion, versionName),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.onSurfaceVariant,
      )

      Spacer(Modifier.requiredHeight(24.dp))

      changelogContent?.let { content ->
        val markdownColors =
          markdownColor(
            text = AppTheme.colorScheme.onSurface,
            codeBackground = AppTheme.colorScheme.onSurface.copy(alpha = 0.1f),
          )
        val markdownTypography =
          markdownTypography(
            h1 = MaterialTheme.typography.headlineMedium,
            h2 = MaterialTheme.typography.titleLarge,
            h3 = MaterialTheme.typography.titleMedium,
          )

        CompositionLocalProvider(
          LocalMarkdownPadding provides markdownPadding(block = 8.dp),
          LocalMarkdownColors provides markdownColors,
          LocalMarkdownTypography provides markdownTypography,
        ) {
          Markdown(
            content = content,
            colors = markdownColors,
            typography = markdownTypography,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }
  }
}
