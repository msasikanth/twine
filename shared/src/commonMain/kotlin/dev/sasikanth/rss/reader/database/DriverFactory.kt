package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.db.SqlDriver

internal const val DB_NAME = "rss_reader.db"

expect class DriverFactory {
  fun createDriver(): SqlDriver
}
