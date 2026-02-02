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

val TwineIcons.DeleteOutline: ImageVector
  get() {
    if (deleteOutline != null) {
      return deleteOutline!!
    }
    deleteOutline =
      Builder(
          name = "Delete",
          defaultWidth = 21.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 21.0f,
          viewportHeight = 20.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF00E0BB)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(7.8334f, 2.5001f)
            verticalLineTo(3.3335f)
            horizontalLineTo(3.6667f)
            verticalLineTo(5.0001f)
            horizontalLineTo(4.5001f)
            verticalLineTo(15.8335f)
            curveTo(4.5001f, 16.2755f, 4.6757f, 16.6994f, 4.9882f, 17.012f)
            curveTo(5.3008f, 17.3245f, 5.7247f, 17.5001f, 6.1667f, 17.5001f)
            horizontalLineTo(14.5001f)
            curveTo(14.9421f, 17.5001f, 15.366f, 17.3245f, 15.6786f, 17.012f)
            curveTo(15.9912f, 16.6994f, 16.1667f, 16.2755f, 16.1667f, 15.8335f)
            verticalLineTo(5.0001f)
            horizontalLineTo(17.0001f)
            verticalLineTo(3.3335f)
            horizontalLineTo(12.8334f)
            verticalLineTo(2.5001f)
            horizontalLineTo(7.8334f)
            close()
            moveTo(6.1667f, 5.0001f)
            horizontalLineTo(14.5001f)
            verticalLineTo(15.8335f)
            horizontalLineTo(6.1667f)
            verticalLineTo(5.0001f)
            close()
            moveTo(7.8334f, 6.6668f)
            verticalLineTo(14.1668f)
            horizontalLineTo(9.5001f)
            verticalLineTo(6.6668f)
            horizontalLineTo(7.8334f)
            close()
            moveTo(11.1667f, 6.6668f)
            verticalLineTo(14.1668f)
            horizontalLineTo(12.8334f)
            verticalLineTo(6.6668f)
            horizontalLineTo(11.1667f)
            close()
          }
        }
        .build()
    return deleteOutline!!
  }

private var deleteOutline: ImageVector? = null
