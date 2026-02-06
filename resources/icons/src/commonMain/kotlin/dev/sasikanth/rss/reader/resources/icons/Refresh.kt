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

public val TwineIcons.Refresh: ImageVector
  get() {
    if (_refresh != null) {
      return _refresh!!
    }
    _refresh =
      ImageVector.Builder(
          name = "Refresh",
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
              /* pathData = "M480-160q-134 0-227-93t-93-227q0-134 93-227t227-93q69 0 132 28.5T720-690v-70q0-17 11.5-28.5T760-800q17 0 28.5 11.5T800-760v200q0 17-11.5 28.5T760-520H560q-17 0-28.5-11.5T520-560q0-17 11.5-28.5T560-600h128q-32-56-87.5-88T480-720q-100 0-170 70t-70 170q0 100 70 170t170 70q68 0 124.5-34.5T692-367q8-14 22.5-19.5t29.5-.5q16 5 23 21t-1 30q-41 80-117 128t-169 48Z" */
              moveTo(480.0f, -160.0f)
              quadToRelative(-134.0f, 0.0f, -227.0f, -93.0f)
              reflectiveQuadToRelative(-93.0f, -227.0f)
              quadToRelative(0.0f, -134.0f, 93.0f, -227.0f)
              reflectiveQuadToRelative(227.0f, -93.0f)
              quadToRelative(69.0f, 0.0f, 132.0f, 28.5f)
              reflectiveQuadTo(720.0f, -690.0f)
              verticalLineToRelative(-70.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(760.0f, -800.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(800.0f, -760.0f)
              verticalLineToRelative(200.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(760.0f, -520.0f)
              horizontalLineTo(560.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(520.0f, -560.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(560.0f, -600.0f)
              horizontalLineToRelative(128.0f)
              quadToRelative(-32.0f, -56.0f, -87.5f, -88.0f)
              reflectiveQuadTo(480.0f, -720.0f)
              quadToRelative(-100.0f, 0.0f, -170.0f, 70.0f)
              reflectiveQuadToRelative(-70.0f, 170.0f)
              quadToRelative(0.0f, 100.0f, 70.0f, 170.0f)
              reflectiveQuadToRelative(170.0f, 70.0f)
              quadToRelative(68.0f, 0.0f, 124.5f, -34.5f)
              reflectiveQuadTo(692.0f, -367.0f)
              quadToRelative(8.0f, -14.0f, 22.5f, -19.5f)
              reflectiveQuadToRelative(29.5f, -0.5f)
              quadToRelative(16.0f, 5.0f, 23.0f, 21.0f)
              reflectiveQuadToRelative(-1.0f, 30.0f)
              quadToRelative(-41.0f, 80.0f, -117.0f, 128.0f)
              reflectiveQuadToRelative(-169.0f, 48.0f)
              close()
            }
          }
        }
        .build()
    return _refresh!!
  }

private var _refresh: ImageVector? = null
