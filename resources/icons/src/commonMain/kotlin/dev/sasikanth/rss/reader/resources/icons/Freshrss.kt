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

public val TwineIcons.Freshrss: ImageVector
  get() {
    if (_freshrss != null) {
      return _freshrss!!
    }
    _freshrss =
      Builder(
          name = "Freshrss",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
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
            moveTo(11.738f, 0.003f)
            curveTo(5.217f, 0.151f, 0.006f, 5.476f, 0.0f, 11.999f)
            horizontalLineToRelative(2.25f)
            arcToRelative(9.74f, 9.74f, 0.0f, false, true, 6.02f, -9.008f)
            arcToRelative(9.74f, 9.74f, 0.0f, false, true, 10.628f, 2.113f)
            arcToRelative(9.74f, 9.74f, 0.0f, false, true, 2.113f, 10.626f)
            arcToRelative(9.74f, 9.74f, 0.0f, false, true, -9.01f, 6.02f)
            verticalLineTo(24.0f)
            curveToRelative(4.85f, 0.0f, 9.23f, -2.927f, 11.088f, -7.408f)
            arcTo(12.0f, 12.0f, 0.0f, false, false, 11.738f, 0.003f)
            moveToRelative(0.264f, 0.5f)
            verticalLineToRelative(1.252f)
            curveToRelative(-1.32f, 0.0f, -2.653f, 0.25f, -3.922f, 0.775f)
            curveToRelative(-3.674f, 1.521f, -6.06f, 5.03f, -6.256f, 8.97f)
            horizontalLineTo(0.574f)
            curveToRelative(0.2f, -4.443f, 2.89f, -8.413f, 7.028f, -10.126f)
            arcTo(11.4f, 11.4f, 0.0f, false, true, 12.0f, 0.503f)
            moveToRelative(-0.031f, 3.434f)
            arcToRelative(8.0f, 8.0f, 0.0f, false, false, -3.055f, 0.613f)
            arcTo(8.07f, 8.07f, 0.0f, false, false, 3.938f, 12.0f)
            horizontalLineToRelative(2.25f)
            arcToRelative(5.8f, 5.8f, 0.0f, false, true, 3.589f, -5.37f)
            arcToRelative(5.81f, 5.81f, 0.0f, false, true, 6.334f, 1.26f)
            arcToRelative(5.8f, 5.8f, 0.0f, false, true, 1.26f, 6.335f)
            arcToRelative(5.8f, 5.8f, 0.0f, false, true, -5.37f, 3.588f)
            verticalLineToRelative(2.25f)
            arcToRelative(8.07f, 8.07f, 0.0f, false, false, 7.451f, -4.977f)
            arcToRelative(8.07f, 8.07f, 0.0f, false, false, -1.75f, -8.788f)
            curveToRelative(-2.125f, -2.125f, -4.667f, -2.365f, -5.732f, -2.362f)
            moveToRelative(0.03f, 0.501f)
            verticalLineTo(5.69f)
            arcToRelative(6.3f, 6.3f, 0.0f, false, false, -2.415f, 0.477f)
            curveToRelative(-2.2f, 0.911f, -3.633f, 2.987f, -3.823f, 5.332f)
            horizontalLineToRelative(-1.25f)
            curveTo(4.703f, 8.65f, 6.44f, 6.115f, 9.105f, 5.012f)
            arcTo(7.5f, 7.5f, 0.0f, false, true, 12.0f, 4.438f)
            moveTo(18.312f, 12.0f)
            horizontalLineToRelative(1.248f)
            arcToRelative(7.6f, 7.6f, 0.0f, false, true, -0.57f, 2.896f)
            curveToRelative(-1.104f, 2.664f, -3.639f, 4.4f, -6.488f, 4.593f)
            verticalLineToRelative(-1.25f)
            curveToRelative(2.345f, -0.19f, 4.42f, -1.621f, 5.333f, -3.822f)
            arcTo(6.3f, 6.3f, 0.0f, false, false, 18.312f, 12.0f)
            moveToRelative(3.936f, 0.0f)
            horizontalLineToRelative(1.248f)
            curveToRelative(0.0f, 1.483f, -0.278f, 2.978f, -0.867f, 4.4f)
            curveToRelative(-1.714f, 4.137f, -5.685f, 6.828f, -10.127f, 7.027f)
            verticalLineToRelative(-1.25f)
            curveToRelative(3.94f, -0.197f, 7.45f, -2.582f, 8.97f, -6.254f)
            arcTo(10.3f, 10.3f, 0.0f, false, false, 22.249f, 12.0f)
            moveToRelative(-7.155f, 0.0f)
            arcTo(3.094f, 3.094f, 0.0f, false, true, 12.0f, 15.094f)
            arcTo(3.094f, 3.094f, 0.0f, false, true, 8.906f, 12.0f)
            arcTo(3.094f, 3.094f, 0.0f, false, true, 12.0f, 8.906f)
            arcTo(3.094f, 3.094f, 0.0f, false, true, 15.094f, 12.0f)
          }
        }
        .build()
    return _freshrss!!
  }

private var _freshrss: ImageVector? = null
