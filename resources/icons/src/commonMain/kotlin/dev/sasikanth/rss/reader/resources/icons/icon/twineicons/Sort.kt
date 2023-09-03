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

val TwineIcons.Sort: ImageVector
  get() {
    if (sort != null) {
      return sort!!
    }
    sort =
      Builder(
          name = "Sort",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
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
            moveTo(2.5f, 15.0f)
            verticalLineTo(13.333f)
            horizontalLineTo(7.5f)
            verticalLineTo(15.0f)
            horizontalLineTo(2.5f)
            close()
            moveTo(2.5f, 10.833f)
            verticalLineTo(9.167f)
            horizontalLineTo(12.5f)
            verticalLineTo(10.833f)
            horizontalLineTo(2.5f)
            close()
            moveTo(2.5f, 6.667f)
            verticalLineTo(5.0f)
            horizontalLineTo(17.5f)
            verticalLineTo(6.667f)
            horizontalLineTo(2.5f)
            close()
          }
        }
        .build()
    return sort!!
  }

private var sort: ImageVector? = null
