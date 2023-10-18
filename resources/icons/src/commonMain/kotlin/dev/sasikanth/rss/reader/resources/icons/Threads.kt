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

val TwineIcons.Threads: ImageVector
  get() {
    if (threads != null) {
      return threads!!
    }
    threads =
      Builder(
          name = "Threads",
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
            moveTo(14.8568f, 9.3305f)
            curveTo(14.7778f, 9.2926f, 14.6976f, 9.2562f, 14.6163f, 9.2213f)
            curveTo(14.4748f, 6.6139f, 13.0501f, 5.1211f, 10.6577f, 5.1059f)
            curveTo(10.6469f, 5.1058f, 10.6361f, 5.1058f, 10.6253f, 5.1058f)
            curveTo(9.1944f, 5.1058f, 8.0043f, 5.7166f, 7.2718f, 6.828f)
            lineTo(8.5875f, 7.7306f)
            curveTo(9.1347f, 6.9004f, 9.9935f, 6.7234f, 10.6259f, 6.7234f)
            curveTo(10.6332f, 6.7234f, 10.6406f, 6.7234f, 10.6478f, 6.7234f)
            curveTo(11.4355f, 6.7285f, 12.0299f, 6.9575f, 12.4146f, 7.4041f)
            curveTo(12.6946f, 7.7293f, 12.8818f, 8.1786f, 12.9745f, 8.7457f)
            curveTo(12.2761f, 8.627f, 11.5208f, 8.5905f, 10.7134f, 8.6368f)
            curveTo(8.4389f, 8.7678f, 6.9767f, 10.0943f, 7.0749f, 11.9376f)
            curveTo(7.1247f, 12.8726f, 7.5905f, 13.677f, 8.3864f, 14.2024f)
            curveTo(9.0594f, 14.6466f, 9.926f, 14.8639f, 10.8268f, 14.8147f)
            curveTo(12.0163f, 14.7495f, 12.9495f, 14.2956f, 13.6005f, 13.4658f)
            curveTo(14.095f, 12.8356f, 14.4077f, 12.0189f, 14.5458f, 10.9898f)
            curveTo(15.1127f, 11.3319f, 15.5328f, 11.7821f, 15.7648f, 12.3234f)
            curveTo(16.1594f, 13.2434f, 16.1824f, 14.7552f, 14.9488f, 15.9877f)
            curveTo(13.868f, 17.0675f, 12.5688f, 17.5346f, 10.6054f, 17.549f)
            curveTo(8.4274f, 17.5329f, 6.7802f, 16.8344f, 5.7093f, 15.473f)
            curveTo(4.7064f, 14.1981f, 4.1881f, 12.3568f, 4.1688f, 10.0f)
            curveTo(4.1881f, 7.6432f, 4.7064f, 5.8018f, 5.7093f, 4.527f)
            curveTo(6.7802f, 3.1656f, 8.4274f, 2.4671f, 10.6053f, 2.4509f)
            curveTo(12.7991f, 2.4672f, 14.475f, 3.169f, 15.5869f, 4.537f)
            curveTo(16.1322f, 5.2079f, 16.5432f, 6.0515f, 16.8142f, 7.0352f)
            lineTo(18.356f, 6.6238f)
            curveTo(18.0276f, 5.4131f, 17.5107f, 4.3697f, 16.8073f, 3.5045f)
            curveTo(15.3818f, 1.7506f, 13.297f, 0.852f, 10.6107f, 0.8333f)
            horizontalLineTo(10.6f)
            curveTo(7.9192f, 0.8519f, 5.8577f, 1.754f, 4.4728f, 3.5145f)
            curveTo(3.2404f, 5.0812f, 2.6047f, 7.261f, 2.5833f, 9.9936f)
            lineTo(2.5833f, 10.0f)
            lineTo(2.5833f, 10.0064f)
            curveTo(2.6047f, 12.7389f, 3.2404f, 14.9189f, 4.4728f, 16.4855f)
            curveTo(5.8577f, 18.246f, 7.9192f, 19.1481f, 10.6f, 19.1667f)
            horizontalLineTo(10.6107f)
            curveTo(12.9941f, 19.1501f, 14.6741f, 18.5261f, 16.0581f, 17.1434f)
            curveTo(17.8688f, 15.3344f, 17.8142f, 13.0669f, 17.2175f, 11.6749f)
            curveTo(16.7893f, 10.6767f, 15.973f, 9.8659f, 14.8568f, 9.3305f)
            close()
            moveTo(10.7416f, 13.1994f)
            curveTo(9.7448f, 13.2556f, 8.7091f, 12.8081f, 8.658f, 11.8498f)
            curveTo(8.6202f, 11.1391f, 9.1637f, 10.3462f, 10.8028f, 10.2518f)
            curveTo(10.9905f, 10.2409f, 11.1747f, 10.2357f, 11.3557f, 10.2357f)
            curveTo(11.951f, 10.2357f, 12.508f, 10.2935f, 13.0143f, 10.4042f)
            curveTo(12.8255f, 12.7629f, 11.7176f, 13.1459f, 10.7416f, 13.1994f)
            close()
          }
        }
        .build()
    return threads!!
  }

private var threads: ImageVector? = null
