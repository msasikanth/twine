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

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.UnsupportedTemporalTypeException
import java.util.Locale
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime

@Throws(DateTimeFormatException::class)
actual fun String?.dateStringToEpochMillis(clock: Clock): Long? {
  if (this.isNullOrBlank()) return null

  val currentDate =
    clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()

  for (pattern in dateFormatterPatterns) {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(pattern, Locale.US)

    try {
      val parsedValue = parseToInstant(dateTimeFormatter, this)
      return parsedValue.toEpochMilli()
    } catch (e: Exception) {
      try {
        val parsedValue = fallbackParseToInstant(currentDate, dateTimeFormatter, this)
        return parsedValue.toEpochMilli()
      } catch (e: Exception) {
        // no-op
      }
    }
  }

  return null
}

private fun parseToInstant(dateTimeFormatter: DateTimeFormatter, text: String): Instant {
  return dateTimeFormatter.parse(text, Instant::from)
}

/**
 * In case the date string has values missing from it like year, day, hour, etc., we fill those
 * using [currentDate] and then try to convert it to instant.
 */
private fun fallbackParseToInstant(
  currentDate: LocalDateTime,
  dateTimeFormatter: DateTimeFormatter,
  text: String
): Instant {
  val parsedValue = dateTimeFormatter.parse(text)

  val year =
    try {
      parsedValue.get(ChronoField.YEAR)
    } catch (e: UnsupportedTemporalTypeException) {
      currentDate.get(ChronoField.YEAR)
    }
  val hourOfDay =
    try {
      parsedValue.get(ChronoField.HOUR_OF_DAY)
    } catch (e: UnsupportedTemporalTypeException) {
      0
    }
  val minuteOfHour =
    try {
      parsedValue.get(ChronoField.MINUTE_OF_HOUR)
    } catch (e: UnsupportedTemporalTypeException) {
      0
    }
  val secondOfMinute =
    try {
      parsedValue.get(ChronoField.SECOND_OF_MINUTE)
    } catch (e: UnsupportedTemporalTypeException) {
      0
    }
  val nanoOfSecond =
    try {
      parsedValue.get(ChronoField.NANO_OF_SECOND)
    } catch (e: UnsupportedTemporalTypeException) {
      0
    }

  val updatedDate =
    currentDate
      .withDayOfMonth(parsedValue.get(ChronoField.DAY_OF_MONTH))
      .withMonth(parsedValue.get(ChronoField.MONTH_OF_YEAR))
      .withYear(year)
      .withHour(hourOfDay)
      .withMinute(minuteOfHour)
      .withSecond(secondOfMinute)
      .withNano(nanoOfSecond)

  val zoneId =
    try {
      ZoneId.from(parsedValue)
    } catch (e: Exception) {
      TimeZone.UTC.toJavaZoneId()
    }

  return updatedDate.atZone(zoneId).toInstant()
}
