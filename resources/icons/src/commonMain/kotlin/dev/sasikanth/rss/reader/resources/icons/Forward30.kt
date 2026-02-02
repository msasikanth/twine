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

public val TwineIcons.Forward30: ImageVector
  get() {
    if (_forward30 != null) {
      return _forward30!!
    }
    _forward30 =
      ImageVector.Builder(
          name = "Forward30",
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
              /* pathData = "M480-80q-75 0-140.5-28.5t-114-77q-48.5-48.5-77-114T120-440q0-75 28.5-140.5t77-114q48.5-48.5 114-77T480-800h6l-34-34q-12-12-11.5-28t11.5-28q12-12 28.5-12.5T509-891l103 103q12 12 12 28t-12 28L509-629q-12 12-28.5 11.5T452-630q-11-12-11.5-28t11.5-28l34-34h-6q-117 0-198.5 81.5T200-440q0 117 81.5 198.5T480-160q117 0 198.5-81.5T760-440q0-17 11.5-28.5T800-480q17 0 28.5 11.5T840-440q0 75-28.5 140.5t-77 114q-48.5 48.5-114 77T480-80Zm-60-240h-90q-13 0-21.5-8.5T300-350q0-13 8.5-21.5T330-380h70v-40h-40q-8 0-14-6t-6-14q0-8 6-14t14-6h40v-40h-70q-13 0-21.5-8.5T300-530q0-13 8.5-21.5T330-560h90q17 0 28.5 11.5T460-520v160q0 17-11.5 28.5T420-320Zm120 0q-17 0-28.5-11.5T500-360v-160q0-17 11.5-28.5T540-560h80q17 0 28.5 11.5T660-520v160q0 17-11.5 28.5T620-320h-80Zm20-60h40v-120h-40v120Z" */
              moveTo(480.0f, -80.0f)
              quadToRelative(-75.0f, 0.0f, -140.5f, -28.5f)
              reflectiveQuadToRelative(-114.0f, -77.0f)
              quadToRelative(-48.5f, -48.5f, -77.0f, -114.0f)
              reflectiveQuadTo(120.0f, -440.0f)
              quadToRelative(0.0f, -75.0f, 28.5f, -140.5f)
              reflectiveQuadToRelative(77.0f, -114.0f)
              quadToRelative(48.5f, -48.5f, 114.0f, -77.0f)
              reflectiveQuadTo(480.0f, -800.0f)
              horizontalLineToRelative(6.0f)
              lineToRelative(-34.0f, -34.0f)
              quadToRelative(-12.0f, -12.0f, -11.5f, -28.0f)
              reflectiveQuadToRelative(11.5f, -28.0f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.5f)
              reflectiveQuadTo(509.0f, -891.0f)
              lineToRelative(103.0f, 103.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.0f)
              reflectiveQuadToRelative(-12.0f, 28.0f)
              lineTo(509.0f, -629.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 11.5f)
              reflectiveQuadTo(452.0f, -630.0f)
              quadToRelative(-11.0f, -12.0f, -11.5f, -28.0f)
              reflectiveQuadToRelative(11.5f, -28.0f)
              lineToRelative(34.0f, -34.0f)
              horizontalLineToRelative(-6.0f)
              quadToRelative(-117.0f, 0.0f, -198.5f, 81.5f)
              reflectiveQuadTo(200.0f, -440.0f)
              quadToRelative(0.0f, 117.0f, 81.5f, 198.5f)
              reflectiveQuadTo(480.0f, -160.0f)
              quadToRelative(117.0f, 0.0f, 198.5f, -81.5f)
              reflectiveQuadTo(760.0f, -440.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(800.0f, -480.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(840.0f, -440.0f)
              quadToRelative(0.0f, 75.0f, -28.5f, 140.5f)
              reflectiveQuadToRelative(-77.0f, 114.0f)
              quadToRelative(-48.5f, 48.5f, -114.0f, 77.0f)
              reflectiveQuadTo(480.0f, -80.0f)
              close()
              moveToRelative(-60.0f, -240.0f)
              horizontalLineToRelative(-90.0f)
              quadToRelative(-13.0f, 0.0f, -21.5f, -8.5f)
              reflectiveQuadTo(300.0f, -350.0f)
              quadToRelative(0.0f, -13.0f, 8.5f, -21.5f)
              reflectiveQuadTo(330.0f, -380.0f)
              horizontalLineToRelative(70.0f)
              verticalLineToRelative(-40.0f)
              horizontalLineToRelative(-40.0f)
              quadToRelative(-8.0f, 0.0f, -14.0f, -6.0f)
              reflectiveQuadToRelative(-6.0f, -14.0f)
              quadToRelative(0.0f, -8.0f, 6.0f, -14.0f)
              reflectiveQuadToRelative(14.0f, -6.0f)
              horizontalLineToRelative(40.0f)
              verticalLineToRelative(-40.0f)
              horizontalLineToRelative(-70.0f)
              quadToRelative(-13.0f, 0.0f, -21.5f, -8.5f)
              reflectiveQuadTo(300.0f, -530.0f)
              quadToRelative(0.0f, -13.0f, 8.5f, -21.5f)
              reflectiveQuadTo(330.0f, -560.0f)
              horizontalLineToRelative(90.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(460.0f, -520.0f)
              verticalLineToRelative(160.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(420.0f, -320.0f)
              close()
              moveToRelative(120.0f, 0.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(500.0f, -360.0f)
              verticalLineToRelative(-160.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(540.0f, -560.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(660.0f, -520.0f)
              verticalLineToRelative(160.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(620.0f, -320.0f)
              horizontalLineToRelative(-80.0f)
              close()
              moveToRelative(20.0f, -60.0f)
              horizontalLineToRelative(40.0f)
              verticalLineToRelative(-120.0f)
              horizontalLineToRelative(-40.0f)
              verticalLineToRelative(120.0f)
              close()
            }
          }
        }
        .build()
    return _forward30!!
  }

private var _forward30: ImageVector? = null
