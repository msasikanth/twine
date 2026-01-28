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

val TwineIcons.MarkAllAsRead: ImageVector
  get() {
    if (markAllAsRead != null) {
      return markAllAsRead!!
    }
    markAllAsRead =
      Builder(
          name = "MarkAllAsRead",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF45483C)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(16.927f, 10.125f)
            curveTo(17.128f, 10.125f, 17.305f, 10.203f, 17.458f, 10.357f)
            curveTo(17.611f, 10.511f, 17.688f, 10.691f, 17.688f, 10.897f)
            curveTo(17.688f, 11.103f, 17.611f, 11.283f, 17.458f, 11.437f)
            lineTo(13.917f, 14.979f)
            curveTo(13.769f, 15.132f, 13.596f, 15.208f, 13.399f, 15.208f)
            curveTo(13.203f, 15.208f, 13.028f, 15.132f, 12.875f, 14.979f)
            lineTo(11.083f, 13.187f)
            curveTo(10.931f, 13.034f, 10.854f, 12.861f, 10.854f, 12.667f)
            curveTo(10.854f, 12.472f, 10.93f, 12.299f, 11.083f, 12.146f)
            curveTo(11.236f, 11.993f, 11.413f, 11.917f, 11.614f, 11.917f)
            curveTo(11.816f, 11.917f, 11.995f, 11.996f, 12.152f, 12.153f)
            lineTo(13.375f, 13.375f)
            lineTo(16.396f, 10.354f)
            curveTo(16.548f, 10.201f, 16.726f, 10.125f, 16.927f, 10.125f)
            close()
            moveTo(8.25f, 12.5f)
            curveTo(8.462f, 12.5f, 8.64f, 12.571f, 8.784f, 12.714f)
            curveTo(8.928f, 12.857f, 9.0f, 13.034f, 9.0f, 13.246f)
            curveTo(9.0f, 13.457f, 8.928f, 13.635f, 8.784f, 13.781f)
            curveTo(8.64f, 13.927f, 8.462f, 14.0f, 8.25f, 14.0f)
            horizontalLineTo(2.75f)
            curveTo(2.538f, 14.0f, 2.36f, 13.929f, 2.216f, 13.786f)
            curveTo(2.072f, 13.643f, 2.0f, 13.466f, 2.0f, 13.254f)
            curveTo(2.0f, 13.043f, 2.072f, 12.864f, 2.216f, 12.718f)
            curveTo(2.36f, 12.573f, 2.538f, 12.5f, 2.75f, 12.5f)
            horizontalLineTo(8.25f)
            close()
            moveTo(16.927f, 3.635f)
            curveTo(17.128f, 3.628f, 17.305f, 3.703f, 17.458f, 3.857f)
            curveTo(17.611f, 4.011f, 17.688f, 4.191f, 17.688f, 4.397f)
            curveTo(17.688f, 4.603f, 17.611f, 4.783f, 17.458f, 4.937f)
            lineTo(13.917f, 8.479f)
            curveTo(13.769f, 8.632f, 13.596f, 8.709f, 13.399f, 8.709f)
            curveTo(13.203f, 8.708f, 13.028f, 8.632f, 12.875f, 8.479f)
            lineTo(11.083f, 6.687f)
            curveTo(10.931f, 6.534f, 10.857f, 6.357f, 10.864f, 6.156f)
            curveTo(10.871f, 5.955f, 10.952f, 5.777f, 11.104f, 5.625f)
            curveTo(11.257f, 5.472f, 11.434f, 5.396f, 11.636f, 5.396f)
            curveTo(11.837f, 5.396f, 12.014f, 5.472f, 12.167f, 5.625f)
            lineTo(13.396f, 6.875f)
            lineTo(16.396f, 3.875f)
            curveTo(16.548f, 3.722f, 16.726f, 3.642f, 16.927f, 3.635f)
            close()
            moveTo(8.25f, 6.0f)
            curveTo(8.462f, 6.0f, 8.64f, 6.072f, 8.784f, 6.214f)
            curveTo(8.928f, 6.357f, 9.0f, 6.534f, 9.0f, 6.746f)
            curveTo(9.0f, 6.957f, 8.928f, 7.135f, 8.784f, 7.281f)
            curveTo(8.64f, 7.427f, 8.462f, 7.5f, 8.25f, 7.5f)
            horizontalLineTo(2.75f)
            curveTo(2.538f, 7.5f, 2.36f, 7.429f, 2.216f, 7.286f)
            curveTo(2.072f, 7.143f, 2.0f, 6.966f, 2.0f, 6.754f)
            curveTo(2.0f, 6.543f, 2.072f, 6.364f, 2.216f, 6.218f)
            curveTo(2.36f, 6.073f, 2.538f, 6.0f, 2.75f, 6.0f)
            horizontalLineTo(8.25f)
            close()
          }
        }
        .build()
    return markAllAsRead!!
  }

private var markAllAsRead: ImageVector? = null
