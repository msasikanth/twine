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
package dev.sasikanth.rss.reader.util

import android.os.Build
import android.text.Html
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
actual val canBlurImage: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

actual fun String.decodeHTMLString(): String {
  return try {
    Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()
  } catch (e: Exception) {
    this
  }
}
