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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
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

  val unreadPosts: Flow<List<WidgetPost>>
    get() =
      postQueries
        .allPosts(
          isSourceIdsEmpty = true,
          sourceIds = emptyList(),
          unreadOnly = true,
          postsAfter = Instant.DISTANT_PAST,
          numberOfFeaturedPosts = 0,
          lastSyncedAt = Instant.DISTANT_FUTURE,
          limit = 15,
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
