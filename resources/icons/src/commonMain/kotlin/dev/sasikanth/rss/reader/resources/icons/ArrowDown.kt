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

val TwineIcons.ArrowDown: ImageVector
  get() {
    if (_ArrowDown != null) {
      return _ArrowDown!!
    }
    _ArrowDown =
      ImageVector.Builder(
          name = "ArrowDown",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF1E1B13))) {
            moveTo(10f, 14.462f)
            curveTo(9.868f, 14.462f, 9.745f, 14.441f, 9.63f, 14.4f)
            curveTo(9.515f, 14.359f, 9.408f, 14.289f, 9.309f, 14.191f)
            lineTo(4.771f, 9.652f)
            curveTo(4.59f, 9.472f, 4.5f, 9.241f, 4.5f, 8.962f)
            curveTo(4.5f, 8.682f, 4.59f, 8.452f, 4.771f, 8.271f)
            curveTo(4.952f, 8.09f, 5.182f, 8f, 5.462f, 8f)
            curveTo(5.741f, 8f, 5.972f, 8.09f, 6.152f, 8.271f)
            lineTo(10f, 12.119f)
            lineTo(13.847f, 8.271f)
            curveTo(14.028f, 8.09f, 14.259f, 8f, 14.538f, 8f)
            curveTo(14.818f, 8f, 15.048f, 8.09f, 15.229f, 8.271f)
            curveTo(15.41f, 8.452f, 15.5f, 8.682f, 15.5f, 8.962f)
            curveTo(15.5f, 9.241f, 15.41f, 9.472f, 15.229f, 9.652f)
            lineTo(10.691f, 14.191f)
            curveTo(10.592f, 14.289f, 10.485f, 14.359f, 10.37f, 14.4f)
            curveTo(10.255f, 14.441f, 10.132f, 14.462f, 10f, 14.462f)
            close()
          }
        }
        .build()

    return _ArrowDown!!
  }

@Suppress("ObjectPropertyName") private var _ArrowDown: ImageVector? = null
