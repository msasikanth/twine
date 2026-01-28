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

public val TwineIcons.Search: ImageVector
  get() {
    if (_search != null) {
      return _search!!
    }
    _search =
      ImageVector.Builder(
          name = "Search",
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
              /* pathData = "M380-320q-109 0-184.5-75.5T120-580q0-109 75.5-184.5T380-840q109 0 184.5 75.5T640-580q0 44-14 83t-38 69l224 224q11 11 11 28t-11 28q-11 11-28 11t-28-11L532-372q-30 24-69 38t-83 14Zm0-80q75 0 127.5-52.5T560-580q0-75-52.5-127.5T380-760q-75 0-127.5 52.5T200-580q0 75 52.5 127.5T380-400Z" */
              moveTo(380.0f, -320.0f)
              quadToRelative(-109.0f, 0.0f, -184.5f, -75.5f)
              reflectiveQuadTo(120.0f, -580.0f)
              quadToRelative(0.0f, -109.0f, 75.5f, -184.5f)
              reflectiveQuadTo(380.0f, -840.0f)
              quadToRelative(109.0f, 0.0f, 184.5f, 75.5f)
              reflectiveQuadTo(640.0f, -580.0f)
              quadToRelative(0.0f, 44.0f, -14.0f, 83.0f)
              reflectiveQuadToRelative(-38.0f, 69.0f)
              lineToRelative(224.0f, 224.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              quadToRelative(-11.0f, 11.0f, -28.0f, 11.0f)
              reflectiveQuadToRelative(-28.0f, -11.0f)
              lineTo(532.0f, -372.0f)
              quadToRelative(-30.0f, 24.0f, -69.0f, 38.0f)
              reflectiveQuadToRelative(-83.0f, 14.0f)
              close()
              moveToRelative(0.0f, -80.0f)
              quadToRelative(75.0f, 0.0f, 127.5f, -52.5f)
              reflectiveQuadTo(560.0f, -580.0f)
              quadToRelative(0.0f, -75.0f, -52.5f, -127.5f)
              reflectiveQuadTo(380.0f, -760.0f)
              quadToRelative(-75.0f, 0.0f, -127.5f, 52.5f)
              reflectiveQuadTo(200.0f, -580.0f)
              quadToRelative(0.0f, 75.0f, 52.5f, 127.5f)
              reflectiveQuadTo(380.0f, -400.0f)
              close()
            }
          }
        }
        .build()
    return _search!!
  }

private var _search: ImageVector? = null
