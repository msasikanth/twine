package dev.sasikanth.rss.reader.utils

import android.text.format.DateUtils
import kotlinx.datetime.Instant

actual fun Instant.relativeDurationString(): String {
  return DateUtils.getRelativeTimeSpanString(toEpochMilliseconds()).toString()
}
