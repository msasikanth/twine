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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

actual fun String.formatReadingTrendDate(): String {
  return try {
    val date = LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
    date.format(DateTimeFormatter.ofPattern("dd-MMM-yy", Locale.getDefault()))
  } catch (_: Exception) {
    this
  }
}
