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

package dev.sasikanth.rss.reader.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val KeyboardArrowDown: ImageVector
  get() {
    if (_keyboardArrowDown != null) {
      return _keyboardArrowDown!!
    }
    _keyboardArrowDown =
      ImageVector.Builder(
          name = "KeyboardArrowDown",
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
              /* pathData = "M465-363.5q-7-2.5-13-8.5L268-556q-11-11-11-28t11-28q11-11 28-11t28 11l156 156 156-156q11-11 28-11t28 11q11 11 11 28t-11 28L508-372q-6 6-13 8.5t-15 2.5q-8 0-15-2.5Z" */
              moveTo(465.0f, -363.5f)
              quadToRelative(-7.0f, -2.5f, -13.0f, -8.5f)
              lineTo(268.0f, -556.0f)
              quadToRelative(-11.0f, -11.0f, -11.0f, -28.0f)
              reflectiveQuadToRelative(11.0f, -28.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              lineToRelative(156.0f, 156.0f)
              lineToRelative(156.0f, -156.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              lineTo(508.0f, -372.0f)
              quadToRelative(-6.0f, 6.0f, -13.0f, 8.5f)
              reflectiveQuadToRelative(-15.0f, 2.5f)
              quadToRelative(-8.0f, 0.0f, -15.0f, -2.5f)
              close()
            }
          }
        }
        .build()
    return _keyboardArrowDown!!
  }

private var _keyboardArrowDown: ImageVector? = null
