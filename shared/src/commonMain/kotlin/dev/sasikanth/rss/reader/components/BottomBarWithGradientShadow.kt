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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.ui.AppTheme

@Composable
internal fun BottomBarWithGradientShadow(
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .heightIn(min = 120.dp)
        .dropShadow(shape = shape) {
          radius = 4.dp.toPx()
          brush =
            Brush.verticalGradient(
              colorStops = arrayOf(0f to Color.Transparent, 0.8f to Color.Black, 1f to Color.Black)
            )
          offset = Offset(0f, -4.dp.toPx())
        }
        .background(color = AppTheme.colorScheme.backdrop, shape = shape)
        .pointerInput(Unit) {
          // Consume bottom bar taps
        }
        .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    content()
  }
}
