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

package dev.sasikanth.rss.reader.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.data.database.DriverFactory
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import java.io.File
import me.tatarka.inject.annotations.Provides
import okio.Path.Companion.toOkioPath

actual interface SqlDriverPlatformComponent {

  @Provides
  @AppScope
  fun providesJvmSqlDriver(driverFactory: DriverFactory): SqlDriver {
    return driverFactory.createDriver()
  }
}

actual interface DataStorePlatformComponent {

  @Provides
  @AppScope
  fun providesDataStore(): DataStore<Preferences> {
    val appConfigPath = File(System.getProperty("user.home"), ".twine")
    if (!appConfigPath.exists()) {
      appConfigPath.mkdirs()
    }

    return PreferenceDataStoreFactory.createWithPath(
      produceFile = { appConfigPath.toOkioPath().resolve(Constants.DATA_STORE_FILE_NAME) }
    )
  }
}
