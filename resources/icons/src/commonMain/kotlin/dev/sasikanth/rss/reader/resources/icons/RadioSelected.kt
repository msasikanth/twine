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
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.RadioSelected: ImageVector
  get() {
    if (radioSelected != null) {
      return radioSelected!!
    }
    radioSelected =
      Builder(
          name = "RadioSelected",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF75DAA3)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(10.6f, 16.5998f)
            lineTo(17.65f, 9.5498f)
            lineTo(16.25f, 8.1498f)
            lineTo(10.6f, 13.7998f)
            lineTo(7.75f, 10.9498f)
            lineTo(6.35f, 12.3498f)
            lineTo(10.6f, 16.5998f)
            close()
            moveTo(12.0f, 21.9998f)
            curveTo(6.545f, 21.9998f, 2.0f, 17.4547f, 2.0f, 11.9998f)
            curveTo(2.0f, 6.5448f, 6.545f, 1.9998f, 12.0f, 1.9998f)
            curveTo(17.455f, 1.9998f, 22.0f, 6.5448f, 22.0f, 11.9998f)
            curveTo(22.0f, 17.4547f, 17.455f, 21.9998f, 12.0f, 21.9998f)
            close()
          }
        }
        .build()
    return radioSelected!!
  }

private var radioSelected: ImageVector? = null
