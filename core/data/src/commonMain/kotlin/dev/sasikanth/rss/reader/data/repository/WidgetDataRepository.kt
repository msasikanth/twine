/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.repository

import app.cash.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.WidgetPost
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
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
    return postQueries
      .allPosts(
        isSourceIdsEmpty = true,
        sourceIds = emptyList(),
        unreadOnly = true,
        postsAfter = Instant.DISTANT_PAST,
        numberOfFeaturedPosts = 0,
        lastSyncedAt = Instant.DISTANT_FUTURE,
        limit = numberOfPosts.toLong(),
        offset = 0,
        mapper = {
          id,
          sourceId,
          title,
          description,
          imageUrl,
          date,
          link,
          commentsLink,
          flags,
          feedName,
          feedIcon,
          feedHomepageLink,
          alwaysFetchSourceArticle,
          _ ->
          WidgetPost(
            id = id,
            title = title,
            description = description,
            image = imageUrl,
            postedOn = date,
            feedName = feedName,
            feedIcon = feedIcon,
          )
        }
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
            id,
            sourceId,
            title,
            description,
            imageUrl,
            date,
            link,
            commentsLink,
            flags,
            feedName,
            feedIcon,
            feedHomepageLink,
            alwaysFetchSourceArticle ->
            WidgetPost(
              id = id,
              title = title,
              description = description,
              image = imageUrl,
              postedOn = date,
              feedName = feedName,
              feedIcon = feedIcon,
            )
          }
        )
        .executeAsList()
    }
  }

  fun unreadPostsPager(): PagingSource<Int, PostWithMetadata> {
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
            date,
            link,
            commentsLink,
            flags,
            feedName,
            feedIcon,
            feedHomepageLink,
            alwaysFetchSourceArticle ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
              date = date,
              link = link,
              commentsLink = commentsLink,
              flags = flags,
              feedName = feedName,
              feedIcon = feedIcon,
              feedHomepageLink = feedHomepageLink,
              alwaysFetchFullArticle = alwaysFetchSourceArticle,
            )
          }
        )
      }
    )
  }
}
