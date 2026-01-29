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

public val TwineIcons.HomeFilled: ImageVector
  get() {
    if (_homeFilled != null) {
      return _homeFilled!!
    }
    _homeFilled =
      ImageVector.Builder(
          name = "HomeFilled",
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
              /* pathData = "M160-200v-360q0-19 8.5-36t23.5-28l240-180q21-16 48-16t48 16l240 180q15 11 23.5 28t8.5 36v360q0 33-23.5 56.5T720-120H600q-17 0-28.5-11.5T560-160v-200q0-17-11.5-28.5T520-400h-80q-17 0-28.5 11.5T400-360v200q0 17-11.5 28.5T360-120H240q-33 0-56.5-23.5T160-200Z" */
              moveTo(160.0f, -200.0f)
              verticalLineToRelative(-360.0f)
              quadToRelative(0.0f, -19.0f, 8.5f, -36.0f)
              reflectiveQuadToRelative(23.5f, -28.0f)
              lineToRelative(240.0f, -180.0f)
              quadToRelative(21.0f, -16.0f, 48.0f, -16.0f)
              reflectiveQuadToRelative(48.0f, 16.0f)
              lineToRelative(240.0f, 180.0f)
              quadToRelative(15.0f, 11.0f, 23.5f, 28.0f)
              reflectiveQuadToRelative(8.5f, 36.0f)
              verticalLineToRelative(360.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(720.0f, -120.0f)
              horizontalLineTo(600.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(560.0f, -160.0f)
              verticalLineToRelative(-200.0f)
              quadToRelative(0.0f, -17.0f, -11.5f, -28.5f)
              reflectiveQuadTo(520.0f, -400.0f)
              horizontalLineToRelative(-80.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, 11.5f)
              reflectiveQuadTo(400.0f, -360.0f)
              verticalLineToRelative(200.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(360.0f, -120.0f)
              horizontalLineTo(240.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(160.0f, -200.0f)
              close()
            }
          }
        }
        .build()
    return _homeFilled!!
  }

private var _homeFilled: ImageVector? = null
