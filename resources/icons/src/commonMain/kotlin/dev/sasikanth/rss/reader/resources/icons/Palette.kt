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

public val TwineIcons.Palette: ImageVector
  get() {
    if (_palette != null) {
      return _palette!!
    }
    _palette =
      ImageVector.Builder(
          name = "Palette",
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
              /* pathData = "M480-80q-82 0-155-31.5t-127.5-86Q143-252 111.5-325T80-480q0-83 32.5-156t88-127Q256-817 330-848.5T488-880q80 0 151 27.5t124.5 76q53.5 48.5 85 115T880-518q0 115-70 176.5T640-280h-74q-9 0-12.5 5t-3.5 11q0 12 15 34.5t15 51.5q0 50-27.5 74T480-80Zm0-400Zm-177 23q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17Zm120-160q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17Zm200 0q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17Zm120 160q17-17 17-43t-17-43q-17-17-43-17t-43 17q-17 17-17 43t17 43q17 17 43 17t43-17ZM480-160q9 0 14.5-5t5.5-13q0-14-15-33t-15-57q0-42 29-67t71-25h70q66 0 113-38.5T800-518q0-121-92.5-201.5T488-800q-136 0-232 93t-96 227q0 133 93.5 226.5T480-160Z" */
              moveTo(480.0f, -80.0f)
              quadToRelative(-82.0f, 0.0f, -155.0f, -31.5f)
              reflectiveQuadToRelative(-127.5f, -86.0f)
              quadTo(143.0f, -252.0f, 111.5f, -325.0f)
              reflectiveQuadTo(80.0f, -480.0f)
              quadToRelative(0.0f, -83.0f, 32.5f, -156.0f)
              reflectiveQuadToRelative(88.0f, -127.0f)
              quadTo(256.0f, -817.0f, 330.0f, -848.5f)
              reflectiveQuadTo(488.0f, -880.0f)
              quadToRelative(80.0f, 0.0f, 151.0f, 27.5f)
              reflectiveQuadToRelative(124.5f, 76.0f)
              quadToRelative(53.5f, 48.5f, 85.0f, 115.0f)
              reflectiveQuadTo(880.0f, -518.0f)
              quadToRelative(0.0f, 115.0f, -70.0f, 176.5f)
              reflectiveQuadTo(640.0f, -280.0f)
              horizontalLineToRelative(-74.0f)
              quadToRelative(-9.0f, 0.0f, -12.5f, 5.0f)
              reflectiveQuadToRelative(-3.5f, 11.0f)
              quadToRelative(0.0f, 12.0f, 15.0f, 34.5f)
              reflectiveQuadToRelative(15.0f, 51.5f)
              quadToRelative(0.0f, 50.0f, -27.5f, 74.0f)
              reflectiveQuadTo(480.0f, -80.0f)
              close()
              moveToRelative(0.0f, -400.0f)
              close()
              moveToRelative(-177.0f, 23.0f)
              quadToRelative(17.0f, -17.0f, 17.0f, -43.0f)
              reflectiveQuadToRelative(-17.0f, -43.0f)
              quadToRelative(-17.0f, -17.0f, -43.0f, -17.0f)
              reflectiveQuadToRelative(-43.0f, 17.0f)
              quadToRelative(-17.0f, 17.0f, -17.0f, 43.0f)
              reflectiveQuadToRelative(17.0f, 43.0f)
              quadToRelative(17.0f, 17.0f, 43.0f, 17.0f)
              reflectiveQuadToRelative(43.0f, -17.0f)
              close()
              moveToRelative(120.0f, -160.0f)
              quadToRelative(17.0f, -17.0f, 17.0f, -43.0f)
              reflectiveQuadToRelative(-17.0f, -43.0f)
              quadToRelative(-17.0f, -17.0f, -43.0f, -17.0f)
              reflectiveQuadToRelative(-43.0f, 17.0f)
              quadToRelative(-17.0f, 17.0f, -17.0f, 43.0f)
              reflectiveQuadToRelative(17.0f, 43.0f)
              quadToRelative(17.0f, 17.0f, 43.0f, 17.0f)
              reflectiveQuadToRelative(43.0f, -17.0f)
              close()
              moveToRelative(200.0f, 0.0f)
              quadToRelative(17.0f, -17.0f, 17.0f, -43.0f)
              reflectiveQuadToRelative(-17.0f, -43.0f)
              quadToRelative(-17.0f, -17.0f, -43.0f, -17.0f)
              reflectiveQuadToRelative(-43.0f, 17.0f)
              quadToRelative(-17.0f, 17.0f, -17.0f, 43.0f)
              reflectiveQuadToRelative(17.0f, 43.0f)
              quadToRelative(17.0f, 17.0f, 43.0f, 17.0f)
              reflectiveQuadToRelative(43.0f, -17.0f)
              close()
              moveToRelative(120.0f, 160.0f)
              quadToRelative(17.0f, -17.0f, 17.0f, -43.0f)
              reflectiveQuadToRelative(-17.0f, -43.0f)
              quadToRelative(-17.0f, -17.0f, -43.0f, -17.0f)
              reflectiveQuadToRelative(-43.0f, 17.0f)
              quadToRelative(-17.0f, 17.0f, -17.0f, 43.0f)
              reflectiveQuadToRelative(17.0f, 43.0f)
              quadToRelative(17.0f, 17.0f, 43.0f, 17.0f)
              reflectiveQuadToRelative(43.0f, -17.0f)
              close()
              moveTo(480.0f, -160.0f)
              quadToRelative(9.0f, 0.0f, 14.5f, -5.0f)
              reflectiveQuadToRelative(5.5f, -13.0f)
              quadToRelative(0.0f, -14.0f, -15.0f, -33.0f)
              reflectiveQuadToRelative(-15.0f, -57.0f)
              quadToRelative(0.0f, -42.0f, 29.0f, -67.0f)
              reflectiveQuadToRelative(71.0f, -25.0f)
              horizontalLineToRelative(70.0f)
              quadToRelative(66.0f, 0.0f, 113.0f, -38.5f)
              reflectiveQuadTo(800.0f, -518.0f)
              quadToRelative(0.0f, -121.0f, -92.5f, -201.5f)
              reflectiveQuadTo(488.0f, -800.0f)
              quadToRelative(-136.0f, 0.0f, -232.0f, 93.0f)
              reflectiveQuadToRelative(-96.0f, 227.0f)
              quadToRelative(0.0f, 133.0f, 93.5f, 226.5f)
              reflectiveQuadTo(480.0f, -160.0f)
              close()
            }
          }
        }
        .build()
    return _palette!!
  }

private var _palette: ImageVector? = null
