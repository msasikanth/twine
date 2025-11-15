package dev.sasikanth.rss.reader.resources.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Round
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val TwineIcons.CopyLink: ImageVector
  get() {
    if (copyLink != null) {
      return copyLink!!
    }
    copyLink =
      Builder(
          name = "Copy-link-stroke-rounded",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 24.0f,
          viewportHeight = 24.0f
        )
        .apply {
          path(
            fill = SolidColor(Color(0x00000000)),
            stroke = SolidColor(Color(0xFF000000)),
            strokeLineWidth = 1.5f,
            strokeLineCap = Round,
            strokeLineJoin = StrokeJoin.Companion.Round,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(14.556f, 13.218f)
            curveTo(13.514f, 14.261f, 11.824f, 14.261f, 10.782f, 13.218f)
            curveTo(9.739f, 12.176f, 9.739f, 10.486f, 10.782f, 9.444f)
            lineTo(13.141f, 7.084f)
            curveTo(14.136f, 6.09f, 15.721f, 6.044f, 16.769f, 6.949f)
            moveTo(16.444f, 3.782f)
            curveTo(17.486f, 2.739f, 19.176f, 2.739f, 20.218f, 3.782f)
            curveTo(21.261f, 4.824f, 21.261f, 6.514f, 20.218f, 7.556f)
            lineTo(17.859f, 9.915f)
            curveTo(16.864f, 10.91f, 15.279f, 10.956f, 14.231f, 10.051f)
          }
          path(
            fill = SolidColor(Color(0x00000000)),
            stroke = SolidColor(Color(0xFF000000)),
            strokeLineWidth = 1.5f,
            strokeLineCap = Round,
            strokeLineJoin = StrokeJoin.Companion.Round,
            strokeLineMiter = 4.0f,
            pathFillType = NonZero
          ) {
            moveTo(10.5f, 3.0f)
            curveTo(7.213f, 3.0f, 5.569f, 3.0f, 4.463f, 3.908f)
            curveTo(4.26f, 4.074f, 4.074f, 4.26f, 3.908f, 4.462f)
            curveTo(3.0f, 5.569f, 3.0f, 7.212f, 3.0f, 10.5f)
            lineTo(3.0f, 13.0f)
            curveTo(3.0f, 16.771f, 3.0f, 18.657f, 4.172f, 19.828f)
            curveTo(5.343f, 21.0f, 7.229f, 21.0f, 11.0f, 21.0f)
            horizontalLineTo(13.5f)
            curveTo(16.787f, 21.0f, 18.431f, 21.0f, 19.538f, 20.092f)
            curveTo(19.74f, 19.926f, 19.926f, 19.74f, 20.092f, 19.538f)
            curveTo(21.0f, 18.431f, 21.0f, 16.788f, 21.0f, 13.5f)
          }
        }
        .build()
    return copyLink!!
  }

private var copyLink: ImageVector? = null
