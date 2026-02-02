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

package dev.sasikanth.rss.reader.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

fun TwineIcons.allToPlus(progress: Float): ImageVector {
  return ImageVector.Builder(
      name = "all_to_plus",
      defaultWidth = 24.0.dp,
      defaultHeight = 24.0.dp,
      viewportWidth = 24.0f,
      viewportHeight = 24.0f,
    )
    .apply {
      group(name = "rotate", pivotX = 12f, pivotY = 12f, rotate = 180 * progress) {
        val dotProgress = ((0.16f - progress) / 0.16f).coerceIn(0.0f, 1.0f)

        group(
          name = "dots",
          pivotX = 12.0f,
          pivotY = 12.0f,
          scaleX = lerp(1f, 0.5f, 1f - dotProgress),
          scaleY = lerp(1f, 0.5f, 1f - dotProgress),
        ) {
          group(name = "dot_mov_01", translationX = -6f * dotProgress) {
            path(
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 6.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(12.0f, 12.0f)
              lineTo(12.0f, 12.0f)
            }
          }

          group(name = "dot_mov_02", translationY = -6f * dotProgress) {
            path(
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 6.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(12.0f, 12.0f)
              lineTo(12.0f, 12.0f)
            }
          }

          group(name = "dot_mov_03", translationX = 6f * dotProgress) {
            path(
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 6.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(12.0f, 12.0f)
              lineTo(12.0f, 12.0f)
            }
          }

          group(name = "dot_mov_04", translationY = 6f * dotProgress) {
            path(
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 6.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(12.0f, 12.0f)
              lineTo(12.0f, 12.0f)
            }
          }
        }

        if (progress >= 0.16f) {
          group(name = "plus", pivotX = 12.0f, pivotY = 12.0f) {
            val transformedMove = lerp(12.0f, 5.0f, progress)
            val transformedLine = lerp(12.0f, 19.0f, progress)

            path(
              name = "plus_horizontal",
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 2.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(transformedMove, 12.0f)
              lineTo(transformedLine, 12.0f)
            }

            path(
              name = "plus_vertical",
              stroke = SolidColor(Color(0xFF000000)),
              strokeLineWidth = 2.0f,
              strokeLineCap = StrokeCap.Round,
              strokeLineJoin = StrokeJoin.Round,
            ) {
              moveTo(12.0f, transformedMove)
              lineTo(12.0f, transformedLine)
            }
          }
        }
      }
    }
    .build()
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
  return start + fraction * (end - start)
}
