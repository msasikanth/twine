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

val TwineIcons.All: ImageVector
  get() {
    if (all != null) {
      return all!!
    }
    all =
      Builder(
          name = "All",
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
            moveTo(12.0f, 5.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(5.0f, 12.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(19.0f, 12.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
          path(
            fill = SolidColor(Color(0xFF000000)),
            stroke = null,
            strokeLineWidth = 0.0f,
            strokeLineCap = Butt,
            strokeLineJoin = Miter,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(12.0f, 19.0f)
            moveToRelative(-3.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, 6.0f, 0.0f)
            arcToRelative(3.0f, 3.0f, 0.0f, true, true, -6.0f, 0.0f)
          }
        }
        .build()
    return all!!
  }

private var all: ImageVector? = null
