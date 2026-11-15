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

import kotlin.native.concurrent.ThreadLocal
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localeIdentifier

@ThreadLocal
private object ThreadLocalDisplayDateFormatters {
  var readerLocaleId: String? = null
  val readerDateFormatter = NSDateFormatter().apply { dateStyle = NSDateFormatterMediumStyle }
  val readerTimeFormatter = NSDateFormatter().apply { timeStyle = NSDateFormatterShortStyle }

  var appBarLocaleId: String? = null
  val appBarFormatter = NSDateFormatter()
}

actual fun Instant.readerDateTimestamp(): String {
  val currentLocale = NSLocale.currentLocale()
  val localeId = currentLocale.localeIdentifier
  val cache = ThreadLocalDisplayDateFormatters
  if (cache.readerLocaleId != localeId) {
    cache.readerDateFormatter.locale = currentLocale
    cache.readerTimeFormatter.locale = currentLocale
    cache.readerLocaleId = localeId
  }

  val nsDate = toNSDate()
  val formattedDate = cache.readerDateFormatter.stringFromDate(nsDate)
  val formattedTime = cache.readerTimeFormatter.stringFromDate(nsDate)

  return "$formattedDate • $formattedTime"
}

actual fun LocalDateTime.homeAppBarTimestamp(): String {
  val currentLocale = NSLocale.currentLocale()
  val localeId = currentLocale.localeIdentifier
  val cache = ThreadLocalDisplayDateFormatters
  if (cache.appBarLocaleId != localeId) {
    val datePattern =
      when (currentLocale.languageCode()) {
        "de" -> "EEE, d. MMM"
        "fr" -> "EEE d MMM"
        else -> "EEE, MMM d"
      }
    cache.appBarFormatter.locale = currentLocale
    cache.appBarFormatter.dateFormat = datePattern
    cache.appBarLocaleId = localeId
  }

  val date = this.toInstant(TimeZone.currentSystemDefault()).toNSDate()
  return cache.appBarFormatter.stringFromDate(date)
}
