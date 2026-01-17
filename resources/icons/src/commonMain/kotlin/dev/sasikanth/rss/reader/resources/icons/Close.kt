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
              /* pathData = "M480-424 284-228q-11 11-28 11t-28-11q-11-11-11-28t11-28l196-196-196-196q-11-11-11-28t11-28q11-11 28-11t28 11l196 196 196-196q11-11 28-11t28 11q11 11 11 28t-11 28L536-480l196 196q11 11 11 28t-11 28q-11 11-28 11t-28-11L480-424Z" */
              moveTo(480.0f, -424.0f)
              lineTo(284.0f, -228.0f)
              quadToRelative(-11.0f, 11.0f, -28.0f, 11.0f)
              reflectiveQuadToRelative(-28.0f, -11.0f)
              quadToRelative(-11.0f, -11.0f, -11.0f, -28.0f)
              reflectiveQuadToRelative(11.0f, -28.0f)
              lineToRelative(196.0f, -196.0f)
              lineToRelative(-196.0f, -196.0f)
              quadToRelative(-11.0f, -11.0f, -11.0f, -28.0f)
              reflectiveQuadToRelative(11.0f, -28.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              lineToRelative(196.0f, 196.0f)
              lineToRelative(196.0f, -196.0f)
              quadToRelative(11.0f, -11.0f, 28.0f, -11.0f)
              reflectiveQuadToRelative(28.0f, 11.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              lineTo(536.0f, -480.0f)
              lineToRelative(196.0f, 196.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 28.0f)
              reflectiveQuadToRelative(-11.0f, 28.0f)
              quadToRelative(-11.0f, 11.0f, -28.0f, 11.0f)
              reflectiveQuadToRelative(-28.0f, -11.0f)
              lineTo(480.0f, -424.0f)
              close()
            }
          }
        }
        .build()
    return _close!!
  }

private var _close: ImageVector? = null
