package dev.sasikanth.rss.reader.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate
import platform.Foundation.NSRelativeDateTimeFormatter
import platform.Foundation.NSRelativeDateTimeFormatterUnitsStyleFull
import platform.Foundation.now

actual fun Instant.relativeDurationString(): String {
  val formatter = NSRelativeDateTimeFormatter()
  formatter.unitsStyle = NSRelativeDateTimeFormatterUnitsStyleFull

  return formatter.localizedStringForDate(
    date = toNSDate(), relativeToDate = NSDate.now
  )
}
