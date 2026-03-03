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

public val TwineIcons.CalendarClock: ImageVector
  get() {
    if (_calendarClock != null) {
      return _calendarClock!!
    }
    _calendarClock =
      ImageVector.Builder(
          name = "CalendarClock",
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
              /* pathData = "M200-640h560v-80H200v80Zm0 0v-80 80Zm0 560q-33 0-56.5-23.5T120-160v-560q0-33 23.5-56.5T200-800h40v-40q0-17 11.5-28.5T280-880q17 0 28.5 11.5T320-840v40h320v-40q0-17 11.5-28.5T680-880q17 0 28.5 11.5T720-840v40h40q33 0 56.5 23.5T840-720v187q0 17-11.5 28.5T800-493q-17 0-28.5-11.5T760-533v-27H200v400h232q17 0 28.5 11.5T472-120q0 17-11.5 28.5T432-80H200Zm378.5-18.5Q520-157 520-240t58.5-141.5Q637-440 720-440t141.5 58.5Q920-323 920-240T861.5-98.5Q803-40 720-40T578.5-98.5ZM740-248v-92q0-8-6-14t-14-6q-8 0-14 6t-6 14v91q0 8 3 15.5t9 13.5l61 61q6 6 14 6t14-6q6-6 6-14t-6-14l-61-61Z" */
              moveTo(200.0f, -640.0f)
              horizontalLineToRelative(560.0f)
              verticalLineToRelative(-80.0f)
              horizontalLineTo(200.0f)
              verticalLineToRelative(80.0f)
              close()
              moveToRelative(0.0f, 0.0f)
              verticalLineToRelative(-80.0f)
              verticalLineToRelative(80.0f)
              close()
              moveToRelative(0.0f, 560.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(120.0f, -160.0f)
              verticalLineToRelative(-560.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(200.0f, -800.0f)
              horizontalLineToRelative(40.0f)
              verticalLineToRelative(-40.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(280.0f, -880.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(320.0f, -840.0f)
              verticalLineToRelative(40.0f)
              horizontalLineToRelative(320.0f)
              verticalLineToRelative(-40.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(680.0f, -880.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(720.0f, -840.0f)
              verticalLineToRelative(40.0f)
              horizontalLineToRelative(40.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(840.0f, -720.0f)
              verticalLineToRelative(187.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(800.0f, -493.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(760.0f, -533.0f)
              verticalLineToRelative(-27.0f)
              horizontalLineTo(200.0f)
              verticalLineToRelative(400.0f)
              horizontalLineToRelative(232.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(472.0f, -120.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(432.0f, -80.0f)
              horizontalLineTo(200.0f)
              close()
              moveToRelative(378.5f, -18.5f)
              quadTo(520.0f, -157.0f, 520.0f, -240.0f)
              reflectiveQuadToRelative(58.5f, -141.5f)
              quadTo(637.0f, -440.0f, 720.0f, -440.0f)
              reflectiveQuadToRelative(141.5f, 58.5f)
              quadTo(920.0f, -323.0f, 920.0f, -240.0f)
              reflectiveQuadTo(861.5f, -98.5f)
              quadTo(803.0f, -40.0f, 720.0f, -40.0f)
              reflectiveQuadTo(578.5f, -98.5f)
              close()
              moveTo(740.0f, -248.0f)
              verticalLineToRelative(-92.0f)
              quadToRelative(0.0f, -8.0f, -6.0f, -14.0f)
              reflectiveQuadToRelative(-14.0f, -6.0f)
              quadToRelative(-8.0f, 0.0f, -14.0f, 6.0f)
              reflectiveQuadToRelative(-6.0f, 14.0f)
              verticalLineToRelative(91.0f)
              quadToRelative(0.0f, 8.0f, 3.0f, 15.5f)
              reflectiveQuadToRelative(9.0f, 13.5f)
              lineToRelative(61.0f, 61.0f)
              quadToRelative(6.0f, 6.0f, 14.0f, 6.0f)
              reflectiveQuadToRelative(14.0f, -6.0f)
              quadToRelative(6.0f, -6.0f, 6.0f, -14.0f)
              reflectiveQuadToRelative(-6.0f, -14.0f)
              lineToRelative(-61.0f, -61.0f)
              close()
            }
          }
        }
        .build()
    return _calendarClock!!
  }

private var _calendarClock: ImageVector? = null
