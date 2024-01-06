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

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.UnsupportedTemporalTypeException
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime

private val dateFormatters =
  listOf(
    DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss O"),
    DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss Z"),
    DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss z"),
    DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm Z"),
    DateTimeFormatter.ofPattern("E, d MMM yy HH:mm:ss Z"),
    DateTimeFormatter.ofPattern("E, dd MMM yyyy"),
    DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss z"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss zzzz"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"),
  )

@Throws(DateTimeFormatException::class)
internal actual fun String?.dateStringToEpochMillis(clock: Clock): Long? {
  if (this.isNullOrBlank()) return null

  val currentDate =
    clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()

  for (dateFormatter in dateFormatters) {
    try {
      val parsedValue = dateFormatter.parse(this)

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

      return updatedDate.atZone(zoneId).toInstant().toEpochMilli()
    } catch (e: Exception) {
      // no-op
    }
  }

  return null
}
