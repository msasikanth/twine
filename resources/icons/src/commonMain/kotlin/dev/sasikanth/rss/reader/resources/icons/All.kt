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

val TwineIcons.All: ImageVector
  get() {
    if (all != null) {
      return all!!
    }
    all =
      Builder(
          name = "All",
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
            moveTo(12.0f, 5.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(5.0f, 12.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(19.0f, 12.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(12.0f, 19.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
        }
        .build()
    return all!!
  }

private var all: ImageVector? = null
