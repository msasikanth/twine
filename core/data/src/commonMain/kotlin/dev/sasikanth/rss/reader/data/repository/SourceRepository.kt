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
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.database.SourceQueries
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.splitAndTrim
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class SourceRepository(
  private val sourceQueries: SourceQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  fun pinnedSources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<List<Source>> {
    return sourceQueries
      .pinnedSources(
        postsAfter = postsAfter,
        postsUpperBound = postsUpperBound,
        mapper = ::mapToSource,
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  fun sources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Source> {
    return QueryPagingSource(
      countQuery = sourceQueries.sourcesCount(),
      transacter = sourceQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        sourceQueries.sources(
          postsAfter = postsAfter,
          postsUpperBound = postsUpperBound,
          orderBy = orderBy.value,
          limit = limit,
          offset = offset,
          mapper = ::mapToSource,
        )
      },
    )
  }

  fun source(
    id: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Source?> {
    return sourceQueries
      .source(
        postsAfter = postsAfter,
        postsUpperBound = postsUpperBound,
        id = id,
        mapper = ::mapToSource,
      )
      .asFlow()
      .mapToOneOrNull(dispatchersProvider.databaseRead)
  }

  private fun mapToFeedShowFavIconSettings(feedShowFavIconSettings: String?): List<Boolean> {
    return feedShowFavIconSettings?.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR)?.map {
      when (it) {
        "1" -> true
        else -> false
      }
    } ?: emptyList()
  }

  private fun mapToSource(
    type: String,
    id: String,
    name: String,
    icon: String?,
    description: String?,
    link: String?,
    homepageLink: String?,
    createdAt: Instant,
    pinnedAt: Instant?,
    lastCleanUpAt: Instant?,
    numberOfUnreadPosts: Long,
    feedIds: String?,
    feedHomepageLinks: String?,
    feedIcons: String?,
    feedShowFavIconSettings: String?,
    updatedAt: Instant?,
    pinnedPosition: Double,
    showFeedFavIcon: Boolean?,
    remoteId: String?,
  ): Source {
    return if (type == "group") {
      FeedGroup(
        id = id,
        name = name,
        feedIds = feedIds.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
        feedHomepageLinks =
          feedHomepageLinks.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
        feedIconLinks = feedIcons.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
        feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
        createdAt = createdAt,
        updatedAt = updatedAt!!,
        pinnedAt = pinnedAt,
        numberOfUnreadPosts = numberOfUnreadPosts,
        pinnedPosition = pinnedPosition,
      )
    } else {
      Feed(
        id = id,
        name = name,
        icon = icon!!,
        description = description!!,
        link = link!!,
        homepageLink = homepageLink!!,
        createdAt = createdAt,
        pinnedAt = pinnedAt,
        lastCleanUpAt = lastCleanUpAt,
        numberOfUnreadPosts = numberOfUnreadPosts,
        pinnedPosition = pinnedPosition,
        showFeedFavIcon = showFeedFavIcon ?: true,
        remoteId = remoteId,
      )
    }
  }
}
