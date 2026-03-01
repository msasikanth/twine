/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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

public val TwineIcons.LightAndDarkMode: ImageVector
  get() {
    if (_routine != null) {
      return _routine!!
    }
    _routine =
      ImageVector.Builder(
          name = "Routine",
          defaultWidth = 24.0.dp,
          defaultHeight = 24.0.dp,
          viewportWidth = 960.0f,
          viewportHeight = 960.0f,
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
              pathFillType = NonZero,
            ) {
              /* pathData = "M337.5-463Q311-498 289-537q-5 14-6.5 28.5T281-480q0 83 58 141t141 58q14 0 28.5-2t28.5-6q-39-22-74-48.5T396-396q-32-32-58.5-67ZM481-200q-56 0-107-21t-91-61q-40-40-61-91t-21-107q0-51 17-97.5t50-84.5q13-14 32-9.5t27 24.5q21 55 52.5 104t73.5 91q42 42 91 73.5T648-326q20 8 24.5 27t-9.5 32q-38 33-84.5 50T481-200Zm223-192q-16-5-23-20.5t-4-32.5q9-48-6-94.5T621-621q-35-35-80.5-49.5T448-677q-17 3-32-4t-21-23q-6-16 1.5-31t23.5-19q69-15 138 4.5T679-678q51 51 71 120t5 138q-4 17-19 25t-32 3ZM480-840q-17 0-28.5-11.5T440-880v-40q0-17 11.5-28.5T480-960q17 0 28.5 11.5T520-920v40q0 17-11.5 28.5T480-840Zm0 840q-17 0-28.5-11.5T440-40v-40q0-17 11.5-28.5T480-120q17 0 28.5 11.5T520-80v40q0 17-11.5 28.5T480 0Zm255-734q-12-12-12-28.5t12-28.5l28-28q11-11 27.5-11t28.5 11q12 12 12 28.5T819-762l-28 28q-12 12-28 12t-28-12ZM141-141q-12-12-12-28.5t12-28.5l28-28q12-12 28-12t28 12q12 12 12 28.5T225-169l-28 28q-11 11-27.5 11T141-141Zm739-299q-17 0-28.5-11.5T840-480q0-17 11.5-28.5T880-520h40q17 0 28.5 11.5T960-480q0 17-11.5 28.5T920-440h-40Zm-840 0q-17 0-28.5-11.5T0-480q0-17 11.5-28.5T40-520h40q17 0 28.5 11.5T120-480q0 17-11.5 28.5T80-440H40Zm779 299q-12 12-28.5 12T762-141l-28-28q-12-12-12-28t12-28q12-12 28.5-12t28.5 12l28 28q11 11 11 27.5T819-141ZM226-735q-12 12-28.5 12T169-735l-28-28q-11-11-11-27.5t11-28.5q12-12 28.5-12t28.5 12l28 28q12 12 12 28t-12 28Zm170 339Z" */
              moveTo(337.5f, -463.0f)
              quadTo(311.0f, -498.0f, 289.0f, -537.0f)
              quadToRelative(-5.0f, 14.0f, -6.5f, 28.5f)
              reflectiveQuadTo(281.0f, -480.0f)
              quadToRelative(0.0f, 83.0f, 58.0f, 141.0f)
              reflectiveQuadToRelative(141.0f, 58.0f)
              quadToRelative(14.0f, 0.0f, 28.5f, -2.0f)
              reflectiveQuadToRelative(28.5f, -6.0f)
              quadToRelative(-39.0f, -22.0f, -74.0f, -48.5f)
              reflectiveQuadTo(396.0f, -396.0f)
              quadToRelative(-32.0f, -32.0f, -58.5f, -67.0f)
              close()
              moveTo(481.0f, -200.0f)
              quadToRelative(-56.0f, 0.0f, -107.0f, -21.0f)
              reflectiveQuadToRelative(-91.0f, -61.0f)
              quadToRelative(-40.0f, -40.0f, -61.0f, -91.0f)
              reflectiveQuadToRelative(-21.0f, -107.0f)
              quadToRelative(0.0f, -51.0f, 17.0f, -97.5f)
              reflectiveQuadToRelative(50.0f, -84.5f)
              quadToRelative(13.0f, -14.0f, 32.0f, -9.5f)
              reflectiveQuadToRelative(27.0f, 24.5f)
              quadToRelative(21.0f, 55.0f, 52.5f, 104.0f)
              reflectiveQuadToRelative(73.5f, 91.0f)
              quadToRelative(42.0f, 42.0f, 91.0f, 73.5f)
              reflectiveQuadTo(648.0f, -326.0f)
              quadToRelative(20.0f, 8.0f, 24.5f, 27.0f)
              reflectiveQuadToRelative(-9.5f, 32.0f)
              quadToRelative(-38.0f, 33.0f, -84.5f, 50.0f)
              reflectiveQuadTo(481.0f, -200.0f)
              close()
              moveToRelative(223.0f, -192.0f)
              quadToRelative(-16.0f, -5.0f, -23.0f, -20.5f)
              reflectiveQuadToRelative(-4.0f, -32.5f)
              quadToRelative(9.0f, -48.0f, -6.0f, -94.5f)
              reflectiveQuadTo(621.0f, -621.0f)
              quadToRelative(-35.0f, -35.0f, -80.5f, -49.5f)
              reflectiveQuadTo(448.0f, -677.0f)
              quadToRelative(-17.0f, 3.0f, -32.0f, -4.0f)
              reflectiveQuadToRelative(-21.0f, -23.0f)
              quadToRelative(-6.0f, -16.0f, 1.5f, -31.0f)
              reflectiveQuadToRelative(23.5f, -19.0f)
              quadToRelative(69.0f, -15.0f, 138.0f, 4.5f)
              reflectiveQuadTo(679.0f, -678.0f)
              quadToRelative(51.0f, 51.0f, 71.0f, 120.0f)
              reflectiveQuadToRelative(5.0f, 138.0f)
              quadToRelative(-4.0f, 17.0f, -19.0f, 25.0f)
              reflectiveQuadToRelative(-32.0f, 3.0f)
              close()
              moveTo(480.0f, -840.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(440.0f, -880.0f)
              verticalLineToRelative(-40.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(480.0f, -960.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(520.0f, -920.0f)
              verticalLineToRelative(40.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(480.0f, -840.0f)
              close()
              moveToRelative(0.0f, 840.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(440.0f, -40.0f)
              verticalLineToRelative(-40.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(480.0f, -120.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(520.0f, -80.0f)
              verticalLineToRelative(40.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(480.0f, 0.0f)
              close()
              moveToRelative(255.0f, -734.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadToRelative(12.0f, -28.5f)
              lineToRelative(28.0f, -28.0f)
              quadToRelative(11.0f, -11.0f, 27.5f, -11.0f)
              reflectiveQuadToRelative(28.5f, 11.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              reflectiveQuadTo(819.0f, -762.0f)
              lineToRelative(-28.0f, 28.0f)
              quadToRelative(-12.0f, 12.0f, -28.0f, 12.0f)
              reflectiveQuadToRelative(-28.0f, -12.0f)
              close()
              moveTo(141.0f, -141.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.5f)
              reflectiveQuadToRelative(12.0f, -28.5f)
              lineToRelative(28.0f, -28.0f)
              quadToRelative(12.0f, -12.0f, 28.0f, -12.0f)
              reflectiveQuadToRelative(28.0f, 12.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.5f)
              reflectiveQuadTo(225.0f, -169.0f)
              lineToRelative(-28.0f, 28.0f)
              quadToRelative(-11.0f, 11.0f, -27.5f, 11.0f)
              reflectiveQuadTo(141.0f, -141.0f)
              close()
              moveToRelative(739.0f, -299.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(840.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(880.0f, -520.0f)
              horizontalLineToRelative(40.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(960.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(920.0f, -440.0f)
              horizontalLineToRelative(-40.0f)
              close()
              moveToRelative(-840.0f, 0.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(0.0f, -480.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(40.0f, -520.0f)
              horizontalLineToRelative(40.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(120.0f, -480.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(80.0f, -440.0f)
              horizontalLineTo(40.0f)
              close()
              moveToRelative(779.0f, 299.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 12.0f)
              reflectiveQuadTo(762.0f, -141.0f)
              lineToRelative(-28.0f, -28.0f)
              quadToRelative(-12.0f, -12.0f, -12.0f, -28.0f)
              reflectiveQuadToRelative(12.0f, -28.0f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.0f)
              reflectiveQuadToRelative(28.5f, 12.0f)
              lineToRelative(28.0f, 28.0f)
              quadToRelative(11.0f, 11.0f, 11.0f, 27.5f)
              reflectiveQuadTo(819.0f, -141.0f)
              close()
              moveTo(226.0f, -735.0f)
              quadToRelative(-12.0f, 12.0f, -28.5f, 12.0f)
              reflectiveQuadTo(169.0f, -735.0f)
              lineToRelative(-28.0f, -28.0f)
              quadToRelative(-11.0f, -11.0f, -11.0f, -27.5f)
              reflectiveQuadToRelative(11.0f, -28.5f)
              quadToRelative(12.0f, -12.0f, 28.5f, -12.0f)
              reflectiveQuadToRelative(28.5f, 12.0f)
              lineToRelative(28.0f, 28.0f)
              quadToRelative(12.0f, 12.0f, 12.0f, 28.0f)
              reflectiveQuadToRelative(-12.0f, 28.0f)
              close()
              moveToRelative(170.0f, 339.0f)
              close()
            }
          }
        }
        .build()
    return _routine!!
  }

private var _routine: ImageVector? = null
