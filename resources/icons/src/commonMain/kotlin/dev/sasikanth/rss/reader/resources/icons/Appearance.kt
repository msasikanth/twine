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

val TwineIcons.Appearance: ImageVector
  get() {
    if (_Appearance != null) {
      return _Appearance!!
    }
    _Appearance =
      ImageVector.Builder(
          name = "Appearance",
          defaultWidth = 20.dp,
          defaultHeight = 20.dp,
          viewportWidth = 20f,
          viewportHeight = 20f,
        )
        .apply {
          path(fill = SolidColor(Color(0xFF211A1D)), pathFillType = PathFillType.EvenOdd) {
            moveTo(12.75f, 2f)
            curveTo(14.269f, 2f, 15.5f, 3.231f, 15.5f, 4.75f)
            curveTo(15.5f, 6.174f, 14.418f, 7.344f, 13.031f, 7.485f)
            lineTo(12.75f, 7.5f)
            horizontalLineTo(8.25f)
            lineTo(7.969f, 7.485f)
            curveTo(6.835f, 7.37f, 5.906f, 6.567f, 5.604f, 5.5f)
            horizontalLineTo(5f)
            curveTo(4.724f, 5.5f, 4.5f, 5.724f, 4.5f, 6f)
            verticalLineTo(8.5f)
            curveTo(4.5f, 8.776f, 4.724f, 9f, 5f, 9f)
            horizontalLineTo(9.5f)
            curveTo(10.605f, 9f, 11.5f, 9.895f, 11.5f, 11f)
            verticalLineTo(11.631f)
            curveTo(12.373f, 11.94f, 13f, 12.771f, 13f, 13.75f)
            verticalLineTo(16.75f)
            curveTo(13f, 17.993f, 11.993f, 19f, 10.75f, 19f)
            curveTo(9.507f, 19f, 8.5f, 17.993f, 8.5f, 16.75f)
            verticalLineTo(13.75f)
            curveTo(8.5f, 12.771f, 9.127f, 11.94f, 10f, 11.631f)
            verticalLineTo(11f)
            curveTo(10f, 10.724f, 9.776f, 10.5f, 9.5f, 10.5f)
            horizontalLineTo(5f)
            curveTo(3.895f, 10.5f, 3f, 9.605f, 3f, 8.5f)
            verticalLineTo(6f)
            curveTo(3f, 4.895f, 3.895f, 4f, 5f, 4f)
            horizontalLineTo(5.606f)
            curveTo(5.933f, 2.846f, 6.991f, 2f, 8.25f, 2f)
            horizontalLineTo(12.75f)
            close()
            moveTo(10.75f, 13f)
            curveTo(10.336f, 13f, 10f, 13.336f, 10f, 13.75f)
            verticalLineTo(16.75f)
            lineTo(10.004f, 16.827f)
            curveTo(10.042f, 17.205f, 10.362f, 17.5f, 10.75f, 17.5f)
            curveTo(11.138f, 17.5f, 11.458f, 17.205f, 11.496f, 16.827f)
            lineTo(11.5f, 16.75f)
            verticalLineTo(13.75f)
            curveTo(11.5f, 13.336f, 11.164f, 13f, 10.75f, 13f)
            close()
            moveTo(8.25f, 3.5f)
            curveTo(7.56f, 3.5f, 7f, 4.06f, 7f, 4.75f)
            curveTo(7f, 5.44f, 7.56f, 6f, 8.25f, 6f)
            horizontalLineTo(12.75f)
            curveTo(13.44f, 6f, 14f, 5.44f, 14f, 4.75f)
            curveTo(14f, 4.06f, 13.44f, 3.5f, 12.75f, 3.5f)
            horizontalLineTo(8.25f)
            close()
          }
        }
        .build()

    return _Appearance!!
  }

@Suppress("ObjectPropertyName") private var _Appearance: ImageVector? = null
