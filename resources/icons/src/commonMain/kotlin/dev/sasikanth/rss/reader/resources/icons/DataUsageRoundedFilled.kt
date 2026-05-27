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

public val TwineIcons.DataUsageRoundedFilled: ImageVector
  get() {
    if (_dataUsageRoundedFilled != null) {
      return _dataUsageRoundedFilled!!
    }
    _dataUsageRoundedFilled =
      ImageVector.Builder(
          name = "DataUsageRoundedFilled",
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
              /* pathData = "M480-80q-83 0-156-31.5T197-197q-54-54-85.5-127T80-480q0-130 75-234t199-145q29-10 53.5 7t24.5 46q0 20-11.5 36.5T391-747q-86 27-138.5 100.5T200-480q0 117 81.5 198.5T480-200q52 0 100.5-18t86.5-52q15-14 36.5-14t36.5 14q23 21 24 47.5T742-176q-54 47-120.5 71.5T480-80Zm280-400q0-92-53-165.5T568-747q-18-6-29.5-22.5T527-806q0-29 24.5-46t53.5-7q125 42 200 146t75 233q0 18-1.5 36.5T873-403q-5 29-29.5 41.5T790-360q-19-7-29.5-25.5T754-424q3-17 4.5-30t1.5-26Z" */
              moveTo(480.0f, -80.0f)
              quadToRelative(-83.0f, 0.0f, -156.0f, -31.5f)
              reflectiveQuadTo(197.0f, -197.0f)
              quadToRelative(-54.0f, -54.0f, -85.5f, -127.0f)
              reflectiveQuadTo(80.0f, -480.0f)
              quadToRelative(0.0f, -130.0f, 75.0f, -234.0f)
              reflectiveQuadToRelative(199.0f, -145.0f)
              quadToRelative(29.0f, -10.0f, 53.5f, 7.0f)
              reflectiveQuadToRelative(24.5f, 46.0f)
              quadToRelative(0.0f, 20.0f, -11.5f, 36.5f)
              reflectiveQuadTo(391.0f, -747.0f)
              quadToRelative(-86.0f, 27.0f, -138.5f, 100.5f)
              reflectiveQuadTo(200.0f, -480.0f)
              quadToRelative(0.0f, 117.0f, 81.5f, 198.5f)
              reflectiveQuadTo(480.0f, -200.0f)
              quadToRelative(52.0f, 0.0f, 100.5f, -18.0f)
              reflectiveQuadToRelative(86.5f, -52.0f)
              quadToRelative(15.0f, -14.0f, 36.5f, -14.0f)
              reflectiveQuadToRelative(36.5f, 14.0f)
              quadToRelative(23.0f, 21.0f, 24.0f, 47.5f)
              reflectiveQuadTo(742.0f, -176.0f)
              quadToRelative(-54.0f, 47.0f, -120.5f, 71.5f)
              reflectiveQuadTo(480.0f, -80.0f)
              close()
              moveToRelative(280.0f, -400.0f)
              quadToRelative(0.0f, -92.0f, -53.0f, -165.5f)
              reflectiveQuadTo(568.0f, -747.0f)
              quadToRelative(-18.0f, -6.0f, -29.5f, -22.5f)
              reflectiveQuadTo(527.0f, -806.0f)
              quadToRelative(0.0f, -29.0f, 24.5f, -46.0f)
              reflectiveQuadToRelative(53.5f, -7.0f)
              quadToRelative(125.0f, 42.0f, 200.0f, 146.0f)
              reflectiveQuadToRelative(75.0f, 233.0f)
              quadToRelative(0.0f, 18.0f, -1.5f, 36.5f)
              reflectiveQuadTo(873.0f, -403.0f)
              quadToRelative(-5.0f, 29.0f, -29.5f, 41.5f)
              reflectiveQuadTo(790.0f, -360.0f)
              quadToRelative(-19.0f, -7.0f, -29.5f, -25.5f)
              reflectiveQuadTo(754.0f, -424.0f)
              quadToRelative(3.0f, -17.0f, 4.5f, -30.0f)
              reflectiveQuadToRelative(1.5f, -26.0f)
              close()
            }
          }
        }
        .build()
    return _dataUsageRoundedFilled!!
  }

private var _dataUsageRoundedFilled: ImageVector? = null
