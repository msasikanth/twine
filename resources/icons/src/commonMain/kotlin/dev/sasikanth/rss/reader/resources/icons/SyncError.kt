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
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.SyncError: ImageVector
  get() {
    if (_SyncError != null) {
      return _SyncError!!
    }
    _SyncError =
      ImageVector.Builder(
          name = "SyncError",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF211A1D)), pathFillType = PathFillType.EvenOdd) {
            moveTo(10f, 2f)
            curveTo(14.418f, 2f, 18f, 5.582f, 18f, 10f)
            curveTo(18f, 14.418f, 14.418f, 18f, 10f, 18f)
            curveTo(5.582f, 18f, 2f, 14.418f, 2f, 10f)
            curveTo(2f, 5.582f, 5.582f, 2f, 10f, 2f)
            close()
            moveTo(10f, 5.25f)
            curveTo(9.586f, 5.25f, 9.25f, 5.586f, 9.25f, 6f)
            lineTo(9.25f, 11f)
            curveTo(9.25f, 11.414f, 9.586f, 11.75f, 10f, 11.75f)
            curveTo(10.414f, 11.75f, 10.75f, 11.414f, 10.75f, 11f)
            lineTo(10.75f, 6f)
            curveTo(10.75f, 5.586f, 10.414f, 5.25f, 10f, 5.25f)
            close()
            moveTo(10f, 13f)
            curveTo(10.552f, 13f, 11f, 13.448f, 11f, 14f)
            curveTo(11f, 14.552f, 10.552f, 15f, 10f, 15f)
            curveTo(9.448f, 15f, 9f, 14.552f, 9f, 14f)
            curveTo(9f, 13.448f, 9.448f, 13f, 10f, 13f)
            close()
          }
        }
        .build()

    return _SyncError!!
  }

@Suppress("ObjectPropertyName") private var _SyncError: ImageVector? = null
