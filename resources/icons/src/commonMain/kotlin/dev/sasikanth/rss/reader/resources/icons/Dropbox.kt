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

public val TwineIcons.Dropbox: ImageVector
  get() {
    if (_dropbox != null) {
      return _dropbox!!
    }
    _dropbox =
      Builder(
          name = "Dropbox",
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
            moveTo(6.0f, 1.807f)
            lineTo(0.0f, 5.629f)
            lineToRelative(6.0f, 3.822f)
            lineToRelative(6.001f, -3.822f)
            lineTo(6.0f, 1.807f)
            close()
            moveTo(18.0f, 1.807f)
            lineToRelative(-6.0f, 3.822f)
            lineToRelative(6.0f, 3.822f)
            lineToRelative(6.0f, -3.822f)
            lineToRelative(-6.0f, -3.822f)
            close()
            moveTo(0.0f, 13.274f)
            lineToRelative(6.0f, 3.822f)
            lineToRelative(6.001f, -3.822f)
            lineTo(6.0f, 9.452f)
            lineToRelative(-6.0f, 3.822f)
            close()
            moveTo(18.0f, 9.452f)
            lineToRelative(-6.0f, 3.822f)
            lineToRelative(6.0f, 3.822f)
            lineToRelative(6.0f, -3.822f)
            lineToRelative(-6.0f, -3.822f)
            close()
            moveTo(6.0f, 18.371f)
            lineToRelative(6.001f, 3.822f)
            lineToRelative(6.0f, -3.822f)
            lineToRelative(-6.0f, -3.822f)
            lineTo(6.0f, 18.371f)
            close()
          }
        }
        .build()
    return _dropbox!!
  }

private var _dropbox: ImageVector? = null
