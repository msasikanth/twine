/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.util

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitNanosecond
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

@Throws(DateTimeFormatException::class)
actual fun String?.dateStringToEpochMillis(clock: Clock): Long? {
  if (this.isNullOrBlank()) return null

  try {
    val date =
      dateFormatterPatterns.firstNotNullOfOrNull { pattern ->
        val timeZone =
          if (hasTimeZonePattern(pattern)) {
            null
          } else {
            TimeZone.UTC
          }
        val dateTimeFormatter =
          createDateFormatter(
            pattern = pattern,
            timeZone = timeZone,
          )

        dateTimeFormatter.dateFromString(this.trim())
      }

    if (date != null) {
      val currentDate = clock.now().toNSDate()
      val calendar = NSCalendar(NSCalendarIdentifierGregorian)

      val currentYear = calendar.component(NSCalendarUnitYear, currentDate)
      val parsedYear = calendar.component(NSCalendarUnitYear, date)

      val updatedDateComponents =
        calendar.components(
          NSCalendarUnitYear +
            NSCalendarUnitMonth +
            NSCalendarUnitDay +
            NSCalendarUnitHour +
            NSCalendarUnitMinute +
            NSCalendarUnitSecond +
            NSCalendarUnitNanosecond,
          date
        )

      // 2000 seems to be the default year for gregorian calendar in iOS?
      updatedDateComponents.year =
        if (parsedYear > 2000) {
          parsedYear
        } else {
          currentYear
        }

      val updatedDate = calendar.dateFromComponents(updatedDateComponents)
      return updatedDate?.timeIntervalSince1970?.times(1000)?.toLong()
    }
  } catch (e: Exception) {
    // no-op
  }

  return null
}

private fun hasTimeZonePattern(pattern: String) =
  pattern.contains("Z", ignoreCase = true) ||
    pattern.contains("O", ignoreCase = true) ||
    pattern.contains("X", ignoreCase = true)

private fun createDateFormatter(
  pattern: String,
  timeZone: TimeZone? = null,
): NSDateFormatter {
  return NSDateFormatter().apply {
    dateFormat = pattern
    locale = NSLocale("en_US_POSIX")

    timeZone?.let { this.timeZone = it.toNSTimeZone() }
  }
}
