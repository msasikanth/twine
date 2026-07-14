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

package dev.sasikanth.rss.reader.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

/**
 * Bridges the app's backdrop color to the desktop window. The last color is replayed on [listener]
 * registration since the theme usually composes before the listener attaches.
 */
object DesktopWindowChrome {
  private var lastBackdropArgb: Int? = null

  var listener: ((argb: Int) -> Unit)? = null
    set(value) {
      field = value
      lastBackdropArgb?.let { value?.invoke(it) }
    }

  internal fun update(argb: Int) {
    lastBackdropArgb = argb
    listener?.invoke(argb)
  }
}

internal actual fun updateWindowBackdropColor(color: Color) {
  DesktopWindowChrome.update(color.toArgb())
}
