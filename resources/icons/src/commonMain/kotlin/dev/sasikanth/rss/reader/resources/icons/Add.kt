/*
 * Copyright 2023 Sasikanth Miriyampalli
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

val TwineIcons.Add: ImageVector
  get() {
    if (add != null) {
      return add!!
    }
    add =
      Builder(
          name = "Add",
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
            moveTo(11.0f, 11.0f)
            lineTo(5.0f, 11.0f)
            curveTo(4.448f, 11.0f, 4.0f, 11.448f, 4.0f, 12.0f)
            curveTo(4.0f, 12.552f, 4.448f, 13.0f, 5.0f, 13.0f)
            lineTo(11.0f, 13.0f)
            verticalLineTo(19.0f)
            curveTo(11.0f, 19.552f, 11.448f, 20.0f, 12.0f, 20.0f)
            curveTo(12.552f, 20.0f, 13.0f, 19.552f, 13.0f, 19.0f)
            verticalLineTo(13.0f)
            horizontalLineTo(19.0f)
            curveTo(19.552f, 13.0f, 20.0f, 12.552f, 20.0f, 12.0f)
            curveTo(20.0f, 11.448f, 19.552f, 11.0f, 19.0f, 11.0f)
            lineTo(13.0f, 11.0f)
            verticalLineTo(5.0f)
            curveTo(13.0f, 4.448f, 12.552f, 4.0f, 12.0f, 4.0f)
            curveTo(11.448f, 4.0f, 11.0f, 4.448f, 11.0f, 5.0f)
            verticalLineTo(11.0f)
            close()
          }
        }
        .build()
    return add!!
  }

private var add: ImageVector? = null
