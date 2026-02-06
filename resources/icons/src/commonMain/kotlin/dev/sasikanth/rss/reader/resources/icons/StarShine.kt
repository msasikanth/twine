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

public val TwineIcons.StarShine: ImageVector
  get() {
    if (_starShine != null) {
      return _starShine!!
    }
    _starShine =
      ImageVector.Builder(
          name = "StarShine",
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
              /* pathData = "M788.5-372q16.5 0 28.5 12l63 64q12 12 12 28t-12 28q-12 12-28 12t-28-12l-64-63q-12-12-12-28.5t12-28.5q12-12 28.5-12ZM812-811.5q0 16.5-12 28.5l-63 63q-12 12-28.5 12T680-720q-12-12-12-28.5t12-28.5l64-63q12-12 28-12t28 12q12 12 12 28.5ZM188.5-852q16.5 0 28.5 12l63 64q12 12 12 28t-12 28q-12 12-28.5 12T223-720l-63-63q-12-12-12-28.5t12-28.5q12-12 28.5-12ZM212-331.5q0 16.5-12 28.5l-63 63q-12 12-28.5 12T80-240q-12-12-12-28.5T80-297l64-63q12-12 28-12t28 12q12 12 12 28.5ZM354-287l126-76 126 77-33-144 111-96-146-13-58-136-58 135-146 13 111 97-33 143Zm126-194Zm0 212L314-169q-11 7-23 6t-21-8q-9-7-14-17.5t-2-23.5l44-189-147-127q-10-9-12.5-20.5T140-571q4-11 12-18t22-9l194-17 75-178q5-12 15.5-18t21.5-6q11 0 21.5 6t15.5 18l75 178 194 17q14 2 22 9t12 18q4 11 1.5 22.5T809-528L662-401l44 189q3 13-2 23.5T690-171q-9 7-21 8t-23-6L480-269Z" */
              moveTo(788.5f, -372.0f)
              quadToRelative(16.5f, 0.0f, 28.5f, 12.0f)
              lineToRelative(63.0f, 64.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.0f)
              reflectiveQuadToRelative(-12.0f, 28.0f)
              quadToRelative(-12.0f, 12.0f, -28.0f, 12.0f)
              reflectiveQuadToRelative(-28.0f, -12.0f)
              lineToRelative(-64.0f, -63.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadToRelative(12.0f, -28.5f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.0f)
              close()
              moveTo(812.0f, -811.5f)
              quadToRelative(0.0f, 16.5f, -12.0f, 28.5f)
              lineToRelative(-63.0f, 63.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 12.0f)
              reflectiveQuadTo(680.0f, -720.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadToRelative(12.0f, -28.5f)
              lineToRelative(64.0f, -63.0f)
              quadToRelative(12.0f, -12.0f, 28.0f, -12.0f)
              reflectiveQuadToRelative(28.0f, 12.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              close()
              moveTo(188.5f, -852.0f)
              quadToRelative(16.5f, 0.0f, 28.5f, 12.0f)
              lineToRelative(63.0f, 64.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.0f)
              reflectiveQuadToRelative(-12.0f, 28.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 12.0f)
              reflectiveQuadTo(223.0f, -720.0f)
              lineToRelative(-63.0f, -63.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadToRelative(12.0f, -28.5f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.0f)
              close()
              moveTo(212.0f, -331.5f)
              quadToRelative(0.0f, 16.5f, -12.0f, 28.5f)
              lineToRelative(-63.0f, 63.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 12.0f)
              reflectiveQuadTo(80.0f, -240.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadTo(80.0f, -297.0f)
              lineToRelative(64.0f, -63.0f)
              quadToRelative(12.0f, -12.0f, 28.0f, -12.0f)
              reflectiveQuadToRelative(28.0f, 12.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              close()
              moveTo(354.0f, -287.0f)
              lineToRelative(126.0f, -76.0f)
              lineToRelative(126.0f, 77.0f)
              lineToRelative(-33.0f, -144.0f)
              lineToRelative(111.0f, -96.0f)
              lineToRelative(-146.0f, -13.0f)
              lineToRelative(-58.0f, -136.0f)
              lineToRelative(-58.0f, 135.0f)
              lineToRelative(-146.0f, 13.0f)
              lineToRelative(111.0f, 97.0f)
              lineToRelative(-33.0f, 143.0f)
              close()
              moveToRelative(126.0f, -194.0f)
              close()
              moveToRelative(0.0f, 212.0f)
              lineTo(314.0f, -169.0f)
              quadToRelative(-11.0f, 7.0f, -23.0f, 6.0f)
              reflectiveQuadToRelative(-21.0f, -8.0f)
              quadToRelative(-9.0f, -7.0f, -14.0f, -17.5f)
              reflectiveQuadToRelative(-2.0f, -23.5f)
              lineToRelative(44.0f, -189.0f)
              lineToRelative(-147.0f, -127.0f)
              quadToRelative(-10.0f, -9.0f, -12.5f, -20.5f)
              reflectiveQuadTo(140.0f, -571.0f)
              quadToRelative(4.0f, -11.0f, 12.0f, -18.0f)
              reflectiveQuadToRelative(22.0f, -9.0f)
              lineToRelative(194.0f, -17.0f)
              lineToRelative(75.0f, -178.0f)
              quadToRelative(5.0f, -12.0f, 15.5f, -18.0f)
              reflectiveQuadToRelative(21.5f, -6.0f)
              quadToRelative(11.0f, 0.0f, 21.5f, 6.0f)
              reflectiveQuadToRelative(15.5f, 18.0f)
              lineToRelative(75.0f, 178.0f)
              lineToRelative(194.0f, 17.0f)
              quadToRelative(14.0f, 2.0f, 22.0f, 9.0f)
              reflectiveQuadToRelative(12.0f, 18.0f)
              quadToRelative(4.0f, 11.0f, 1.5f, 22.5f)
              reflectiveQuadTo(809.0f, -528.0f)
              lineTo(662.0f, -401.0f)
              lineToRelative(44.0f, 189.0f)
              quadToRelative(3.0f, 13.0f, -2.0f, 23.5f)
              reflectiveQuadTo(690.0f, -171.0f)
              quadToRelative(-9.0f, 7.0f, -21.0f, 8.0f)
              reflectiveQuadToRelative(-23.0f, -6.0f)
              lineTo(480.0f, -269.0f)
              close()
            }
          }
        }
        .build()
    return _starShine!!
  }

private var _starShine: ImageVector? = null
