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

val TwineIcons.RemoveFeed: ImageVector
  get() {
    if (_Delete != null) {
      return _Delete!!
    }
    _Delete =
      ImageVector.Builder(
          name = "RemoveFeed",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF4C4639))) {
            moveTo(14.5f, 2.5f)
            curveTo(16.157f, 2.5f, 17.5f, 3.843f, 17.5f, 5.5f)
            verticalLineTo(14.5f)
            curveTo(17.5f, 16.157f, 16.157f, 17.5f, 14.5f, 17.5f)
            horizontalLineTo(5.5f)
            curveTo(3.843f, 17.5f, 2.5f, 16.157f, 2.5f, 14.5f)
            verticalLineTo(5.5f)
            curveTo(2.5f, 3.843f, 3.843f, 2.5f, 5.5f, 2.5f)
            horizontalLineTo(14.5f)
            close()
            moveTo(5.5f, 4f)
            curveTo(4.672f, 4f, 4f, 4.672f, 4f, 5.5f)
            verticalLineTo(14.5f)
            curveTo(4f, 15.328f, 4.672f, 16f, 5.5f, 16f)
            horizontalLineTo(14.5f)
            curveTo(15.328f, 16f, 16f, 15.328f, 16f, 14.5f)
            verticalLineTo(5.5f)
            curveTo(16f, 4.672f, 15.328f, 4f, 14.5f, 4f)
            horizontalLineTo(5.5f)
            close()
            moveTo(12.229f, 7.021f)
            curveTo(12.424f, 7.021f, 12.597f, 7.097f, 12.75f, 7.25f)
            curveTo(12.903f, 7.403f, 12.979f, 7.576f, 12.979f, 7.771f)
            curveTo(12.979f, 7.965f, 12.903f, 8.139f, 12.75f, 8.292f)
            lineTo(11.063f, 10f)
            lineTo(12.75f, 11.708f)
            curveTo(12.903f, 11.861f, 12.979f, 12.035f, 12.979f, 12.229f)
            curveTo(12.979f, 12.424f, 12.903f, 12.597f, 12.75f, 12.75f)
            curveTo(12.597f, 12.903f, 12.424f, 12.979f, 12.229f, 12.979f)
            curveTo(12.035f, 12.979f, 11.861f, 12.903f, 11.708f, 12.75f)
            lineTo(10f, 11.063f)
            lineTo(8.292f, 12.75f)
            curveTo(8.139f, 12.903f, 7.965f, 12.979f, 7.771f, 12.979f)
            curveTo(7.576f, 12.979f, 7.403f, 12.903f, 7.25f, 12.75f)
            curveTo(7.097f, 12.597f, 7.021f, 12.424f, 7.021f, 12.229f)
            curveTo(7.021f, 12.035f, 7.097f, 11.861f, 7.25f, 11.708f)
            lineTo(8.938f, 10f)
            lineTo(7.25f, 8.292f)
            curveTo(7.097f, 8.139f, 7.021f, 7.965f, 7.021f, 7.771f)
            curveTo(7.021f, 7.576f, 7.097f, 7.403f, 7.25f, 7.25f)
            curveTo(7.403f, 7.097f, 7.576f, 7.021f, 7.771f, 7.021f)
            curveTo(7.965f, 7.021f, 8.139f, 7.097f, 8.292f, 7.25f)
            lineTo(10f, 8.938f)
            lineTo(11.708f, 7.25f)
            curveTo(11.861f, 7.097f, 12.035f, 7.021f, 12.229f, 7.021f)
            close()
          }
        }
        .build()

    return _Delete!!
  }

@Suppress("ObjectPropertyName") private var _Delete: ImageVector? = null
