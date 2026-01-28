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

val TwineIcons.DropdownIcon: ImageVector
  get() {
    if (dropdownIcon != null) {
      return dropdownIcon!!
    }
    dropdownIcon =
      Builder(
          name = "Icon",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF77574F)),
            stroke = null,
            fillAlpha = 0.08f,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(0.0f, 10.0f)
            curveTo(0.0f, 4.477f, 4.477f, 0.0f, 10.0f, 0.0f)
            curveTo(15.523f, 0.0f, 20.0f, 4.477f, 20.0f, 10.0f)
            curveTo(20.0f, 15.523f, 15.523f, 20.0f, 10.0f, 20.0f)
            curveTo(4.477f, 20.0f, 0.0f, 15.523f, 0.0f, 10.0f)
            close()
          }
          path(
            fill = SolidColor(Color(0x00000000)),
            stroke = SolidColor(Color(0xFF77574F)),
            strokeAlpha = 0.16f,
            strokeLineWidth = 1.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(10.0f, 0.5f)
            curveTo(15.247f, 0.5f, 19.5f, 4.753f, 19.5f, 10.0f)
            curveTo(19.5f, 15.247f, 15.247f, 19.5f, 10.0f, 19.5f)
            curveTo(4.753f, 19.5f, 0.5f, 15.247f, 0.5f, 10.0f)
            curveTo(0.5f, 4.753f, 4.753f, 0.5f, 10.0f, 0.5f)
            close()
          }
          path(
            fill = SolidColor(Color(0xFF1F1A1F)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(10.0f, 13.569f)
            curveTo(9.895f, 13.569f, 9.796f, 13.553f, 9.704f, 13.52f)
            curveTo(9.612f, 13.487f, 9.527f, 13.431f, 9.448f, 13.352f)
            lineTo(5.817f, 9.722f)
            curveTo(5.672f, 9.577f, 5.6f, 9.393f, 5.6f, 9.17f)
            curveTo(5.6f, 8.946f, 5.672f, 8.762f, 5.817f, 8.617f)
            curveTo(5.962f, 8.472f, 6.146f, 8.4f, 6.37f, 8.4f)
            curveTo(6.593f, 8.4f, 6.777f, 8.472f, 6.922f, 8.617f)
            lineTo(10.0f, 11.695f)
            lineTo(13.078f, 8.617f)
            curveTo(13.223f, 8.472f, 13.407f, 8.4f, 13.631f, 8.4f)
            curveTo(13.854f, 8.4f, 14.038f, 8.472f, 14.183f, 8.617f)
            curveTo(14.328f, 8.762f, 14.4f, 8.946f, 14.4f, 9.17f)
            curveTo(14.4f, 9.393f, 14.328f, 9.577f, 14.183f, 9.722f)
            lineTo(10.553f, 13.352f)
            curveTo(10.474f, 13.431f, 10.388f, 13.487f, 10.296f, 13.52f)
            curveTo(10.204f, 13.553f, 10.105f, 13.569f, 10.0f, 13.569f)
            close()
          }
        }
        .build()
    return dropdownIcon!!
  }

private var dropdownIcon: ImageVector? = null
