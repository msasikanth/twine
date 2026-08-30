/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.sasikanth.rss.reader.di.scopes.AppScope
import java.io.File
import java.util.Properties
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class DriverFactory(private val codeMigrations: Array<AfterVersion>) {

  actual fun createDriver(): SqlDriver {
    useBundledSqliteNativeLibrary()

    val databasePath = File(System.getProperty("user.home"), ".twine/${DB_NAME}")
    databasePath.parentFile.mkdirs()

    if (databasePath.exists() && readUserVersion(databasePath) == 0L) {
      val backupPath = File(databasePath.parentFile, "$DB_NAME.bak")
      backupPath.delete()
      databasePath.renameTo(backupPath)
    }

    val driver =
      JdbcSqliteDriver(
        url = "jdbc:sqlite:${databasePath.absolutePath}",
        properties = Properties(),
        schema = ReaderDatabase.Schema,
        callbacks = *codeMigrations,
      )

    driver.execute(null, "PRAGMA foreign_keys = ON;", 0)

    return driver
  }

  // Without this sqlite-jdbc unpacks its native library into a temp directory and loads it
  // from there, which macOS refuses for a sandboxed App Store build. The packaged app ships a
  // signed copy in its resources; outside a package the property is absent and the default
  // extraction path is used.
  private fun useBundledSqliteNativeLibrary() {
    val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return
    val library = File(resourcesDir, SQLITE_LIBRARY_NAME)
    if (!library.exists()) return

    System.setProperty("org.sqlite.lib.path", resourcesDir)
    System.setProperty("org.sqlite.lib.name", SQLITE_LIBRARY_NAME)
  }

  private companion object {
    const val SQLITE_LIBRARY_NAME = "libsqlitejdbc.dylib"
  }

  private fun readUserVersion(databasePath: File): Long {
    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")
    driver.use { driver ->
      return driver
        .executeQuery(
          identifier = null,
          sql = "PRAGMA user_version",
          mapper = { cursor ->
            QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else null)
          },
          parameters = 0,
          binders = null,
        )
        .value ?: 0L
    }
  }
}
