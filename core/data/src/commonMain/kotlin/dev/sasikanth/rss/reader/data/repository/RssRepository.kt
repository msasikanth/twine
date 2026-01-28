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
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.data.database.AppConfigQueries
import dev.sasikanth.rss.reader.data.database.BlockedWordsQueries
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
import dev.sasikanth.rss.reader.data.sync.ReadPostSyncEntity
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class RssRepository(
  private val transactionRunner: TransactionRunner,
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val postContentQueries: PostContentQueries,
  private val postSearchFTSQueries: PostSearchFTSQueries,
  private val bookmarkQueries: BookmarkQueries,
  private val feedSearchFTSQueries: FeedSearchFTSQueries,
  private val feedGroupQueries: FeedGroupQueries,
  private val feedGroupFeedQueries: FeedGroupFeedQueries,
  private val blockedWordsQueries: BlockedWordsQueries,
  private val appConfigQueries: AppConfigQueries,
  private val sourceQueries: SourceQueries,
  private val dispatchersProvider: DispatchersProvider
) {

  suspend fun deleteAllLocalData() {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postQueries.deleteAll()
        postContentQueries.deleteAll()
        feedGroupFeedQueries.deleteAll()
        feedQueries.deleteAll()
        feedGroupQueries.deleteAll()
        blockedWordsQueries.deleteAll()
        appConfigQueries.deleteAll()
      }
    }
  }

  suspend fun upsertFeedWithPosts(
    feedPayload: FeedPayload,
    feedId: String? = null,
    title: String? = null,
    feedLastCleanUpAt: Instant? = null,
    alwaysFetchSourceArticle: Boolean = false,
    showWebsiteFavIcon: Boolean = true,
    updateFeed: Boolean = true,
  ): String {
    val finalFeedId = feedId ?: nameBasedUuidOf(feedPayload.link).toString()

    if (updateFeed) {
      val name = if (title.isNullOrBlank()) feedPayload.name else title
      withContext(dispatchersProvider.databaseWrite) {
        feedQueries.upsert(
          id = finalFeedId,
          name = name,
          icon = feedPayload.icon,
          description = feedPayload.description,
          homepageLink = feedPayload.homepageLink,
          link = feedPayload.link,
          showFeedFavIcon = showWebsiteFavIcon,
          alwaysFetchSourceArticle = alwaysFetchSourceArticle,
          createdAt = Clock.System.now(),
          lastUpdatedAt = Clock.System.now(),
        )
      }
    }

    val feedLastCleanUpAtEpochMilli =
      feedLastCleanUpAt?.toEpochMilliseconds() ?: Instant.DISTANT_PAST.toEpochMilliseconds()

    val posts =
      feedPayload.posts
        .filter { it.date >= feedLastCleanUpAtEpochMilli }
        .toList()
        .sortedBy { it.date }

    withContext(dispatchersProvider.databaseWrite) {
      posts.forEach { postPayload ->
        val postId = nameBasedUuidOf(postPayload.link).toString()
        transactionRunner.invoke {
          postQueries.upsert(
            id = postId,
            sourceId = finalFeedId,
            title = postPayload.title,
            description = postPayload.description,
            imageUrl = postPayload.imageUrl,
            postDate = Instant.fromEpochMilliseconds(postPayload.date),
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now(),
            syncedAt = Clock.System.now(),
            link = postPayload.link,
            commentsLink = postPayload.commentsLink,
            isDateParsedCorrectly = if (postPayload.isDateParsedCorrectly) 1 else 0,
          )

          postContentQueries.upsert(
            id = postId,
            rawContent = postPayload.rawContent,
            rawContentLen = postPayload.rawContent.orEmpty().length.toLong(),
            htmlContent = postPayload.fullContent,
            createdAt = Clock.System.now(),
          )
        }
      }
    }

    return finalFeedId
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
          refreshInterval: String,
          isDeleted: Boolean,
          hideFromAllFeeds: Boolean,
          remoteId: String? ->
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
            refreshInterval = Duration.parse(refreshInterval),
            alwaysFetchSourceArticle = alwaysFetchSourceArticle,
            pinnedPosition = pinnedPosition,
            showFeedFavIcon = showFeedFavIcon,
            hideFromAllFeeds = hideFromAllFeeds,
            isDeleted = isDeleted,
            remoteId = remoteId,
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
  ): Long? {
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
    }
  }

  fun allPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
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
          orderBy = postsSortOrder.name,
          limit = limit,
          offset = offset,
          mapper = {
            id: String,
            sourceId: String,
            title: String,
            description: String,
            imageUrl: String?,
            date: Instant,
            createdAt: Instant,
            link: String,
            commentsLink: String?,
            flags: Set<PostFlag>,
            remoteId: String?,
            feedName: String,
            feedIcon: String,
            feedHomepageLink: String,
            alwaysFetchFullArticle: Boolean,
            showFeedFavIcon: Boolean,
            _: Long ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
              date = date,
              createdAt = createdAt,
              link = link,
              commentsLink = commentsLink,
              flags = flags,
              feedName = feedName,
              feedIcon = feedIcon,
              feedHomepageLink = feedHomepageLink,
              alwaysFetchFullArticle = alwaysFetchFullArticle,
              showFeedFavIcon = showFeedFavIcon,
              remoteId = remoteId,
            )
          }
        )
      }
    )
  }

  suspend fun updateBookmarkStatus(bookmarked: Boolean, id: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateBookmarkStatus(
        bookmarked = if (bookmarked) 1L else 0L,
        id = id,
        updatedAt = Clock.System.now()
      )
    }
  }

  suspend fun updatePostReadStatus(read: Boolean, id: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateReadStatus(
        read = if (read) 1L else 0L,
        id = id,
        updatedAt = Clock.System.now()
      )
    }
  }

  suspend fun allReadPostsBlocking(): List<ReadPostSyncEntity> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries
        .allReadPostsBlocking { id, updatedAt ->
          ReadPostSyncEntity(id, updatedAt.toEpochMilliseconds())
        }
        .executeAsList()
    }
  }

  suspend fun deleteBookmark(id: String) {
    withContext(dispatchersProvider.databaseWrite) { bookmarkQueries.deleteBookmark(id) }
  }

  suspend fun allBookmarkIdsBlocking(): List<String> {
    return withContext(dispatchersProvider.databaseRead) {
      bookmarkQueries.allBookmarkIds().executeAsList()
    }
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries
        .allFeedsBlocking(
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
            refreshInterval: String,
            isDeleted: Boolean,
            hideFromAllFeeds: Boolean,
            remoteId: String? ->
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
              refreshInterval = Duration.parse(refreshInterval),
              alwaysFetchSourceArticle = alwaysFetchSourceArticle,
              pinnedPosition = pinnedPosition,
              showFeedFavIcon = showFeedFavIcon,
              hideFromAllFeeds = hideFromAllFeeds,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          }
        )
        .executeAsList()
    }
  }

  suspend fun allFeedGroupsBlocking(): List<FeedGroup> {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .allGroupsBlocking(
          mapper = {
            id: String,
            name: String,
            feedIds: String?,
            feedHomepageLinks: String,
            feedIconLinks: String,
            feedShowFavIconSettings: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double,
            isDeleted: Boolean,
            remoteId: String? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds?.split(Constants.GROUP_CONCAT_SEPARATOR)?.filter { it.isNotBlank() }
                  ?: emptyList(),
              feedHomepageLinks =
                feedHomepageLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filter {
                  it.isNotBlank()
                },
              feedIconLinks =
                feedIconLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filter { it.isNotBlank() },
              feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition,
              isDeleted = isDeleted,
              remoteId = remoteId,
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
          mapper = {
            id,
            name,
            icon,
            description,
            link,
            homepageLink,
            createdAt,
            pinnedAt,
            lastCleanUpAt,
            alwaysFetchSourceArticle,
            pinnedPosition,
            lastUpdatedAt,
            refreshInterval,
            isDeleted,
            remoteId,
            numberOfUnreadPosts,
            showFeedFavIcon,
            hideFromAllFeeds ->
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
          pinnedPosition: Double,
          numberOfUnreadPosts: Long,
          showFeedFavIcon: Boolean,
          hideFromAllFeeds: Boolean,
          isDeleted: Boolean,
          remoteId: String? ->
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
            pinnedPosition = pinnedPosition,
            numberOfUnreadPosts = numberOfUnreadPosts,
            showFeedFavIcon = showFeedFavIcon,
            hideFromAllFeeds = hideFromAllFeeds,
            isDeleted = isDeleted,
            remoteId = remoteId,
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
            pinnedPosition: Double,
            numberOfUnreadPosts: Long,
            showFeedFavIcon: Boolean,
            hideFromAllFeeds: Boolean,
            isDeleted: Boolean,
            remoteId: String? ->
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
              pinnedPosition = pinnedPosition,
              numberOfUnreadPosts = numberOfUnreadPosts,
              showFeedFavIcon = showFeedFavIcon,
              hideFromAllFeeds = hideFromAllFeeds,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          }
        )
        .executeAsOne()
    }
  }

  suspend fun removeFeed(feedId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.transaction {
        feedQueries.markAsDeleted(id = feedId, lastUpdatedAt = Clock.System.now())
        postQueries.deletePostsForFeed(feedId)
      }
    }
  }

  private fun mapToFeedShowFavIconSettings(feedShowFavIconSettings: String?): List<Boolean> {
    return feedShowFavIconSettings
      ?.split(Constants.GROUP_CONCAT_SEPARATOR)
      ?.filterNot { it.isBlank() }
      ?.map {
        when (it) {
          "true" -> true
          "false" -> false
          else -> true
        }
      }
      .orEmpty()
  }

  suspend fun updateFeedName(newFeedName: String, feedId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateFeedName(
        newFeedName = newFeedName,
        id = feedId,
        lastUpdatedAt = Clock.System.now()
      )
    }
  }

  suspend fun updateFeedLastUpdatedAt(feedId: String, lastUpdatedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateLastUpdatedAt(lastUpdatedAt = lastUpdatedAt, id = feedId)
    }
  }

  suspend fun updateFeedGroupUpdatedAt(groupId: String, updatedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateUpdatedAt(updatedAt, groupId)
    }
  }

  suspend fun updateFeedRefreshInterval(feedId: String, refreshInterval: Duration) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateRefreshInterval(refreshInterval = refreshInterval.toString(), id = feedId)
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
          mapper = {
            id: String,
            sourceId: String,
            title: String,
            description: String,
            imageUrl: String?,
            date: Instant,
            createdAt: Instant,
            link: String,
            commentsLink: String?,
            flags: Set<PostFlag>,
            feedName: String,
            feedIcon: String,
            feedHomepageLink: String,
            alwaysFetchSourceArticle: Boolean,
            showFeedFavIcon: Boolean ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
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
            )
          }
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
            id: String,
            sourceId: String,
            title: String,
            description: String,
            imageUrl: String?,
            date: Instant,
            createdAt: Instant,
            link: String,
            commentsLink: String?,
            flags: Set<PostFlag>,
            feedName: String,
            feedIcon: String,
            feedHomepageLink: String,
            showFeedFavIcon: Boolean ->
            PostWithMetadata(
              id = id,
              sourceId = sourceId,
              title = title,
              description = description,
              imageUrl = imageUrl,
              date = date,
              createdAt = createdAt,
              link = link,
              commentsLink = commentsLink,
              flags = flags,
              feedName = feedName,
              feedIcon = feedIcon,
              feedHomepageLink = feedHomepageLink,
              alwaysFetchFullArticle = true,
              showFeedFavIcon = showFeedFavIcon,
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
      feedQueries.updatePinnedAt(pinnedAt = now, id = feed.id, lastUpdatedAt = Clock.System.now())
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

  suspend fun updateFeedsLastCleanUpAt(
    feedIds: List<String>,
    lastCleanUpAt: Instant = Clock.System.now()
  ) {
    val feedIdsSnapshot = feedIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedIdsSnapshot.forEach { feedId ->
          feedQueries.updateLastCleanUpAt(lastCleanUpAt = lastCleanUpAt, id = feedId)
        }
      }
    }
  }

  suspend fun markPostsAsRead(postsAfter: Instant = Instant.DISTANT_PAST) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.markPostsAsRead(
        sourceId = null,
        after = postsAfter,
        updatedAt = Clock.System.now()
      )
    }
  }

  suspend fun markPostsAsRead(postIds: Set<String>) {
    val postIdsSnapshot = postIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIdsSnapshot.forEach { postId ->
          postQueries.updateReadStatus(read = 1L, id = postId, updatedAt = Clock.System.now())
        }
      }
    }
  }

  suspend fun markPostsInFeedAsRead(
    feedIds: List<String>,
    postsAfter: Instant = Instant.DISTANT_PAST
  ) {
    val feedIdsSnapshot = feedIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedIdsSnapshot.forEach { feedId ->
          postQueries.markPostsAsRead(
            sourceId = feedId,
            after = postsAfter,
            updatedAt = Clock.System.now()
          )
        }
      }
    }
  }

  suspend fun post(postId: String): Post {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.post(postId, ::Post).executeAsOne()
    }
  }

  suspend fun postOrNull(postId: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.post(postId, ::Post).executeAsList().firstOrNull()
    }
  }

  suspend fun updateFeedRemoteId(
    remoteId: String,
    feedId: String,
    lastUpdatedAt: Instant = Clock.System.now(),
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateFeedRemoteId(
        remoteId = remoteId,
        lastUpdatedAt = lastUpdatedAt,
        id = feedId,
      )
    }
  }

  suspend fun updatePostRemoteId(remoteId: String, postId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updatePostRemoteId(
        remoteId = remoteId,
        updatedAt = Clock.System.now(),
        id = postId,
      )
    }
  }

  suspend fun updatePostSyncedAt(postId: String, syncedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updatePostSyncedAt(syncedAt = syncedAt, id = postId)
    }
  }

  suspend fun updateFeedGroupRemoteId(
    remoteId: String,
    groupId: String,
    updatedAt: Instant = Clock.System.now(),
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateFeedGroupRemoteId(
        remoteId = remoteId,
        updatedAt = updatedAt,
        id = groupId,
      )
    }
  }

  suspend fun feedGroupByRemoteId(remoteId: String): FeedGroup? {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .feedGroupByRemoteId(
          remoteId = remoteId,
          mapper = {
            id: String,
            name: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double,
            isDeleted: Boolean,
            remoteId: String? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = emptyList(),
              feedHomepageLinks = emptyList(),
              feedIconLinks = emptyList(),
              feedShowFavIconSettings = emptyList(),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          }
        )
        .executeAsOneOrNull()
    }
  }

  suspend fun postsWithRemoteId(): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithRemoteId(::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChanges(): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChanges(::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChangesForFeed(feedId: String): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChangesForFeed(feedId, ::Post).executeAsList()
    }
  }

  suspend fun postByRemoteId(remoteId: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postByRemoteId(remoteId, ::Post).executeAsOneOrNull()
    }
  }

  suspend fun postByLink(link: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postByLink(link, ::Post).executeAsOneOrNull()
    }
  }

  fun feedByRemoteId(remoteId: String): Feed? {
    return feedQueries
      .feedByRemoteId(
        remoteId,
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
          refreshInterval: String,
          isDeleted: Boolean,
          hideFromAllFeeds: Boolean,
          remoteId: String? ->
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
            refreshInterval = Duration.parse(refreshInterval),
            alwaysFetchSourceArticle = alwaysFetchSourceArticle,
            pinnedPosition = pinnedPosition,
            showFeedFavIcon = showFeedFavIcon,
            hideFromAllFeeds = hideFromAllFeeds,
            isDeleted = isDeleted,
            remoteId = remoteId,
          )
        }
      )
      .executeAsOneOrNull()
  }

  suspend fun upsertPosts(posts: List<Post>) {
    val postsSnapshot = posts.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postsSnapshot.forEach { post ->
          postQueries.upsertSyncPost(
            id = post.id,
            sourceId = post.sourceId,
            title = post.title,
            description = post.description,
            imageUrl = post.imageUrl,
            postDate = post.postDate,
            createdAt = post.createdAt,
            updatedAt = post.updatedAt,
            syncedAt = post.syncedAt,
            link = post.link,
            commentsLink = post.commentsLink,
            flags = post.flags,
            remoteId = post.remoteId,
          )
        }
      }
    }
  }

  suspend fun upsertFeeds(feeds: List<Feed>) {
    val feedsSnapshot = feeds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedsSnapshot.forEach { feed ->
          feedQueries.upsertSyncFeed(
            id = feed.id,
            name = feed.name,
            icon = feed.icon,
            description = feed.description,
            link = feed.link,
            homepageLink = feed.homepageLink,
            createdAt = feed.createdAt,
            pinnedAt = feed.pinnedAt,
            lastCleanUpAt = feed.lastCleanUpAt,
            alwaysFetchSourceArticle = feed.alwaysFetchSourceArticle,
            lastUpdatedAt = feed.lastUpdatedAt,
            isDeleted = feed.isDeleted,
            hideFromAllFeeds = feed.hideFromAllFeeds,
            remoteId = feed.remoteId,
          )
        }
      }
    }
  }

  suspend fun upsertGroup(
    id: String,
    name: String,
    pinnedAt: Instant?,
    updatedAt: Instant,
    isDeleted: Boolean,
    remoteId: String? = null,
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.upsertSyncGroup(
        id = id,
        name = name,
        createdAt = Clock.System.now(),
        updatedAt = updatedAt,
        pinnedAt = pinnedAt,
        isDeleted = isDeleted,
        remoteId = remoteId,
      )
    }
  }

  suspend fun feedGroupBlocking(id: String): FeedGroup? {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .group(
          id,
          mapper = {
            id: String,
            name: String,
            feedIds: String?,
            feedHomepageLinks: String,
            feedIconLinks: String,
            feedShowFavIconSettings: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double,
            isDeleted: Boolean,
            remoteId: String? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds?.split(Constants.GROUP_CONCAT_SEPARATOR)?.filter { it.isNotBlank() }
                  ?: emptyList(),
              feedHomepageLinks =
                feedHomepageLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filter {
                  it.isNotBlank()
                },
              feedIconLinks =
                feedIconLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filter { it.isNotBlank() },
              feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          }
        )
        .executeAsOneOrNull()
    }
  }

  suspend fun replaceFeedsInGroup(groupId: String, feedIds: List<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedGroupFeedQueries.removeAllFeedsFromGroup(groupId)
        feedIds.forEach { feedId ->
          feedGroupFeedQueries.addFeedToGroup(feedGroupId = groupId, feedId = feedId)
        }
      }
    }
  }

  suspend fun deleteReadPostsForFeedOlderThan(feedId: String, before: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.deleteReadPostsForFeed(feedId = feedId, before = before)
    }
  }

  suspend fun updateFeedAlwaysFetchSource(feedId: String, newValue: Boolean) {
    return withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateAlwaysFetchSourceArticle(newValue, feedId)
    }
  }

  suspend fun updateFeedShowFavIcon(feedId: String, newValue: Boolean) {
    return withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateShowFeedFavIcon(newValue, feedId)
    }
  }

  suspend fun updateFeedHideFromAllFeeds(feedId: String, newValue: Boolean) {
    return withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateHideFromAllFeeds(
        hideFromAllFeeds = newValue,
        lastUpdatedAt = Clock.System.now(),
        id = feedId
      )
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
      feedGroupQueries.updateGroupName(name = name, id = groupId, updatedAt = Clock.System.now())
    }
  }

  suspend fun addFeedIdsToGroups(groupIds: Set<String>, feedIds: List<String>) {
    val groupIdsSnapshot = groupIds.toList()
    val feedIdsSnapshot = feedIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        feedIdsSnapshot.forEach { feedId ->
          val currentGroupIds =
            feedGroupFeedQueries.groupIdsForFeed(feedId).executeAsList().mapNotNull {
              it.feedGroupId
            }

          currentGroupIds.forEach { groupId -> feedGroupQueries.updateUpdatedAt(now, groupId) }

          feedGroupFeedQueries.removeFeedFromAllGroups(feedId)
          feedQueries.updateLastUpdatedAt(lastUpdatedAt = now, id = feedId)
        }

        groupIdsSnapshot.forEach { groupId ->
          feedIdsSnapshot.forEach { feedId ->
            feedGroupFeedQueries.addFeedToGroup(feedGroupId = groupId, feedId = feedId)
          }
          feedGroupQueries.updateUpdatedAt(now, groupId)
        }
      }
    }
  }

  suspend fun groupIdsForFeed(feedId: String): List<String> {
    return withContext(dispatchersProvider.io) {
      feedGroupFeedQueries
        .groupIdsForFeed(feedId)
        .executeAsList()
        .map { it.feedGroupId }
        .filterNotNull()
    }
  }

  suspend fun removeFeedIdsFromGroups(groupIds: Set<String>, feedIds: List<String>) {
    val groupIdsSnapshot = groupIds.toList()
    val feedIdsSnapshot = feedIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        groupIdsSnapshot.forEach { groupId ->
          feedIdsSnapshot.forEach { feedId ->
            feedGroupFeedQueries.removeFeedFromGroup(feedId = feedId, feedGroupId = groupId)
          }
          feedGroupQueries.updateUpdatedAt(Clock.System.now(), groupId)
        }
      }
    }
  }

  suspend fun pinSources(sources: Set<Source>) {
    val sourcesSnapshot = sources.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sourcesSnapshot.forEach { source ->
          feedQueries.updatePinnedAt(id = source.id, pinnedAt = now, lastUpdatedAt = now)
          feedGroupQueries.updatePinnedAt(id = source.id, pinnedAt = now, updatedAt = now)
        }
      }
    }
  }

  suspend fun unpinSources(sources: Set<Source>) {
    val sourcesSnapshot = sources.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sourcesSnapshot.forEach { source ->
          feedQueries.updatePinnedAt(id = source.id, pinnedAt = null, lastUpdatedAt = now)
          feedGroupQueries.updatePinnedAt(id = source.id, pinnedAt = null, updatedAt = now)
        }
      }
    }
  }

  suspend fun markSourcesAsDeleted(sources: Set<Source>) {
    val sourcesSnapshot = sources.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sourcesSnapshot.forEach { source ->
          feedQueries.markAsDeleted(id = source.id, lastUpdatedAt = now)
          postQueries.deletePostsForFeed(source.id)
          feedGroupQueries.markAsDeleted(id = source.id, updatedAt = now)
        }
      }
    }
  }

  suspend fun deleteSources(sources: Set<Source>) {
    val sourcesSnapshot = sources.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sourcesSnapshot.forEach { source ->
          feedQueries.remove(id = source.id)
          postQueries.deletePostsForFeed(source.id)
          feedGroupQueries.remove(id = source.id)
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
          remoteId: String? ->
          if (type == "group") {
            FeedGroup(
              id = id,
              name = name,
              feedIds =
                feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedHomepageLinks =
                feedHomepageLinks
                  ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                  ?.filterNot { it.isBlank() }
                  .orEmpty(),
              feedIconLinks =
                feedIcons
                  ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                  ?.filterNot { it.isBlank() }
                  .orEmpty(),
              feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
              createdAt = createdAt,
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
              createdAt = createdAt,
              pinnedAt = pinnedAt,
              lastCleanUpAt = lastCleanUpAt,
              numberOfUnreadPosts = numberOfUnreadPosts,
              pinnedPosition = pinnedPosition,
              showFeedFavIcon = showFeedFavIcon ?: true,
              remoteId = remoteId
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
            feedShowFavIconSettings: String?,
            updatedAt: Instant?,
            pinnedPosition: Double,
            showFeedFavIcon: Boolean?,
            remoteId: String? ->
            if (type == "group") {
              FeedGroup(
                id = id,
                name = name,
                feedIds =
                  feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                    it.isBlank()
                  },
                feedHomepageLinks =
                  feedHomepageLinks
                    ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                    ?.filterNot { it.isBlank() }
                    .orEmpty(),
                feedIconLinks =
                  feedIcons
                    ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                    ?.filterNot { it.isBlank() }
                    .orEmpty(),
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
                remoteId = remoteId
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
          feedShowFavIconSettings: String?,
          updatedAt: Instant?,
          pinnedPosition: Double,
          showFeedFavIcon: Boolean?,
          remoteId: String? ->
          if (type == "group") {
            FeedGroup(
              id = id,
              name = name,
              feedIds =
                feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedHomepageLinks =
                feedHomepageLinks
                  ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                  ?.filterNot { it.isBlank() }
                  .orEmpty(),
              feedIconLinks =
                feedIcons
                  ?.split(Constants.GROUP_CONCAT_SEPARATOR)
                  ?.filterNot { it.isBlank() }
                  .orEmpty(),
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
              remoteId = remoteId
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
            feedShowFavIconSettings: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            pinnedPosition: Double,
            remoteId: String? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds =
                feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedHomepageLinks =
                feedHomepageLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedIconLinks =
                feedIcons.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot { it.isBlank() },
              feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition,
              remoteId = remoteId
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
            feedShowFavIconSettings: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant?,
            remoteId: String? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds =
                feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedHomepageLinks =
                feedHomepageLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                  it.isBlank()
                },
              feedIconLinks =
                feedIcons.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot { it.isBlank() },
              feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              remoteId = remoteId,
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
          feedShowFavIconSettings: String,
          createdAt: Instant,
          updatedAt: Instant,
          pinnedAt: Instant?,
          remoteId: String? ->
          FeedGroup(
            id = id,
            name = name,
            feedIds =
              feedIds.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot { it.isBlank() },
            feedHomepageLinks =
              feedHomepageLinks.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot { it.isBlank() },
            feedIconLinks =
              feedIcons.split(Constants.GROUP_CONCAT_SEPARATOR).filterNot { it.isBlank() },
            feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
            createdAt = createdAt,
            updatedAt = updatedAt,
            pinnedAt = pinnedAt,
            remoteId = remoteId,
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
            alwaysFetchSourceArticle: Boolean,
            pinnedPosition: Double,
            numberOfUnreadPosts: Long,
            showFeedFavIcon: Boolean,
            hideFromAllFeeds: Boolean,
            isDeleted: Boolean,
            remoteId: String? ->
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
              alwaysFetchSourceArticle = alwaysFetchSourceArticle,
              pinnedPosition = pinnedPosition,
              numberOfUnreadPosts = numberOfUnreadPosts,
              showFeedFavIcon = showFeedFavIcon,
              hideFromAllFeeds = hideFromAllFeeds,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          }
        )
      }
    )
  }

  suspend fun updatedSourcePinnedPosition(sources: List<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sources.forEachIndexed { index, source ->
          feedQueries.updatedPinnedPosition(
            pinnedPosition = index.toDouble(),
            id = source.id,
            lastUpdatedAt = now
          )
          feedGroupQueries.updatedPinnedPosition(
            pinnedPosition = index.toDouble(),
            id = source.id,
            updatedAt = now
          )
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
        mapper = { count, feedHomepageLinks, feedIcons, feedShowFavIconSettings ->
          UnreadSinceLastSync(
            newArticleCount = count,
            hasNewArticles = count > 0,
            feedHomepageLinks =
              feedHomepageLinks.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                it.isBlank()
              },
            feedIcons =
              feedIcons.orEmpty().split(Constants.GROUP_CONCAT_SEPARATOR).filterNot {
                it.isBlank()
              },
            feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings)
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
