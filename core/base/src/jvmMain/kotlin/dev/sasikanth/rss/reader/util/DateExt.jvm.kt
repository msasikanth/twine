/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.util

import java.time.LocalDateTime as JavaLocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDateTime

actual fun Instant.relativeDurationString(): String {
  val now = Clock.System.now()
  val duration = now - this
  return when {
    duration.inWholeSeconds < 60 -> "now"
    duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}m"
    duration.inWholeHours < 24 -> "${duration.inWholeHours}h"
    duration.inWholeDays < 30 -> "${duration.inWholeDays}d"
    else -> readerDateTimestamp()
  }
}

actual fun Instant.readerDateTimestamp(): String {
  val dateTime = JavaLocalDateTime.ofInstant(toJavaInstant(), TimeZone.getDefault().toZoneId())
  val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
  val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

  val formattedDate = dateFormatter.format(dateTime)
  val formattedTime = timeFormatter.format(dateTime)

  return "$formattedDate • $formattedTime"
}

actual fun LocalDateTime.homeAppBarTimestamp(): String {
  val locale = Locale.getDefault()
  val pattern =
    when (locale.language) {
      "de" -> "EEE, d. MMM"
      "fr" -> "EEE d MMM"
      else -> "EEE, MMM d"
    }

  val formatter = DateTimeFormatter.ofPattern(pattern, locale)
  return formatter.format(toJavaLocalDateTime())
}
