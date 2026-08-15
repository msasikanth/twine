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
public val TwineIcons.Play: ImageVector
  get() {
    if (_play_arrow != null) {
      return _play_arrow!!
    }
    _play_arrow =
      ImageVector.Builder(
          name = "play_arrow",
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
            moveTo(5.11f, 16.08f)
            verticalLineTo(3.89f)
            quadTo(5.11f, 3.38f, 5.45f, 3.07f)
            reflectiveQuadTo(6.24f, 2.78f)
            quadToRelative(0.13f, 0f, 0.3f, 0.04f)
            reflectiveQuadToRelative(0.3f, 0.13f)
            lineToRelative(9.41f, 6.11f)
            quadToRelative(0.25f, 0.16f, 0.38f, 0.41f)
            reflectiveQuadTo(16.77f, 9.98f)
            reflectiveQuadToRelative(-0.13f, 0.52f)
            reflectiveQuadToRelative(-0.38f, 0.4f)
            lineTo(6.83f, 17.03f)
            quadToRelative(-0.15f, 0.09f, -0.3f, 0.13f)
            reflectiveQuadToRelative(-0.3f, 0.04f)
            quadToRelative(-0.44f, 0f, -0.78f, -0.31f)
            reflectiveQuadTo(5.11f, 16.08f)
            close()
            moveTo(7.36f, 9.95f)
            close()
            moveTo(7.33f, 14.05f)
            lineTo(13.62f, 9.98f)
            lineTo(7.33f, 5.91f)
            verticalLineToRelative(8.14f)
            close()
          }
        }
        .build()
    return _play_arrow!!
  }

private var _play_arrow: ImageVector? = null
