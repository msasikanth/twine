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

val TwineIcons.Delete: ImageVector
  get() {
    if (delete != null) {
      return delete!!
    }
    delete =
      Builder(
          name = "Delete",
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
            moveTo(7.0f, 12.0f)
            curveTo(7.0f, 12.552f, 7.448f, 13.0f, 8.0f, 13.0f)
            horizontalLineTo(16.0f)
            curveTo(16.552f, 13.0f, 17.0f, 12.552f, 17.0f, 12.0f)
            curveTo(17.0f, 11.448f, 16.552f, 11.0f, 16.0f, 11.0f)
            horizontalLineTo(8.0f)
            curveTo(7.448f, 11.0f, 7.0f, 11.448f, 7.0f, 12.0f)
            close()
            moveTo(12.0f, 22.0f)
            curveTo(10.617f, 22.0f, 9.317f, 21.737f, 8.1f, 21.212f)
            curveTo(6.883f, 20.688f, 5.825f, 19.975f, 4.925f, 19.075f)
            curveTo(4.025f, 18.175f, 3.313f, 17.117f, 2.787f, 15.9f)
            curveTo(2.263f, 14.683f, 2.0f, 13.383f, 2.0f, 12.0f)
            curveTo(2.0f, 10.617f, 2.263f, 9.317f, 2.787f, 8.1f)
            curveTo(3.313f, 6.883f, 4.025f, 5.825f, 4.925f, 4.925f)
            curveTo(5.825f, 4.025f, 6.883f, 3.313f, 8.1f, 2.788f)
            curveTo(9.317f, 2.263f, 10.617f, 2.0f, 12.0f, 2.0f)
            curveTo(13.383f, 2.0f, 14.683f, 2.263f, 15.9f, 2.788f)
            curveTo(17.117f, 3.313f, 18.175f, 4.025f, 19.075f, 4.925f)
            curveTo(19.975f, 5.825f, 20.688f, 6.883f, 21.212f, 8.1f)
            curveTo(21.737f, 9.317f, 22.0f, 10.617f, 22.0f, 12.0f)
            curveTo(22.0f, 13.383f, 21.737f, 14.683f, 21.212f, 15.9f)
            curveTo(20.688f, 17.117f, 19.975f, 18.175f, 19.075f, 19.075f)
            curveTo(18.175f, 19.975f, 17.117f, 20.688f, 15.9f, 21.212f)
            curveTo(14.683f, 21.737f, 13.383f, 22.0f, 12.0f, 22.0f)
            close()
            moveTo(12.0f, 20.0f)
            curveTo(14.233f, 20.0f, 16.125f, 19.225f, 17.675f, 17.675f)
            curveTo(19.225f, 16.125f, 20.0f, 14.233f, 20.0f, 12.0f)
            curveTo(20.0f, 9.767f, 19.225f, 7.875f, 17.675f, 6.325f)
            curveTo(16.125f, 4.775f, 14.233f, 4.0f, 12.0f, 4.0f)
            curveTo(9.767f, 4.0f, 7.875f, 4.775f, 6.325f, 6.325f)
            curveTo(4.775f, 7.875f, 4.0f, 9.767f, 4.0f, 12.0f)
            curveTo(4.0f, 14.233f, 4.775f, 16.125f, 6.325f, 17.675f)
            curveTo(7.875f, 19.225f, 9.767f, 20.0f, 12.0f, 20.0f)
            close()
          }
        }
        .build()
    return delete!!
  }

private var delete: ImageVector? = null
