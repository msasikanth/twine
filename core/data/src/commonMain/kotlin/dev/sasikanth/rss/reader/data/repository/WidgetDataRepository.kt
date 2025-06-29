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
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class WidgetDataRepository(
  private val postQueries: PostQueries,
  private val dispatcherProvider: DispatchersProvider,
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
        .mapToOne(dispatcherProvider.databaseRead)

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
          rawContent,
          imageUrl,
          date,
          link,
          commentsLink,
          bookmarked,
          read,
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
      .mapToList(dispatcherProvider.databaseRead)
  }

  fun unreadPostsPager(): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery =
        postQueries.allPostsCount(
          isSourceIdsEmpty = true,
          sourceIds = emptyList(),
          unreadOnly = true,
          postsAfter = Instant.DISTANT_PAST,
          lastSyncedAt = Instant.DISTANT_FUTURE,
        ),
      transacter = postQueries,
      context = dispatcherProvider.databaseRead,
      queryProvider = { limit, offset ->
        postQueries.allPosts(
          isSourceIdsEmpty = true,
          sourceIds = emptyList(),
          unreadOnly = true,
          postsAfter = Instant.DISTANT_PAST,
          numberOfFeaturedPosts = 0,
          lastSyncedAt = Instant.DISTANT_FUTURE,
          limit = limit,
          offset = offset,
          mapper = {
            id,
            sourceId,
            title,
            description,
            rawContent,
            imageUrl,
            date,
            link,
            commentsLink,
            bookmarked,
            read,
            feedName,
            feedIcon,
            feedHomepageLink,
            alwaysFetchSourceArticle,
            _ ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              rawContent = rawContent,
              imageUrl = imageUrl,
              date = date,
              link = link,
              commentsLink = commentsLink,
              bookmarked = bookmarked,
              read = read,
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
