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

public val TwineIcons.CheckCircle: ImageVector
  get() {
    if (_checkCircleFilled != null) {
      return _checkCircleFilled!!
    }
    _checkCircleFilled =
      ImageVector.Builder(
          name = "CheckCircleFilled",
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
              /* pathData = "m424-408-86-86q-11-11-28-11t-28 11q-11 11-11 28t11 28l114 114q12 12 28 12t28-12l226-226q11-11 11-28t-11-28q-11-11-28-11t-28 11L424-408Zm56 328q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-83 31.5-156T197-763q54-54 127-85.5T480-880q83 0 156 31.5T763-763q54 54 85.5 127T880-480q0 83-31.5 156T763-197q-54 54-127 85.5T480-80Z" */
              moveToRelative(424.0f, -408.0f)
              lineToRelative(-86.0f, -86.0f)
              quadToRelative(-11.0f, -11.0f, -28.0f, -11.0f)
              reflectiveQuadToRelative(-28.0f, 11.0f)
              quadToRelative(-11.0f, 11.0f, -11.0f, 28.0f)
              reflectiveQuadToRelative(11.0f, 28.0f)
              lineToRelative(114.0f, 114.0f)
              quadToRelative(12.0f, 12.0f, 28.0f, 12.0f)
              reflectiveQuadToRelative(28.0f, -12.0f)
              lineToRelative(226.0f, -226.0f)
              quadToRelative(11.0f, -11.0f, 11.0f, -28.0f)
              reflectiveQuadToRelative(-11.0f, -28.0f)
              quadToRelative(-11.0f, -11.0f, -28.0f, -11.0f)
              reflectiveQuadToRelative(-28.0f, 11.0f)
              lineTo(424.0f, -408.0f)
              close()
              moveToRelative(56.0f, 328.0f)
              quadToRelative(-83.0f, 0.0f, -156.0f, -31.5f)
              reflectiveQuadTo(197.0f, -197.0f)
              quadToRelative(-54.0f, -54.0f, -85.5f, -127.0f)
              reflectiveQuadTo(80.0f, -480.0f)
              quadToRelative(0.0f, -83.0f, 31.5f, -156.0f)
              reflectiveQuadTo(197.0f, -763.0f)
              quadToRelative(54.0f, -54.0f, 127.0f, -85.5f)
              reflectiveQuadTo(480.0f, -880.0f)
              quadToRelative(83.0f, 0.0f, 156.0f, 31.5f)
              reflectiveQuadTo(763.0f, -763.0f)
              quadToRelative(54.0f, 54.0f, 85.5f, 127.0f)
              reflectiveQuadTo(880.0f, -480.0f)
              quadToRelative(0.0f, 83.0f, -31.5f, 156.0f)
              reflectiveQuadTo(763.0f, -197.0f)
              quadToRelative(-54.0f, 54.0f, -127.0f, 85.5f)
              reflectiveQuadTo(480.0f, -80.0f)
              close()
            }
          }
        }
        .build()
    return _checkCircleFilled!!
  }

private var _checkCircleFilled: ImageVector? = null
