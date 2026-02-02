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
package dev.sasikanth.rss.reader

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import co.touchlab.crashkios.bugsnag.enableBugsnag
import com.bugsnag.android.Bugsnag
import dev.sasikanth.rss.reader.di.ApplicationComponent
import dev.sasikanth.rss.reader.di.create
import dev.sasikanth.rss.reader.media.AudioCacheProvider

class ReaderApplication : Application(), Configuration.Provider {

  val appComponent by
    lazy(LazyThreadSafetyMode.NONE) { ApplicationComponent::class.create(context = this) }

  private val workManager by lazy(LazyThreadSafetyMode.NONE) { WorkManager.getInstance(this) }

  override val workManagerConfiguration: Configuration
    get() =
      Configuration.Builder()
        .setMinimumLoggingLevel(
          if (BuildConfig.DEBUG) {
            Log.DEBUG
          } else {
            Log.ERROR
          }
        )
        .setWorkerFactory(
          object : WorkerFactory() {
            override fun createWorker(
              appContext: Context,
              workerClassName: String,
              workerParameters: WorkerParameters,
            ): ListenableWorker? {
              return when (workerClassName) {
                FeedsRefreshWorker::class.qualifiedName -> {
                  FeedsRefreshWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    syncCoordinator = appComponent.syncCoordinator,
                    refreshPolicy = appComponent.refreshPolicy,
                    settingsRepository = appComponent.settingsRepository,
                    newArticleNotifier = appComponent.newArticleNotifier,
                  )
                }
                CloudSyncWorker::class.qualifiedName -> {
                  CloudSyncWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    syncCoordinator = appComponent.syncCoordinator,
                  )
                }
                PostsCleanUpWorker::class.qualifiedName -> {
                  PostsCleanUpWorker(
                    context = appContext,
                    workerParameters = workerParameters,
                    rssRepository = appComponent.rssRepository,
                    settingsRepository = appComponent.settingsRepository,
                  )
                }
                else -> null
              }
            }
          }
        )
        .build()

  @OptIn(UnstableApi::class)
  override fun onCreate() {
    super.onCreate()

    if (!BuildConfig.DEBUG) {
      Bugsnag.start(this)
      enableBugsnag()
    }

    enqueuePeriodicFeedsRefresh()
    enqueuePeriodicPostsCleanUp()
    enqueueCloudSyncWorker()

    AudioCacheProvider.cache = appComponent.audioCache.cache

    appComponent.initializers.forEach { it.initialize() }
  }

  private fun enqueueCloudSyncWorker() {
    workManager.enqueueUniquePeriodicWork(
      CloudSyncWorker.TAG,
      ExistingPeriodicWorkPolicy.KEEP,
      CloudSyncWorker.periodicRequest(),
    )
  }

  private fun enqueuePeriodicPostsCleanUp() {
    workManager.enqueueUniquePeriodicWork(
      PostsCleanUpWorker.TAG,
      ExistingPeriodicWorkPolicy.KEEP,
      PostsCleanUpWorker.periodicRequest(),
    )
  }

  private fun enqueuePeriodicFeedsRefresh() {
    workManager.enqueueUniquePeriodicWork(
      FeedsRefreshWorker.TAG,
      ExistingPeriodicWorkPolicy.KEEP,
      FeedsRefreshWorker.periodicRequest(),
    )
  }
}
