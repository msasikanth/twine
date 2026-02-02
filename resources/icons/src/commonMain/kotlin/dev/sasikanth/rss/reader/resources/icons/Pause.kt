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

public val TwineIcons.Pause: ImageVector
  get() {
    if (_pause != null) {
      return _pause!!
    }
    _pause =
      ImageVector.Builder(
          name = "Pause",
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
              /* pathData = "M600-200q-33 0-56.5-23.5T520-280v-400q0-33 23.5-56.5T600-760h80q33 0 56.5 23.5T760-680v400q0 33-23.5 56.5T680-200h-80Zm-320 0q-33 0-56.5-23.5T200-280v-400q0-33 23.5-56.5T280-760h80q33 0 56.5 23.5T440-680v400q0 33-23.5 56.5T360-200h-80Zm320-80h80v-400h-80v400Zm-320 0h80v-400h-80v400Zm0-400v400-400Zm320 0v400-400Z" */
              moveTo(600.0f, -200.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(520.0f, -280.0f)
              verticalLineToRelative(-400.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(600.0f, -760.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(760.0f, -680.0f)
              verticalLineToRelative(400.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(680.0f, -200.0f)
              horizontalLineToRelative(-80.0f)
              close()
              moveToRelative(-320.0f, 0.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(200.0f, -280.0f)
              verticalLineToRelative(-400.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(280.0f, -760.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(440.0f, -680.0f)
              verticalLineToRelative(400.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(360.0f, -200.0f)
              horizontalLineToRelative(-80.0f)
              close()
              moveToRelative(320.0f, -80.0f)
              horizontalLineToRelative(80.0f)
              verticalLineToRelative(-400.0f)
              horizontalLineToRelative(-80.0f)
              verticalLineToRelative(400.0f)
              close()
              moveToRelative(-320.0f, 0.0f)
              horizontalLineToRelative(80.0f)
              verticalLineToRelative(-400.0f)
              horizontalLineToRelative(-80.0f)
              verticalLineToRelative(400.0f)
              close()
              moveToRelative(0.0f, -400.0f)
              verticalLineToRelative(400.0f)
              verticalLineToRelative(-400.0f)
              close()
              moveToRelative(320.0f, 0.0f)
              verticalLineToRelative(400.0f)
              verticalLineToRelative(-400.0f)
              close()
            }
          }
        }
        .build()
    return _pause!!
  }

private var _pause: ImageVector? = null
