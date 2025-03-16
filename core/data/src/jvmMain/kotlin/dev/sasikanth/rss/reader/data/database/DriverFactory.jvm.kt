/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    val appConfigPath = File(System.getProperty("user.home"), ".twine")
    if (!appConfigPath.exists()) {
      appConfigPath.mkdirs()
    }

    val driver: SqlDriver =
      JdbcSqliteDriver(
        url = "jdbc:sqlite:${appConfigPath.resolve(DB_NAME).path}",
        callbacks = codeMigrations,
        schema = ReaderDatabase.Schema
      )

    return driver
  }
}
