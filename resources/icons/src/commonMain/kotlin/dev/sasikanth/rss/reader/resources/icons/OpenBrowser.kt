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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.OpenBrowser: ImageVector
  get() {
    if (_openBrowser != null) {
      return _openBrowser!!
    }
    _openBrowser =
      Builder(
          name = "OpenBrowser",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFFC2C9BD)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(3.5f, 16.0f)
            horizontalLineTo(6.25f)
            curveTo(6.668f, 16.0f, 7.0f, 15.663f, 7.0f, 15.246f)
            curveTo(7.0f, 14.828f, 6.666f, 14.5f, 6.25f, 14.5f)
            horizontalLineTo(3.5f)
            verticalLineTo(7.5f)
            horizontalLineTo(16.5f)
            verticalLineTo(14.5f)
            horizontalLineTo(13.75f)
            curveTo(13.332f, 14.5f, 13.0f, 14.837f, 13.0f, 15.254f)
            curveTo(13.0f, 15.672f, 13.334f, 16.0f, 13.75f, 16.0f)
            horizontalLineTo(16.5f)
            curveTo(17.319f, 16.0f, 18.0f, 15.319f, 18.0f, 14.5f)
            verticalLineTo(4.5f)
            curveTo(18.0f, 3.681f, 17.319f, 3.0f, 16.5f, 3.0f)
            horizontalLineTo(3.5f)
            curveTo(2.681f, 3.0f, 2.0f, 3.681f, 2.0f, 4.5f)
            lineTo(2.0f, 14.5f)
            curveTo(2.0f, 15.319f, 2.681f, 16.0f, 3.5f, 16.0f)
            close()
            moveTo(16.5f, 4.5f)
            horizontalLineTo(3.5f)
            verticalLineTo(6.0f)
            horizontalLineTo(16.5f)
            verticalLineTo(4.5f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFFC2C9BD)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(9.25f, 12.563f)
            verticalLineTo(16.25f)
            curveTo(9.25f, 16.668f, 9.587f, 17.0f, 10.004f, 17.0f)
            curveTo(10.422f, 17.0f, 10.75f, 16.666f, 10.75f, 16.25f)
            verticalLineTo(12.563f)
            lineTo(11.417f, 13.214f)
            curveTo(11.723f, 13.512f, 12.184f, 13.5f, 12.489f, 13.208f)
            curveTo(12.775f, 12.909f, 12.776f, 12.464f, 12.483f, 12.169f)
            lineTo(10.527f, 10.208f)
            curveTo(10.376f, 10.057f, 10.201f, 10.0f, 10.0f, 10.0f)
            curveTo(9.799f, 10.0f, 9.624f, 10.057f, 9.473f, 10.208f)
            lineTo(7.517f, 12.169f)
            curveTo(7.222f, 12.465f, 7.223f, 12.928f, 7.511f, 13.229f)
            curveTo(7.817f, 13.523f, 8.279f, 13.533f, 8.583f, 13.23f)
            lineTo(9.25f, 12.563f)
            close()
          }
        }
        .build()
    return _openBrowser!!
  }

private var _openBrowser: ImageVector? = null
