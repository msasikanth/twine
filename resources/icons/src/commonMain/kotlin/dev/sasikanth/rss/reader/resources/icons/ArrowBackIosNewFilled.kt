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

public val TwineIcons.ArrowBackIos: ImageVector
  get() {
    if (_arrowBackIosNewFilled != null) {
      return _arrowBackIosNewFilled!!
    }
    _arrowBackIosNewFilled =
      ImageVector.Builder(
          name = "ArrowBackIosNewFilled",
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
              /* pathData = "m382-480 294 294q15 15 14.5 35T675-116q-15 15-35 15t-35-15L297-423q-12-12-18-27t-6-30q0-15 6-30t18-27l308-308q15-15 35.5-14.5T676-844q15 15 15 35t-15 35L382-480Z" */
              moveToRelative(382.0f, -480.0f)
              lineToRelative(294.0f, 294.0f)
              quadToRelative(15.0f, 15.0f, 14.5f, 35.0f)
              reflectiveQuadTo(675.0f, -116.0f)
              quadToRelative(-15.0f, 15.0f, -35.0f, 15.0f)
              reflectiveQuadToRelative(-35.0f, -15.0f)
              lineTo(297.0f, -423.0f)
              quadToRelative(-12.0f, -12.0f, -18.0f, -27.0f)
              reflectiveQuadToRelative(-6.0f, -30.0f)
              quadToRelative(0.0f, -15.0f, 6.0f, -30.0f)
              reflectiveQuadToRelative(18.0f, -27.0f)
              lineToRelative(308.0f, -308.0f)
              quadToRelative(15.0f, -15.0f, 35.5f, -14.5f)
              reflectiveQuadTo(676.0f, -844.0f)
              quadToRelative(15.0f, 15.0f, 15.0f, 35.0f)
              reflectiveQuadToRelative(-15.0f, 35.0f)
              lineTo(382.0f, -480.0f)
              close()
            }
          }
        }
        .build()
    return _arrowBackIosNewFilled!!
  }

private var _arrowBackIosNewFilled: ImageVector? = null
