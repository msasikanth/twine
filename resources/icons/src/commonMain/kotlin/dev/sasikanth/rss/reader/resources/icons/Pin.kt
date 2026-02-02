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

val TwineIcons.Pin: ImageVector
  get() {
    if (pin != null) {
      return pin!!
    }
    pin =
      Builder(
          name = "Pin",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(14.0f, 4.0f)
            verticalLineToRelative(5.0f)
            curveToRelative(0.0f, 1.12f, 0.37f, 2.16f, 1.0f, 3.0f)
            horizontalLineTo(9.0f)
            curveToRelative(0.65f, -0.86f, 1.0f, -1.9f, 1.0f, -3.0f)
            verticalLineTo(4.0f)
            horizontalLineTo(14.0f)
            moveTo(17.0f, 2.0f)
            horizontalLineTo(7.0f)
            curveTo(6.45f, 2.0f, 6.0f, 2.45f, 6.0f, 3.0f)
            curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
            curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            lineToRelative(1.0f, 0.0f)
            verticalLineToRelative(5.0f)
            curveToRelative(0.0f, 1.66f, -1.34f, 3.0f, -3.0f, 3.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(5.97f)
            verticalLineToRelative(7.0f)
            lineToRelative(1.0f, 1.0f)
            lineToRelative(1.0f, -1.0f)
            verticalLineToRelative(-7.0f)
            horizontalLineTo(19.0f)
            verticalLineToRelative(-2.0f)
            curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            curveToRelative(-1.66f, 0.0f, -3.0f, -1.34f, -3.0f, -3.0f)
            verticalLineTo(4.0f)
            lineToRelative(1.0f, 0.0f)
            curveToRelative(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
            curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
            curveTo(18.0f, 2.45f, 17.55f, 2.0f, 17.0f, 2.0f)
            lineTo(17.0f, 2.0f)
            close()
          }
        }
        .build()
    return pin!!
  }

private var pin: ImageVector? = null
