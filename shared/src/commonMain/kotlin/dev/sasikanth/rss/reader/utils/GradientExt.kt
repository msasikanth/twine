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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private const val FADE_STOPS = 12

/**
 * Vertical fade from [color] to transparent whose alpha follows a smootherstep curve instead of a
 * straight line. The curve is flat at both ends, so neither the opaque edge nor the transparent
 * edge shows the crease a linear ramp leaves behind.
 *
 * [solidFraction] keeps the first portion of the gradient fully opaque before the fade starts.
 */
fun smoothVerticalFade(
  color: Color,
  startY: Float = 0f,
  endY: Float,
  solidFraction: Float = 0f,
  reversed: Boolean = false,
): Brush {
  val clampedSolid = solidFraction.coerceIn(0f, 1f)
  val fadeSpan = 1f - clampedSolid
  val stops = ArrayList<Pair<Float, Color>>(FADE_STOPS + 1)

  if (clampedSolid > 0f) {
    stops.add(0f to color)
  }

  for (step in 0..FADE_STOPS) {
    val t = step / FADE_STOPS.toFloat()
    stops.add((clampedSolid + (t * fadeSpan)) to color.copy(alpha = 1f - smootherStep(t)))
  }

  val orderedStops =
    if (reversed) {
      stops.map { (position, stopColor) -> (1f - position) to stopColor }.asReversed()
    } else {
      stops
    }

  return Brush.verticalGradient(
    colorStops = orderedStops.toTypedArray(),
    startY = startY,
    endY = endY,
  )
}

private fun smootherStep(t: Float): Float = t * t * t * (t * (t * 6f - 15f) + 10f)
