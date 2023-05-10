package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.ColumnAdapter
import kotlinx.datetime.Instant

object DateAdapter : ColumnAdapter<Instant, Long> {

  override fun decode(databaseValue: Long): Instant {
    return Instant.fromEpochMilliseconds(databaseValue)
  }

  override fun encode(value: Instant): Long {
    return value.toEpochMilliseconds()
  }
}
