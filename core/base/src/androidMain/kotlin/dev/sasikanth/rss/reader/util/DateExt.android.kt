/*
 * Copyright 2025 Sasikanth Miriyampalli
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

import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toJavaLocalDateTime

actual fun Instant.readerDateTimestamp(): String {
  val dateTime = JavaLocalDateTime.ofInstant(toJavaInstant(), TimeZone.getDefault().toZoneId())
  val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
  val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

  val formattedDate = dateFormatter.format(dateTime)
  val formattedTime = timeFormatter.format(dateTime)

  return "$formattedDate • $formattedTime"
}

actual fun LocalDateTime.homeAppBarTimestamp(): String {
  val locale = Locale.getDefault()

  // Java DateTimeFormatter does not provides a localized FormatStyle corresponding to the
  // 'EEE, MMM d' pattern, so we define a custom pattern for each locale.
  val pattern =
    when (locale.language) {
      "de" -> "EEE, d. MMM"
      "fr" -> "EEE d MMM"
      else -> "EEE, MMM d" // fallback to English pattern
    }

  val formatter = DateTimeFormatter.ofPattern(pattern, locale)
  return formatter.format(toJavaLocalDateTime())
}
