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

public val TwineIcons.Bazqux: ImageVector
  get() {
    if (_bazqux != null) {
      return _bazqux!!
    }
    _bazqux =
      Builder(
          name = "Bazqux",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd,
          ) {
            moveTo(5.25f, 0.0f)
            lineTo(18.75f, 0.0f)
            curveTo(21.65f, 0.0f, 24.0f, 2.35f, 24.0f, 5.25f)
            lineTo(24.0f, 18.75f)
            curveTo(24.0f, 21.65f, 21.65f, 24.0f, 18.75f, 24.0f)
            lineTo(5.25f, 24.0f)
            curveTo(2.35f, 24.0f, 0.0f, 21.65f, 0.0f, 18.75f)
            lineTo(0.0f, 5.25f)
            curveTo(0.0f, 2.35f, 2.35f, 0.0f, 5.25f, 0.0f)
            close()
            moveTo(19.175f, 18.25f)
            lineTo(14.383f, 18.25f)
            quadTo(13.141f, 16.347f, 12.317f, 15.22f)
            quadTo(11.493f, 14.092f, 10.269f, 12.612f)
            lineTo(9.785f, 12.612f)
            lineTo(9.785f, 16.409f)
            quadTo(9.785f, 16.726f, 9.903f, 16.946f)
            quadTo(10.022f, 17.166f, 10.357f, 17.316f)
            quadTo(10.524f, 17.387f, 10.881f, 17.462f)
            quadTo(11.238f, 17.536f, 11.493f, 17.563f)
            lineTo(11.493f, 18.25f)
            lineTo(4.825f, 18.25f)
            lineTo(4.825f, 17.563f)
            quadTo(5.081f, 17.536f, 5.499f, 17.488f)
            quadTo(5.917f, 17.44f, 6.094f, 17.369f)
            quadTo(6.428f, 17.228f, 6.543f, 17.012f)
            quadTo(6.657f, 16.797f, 6.657f, 16.462f)
            lineTo(6.657f, 7.644f)
            quadTo(6.657f, 7.327f, 6.56f, 7.115f)
            quadTo(6.464f, 6.904f, 6.094f, 6.745f)
            quadTo(5.812f, 6.631f, 5.428f, 6.552f)
            quadTo(5.045f, 6.472f, 4.825f, 6.437f)
            lineTo(4.825f, 5.75f)
            lineTo(11.837f, 5.75f)
            quadTo(14.189f, 5.75f, 15.409f, 6.486f)
            quadTo(16.629f, 7.221f, 16.629f, 8.71f)
            quadTo(16.629f, 10.022f, 15.876f, 10.82f)
            quadTo(15.123f, 11.617f, 13.581f, 12.066f)
            quadTo(14.18f, 12.841f, 14.977f, 13.863f)
            quadTo(15.775f, 14.885f, 16.638f, 15.951f)
            quadTo(16.911f, 16.294f, 17.382f, 16.792f)
            quadTo(17.854f, 17.29f, 18.215f, 17.404f)
            quadTo(18.417f, 17.466f, 18.73f, 17.51f)
            quadTo(19.043f, 17.554f, 19.175f, 17.563f)
            close()
            moveTo(13.176f, 8.956f)
            quadTo(13.176f, 7.706f, 12.476f, 7.129f)
            quadTo(11.775f, 6.552f, 10.48f, 6.552f)
            lineTo(9.785f, 6.552f)
            lineTo(9.785f, 11.784f)
            lineTo(10.454f, 11.784f)
            quadTo(11.749f, 11.784f, 12.462f, 11.106f)
            quadTo(13.176f, 10.428f, 13.176f, 8.956f)
            close()
          }
        }
        .build()
    return _bazqux!!
  }

private var _bazqux: ImageVector? = null
