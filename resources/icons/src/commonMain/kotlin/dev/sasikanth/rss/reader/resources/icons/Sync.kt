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

val TwineIcons.Sync: ImageVector
  get() {
    if (_Sync != null) {
      return _Sync!!
    }
    _Sync =
      ImageVector.Builder(
          name = "Sync",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF211A1D))) {
            moveTo(3.276f, 7.168f)
            curveTo(3.571f, 6.928f, 4.006f, 6.945f, 4.28f, 7.22f)
            lineTo(6.78f, 9.72f)
            curveTo(7.073f, 10.013f, 7.073f, 10.487f, 6.78f, 10.78f)
            curveTo(6.487f, 11.073f, 6.013f, 11.073f, 5.72f, 10.78f)
            lineTo(4.516f, 9.576f)
            curveTo(4.505f, 9.716f, 4.5f, 9.857f, 4.5f, 10f)
            curveTo(4.5f, 13.038f, 6.962f, 15.5f, 10f, 15.5f)
            curveTo(10.771f, 15.5f, 11.502f, 15.342f, 12.166f, 15.058f)
            curveTo(12.547f, 14.894f, 12.987f, 15.071f, 13.15f, 15.451f)
            curveTo(13.314f, 15.832f, 13.137f, 16.272f, 12.757f, 16.435f)
            curveTo(11.91f, 16.799f, 10.977f, 17f, 10f, 17f)
            curveTo(6.134f, 17f, 3f, 13.866f, 3f, 10f)
            curveTo(3f, 9.847f, 3.006f, 9.695f, 3.016f, 9.544f)
            lineTo(1.78f, 10.78f)
            curveTo(1.487f, 11.073f, 1.013f, 11.073f, 0.72f, 10.78f)
            curveTo(0.427f, 10.487f, 0.427f, 10.013f, 0.72f, 9.72f)
            lineTo(3.22f, 7.22f)
            lineTo(3.276f, 7.168f)
            close()
            moveTo(10f, 3f)
            curveTo(13.866f, 3f, 17f, 6.134f, 17f, 10f)
            curveTo(17f, 10.153f, 16.993f, 10.304f, 16.983f, 10.455f)
            lineTo(18.22f, 9.22f)
            curveTo(18.513f, 8.927f, 18.987f, 8.927f, 19.28f, 9.22f)
            curveTo(19.573f, 9.513f, 19.573f, 9.987f, 19.28f, 10.28f)
            lineTo(16.78f, 12.78f)
            lineTo(16.724f, 12.832f)
            curveTo(16.429f, 13.072f, 15.994f, 13.055f, 15.72f, 12.78f)
            lineTo(13.22f, 10.28f)
            curveTo(12.927f, 9.987f, 12.927f, 9.513f, 13.22f, 9.22f)
            curveTo(13.513f, 8.927f, 13.987f, 8.927f, 14.28f, 9.22f)
            lineTo(15.483f, 10.423f)
            curveTo(15.494f, 10.283f, 15.5f, 10.142f, 15.5f, 10f)
            curveTo(15.5f, 6.962f, 13.038f, 4.5f, 10f, 4.5f)
            curveTo(9.229f, 4.5f, 8.498f, 4.658f, 7.834f, 4.942f)
            curveTo(7.453f, 5.106f, 7.013f, 4.929f, 6.85f, 4.549f)
            curveTo(6.686f, 4.168f, 6.862f, 3.728f, 7.243f, 3.564f)
            curveTo(8.09f, 3.201f, 9.023f, 3f, 10f, 3f)
            close()
          }
        }
        .build()

    return _Sync!!
  }

@Suppress("ObjectPropertyName") private var _Sync: ImageVector? = null
