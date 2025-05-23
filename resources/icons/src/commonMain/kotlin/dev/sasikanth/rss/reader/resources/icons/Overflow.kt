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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.Overflow: ImageVector
  get() {
    if (_overflow != null) {
      return _overflow!!
    }
    _overflow =
      Builder(
          name = "Overflow",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF231917)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(11.5f, 4.5f)
            curveTo(11.5f, 5.328f, 10.828f, 6.0f, 10.0f, 6.0f)
            curveTo(9.172f, 6.0f, 8.5f, 5.328f, 8.5f, 4.5f)
            curveTo(8.5f, 3.672f, 9.172f, 3.0f, 10.0f, 3.0f)
            curveTo(10.828f, 3.0f, 11.5f, 3.672f, 11.5f, 4.5f)
            close()
            moveTo(11.5f, 10.0f)
            curveTo(11.5f, 10.828f, 10.828f, 11.5f, 10.0f, 11.5f)
            curveTo(9.172f, 11.5f, 8.5f, 10.828f, 8.5f, 10.0f)
            curveTo(8.5f, 9.172f, 9.172f, 8.5f, 10.0f, 8.5f)
            curveTo(10.828f, 8.5f, 11.5f, 9.172f, 11.5f, 10.0f)
            close()
            moveTo(10.0f, 17.0f)
            curveTo(10.828f, 17.0f, 11.5f, 16.328f, 11.5f, 15.5f)
            curveTo(11.5f, 14.672f, 10.828f, 14.0f, 10.0f, 14.0f)
            curveTo(9.172f, 14.0f, 8.5f, 14.672f, 8.5f, 15.5f)
            curveTo(8.5f, 16.328f, 9.172f, 17.0f, 10.0f, 17.0f)
            close()
          }
        }
        .build()
    return _overflow!!
  }

private var _overflow: ImageVector? = null
