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

public val TwineIcons.FormatLineSpacing: ImageVector
  get() {
    if (_formatLineSpacingFilled != null) {
      return _formatLineSpacingFilled!!
    }
    _formatLineSpacingFilled =
      ImageVector.Builder(
          name = "FormatLineSpacingFilled",
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
              /* pathData = "m200-646-36 35q-11 11-27.5 11T108-612q-11-11-11-28t11-28l104-104q6-6 13-8.5t15-2.5q8 0 15 2.5t13 8.5l104 104q11 11 11.5 27.5T372-612q-11 11-27.5 11.5T316-611l-36-35v332l36-35q11-11 27.5-11t28.5 12q11 11 11 28t-11 28L268-188q-6 6-13 8.5t-15 2.5q-8 0-15-2.5t-13-8.5L108-292q-11-11-11.5-27.5T108-348q11-11 27.5-11.5T164-349l36 35v-332Zm320 446q-17 0-28.5-11.5T480-240q0-17 11.5-28.5T520-280h320q17 0 28.5 11.5T880-240q0 17-11.5 28.5T840-200H520Zm0-240q-17 0-28.5-11.5T480-480q0-17 11.5-28.5T520-520h320q17 0 28.5 11.5T880-480q0 17-11.5 28.5T840-440H520Zm0-240q-17 0-28.5-11.5T480-720q0-17 11.5-28.5T520-760h320q17 0 28.5 11.5T880-720q0 17-11.5 28.5T840-680H520Z" */
              moveToRelative(200.0f, -646.0f)
              lineToRelative(-36.0f, 35.0f)
              quadToRelative(-11.0f, 11.0f, -27.5f, 11.0f)
              reflectiveQuadTo(108.0f, -612.0f)
              quadToRelative(-11.0f, -11.0f, -11.0f, -28.0f)
              reflectiveQuadToRelative(11.0f, -28.0f)
              lineToRelative(104.0f, -104.0f)
              quadToRelative(6.0f, -6.0f, 13.0f, -8.5f)
              reflectiveQuadToRelative(15.0f, -2.5f)
              quadToRelative(8.0f, 0.0f, 15.0f, 2.5f)
              reflectiveQuadToRelative(13.0f, 8.5f)
              lineToRelative(104.0f, 104.0f)
              quadToRelative(11.0f, 11.0f, 11.5f, 27.5f)
              reflectiveQuadTo(372.0f, -612.0f)
              quadToRelative(-11.0f, 11.0f, -27.5f, 11.5f)
              reflectiveQuadTo(316.0f, -611.0f)
              lineToRelative(-36.0f, -35.0f)
              verticalLineToRelative(332.0f)
              lineToRelative(36.0f, -35.0f)
              quadToRelative(11.0f, -11.0f, 27.5f, -11.0f)
              reflectiveQuadToRelative(28.5f, 12.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              lineTo(268.0f, -188.0f)
              quadToRelative(-6.0f, 6.0f, -13.0f, 8.5f)
              reflectiveQuadToRelative(-15.0f, 2.5f)
              quadToRelative(-8.0f, 0.0f, -15.0f, -2.5f)
              reflectiveQuadToRelative(-13.0f, -8.5f)
              lineTo(108.0f, -292.0f)
              quadToRelative(-11.0f, -11.0f, -11.5f, -27.5f)
              reflectiveQuadTo(108.0f, -348.0f)
              quadToRelative(11.0f, -11.0f, 27.5f, -11.5f)
              reflectiveQuadTo(164.0f, -349.0f)
              lineToRelative(36.0f, 35.0f)
              verticalLineToRelative(-332.0f)
              close()
              moveToRelative(320.0f, 446.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(480.0f, -240.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(520.0f, -280.0f)
              horizontalLineToRelative(320.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(880.0f, -240.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(840.0f, -200.0f)
              horizontalLineTo(520.0f)
              close()
              moveToRelative(0.0f, -240.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(480.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(520.0f, -520.0f)
              horizontalLineToRelative(320.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(880.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(840.0f, -440.0f)
              horizontalLineTo(520.0f)
              close()
              moveToRelative(0.0f, -240.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(480.0f, -720.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(520.0f, -760.0f)
              horizontalLineToRelative(320.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(880.0f, -720.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(840.0f, -680.0f)
              horizontalLineTo(520.0f)
              close()
            }
          }
        }
        .build()
    return _formatLineSpacingFilled!!
  }

private var _formatLineSpacingFilled: ImageVector? = null
