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

import kotlinx.datetime.Instant
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

actual fun Instant.readerDateTimestamp(): String {
  val currentLocale = NSLocale.currentLocale()
  val dateFormatter =
    NSDateFormatter().apply {
      locale = currentLocale
      dateStyle = NSDateFormatterMediumStyle
    }
  val timeFormatter =
    NSDateFormatter().apply {
      locale = currentLocale
      timeStyle = NSDateFormatterShortStyle
    }

  val formattedDate = dateFormatter.stringFromDate(toNSDate())
  val formattedTime = timeFormatter.stringFromDate(toNSDate())

  return "$formattedDate • $formattedTime"
}

actual fun LocalDateTime.homeAppBarTimestamp(): String {
  val currentLocale = NSLocale.currentLocale()
  val datePattern =
    when (currentLocale.languageCode()) {
      "de" -> "EEE, d. MMM"
      "fr" -> "EEE d MMM"
      else -> "EEE, MMM d"
    }
  val formatter =
    NSDateFormatter().apply {
      locale = currentLocale
      dateFormat = datePattern
    }

  val date = this.toInstant(TimeZone.currentSystemDefault()).toNSDate()
  return formatter.stringFromDate(date)
}
