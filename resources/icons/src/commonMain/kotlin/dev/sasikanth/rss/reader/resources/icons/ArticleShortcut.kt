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
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.ArticleShortcut: ImageVector
  get() {
    if (articleShortcut != null) {
      return articleShortcut!!
    }
    articleShortcut =
      Builder(
          name = "ArticleShortcut",
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
            moveTo(400.0f, 680.0f)
            horizontalLineToRelative(160.0f)
            verticalLineToRelative(-80.0f)
            lineTo(400.0f, 600.0f)
            verticalLineToRelative(80.0f)
            close()
            moveTo(400.0f, 520.0f)
            horizontalLineToRelative(280.0f)
            verticalLineToRelative(-80.0f)
            lineTo(400.0f, 440.0f)
            verticalLineToRelative(80.0f)
            close()
            moveTo(280.0f, 360.0f)
            horizontalLineToRelative(400.0f)
            verticalLineToRelative(-80.0f)
            lineTo(280.0f, 280.0f)
            verticalLineToRelative(80.0f)
            close()
            moveTo(480.0f, 480.0f)
            close()
            moveTo(265.0f, 880.0f)
            quadToRelative(-79.0f, 0.0f, -134.5f, -55.5f)
            reflectiveQuadTo(75.0f, 690.0f)
            quadToRelative(0.0f, -57.0f, 29.5f, -102.0f)
            reflectiveQuadToRelative(77.5f, -68.0f)
            lineTo(80.0f, 520.0f)
            verticalLineToRelative(-80.0f)
            horizontalLineToRelative(240.0f)
            verticalLineToRelative(240.0f)
            horizontalLineToRelative(-80.0f)
            verticalLineToRelative(-97.0f)
            quadToRelative(-37.0f, 8.0f, -61.0f, 38.0f)
            reflectiveQuadToRelative(-24.0f, 69.0f)
            quadToRelative(0.0f, 46.0f, 32.5f, 78.0f)
            reflectiveQuadToRelative(77.5f, 32.0f)
            verticalLineToRelative(80.0f)
            close()
            moveTo(400.0f, 840.0f)
            verticalLineToRelative(-80.0f)
            horizontalLineToRelative(360.0f)
            verticalLineToRelative(-560.0f)
            lineTo(200.0f, 200.0f)
            verticalLineToRelative(160.0f)
            horizontalLineToRelative(-80.0f)
            verticalLineToRelative(-160.0f)
            quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
            reflectiveQuadTo(200.0f, 120.0f)
            horizontalLineToRelative(560.0f)
            quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
            reflectiveQuadTo(840.0f, 200.0f)
            verticalLineToRelative(560.0f)
            quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
            reflectiveQuadTo(760.0f, 840.0f)
            lineTo(400.0f, 840.0f)
            close()
          }
        }
        .build()
    return articleShortcut!!
  }

private var articleShortcut: ImageVector? = null
