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
package dev.sasikanth.rss.reader

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.create
import dev.sasikanth.rss.reader.utils.initialiseLogging

class ReaderApplication : Application(), Configuration.Provider {

  val appComponent by
    lazy(LazyThreadSafetyMode.NONE) { ApplicationComponent::class.create(context = this) }

  private val workManager by lazy(LazyThreadSafetyMode.NONE) { WorkManager.getInstance(this) }

  override fun onCreate() {
    super.onCreate()
    initialiseLogging()
    enqueuePeriodicFeedsRefresh()

    appComponent.initializers.forEach { it.initialize() }
  }

  override fun getWorkManagerConfiguration(): Configuration {
    val loggingLevel =
      if (BuildConfig.DEBUG) {
        Log.DEBUG
      } else {
        Log.ERROR
      }
    return Configuration.Builder()
      .setMinimumLoggingLevel(loggingLevel)
      .setWorkerFactory(
        object : WorkerFactory() {
          override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
          ): ListenableWorker {
            return FeedsRefreshWorker(
              context = appContext,
              workerParameters = workerParameters,
              rssRepository = appComponent.rssRepository,
              lastUpdatedAt = appComponent.lastUpdatedAt
            )
          }
        }
      )
      .build()
  }

  private fun enqueuePeriodicFeedsRefresh() {
    workManager.enqueueUniquePeriodicWork(
      FeedsRefreshWorker.UNIQUE_WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      FeedsRefreshWorker.periodicRequest()
    )
  }
}
