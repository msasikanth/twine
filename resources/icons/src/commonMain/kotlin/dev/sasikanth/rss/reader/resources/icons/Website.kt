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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.Website: ImageVector
  get() {
    if (website != null) {
      return website!!
    }
    website =
      Builder(
          name = "Website",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(18.3334f, 10.0f)
            curveTo(18.3334f, 14.6024f, 14.6025f, 18.3333f, 10.0001f, 18.3333f)
            curveTo(5.3977f, 18.3333f, 1.6667f, 14.6024f, 1.6667f, 10.0f)
            curveTo(1.6667f, 5.3976f, 5.3977f, 1.6667f, 10.0001f, 1.6667f)
            curveTo(14.6025f, 1.6667f, 18.3334f, 5.3976f, 18.3334f, 10.0f)
            close()
            moveTo(10.0001f, 16.6667f)
            curveTo(10.0f, 16.6667f, 10.0001f, 16.6667f, 10.0001f, 16.6667f)
            curveTo(6.3182f, 16.6667f, 3.3334f, 13.6819f, 3.3334f, 10.0f)
            curveTo(3.3334f, 9.7335f, 3.3491f, 9.4707f, 3.3795f, 9.2124f)
            lineTo(8.3336f, 14.1665f)
            verticalLineTo(14.9998f)
            curveTo(8.3336f, 15.4601f, 8.7067f, 15.8332f, 9.1669f, 15.8332f)
            horizontalLineTo(10.0002f)
            lineTo(10.0001f, 16.6667f)
            close()
            moveTo(11.667f, 16.4566f)
            verticalLineTo(14.9998f)
            curveTo(11.667f, 14.5396f, 11.2939f, 14.1665f, 10.8336f, 14.1665f)
            horizontalLineTo(10.0002f)
            verticalLineTo(13.4761f)
            lineTo(9.0238f, 12.4997f)
            lineTo(12.5002f, 12.4998f)
            verticalLineTo(13.3332f)
            curveTo(12.5002f, 13.7934f, 12.8733f, 14.1665f, 13.3336f, 14.1665f)
            horizontalLineTo(15.2047f)
            curveTo(14.317f, 15.274f, 13.083f, 16.0921f, 11.667f, 16.4566f)
            close()
            moveTo(16.1822f, 12.4998f)
            horizontalLineTo(14.1669f)
            verticalLineTo(11.6665f)
            curveTo(14.1669f, 11.2062f, 13.7938f, 10.8331f, 13.3336f, 10.8331f)
            lineTo(6.6668f, 10.833f)
            verticalLineTo(9.1665f)
            horizontalLineTo(9.167f)
            curveTo(9.6272f, 9.1665f, 10.0003f, 8.7934f, 10.0003f, 8.3331f)
            verticalLineTo(6.6665f)
            horizontalLineTo(10.8334f)
            curveTo(11.7539f, 6.6665f, 12.5001f, 5.9203f, 12.5001f, 4.9998f)
            verticalLineTo(3.8179f)
            curveTo(14.9433f, 4.8069f, 16.6667f, 7.2022f, 16.6667f, 10.0f)
            curveTo(16.6667f, 10.884f, 16.4947f, 11.7278f, 16.1822f, 12.4998f)
            close()
            moveTo(10.8334f, 3.3849f)
            verticalLineTo(4.9998f)
            horizontalLineTo(9.167f)
            curveTo(8.7067f, 4.9998f, 8.3336f, 5.3729f, 8.3336f, 5.8331f)
            verticalLineTo(7.4998f)
            horizontalLineTo(5.9763f)
            curveTo(5.4372f, 7.4998f, 5.0001f, 7.9369f, 5.0001f, 8.476f)
            lineTo(3.8788f, 7.3547f)
            curveTo(4.9027f, 4.9888f, 7.258f, 3.3333f, 10.0001f, 3.3333f)
            curveTo(10.2823f, 3.3333f, 10.5604f, 3.3509f, 10.8334f, 3.3849f)
            close()
          }
        }
        .build()
    return website!!
  }

private var website: ImageVector? = null
