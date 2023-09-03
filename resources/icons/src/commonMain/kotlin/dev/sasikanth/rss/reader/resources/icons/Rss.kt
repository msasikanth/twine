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

val TwineIcons.RSS: ImageVector
  get() {
    if (rss != null) {
      return rss!!
    }
    rss =
      Builder(
          name = "RSS",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f
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
            moveTo(200.0f, 840.0f)
            quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
            reflectiveQuadTo(120.0f, 760.0f)
            quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
            reflectiveQuadTo(200.0f, 680.0f)
            quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
            reflectiveQuadTo(280.0f, 760.0f)
            quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
            reflectiveQuadTo(200.0f, 840.0f)
            close()
            moveTo(739.0f, 840.0f)
            quadToRelative(-24.0f, 0.0f, -41.5f, -17.0f)
            reflectiveQuadTo(677.0f, 781.0f)
            quadToRelative(-10.0f, -98.0f, -52.5f, -184.0f)
            reflectiveQuadToRelative(-109.0f, -152.5f)
            quadTo(449.0f, 378.0f, 363.0f, 335.5f)
            reflectiveQuadTo(179.0f, 283.0f)
            quadToRelative(-25.0f, -3.0f, -42.0f, -21.0f)
            reflectiveQuadToRelative(-17.0f, -43.0f)
            quadToRelative(0.0f, -25.0f, 17.5f, -41.5f)
            reflectiveQuadTo(179.0f, 163.0f)
            quadToRelative(123.0f, 10.0f, 231.0f, 62.0f)
            reflectiveQuadToRelative(190.5f, 134.5f)
            quadTo(683.0f, 442.0f, 735.0f, 550.0f)
            reflectiveQuadToRelative(62.0f, 231.0f)
            quadToRelative(2.0f, 24.0f, -15.0f, 41.5f)
            reflectiveQuadTo(739.0f, 840.0f)
            close()
            moveTo(499.0f, 840.0f)
            quadToRelative(-23.0f, 0.0f, -41.5f, -16.5f)
            reflectiveQuadTo(434.0f, 781.0f)
            quadToRelative(-18.0f, -97.0f, -88.0f, -167.0f)
            reflectiveQuadToRelative(-167.0f, -88.0f)
            quadToRelative(-26.0f, -5.0f, -42.5f, -24.0f)
            reflectiveQuadTo(120.0f, 459.0f)
            quadToRelative(0.0f, -26.0f, 18.0f, -42.0f)
            reflectiveQuadToRelative(41.0f, -13.0f)
            quadToRelative(148.0f, 20.0f, 252.5f, 124.5f)
            reflectiveQuadTo(556.0f, 781.0f)
            quadToRelative(3.0f, 23.0f, -14.0f, 41.0f)
            reflectiveQuadToRelative(-43.0f, 18.0f)
            close()
          }
        }
        .build()
    return rss!!
  }

private var rss: ImageVector? = null
