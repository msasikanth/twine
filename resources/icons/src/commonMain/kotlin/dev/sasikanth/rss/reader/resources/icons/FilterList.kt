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

public val TwineIcons.FilterList: ImageVector
  get() {
    if (_filterList != null) {
      return _filterList!!
    }
    _filterList =
      ImageVector.Builder(
          name = "FilterList",
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
              moveTo(440.0f, -240.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(400.0f, -280.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(440.0f, -320.0f)
              horizontalLineToRelative(80.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(560.0f, -280.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(520.0f, -240.0f)
              horizontalLineToRelative(-80.0f)
              close()
              moveTo(280.0f, -440.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(240.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(280.0f, -520.0f)
              horizontalLineToRelative(400.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(720.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(680.0f, -440.0f)
              horizontalLineTo(280.0f)
              close()
              moveTo(160.0f, -640.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(120.0f, -680.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(160.0f, -720.0f)
              horizontalLineToRelative(640.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(840.0f, -680.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(800.0f, -640.0f)
              horizontalLineTo(160.0f)
              close()
            }
          }
        }
        .build()
    return _filterList!!
  }

private var _filterList: ImageVector? = null
