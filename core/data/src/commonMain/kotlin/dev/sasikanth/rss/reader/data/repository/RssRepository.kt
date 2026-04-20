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
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.ReadingStatistics
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSyncPerFeed
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.data.database.AppConfigQueries
import dev.sasikanth.rss.reader.data.database.BlockedWordsQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupFeedQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupQueries
import dev.sasikanth.rss.reader.data.database.FeedQueries
import dev.sasikanth.rss.reader.data.database.PostContentQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.data.database.ReadingHistoryQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.sync.ReadPostSyncEntity
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import dev.sasikanth.rss.reader.util.splitAndTrim
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.chunked
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
  private val feedGroupQueries: FeedGroupQueries,
  private val feedGroupFeedQueries: FeedGroupFeedQueries,
  private val blockedWordsQueries: BlockedWordsQueries,
  private val appConfigQueries: AppConfigQueries,
  private val readingHistoryQueries: ReadingHistoryQueries,
  private val readingTimeCalculator: ReadingTimeCalculator,
  private val widgetUpdater: WidgetUpdater,
  private val dispatchersProvider: DispatchersProvider,
  private val bookmarkRepository: BookmarkRepository,
  private val readingHistoryRepository: ReadingHistoryRepository,
  private val feedGroupRepository: FeedGroupRepository,
  private val sourceRepository: SourceRepository,
  private val postRepository: PostRepository,
  private val feedRepository: FeedRepository,
  private val syncRepository: SyncRepository,
) {
  private companion object {
    private const val POST_UPSERT_BATCH_SIZE = 200
    private const val SQLITE_BATCH_SIZE = 990
  }

  suspend fun deleteAllLocalData() {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postQueries.deleteAll()
        postContentQueries.deleteAll()
        feedGroupFeedQueries.deleteAll()
        feedQueries.deleteAll()
        feedGroupQueries.deleteAll()
        blockedWordsQueries.deleteAll()
        readingHistoryQueries.deleteAll()
        appConfigQueries.deleteAll()
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun upsertFeedWithPosts(
    feedPayload: FeedPayload,
    feedId: String? = null,
    title: String? = null,
    feedLastCleanUpAt: Instant? = null,
    alwaysFetchSourceArticle: Boolean = false,
    showWebsiteFavIcon: Boolean = true,
    enableNotifications: Boolean = true,
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
          enableNotifications = enableNotifications,
        )
      }
    }

    val feedLastCleanUpAtEpochMilli =
      feedLastCleanUpAt?.toEpochMilliseconds() ?: Instant.DISTANT_PAST.toEpochMilliseconds()

    withContext(dispatchersProvider.databaseWrite) {
      feedPayload.posts
        .filter { it.date >= feedLastCleanUpAtEpochMilli }
        .chunked(POST_UPSERT_BATCH_SIZE)
        .collect { batch -> upsertPostsBatch(batch, finalFeedId) }
    }

    widgetUpdater.updateUnreadWidget()
    return finalFeedId
  }

  private suspend fun upsertPostsBatch(posts: List<PostPayload>, feedId: String) {
    val now = Clock.System.now()
    val postsWithReadingTime =
      withContext(dispatchersProvider.default) {
        posts
          .map { postPayload ->
            async {
              val rawContentReadingTime = readingTimeCalculator.calculate(postPayload.rawContent)
              val htmlContentReadingTime =
                if (postPayload.fullContent != null) {
                  readingTimeCalculator.calculate(postPayload.fullContent)
                } else {
                  null
                }

              postPayload to (rawContentReadingTime to htmlContentReadingTime)
            }
          }
          .awaitAll()
      }

    transactionRunner.invoke {
      postsWithReadingTime.forEach { (postPayload, readingTimes) ->
        val postId = nameBasedUuidOf(postPayload.link).toString()
        postQueries.upsert(
          id = postId,
          sourceId = feedId,
          title = postPayload.title,
          description = postPayload.description,
          imageUrl = postPayload.imageUrl,
          audioUrl = postPayload.audioUrl,
          postDate = Instant.fromEpochMilliseconds(postPayload.date),
          createdAt = now,
          updatedAt = now,
          syncedAt = now,
          link = postPayload.link,
          commentsLink = postPayload.commentsLink,
          isDateParsedCorrectly = if (postPayload.isDateParsedCorrectly) 1 else 0,
          remoteId = postPayload.remoteId,
        )

        postContentQueries.upsert(
          id = postId,
          feedContent = postPayload.rawContent,
          feedContentLen = postPayload.rawContent.orEmpty().length.toLong(),
          articleContent = postPayload.fullContent,
          createdAt = now,
          feedContentReadingTime = readingTimes.first.toLong(),
          articleContentReadingTime = readingTimes.second?.toLong(),
        )
      }
    }
  }

  fun feed(feedId: String): Feed? {
    return feedQueries.feed(feedId, mapper = ::mapToFeed).executeAsOneOrNull()
  }

  suspend fun postsCountForFeed(feedId: String): Long {
    return postRepository.postsCountForFeed(feedId)
  }

  suspend fun allPostsCount(
    activeSourceIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    sessionPostIds: List<String> = emptyList(),
  ): Long? {
    return postRepository.allPostsCount(
      activeSourceIds,
      unreadOnly,
      after,
      postsUpperBound,
      sessionPostIds,
    )
  }

  fun allPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    sessionPostIds: List<String> = emptyList(),
  ): PagingSource<Int, ResolvedPost> {
    return postRepository.allPosts(
      activeSourceIds,
      postsSortOrder,
      unreadOnly,
      after,
      postsUpperBound,
      sessionPostIds,
    )
  }

  fun featuredPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    limit: Long = Constants.NUMBER_OF_FEATURED_POSTS,
    sessionPostIds: List<String> = emptyList(),
  ): Flow<List<ResolvedPost>> {
    return postRepository.featuredPosts(
      activeSourceIds,
      postsSortOrder,
      unreadOnly,
      after,
      featuredPostsAfter,
      postsUpperBound,
      limit,
      sessionPostIds,
    )
  }

  suspend fun featuredPostsBlocking(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    limit: Long = Constants.NUMBER_OF_FEATURED_POSTS,
    sessionPostIds: List<String> = emptyList(),
  ): List<ResolvedPost> {
    return postRepository.featuredPostsBlocking(
      activeSourceIds,
      postsSortOrder,
      unreadOnly,
      after,
      featuredPostsAfter,
      postsUpperBound,
      limit,
      sessionPostIds,
    )
  }

  fun nonFeaturedPosts(
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    numberOfFeaturedPosts: Long = Constants.NUMBER_OF_FEATURED_POSTS,
    sessionPostIds: List<String> = emptyList(),
  ): PagingSource<Int, ResolvedPost> {
    return postRepository.nonFeaturedPosts(
      activeSourceIds,
      postsSortOrder,
      unreadOnly,
      after,
      featuredPostsAfter,
      postsUpperBound,
      numberOfFeaturedPosts,
      sessionPostIds,
    )
  }

  suspend fun postPosition(
    postId: String,
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    sourceId: String? = null,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Int? {
    return postRepository.postPosition(
      postId,
      activeSourceIds,
      postsSortOrder,
      sourceId,
      unreadOnly,
      after,
      postsUpperBound,
    )
  }

  suspend fun nonFeaturedPostPosition(
    postId: String,
    activeSourceIds: List<String>,
    postsSortOrder: PostsSortOrder,
    sourceId: String? = null,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
    featuredPostsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    numberOfFeaturedPosts: Long = Constants.NUMBER_OF_FEATURED_POSTS,
  ): Int? {
    return postRepository.nonFeaturedPostPosition(
      postId,
      activeSourceIds,
      postsSortOrder,
      sourceId,
      unreadOnly,
      after,
      featuredPostsAfter,
      postsUpperBound,
      numberOfFeaturedPosts,
    )
  }

  suspend fun updateBookmarkStatus(bookmarked: Boolean, id: String) {
    bookmarkRepository.updateBookmarkStatus(bookmarked, id)
  }

  suspend fun updatePostReadStatus(read: Boolean, id: String, recordHistory: Boolean = true) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        postQueries.updateReadStatus(read = if (read) 1L else 0L, id = id, updatedAt = now)

        if (recordHistory) {
          if (read) {
            readingHistoryQueries.insertReadingHistoryForPosts(readAt = now, postIds = listOf(id))
          } else {
            readingHistoryQueries.deleteReadingHistory(postId = id)
          }
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun updateSeedColor(seedColor: Int, id: String) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updateSeedColor(seedColor = seedColor.toLong(), id = id)
    }
  }

  suspend fun updateSeedColors(updates: Map<String, Int>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        updates.forEach { (id, seedColor) ->
          postQueries.updateSeedColor(seedColor = seedColor.toLong(), id = id)
        }
      }
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
    bookmarkRepository.deleteBookmark(id)
  }

  suspend fun allBookmarkIdsBlocking(): List<String> {
    return bookmarkRepository.allBookmarkIdsBlocking()
  }

  fun allFeeds(): Flow<List<Feed>> {
    return feedRepository.allFeeds()
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return feedRepository.allFeedsBlocking()
  }

  suspend fun allFeedGroupsBlocking(): List<FeedGroup> {
    return feedGroupRepository.allFeedGroupsBlocking()
  }

  fun numberOfFeeds(): Flow<Long> {
    return feedRepository.numberOfFeeds()
  }

  /** Search feeds, returns all feeds if [searchQuery] is empty */
  fun searchFeed(
    searchQuery: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
  ): PagingSource<Int, Feed> {
    return feedRepository.searchFeed(searchQuery, postsAfter)
  }

  fun feed(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Feed> {
    return feedRepository.feedWithUnreadCount(feedId, postsAfter, postsUpperBound)
  }

  suspend fun feedBlocking(
    feedId: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Feed {
    return feedRepository.feedWithUnreadCountBlocking(feedId, postsAfter, postsUpperBound)
  }

  suspend fun removeFeed(feedId: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.transaction {
        feedQueries.markAsDeleted(id = feedId, lastUpdatedAt = Clock.System.now())
        postQueries.deletePostsForFeed(feedId)
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  private fun mapToFeedShowFavIconSettings(feedShowFavIconSettings: String?): List<Boolean> {
    return feedShowFavIconSettings
      ?.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR)
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
    feedRepository.updateFeedName(newFeedName, feedId)
  }

  suspend fun updateFeedLastUpdatedAt(feedId: String, lastUpdatedAt: Instant) {
    feedRepository.updateFeedLastUpdatedAt(feedId, lastUpdatedAt)
  }

  suspend fun updateFeedGroupUpdatedAt(groupId: String, updatedAt: Instant) {
    feedGroupRepository.updateFeedGroupUpdatedAt(groupId, updatedAt)
  }

  suspend fun updateFeedRefreshInterval(feedId: String, refreshInterval: Duration) {
    feedRepository.updateFeedRefreshInterval(feedId, refreshInterval)
  }

  fun search(
    searchQuery: String,
    sortOrder: SearchSortOrder,
    sourceIds: List<String> = emptyList(),
    onlyBookmarked: Boolean = false,
    onlyUnread: Boolean = false,
    sessionPostIds: List<String> = emptyList(),
  ): PagingSource<Int, ResolvedPost> {
    val sanitizedSearchQuery = sanitizeSearchQuery(searchQuery)

    return QueryPagingSource(
      countQuery =
        postSearchFTSQueries.countSearchResults(
          searchQuery = sanitizedSearchQuery,
          isSourceIdsEmpty = sourceIds.isEmpty(),
          sourceIds = sourceIds,
          onlyBookmarked = if (onlyBookmarked) 1L else 0L,
          onlyUnread = if (onlyUnread) 1L else 0L,
          sessionPostIds = sessionPostIds,
        ),
      transacter = postSearchFTSQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        postSearchFTSQueries.search(
          searchQuery = sanitizedSearchQuery,
          sortOrder = sortOrder.value,
          isSourceIdsEmpty = sourceIds.isEmpty(),
          sourceIds = sourceIds,
          onlyBookmarked = if (onlyBookmarked) 1L else 0L,
          onlyUnread = if (onlyUnread) 1L else 0L,
          limit = limit,
          offset = offset,
          sessionPostIds = sessionPostIds,
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
            feedName: String,
            feedIcon: String,
            feedHomepageLink: String,
            alwaysFetchSourceArticle: Boolean,
            showFeedFavIcon: Boolean,
            feedContentReadingTime: Long?,
            articleContentReadingTime: Long?,
            seedColor: Long?,
            audioProgress: Long,
            audioDuration: Long ->
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
              audioProgress = audioProgress,
              audioDuration = audioDuration,
            )
          },
        )
      },
    )
  }

  fun bookmarks(): PagingSource<Int, ResolvedPost> {
    return bookmarkRepository.bookmarks()
  }

  suspend fun hasFeed(id: String): Boolean {
    return feedRepository.hasFeed(id)
  }

  suspend fun hasPost(id: String): Boolean {
    return postRepository.hasPost(id)
  }

  suspend fun toggleSourcePinStatus(source: Source) {
    val now =
      if (source.pinnedAt == null) {
        Clock.System.now()
      } else {
        null
      }

    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedQueries.updatePinnedAt(
          pinnedAt = now,
          id = source.id,
          lastUpdatedAt = Clock.System.now(),
        )
        feedGroupQueries.updatePinnedAt(
          pinnedAt = now,
          id = source.id,
          updatedAt = Clock.System.now(),
        )
      }
    }
  }

  fun hasFeeds(): Flow<Boolean> {
    return feedRepository.hasFeeds()
  }

  /** @return list of feeds from which posts are deleted from */
  suspend fun deleteReadPosts(before: Instant): List<String> {
    val results =
      withContext(dispatchersProvider.databaseWrite) {
        postQueries.transactionWithResult {
          postQueries.deleteReadPosts(before = before).executeAsList()
        }
      }

    widgetUpdater.updateUnreadWidget()
    return results.distinct()
  }

  suspend fun updateFeedsLastCleanUpAt(
    feedIds: List<String>,
    lastCleanUpAt: Instant = Clock.System.now(),
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
      transactionRunner.invoke {
        val now = Clock.System.now()
        val postIds =
          postQueries.markAllPostsAsRead(after = postsAfter, updatedAt = now).executeAsList()
        postIds.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
          readingHistoryQueries.insertReadingHistoryForPosts(readAt = now, postIds = chunk)
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun markPostsAsRead(postIds: Set<String>) {
    updatePostReadStatus(postIds, read = true)
  }

  suspend fun updatePostReadStatus(
    postIds: Set<String>,
    read: Boolean,
    recordHistory: Boolean = true,
  ) {
    val postIdsSnapshot = postIds.toList()
    val now = Clock.System.now()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIdsSnapshot.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
          postQueries.updatePostsReadStatus(
            read = if (read) 1L else 0L,
            updatedAt = now,
            ids = chunk,
          )

          if (recordHistory) {
            if (read) {
              readingHistoryQueries.insertReadingHistoryForPosts(readAt = now, postIds = chunk)
            } else {
              readingHistoryQueries.deleteReadingHistoryForPosts(postIds = chunk)
            }
          }
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun updateBookmarkStatus(postIds: Set<String>, bookmarked: Boolean) {
    bookmarkRepository.updateBookmarkStatus(postIds, bookmarked)
  }

  suspend fun markPostsInFeedAsRead(
    feedIds: List<String>,
    postsAfter: Instant = Instant.DISTANT_PAST,
  ) {
    val feedIdsSnapshot = feedIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        feedIdsSnapshot.forEach { feedId ->
          val postIds =
            postQueries
              .markPostsAsReadForFeed(sourceId = feedId, after = postsAfter, updatedAt = now)
              .executeAsList()
          postIds.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
            readingHistoryQueries.insertReadingHistoryForPosts(readAt = now, postIds = chunk)
          }
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun post(postId: String): Post {
    return postRepository.post(postId)
  }

  suspend fun postOrNull(postId: String): Post? {
    return postRepository.postOrNull(postId)
  }

  suspend fun resolvedPostById(postId: String): ResolvedPost? {
    return postRepository.resolvedPostById(postId)
  }

  suspend fun updateFeedRemoteId(
    remoteId: String,
    feedId: String,
    lastUpdatedAt: Instant = Clock.System.now(),
  ) {
    syncRepository.updateFeedRemoteId(remoteId, feedId, lastUpdatedAt)
  }

  suspend fun updatePostRemoteId(remoteId: String, postId: String) {
    syncRepository.updatePostRemoteId(remoteId, postId)
  }

  suspend fun updatePostSyncedAt(postId: String, syncedAt: Instant) {
    syncRepository.updatePostSyncedAt(postId, syncedAt)
  }

  suspend fun updatePostSyncedAt(postIds: Set<String>, syncedAt: Instant) {
    syncRepository.updatePostSyncedAt(postIds, syncedAt)
  }

  suspend fun updatePostSyncedAt(posts: List<Post>) {
    syncRepository.updatePostSyncedAt(posts)
  }

  suspend fun updateFeedGroupRemoteId(
    remoteId: String,
    groupId: String,
    updatedAt: Instant = Clock.System.now(),
  ) {
    feedGroupRepository.updateFeedGroupRemoteId(remoteId, groupId, updatedAt)
  }

  suspend fun feedGroupByRemoteId(remoteId: String): FeedGroup? {
    return feedGroupRepository.feedGroupByRemoteId(remoteId)
  }

  suspend fun latestPostRemoteId(): String? {
    return syncRepository.latestPostRemoteId()
  }

  suspend fun latestPostRemoteIdForFeed(feedId: String): String? {
    return syncRepository.latestPostRemoteIdForFeed(feedId)
  }

  suspend fun postsWithRemoteId(): List<Post> {
    return syncRepository.postsWithRemoteId()
  }

  suspend fun postsWithRemoteIdPaged(limit: Long, offset: Long): List<Post> {
    return syncRepository.postsWithRemoteIdPaged(limit, offset)
  }

  suspend fun postsWithLocalChanges(): List<Post> {
    return syncRepository.postsWithLocalChanges()
  }

  suspend fun postsWithLocalChangesPaged(limit: Long, offset: Long): List<Post> {
    return syncRepository.postsWithLocalChangesPaged(limit, offset)
  }

  suspend fun postsWithLocalChangesForFeed(feedId: String): List<Post> {
    return syncRepository.postsWithLocalChangesForFeed(feedId)
  }

  suspend fun postsWithLocalChangesForFeedPaged(
    feedId: String,
    limit: Long,
    offset: Long,
  ): List<Post> {
    return syncRepository.postsWithLocalChangesForFeedPaged(feedId, limit, offset)
  }

  suspend fun postByRemoteId(remoteId: String): Post? {
    return syncRepository.postByRemoteId(remoteId)
  }

  suspend fun postsByRemoteIds(remoteIds: Set<String>): List<Post> {
    return syncRepository.postsByRemoteIds(remoteIds)
  }

  suspend fun postsWithImagesAndNoSeedColor(limit: Long): List<ResolvedPost> {
    return postRepository.postsWithImagesAndNoSeedColor(limit)
  }

  suspend fun postByLink(link: String): Post? {
    return postRepository.postByLink(link)
  }

  suspend fun postsByLinks(links: Set<String>): List<Post> {
    return postRepository.postsByLinks(links)
  }

  fun feedByRemoteId(remoteId: String): Feed? {
    return syncRepository.feedByRemoteId(remoteId)
  }

  fun feedsByRemoteIds(remoteIds: Set<String>): List<Feed> {
    return syncRepository.feedsByRemoteIds(remoteIds)
  }

  suspend fun upsertPosts(posts: List<Post>) {
    syncRepository.upsertPosts(posts)
  }

  suspend fun upsertFeeds(feeds: List<Feed>) {
    syncRepository.upsertFeeds(feeds)
  }

  suspend fun upsertGroup(
    id: String,
    name: String,
    pinnedAt: Instant?,
    updatedAt: Instant,
    isDeleted: Boolean,
    remoteId: String? = null,
  ) {
    feedGroupRepository.upsertGroup(id, name, pinnedAt, updatedAt, isDeleted, remoteId)
  }

  suspend fun feedGroupBlocking(id: String): FeedGroup? {
    return feedGroupRepository.feedGroupBlocking(id)
  }

  suspend fun replaceFeedsInGroup(groupId: String, feedIds: List<String>) {
    feedGroupRepository.replaceFeedsInGroup(groupId, feedIds)
  }

  suspend fun deleteReadPostsForFeedOlderThan(feedId: String, before: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.deleteReadPostsForFeed(feedId = feedId, before = before)
    }
  }

  suspend fun updateFeedAlwaysFetchSource(feedId: String, newValue: Boolean) {
    feedRepository.updateFeedAlwaysFetchSource(feedId, newValue)
  }

  suspend fun updateFeedShowFavIcon(feedId: String, newValue: Boolean) {
    feedRepository.updateFeedShowFavIcon(feedId, newValue)
  }

  suspend fun updateFeedEnableNotifications(feedId: String, newValue: Boolean) {
    feedRepository.updateFeedEnableNotifications(feedId, newValue)
  }

  suspend fun disableNotificationsForFeeds() {
    feedRepository.disableNotificationsForFeeds()
  }

  suspend fun updateFeedHideFromAllFeeds(feedId: String, newValue: Boolean) {
    withContext(dispatchersProvider.databaseWrite) {
      feedQueries.updateHideFromAllFeeds(
        hideFromAllFeeds = newValue,
        lastUpdatedAt = Clock.System.now(),
        id = feedId,
      )
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun createGroup(name: String): String {
    return feedGroupRepository.createGroup(name)
  }

  suspend fun updateGroupName(groupId: String, name: String) {
    feedGroupRepository.updateGroupName(groupId, name)
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
    return feedGroupRepository.groupIdsForFeed(feedId)
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
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun deleteSources(sources: Set<Source>) {
    val sourcesSnapshot = sources.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        sourcesSnapshot.forEach { source ->
          when (source.sourceType) {
            SourceType.Feed -> {
              postQueries.deletePostsForFeed(source.id)
              val postsCount = postQueries.countPostsForFeed(source.id).executeAsOne()
              if (postsCount == 0L) {
                feedQueries.remove(id = source.id)
              }
            }
            SourceType.FeedGroup -> {
              feedGroupQueries.remove(id = source.id)
            }
          }
        }
      }
    }
    widgetUpdater.updateUnreadWidget()
  }

  fun pinnedSources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<List<Source>> {
    return sourceRepository.pinnedSources(postsAfter, postsUpperBound)
  }

  fun sources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Source> {
    return sourceRepository.sources(postsAfter, postsUpperBound, orderBy)
  }

  fun source(
    id: String,
    postsAfter: Instant = Instant.DISTANT_PAST,
    postsUpperBound: Instant = Instant.DISTANT_FUTURE,
  ): Flow<Source?> {
    return sourceRepository.source(id, postsAfter, postsUpperBound)
  }

  fun allGroups(): PagingSource<Int, FeedGroup> {
    return feedGroupRepository.allGroups()
  }

  fun numberOfFeedGroups(): Flow<Long> {
    return feedGroupRepository.numberOfFeedGroups()
  }

  suspend fun groupByIds(ids: Set<String>): List<FeedGroup> {
    return feedGroupRepository.groupByIds(ids)
  }

  fun groupById(groupId: String): Flow<FeedGroup> {
    return feedGroupRepository.groupById(groupId)
  }

  fun feedsInGroup(
    feedIds: List<String>,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Feed> {
    return feedRepository.feedsInGroup(feedIds, orderBy)
  }

  suspend fun updatedSourcePinnedPosition(sources: List<Source>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        val now = Clock.System.now()
        sources.forEachIndexed { index, source ->
          feedQueries.updatedPinnedPosition(
            pinnedPosition = index.toDouble(),
            id = source.id,
            lastUpdatedAt = now,
          )
          feedGroupQueries.updatedPinnedPosition(
            pinnedPosition = index.toDouble(),
            id = source.id,
            updatedAt = now,
          )
        }
      }
    }
  }

  fun hasUnreadPostsInSource(
    activeSourceIds: List<String>,
    postsAfter: Instant = Instant.DISTANT_PAST,
  ): Flow<Boolean> {
    return postQueries
      .unreadPostsCountInSource(
        isSourceIdsEmpty = activeSourceIds.isEmpty(),
        sourceIds = activeSourceIds,
        after = postsAfter,
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
      .map { it > 0 }
  }

  fun unreadSinceLastSync(
    sources: List<String>,
    postsAfter: Instant,
    postsUpperBound: Instant,
  ): Flow<UnreadSinceLastSync> {
    return postQueries
      .unreadSinceLastSync(
        isSourceIdsEmpty = sources.isEmpty(),
        sourceIds = sources,
        postsAfter = postsAfter,
        postsUpperBound = postsUpperBound,
        mapper = { count, feedHomepageLinks, feedIcons, feedShowFavIconSettings ->
          UnreadSinceLastSync(
            newArticleCount = count,
            hasNewArticles = count > 0,
            feedHomepageLinks =
              feedHomepageLinks.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
            feedIcons = feedIcons.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
            feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
          )
        },
      )
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
  }

  fun unreadSinceLastSyncPerFeed(
    sources: List<String>,
    postsAfter: Instant,
    postsUpperBound: Instant,
  ): Flow<List<UnreadSinceLastSyncPerFeed>> {
    return postQueries
      .unreadSinceLastSyncPerFeed(
        isSourceIdsEmpty = sources.isEmpty(),
        sourceIds = sources,
        postsAfter = postsAfter,
        postsUpperBound = postsUpperBound,
        mapper = { feedId, feedName, count, feedHomepageLink, feedIcon, showFeedFavIconStr ->
          UnreadSinceLastSyncPerFeed(
            feedId = feedId,
            feedName = feedName,
            newArticleCount = count,
            feedHomepageLink = feedHomepageLink,
            feedIcon = feedIcon,
            showFeedFavIcon =
              when (showFeedFavIconStr) {
                "true" -> true
                "false" -> false
                else -> true
              },
          )
        },
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  suspend fun getReadingStatistics(startDate: Instant): Flow<ReadingStatistics> {
    return readingHistoryRepository.getReadingStatistics(startDate)
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
    enableNotifications: Boolean,
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
      enableNotifications = enableNotifications,
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
    enableNotifications: Boolean,
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
      enableNotifications = enableNotifications,
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
    enableNotifications: Boolean,
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
      enableNotifications = enableNotifications,
    )
  }

  private fun mapToResolvedPost(
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
    alwaysFetchFullArticle: Boolean,
    showFeedFavIcon: Boolean,
    feedContentReadingTime: Long?,
    articleContentReadingTime: Long?,
    seedColor: Long?,
    audioProgress: Long,
    audioDuration: Long,
  ): ResolvedPost {
    return ResolvedPost(
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
      alwaysFetchFullArticle = alwaysFetchFullArticle,
      showFeedFavIcon = showFeedFavIcon,
      feedContentReadingTime = feedContentReadingTime?.toInt(),
      articleContentReadingTime = articleContentReadingTime?.toInt(),
      seedColor = seedColor?.toInt(),
      remoteId = remoteId,
      audioProgress = audioProgress,
      audioDuration = audioDuration,
    )
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace("\"", "\"\"").run { "\"$this\"" }
  }
}
