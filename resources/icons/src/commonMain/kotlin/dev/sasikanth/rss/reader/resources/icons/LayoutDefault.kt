/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

val TwineIcons.LayoutDefault: ImageVector
  get() {
    if (layoutDefault != null) {
      return layoutDefault!!
    }
    layoutDefault =
      Builder(
          name = "LayoutDefault",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF1A1C15)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(4.0f, 2.0f)
            lineTo(20.0f, 2.0f)
            arcTo(2.0f, 2.0f, 0.0f, false, true, 22.0f, 4.0f)
            lineTo(22.0f, 10.0f)
            arcTo(2.0f, 2.0f, 0.0f, false, true, 20.0f, 12.0f)
            lineTo(4.0f, 12.0f)
            arcTo(2.0f, 2.0f, 0.0f, false, true, 2.0f, 10.0f)
            lineTo(2.0f, 4.0f)
            arcTo(2.0f, 2.0f, 0.0f, false, true, 4.0f, 2.0f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF1A1C15)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(3.0f, 16.0f)
            lineTo(10.0f, 16.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 11.0f, 17.0f)
            lineTo(11.0f, 17.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 10.0f, 18.0f)
            lineTo(3.0f, 18.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 2.0f, 17.0f)
            lineTo(2.0f, 17.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 3.0f, 16.0f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF1A1C15)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(15.5f, 16.0f)
            lineTo(20.5f, 16.0f)
            arcTo(1.5f, 1.5f, 0.0f, false, true, 22.0f, 17.5f)
            lineTo(22.0f, 20.5f)
            arcTo(1.5f, 1.5f, 0.0f, false, true, 20.5f, 22.0f)
            lineTo(15.5f, 22.0f)
            arcTo(1.5f, 1.5f, 0.0f, false, true, 14.0f, 20.5f)
            lineTo(14.0f, 17.5f)
            arcTo(1.5f, 1.5f, 0.0f, false, true, 15.5f, 16.0f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF1A1C15)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(3.0f, 20.0f)
            lineTo(6.0f, 20.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 7.0f, 21.0f)
            lineTo(7.0f, 21.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 6.0f, 22.0f)
            lineTo(3.0f, 22.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 2.0f, 21.0f)
            lineTo(2.0f, 21.0f)
            arcTo(1.0f, 1.0f, 0.0f, false, true, 3.0f, 20.0f)
            close()
          }
        }
        .build()
    return layoutDefault!!
  }

private var layoutDefault: ImageVector? = null
