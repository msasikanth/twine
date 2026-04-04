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

import androidx.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.ReadingHistoryQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.collections.Set
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class WidgetDataRepository(
  private val postQueries: PostQueries,
  private val readingHistoryQueries: ReadingHistoryQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun unreadPostsCountBlocking(): Long {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .unreadPostsCountInSource(
          isSourceIdsEmpty = true,
          sourceIds = emptyList(),
          after = Instant.DISTANT_PAST,
        )
        .executeAsOne()
    }
  }

  fun unreadPosts(numberOfPosts: Int): Flow<List<WidgetPost>> {
    return postQueries
      .widgetUnreadPosts(
        numberOfPosts = numberOfPosts.toLong(),
        offset = 0,
        mapper = {
          id: String,
          _: String,
          title: String,
          description: String,
          imageUrl: String?,
          _: String?,
          date: Instant,
          _: Instant,
          _: String,
          _: String?,
          _: Set<PostFlag>,
          _: String?,
          feedName: String,
          feedIcon: String,
          _: String,
          _: Boolean,
          _: Boolean,
          feedContentReadingTime: Long?,
          _: Long?,
          _: Long? ->
          WidgetPost(
            id = id,
            title = title,
            description = description,
            image = imageUrl,
            postedOn = date,
            feedName = feedName,
            feedIcon = feedIcon,
            readingTimeEstimate = feedContentReadingTime?.toInt() ?: 0,
          )
        },
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  suspend fun unreadPostsBlocking(numberOfPosts: Int): List<WidgetPost> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .widgetUnreadPosts(
          numberOfPosts = numberOfPosts.toLong(),
          offset = 0,
          mapper = {
            id: String,
            _: String,
            title: String,
            description: String,
            imageUrl: String?,
            _: String?,
            date: Instant,
            _: Instant,
            _: String,
            _: String?,
            _: Set<PostFlag>,
            _: String?,
            feedName: String,
            feedIcon: String,
            _: String,
            _: Boolean,
            _: Boolean,
            feedContentReadingTime: Long?,
            _: Long?,
            _: Long? ->
            WidgetPost(
              id = id,
              title = title,
              description = description,
              image = imageUrl,
              postedOn = date,
              feedName = feedName,
              feedIcon = feedIcon,
              readingTimeEstimate = feedContentReadingTime?.toInt() ?: 0,
            )
          },
        )
        .executeAsList()
    }
  }

  fun unreadPostsPager(): PagingSource<Int, ResolvedPost> {
    return QueryPagingSource(
      countQuery = postQueries.widgetUnreadPostsCount(),
      transacter = postQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        postQueries.widgetUnreadPosts(
          numberOfPosts = limit,
          offset = offset,
          mapper = {
            id,
            sourceId,
            title,
            description,
            imageUrl,
            audioUrl,
            date,
            createdAt,
            link,
            commentsLink,
            flags,
            remoteId,
            feedName,
            feedIcon,
            feedHomepageLink,
            alwaysFetchSourceArticle,
            showFeedFavIcon: Boolean,
            feedContentReadingTime: Long?,
            articleContentReadingTime: Long?,
            seedColor: Long? ->
            ResolvedPost(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
              audioUrl = audioUrl,
              date = date,
              createdAt = createdAt,
              link = link,
              commentsLink = commentsLink,
              flags = flags,
              feedName = feedName,
              feedIcon = feedIcon,
              feedHomepageLink = feedHomepageLink,
              alwaysFetchFullArticle = alwaysFetchSourceArticle,
              showFeedFavIcon = showFeedFavIcon,
              feedContentReadingTime = feedContentReadingTime?.toInt(),
              articleContentReadingTime = articleContentReadingTime?.toInt(),
              seedColor = seedColor?.toInt(),
              remoteId = remoteId,
            )
          },
        )
      },
    )
  }
}
