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

public val TwineIcons.Visibility: ImageVector
  get() {
    if (_visibility != null) {
      return _visibility!!
    }
    _visibility =
      ImageVector.Builder(
          name = "Visibility",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f
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
              pathFillType = NonZero
            ) {
              /* pathData = "M480-320q75 0 127.5-52.5T660-500q0-75-52.5-127.5T480-680q-75 0-127.5 52.5T300-500q0 75 52.5 127.5T480-320Zm0-72q-45 0-76.5-31.5T372-500q0-45 31.5-76.5T480-608q45 0 76.5 31.5T588-500q0 45-31.5 76.5T480-392Zm0 192q-134 0-244.5-72T61-462q-5-9-7.5-18.5T51-500q0-10 2.5-19.5T61-538q64-118 174.5-190T480-800q134 0 244.5 72T899-538q5 9 7.5 18.5T909-500q0 10-2.5 19.5T899-462q-64 118-174.5 190T480-200Zm0-300Zm0 220q113 0 207.5-59.5T832-500q-50-101-144.5-160.5T480-720q-113 0-207.5 59.5T128-500q50 101 144.5 160.5T480-280Z" */
              moveTo(480.0f, -320.0f)
              quadToRelative(75.0f, 0.0f, 127.5f, -52.5f)
              reflectiveQuadTo(660.0f, -500.0f)
              quadToRelative(0.0f, -75.0f, -52.5f, -127.5f)
              reflectiveQuadTo(480.0f, -680.0f)
              quadToRelative(-75.0f, 0.0f, -127.5f, 52.5f)
              reflectiveQuadTo(300.0f, -500.0f)
              quadToRelative(0.0f, 75.0f, 52.5f, 127.5f)
              reflectiveQuadTo(480.0f, -320.0f)
              close()
              moveToRelative(0.0f, -72.0f)
              quadToRelative(-45.0f, 0.0f, -76.5f, -31.5f)
              reflectiveQuadTo(372.0f, -500.0f)
              quadToRelative(0.0f, -45.0f, 31.5f, -76.5f)
              reflectiveQuadTo(480.0f, -608.0f)
              quadToRelative(45.0f, 0.0f, 76.5f, 31.5f)
              reflectiveQuadTo(588.0f, -500.0f)
              quadToRelative(0.0f, 45.0f, -31.5f, 76.5f)
              reflectiveQuadTo(480.0f, -392.0f)
              close()
              moveToRelative(0.0f, 192.0f)
              quadToRelative(-134.0f, 0.0f, -244.5f, -72.0f)
              reflectiveQuadTo(61.0f, -462.0f)
              quadToRelative(-5.0f, -9.0f, -7.5f, -18.5f)
              reflectiveQuadTo(51.0f, -500.0f)
              quadToRelative(0.0f, -10.0f, 2.5f, -19.5f)
              reflectiveQuadTo(61.0f, -538.0f)
              quadToRelative(64.0f, -118.0f, 174.5f, -190.0f)
              reflectiveQuadTo(480.0f, -800.0f)
              quadToRelative(134.0f, 0.0f, 244.5f, 72.0f)
              reflectiveQuadTo(899.0f, -538.0f)
              quadToRelative(5.0f, 9.0f, 7.5f, 18.5f)
              reflectiveQuadTo(909.0f, -500.0f)
              quadToRelative(0.0f, 10.0f, -2.5f, 19.5f)
              reflectiveQuadTo(899.0f, -462.0f)
              quadToRelative(-64.0f, 118.0f, -174.5f, 190.0f)
              reflectiveQuadTo(480.0f, -200.0f)
              close()
              moveToRelative(0.0f, -300.0f)
              close()
              moveToRelative(0.0f, 220.0f)
              quadToRelative(113.0f, 0.0f, 207.5f, -59.5f)
              reflectiveQuadTo(832.0f, -500.0f)
              quadToRelative(-50.0f, -101.0f, -144.5f, -160.5f)
              reflectiveQuadTo(480.0f, -720.0f)
              quadToRelative(-113.0f, 0.0f, -207.5f, 59.5f)
              reflectiveQuadTo(128.0f, -500.0f)
              quadToRelative(50.0f, 101.0f, 144.5f, 160.5f)
              reflectiveQuadTo(480.0f, -280.0f)
              close()
            }
          }
        }
        .build()
    return _visibility!!
  }

private var _visibility: ImageVector? = null
