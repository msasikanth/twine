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

val TwineIcons.Account: ImageVector
  get() {
    if (_Account != null) {
      return _Account!!
    }
    _Account =
      ImageVector.Builder(
          name = "Account",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF211A1D))) {
            moveTo(10f, 11f)
            curveTo(13.5f, 11f, 17f, 12f, 17f, 15f)
            curveTo(17f, 16.381f, 15.881f, 17.5f, 14.5f, 17.5f)
            horizontalLineTo(5.5f)
            curveTo(4.119f, 17.5f, 3f, 16.381f, 3f, 15f)
            curveTo(3f, 12f, 6.5f, 11f, 10f, 11f)
            close()
            moveTo(10f, 12.5f)
            curveTo(8.353f, 12.5f, 6.857f, 12.741f, 5.832f, 13.229f)
            curveTo(4.872f, 13.687f, 4.5f, 14.246f, 4.5f, 15f)
            curveTo(4.5f, 15.552f, 4.948f, 16f, 5.5f, 16f)
            horizontalLineTo(14.5f)
            curveTo(15.052f, 16f, 15.5f, 15.552f, 15.5f, 15f)
            curveTo(15.5f, 14.246f, 15.128f, 13.687f, 14.168f, 13.229f)
            curveTo(13.143f, 12.741f, 11.647f, 12.5f, 10f, 12.5f)
            close()
            moveTo(10f, 2.5f)
            curveTo(11.933f, 2.5f, 13.5f, 4.067f, 13.5f, 6f)
            curveTo(13.5f, 7.933f, 11.933f, 9.5f, 10f, 9.5f)
            curveTo(8.067f, 9.5f, 6.5f, 7.933f, 6.5f, 6f)
            curveTo(6.5f, 4.067f, 8.067f, 2.5f, 10f, 2.5f)
            close()
            moveTo(10f, 4f)
            curveTo(8.895f, 4f, 8f, 4.895f, 8f, 6f)
            curveTo(8f, 7.105f, 8.895f, 8f, 10f, 8f)
            curveTo(11.105f, 8f, 12f, 7.105f, 12f, 6f)
            curveTo(12f, 4.895f, 11.105f, 4f, 10f, 4f)
            close()
          }
        }
        .build()

    return _Account!!
  }

@Suppress("ObjectPropertyName") private var _Account: ImageVector? = null
