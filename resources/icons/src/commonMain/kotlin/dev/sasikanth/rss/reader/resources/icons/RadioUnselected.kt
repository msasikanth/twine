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
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.RadioUnselected: ImageVector
  get() {
    if (radioUnselected != null) {
      return radioUnselected!!
    }
    radioUnselected =
      Builder(
          name = "RadioUnselected",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFFDFE4DD)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = EvenOdd
          ) {
            moveTo(12.0f, 20.9998f)
            curveTo(16.9706f, 20.9998f, 21.0f, 16.9703f, 21.0f, 11.9998f)
            curveTo(21.0f, 7.0292f, 16.9706f, 2.9998f, 12.0f, 2.9998f)
            curveTo(7.0294f, 2.9998f, 3.0f, 7.0292f, 3.0f, 11.9998f)
            curveTo(3.0f, 16.9703f, 7.0294f, 20.9998f, 12.0f, 20.9998f)
            close()
            moveTo(12.0f, 19.4998f)
            curveTo(16.1421f, 19.4998f, 19.5f, 16.1419f, 19.5f, 11.9998f)
            curveTo(19.5f, 7.8576f, 16.1421f, 4.4998f, 12.0f, 4.4998f)
            curveTo(7.8579f, 4.4998f, 4.5f, 7.8576f, 4.5f, 11.9998f)
            curveTo(4.5f, 16.1419f, 7.8579f, 19.4998f, 12.0f, 19.4998f)
            close()
          }
        }
        .build()
    return radioUnselected!!
  }

private var radioUnselected: ImageVector? = null
