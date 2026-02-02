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

public val TwineIcons.VisibilityOff: ImageVector
  get() {
    if (_visibilityOff != null) {
      return _visibilityOff!!
    }
    _visibilityOff =
      ImageVector.Builder(
          name = "VisibilityOff",
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
              /* pathData = "M607-627q29 29 42.5 66t9.5 76q0 15-11 25.5T622-449q-15 0-25.5-10.5T586-485q5-26-3-50t-25-41q-17-17-41-26t-51-4q-15 0-25.5-11T430-643q0-15 10.5-25.5T466-679q38-4 75 9.5t66 42.5Zm-127-93q-19 0-37 1.5t-36 5.5q-17 3-30.5-5T358-742q-5-16 3.5-31t24.5-18q23-5 46.5-7t47.5-2q137 0 250.5 72T904-534q4 8 6 16.5t2 17.5q0 9-1.5 17.5T905-466q-18 40-44.5 75T802-327q-12 11-28 9t-26-16q-10-14-8.5-30.5T753-392q24-23 44-50t35-58q-50-101-144.5-160.5T480-720Zm0 520q-134 0-245-72.5T60-463q-5-8-7.5-17.5T50-500q0-10 2-19t7-18q20-40 46.5-76.5T166-680l-83-84q-11-12-10.5-28.5T84-820q11-11 28-11t28 11l680 680q11 11 11.5 27.5T820-84q-11 11-28 11t-28-11L624-222q-35 11-71 16.5t-73 5.5ZM222-624q-29 26-53 57t-41 67q50 101 144.5 160.5T480-280q20 0 39-2.5t39-5.5l-36-38q-11 3-21 4.5t-21 1.5q-75 0-127.5-52.5T300-500q0-11 1.5-21t4.5-21l-84-82Zm319 93Zm-151 75Z" */
              moveTo(607.0f, -627.0f)
              quadToRelative(29.0f, 29.0f, 42.5f, 66.0f)
              reflectiveQuadToRelative(9.5f, 76.0f)
              quadToRelative(0.0f, 15.0f, -11.0f, 25.5f)
              reflectiveQuadTo(622.0f, -449.0f)
              quadToRelative(-15.0f, 0.0f, -25.5f, -10.5f)
              reflectiveQuadTo(586.0f, -485.0f)
              quadToRelative(5.0f, -26.0f, -3.0f, -50.0f)
              reflectiveQuadToRelative(-25.0f, -41.0f)
              quadToRelative(-17.0f, -17.0f, -41.0f, -26.0f)
              reflectiveQuadToRelative(-51.0f, -4.0f)
              quadToRelative(-15.0f, 0.0f, -25.5f, -11.0f)
              reflectiveQuadTo(430.0f, -643.0f)
              quadToRelative(0.0f, -15.0f, 10.5f, -25.5f)
              reflectiveQuadTo(466.0f, -679.0f)
              quadToRelative(38.0f, -4.0f, 75.0f, 9.5f)
              reflectiveQuadToRelative(66.0f, 42.5f)
              close()
              moveToRelative(-127.0f, -93.0f)
              quadToRelative(-19.0f, 0.0f, -37.0f, 1.5f)
              reflectiveQuadToRelative(-36.0f, 5.5f)
              quadToRelative(-17.0f, 3.0f, -30.5f, -5.0f)
              reflectiveQuadTo(358.0f, -742.0f)
              quadToRelative(-5.0f, -16.0f, 3.5f, -31.0f)
              reflectiveQuadToRelative(24.5f, -18.0f)
              quadToRelative(23.0f, -5.0f, 46.5f, -7.0f)
              reflectiveQuadToRelative(47.5f, -2.0f)
              quadToRelative(137.0f, 0.0f, 250.5f, 72.0f)
              reflectiveQuadTo(904.0f, -534.0f)
              quadToRelative(4.0f, 8.0f, 6.0f, 16.5f)
              reflectiveQuadToRelative(2.0f, 17.5f)
              quadToRelative(0.0f, 9.0f, -1.5f, 17.5f)
              reflectiveQuadTo(905.0f, -466.0f)
              quadToRelative(-18.0f, 40.0f, -44.5f, 75.0f)
              reflectiveQuadTo(802.0f, -327.0f)
              quadToRelative(-12.0f, 11.0f, -28.0f, 9.0f)
              reflectiveQuadToRelative(-26.0f, -16.0f)
              quadToRelative(-10.0f, -14.0f, -8.5f, -30.5f)
              reflectiveQuadTo(753.0f, -392.0f)
              quadToRelative(24.0f, -23.0f, 44.0f, -50.0f)
              reflectiveQuadToRelative(35.0f, -58.0f)
              quadToRelative(-50.0f, -101.0f, -144.5f, -160.5f)
              reflectiveQuadTo(480.0f, -720.0f)
              close()
              moveToRelative(0.0f, 520.0f)
              quadToRelative(-134.0f, 0.0f, -245.0f, -72.5f)
              reflectiveQuadTo(60.0f, -463.0f)
              quadToRelative(-5.0f, -8.0f, -7.5f, -17.5f)
              reflectiveQuadTo(50.0f, -500.0f)
              quadToRelative(0.0f, -10.0f, 2.0f, -19.0f)
              reflectiveQuadToRelative(7.0f, -18.0f)
              quadToRelative(20.0f, -40.0f, 46.5f, -76.5f)
              reflectiveQuadTo(166.0f, -680.0f)
              lineToRelative(-83.0f, -84.0f)
              quadToRelative(-11.0f, -12.0f, -10.5f, -28.5f)
              reflectiveQuadTo(84.0f, -820.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              lineToRelative(680.0f, 680.0f)
              quadToRelative(11.0f, 11.0f, 11.5f, 27.5f)
              reflectiveQuadTo(820.0f, -84.0f)
              quadToRelative(-11.0f, 11.0f, -28.0f, 11.0f)
              reflectiveQuadToRelative(-28.0f, -11.0f)
              lineTo(624.0f, -222.0f)
              quadToRelative(-35.0f, 11.0f, -71.0f, 16.5f)
              reflectiveQuadToRelative(-73.0f, 5.5f)
              close()
              moveTo(222.0f, -624.0f)
              quadToRelative(-29.0f, 26.0f, -53.0f, 57.0f)
              reflectiveQuadToRelative(-41.0f, 67.0f)
              quadToRelative(50.0f, 101.0f, 144.5f, 160.5f)
              reflectiveQuadTo(480.0f, -280.0f)
              quadToRelative(20.0f, 0.0f, 39.0f, -2.5f)
              reflectiveQuadToRelative(39.0f, -5.5f)
              lineToRelative(-36.0f, -38.0f)
              quadToRelative(-11.0f, 3.0f, -21.0f, 4.5f)
              reflectiveQuadToRelative(-21.0f, 1.5f)
              quadToRelative(-75.0f, 0.0f, -127.5f, -52.5f)
              reflectiveQuadTo(300.0f, -500.0f)
              quadToRelative(0.0f, -11.0f, 1.5f, -21.0f)
              reflectiveQuadToRelative(4.5f, -21.0f)
              lineToRelative(-84.0f, -82.0f)
              close()
              moveToRelative(319.0f, 93.0f)
              close()
              moveToRelative(-151.0f, 75.0f)
              close()
            }
          }
        }
        .build()
    return _visibilityOff!!
  }

private var _visibilityOff: ImageVector? = null
