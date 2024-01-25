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

val TwineIcons.EditorsChoice: ImageVector
  get() {
    if (_editorsChoice != null) {
      return _editorsChoice!!
    }
    _editorsChoice =
      Builder(
          name = "EditorsChoice",
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
            moveTo(240.0f, 920.0f)
            verticalLineToRelative(-329.0f)
            lineTo(110.0f, 380.0f)
            lineToRelative(185.0f, -300.0f)
            horizontalLineToRelative(370.0f)
            lineToRelative(185.0f, 300.0f)
            lineToRelative(-130.0f, 211.0f)
            verticalLineToRelative(329.0f)
            lineToRelative(-240.0f, -80.0f)
            lineToRelative(-240.0f, 80.0f)
            close()
            moveTo(320.0f, 809.0f)
            lineTo(480.0f, 756.0f)
            lineTo(640.0f, 809.0f)
            verticalLineToRelative(-129.0f)
            lineTo(320.0f, 680.0f)
            verticalLineToRelative(129.0f)
            close()
            moveTo(340.0f, 160.0f)
            lineTo(204.0f, 380.0f)
            lineToRelative(136.0f, 220.0f)
            horizontalLineToRelative(280.0f)
            lineToRelative(136.0f, -220.0f)
            lineToRelative(-136.0f, -220.0f)
            lineTo(340.0f, 160.0f)
            close()
            moveTo(438.0f, 543.0f)
            lineTo(296.0f, 402.0f)
            lineToRelative(57.0f, -57.0f)
            lineToRelative(85.0f, 85.0f)
            lineToRelative(169.0f, -170.0f)
            lineToRelative(57.0f, 56.0f)
            lineToRelative(-226.0f, 227.0f)
            close()
            moveTo(320.0f, 680.0f)
            horizontalLineToRelative(320.0f)
            horizontalLineToRelative(-320.0f)
            close()
          }
        }
        .build()
    return _editorsChoice!!
  }

private var _editorsChoice: ImageVector? = null
