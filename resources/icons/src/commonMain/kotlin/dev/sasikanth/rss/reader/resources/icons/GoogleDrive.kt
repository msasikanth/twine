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
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.GoogleDrive: ImageVector
  get() {
    if (_googleDrive != null) {
      return _googleDrive!!
    }
    _googleDrive =
      Builder(
          name = "GoogleDrive",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(8.267f, 2.0f)
            lineTo(15.733f, 2.0f)
            lineTo(23.2f, 14.933f)
            lineTo(19.467f, 21.4f)
            lineTo(12.0f, 8.467f)
            lineTo(4.533f, 21.4f)
            lineTo(0.8f, 14.933f)
            lineTo(8.267f, 2.0f)
            close()
            moveTo(9.244f, 14.933f)
            lineTo(14.756f, 14.933f)
            lineTo(18.489f, 21.4f)
            lineTo(5.511f, 21.4f)
            lineTo(9.244f, 14.933f)
            close()
          }
        }
        .build()
    return _googleDrive!!
  }

private var _googleDrive: ImageVector? = null
