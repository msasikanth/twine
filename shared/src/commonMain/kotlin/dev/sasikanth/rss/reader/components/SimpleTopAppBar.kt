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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.ArrowBack
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.buttonGoBack

@Composable
fun SimpleTopAppBar(
  title: String,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  actions: @Composable RowScope.() -> Unit = {},
) {
  Box(modifier = modifier) {
    TopAppBar(
      title = {
        Text(
          modifier = Modifier.padding(start = 12.dp),
          text = title,
          color = AppTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.SemiBold,
        )
      },
      navigationIcon = {
        CircularIconButton(
          modifier = Modifier.padding(start = 12.dp),
          icon = TwineIcons.ArrowBack,
          label = stringResource(Res.string.buttonGoBack),
          onClick = onBackClick,
        )
      },
      actions = actions,
      contentPadding = PaddingValues(vertical = 8.dp),
      colors =
        TopAppBarDefaults.topAppBarColors(
          containerColor = AppTheme.colorScheme.backdrop,
          navigationIconContentColor = AppTheme.colorScheme.onSurface,
          titleContentColor = AppTheme.colorScheme.onSurface,
          actionIconContentColor = AppTheme.colorScheme.onSurface,
        ),
    )

    HorizontalDivider(
      modifier = Modifier.fillMaxWidth().align(Alignment.BottomStart),
      color = AppTheme.colorScheme.outlineVariant,
    )
  }
}
