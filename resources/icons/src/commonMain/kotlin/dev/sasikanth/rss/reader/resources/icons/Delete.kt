/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
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
          viewportWidth = 960.0f,
          viewportHeight = 960.0f,
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero,
          ) {
            moveTo(280.0f, 840.0f)
            quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
            reflectiveQuadTo(200.0f, 760.0f)
            verticalLineToRelative(-520.0f)
            horizontalLineToRelative(-40.0f)
            verticalLineToRelative(-80.0f)
            horizontalLineToRelative(200.0f)
            verticalLineToRelative(-40.0f)
            horizontalLineToRelative(240.0f)
            verticalLineToRelative(40.0f)
            horizontalLineToRelative(200.0f)
            verticalLineToRelative(80.0f)
            horizontalLineToRelative(-40.0f)
            verticalLineToRelative(520.0f)
            quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
            reflectiveQuadTo(680.0f, 840.0f)
            lineTo(280.0f, 840.0f)
            close()
            moveTo(680.0f, 240.0f)
            lineTo(280.0f, 240.0f)
            verticalLineToRelative(520.0f)
            horizontalLineToRelative(400.0f)
            verticalLineToRelative(-520.0f)
            close()
            moveTo(360.0f, 680.0f)
            horizontalLineToRelative(80.0f)
            verticalLineToRelative(-360.0f)
            horizontalLineToRelative(-80.0f)
            verticalLineToRelative(360.0f)
            close()
            moveTo(520.0f, 680.0f)
            horizontalLineToRelative(80.0f)
            verticalLineToRelative(-360.0f)
            horizontalLineToRelative(-80.0f)
            verticalLineToRelative(360.0f)
            close()
            moveTo(280.0f, 240.0f)
            verticalLineToRelative(520.0f)
            verticalLineToRelative(-520.0f)
            close()
          }
        }
        .build()
    return delete!!
  }

private var delete: ImageVector? = null
