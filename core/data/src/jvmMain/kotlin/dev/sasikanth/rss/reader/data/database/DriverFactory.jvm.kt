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
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.sasikanth.rss.reader.di.scopes.AppScope
import java.io.File
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class DriverFactory(private val codeMigrations: Array<AfterVersion>) {

  actual fun createDriver(): SqlDriver {
    val databasePath = File(System.getProperty("user.home"), ".twine/${DB_NAME}")
    val isNewDatabase = !databasePath.exists()
    databasePath.parentFile.mkdirs()

    val driver = JdbcSqliteDriver(url = "jdbc:sqlite:${databasePath.absolutePath}")

    if (isNewDatabase) {
      ReaderDatabase.Schema.create(driver)
    }

    driver.execute(null, "PRAGMA foreign_keys = ON;", 0)

    return driver
  }
}
