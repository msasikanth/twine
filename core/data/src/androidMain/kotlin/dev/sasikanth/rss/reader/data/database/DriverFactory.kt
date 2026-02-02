/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package dev.sasikanth.rss.reader.data.database

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.sasikanth.rss.reader.di.scopes.AppScope
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class DriverFactory(
  private val context: Context,
  private val codeMigrations: Array<AfterVersion>,
) {

  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(
      schema = ReaderDatabase.Schema,
      context = context,
      name = DB_NAME,
      callback =
        object : AndroidSqliteDriver.Callback(ReaderDatabase.Schema, callbacks = codeMigrations) {
          override fun onConfigure(db: SupportSQLiteDatabase) {
            super.onConfigure(db)
            db.enableWriteAheadLogging()
          }

          override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
          }

          override fun onOpen(db: SupportSQLiteDatabase) {
            db.setForeignKeyConstraintsEnabled(true)
          }
        },
      factory = RequerySQLiteOpenHelperFactory(),
    )
  }
}
