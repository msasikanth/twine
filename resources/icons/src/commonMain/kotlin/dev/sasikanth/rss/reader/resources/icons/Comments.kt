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

val TwineIcons.Comments: ImageVector
  get() {
    if (comments != null) {
      return comments!!
    }
    comments =
      Builder(
          name = "Comments",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f,
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
            moveTo(10.516f, 17.726f)
            curveTo(10.271f, 18.094f, 9.73f, 18.094f, 9.484f, 17.726f)
            lineTo(8.0f, 15.5f)
            horizontalLineTo(3.5f)
            curveTo(3.088f, 15.5f, 2.734f, 15.353f, 2.441f, 15.059f)
            curveTo(2.147f, 14.766f, 2.0f, 14.413f, 2.0f, 14.0f)
            verticalLineTo(4.5f)
            curveTo(2.0f, 4.088f, 2.147f, 3.734f, 2.441f, 3.441f)
            curveTo(2.734f, 3.147f, 3.088f, 3.0f, 3.5f, 3.0f)
            horizontalLineTo(16.5f)
            curveTo(16.913f, 3.0f, 17.266f, 3.147f, 17.559f, 3.441f)
            curveTo(17.853f, 3.734f, 18.0f, 4.088f, 18.0f, 4.5f)
            verticalLineTo(14.0f)
            curveTo(18.0f, 14.413f, 17.853f, 14.766f, 17.559f, 15.059f)
            curveTo(17.266f, 15.353f, 16.913f, 15.5f, 16.5f, 15.5f)
            horizontalLineTo(12.0f)
            lineTo(10.516f, 17.726f)
            close()
            moveTo(10.0f, 15.792f)
            lineTo(11.208f, 14.0f)
            horizontalLineTo(16.5f)
            verticalLineTo(4.5f)
            horizontalLineTo(3.5f)
            verticalLineTo(14.0f)
            horizontalLineTo(8.792f)
            lineTo(10.0f, 15.792f)
            close()
          }
        }
        .build()
    return comments!!
  }

private var comments: ImageVector? = null
