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

public val TwineIcons.Check: ImageVector
  get() {
    if (_check != null) {
      return _check!!
    }
    _check =
      ImageVector.Builder(
          name = "Check",
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
              /* pathData = "m382-354 339-339q12-12 28-12t28 12q12 12 12 28.5T777-636L410-268q-12 12-28 12t-28-12L182-440q-12-12-11.5-28.5T183-497q12-12 28.5-12t28.5 12l142 143Z" */
              moveToRelative(382.0f, -354.0f)
              lineToRelative(339.0f, -339.0f)
              quadToRelative(12.0f, -12.0f, 28.0f, -12.0f)
              reflectiveQuadToRelative(28.0f, 12.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              reflectiveQuadTo(777.0f, -636.0f)
              lineTo(410.0f, -268.0f)
              quadToRelative(-12.0f, 12.0f, -28.0f, 12.0f)
              reflectiveQuadToRelative(-28.0f, -12.0f)
              lineTo(182.0f, -440.0f)
              quadToRelative(-12.0f, -12.0f, -11.5f, -28.5f)
              reflectiveQuadTo(183.0f, -497.0f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.0f)
              reflectiveQuadToRelative(28.5f, 12.0f)
              lineToRelative(142.0f, 143.0f)
              close()
            }
          }
        }
        .build()
    return _check!!
  }

private var _check: ImageVector? = null
