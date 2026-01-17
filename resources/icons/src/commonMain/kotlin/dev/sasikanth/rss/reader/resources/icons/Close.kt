/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
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

public val TwineIcons.Close: ImageVector
  get() {
    if (_close != null) {
      return _close!!
    }
    _close =
      ImageVector.Builder(
          name = "Close",
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
              /* pathData = "m256-200-56-56 224-224-224-224 56-56 224 224 224-224 56 56-224 224 224 224-56 56-224-224-224 224Z" */
              moveToRelative(256.0f, -200.0f)
              lineToRelative(-56.0f, -56.0f)
              lineToRelative(224.0f, -224.0f)
              lineToRelative(-224.0f, -224.0f)
              lineToRelative(56.0f, -56.0f)
              lineToRelative(224.0f, 224.0f)
              lineToRelative(224.0f, -224.0f)
              lineToRelative(56.0f, 56.0f)
              lineToRelative(-224.0f, 224.0f)
              lineToRelative(224.0f, 224.0f)
              lineToRelative(-56.0f, 56.0f)
              lineToRelative(-224.0f, -224.0f)
              lineToRelative(-224.0f, 224.0f)
              close()
            }
          }
        }
        .build()
    return _close!!
  }

private var _close: ImageVector? = null
