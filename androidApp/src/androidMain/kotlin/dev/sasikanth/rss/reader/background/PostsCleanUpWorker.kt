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

package dev.sasikanth.rss.reader.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.utils.calculateInstantBeforePeriod
import io.sentry.Sentry
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.first

class PostsCleanUpWorker(
  context: Context,
  workerParameters: WorkerParameters,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository
) : CoroutineWorker(context, workerParameters) {

  companion object {

    const val TAG = "POSTS_CLEAN_UP"

    fun periodicRequest(): PeriodicWorkRequest {
      val constraints =
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .setRequiresBatteryNotLow(true)
          .build()

      return PeriodicWorkRequestBuilder<PostsCleanUpWorker>(repeatInterval = Duration.ofDays(1))
        .setConstraints(constraints)
        .build()
    }
  }

  override suspend fun doWork(): Result {
    try {
      val postsDeletionPeriod = settingsRepository.postsDeletionPeriod.first()
      rssRepository.deletePosts(before = postsDeletionPeriod.calculateInstantBeforePeriod())
      return Result.success()
    } catch (e: CancellationException) {
      // no-op
    } catch (e: Exception) {
      Sentry.captureException(e)
    }

    return Result.failure()
  }
}
