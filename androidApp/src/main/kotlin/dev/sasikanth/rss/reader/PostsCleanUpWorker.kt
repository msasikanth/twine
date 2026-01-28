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

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.bugsnag.android.Bugsnag
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.utils.calculateInstantBeforePeriod
import java.time.Duration
import kotlin.coroutines.cancellation.CancellationException

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
      val postsDeletionPeriod = settingsRepository.postsDeletionPeriodImmediate()
      val feedsDeletedFrom =
        rssRepository.deleteReadPosts(before = postsDeletionPeriod.calculateInstantBeforePeriod())

      if (feedsDeletedFrom.isNotEmpty()) {
        rssRepository.updateFeedsLastCleanUpAt(feedsDeletedFrom)
      }
      return Result.success()
    } catch (e: CancellationException) {
      // no-op
    } catch (e: Exception) {
      Bugsnag.leaveBreadcrumb("Background Worker")
      BugsnagKotlin.sendFatalException(e)
    }

    return Result.failure()
  }
}
