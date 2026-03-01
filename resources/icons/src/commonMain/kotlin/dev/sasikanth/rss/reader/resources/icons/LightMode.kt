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

public val TwineIcons.LightMode: ImageVector
  get() {
    if (_lightMode != null) {
      return _lightMode!!
    }
    _lightMode =
      ImageVector.Builder(
          name = "LightMode",
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
              /* pathData = "M565-395q35-35 35-85t-35-85q-35-35-85-35t-85 35q-35 35-35 85t35 85q35 35 85 35t85-35Zm-226.5 56.5Q280-397 280-480t58.5-141.5Q397-680 480-680t141.5 58.5Q680-563 680-480t-58.5 141.5Q563-280 480-280t-141.5-58.5ZM80-440q-17 0-28.5-11.5T40-480q0-17 11.5-28.5T80-520h80q17 0 28.5 11.5T200-480q0 17-11.5 28.5T160-440H80Zm720 0q-17 0-28.5-11.5T760-480q0-17 11.5-28.5T800-520h80q17 0 28.5 11.5T920-480q0 17-11.5 28.5T880-440h-80ZM451.5-771.5Q440-783 440-800v-80q0-17 11.5-28.5T480-920q17 0 28.5 11.5T520-880v80q0 17-11.5 28.5T480-760q-17 0-28.5-11.5Zm0 720Q440-63 440-80v-80q0-17 11.5-28.5T480-200q17 0 28.5 11.5T520-160v80q0 17-11.5 28.5T480-40q-17 0-28.5-11.5ZM226-678l-43-42q-12-11-11.5-28t11.5-29q12-12 29-12t28 12l42 43q11 12 11 28t-11 28q-11 12-27.5 11.5T226-678Zm494 495-42-43q-11-12-11-28.5t11-27.5q11-12 27.5-11.5T734-282l43 42q12 11 11.5 28T777-183q-12 12-29 12t-28-12Zm-42-495q-12-11-11.5-27.5T678-734l42-43q11-12 28-11.5t29 11.5q12 12 12 29t-12 28l-43 42q-12 11-28 11t-28-11ZM183-183q-12-12-12-29t12-28l43-42q12-11 28.5-11t27.5 11q12 11 11.5 27.5T282-226l-42 43q-11 12-28 11.5T183-183Zm297-297Z" */
              moveTo(565.0f, -395.0f)
              quadToRelative(35.0f, -35.0f, 35.0f, -85.0f)
              reflectiveQuadToRelative(-35.0f, -85.0f)
              quadToRelative(-35.0f, -35.0f, -85.0f, -35.0f)
              reflectiveQuadToRelative(-85.0f, 35.0f)
              quadToRelative(-35.0f, 35.0f, -35.0f, 85.0f)
              reflectiveQuadToRelative(35.0f, 85.0f)
              quadToRelative(35.0f, 35.0f, 85.0f, 35.0f)
              reflectiveQuadToRelative(85.0f, -35.0f)
              close()
              moveToRelative(-226.5f, 56.5f)
              quadTo(280.0f, -397.0f, 280.0f, -480.0f)
              reflectiveQuadToRelative(58.5f, -141.5f)
              quadTo(397.0f, -680.0f, 480.0f, -680.0f)
              reflectiveQuadToRelative(141.5f, 58.5f)
              quadTo(680.0f, -563.0f, 680.0f, -480.0f)
              reflectiveQuadToRelative(-58.5f, 141.5f)
              quadTo(563.0f, -280.0f, 480.0f, -280.0f)
              reflectiveQuadToRelative(-141.5f, -58.5f)
              close()
              moveTo(80.0f, -440.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(40.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(80.0f, -520.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(200.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(160.0f, -440.0f)
              horizontalLineTo(80.0f)
              close()
              moveToRelative(720.0f, 0.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(760.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(800.0f, -520.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(920.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(880.0f, -440.0f)
              horizontalLineToRelative(-80.0f)
              close()
              moveTo(451.5f, -771.5f)
              quadTo(440.0f, -783.0f, 440.0f, -800.0f)
              verticalLineToRelative(-80.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(480.0f, -920.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(520.0f, -880.0f)
              verticalLineToRelative(80.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(480.0f, -760.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              close()
              moveToRelative(0.0f, 720.0f)
              quadTo(440.0f, -63.0f, 440.0f, -80.0f)
              verticalLineToRelative(-80.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(480.0f, -200.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(520.0f, -160.0f)
              verticalLineToRelative(80.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(480.0f, -40.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              close()
              moveTo(226.0f, -678.0f)
              lineToRelative(-43.0f, -42.0f)
              quadToRelative(-12.0f, -11.0f, -11.5f, -28.0f)
              reflectiveQuadToRelative(11.5f, -29.0f)
              quadToRelative(12.0f, -12.0f, 29.0f, -12.0f)
              reflectiveQuadToRelative(28.0f, 12.0f)
              lineToRelative(42.0f, 43.0f)
              quadToRelative(11.0f, 12.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              quadToRelative(-11.0f, 12.0f, -27.5f, 11.5f)
              reflectiveQuadTo(226.0f, -678.0f)
              close()
              moveToRelative(494.0f, 495.0f)
              lineToRelative(-42.0f, -43.0f)
              quadToRelative(-11.0f, -12.0f, -11.0f, -28.5f)
              reflectiveQuadToRelative(11.0f, -27.5f)
              quadToRelative(11.0f, -12.0f, 27.5f, -11.5f)
              reflectiveQuadTo(734.0f, -282.0f)
              lineToRelative(43.0f, 42.0f)
              quadToRelative(12.0f, 11.0f, 11.5f, 28.0f)
              reflectiveQuadTo(777.0f, -183.0f)
              quadToRelative(-12.0f, 12.0f, -29.0f, 12.0f)
              reflectiveQuadToRelative(-28.0f, -12.0f)
              close()
              moveToRelative(-42.0f, -495.0f)
              quadToRelative(-12.0f, -11.0f, -11.5f, -27.5f)
              reflectiveQuadTo(678.0f, -734.0f)
              lineToRelative(42.0f, -43.0f)
              quadToRelative(11.0f, -12.0f, 28.0f, -11.5f)
              reflectiveQuadToRelative(29.0f, 11.5f)
              quadToRelative(12.0f, 12.0f, 12.0f, 29.0f)
              reflectiveQuadToRelative(-12.0f, 28.0f)
              lineToRelative(-43.0f, 42.0f)
              quadToRelative(-12.0f, 11.0f, -28.0f, 11.0f)
              reflectiveQuadToRelative(-28.0f, -11.0f)
              close()
              moveTo(183.0f, -183.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -29.0f)
              reflectiveQuadToRelative(12.0f, -28.0f)
              lineToRelative(43.0f, -42.0f)
              quadToRelative(12.0f, -11.0f, 28.5f, -11.0f)
              reflectiveQuadToRelative(27.5f, 11.0f)
              quadToRelative(12.0f, 11.0f, 11.5f, 27.5f)
              reflectiveQuadTo(282.0f, -226.0f)
              lineToRelative(-42.0f, 43.0f)
              quadToRelative(-11.0f, 12.0f, -28.0f, 11.5f)
              reflectiveQuadTo(183.0f, -183.0f)
              close()
              moveToRelative(297.0f, -297.0f)
              close()
            }
          }
        }
        .build()
    return _lightMode!!
  }

private var _lightMode: ImageVector? = null
