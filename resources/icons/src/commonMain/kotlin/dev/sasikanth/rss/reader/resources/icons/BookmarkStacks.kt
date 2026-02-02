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
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.BookmarkStacks: ImageVector
  get() {
    if (_bookmarkStacks != null) {
      return _bookmarkStacks!!
    }
    _bookmarkStacks =
      Builder(
          name = "BookmarkStacks",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF1f1f1f)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(520.0f, 125.0f)
            verticalLineToRelative(155.0f)
            horizontalLineToRelative(316.0f)
            quadToRelative(19.0f, 0.0f, 30.0f, 12.5f)
            reflectiveQuadToRelative(11.0f, 27.5f)
            quadToRelative(0.0f, 10.0f, -5.0f, 19.5f)
            reflectiveQuadTo(856.0f, 355.0f)
            lineTo(518.0f, 539.0f)
            quadToRelative(-18.0f, 10.0f, -38.0f, 10.0f)
            reflectiveQuadToRelative(-38.0f, -10.0f)
            lineTo(104.0f, 355.0f)
            quadToRelative(-11.0f, -6.0f, -15.5f, -15.0f)
            reflectiveQuadTo(84.0f, 320.0f)
            quadToRelative(0.0f, -11.0f, 4.5f, -20.0f)
            reflectiveQuadToRelative(15.5f, -15.0f)
            lineToRelative(357.0f, -195.0f)
            quadToRelative(10.0f, -5.0f, 21.0f, -4.5f)
            reflectiveQuadToRelative(19.0f, 5.5f)
            quadToRelative(8.0f, 5.0f, 13.5f, 14.0f)
            reflectiveQuadToRelative(5.5f, 20.0f)
            close()
            moveTo(480.0f, 469.0f)
            lineTo(680.0f, 360.0f)
            lineTo(440.0f, 360.0f)
            verticalLineToRelative(-167.0f)
            lineTo(207.0f, 320.0f)
            lineToRelative(273.0f, 149.0f)
            close()
            moveTo(440.0f, 360.0f)
            close()
            moveTo(480.0f, 629.0f)
            lineTo(794.0f, 458.0f)
            quadToRelative(2.0f, -1.0f, 19.0f, -5.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(853.0f, 493.0f)
            quadToRelative(0.0f, 11.0f, -5.0f, 20.0f)
            reflectiveQuadToRelative(-16.0f, 15.0f)
            lineTo(518.0f, 699.0f)
            quadToRelative(-9.0f, 5.0f, -18.5f, 7.5f)
            reflectiveQuadTo(480.0f, 709.0f)
            quadToRelative(-10.0f, 0.0f, -19.5f, -2.5f)
            reflectiveQuadTo(442.0f, 699.0f)
            lineTo(128.0f, 528.0f)
            quadToRelative(-11.0f, -6.0f, -16.0f, -15.0f)
            reflectiveQuadToRelative(-5.0f, -20.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(147.0f, 453.0f)
            quadToRelative(5.0f, 0.0f, 9.5f, 1.5f)
            reflectiveQuadToRelative(9.5f, 3.5f)
            lineToRelative(314.0f, 171.0f)
            close()
            moveTo(480.0f, 789.0f)
            lineTo(794.0f, 618.0f)
            quadToRelative(2.0f, -1.0f, 19.0f, -5.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(853.0f, 653.0f)
            quadToRelative(0.0f, 11.0f, -5.0f, 20.0f)
            reflectiveQuadToRelative(-16.0f, 15.0f)
            lineTo(518.0f, 859.0f)
            quadToRelative(-9.0f, 5.0f, -18.5f, 7.5f)
            reflectiveQuadTo(480.0f, 869.0f)
            quadToRelative(-10.0f, 0.0f, -19.5f, -2.5f)
            reflectiveQuadTo(442.0f, 859.0f)
            lineTo(128.0f, 688.0f)
            quadToRelative(-11.0f, -6.0f, -16.0f, -15.0f)
            reflectiveQuadToRelative(-5.0f, -20.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(147.0f, 613.0f)
            quadToRelative(5.0f, 0.0f, 9.5f, 1.5f)
            reflectiveQuadToRelative(9.5f, 3.5f)
            lineToRelative(314.0f, 171.0f)
            close()
          }
        }
        .build()
    return _bookmarkStacks!!
  }

private var _bookmarkStacks: ImageVector? = null
