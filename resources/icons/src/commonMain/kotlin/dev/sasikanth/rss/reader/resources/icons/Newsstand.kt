/*
 * Copyright 2025 Sasikanth Miriyampalli
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
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.Newsstand: ImageVector
  get() {
    if (_newsstand != null) {
      return _newsstand!!
    }
    _newsstand =
      Builder(
          name = "Newsstand",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF1f1f1f)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(120.0f, 800.0f)
            quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
            reflectiveQuadTo(80.0f, 760.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(120.0f, 720.0f)
            horizontalLineToRelative(720.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(880.0f, 760.0f)
            quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
            reflectiveQuadTo(840.0f, 800.0f)
            lineTo(120.0f, 800.0f)
            close()
            moveTo(200.0f, 640.0f)
            quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
            reflectiveQuadTo(160.0f, 600.0f)
            verticalLineToRelative(-240.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(200.0f, 320.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(240.0f, 360.0f)
            verticalLineToRelative(240.0f)
            quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
            reflectiveQuadTo(200.0f, 640.0f)
            close()
            moveTo(360.0f, 640.0f)
            quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
            reflectiveQuadTo(320.0f, 600.0f)
            verticalLineToRelative(-400.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(360.0f, 160.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(400.0f, 200.0f)
            verticalLineToRelative(400.0f)
            quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
            reflectiveQuadTo(360.0f, 640.0f)
            close()
            moveTo(520.0f, 640.0f)
            quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
            reflectiveQuadTo(480.0f, 600.0f)
            verticalLineToRelative(-400.0f)
            quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
            reflectiveQuadTo(520.0f, 160.0f)
            quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
            reflectiveQuadTo(560.0f, 200.0f)
            verticalLineToRelative(400.0f)
            quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
            reflectiveQuadTo(520.0f, 640.0f)
            close()
            moveTo(795.0f, 620.0f)
            quadToRelative(-14.0f, 8.0f, -30.5f, 3.5f)
            reflectiveQuadTo(740.0f, 605.0f)
            lineTo(620.0f, 395.0f)
            quadToRelative(-8.0f, -14.0f, -3.5f, -30.5f)
            reflectiveQuadTo(635.0f, 340.0f)
            quadToRelative(14.0f, -8.0f, 30.5f, -3.5f)
            reflectiveQuadTo(690.0f, 355.0f)
            lineToRelative(120.0f, 210.0f)
            quadToRelative(8.0f, 14.0f, 3.5f, 30.5f)
            reflectiveQuadTo(795.0f, 620.0f)
            close()
          }
        }
        .build()
    return _newsstand!!
  }

private var _newsstand: ImageVector? = null
