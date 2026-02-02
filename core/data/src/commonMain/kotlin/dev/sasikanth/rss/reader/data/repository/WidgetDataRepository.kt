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

import app.cash.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.collections.Set
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class WidgetDataRepository(
  private val postQueries: PostQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  val unreadPostsCount: Flow<Long>
    get() =
      postQueries
        .unreadPostsCountInSource(
          isSourceIdsEmpty = true,
          sourceIds = emptyList(),
          after = Instant.DISTANT_PAST,
        )
        .asFlow()
        .mapToOne(dispatchersProvider.databaseRead)

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
    val featuredPostsAfter = Clock.System.now().minus(24.hours)

    return postQueries
      .allPosts(
        isSourceIdsEmpty = true,
        sourceIds = emptyList(),
        unreadOnly = true,
        postsAfter = Instant.DISTANT_PAST,
        featuredPostsAfter = featuredPostsAfter,
        numberOfFeaturedPosts = 0,
        lastSyncedAt = Instant.DISTANT_FUTURE,
        limit = numberOfPosts.toLong(),
        offset = 0,
        orderBy = PostsSortOrder.Latest.name,
        mapper = {
          id: String,
          sourceId: String,
          title: String,
          description: String,
          imageUrl: String?,
          audioUrl: String?,
          date: Instant,
          createdAt: Instant,
          link: String,
          commentsLink: String?,
          flags: Set<PostFlag>,
          remoteId: String?,
          feedName: String,
          feedIcon: String,
          feedHomepageLink: String,
          alwaysFetchSourceArticle: Boolean,
          showFeedFavIcon: Boolean,
          feedContentReadingTime: Long?,
          articleContentReadingTime: Long?,
          _: Long ->
          WidgetPost(
            id = id,
            title = title,
            description = description,
            image = imageUrl,
            postedOn = date,
            feedName = feedName,
            feedIcon = feedIcon,
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
            sourceId: String,
            title: String,
            description: String,
            imageUrl: String?,
            audioUrl: String?,
            date: Instant,
            createdAt: Instant,
            link: String,
            commentsLink: String?,
            flags: Set<PostFlag>,
            remoteId: String?,
            feedName: String,
            feedIcon: String,
            feedHomepageLink: String,
            alwaysFetchSourceArticle: Boolean,
            showFeedFavIcon: Boolean,
            feedContentReadingTime: Long?,
            articleContentReadingTime: Long? ->
            WidgetPost(
              id = id,
              title = title,
              description = description,
              image = imageUrl,
              postedOn = date,
              feedName = feedName,
              feedIcon = feedIcon,
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
            articleContentReadingTime: Long? ->
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
              remoteId = remoteId,
            )
          },
        )
      },
    )
  }
}
