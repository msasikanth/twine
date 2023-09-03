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

val TwineIcons.Bookmarks: ImageVector
  get() {
    if (bookmarks != null) {
      return bookmarks!!
    }
    bookmarks =
      Builder(
          name = "Bookmarks",
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
            moveTo(3.0f, 23.0f)
            verticalLineTo(7.0f)
            curveTo(3.0f, 6.45f, 3.196f, 5.979f, 3.588f, 5.588f)
            curveTo(3.979f, 5.196f, 4.45f, 5.0f, 5.0f, 5.0f)
            horizontalLineTo(15.0f)
            curveTo(15.55f, 5.0f, 16.021f, 5.196f, 16.413f, 5.588f)
            curveTo(16.804f, 5.979f, 17.0f, 6.45f, 17.0f, 7.0f)
            verticalLineTo(23.0f)
            lineTo(10.0f, 20.0f)
            lineTo(3.0f, 23.0f)
            close()
            moveTo(5.0f, 19.95f)
            lineTo(10.0f, 17.8f)
            lineTo(15.0f, 19.95f)
            verticalLineTo(7.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(19.95f)
            close()
            moveTo(19.0f, 20.0f)
            verticalLineTo(3.0f)
            horizontalLineTo(6.0f)
            verticalLineTo(1.0f)
            horizontalLineTo(19.0f)
            curveTo(19.55f, 1.0f, 20.021f, 1.196f, 20.413f, 1.587f)
            curveTo(20.804f, 1.979f, 21.0f, 2.45f, 21.0f, 3.0f)
            verticalLineTo(20.0f)
            horizontalLineTo(19.0f)
            close()
          }
        }
        .build()
    return bookmarks!!
  }

private var bookmarks: ImageVector? = null
