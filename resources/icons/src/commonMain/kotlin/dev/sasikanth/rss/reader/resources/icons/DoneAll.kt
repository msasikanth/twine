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

val TwineIcons.DoneAll: ImageVector
  get() {
    if (doneAll != null) {
      return doneAll!!
    }
    doneAll =
      Builder(
          name = "DoneAll",
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
            moveTo(6.0f, 17.3f)
            lineTo(1.75f, 13.05f)
            curveTo(1.55f, 12.85f, 1.454f, 12.617f, 1.462f, 12.35f)
            curveTo(1.471f, 12.083f, 1.575f, 11.85f, 1.775f, 11.65f)
            curveTo(1.975f, 11.467f, 2.208f, 11.371f, 2.475f, 11.363f)
            curveTo(2.742f, 11.354f, 2.975f, 11.45f, 3.175f, 11.65f)
            lineTo(6.725f, 15.2f)
            lineTo(8.125f, 16.6f)
            lineTo(7.4f, 17.3f)
            curveTo(7.2f, 17.483f, 6.967f, 17.579f, 6.7f, 17.587f)
            curveTo(6.433f, 17.596f, 6.2f, 17.5f, 6.0f, 17.3f)
            close()
            moveTo(11.65f, 17.3f)
            lineTo(7.4f, 13.05f)
            curveTo(7.217f, 12.867f, 7.125f, 12.637f, 7.125f, 12.363f)
            curveTo(7.125f, 12.087f, 7.217f, 11.85f, 7.4f, 11.65f)
            curveTo(7.6f, 11.45f, 7.838f, 11.35f, 8.113f, 11.35f)
            curveTo(8.387f, 11.35f, 8.625f, 11.45f, 8.825f, 11.65f)
            lineTo(12.35f, 15.175f)
            lineTo(20.85f, 6.675f)
            curveTo(21.05f, 6.475f, 21.283f, 6.379f, 21.55f, 6.387f)
            curveTo(21.817f, 6.396f, 22.05f, 6.5f, 22.25f, 6.7f)
            curveTo(22.433f, 6.9f, 22.529f, 7.133f, 22.538f, 7.4f)
            curveTo(22.546f, 7.667f, 22.45f, 7.9f, 22.25f, 8.1f)
            lineTo(13.05f, 17.3f)
            curveTo(12.85f, 17.5f, 12.617f, 17.6f, 12.35f, 17.6f)
            curveTo(12.083f, 17.6f, 11.85f, 17.5f, 11.65f, 17.3f)
            close()
            moveTo(12.35f, 12.35f)
            lineTo(10.925f, 10.95f)
            lineTo(15.175f, 6.7f)
            curveTo(15.358f, 6.517f, 15.587f, 6.425f, 15.863f, 6.425f)
            curveTo(16.138f, 6.425f, 16.375f, 6.517f, 16.575f, 6.7f)
            curveTo(16.775f, 6.9f, 16.875f, 7.137f, 16.875f, 7.412f)
            curveTo(16.875f, 7.687f, 16.775f, 7.925f, 16.575f, 8.125f)
            lineTo(12.35f, 12.35f)
            close()
          }
        }
        .build()
    return doneAll!!
  }

private var doneAll: ImageVector? = null
