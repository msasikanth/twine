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

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import dev.sasikanth.rss.reader.repository.RssRepository
import java.lang.Exception
import java.time.Duration

class FeedsRefreshWorker(
  context: Context,
  workerParameters: WorkerParameters,
  private val rssRepository: RssRepository
) : CoroutineWorker(context, workerParameters) {

  companion object {

    const val UNIQUE_WORK_NAME = "REFRESH_FEEDS"

    fun periodicRequest(): PeriodicWorkRequest {
      return PeriodicWorkRequestBuilder<FeedsRefreshWorker>(repeatInterval = Duration.ofHours(1))
        .build()
    }
  }

  override suspend fun doWork(): Result {
    return try {
      rssRepository.updateFeeds()
      Result.success()
    } catch (e: Exception) {
      Result.failure()
    }
  }
}
