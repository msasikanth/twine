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
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val TwineIcons.CustomTypography: ImageVector
  get() {
    if (customTypography != null) {
      return customTypography!!
    }
    customTypography =
      Builder(
          name = "CustomTypography",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
        )
        .apply {
          group {
            path(
              fill = SolidColor(Color(0xFFC2CAAA)),
              stroke = null,
              strokeLineWidth = 0.0f,
              strokeLineCap = Butt,
              strokeLineJoin = Miter,
              strokeLineMiter = 4.0f,
              pathFillType = NonZero
            ) {
              moveTo(9.5f, 18.5f)
              verticalLineTo(13.0f)
              horizontalLineTo(11.0f)
              verticalLineTo(15.0f)
              horizontalLineTo(17.0f)
              verticalLineTo(16.5f)
              horizontalLineTo(11.0f)
              verticalLineTo(18.5f)
              horizontalLineTo(9.5f)
              close()
              moveTo(3.0f, 16.5f)
              verticalLineTo(15.0f)
              horizontalLineTo(8.0f)
              verticalLineTo(16.5f)
              horizontalLineTo(3.0f)
              close()
              moveTo(5.813f, 11.0f)
              horizontalLineTo(7.369f)
              lineTo(8.194f, 8.708f)
              horizontalLineTo(11.813f)
              lineTo(12.623f, 11.0f)
              horizontalLineTo(14.188f)
              lineTo(10.792f, 2.0f)
              horizontalLineTo(9.208f)
              lineTo(5.813f, 11.0f)
              close()
              moveTo(8.667f, 7.396f)
              lineTo(10.042f, 3.667f)
              lineTo(11.354f, 7.396f)
              horizontalLineTo(8.667f)
              close()
            }
          }
        }
        .build()
    return customTypography!!
  }

private var customTypography: ImageVector? = null
