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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.Share: ImageVector
  get() {
    if (share != null) {
      return share!!
    }

    share =
      Builder(
          name = "Share",
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
            pathFillType = EvenOdd
          ) {
            moveTo(17.0f, 6.0f)
            curveTo(17.0f, 6.552f, 16.552f, 7.0f, 16.0f, 7.0f)
            curveTo(15.448f, 7.0f, 15.0f, 6.552f, 15.0f, 6.0f)
            curveTo(15.0f, 5.448f, 15.448f, 5.0f, 16.0f, 5.0f)
            curveTo(16.552f, 5.0f, 17.0f, 5.448f, 17.0f, 6.0f)
            close()
            moveTo(19.0f, 6.0f)
            curveTo(19.0f, 7.657f, 17.657f, 9.0f, 16.0f, 9.0f)
            curveTo(15.274f, 9.0f, 14.608f, 8.742f, 14.089f, 8.313f)
            lineTo(8.94f, 11.402f)
            curveTo(8.979f, 11.595f, 9.0f, 11.795f, 9.0f, 12.0f)
            curveTo(9.0f, 12.205f, 8.979f, 12.405f, 8.94f, 12.598f)
            lineTo(14.089f, 15.687f)
            curveTo(14.608f, 15.258f, 15.274f, 15.0f, 16.0f, 15.0f)
            curveTo(17.657f, 15.0f, 19.0f, 16.343f, 19.0f, 18.0f)
            curveTo(19.0f, 19.657f, 17.657f, 21.0f, 16.0f, 21.0f)
            curveTo(14.343f, 21.0f, 13.0f, 19.657f, 13.0f, 18.0f)
            curveTo(13.0f, 17.795f, 13.021f, 17.595f, 13.06f, 17.402f)
            lineTo(7.911f, 14.313f)
            curveTo(7.392f, 14.742f, 6.726f, 15.0f, 6.0f, 15.0f)
            curveTo(4.343f, 15.0f, 3.0f, 13.657f, 3.0f, 12.0f)
            curveTo(3.0f, 10.343f, 4.343f, 9.0f, 6.0f, 9.0f)
            curveTo(6.726f, 9.0f, 7.392f, 9.258f, 7.911f, 9.687f)
            lineTo(13.06f, 6.598f)
            curveTo(13.021f, 6.405f, 13.0f, 6.205f, 13.0f, 6.0f)
            curveTo(13.0f, 4.343f, 14.343f, 3.0f, 16.0f, 3.0f)
            curveTo(17.657f, 3.0f, 19.0f, 4.343f, 19.0f, 6.0f)
            close()
            moveTo(7.0f, 12.0f)
            curveTo(7.0f, 12.552f, 6.552f, 13.0f, 6.0f, 13.0f)
            curveTo(5.448f, 13.0f, 5.0f, 12.552f, 5.0f, 12.0f)
            curveTo(5.0f, 11.448f, 5.448f, 11.0f, 6.0f, 11.0f)
            curveTo(6.552f, 11.0f, 7.0f, 11.448f, 7.0f, 12.0f)
            close()
            moveTo(16.0f, 19.0f)
            curveTo(16.552f, 19.0f, 17.0f, 18.552f, 17.0f, 18.0f)
            curveTo(17.0f, 17.448f, 16.552f, 17.0f, 16.0f, 17.0f)
            curveTo(15.448f, 17.0f, 15.0f, 17.448f, 15.0f, 18.0f)
            curveTo(15.0f, 18.552f, 15.448f, 19.0f, 16.0f, 19.0f)
            close()
          }
        }
        .build()
    return share!!
  }

private var share: ImageVector? = null
