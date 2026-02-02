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

public val TwineIcons.BookmarkFilled: ImageVector
  get() {
    if (_bookmarkFilled != null) {
      return _bookmarkFilled!!
    }
    _bookmarkFilled =
      ImageVector.Builder(
          name = "BookmarkFilled",
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
              /* pathData = "m480-240-168 72q-40 17-76-6.5T200-241v-519q0-33 23.5-56.5T280-840h400q33 0 56.5 23.5T760-760v519q0 43-36 66.5t-76 6.5l-168-72Z" */
              moveToRelative(480.0f, -240.0f)
              lineToRelative(-168.0f, 72.0f)
              quadToRelative(-40.0f, 17.0f, -76.0f, -6.5f)
              reflectiveQuadTo(200.0f, -241.0f)
              verticalLineToRelative(-519.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(280.0f, -840.0f)
              horizontalLineToRelative(400.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(760.0f, -760.0f)
              verticalLineToRelative(519.0f)
              quadToRelative(0.0f, 43.0f, -36.0f, 66.5f)
              reflectiveQuadToRelative(-76.0f, 6.5f)
              lineToRelative(-168.0f, -72.0f)
              close()
            }
          }
        }
        .build()
    return _bookmarkFilled!!
  }

private var _bookmarkFilled: ImageVector? = null
