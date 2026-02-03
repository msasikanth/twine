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

public val TwineIcons.Timer: ImageVector
  get() {
    if (_timer != null) {
      return _timer!!
    }
    _timer =
      ImageVector.Builder(
          name = "Timer",
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
              /* pathData = "M400-840q-17 0-28.5-11.5T360-880q0-17 11.5-28.5T400-920h160q17 0 28.5 11.5T600-880q0 17-11.5 28.5T560-840H400Zm80 440q17 0 28.5-11.5T520-440v-160q0-17-11.5-28.5T480-640q-17 0-28.5 11.5T440-600v160q0 17 11.5 28.5T480-400Zm0 320q-74 0-139.5-28.5T226-186q-49-49-77.5-114.5T120-440q0-74 28.5-139.5T226-694q49-49 114.5-77.5T480-800q62 0 119 20t107 58l28-28q11-11 28-11t28 11q11 11 11 28t-11 28l-28 28q38 50 58 107t20 119q0 74-28.5 139.5T734-186q-49 49-114.5 77.5T480-80Zm0-80q116 0 198-82t82-198q0-116-82-198t-198-82q-116 0-198 82t-82 198q0 116 82 198t198 82Zm0-280Z" */
              moveTo(400.0f, -840.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(360.0f, -880.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(400.0f, -920.0f)
              horizontalLineToRelative(160.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(600.0f, -880.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(560.0f, -840.0f)
              horizontalLineTo(400.0f)
              close()
              moveToRelative(80.0f, 440.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, -11.5f)
              reflectiveQuadTo(520.0f, -440.0f)
              verticalLineToRelative(-160.0f)
              quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
              reflectiveQuadTo(480.0f, -640.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
              reflectiveQuadTo(440.0f, -600.0f)
              verticalLineToRelative(160.0f)
              quadToRelative(0.0f, 17.0f, 11.5f, 28.5f)
              reflectiveQuadTo(480.0f, -400.0f)
              close()
              moveToRelative(0.0f, 320.0f)
              quadToRelative(-74.0f, 0.0f, -139.5f, -28.5f)
              reflectiveQuadTo(226.0f, -186.0f)
              quadToRelative(-49.0f, -49.0f, -77.5f, -114.5f)
              reflectiveQuadTo(120.0f, -440.0f)
              quadToRelative(0.0f, -74.0f, 28.5f, -139.5f)
              reflectiveQuadTo(226.0f, -694.0f)
              quadToRelative(49.0f, -49.0f, 114.5f, -77.5f)
              reflectiveQuadTo(480.0f, -800.0f)
              quadToRelative(62.0f, 0.0f, 119.0f, 20.0f)
              reflectiveQuadToRelative(107.0f, 58.0f)
              lineToRelative(28.0f, -28.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              lineToRelative(-28.0f, 28.0f)
              quadToRelative(38.0f, 50.0f, 58.0f, 107.0f)
              reflectiveQuadToRelative(20.0f, 119.0f)
              quadToRelative(0.0f, 74.0f, -28.5f, 139.5f)
              reflectiveQuadTo(734.0f, -186.0f)
              quadToRelative(-49.0f, 49.0f, -114.5f, 77.5f)
              reflectiveQuadTo(480.0f, -80.0f)
              close()
              moveToRelative(0.0f, -80.0f)
              quadToRelative(116.0f, 0.0f, 198.0f, -82.0f)
              reflectiveQuadToRelative(82.0f, -198.0f)
              quadToRelative(0.0f, -116.0f, -82.0f, -198.0f)
              reflectiveQuadToRelative(-198.0f, -82.0f)
              quadToRelative(-116.0f, 0.0f, -198.0f, 82.0f)
              reflectiveQuadToRelative(-82.0f, 198.0f)
              quadToRelative(0.0f, 116.0f, 82.0f, 198.0f)
              reflectiveQuadToRelative(198.0f, 82.0f)
              close()
              moveToRelative(0.0f, -280.0f)
              close()
            }
          }
        }
        .build()
    return _timer!!
  }

private var _timer: ImageVector? = null
