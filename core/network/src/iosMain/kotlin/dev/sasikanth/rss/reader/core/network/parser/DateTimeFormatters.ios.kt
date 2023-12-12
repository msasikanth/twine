/*
 * Copyright 2023 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.core.network.parser

import kotlinx.datetime.TimeZone
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
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.timeIntervalSince1970

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

private val dateFormatters =
  listOf(
    // Keep the two character year before parsing the four
    // character year of similar pattern. Not sure why,
    // but unlike JVM, iOS is not keep it strict?
    createDateFormatter("E, d MMM yy HH:mm:ss Z"),
    createDateFormatter("E, d MMM yyyy HH:mm:ss O"),
    createDateFormatter("E, d MMM yyyy HH:mm:ss Z"),
    createDateFormatter("E, d MMM yyyy HH:mm:ss z"),
    createDateFormatter("E, d MMM yyyy HH:mm Z"),
    createDateFormatter("E, dd MMM yyyy", TimeZone.UTC),
    createDateFormatter("d MMM yyyy HH:mm:ss z"),
    createDateFormatter("MM-dd HH:mm:ss", TimeZone.UTC),
    createDateFormatter("yyyy-MM-dd'T'HH:mm:ssz"),
    createDateFormatter("yyyy-MM-dd'T'HH:mm:ssZ"),
    createDateFormatter("yyyy-MM-dd'T'HH:mm:ss", TimeZone.UTC),
    createDateFormatter("yyyy-MM-dd HH:mm:ss", TimeZone.UTC),
    createDateFormatter("yyyy-MM-dd HH:mm:ss z"),
    createDateFormatter("yyyy-MM-dd", TimeZone.UTC),
    createDateFormatter("E, d MMM yyyy HH:mm:ss zzzz"),
    createDateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
  )

@Throws(DateTimeFormatException::class)
internal actual fun String?.dateStringToEpochMillis(): Long? {
  if (this.isNullOrBlank()) return null

  try {
    val date =
      dateFormatters.firstNotNullOfOrNull { dateFormatter ->
        dateFormatter.dateFromString(this.trim())
      }

    if (date != null) {
      val currentDate = NSDate()
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
