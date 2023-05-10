package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration

actual class DriverFactory {

  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(
      DatabaseConfiguration(
        name = DB_NAME,
        version = ReaderDatabase.Schema.version,
        create = { connection ->
          wrapConnection(connection) { ReaderDatabase.Schema.create(it) }
        },
        upgrade = { connection, oldVersion, newVersion ->
          wrapConnection(connection) { ReaderDatabase.Schema.migrate(it, oldVersion, newVersion) }
        },
        extendedConfig = DatabaseConfiguration.Extended(
          foreignKeyConstraints = true
        )
      )
    )
  }
}
