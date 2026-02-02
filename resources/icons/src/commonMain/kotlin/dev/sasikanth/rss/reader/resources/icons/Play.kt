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

public val TwineIcons.Play: ImageVector
  get() {
    if (_playArrow != null) {
      return _playArrow!!
    }
    _playArrow =
      ImageVector.Builder(
          name = "PlayArrow",
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
              /* pathData = "M320-273v-414q0-17 12-28.5t28-11.5q5 0 10.5 1.5T381-721l326 207q9 6 13.5 15t4.5 19q0 10-4.5 19T707-446L381-239q-5 3-10.5 4.5T360-233q-16 0-28-11.5T320-273Zm80-207Zm0 134 210-134-210-134v268Z" */
              moveTo(320.0f, -273.0f)
              verticalLineToRelative(-414.0f)
              quadToRelative(0.0f, -17.0f, 12.0f, -28.5f)
              reflectiveQuadToRelative(28.0f, -11.5f)
              quadToRelative(5.0f, 0.0f, 10.5f, 1.5f)
              reflectiveQuadTo(381.0f, -721.0f)
              lineToRelative(326.0f, 207.0f)
              quadToRelative(9.0f, 6.0f, 13.5f, 15.0f)
              reflectiveQuadToRelative(4.5f, 19.0f)
              quadToRelative(0.0f, 10.0f, -4.5f, 19.0f)
              reflectiveQuadTo(707.0f, -446.0f)
              lineTo(381.0f, -239.0f)
              quadToRelative(-5.0f, 3.0f, -10.5f, 4.5f)
              reflectiveQuadTo(360.0f, -233.0f)
              quadToRelative(-16.0f, 0.0f, -28.0f, -11.5f)
              reflectiveQuadTo(320.0f, -273.0f)
              close()
              moveToRelative(80.0f, -207.0f)
              close()
              moveToRelative(0.0f, 134.0f)
              lineToRelative(210.0f, -134.0f)
              lineToRelative(-210.0f, -134.0f)
              verticalLineToRelative(268.0f)
              close()
            }
          }
        }
        .build()
    return _playArrow!!
  }

private var _playArrow: ImageVector? = null
