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
package dev.sasikanth.rss.reader.data.repository

import dev.sasikanth.rss.reader.core.model.local.FeedReadCount
import dev.sasikanth.rss.reader.core.model.local.ReadingStatistics
import dev.sasikanth.rss.reader.core.model.local.ReadingTrend
import dev.sasikanth.rss.reader.data.database.ReadingHistoryQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class ReadingHistoryRepository(
  private val readingHistoryQueries: ReadingHistoryQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun deleteAll() {
    withContext(dispatchersProvider.databaseWrite) { readingHistoryQueries.deleteAll() }
  }

  suspend fun getReadingStatistics(startDate: Instant): Flow<ReadingStatistics> {
    return withContext(dispatchersProvider.databaseRead) {
      val totalReadCount = readingHistoryQueries.totalReadPostsCount().executeAsOne()
      val firstReadingDate =
        readingHistoryQueries.firstReadingDate().executeAsOneOrNull()?.firstReadingDate
      val dailyAverage =
        if (firstReadingDate != null) {
          val today =
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
          val firstDay =
            firstReadingDate.toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays()
          val daysSinceFirstReading = (today - firstDay) + 1
          (totalReadCount / daysSinceFirstReading).toInt()
        } else {
          0
        }

      val topFeeds =
        readingHistoryQueries.readPostsByFeed().executeAsList().map {
          FeedReadCount(
            feedId = it.feedId,
            feedName = it.feedName,
            feedIcon = it.feedIcon,
            homepageLink = it.feedHomepageLink,
            showFeedFavIcon = it.showFeedFavIcon,
            readCount = it.readCount,
          )
        }

      val readingTrends =
        readingHistoryQueries.readPostsOverTime(startDate).executeAsList().map {
          ReadingTrend(date = it.date, count = it.count)
        }

      flowOf(
        ReadingStatistics(
          totalReadCount = totalReadCount,
          dailyAverage = dailyAverage,
          topFeeds = topFeeds.toImmutableList(),
          readingTrends = readingTrends.toImmutableList(),
        )
      )
    }
  }
}
