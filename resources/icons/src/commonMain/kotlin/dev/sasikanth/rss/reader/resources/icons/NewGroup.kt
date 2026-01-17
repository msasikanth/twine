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
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.NewGroup: ImageVector
  get() {
    if (_newGroup != null) {
      return _newGroup!!
    }
    _newGroup =
      Builder(
          name = "NewGroup",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0xFF817379)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(15.75f, 12.0f)
            curveTo(16.164f, 12.0f, 16.5f, 12.336f, 16.5f, 12.75f)
            verticalLineTo(15.0f)
            horizontalLineTo(18.75f)
            curveTo(19.164f, 15.0f, 19.5f, 15.336f, 19.5f, 15.75f)
            curveTo(19.5f, 16.164f, 19.164f, 16.5f, 18.75f, 16.5f)
            horizontalLineTo(16.5f)
            verticalLineTo(18.75f)
            curveTo(16.5f, 19.164f, 16.164f, 19.5f, 15.75f, 19.5f)
            curveTo(15.336f, 19.5f, 15.0f, 19.164f, 15.0f, 18.75f)
            verticalLineTo(16.5f)
            horizontalLineTo(12.75f)
            curveTo(12.336f, 16.5f, 12.0f, 16.164f, 12.0f, 15.75f)
            curveTo(12.0f, 15.336f, 12.336f, 15.0f, 12.75f, 15.0f)
            horizontalLineTo(15.0f)
            verticalLineTo(12.75f)
            curveTo(15.0f, 12.336f, 15.336f, 12.0f, 15.75f, 12.0f)
            close()
            moveTo(13.5f, 2.5f)
            curveTo(15.157f, 2.5f, 16.5f, 3.843f, 16.5f, 5.5f)
            verticalLineTo(9.25f)
            curveTo(16.5f, 9.664f, 16.164f, 10.0f, 15.75f, 10.0f)
            curveTo(15.336f, 10.0f, 15.0f, 9.664f, 15.0f, 9.25f)
            verticalLineTo(5.5f)
            curveTo(15.0f, 4.672f, 14.328f, 4.0f, 13.5f, 4.0f)
            horizontalLineTo(5.5f)
            curveTo(4.672f, 4.0f, 4.0f, 4.672f, 4.0f, 5.5f)
            verticalLineTo(13.5f)
            lineTo(4.008f, 13.653f)
            curveTo(4.079f, 14.359f, 4.641f, 14.92f, 5.347f, 14.992f)
            lineTo(5.5f, 15.0f)
            horizontalLineTo(9.25f)
            curveTo(9.664f, 15.0f, 10.0f, 15.336f, 10.0f, 15.75f)
            curveTo(10.0f, 16.164f, 9.664f, 16.5f, 9.25f, 16.5f)
            horizontalLineTo(5.5f)
            curveTo(3.843f, 16.5f, 2.5f, 15.157f, 2.5f, 13.5f)
            verticalLineTo(5.5f)
            curveTo(2.5f, 3.843f, 3.843f, 2.5f, 5.5f, 2.5f)
            horizontalLineTo(13.5f)
            close()
          }
        }
        .build()
    return _newGroup!!
  }

private var _newGroup: ImageVector? = null
