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

val TwineIcons.NewTag: ImageVector
  get() {
    if (newTag != null) {
      return newTag!!
    }
    newTag =
      Builder(
          name = "NewTag",
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
            moveTo(5.0f, 6.0f)
            curveTo(5.0f, 5.4477f, 5.4477f, 5.0f, 6.0f, 5.0f)
            horizontalLineTo(11.3551f)
            curveTo(11.7529f, 5.0f, 12.1344f, 5.158f, 12.4157f, 5.4393f)
            lineTo(19.7087f, 12.7323f)
            curveTo(20.0992f, 13.1228f, 20.0992f, 13.756f, 19.7087f, 14.1465f)
            lineTo(18.9734f, 14.8818f)
            curveTo(18.5829f, 15.2723f, 18.5829f, 15.9055f, 18.9734f, 16.296f)
            curveTo(19.364f, 16.6865f, 19.9971f, 16.6865f, 20.3876f, 16.296f)
            lineTo(21.1229f, 15.5607f)
            curveTo(22.2945f, 14.3892f, 22.2945f, 12.4897f, 21.1229f, 11.3181f)
            lineTo(13.8299f, 4.0251f)
            curveTo(13.1736f, 3.3688f, 12.2833f, 3.0f, 11.3551f, 3.0f)
            horizontalLineTo(6.0f)
            curveTo(4.3431f, 3.0f, 3.0f, 4.3432f, 3.0f, 6.0f)
            verticalLineTo(11.3551f)
            curveTo(3.0f, 12.2833f, 3.3688f, 13.1736f, 4.0251f, 13.8299f)
            lineTo(5.8905f, 15.6953f)
            curveTo(6.281f, 16.0858f, 6.9142f, 16.0858f, 7.3047f, 15.6953f)
            curveTo(7.6952f, 15.3048f, 7.6952f, 14.6716f, 7.3047f, 14.2811f)
            lineTo(5.4393f, 12.4157f)
            curveTo(5.158f, 12.1344f, 5.0f, 11.7529f, 5.0f, 11.3551f)
            verticalLineTo(6.0f)
            close()
            moveTo(9.3f, 8.0f)
            curveTo(9.3f, 8.718f, 8.718f, 9.3f, 8.0f, 9.3f)
            curveTo(7.282f, 9.3f, 6.7f, 8.718f, 6.7f, 8.0f)
            curveTo(6.7f, 7.282f, 7.282f, 6.7f, 8.0f, 6.7f)
            curveTo(8.718f, 6.7f, 9.3f, 7.282f, 9.3f, 8.0f)
            close()
            moveTo(13.0f, 14.0f)
            curveTo(13.5523f, 14.0f, 14.0f, 14.4477f, 14.0f, 15.0f)
            verticalLineTo(18.0f)
            horizontalLineTo(17.0f)
            curveTo(17.5523f, 18.0f, 18.0f, 18.4477f, 18.0f, 19.0f)
            curveTo(18.0f, 19.5523f, 17.5523f, 20.0f, 17.0f, 20.0f)
            horizontalLineTo(14.0f)
            verticalLineTo(23.0f)
            curveTo(14.0f, 23.5523f, 13.5523f, 24.0f, 13.0f, 24.0f)
            curveTo(12.4477f, 24.0f, 12.0f, 23.5523f, 12.0f, 23.0f)
            verticalLineTo(20.0f)
            horizontalLineTo(9.0f)
            curveTo(8.4477f, 20.0f, 8.0f, 19.5523f, 8.0f, 19.0f)
            curveTo(8.0f, 18.4477f, 8.4477f, 18.0f, 9.0f, 18.0f)
            horizontalLineTo(12.0f)
            verticalLineTo(15.0f)
            curveTo(12.0f, 14.4477f, 12.4477f, 14.0f, 13.0f, 14.0f)
            close()
          }
        }
        .build()
    return newTag!!
  }

private var newTag: ImageVector? = null
