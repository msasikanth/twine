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

val TwineIcons.DragIndicator: ImageVector
  get() {
    if (dragIndicator != null) {
      return dragIndicator!!
    }
    dragIndicator =
      Builder(
          name = "DragIndicator",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFFD7C1C3)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(7.0f, 17.4956f)
            curveTo(7.0f, 18.3148f, 7.674f, 19.0f, 8.4956f, 19.0f)
            curveTo(9.3148f, 19.0f, 10.0f, 18.326f, 10.0f, 17.5044f)
            curveTo(10.0f, 16.6852f, 9.326f, 16.0f, 8.5044f, 16.0f)
            curveTo(7.6852f, 16.0f, 7.0f, 16.674f, 7.0f, 17.4956f)
            close()
            moveTo(7.0f, 11.9961f)
            curveTo(7.0f, 12.8153f, 7.674f, 13.5005f, 8.4956f, 13.5005f)
            curveTo(9.3148f, 13.5005f, 10.0f, 12.8265f, 10.0f, 12.0049f)
            curveTo(10.0f, 11.1858f, 9.326f, 10.5005f, 8.5044f, 10.5005f)
            curveTo(7.6852f, 10.5005f, 7.0f, 11.1745f, 7.0f, 11.9961f)
            close()
            moveTo(7.0f, 6.4956f)
            curveTo(7.0f, 7.3148f, 7.674f, 8.0f, 8.4956f, 8.0f)
            curveTo(9.3148f, 8.0f, 10.0f, 7.326f, 10.0f, 6.5044f)
            curveTo(10.0f, 5.6852f, 9.326f, 5.0f, 8.5044f, 5.0f)
            curveTo(7.6852f, 5.0f, 7.0f, 5.674f, 7.0f, 6.4956f)
            close()
            moveTo(14.0f, 17.4956f)
            curveTo(14.0f, 18.3148f, 14.674f, 19.0f, 15.4956f, 19.0f)
            curveTo(16.3148f, 19.0f, 17.0f, 18.326f, 17.0f, 17.5044f)
            curveTo(17.0f, 16.6852f, 16.326f, 16.0f, 15.5044f, 16.0f)
            curveTo(14.6852f, 16.0f, 14.0f, 16.674f, 14.0f, 17.4956f)
            close()
            moveTo(14.0f, 11.9961f)
            curveTo(14.0f, 12.8153f, 14.674f, 13.5005f, 15.4956f, 13.5005f)
            curveTo(16.3148f, 13.5005f, 17.0f, 12.8265f, 17.0f, 12.0049f)
            curveTo(17.0f, 11.1858f, 16.326f, 10.5005f, 15.5044f, 10.5005f)
            curveTo(14.6852f, 10.5005f, 14.0f, 11.1745f, 14.0f, 11.9961f)
            close()
            moveTo(14.0f, 6.4956f)
            curveTo(14.0f, 7.3148f, 14.674f, 8.0f, 15.4956f, 8.0f)
            curveTo(16.3148f, 8.0f, 17.0f, 7.326f, 17.0f, 6.5044f)
            curveTo(17.0f, 5.6852f, 16.326f, 5.0f, 15.5044f, 5.0f)
            curveTo(14.6852f, 5.0f, 14.0f, 5.674f, 14.0f, 6.4956f)
            close()
          }
        }
        .build()
    return dragIndicator!!
  }

private var dragIndicator: ImageVector? = null
