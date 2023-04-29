package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DriverFactory {

  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(
      ReaderDatabase.Schema,
      DB_NAME
    )
  }
}
