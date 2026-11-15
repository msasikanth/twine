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

package dev.sasikanth.rss.reader.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
class TranslucentStyle(
  private val backgroundGetter: () -> Color,
  private val foregroundGetter: () -> Color,
  private val outlineGetter: () -> Color,
) {
  constructor(
    background: Color,
    foreground: Color,
    outline: Color,
  ) : this({ background }, { foreground }, { outline })

  val background: Color
    get() = backgroundGetter()

  val foreground: Color
    get() = foregroundGetter()

  val outline: Color
    get() = outlineGetter()

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is TranslucentStyle) return false
    return background == other.background &&
      foreground == other.foreground &&
      outline == other.outline
  }

  override fun hashCode(): Int {
    var result = background.hashCode()
    result = 31 * result + foreground.hashCode()
    result = 31 * result + outline.hashCode()
    return result
  }
}

@Stable class TranslucentStyles(val default: TranslucentStyle, val prominent: TranslucentStyle)

internal val LocalTranslucentStyles =
  compositionLocalOf<TranslucentStyles> {
    throw IllegalStateException("Translucent styles is not provided")
  }
