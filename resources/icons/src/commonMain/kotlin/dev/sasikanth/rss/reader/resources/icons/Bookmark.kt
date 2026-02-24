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

val TwineIcons.Bookmark: ImageVector
  get() {
    if (_Bookmarks != null) {
      return _Bookmarks!!
    }
    _Bookmarks =
      ImageVector.Builder(
          name = "Bookmarks",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF514347))) {
            moveTo(4f, 5.5f)
            curveTo(4f, 4.119f, 5.119f, 3f, 6.5f, 3f)
            verticalLineTo(4.5f)
            lineTo(6.397f, 4.505f)
            curveTo(5.893f, 4.556f, 5.5f, 4.982f, 5.5f, 5.5f)
            verticalLineTo(16.5f)
            lineTo(10f, 14.25f)
            lineTo(14.5f, 16.5f)
            verticalLineTo(5.5f)
            curveTo(14.5f, 4.948f, 14.052f, 4.5f, 13.5f, 4.5f)
            verticalLineTo(3f)
            curveTo(14.881f, 3f, 16f, 4.119f, 16f, 5.5f)
            verticalLineTo(16.5f)
            curveTo(16f, 17.02f, 15.731f, 17.503f, 15.289f, 17.776f)
            curveTo(14.847f, 18.05f, 14.294f, 18.074f, 13.829f, 17.842f)
            lineTo(10f, 15.927f)
            lineTo(6.171f, 17.842f)
            curveTo(5.706f, 18.074f, 5.153f, 18.05f, 4.711f, 17.776f)
            curveTo(4.269f, 17.503f, 4f, 17.02f, 4f, 16.5f)
            verticalLineTo(5.5f)
            close()
            moveTo(13.5f, 3f)
            verticalLineTo(4.5f)
            horizontalLineTo(6.5f)
            verticalLineTo(3f)
            horizontalLineTo(13.5f)
            close()
          }
        }
        .build()

    return _Bookmarks!!
  }

@Suppress("ObjectPropertyName") private var _Bookmarks: ImageVector? = null
