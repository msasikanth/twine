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

val TwineIcons.Feed: ImageVector
  get() {
    if (feed != null) {
      return feed!!
    }
    feed =
      Builder(
          name = "Feed",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f,
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
            moveTo(5.0f, 21.0f)
            curveTo(4.45f, 21.0f, 3.979f, 20.804f, 3.588f, 20.413f)
            curveTo(3.196f, 20.021f, 3.0f, 19.55f, 3.0f, 19.0f)
            verticalLineTo(5.0f)
            curveTo(3.0f, 4.45f, 3.196f, 3.979f, 3.588f, 3.588f)
            curveTo(3.979f, 3.196f, 4.45f, 3.0f, 5.0f, 3.0f)
            horizontalLineTo(16.0f)
            lineTo(21.0f, 8.0f)
            verticalLineTo(19.0f)
            curveTo(21.0f, 19.55f, 20.804f, 20.021f, 20.413f, 20.413f)
            curveTo(20.021f, 20.804f, 19.55f, 21.0f, 19.0f, 21.0f)
            horizontalLineTo(5.0f)
            close()
            moveTo(5.0f, 19.0f)
            horizontalLineTo(19.0f)
            verticalLineTo(9.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(5.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(19.0f)
            close()
            moveTo(7.0f, 17.0f)
            horizontalLineTo(17.0f)
            verticalLineTo(15.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(17.0f)
            close()
            moveTo(7.0f, 9.0f)
            horizontalLineTo(12.0f)
            verticalLineTo(7.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(9.0f)
            close()
            moveTo(7.0f, 13.0f)
            horizontalLineTo(17.0f)
            verticalLineTo(11.0f)
            horizontalLineTo(7.0f)
            verticalLineTo(13.0f)
            close()
          }
        }
        .build()
    return feed!!
  }

private var feed: ImageVector? = null
