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
package dev.sasikanth.rss.reader.resources.icons.icon.twineicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.icon.TwineIcons

val TwineIcons.Bookmark: ImageVector
  get() {
    if (bookmark != null) {
      return bookmark!!
    }
    bookmark =
      Builder(
          name = "Bookmark",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF201A18)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(5.0f, 21.0f)
            verticalLineTo(5.0f)
            curveTo(5.0f, 4.45f, 5.196f, 3.979f, 5.588f, 3.588f)
            curveTo(5.979f, 3.196f, 6.45f, 3.0f, 7.0f, 3.0f)
            horizontalLineTo(17.0f)
            curveTo(17.55f, 3.0f, 18.021f, 3.196f, 18.413f, 3.588f)
            curveTo(18.804f, 3.979f, 19.0f, 4.45f, 19.0f, 5.0f)
            verticalLineTo(21.0f)
            lineTo(12.0f, 18.0f)
            lineTo(5.0f, 21.0f)
            close()
            moveTo(7.0f, 17.95f)
            lineTo(12.0f, 15.8f)
            lineTo(17.0f, 17.95f)
            verticalLineTo(5.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(17.95f)
            close()
          }
        }
        .build()
    return bookmark!!
  }

private var bookmark: ImageVector? = null
