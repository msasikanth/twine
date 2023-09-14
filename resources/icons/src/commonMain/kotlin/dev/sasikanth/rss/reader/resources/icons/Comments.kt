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

val TwineIcons.Comments: ImageVector
  get() {
    if (comments != null) {
      return comments!!
    }
    comments =
      Builder(
          name = "Comments",
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
            moveTo(21.99f, 4.0f)
            curveToRelative(0.0f, -1.1f, -0.89f, -2.0f, -1.99f, -2.0f)
            lineTo(4.0f, 2.0f)
            curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
            verticalLineToRelative(12.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(14.0f)
            lineToRelative(4.0f, 4.0f)
            lineToRelative(-0.01f, -18.0f)
            close()
            moveTo(20.0f, 4.0f)
            verticalLineToRelative(13.17f)
            lineTo(18.83f, 16.0f)
            lineTo(4.0f, 16.0f)
            lineTo(4.0f, 4.0f)
            horizontalLineToRelative(16.0f)
            close()
            moveTo(6.0f, 12.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(2.0f)
            lineTo(6.0f, 14.0f)
            close()
            moveTo(6.0f, 9.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(2.0f)
            lineTo(6.0f, 11.0f)
            close()
            moveTo(6.0f, 6.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(2.0f)
            lineTo(6.0f, 8.0f)
            close()
          }
        }
        .build()
    return comments!!
  }

private var comments: ImageVector? = null
