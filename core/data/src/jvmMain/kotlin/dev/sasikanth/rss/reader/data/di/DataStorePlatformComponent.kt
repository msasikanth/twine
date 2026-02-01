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

package dev.sasikanth.rss.reader.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import java.io.File
import me.tatarka.inject.annotations.Provides
import okio.Path.Companion.toPath

actual interface DataStorePlatformComponent {

  @Provides
  @AppScope
  fun providesDataStore(): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
      produceFile = {
        val dataStoreFile =
          File(System.getProperty("user.home"), ".twine/${Constants.DATA_STORE_FILE_NAME}")
        dataStoreFile.parentFile.mkdirs()
        dataStoreFile.absolutePath.toPath()
      }
    )
  }
}
