/*
 * Copyright 2024 Sasikanth Miriyampalli
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

val TwineIcons.NewGroup: ImageVector
  get() {
    if (newGroup != null) {
      return newGroup!!
    }
    newGroup =
      Builder(
          name = "NewGroup",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF171D19)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(5.0f, 5.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(5.0f)
            close()
            moveTo(3.0f, 5.0f)
            curveTo(3.0f, 3.8954f, 3.8954f, 3.0f, 5.0f, 3.0f)
            horizontalLineTo(9.0f)
            curveTo(10.1046f, 3.0f, 11.0f, 3.8954f, 11.0f, 5.0f)
            verticalLineTo(9.0f)
            curveTo(11.0f, 10.1046f, 10.1046f, 11.0f, 9.0f, 11.0f)
            horizontalLineTo(5.0f)
            curveTo(3.8954f, 11.0f, 3.0f, 10.1046f, 3.0f, 9.0f)
            verticalLineTo(5.0f)
            close()
            moveTo(15.0f, 5.0f)
            horizontalLineTo(19.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(5.0f)
            close()
            moveTo(13.0f, 5.0f)
            curveTo(13.0f, 3.8954f, 13.8954f, 3.0f, 15.0f, 3.0f)
            horizontalLineTo(19.0f)
            curveTo(20.1046f, 3.0f, 21.0f, 3.8954f, 21.0f, 5.0f)
            verticalLineTo(9.0f)
            curveTo(21.0f, 10.1046f, 20.1046f, 11.0f, 19.0f, 11.0f)
            horizontalLineTo(15.0f)
            curveTo(13.8954f, 11.0f, 13.0f, 10.1046f, 13.0f, 9.0f)
            verticalLineTo(5.0f)
            close()
            moveTo(9.0f, 15.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(19.0f)
            horizontalLineTo(9.0f)
            verticalLineTo(15.0f)
            close()
            moveTo(5.0f, 13.0f)
            curveTo(3.8954f, 13.0f, 3.0f, 13.8954f, 3.0f, 15.0f)
            verticalLineTo(19.0f)
            curveTo(3.0f, 20.1046f, 3.8954f, 21.0f, 5.0f, 21.0f)
            horizontalLineTo(9.0f)
            curveTo(10.1046f, 21.0f, 11.0f, 20.1046f, 11.0f, 19.0f)
            verticalLineTo(15.0f)
            curveTo(11.0f, 13.8954f, 10.1046f, 13.0f, 9.0f, 13.0f)
            horizontalLineTo(5.0f)
            close()
            moveTo(16.0f, 14.0f)
            curveTo(16.0f, 13.4477f, 16.4477f, 13.0f, 17.0f, 13.0f)
            curveTo(17.5523f, 13.0f, 18.0f, 13.4477f, 18.0f, 14.0f)
            verticalLineTo(16.0f)
            horizontalLineTo(20.0f)
            curveTo(20.5523f, 16.0f, 21.0f, 16.4477f, 21.0f, 17.0f)
            curveTo(21.0f, 17.5523f, 20.5523f, 18.0f, 20.0f, 18.0f)
            horizontalLineTo(18.0f)
            verticalLineTo(20.0f)
            curveTo(18.0f, 20.5523f, 17.5523f, 21.0f, 17.0f, 21.0f)
            curveTo(16.4477f, 21.0f, 16.0f, 20.5523f, 16.0f, 20.0f)
            verticalLineTo(18.0f)
            horizontalLineTo(14.0f)
            curveTo(13.4477f, 18.0f, 13.0f, 17.5523f, 13.0f, 17.0f)
            curveTo(13.0f, 16.4477f, 13.4477f, 16.0f, 14.0f, 16.0f)
            horizontalLineTo(16.0f)
            verticalLineTo(14.0f)
            close()
          }
        }
        .build()
    return newGroup!!
  }

private var newGroup: ImageVector? = null
