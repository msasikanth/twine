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

public val TwineIcons.FormatSize: ImageVector
  get() {
    if (_formatSizeFilled != null) {
      return _formatSizeFilled!!
    }
    _formatSizeFilled =
      ImageVector.Builder(
          name = "FormatSizeFilled",
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
              /* pathData = "M560-680H420q-25 0-42.5-17.5T360-740q0-25 17.5-42.5T420-800h400q25 0 42.5 17.5T880-740q0 25-17.5 42.5T820-680H680v460q0 25-17.5 42.5T620-160q-25 0-42.5-17.5T560-220v-460ZM200-480h-60q-25 0-42.5-17.5T80-540q0-25 17.5-42.5T140-600h240q25 0 42.5 17.5T440-540q0 25-17.5 42.5T380-480h-60v260q0 25-17.5 42.5T260-160q-25 0-42.5-17.5T200-220v-260Z" */
              moveTo(560.0f, -680.0f)
              horizontalLineTo(420.0f)
              quadToRelative(-25.0f, 0.0f, -42.5f, -17.5f)
              reflectiveQuadTo(360.0f, -740.0f)
              quadToRelative(0.0f, -25.0f, 17.5f, -42.5f)
              reflectiveQuadTo(420.0f, -800.0f)
              horizontalLineToRelative(400.0f)
              quadToRelative(25.0f, 0.0f, 42.5f, 17.5f)
              reflectiveQuadTo(880.0f, -740.0f)
              quadToRelative(0.0f, 25.0f, -17.5f, 42.5f)
              reflectiveQuadTo(820.0f, -680.0f)
              horizontalLineTo(680.0f)
              verticalLineToRelative(460.0f)
              quadToRelative(0.0f, 25.0f, -17.5f, 42.5f)
              reflectiveQuadTo(620.0f, -160.0f)
              quadToRelative(-25.0f, 0.0f, -42.5f, -17.5f)
              reflectiveQuadTo(560.0f, -220.0f)
              verticalLineToRelative(-460.0f)
              close()
              moveTo(200.0f, -480.0f)
              horizontalLineToRelative(-60.0f)
              quadToRelative(-25.0f, 0.0f, -42.5f, -17.5f)
              reflectiveQuadTo(80.0f, -540.0f)
              quadToRelative(0.0f, -25.0f, 17.5f, -42.5f)
              reflectiveQuadTo(140.0f, -600.0f)
              horizontalLineToRelative(240.0f)
              quadToRelative(25.0f, 0.0f, 42.5f, 17.5f)
              reflectiveQuadTo(440.0f, -540.0f)
              quadToRelative(0.0f, 25.0f, -17.5f, 42.5f)
              reflectiveQuadTo(380.0f, -480.0f)
              horizontalLineToRelative(-60.0f)
              verticalLineToRelative(260.0f)
              quadToRelative(0.0f, 25.0f, -17.5f, 42.5f)
              reflectiveQuadTo(260.0f, -160.0f)
              quadToRelative(-25.0f, 0.0f, -42.5f, -17.5f)
              reflectiveQuadTo(200.0f, -220.0f)
              verticalLineToRelative(-260.0f)
              close()
            }
          }
        }
        .build()
    return _formatSizeFilled!!
  }

private var _formatSizeFilled: ImageVector? = null
