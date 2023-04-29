package dev.sasikanth.rss.reader.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {

  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
      ReaderDatabase.Schema,
      context,
      DB_NAME
    )
  }
}
