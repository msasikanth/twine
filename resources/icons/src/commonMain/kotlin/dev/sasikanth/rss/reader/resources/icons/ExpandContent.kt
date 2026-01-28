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
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.ExpandContent: ImageVector
  get() {
    if (_expandContent != null) {
      return _expandContent!!
    }
    _expandContent =
      ImageVector.Builder(
          name = "ExpandContent",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f
        )
        .apply {
          group(translationX = -0.0f, translationY = 960.0f) {
            path(
              fill = SolidColor(Color(0xFF000000)),
              stroke = null,
              strokeLineWidth = 0.0f,
              strokeLineCap = Butt,
              strokeLineJoin = Miter,
              strokeLineMiter = 4.0f,
              pathFillType = NonZero
            ) {
              moveTo(200.0f, -200.0f)
              verticalLineToRelative(-240.0f)
              horizontalLineToRelative(80.0f)
              verticalLineToRelative(160.0f)
              horizontalLineToRelative(160.0f)
              verticalLineToRelative(80.0f)
              horizontalLineTo(200.0f)
              close()
              moveToRelative(480.0f, -320.0f)
              verticalLineToRelative(-160.0f)
              horizontalLineTo(520.0f)
              verticalLineToRelative(-80.0f)
              horizontalLineToRelative(240.0f)
              verticalLineToRelative(240.0f)
              horizontalLineToRelative(-80.0f)
              close()
            }
          }
        }
        .build()
    return _expandContent!!
  }

private var _expandContent: ImageVector? = null
