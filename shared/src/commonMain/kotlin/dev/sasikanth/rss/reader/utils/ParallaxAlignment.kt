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

package dev.sasikanth.rss.reader.utils

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.roundToInt

// source: https://chrisbanes.me/posts/parallax-effect-compose/#alignmentparallax
internal class ParallaxAlignment(
  private val horizontalBias: () -> Float,
  private val verticalBias: Float = 0f,
  private val multiplier: Float = 1f,
) : Alignment {
  override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
    val horizontalBiasValue = horizontalBias() * multiplier
    val centerX = (space.width - size.width).toFloat() / 2f
    val centerY = (space.height - size.height).toFloat() / 2f
    val resolvedHorizontalBias =
      if (layoutDirection == LayoutDirection.Ltr) horizontalBiasValue else -horizontalBiasValue

    val x = centerX * (1 + resolvedHorizontalBias)
    val y = centerY * (1 + verticalBias)
    return IntOffset(x.roundToInt(), y.roundToInt())
  }
}
