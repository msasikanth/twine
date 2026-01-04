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

public val TwineIcons.CollapseContent: ImageVector
  get() {
    if (_collapseContent != null) {
      return _collapseContent!!
    }
    _collapseContent =
      ImageVector.Builder(
          name = "CollapseContent",
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
              moveTo(440.0f, -440.0f)
              verticalLineToRelative(240.0f)
              horizontalLineToRelative(-80.0f)
              verticalLineToRelative(-160.0f)
              horizontalLineTo(200.0f)
              verticalLineToRelative(-80.0f)
              horizontalLineToRelative(240.0f)
              close()
              moveToRelative(160.0f, -320.0f)
              verticalLineToRelative(160.0f)
              horizontalLineToRelative(160.0f)
              verticalLineToRelative(80.0f)
              horizontalLineTo(520.0f)
              verticalLineToRelative(-240.0f)
              horizontalLineToRelative(80.0f)
              close()
            }
          }
        }
        .build()
    return _collapseContent!!
  }

private var _collapseContent: ImageVector? = null
