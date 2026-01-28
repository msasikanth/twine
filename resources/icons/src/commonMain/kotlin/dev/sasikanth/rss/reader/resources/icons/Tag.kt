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

val TwineIcons.Tag: ImageVector
  get() {
    if (tag != null) {
      return tag!!
    }
    tag =
      Builder(
          name = "Tag",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
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
            moveTo(6.0f, 5.0f)
            curveTo(5.4477f, 5.0f, 5.0f, 5.4477f, 5.0f, 6.0f)
            verticalLineTo(11.3551f)
            curveTo(5.0f, 11.7529f, 5.158f, 12.1344f, 5.4393f, 12.4157f)
            lineTo(12.7323f, 19.7087f)
            curveTo(13.1228f, 20.0992f, 13.756f, 20.0992f, 14.1465f, 19.7087f)
            lineTo(19.7087f, 14.1465f)
            curveTo(20.0992f, 13.756f, 20.0992f, 13.1228f, 19.7087f, 12.7323f)
            lineTo(12.4157f, 5.4393f)
            curveTo(12.1344f, 5.158f, 11.7529f, 5.0f, 11.3551f, 5.0f)
            horizontalLineTo(6.0f)
            close()
            moveTo(3.0f, 6.0f)
            curveTo(3.0f, 4.3432f, 4.3431f, 3.0f, 6.0f, 3.0f)
            horizontalLineTo(11.3551f)
            curveTo(12.2833f, 3.0f, 13.1736f, 3.3688f, 13.8299f, 4.0251f)
            lineTo(21.1229f, 11.3181f)
            curveTo(22.2945f, 12.4897f, 22.2945f, 14.3892f, 21.1229f, 15.5607f)
            lineTo(15.5607f, 21.1229f)
            curveTo(14.3892f, 22.2945f, 12.4897f, 22.2945f, 11.3181f, 21.1229f)
            lineTo(4.0251f, 13.8299f)
            curveTo(3.3688f, 13.1736f, 3.0f, 12.2833f, 3.0f, 11.3551f)
            verticalLineTo(6.0f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(9.3f, 7.9999f)
            curveTo(9.3f, 8.7179f, 8.718f, 9.2999f, 8.0f, 9.2999f)
            curveTo(7.282f, 9.2999f, 6.7f, 8.7179f, 6.7f, 7.9999f)
            curveTo(6.7f, 7.282f, 7.282f, 6.7f, 8.0f, 6.7f)
            curveTo(8.718f, 6.7f, 9.3f, 7.282f, 9.3f, 7.9999f)
            close()
          }
        }
        .build()
    return tag!!
  }

private var tag: ImageVector? = null
