/*
 * Copyright 2023 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ElevationTokens
import kotlinx.coroutines.launch

@Composable
internal fun BoxScope.NewArticlesScrollToTopButton(
  label: String,
  visible: Boolean,
  modifier: Modifier = Modifier,
  onClick: suspend () -> Unit
) {
  val coroutineScope = rememberCoroutineScope()
  AnimatedVisibility(
    visible = visible,
    enter = slideInVertically { it },
    exit = slideOutVertically { it },
    modifier = Modifier.align(Alignment.BottomCenter)
  ) {
    FilledTonalButton(
      modifier = modifier,
      colors =
        ButtonDefaults.filledTonalButtonColors(
          containerColor = AppTheme.colorScheme.tintedHighlight
        ),
      shape = MaterialTheme.shapes.medium,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      onClick = { coroutineScope.launch { onClick() } },
      elevation =
        ButtonDefaults.filledTonalButtonElevation(
          defaultElevation = ElevationTokens.Level3,
          hoveredElevation = ElevationTokens.Level5,
          pressedElevation = ElevationTokens.Level5,
        )
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisHigh
      )
    }
  }
}
