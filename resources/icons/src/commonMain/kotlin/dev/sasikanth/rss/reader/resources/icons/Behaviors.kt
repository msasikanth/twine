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

val TwineIcons.Behaviors: ImageVector
  get() {
    if (_Behaviors != null) {
      return _Behaviors!!
    }
    _Behaviors =
      ImageVector.Builder(
          name = "Behaviors",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF211A1D))) {
            moveTo(14.257f, 5.007f)
            curveTo(16.899f, 5.141f, 19f, 7.325f, 19f, 10f)
            curveTo(19f, 12.675f, 16.899f, 14.859f, 14.257f, 14.993f)
            lineTo(14f, 15f)
            horizontalLineTo(6f)
            curveTo(3.239f, 15f, 1f, 12.761f, 1f, 10f)
            curveTo(1f, 7.239f, 3.239f, 5f, 6f, 5f)
            horizontalLineTo(14f)
            lineTo(14.257f, 5.007f)
            close()
            moveTo(6f, 6.5f)
            curveTo(4.067f, 6.5f, 2.5f, 8.067f, 2.5f, 10f)
            curveTo(2.5f, 11.933f, 4.067f, 13.5f, 6f, 13.5f)
            horizontalLineTo(14f)
            curveTo(15.933f, 13.5f, 17.5f, 11.933f, 17.5f, 10f)
            curveTo(17.5f, 8.067f, 15.933f, 6.5f, 14f, 6.5f)
            horizontalLineTo(6f)
            close()
            moveTo(14f, 8f)
            curveTo(15.105f, 8f, 16f, 8.895f, 16f, 10f)
            curveTo(16f, 11.105f, 15.105f, 12f, 14f, 12f)
            curveTo(12.895f, 12f, 12f, 11.105f, 12f, 10f)
            curveTo(12f, 8.895f, 12.895f, 8f, 14f, 8f)
            close()
          }
        }
        .build()

    return _Behaviors!!
  }

@Suppress("ObjectPropertyName") private var _Behaviors: ImageVector? = null
