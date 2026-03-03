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
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.ArrowBackDefault: ImageVector
  get() {
    if (_arrowBackFilled != null) {
      return _arrowBackFilled!!
    }
    _arrowBackFilled =
      ImageVector.Builder(
          name = "ArrowBackFilled",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f,
        )
        .apply {
          group(translationX = -0.0f, translationY = 960.0f) {
            path(
              fill = SolidColor(Color(0xFF000000)),
              stroke = null,
              strokeLineWidth = 0.0f,
              strokeLineCap = Butt,
              strokeLineJoin = Miter,
              strokeLineMiter = 4.0f,
              pathFillType = NonZero,
            ) {
              /* pathData = "m313-440 196 196q12 12 11.5 28T508-188q-12 11-28 11.5T452-188L188-452q-6-6-8.5-13t-2.5-15q0-8 2.5-15t8.5-13l264-264q11-11 27.5-11t28.5 11q12 12 12 28.5T508-715L313-520h447q17 0 28.5 11.5T800-480q0 17-11.5 28.5T760-440H313Z" */
              moveToRelative(313.0f, -440.0f)
              lineToRelative(196.0f, 196.0f)
              quadToRelative(12.0f, 12.0f, 11.5f, 28.0f)
              reflectiveQuadTo(508.0f, -188.0f)
              quadToRelative(-12.0f, 11.0f, -28.0f, 11.5f)
              reflectiveQuadTo(452.0f, -188.0f)
              lineTo(188.0f, -452.0f)
              quadToRelative(-6.0f, -6.0f, -8.5f, -13.0f)
              reflectiveQuadToRelative(-2.5f, -15.0f)
              quadToRelative(0.0f, -8.0f, 2.5f, -15.0f)
              reflectiveQuadToRelative(8.5f, -13.0f)
              lineToRelative(264.0f, -264.0f)
              quadToRelative(11.0f, -11.0f, 27.5f, -11.0f)
              reflectiveQuadToRelative(28.5f, 11.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              reflectiveQuadTo(508.0f, -715.0f)
              lineTo(313.0f, -520.0f)
              horizontalLineToRelative(447.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(800.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(760.0f, -440.0f)
              horizontalLineTo(313.0f)
              close()
            }
          }
        }
        .build()
    return _arrowBackFilled!!
  }

private var _arrowBackFilled: ImageVector? = null
