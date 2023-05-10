package dev.sasikanth.rss.reader.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DriverFactory(private val context: Context) {

  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
      schema = ReaderDatabase.Schema,
      context = context,
      name = DB_NAME,
      callback = object : AndroidSqliteDriver.Callback(ReaderDatabase.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
          db.setForeignKeyConstraintsEnabled(true)
        }
      }
    )
  }
}
