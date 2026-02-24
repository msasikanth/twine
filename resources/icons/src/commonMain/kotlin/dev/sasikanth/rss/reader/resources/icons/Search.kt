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

val TwineIcons.Search: ImageVector
  get() {
    if (_Search != null) {
      return _Search!!
    }
    _Search =
      ImageVector.Builder(
          name = "Search",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF514347))) {
            moveTo(8.001f, 13f)
            curveTo(6.611f, 13f, 5.431f, 12.514f, 4.458f, 11.542f)
            curveTo(3.486f, 10.569f, 3f, 9.389f, 3f, 8f)
            curveTo(3f, 6.611f, 3.486f, 5.431f, 4.458f, 4.458f)
            curveTo(5.431f, 3.486f, 6.611f, 3f, 8f, 3f)
            curveTo(9.389f, 3f, 10.569f, 3.486f, 11.542f, 4.458f)
            curveTo(12.514f, 5.431f, 13f, 6.611f, 13f, 8.001f)
            curveTo(13f, 8.562f, 12.913f, 9.091f, 12.74f, 9.588f)
            curveTo(12.566f, 10.085f, 12.326f, 10.542f, 12.021f, 10.958f)
            lineTo(16.479f, 15.417f)
            curveTo(16.632f, 15.569f, 16.708f, 15.743f, 16.708f, 15.938f)
            curveTo(16.708f, 16.132f, 16.632f, 16.306f, 16.479f, 16.458f)
            curveTo(16.326f, 16.611f, 16.149f, 16.688f, 15.948f, 16.688f)
            curveTo(15.747f, 16.688f, 15.569f, 16.611f, 15.417f, 16.458f)
            lineTo(10.958f, 12.021f)
            curveTo(10.542f, 12.326f, 10.085f, 12.566f, 9.588f, 12.74f)
            curveTo(9.091f, 12.913f, 8.562f, 13f, 8.001f, 13f)
            close()
            moveTo(8f, 11.5f)
            curveTo(8.972f, 11.5f, 9.799f, 11.16f, 10.479f, 10.479f)
            curveTo(11.16f, 9.799f, 11.5f, 8.972f, 11.5f, 8f)
            curveTo(11.5f, 7.028f, 11.16f, 6.201f, 10.479f, 5.521f)
            curveTo(9.799f, 4.84f, 8.972f, 4.5f, 8f, 4.5f)
            curveTo(7.028f, 4.5f, 6.201f, 4.84f, 5.521f, 5.521f)
            curveTo(4.84f, 6.201f, 4.5f, 7.028f, 4.5f, 8f)
            curveTo(4.5f, 8.972f, 4.84f, 9.799f, 5.521f, 10.479f)
            curveTo(6.201f, 11.16f, 7.028f, 11.5f, 8f, 11.5f)
            close()
          }
        }
        .build()

    return _Search!!
  }

@Suppress("ObjectPropertyName") private var _Search: ImageVector? = null
