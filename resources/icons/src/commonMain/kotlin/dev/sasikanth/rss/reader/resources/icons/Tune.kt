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

val TwineIcons.Tune: ImageVector
  get() {
    if (tune != null) {
      return tune!!
    }
    tune =
      Builder(
          name = "Tune",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(11.0f, 21.0f)
            verticalLineTo(15.0f)
            horizontalLineTo(13.0f)
            verticalLineTo(17.0f)
            horizontalLineTo(21.0f)
            verticalLineTo(19.0f)
            horizontalLineTo(13.0f)
            verticalLineTo(21.0f)
            horizontalLineTo(11.0f)
            close()
            moveTo(3.0f, 19.0f)
            verticalLineTo(17.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(19.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(7.0f, 15.0f)
            verticalLineTo(13.0f)
            horizontalLineTo(3.0f)
            verticalLineTo(11.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(15.0f)
            horizontalLineTo(7.0f)
            close()
            moveTo(11.0f, 13.0f)
            verticalLineTo(11.0f)
            horizontalLineTo(21.0f)
            verticalLineTo(13.0f)
            horizontalLineTo(11.0f)
            close()
            moveTo(15.0f, 9.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(17.0f)
            verticalLineTo(5.0f)
            horizontalLineTo(21.0f)
            verticalLineTo(7.0f)
            horizontalLineTo(17.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(15.0f)
            close()
            moveTo(3.0f, 7.0f)
            verticalLineTo(5.0f)
            horizontalLineTo(13.0f)
            verticalLineTo(7.0f)
            horizontalLineTo(3.0f)
            close()
          }
        }
        .build()
    return tune!!
  }

private var tune: ImageVector? = null
