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

public val TwineIcons.DarkMode: ImageVector
  get() {
    if (_darkMode != null) {
      return _darkMode!!
    }
    _darkMode =
      ImageVector.Builder(
          name = "DarkMode",
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
              /* pathData = "M480-120q-151 0-255.5-104.5T120-480q0-138 90-239.5T440-838q13-2 23 3.5t16 14.5q6 9 6.5 21t-7.5 23q-17 26-25.5 55t-8.5 61q0 90 63 153t153 63q31 0 61.5-9t54.5-25q11-7 22.5-6.5T819-479q10 5 15.5 15t3.5 24q-14 138-117.5 229T480-120Zm0-80q88 0 158-48.5T740-375q-20 5-40 8t-40 3q-123 0-209.5-86.5T364-660q0-20 3-40t8-40q-78 32-126.5 102T200-480q0 116 82 198t198 82Zm-10-270Z" */
              moveTo(480.0f, -120.0f)
              quadToRelative(-151.0f, 0.0f, -255.5f, -104.5f)
              reflectiveQuadTo(120.0f, -480.0f)
              quadToRelative(0.0f, -138.0f, 90.0f, -239.5f)
              reflectiveQuadTo(440.0f, -838.0f)
              quadToRelative(13.0f, -2.0f, 23.0f, 3.5f)
              reflectiveQuadToRelative(16.0f, 14.5f)
              quadToRelative(6.0f, 9.0f, 6.5f, 21.0f)
              reflectiveQuadToRelative(-7.5f, 23.0f)
              quadToRelative(-17.0f, 26.0f, -25.5f, 55.0f)
              reflectiveQuadToRelative(-8.5f, 61.0f)
              quadToRelative(0.0f, 90.0f, 63.0f, 153.0f)
              reflectiveQuadToRelative(153.0f, 63.0f)
              quadToRelative(31.0f, 0.0f, 61.5f, -9.0f)
              reflectiveQuadToRelative(54.5f, -25.0f)
              quadToRelative(11.0f, -7.0f, 22.5f, -6.5f)
              reflectiveQuadTo(819.0f, -479.0f)
              quadToRelative(10.0f, 5.0f, 15.5f, 15.0f)
              reflectiveQuadToRelative(3.5f, 24.0f)
              quadToRelative(-14.0f, 138.0f, -117.5f, 229.0f)
              reflectiveQuadTo(480.0f, -120.0f)
              close()
              moveToRelative(0.0f, -80.0f)
              quadToRelative(88.0f, 0.0f, 158.0f, -48.5f)
              reflectiveQuadTo(740.0f, -375.0f)
              quadToRelative(-20.0f, 5.0f, -40.0f, 8.0f)
              reflectiveQuadToRelative(-40.0f, 3.0f)
              quadToRelative(-123.0f, 0.0f, -209.5f, -86.5f)
              reflectiveQuadTo(364.0f, -660.0f)
              quadToRelative(0.0f, -20.0f, 3.0f, -40.0f)
              reflectiveQuadToRelative(8.0f, -40.0f)
              quadToRelative(-78.0f, 32.0f, -126.5f, 102.0f)
              reflectiveQuadTo(200.0f, -480.0f)
              quadToRelative(0.0f, 116.0f, 82.0f, 198.0f)
              reflectiveQuadToRelative(198.0f, 82.0f)
              close()
              moveToRelative(-10.0f, -270.0f)
              close()
            }
          }
        }
        .build()
    return _darkMode!!
  }

private var _darkMode: ImageVector? = null
