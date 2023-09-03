package dev.sasikanth.rss.reader.resources.icons.icon.twineicons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.resources.icons.icon.TwineIcons

val TwineIcons.Bookmarked: ImageVector
  get() {
    if (bookmarked != null) {
      return bookmarked!!
    }
    bookmarked =
      Builder(
          name = "Bookmarked",
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
            moveTo(17.0f, 3.0f)
            horizontalLineTo(7.0f)
            curveToRelative(-1.1f, 0.0f, -1.99f, 0.9f, -1.99f, 2.0f)
            lineTo(5.0f, 21.0f)
            lineToRelative(7.0f, -3.0f)
            lineToRelative(7.0f, 3.0f)
            verticalLineTo(5.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            close()
          }
        }
        .build()
    return bookmarked!!
  }

private var bookmarked: ImageVector? = null
