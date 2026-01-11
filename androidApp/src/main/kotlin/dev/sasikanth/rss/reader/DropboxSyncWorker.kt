/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 */
package dev.sasikanth.rss.reader

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.bugsnag.android.Bugsnag
import dev.sasikanth.rss.reader.data.sync.CloudSyncService
import dev.sasikanth.rss.reader.data.sync.DropboxSyncProvider
import java.time.Duration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first

class DropboxSyncWorker(
  context: Context,
  workerParameters: WorkerParameters,
  private val cloudSyncService: CloudSyncService,
  private val dropboxSyncProvider: DropboxSyncProvider,
) : CoroutineWorker(context, workerParameters) {

  companion object {

    const val TAG = "DROPBOX_SYNC"

    fun periodicRequest(): PeriodicWorkRequest {
      val constraints =
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .setRequiresBatteryNotLow(true)
          .build()

      return PeriodicWorkRequestBuilder<DropboxSyncWorker>(repeatInterval = Duration.ofMinutes(15))
        .setConstraints(constraints)
        .build()
    }
  }

  override suspend fun doWork(): Result {
    return try {
      if (dropboxSyncProvider.isSignedIn().first()) {
        cloudSyncService.sync(dropboxSyncProvider)
      }
      Result.success()
    } catch (e: CancellationException) {
      Result.failure()
    } catch (e: Exception) {
      Bugsnag.leaveBreadcrumb("Background Worker")
      BugsnagKotlin.sendFatalException(e)
      Result.failure()
    }
  }
}
