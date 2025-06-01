/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.placeholder

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.databaseMaintainenceSubtitle
import twine.shared.generated.resources.databaseMaintainenceTitle
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlaceholderScreen(modifier: Modifier = Modifier) {
  var isVisible by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    delay(3.seconds)
    isVisible = true
  }

  Box(
    modifier =
      modifier.background(AppTheme.colorScheme.surfaceContainerLowest).padding(horizontal = 24.dp),
    contentAlignment = Alignment.Center
  ) {
    AnimatedVisibility(
      visible = isVisible,
      enter = fadeIn(),
      exit = fadeOut(),
    ) {
      Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        CircularProgressIndicator(
          color = AppTheme.colorScheme.tintedForeground,
          modifier = Modifier.requiredSize(24.dp),
          strokeWidth = 2.dp
        )

        Spacer(Modifier.requiredHeight(4.dp))

        Text(
          text = stringResource(Res.string.databaseMaintainenceTitle),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.titleLarge,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )

        Text(
          text = stringResource(Res.string.databaseMaintainenceSubtitle),
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
      }
    }
  }
}
