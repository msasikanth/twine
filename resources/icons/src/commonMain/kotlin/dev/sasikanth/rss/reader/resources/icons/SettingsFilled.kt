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

public val TwineIcons.SettingsFilled: ImageVector
  get() {
    if (_settingsFilled != null) {
      return _settingsFilled!!
    }
    _settingsFilled =
      ImageVector.Builder(
          name = "SettingsFilled",
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
              /* pathData = "M433-80q-27 0-46.5-18T363-142l-9-66q-13-5-24.5-12T307-235l-62 26q-25 11-50 2t-39-32l-47-82q-14-23-8-49t27-43l53-40q-1-7-1-13.5v-27q0-6.5 1-13.5l-53-40q-21-17-27-43t8-49l47-82q14-23 39-32t50 2l62 26q11-8 23-15t24-12l9-66q4-26 23.5-44t46.5-18h94q27 0 46.5 18t23.5 44l9 66q13 5 24.5 12t22.5 15l62-26q25-11 50-2t39 32l47 82q14 23 8 49t-27 43l-53 40q1 7 1 13.5v27q0 6.5-2 13.5l53 40q21 17 27 43t-8 49l-48 82q-14 23-39 32t-50-2l-60-26q-11 8-23 15t-24 12l-9 66q-4 26-23.5 44T527-80h-94Zm49-260q58 0 99-41t41-99q0-58-41-99t-99-41q-59 0-99.5 41T342-480q0 58 40.5 99t99.5 41Z" */
              moveTo(433.0f, -80.0f)
              quadToRelative(-27.0f, 0.0f, -46.5f, -18.0f)
              reflectiveQuadTo(363.0f, -142.0f)
              lineToRelative(-9.0f, -66.0f)
              quadToRelative(-13.0f, -5.0f, -24.5f, -12.0f)
              reflectiveQuadTo(307.0f, -235.0f)
              lineToRelative(-62.0f, 26.0f)
              quadToRelative(-25.0f, 11.0f, -50.0f, 2.0f)
              reflectiveQuadToRelative(-39.0f, -32.0f)
              lineToRelative(-47.0f, -82.0f)
              quadToRelative(-14.0f, -23.0f, -8.0f, -49.0f)
              reflectiveQuadToRelative(27.0f, -43.0f)
              lineToRelative(53.0f, -40.0f)
              quadToRelative(-1.0f, -7.0f, -1.0f, -13.5f)
              verticalLineToRelative(-27.0f)
              quadToRelative(0.0f, -6.5f, 1.0f, -13.5f)
              lineToRelative(-53.0f, -40.0f)
              quadToRelative(-21.0f, -17.0f, -27.0f, -43.0f)
              reflectiveQuadToRelative(8.0f, -49.0f)
              lineToRelative(47.0f, -82.0f)
              quadToRelative(14.0f, -23.0f, 39.0f, -32.0f)
              reflectiveQuadToRelative(50.0f, 2.0f)
              lineToRelative(62.0f, 26.0f)
              quadToRelative(11.0f, -8.0f, 23.0f, -15.0f)
              reflectiveQuadToRelative(24.0f, -12.0f)
              lineToRelative(9.0f, -66.0f)
              quadToRelative(4.0f, -26.0f, 23.5f, -44.0f)
              reflectiveQuadToRelative(46.5f, -18.0f)
              horizontalLineToRelative(94.0f)
              quadToRelative(27.0f, 0.0f, 46.5f, 18.0f)
              reflectiveQuadToRelative(23.5f, 44.0f)
              lineToRelative(9.0f, 66.0f)
              quadToRelative(13.0f, 5.0f, 24.5f, 12.0f)
              reflectiveQuadToRelative(22.5f, 15.0f)
              lineToRelative(62.0f, -26.0f)
              quadToRelative(25.0f, -11.0f, 50.0f, -2.0f)
              reflectiveQuadToRelative(39.0f, 32.0f)
              lineToRelative(47.0f, 82.0f)
              quadToRelative(14.0f, 23.0f, 8.0f, 49.0f)
              reflectiveQuadToRelative(-27.0f, 43.0f)
              lineToRelative(-53.0f, 40.0f)
              quadToRelative(1.0f, 7.0f, 1.0f, 13.5f)
              verticalLineToRelative(27.0f)
              quadToRelative(0.0f, 6.5f, -2.0f, 13.5f)
              lineToRelative(53.0f, 40.0f)
              quadToRelative(21.0f, 17.0f, 27.0f, 43.0f)
              reflectiveQuadToRelative(-8.0f, 49.0f)
              lineToRelative(-48.0f, 82.0f)
              quadToRelative(-14.0f, 23.0f, -39.0f, 32.0f)
              reflectiveQuadToRelative(-50.0f, -2.0f)
              lineToRelative(-60.0f, -26.0f)
              quadToRelative(-11.0f, 8.0f, -23.0f, 15.0f)
              reflectiveQuadToRelative(-24.0f, 12.0f)
              lineToRelative(-9.0f, 66.0f)
              quadToRelative(-4.0f, 26.0f, -23.5f, 44.0f)
              reflectiveQuadTo(527.0f, -80.0f)
              horizontalLineToRelative(-94.0f)
              close()
              moveToRelative(49.0f, -260.0f)
              quadToRelative(58.0f, 0.0f, 99.0f, -41.0f)
              reflectiveQuadToRelative(41.0f, -99.0f)
              quadToRelative(0.0f, -58.0f, -41.0f, -99.0f)
              reflectiveQuadToRelative(-99.0f, -41.0f)
              quadToRelative(-59.0f, 0.0f, -99.5f, 41.0f)
              reflectiveQuadTo(342.0f, -480.0f)
              quadToRelative(0.0f, 58.0f, 40.5f, 99.0f)
              reflectiveQuadToRelative(99.5f, 41.0f)
              close()
            }
          }
        }
        .build()
    return _settingsFilled!!
  }

private var _settingsFilled: ImageVector? = null
