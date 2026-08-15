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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val TwineIcons.Pause: ImageVector
  get() {
    if (_pause != null) {
      return _pause!!
    }
    _pause =
      ImageVector.Builder(
          name = "pause",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(12.96f, 17.12f)
            quadToRelative(-0.74f, 0f, -1.26f, -0.52f)
            reflectiveQuadTo(11.17f, 15.34f)
            verticalLineToRelative(-10.71f)
            quadTo(11.17f, 3.89f, 11.7f, 3.37f)
            reflectiveQuadTo(12.96f, 2.84f)
            horizontalLineToRelative(2.38f)
            quadToRelative(0.74f, 0f, 1.26f, 0.52f)
            reflectiveQuadTo(17.12f, 4.63f)
            verticalLineToRelative(10.71f)
            quadToRelative(0f, 0.74f, -0.52f, 1.26f)
            reflectiveQuadTo(15.34f, 17.12f)
            horizontalLineToRelative(-2.38f)
            close()
            moveToRelative(-8.33f, 0f)
            quadTo(3.89f, 17.12f, 3.37f, 16.6f)
            reflectiveQuadTo(2.84f, 15.34f)
            verticalLineToRelative(-10.71f)
            quadTo(2.84f, 3.89f, 3.37f, 3.37f)
            reflectiveQuadTo(4.63f, 2.84f)
            horizontalLineToRelative(2.38f)
            quadTo(7.75f, 2.84f, 8.27f, 3.37f)
            reflectiveQuadTo(8.79f, 4.63f)
            verticalLineToRelative(10.71f)
            quadToRelative(0f, 0.74f, -0.52f, 1.26f)
            reflectiveQuadTo(7.01f, 17.12f)
            horizontalLineToRelative(-2.38f)
            close()
            moveToRelative(8.33f, -1.78f)
            horizontalLineToRelative(2.38f)
            verticalLineToRelative(-10.71f)
            horizontalLineToRelative(-2.38f)
            verticalLineToRelative(10.71f)
            close()
            moveToRelative(-8.33f, 0f)
            horizontalLineToRelative(2.38f)
            verticalLineToRelative(-10.71f)
            horizontalLineToRelative(-2.38f)
            verticalLineToRelative(10.71f)
            close()
            moveToRelative(0f, -10.71f)
            verticalLineToRelative(10.71f)
            verticalLineToRelative(-10.71f)
            close()
            moveToRelative(8.33f, 0f)
            verticalLineToRelative(10.71f)
            verticalLineToRelative(-10.71f)
            close()
          }
        }
        .build()
    return _pause!!
  }

private var _pause: ImageVector? = null
