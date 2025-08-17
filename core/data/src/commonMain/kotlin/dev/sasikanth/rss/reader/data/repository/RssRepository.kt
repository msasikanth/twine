/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.data.repository

import app.cash.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import app.cash.sqldelight.paging3.QueryPagingSource
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.data.database.BookmarkQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupFeedQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupQueries
import dev.sasikanth.rss.reader.data.database.FeedQueries
import dev.sasikanth.rss.reader.data.database.FeedSearchFTSQueries
import dev.sasikanth.rss.reader.data.database.PostContentQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.data.database.SourceQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class RssRepository(
  private val feedFetcher: FeedFetcher,
  private val transactionRunner: TransactionRunner,
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val postContentQueries: PostContentQueries,
  private val postSearchFTSQueries: PostSearchFTSQueries,
  private val bookmarkQueries: BookmarkQueries,
  private val feedSearchFTSQueries: FeedSearchFTSQueries,
  private val feedGroupQueries: FeedGroupQueries,
  private val feedGroupFeedQueries: FeedGroupFeedQueries,
  private val sourceQueries: SourceQueries,
  private val dispatchersProvider: DispatchersProvider
) {

  suspend fun fetchAndAddFeed(
    feedLink: String,
    title: String? = null,
    feedLastCleanUpAt: Instant? = null,
  ): FeedAddResult {
    return when (val feedFetchResult = feedFetcher.fetch(url = feedLink)) {
      is FeedFetchResult.Success -> {
        try {
          val feedPayload = feedFetchResult.feedPayload
          val feedId = nameBasedUuidOf(feedPayload.link).toString()
          val name = if (title.isNullOrBlank()) feedPayload.name else title

          withContext(dispatchersProvider.databaseWrite) {
            feedQueries.upsert(
              name = name,
              icon = feedPayload.icon,
              description = feedPayload.description,
              homepageLink = feedPayload.homepageLink,
              createdAt = Clock.System.now(),
              link = feedPayload.link,
              id = feedId
            )

            transactionRunner.invoke {
              val feedLastCleanUpAtEpochMilli =
                feedLastCleanUpAt?.toEpochMilliseconds()
                  ?: Instant.DISTANT_PAST.toEpochMilliseconds()

              feedPayload.posts.forEach { postPayload ->
                if (postPayload.date < feedLastCleanUpAtEpochMilli) return@forEach

                val postId = nameBasedUuidOf(postPayload.link).toString()
                postQueries.upsert(
                  id = postId,
                  sourceId = feedId,
                  title = postPayload.title,
                  description = postPayload.description,
                  imageUrl = postPayload.imageUrl,
                  date = Instant.fromEpochMilliseconds(postPayload.date),
                  syncedAt = Clock.System.now(),
                  link = postPayload.link,
                  commnetsLink = postPayload.commentsLink,
                  isDateParsedCorrectly = postPayload.isDateParsedCorrectly,
                )

                if (postPayload.rawContent != null) {
                  postContentQueries.upsert(
                    id = postId,
                    rawContent = postPayload.rawContent,
                    rawContentLen = postPayload.rawContent.orEmpty().length.toLong(),
                    htmlContent = null,
                    createdAt = Clock.System.now(),
                  )
                }
              }
            }
          }

          FeedAddResult.Success(feedId)
        } catch (e: Exception) {
          FeedAddResult.DatabaseError(e)
        }
      }
      is FeedFetchResult.HttpStatusError -> {
        FeedAddResult.HttpStatusError(feedFetchResult.statusCode)
      }
      is FeedFetchResult.Error -> {
        Logger.e("FeedFetchException", feedFetchResult.exception)
        FeedAddResult.NetworkError(feedFetchResult.exception)
      }
      FeedFetchResult.TooManyRedirects -> {
        FeedAddResult.TooManyRedirects
      }
    }
  }

  fun feed(feedId: String): Feed? {
    return feedQueries
      .feed(
        feedId,
        mapper = {
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
          refreshInterval: Duration ->
          Feed(
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
            refreshInterval = refreshInterval,
            alwaysFetchSourceArticle = alwaysFetchSourceArticle,
            pinnedPosition = pinnedPosition,
            showFeedFavIcon = showFeedFavIcon,
          )
        }
      )
      .executeAsOneOrNull()
  }

  suspend fun postsCountForFeed(feedId: String): Long {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.countPostsForFeed(feedId).executeAsOne()
    }
  }

  suspend fun allPostsCount(
    activeSourceIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): Long {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .allPostsCount(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          lastSyncedAt = lastSyncedAt,
        )
        .executeAsOneOrNull()
        ?: 0L
    }
  }

  fun allPosts(
    activeSourceIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery =
        postQueries.allPostsCount(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          lastSyncedAt = lastSyncedAt,
        ),
      transacter = postQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        postQueries.allPosts(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          numberOfFeaturedPosts = Constants.NUMBER_OF_FEATURED_POSTS,
          lastSyncedAt = lastSyncedAt,
          limit = limit,
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

  suspend fun updateBookmarkStatus(bookmarked: Boolean, id: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateBookmarkStatus(bookmarked = bookmarked, id = id)
    }
  }

  suspend fun updatePostReadStatus(read: Boolean, id: String) {
    withContext(dispatchersProvider.databaseWrite) { postQueries.updateReadStatus(read, id) }
  }

  suspend fun deleteBookmark(id: String) {
    withContext(dispatchersProvider.databaseWrite) { bookmarkQueries.deleteBookmark(id) }
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries
        .feeds(
          mapper = {
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
            refreshInterval: Duration ->
            Feed(
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
              refreshInterval = refreshInterval,
              alwaysFetchSourceArticle = alwaysFetchSourceArticle,
              pinnedPosition = pinnedPosition,
              showFeedFavIcon = showFeedFavIcon,
            )
          }
        )
        .executeAsList()
    }
  }

  suspend fun allFeedGroupsBlocking(): List<FeedGroup> {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .groupsBlocking(
          mapper = {
            id: String,
            name: String,
            feedIds: String?,
            feedHomepageLinks: String,
            feedIconLinks: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
              feedHomepageLinks = feedHomepageLinks.split(",").filterNot { it.isBlank() },
              feedIconLinks = feedIconLinks.split(",").filterNot { it.isBlank() },
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition
            )
          }
        )
        .executeAsList()
    }
  }

  fun numberOfFeeds(): Flow<Long> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(dispatchersProvider.databaseRead)
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
          mapper = ::Feed
        )
      }
    )
  }

  fun feed(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Feed> {
    return feedQueries
      .feedWithUnreadPostsCount(
        id = feedId,
        postsAfter = postsAfter,
        lastSyncedAt = lastSyncedAt,
        mapper = {
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
          numberOfUnreadPosts: Long,
          showFeedFavIcon: Boolean ->
          Feed(
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
            numberOfUnreadPosts = numberOfUnreadPosts,
            showFeedFavIcon = showFeedFavIcon,
          )
        }
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
  }

  suspend fun feedBlocking(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): Feed {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries
        .feedWithUnreadPostsCount(
          id = feedId,
          postsAfter = postsAfter,
          lastSyncedAt = lastSyncedAt,
          mapper = {
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
            numberOfUnreadPosts: Long,
            showFeedFavIcon: Boolean ->
            Feed(
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
              numberOfUnreadPosts = numberOfUnreadPosts,
              showFeedFavIcon = showFeedFavIcon,
            )
          }
        )
        .executeAsOne()
    }
  }

  suspend fun removeFeed(feedId: String) {
    withContext(dispatchersProvider.databaseWrite) { feedQueries.remove(feedId) }
  }

  suspend fun updateFeedName(newFeedName: String, feedId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateFeedName(newFeedName, feedId)
    }
  }

  suspend fun updateFeedLastUpdatedAt(feedId: String, lastUpdatedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateLastUpdatedAt(lastUpdatedAt = lastUpdatedAt, id = feedId)
    }
  }

  suspend fun updateFeedRefreshInterval(feedId: String, refreshInterval: Duration) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateRefreshInterval(refreshInterval = refreshInterval, id = feedId)
    }
  }

  fun search(searchQuery: String, sortOrder: SearchSortOrder): PagingSource<Int, PostWithMetadata> {
    val sanitizedSearchQuery = sanitizeSearchQuery(searchQuery)

    return QueryPagingSource(
      countQuery = postSearchFTSQueries.countSearchResults(sanitizedSearchQuery),
      transacter = postSearchFTSQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        postSearchFTSQueries.search(
          searchQuery = sanitizedSearchQuery,
          sortOrder = sortOrder.value,
          limit = limit,
          offset = offset,
          mapper = ::PostWithMetadata
        )
      }
    )
  }

  fun bookmarks(): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery = bookmarkQueries.countBookmarks(),
      transacter = bookmarkQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        bookmarkQueries.bookmarks(
          limit,
          offset,
          mapper = {
            id,
            sourceId,
            title,
            description,
            imageUrl,
            date,
            link,
            commentsLink,
            bookmarked,
            read,
            feedName,
            feedIcon,
            feedHomepageLink ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
              date = date,
              link = link,
              commentsLink = commentsLink,
              bookmarked = bookmarked,
              read = read,
              feedName = feedName,
              feedIcon = feedIcon,
              feedHomepageLink = feedHomepageLink,
              alwaysFetchFullArticle = true,
            )
          }
        )
      }
    )
  }

  suspend fun hasFeed(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) { feedQueries.hasFeed(id).executeAsOne() }
  }

  suspend fun hasPost(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.post(id).executeAsOneOrNull() != null
    }
  }

  suspend fun toggleFeedPinStatus(feed: Feed) {
    val now =
      if (feed.pinnedAt == null) {
        Clock.System.now()
      } else {
        null
      }
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updatePinnedAt(pinnedAt = now, id = feed.id)
    }
  }

  fun hasFeeds(): Flow<Boolean> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(dispatchersProvider.databaseRead).map {
      it > 0
    }
  }

  /** @return list of feeds from which posts are deleted from */
  suspend fun deleteReadPosts(before: Instant): List<String> {
    return withContext(dispatchersProvider.databaseWrite) {
        postQueries.transactionWithResult {
          postQueries.deleteReadPosts(before = before).executeAsList()
        }
      }
      .distinct()
  }

  suspend fun updateFeedsLastCleanUpAt(feedIds: List<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedIds.forEach { feedId ->
          feedQueries.updateLastCleanUpAt(lastCleanUpAt = Clock.System.now(), id = feedId)
        }
      }
    }
  }

  suspend fun markPostsAsRead(postsAfter: Instant = Instant.DISTANT_PAST) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.markPostsAsRead(sourceId = null, after = postsAfter)
    }
  }

  suspend fun markPostsAsRead(postIds: Set<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIds.forEach { postId -> postQueries.updateReadStatus(read = true, id = postId) }
      }
    }
  }

  suspend fun markPostsInFeedAsRead(
    feedIds: List<String>,
    postsAfter: Instant = Instant.DISTANT_PAST
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedIds.forEach { feedId ->
          postQueries.markPostsAsRead(sourceId = feedId, after = postsAfter)
        }
      }
    }
  }

  suspend fun post(postId: String): Post {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.post(postId, ::Post).executeAsOne()
    }
  }

  suspend fun updateFeedAlwaysFetchSource(feedId: String, newValue: Boolean) {
    return withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateAlwaysFetchSourceArticle(newValue, feedId)
    }
  }

  suspend fun createGroup(name: String): String {
    return withContext(dispatchersProvider.databaseWrite) {
      val id = nameBasedUuidOf(name).toString()
      feedGroupQueries.createGroup(
        id = id,
        name = name,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
      )

      return@withContext id
    }
  }

  suspend fun updateGroupName(groupId: String, name: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateGroupName(name, groupId)
    }
  }

  suspend fun addFeedIdsToGroups(groupIds: Set<String>, feedIds: List<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        groupIds.forEach { groupId ->
          feedIds.forEach { feedId ->
            feedGroupFeedQueries.addFeedToGroup(feedGroupId = groupId, feedId = feedId)
          }
        }
      }
    }
  }

  suspend fun removeFeedIdsFromGroups(groupIds: Set<String>, feedIds: List<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        groupIds.forEach { groupId ->
          feedIds.forEach { feedId ->
            feedGroupFeedQueries.removeFeedFromGroup(feedId = feedId, feedGroupId = groupId)
          }
        }
      }
    }
  }

  suspend fun pinSources(sources: Set<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        sources.forEach { source ->
          val pinnedAt = Clock.System.now()
          feedQueries.updatePinnedAt(id = source.id, pinnedAt = pinnedAt)
          feedGroupQueries.updatePinnedAt(id = source.id, pinnedAt = pinnedAt)
        }
      }
    }
  }

  suspend fun unpinSources(sources: Set<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        sources.forEach { source ->
          feedQueries.updatePinnedAt(id = source.id, pinnedAt = null)
          feedGroupQueries.updatePinnedAt(id = source.id, pinnedAt = null)
        }
      }
    }
  }

  suspend fun deleteSources(sources: Set<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        sources.forEach { source ->
          feedQueries.remove(id = source.id)
          feedGroupQueries.deleteGroup(id = source.id)
        }
      }
    }
  }

  fun pinnedSources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): Flow<List<Source>> {
    return sourceQueries
      .pinnedSources(
        postsAfter = postsAfter,
        lastSyncedAt = lastSyncedAt,
        mapper = {
          type: String,
          id: String,
          name: String,
          icon: String?,
          description: String?,
          link: String?,
          homepageLink: String?,
          createdAt: Instant?,
          pinnedAt: Instant?,
          lastCleanUpAt: Instant?,
          numberOfUnreadPosts: Long,
          feedIds: String?,
          feedHomepageLinks: String?,
          feedIcons: String?,
          updatedAt: Instant?,
          pinnedPosition: Double ->
          if (type == "group") {
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
              feedHomepageLinks =
                feedHomepageLinks?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
              feedIconLinks = feedIcons?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
              createdAt = createdAt!!,
              updatedAt = updatedAt!!,
              pinnedAt = pinnedAt,
              numberOfUnreadPosts = numberOfUnreadPosts,
              pinnedPosition = pinnedPosition
            )
          } else {
            Feed(
              id = id,
              name = name,
              icon = icon!!,
              description = description!!,
              link = link!!,
              homepageLink = homepageLink!!,
              createdAt = createdAt!!,
              pinnedAt = pinnedAt,
              lastCleanUpAt = lastCleanUpAt,
              numberOfUnreadPosts = numberOfUnreadPosts,
              pinnedPosition = pinnedPosition
            )
          }
        }
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  fun sources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Source> {
    return QueryPagingSource(
      countQuery = sourceQueries.sourcesCount(),
      transacter = sourceQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        sourceQueries.sources(
          postsAfter = postsAfter,
          lastSyncedAt = lastSyncedAt,
          orderBy = orderBy.value,
          limit = limit,
          offset = offset,
          mapper = {
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
            updatedAt: Instant? ->
            if (type == "group") {
              FeedGroup(
                id = id,
                name = name,
                feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
                feedHomepageLinks =
                  feedHomepageLinks?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
                feedIconLinks = feedIcons?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
                createdAt = createdAt,
                updatedAt = updatedAt!!,
                pinnedAt = pinnedAt,
                numberOfUnreadPosts = numberOfUnreadPosts,
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
              )
            }
          }
        )
      }
    )
  }

  fun source(
    id: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    lastSyncedAt: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Source?> {
    return sourceQueries
      .source(
        postsAfter = postsAfter,
        lastSyncedAt = lastSyncedAt,
        id = id,
        mapper = {
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
          updatedAt: Instant? ->
          if (type == "group") {
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
              feedHomepageLinks =
                feedHomepageLinks?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
              feedIconLinks = feedIcons?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
              createdAt = createdAt,
              updatedAt = updatedAt!!,
              pinnedAt = pinnedAt,
              numberOfUnreadPosts = numberOfUnreadPosts,
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
            )
          }
        }
      )
      .asFlow()
      .mapToOneOrNull(dispatchersProvider.databaseRead)
  }

  fun allGroups(): PagingSource<Int, FeedGroup> {
    return QueryPagingSource(
      countQuery = feedGroupQueries.count(),
      transacter = feedGroupQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        feedGroupQueries.groups(
          limit = limit,
          offset = offset,
          mapper = {
            id: String,
            name: String,
            feedIds: String?,
            feedHomepageLinks: String,
            feedIcons: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
              feedHomepageLinks = feedHomepageLinks.split(",").filterNot { it.isBlank() },
              feedIconLinks = feedIcons.split(",").filterNot { it.isBlank() },
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition
            )
          }
        )
      }
    )
  }

  fun numberOfFeedGroups(): Flow<Long> {
    return feedGroupQueries.count().asFlow().mapToOne(dispatchersProvider.databaseRead)
  }

  suspend fun groupByIds(ids: Set<String>): List<FeedGroup> {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .groupsByIds(
          ids = ids,
          mapper = {
            id: String,
            name: String,
            feedIds: String?,
            feedHomepageLinks: String,
            feedIcons: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
              feedHomepageLinks = feedHomepageLinks.split(",").filterNot { it.isBlank() },
              feedIconLinks = feedIcons.split(",").filterNot { it.isBlank() },
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
            )
          }
        )
        .executeAsList()
    }
  }

  fun groupById(groupId: String): Flow<FeedGroup> {
    return feedGroupQueries
      .groupsByIds(
        ids = setOf(groupId),
        mapper = {
          id: String,
          name: String,
          feedIds: String?,
          feedHomepageLinks: String,
          feedIcons: String,
          createdAt: Instant,
          updatedAt: Instant,
          pinnedAt: Instant? ->
          FeedGroup(
            id = id,
            name = name,
            feedIds = feedIds.orEmpty().split(",").filterNot { it.isBlank() },
            feedHomepageLinks = feedHomepageLinks.split(",").filterNot { it.isBlank() },
            feedIconLinks = feedIcons.split(",").filterNot { it.isBlank() },
            createdAt = createdAt,
            updatedAt = updatedAt,
            pinnedAt = pinnedAt,
          )
        }
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
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
          mapper = {
            id: String,
            name: String,
            icon: String,
            description: String,
            link: String,
            homepageLink: String,
            createdAt: Instant,
            pinnedAt: Instant?,
            lastCleanUpAt: Instant?,
            numberOfUnreadPosts: Long,
            showFeedFavIcon: Boolean ->
            Feed(
              id = id,
              name = name,
              icon = icon,
              description = description,
              link = link,
              homepageLink = homepageLink,
              createdAt = createdAt,
              pinnedAt = pinnedAt,
              lastCleanUpAt = lastCleanUpAt,
              numberOfUnreadPosts = numberOfUnreadPosts,
              showFeedFavIcon = showFeedFavIcon,
            )
          }
        )
      }
    )
  }

  suspend fun updatedSourcePinnedPosition(sources: List<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        sources.forEachIndexed { index, source ->
          feedQueries.updatedPinnedPosition(index.toDouble(), source.id)
          feedGroupQueries.updatedPinnedPosition(index.toDouble(), source.id)
        }
      }
    }
  }

  fun hasUnreadPostsInSource(
    activeSourceIds: List<String>,
    postsAfter: Instant = Instant.DISTANT_PAST
  ): Flow<Boolean> {
    return postQueries
      .unreadPostsCountInSource(
        isSourceIdsEmpty = activeSourceIds.isEmpty(),
        sourceIds = activeSourceIds,
        after = postsAfter
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
      .map { it > 0 }
  }

  fun unreadSinceLastSync(
    sources: List<String>,
    postsAfter: Instant,
    lastSyncedAt: Instant
  ): Flow<UnreadSinceLastSync> {
    return postQueries
      .unreadSinceLastSync(
        isSourceIdsEmpty = sources.isEmpty(),
        sourceIds = sources,
        postsAfter = postsAfter,
        lastSyncedAt = lastSyncedAt,
        mapper = { count, feedHomepageLinks, feedIcons ->
          UnreadSinceLastSync(
            hasNewArticles = count > 0,
            feedHomepageLinks = feedHomepageLinks.orEmpty().split(",").filterNot { it.isBlank() },
            feedIcons = feedIcons.orEmpty().split(",").filterNot { it.isBlank() }
          )
        }
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace(Regex.fromLiteral("\""), "\"\"").run { "\"$this\"" }
  }
}
