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
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.data.database.FeedQueries
import dev.sasikanth.rss.reader.data.database.FeedSearchFTSQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FeedRepository(
  private val feedQueries: FeedQueries,
  private val feedSearchFTSQueries: FeedSearchFTSQueries,
  private val dispatchersProvider: DispatchersProvider,
) {

  fun feed(feedId: String): Feed? {
    return feedQueries.feed(feedId, mapper = ::mapToFeed).executeAsOneOrNull()
  }

  fun allFeeds(): Flow<List<Feed>> {
    return feedQueries
      .allFeedsBlocking(mapper = ::mapToFeed)
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries.allFeedsBlocking(mapper = ::mapToFeed).executeAsList()
    }
  }

  fun numberOfFeeds(): Flow<Long> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(dispatchersProvider.databaseRead)
  }

  fun hasFeeds(): Flow<Boolean> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(dispatchersProvider.databaseRead).map {
      it > 0
    }
  }

  /** Search feeds, returns all feeds if [searchQuery] is empty */
  fun searchFeed(
    searchQuery: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
  ): PagingSource<Int, Feed> {
    val sanitizedSearchQuery = sanitizeSearchQuery(searchQuery)

    return QueryPagingSource(
      countQuery = feedSearchFTSQueries.countSearchResults(searchQuery = sanitizedSearchQuery),
      transacter = feedSearchFTSQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        feedSearchFTSQueries.search(
          searchQuery = sanitizedSearchQuery,
          postsAfter = postsAfter,
          limit = limit,
          offset = offset,
          mapper = ::mapToFeedWithUnreadCountAndRefreshInterval,
        )
      },
    )
  }

  fun feedWithUnreadCount(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Feed> {
    return feedQueries
      .feedWithUnreadPostsCount(
        id = feedId,
        postsAfter = postsAfter,
        postsUpperBound = postsUpperBound,
        mapper = ::mapToFeedWithUnreadCount,
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
  }

  suspend fun feedWithUnreadCountBlocking(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Feed {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries
        .feedWithUnreadPostsCount(
          id = feedId,
          postsAfter = postsAfter,
          postsUpperBound = postsUpperBound,
          mapper = ::mapToFeedWithUnreadCount,
        )
        .executeAsOne()
    }
  }

  suspend fun hasFeed(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) { feedQueries.hasFeed(id).executeAsOne() }
  }

  suspend fun updateFeedName(newFeedName: String, feedId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateFeedName(
        newFeedName = newFeedName,
        id = feedId,
        lastUpdatedAt = kotlin.time.Clock.System.now(),
      )
    }
  }

  suspend fun updateFeedLastUpdatedAt(feedId: String, lastUpdatedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateLastUpdatedAt(lastUpdatedAt = lastUpdatedAt, id = feedId)
    }
  }

  suspend fun updateFeedRefreshInterval(feedId: String, refreshInterval: Duration) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateRefreshInterval(refreshInterval = refreshInterval.toString(), id = feedId)
    }
  }

  suspend fun updateFeedAlwaysFetchSource(feedId: String, newValue: Boolean) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateAlwaysFetchSourceArticle(newValue, feedId)
    }
  }

  suspend fun updateFeedShowFavIcon(feedId: String, newValue: Boolean) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateShowFeedFavIcon(newValue, feedId)
    }
  }

  fun feedsInGroup(
    feedIds: List<String>,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Feed> {
    return QueryPagingSource(
      countQuery = feedQueries.feedsInGroupPaginatedCount(feedIds),
      transacter = feedQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        feedQueries.feedsInGroupPaginated(
          feedIds = feedIds,
          orderBy = orderBy.value,
          limit = limit,
          offset = offset,
          mapper = ::mapToFeedWithUnreadCount,
        )
      },
    )
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace("\"", "\"\"").run { "\"$this\"" }
  }

  private fun mapToFeed(
    id: String,
    name: String,
    icon: String,
    description: String,
    link: String,
    homepageLink: String,
    createdAt: Instant,
    pinnedAt: Instant?,
    lastCleanUpAt: Instant?,
    alwaysFetchSourceArticle: Boolean,
    pinnedPosition: Double,
    showFeedFavIcon: Boolean,
    lastUpdatedAt: Instant?,
    refreshInterval: String,
    isDeleted: Boolean,
    hideFromAllFeeds: Boolean,
    remoteId: String?,
  ): Feed {
    return Feed(
      id = id,
      name = name,
      icon = icon,
      description = description,
      homepageLink = homepageLink,
      createdAt = createdAt,
      link = link,
      pinnedAt = pinnedAt,
      lastCleanUpAt = lastCleanUpAt,
      lastUpdatedAt = lastUpdatedAt,
      refreshInterval = Duration.parse(refreshInterval),
      alwaysFetchSourceArticle = alwaysFetchSourceArticle,
      pinnedPosition = pinnedPosition,
      showFeedFavIcon = showFeedFavIcon,
      hideFromAllFeeds = hideFromAllFeeds,
      isDeleted = isDeleted,
      remoteId = remoteId,
    )
  }

  private fun mapToFeedWithUnreadCount(
    id: String,
    name: String,
    icon: String,
    description: String,
    link: String,
    homepageLink: String,
    createdAt: Instant,
    pinnedAt: Instant?,
    lastCleanUpAt: Instant?,
    alwaysFetchSourceArticle: Boolean,
    pinnedPosition: Double,
    numberOfUnreadPosts: Long,
    showFeedFavIcon: Boolean,
    hideFromAllFeeds: Boolean,
    isDeleted: Boolean,
    remoteId: String?,
  ): Feed {
    return Feed(
      id = id,
      name = name,
      icon = icon,
      description = description,
      homepageLink = homepageLink,
      createdAt = createdAt,
      link = link,
      pinnedAt = pinnedAt,
      lastCleanUpAt = lastCleanUpAt,
      alwaysFetchSourceArticle = alwaysFetchSourceArticle,
      pinnedPosition = pinnedPosition,
      numberOfUnreadPosts = numberOfUnreadPosts,
      showFeedFavIcon = showFeedFavIcon,
      hideFromAllFeeds = hideFromAllFeeds,
      isDeleted = isDeleted,
      remoteId = remoteId,
    )
  }

  private fun mapToFeedWithUnreadCountAndRefreshInterval(
    id: String,
    name: String,
    icon: String,
    description: String,
    link: String,
    homepageLink: String,
    createdAt: Instant,
    pinnedAt: Instant?,
    lastCleanUpAt: Instant?,
    alwaysFetchSourceArticle: Boolean,
    pinnedPosition: Double,
    lastUpdatedAt: Instant?,
    refreshInterval: String,
    isDeleted: Boolean,
    remoteId: String?,
    numberOfUnreadPosts: Long,
    showFeedFavIcon: Boolean,
    hideFromAllFeeds: Boolean,
  ): Feed {
    return Feed(
      id = id,
      name = name,
      icon = icon,
      description = description,
      link = link,
      homepageLink = homepageLink,
      createdAt = createdAt,
      pinnedAt = pinnedAt,
      lastCleanUpAt = lastCleanUpAt,
      alwaysFetchSourceArticle = alwaysFetchSourceArticle,
      pinnedPosition = pinnedPosition,
      lastUpdatedAt = lastUpdatedAt,
      refreshInterval = Duration.parse(refreshInterval),
      isDeleted = isDeleted,
      remoteId = remoteId,
      numberOfUnreadPosts = numberOfUnreadPosts,
      showFeedFavIcon = showFeedFavIcon,
      hideFromAllFeeds = hideFromAllFeeds,
    )
  }
}
