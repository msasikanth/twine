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

public val TwineIcons.Cards: ImageVector
  get() {
    if (_cards != null) {
      return _cards!!
    }
    _cards =
      ImageVector.Builder(
          name = "Cards",
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
              /* pathData = "M420-500H280q-17 0-28.5-11.5T240-540v-140q0-17 11.5-28.5T280-720h140q17 0 28.5 11.5T460-680v140q0 17-11.5 28.5T420-500Zm0 260H280q-17 0-28.5-11.5T240-280v-140q0-17 11.5-28.5T280-460h140q17 0 28.5 11.5T460-420v140q0 17-11.5 28.5T420-240Zm260-260H540q-17 0-28.5-11.5T500-540v-140q0-17 11.5-28.5T540-720h140q17 0 28.5 11.5T720-680v140q0 17-11.5 28.5T680-500Zm0 260H540q-17 0-28.5-11.5T500-280v-140q0-17 11.5-28.5T540-460h140q17 0 28.5 11.5T720-420v140q0 17-11.5 28.5T680-240ZM320-580h60v-60h-60v60Zm260 0h60v-60h-60v60ZM320-320h60v-60h-60v60Zm260 0h60v-60h-60v60ZM380-580Zm200 0Zm0 200Zm-200 0ZM200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Z" */
              moveTo(420.0f, -500.0f)
              horizontalLineTo(280.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(240.0f, -540.0f)
              verticalLineToRelative(-140.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(280.0f, -720.0f)
              horizontalLineToRelative(140.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(460.0f, -680.0f)
              verticalLineToRelative(140.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(420.0f, -500.0f)
              close()
              moveToRelative(0.0f, 260.0f)
              horizontalLineTo(280.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(240.0f, -280.0f)
              verticalLineToRelative(-140.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(280.0f, -460.0f)
              horizontalLineToRelative(140.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(460.0f, -420.0f)
              verticalLineToRelative(140.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(420.0f, -240.0f)
              close()
              moveToRelative(260.0f, -260.0f)
              horizontalLineTo(540.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(500.0f, -540.0f)
              verticalLineToRelative(-140.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(540.0f, -720.0f)
              horizontalLineToRelative(140.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(720.0f, -680.0f)
              verticalLineToRelative(140.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(680.0f, -500.0f)
              close()
              moveToRelative(0.0f, 260.0f)
              horizontalLineTo(540.0f)
              quadToRelative(-17.0f, 0.0f, -28.5f, -11.5f)
              reflectiveQuadTo(500.0f, -280.0f)
              verticalLineToRelative(-140.0f)
              quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
              reflectiveQuadTo(540.0f, -460.0f)
              horizontalLineToRelative(140.0f)
              quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
              reflectiveQuadTo(720.0f, -420.0f)
              verticalLineToRelative(140.0f)
              quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
              reflectiveQuadTo(680.0f, -240.0f)
              close()
              moveTo(320.0f, -580.0f)
              horizontalLineToRelative(60.0f)
              verticalLineToRelative(-60.0f)
              horizontalLineToRelative(-60.0f)
              verticalLineToRelative(60.0f)
              close()
              moveToRelative(260.0f, 0.0f)
              horizontalLineToRelative(60.0f)
              verticalLineToRelative(-60.0f)
              horizontalLineToRelative(-60.0f)
              verticalLineToRelative(60.0f)
              close()
              moveTo(320.0f, -320.0f)
              horizontalLineToRelative(60.0f)
              verticalLineToRelative(-60.0f)
              horizontalLineToRelative(-60.0f)
              verticalLineToRelative(60.0f)
              close()
              moveToRelative(260.0f, 0.0f)
              horizontalLineToRelative(60.0f)
              verticalLineToRelative(-60.0f)
              horizontalLineToRelative(-60.0f)
              verticalLineToRelative(60.0f)
              close()
              moveTo(380.0f, -580.0f)
              close()
              moveToRelative(200.0f, 0.0f)
              close()
              moveToRelative(0.0f, 200.0f)
              close()
              moveToRelative(-200.0f, 0.0f)
              close()
              moveTo(200.0f, -120.0f)
              quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
              reflectiveQuadTo(120.0f, -200.0f)
              verticalLineToRelative(-560.0f)
              quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
              reflectiveQuadTo(200.0f, -840.0f)
              horizontalLineToRelative(560.0f)
              quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
              reflectiveQuadTo(840.0f, -760.0f)
              verticalLineToRelative(560.0f)
              quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
              reflectiveQuadTo(760.0f, -120.0f)
              horizontalLineTo(200.0f)
              close()
              moveToRelative(0.0f, -80.0f)
              horizontalLineToRelative(560.0f)
              verticalLineToRelative(-560.0f)
              horizontalLineTo(200.0f)
              verticalLineToRelative(560.0f)
              close()
            }
          }
        }
        .build()
    return _cards!!
  }

private var _cards: ImageVector? = null
