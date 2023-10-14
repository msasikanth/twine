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
package dev.sasikanth.rss.reader.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.utils.Constants
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Provides
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual interface DataStorePlatformComponent {

  @Provides
  @AppScope
  @OptIn(ExperimentalForeignApi::class)
  fun providesDataStore(dispatchersProvider: DispatchersProvider): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
      produceFile = {
        val documentDirectory: NSURL? =
          NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
          )

        (requireNotNull(documentDirectory).path + "/${Constants.DATA_STORE_FILE_NAME}").toPath()
      },
      corruptionHandler = null,
      migrations = emptyList(),
      scope = CoroutineScope(dispatchersProvider.io + SupervisorJob())
    )
  }
}
