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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.PostContent
import dev.sasikanth.rss.reader.data.database.PostContentQueries
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class PostContentRepository(
  private val postContentQueries: PostContentQueries,
  private val dispatcherProvider: DispatchersProvider,
  private val readingTimeCalculator: ReadingTimeCalculator,
) {

  fun postContent(postId: String): Flow<PostContent?> {
    return postContentQueries
      .getByPostId(postId) {
        id,
        feedContent,
        articleContent,
        _,
        _,
        feedContentReadingTime,
        articleContentReadingTime ->
        PostContent(
          id = id,
          feedContent = feedContent,
          articleContent = articleContent,
          feedContentReadingTime = feedContentReadingTime?.toInt(),
          articleContentReadingTime = articleContentReadingTime?.toInt(),
        )
      }
      .asFlow()
      .mapToOneOrNull(dispatcherProvider.databaseRead)
      .catch { error ->
        Logger.e("PostContentError", error) { "Failed to load post content for $postId" }
        emit(null)
      }
  }

  suspend fun updateFullArticleContent(postId: String, articleContent: String?) {
    val readingTime = readingTimeCalculator.calculate(articleContent)
    withContext(dispatcherProvider.databaseWrite) {
      postContentQueries.updateArticleContent(
        articleContent = articleContent,
        articleContentReadingTime = readingTime.toLong(),
        id = postId,
      )
    }
  }

  suspend fun upsert(
    postId: String,
    feedContent: String?,
    articleContent: String?,
    createdAt: Instant,
  ) {
    val feedReadingTime = readingTimeCalculator.calculate(feedContent)
    val articleReadingTime =
      if (articleContent != null) {
        readingTimeCalculator.calculate(articleContent)
      } else {
        null
      }

    withContext(dispatcherProvider.databaseWrite) {
      postContentQueries.upsert(
        id = postId,
        feedContent = feedContent,
        feedContentLen = feedContent?.length?.toLong() ?: 0L,
        articleContent = articleContent,
        createdAt = createdAt,
        feedContentReadingTime = feedReadingTime.toLong(),
        articleContentReadingTime = articleReadingTime?.toLong(),
      )
    }
  }
}
