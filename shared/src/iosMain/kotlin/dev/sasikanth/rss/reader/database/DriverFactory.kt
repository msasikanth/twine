/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.database

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import app.cash.sqldelight.driver.native.wrapConnection
import co.touchlab.sqliter.DatabaseConfiguration
import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class DriverFactory(private val codeMigrations: Array<AfterVersion>) {

  actual fun createDriver(): SqlDriver {
    return NativeSqliteDriver(
      DatabaseConfiguration(
        name = DB_NAME,
        version = ReaderDatabase.Schema.version.toInt(),
        create = { connection -> wrapConnection(connection) { ReaderDatabase.Schema.create(it) } },
        upgrade = { connection, oldVersion, newVersion ->
          wrapConnection(connection) {
            ReaderDatabase.Schema.migrate(
              driver = it,
              oldVersion = oldVersion.toLong(),
              newVersion = newVersion.toLong(),
              callbacks = codeMigrations
            )
          }
        },
        extendedConfig = DatabaseConfiguration.Extended(foreignKeyConstraints = true),
      )
    )
  }
}
