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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.ArrowUp: ImageVector
  get() {
    if (_ArrowUp != null) {
      return _ArrowUp!!
    }
    _ArrowUp =
      ImageVector.Builder(
          name = "ArrowUp",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF1E1B13))) {
            moveTo(10f, 6.54f)
            curveTo(9.868f, 6.54f, 9.745f, 6.561f, 9.63f, 6.602f)
            curveTo(9.515f, 6.643f, 9.408f, 6.713f, 9.309f, 6.811f)
            lineTo(4.771f, 11.349f)
            curveTo(4.59f, 11.53f, 4.5f, 11.76f, 4.5f, 12.04f)
            curveTo(4.5f, 12.319f, 4.59f, 12.55f, 4.771f, 12.731f)
            curveTo(4.952f, 12.911f, 5.182f, 13.002f, 5.462f, 13.002f)
            curveTo(5.741f, 13.002f, 5.972f, 12.911f, 6.152f, 12.731f)
            lineTo(10f, 8.883f)
            lineTo(13.847f, 12.731f)
            curveTo(14.028f, 12.911f, 14.259f, 13.002f, 14.538f, 13.002f)
            curveTo(14.818f, 13.002f, 15.048f, 12.911f, 15.229f, 12.731f)
            curveTo(15.41f, 12.55f, 15.5f, 12.319f, 15.5f, 12.04f)
            curveTo(15.5f, 11.76f, 15.41f, 11.53f, 15.229f, 11.349f)
            lineTo(10.691f, 6.811f)
            curveTo(10.592f, 6.713f, 10.485f, 6.643f, 10.37f, 6.602f)
            curveTo(10.255f, 6.561f, 10.132f, 6.54f, 10f, 6.54f)
            close()
          }
        }
        .build()

    return _ArrowUp!!
  }

@Suppress("ObjectPropertyName") private var _ArrowUp: ImageVector? = null
