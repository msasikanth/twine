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

import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

actual fun String.formatReadingTrendDate(): String {
  val formatter = NSDateFormatter()
  formatter.locale = NSLocale.currentLocale()
  formatter.dateFormat = "yyyy-MM-dd"
  val parsedDate = formatter.dateFromString(this) ?: return this
  formatter.dateFormat = "dd-MMM-yy"
  return formatter.stringFromDate(parsedDate)
}
