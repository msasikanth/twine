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

val TwineIcons.Twitter: ImageVector
  get() {
    if (twitter != null) {
      return twitter!!
    }
    twitter =
      Builder(
          name = "Twitter",
          defaultWidth = 20.0.dp,
          defaultHeight = 20.0.dp,
          viewportWidth = 20.0f,
          viewportHeight = 20.0f
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
            moveTo(14.7673f, 2.5f)
            horizontalLineTo(17.3129f)
            lineTo(11.7528f, 8.8534f)
            lineTo(18.2937f, 17.5f)
            horizontalLineTo(13.1735f)
            lineTo(9.1603f, 12.2572f)
            lineTo(4.5738f, 17.5f)
            horizontalLineTo(2.0245f)
            lineTo(7.9704f, 10.7031f)
            lineTo(1.6999f, 2.5f)
            horizontalLineTo(6.95f)
            lineTo(10.5737f, 7.2921f)
            lineTo(14.7673f, 2.5f)
            close()
            moveTo(13.873f, 15.9784f)
            horizontalLineTo(15.2829f)
            lineTo(6.1819f, 3.9423f)
            horizontalLineTo(4.6675f)
            lineTo(13.873f, 15.9784f)
            close()
          }
        }
        .build()
    return twitter!!
  }

private var twitter: ImageVector? = null
