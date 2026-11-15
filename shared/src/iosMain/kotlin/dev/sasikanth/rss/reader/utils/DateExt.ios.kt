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

import kotlin.native.concurrent.ThreadLocal
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

@ThreadLocal
private object ReadingTrendDateFormatters {
  var localeId: String? = null
  val parser = NSDateFormatter().apply { dateFormat = "yyyy-MM-dd" }
  val output = NSDateFormatter().apply { dateFormat = "dd-MMM-yy" }
}

actual fun String.formatReadingTrendDate(): String {
  val currentLocale = NSLocale.currentLocale()
  val currentLocaleId = currentLocale.localeIdentifier
  val cache = ReadingTrendDateFormatters
  if (cache.localeId != currentLocaleId) {
    cache.parser.locale = currentLocale
    cache.output.locale = currentLocale
    cache.localeId = currentLocaleId
  }

  val parsedDate = cache.parser.dateFromString(this) ?: return this
  return cache.output.stringFromDate(parsedDate)
}
