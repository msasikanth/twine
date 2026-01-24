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
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import java.time.Duration
import kotlinx.coroutines.CancellationException

class DropboxSyncWorker(
  context: Context,
  workerParameters: WorkerParameters,
  private val syncCoordinator: SyncCoordinator,
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
      syncCoordinator.push()
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
