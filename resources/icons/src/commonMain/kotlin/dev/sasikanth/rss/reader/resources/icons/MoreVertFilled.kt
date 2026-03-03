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

public val TwineIcons.MoreVert: ImageVector
  get() {
    if (_moreVertFilled != null) {
      return _moreVertFilled!!
    }
    _moreVertFilled =
      ImageVector.Builder(
          name = "MoreVertFilled",
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
              /* pathData = "M480-160q-33 0-56.5-23.5T400-240q0-33 23.5-56.5T480-320q33 0 56.5 23.5T560-240q0 33-23.5 56.5T480-160Zm0-240q-33 0-56.5-23.5T400-480q0-33 23.5-56.5T480-560q33 0 56.5 23.5T560-480q0 33-23.5 56.5T480-400Zm0-240q-33 0-56.5-23.5T400-720q0-33 23.5-56.5T480-800q33 0 56.5 23.5T560-720q0 33-23.5 56.5T480-640Z" */
              moveTo(480.0f, -160.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(400.0f, -240.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(480.0f, -320.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(560.0f, -240.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(480.0f, -160.0f)
              close()
              moveToRelative(0.0f, -240.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(400.0f, -480.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(480.0f, -560.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(560.0f, -480.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(480.0f, -400.0f)
              close()
              moveToRelative(0.0f, -240.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(400.0f, -720.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(480.0f, -800.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(560.0f, -720.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(480.0f, -640.0f)
              close()
            }
          }
        }
        .build()
    return _moreVertFilled!!
  }

private var _moreVertFilled: ImageVector? = null
