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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun BottomBarWithGradientShadow(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(modifier = modifier) {
    Box(
      Modifier.fillMaxWidth()
        .requiredHeight(120.dp)
        .offset(y = 24.dp)
        .background(
          Brush.verticalGradient(
            colorStops =
              arrayOf(
                0f to Color.Transparent,
                0.5f to AppTheme.colorScheme.tintedBackground.copy(alpha = 0.5f),
                1f to AppTheme.colorScheme.tintedBackground
              )
          )
        )
    )

    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            color = AppTheme.colorScheme.tintedSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
          )
          .pointerInput(Unit) {
            // Consume bottom bar taps
          }
          .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
      content()
    }
  }
}
