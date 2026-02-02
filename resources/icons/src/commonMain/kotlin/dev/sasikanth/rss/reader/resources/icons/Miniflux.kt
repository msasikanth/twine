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

public val TwineIcons.Miniflux: ImageVector
  get() {
    if (_miniflux != null) {
      return _miniflux!!
    }
    _miniflux =
      Builder(
          name = "Miniflux",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(6.19f, 4.583f)
            curveTo(6.808f, 4.287f, 7.496f, 4.135f, 8.194f, 4.141f)
            curveTo(9.673f, 4.141f, 10.615f, 4.728f, 11.021f, 5.903f)
            curveTo(11.487f, 5.4f, 12.048f, 4.977f, 12.676f, 4.653f)
            curveTo(13.326f, 4.312f, 14.05f, 4.141f, 14.849f, 4.141f)
            curveTo(15.959f, 4.141f, 16.752f, 4.45f, 17.229f, 5.069f)
            curveTo(17.705f, 5.688f, 17.943f, 6.618f, 17.943f, 7.857f)
            verticalLineTo(14.857f)
            curveTo(17.943f, 15.032f, 17.969f, 15.151f, 18.023f, 15.214f)
            curveTo(18.077f, 15.278f, 18.193f, 15.335f, 18.372f, 15.381f)
            lineTo(19.0f, 15.572f)
            verticalLineTo(16.0f)
            horizontalLineTo(15.279f)
            curveTo(14.955f, 16.0f, 14.723f, 15.889f, 14.581f, 15.667f)
            curveTo(14.439f, 15.444f, 14.368f, 15.111f, 14.366f, 14.666f)
            verticalLineTo(7.429f)
            curveTo(14.366f, 6.714f, 14.28f, 6.206f, 14.107f, 5.905f)
            curveTo(13.934f, 5.603f, 13.645f, 5.453f, 13.239f, 5.452f)
            curveTo(12.595f, 5.452f, 11.909f, 5.801f, 11.182f, 6.498f)
            curveTo(11.258f, 6.948f, 11.294f, 7.402f, 11.29f, 7.857f)
            verticalLineTo(14.857f)
            curveTo(11.29f, 15.032f, 11.317f, 15.151f, 11.371f, 15.214f)
            curveTo(11.425f, 15.278f, 11.541f, 15.335f, 11.72f, 15.381f)
            lineTo(12.346f, 15.572f)
            verticalLineTo(16.0f)
            horizontalLineTo(8.624f)
            curveTo(8.3f, 16.0f, 8.068f, 15.889f, 7.926f, 15.667f)
            curveTo(7.784f, 15.444f, 7.712f, 15.111f, 7.711f, 14.666f)
            verticalLineTo(7.429f)
            curveTo(7.711f, 6.714f, 7.624f, 6.206f, 7.452f, 5.905f)
            curveTo(7.279f, 5.603f, 6.99f, 5.453f, 6.584f, 5.452f)
            curveTo(5.951f, 5.452f, 5.301f, 5.77f, 4.634f, 6.405f)
            verticalLineTo(14.857f)
            curveTo(4.634f, 15.032f, 4.66f, 15.155f, 4.714f, 15.226f)
            curveTo(4.768f, 15.297f, 4.878f, 15.357f, 5.045f, 15.405f)
            lineTo(5.653f, 15.572f)
            verticalLineTo(16.0f)
            horizontalLineTo(0.0f)
            verticalLineTo(15.572f)
            lineTo(0.626f, 15.381f)
            curveTo(0.805f, 15.333f, 0.921f, 15.278f, 0.975f, 15.214f)
            curveTo(1.029f, 15.151f, 1.056f, 15.032f, 1.056f, 14.857f)
            verticalLineTo(5.857f)
            curveTo(1.056f, 5.683f, 1.029f, 5.564f, 0.975f, 5.5f)
            curveTo(0.921f, 5.437f, 0.805f, 5.381f, 0.626f, 5.333f)
            lineTo(0.0f, 5.143f)
            verticalLineTo(4.714f)
            lineTo(4.294f, 4.0f)
            horizontalLineTo(4.598f)
            verticalLineTo(5.667f)
            curveTo(5.062f, 5.231f, 5.6f, 4.865f, 6.19f, 4.583f)
            close()
          }
        }
        .build()
    return _miniflux!!
  }

private var _miniflux: ImageVector? = null
