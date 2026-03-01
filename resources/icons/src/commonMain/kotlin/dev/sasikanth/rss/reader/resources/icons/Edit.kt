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

public val TwineIcons.Edit: ImageVector
  get() {
    if (_edit != null) {
      return _edit!!
    }
    _edit =
      ImageVector.Builder(
          name = "Edit",
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
              /* pathData = "M200-200h57l391-391-57-57-391 391v57Zm-40 80q-17 0-28.5-11.5T120-160v-97q0-16 6-30.5t17-25.5l505-504q12-11 26.5-17t30.5-6q16 0 31 6t26 18l55 56q12 11 17.5 26t5.5 30q0 16-5.5 30.5T817-647L313-143q-11 11-25.5 17t-30.5 6h-97Zm600-584-56-56 56 56Zm-141 85-28-29 57 57-29-28Z" */
              moveTo(200.0f, -200.0f)
              horizontalLineToRelative(57.0f)
              lineToRelative(391.0f, -391.0f)
              lineToRelative(-57.0f, -57.0f)
              lineToRelative(-391.0f, 391.0f)
              verticalLineToRelative(57.0f)
              close()
              moveToRelative(-40.0f, 80.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(120.0f, -160.0f)
              verticalLineToRelative(-97.0f)
              quadToRelative(0.0f, -16.0f, 6.0f, -30.5f)
              reflectiveQuadToRelative(17.0f, -25.5f)
              lineToRelative(505.0f, -504.0f)
              quadToRelative(12.0f, -11.0f, 26.5f, -17.0f)
              reflectiveQuadToRelative(30.5f, -6.0f)
              quadToRelative(16.0f, 0.0f, 31.0f, 6.0f)
              reflectiveQuadToRelative(26.0f, 18.0f)
              lineToRelative(55.0f, 56.0f)
              quadToRelative(12.0f, 11.0f, 17.5f, 26.0f)
              reflectiveQuadToRelative(5.5f, 30.0f)
              quadToRelative(0.0f, 16.0f, -5.5f, 30.5f)
              reflectiveQuadTo(817.0f, -647.0f)
              lineTo(313.0f, -143.0f)
              quadToRelative(-11.0f, 11.0f, -25.5f, 17.0f)
              reflectiveQuadToRelative(-30.5f, 6.0f)
              horizontalLineToRelative(-97.0f)
              close()
              moveToRelative(600.0f, -584.0f)
              lineToRelative(-56.0f, -56.0f)
              lineToRelative(56.0f, 56.0f)
              close()
              moveToRelative(-141.0f, 85.0f)
              lineToRelative(-28.0f, -29.0f)
              lineToRelative(57.0f, 57.0f)
              lineToRelative(-29.0f, -28.0f)
              close()
            }
          }
        }
        .build()
    return _edit!!
  }

private var _edit: ImageVector? = null
