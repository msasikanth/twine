/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
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
