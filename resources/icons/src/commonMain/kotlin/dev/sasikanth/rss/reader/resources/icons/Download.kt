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

val TwineIcons.Download: ImageVector
  get() {
    if (_download != null) {
      return _download!!
    }
    _download =
      ImageVector.Builder(
          name = "Download",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF000000))) {
            moveTo(12.0f, 16.0f)
            lineTo(7.0f, 11.0f)
            lineTo(8.4f, 9.55f)
            lineTo(11.0f, 12.15f)
            verticalLineTo(4.0f)
            horizontalLineTo(13.0f)
            verticalLineTo(12.15f)
            lineTo(15.6f, 9.55f)
            lineTo(17.0f, 11.0f)
            lineTo(12.0f, 16.0f)
            close()
            moveTo(6.0f, 20.0f)
            curveTo(5.45f, 20.0f, 4.979f, 19.804f, 4.588f, 19.413f)
            curveTo(4.196f, 19.021f, 4.0f, 18.55f, 4.0f, 18.0f)
            verticalLineTo(15.0f)
            horizontalLineTo(6.0f)
            verticalLineTo(18.0f)
            horizontalLineTo(18.0f)
            verticalLineTo(15.0f)
            horizontalLineTo(20.0f)
            verticalLineTo(18.0f)
            curveTo(20.0f, 18.55f, 19.804f, 19.021f, 19.413f, 19.413f)
            curveTo(19.021f, 19.804f, 18.55f, 20.0f, 18.0f, 20.0f)
            horizontalLineTo(6.0f)
            close()
          }
        }
        .build()
    return _download!!
  }

private var _download: ImageVector? = null
