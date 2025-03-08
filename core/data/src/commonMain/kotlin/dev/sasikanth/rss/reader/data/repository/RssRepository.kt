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
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.data.database.BookmarkQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupFeedQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupQueries
import dev.sasikanth.rss.reader.data.database.FeedQueries
import dev.sasikanth.rss.reader.data.database.FeedSearchFTSQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.data.database.SourceQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class RssRepository(
  private val feedFetcher: FeedFetcher,
  private val transactionRunner: TransactionRunner,
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val postSearchFTSQueries: PostSearchFTSQueries,
  private val bookmarkQueries: BookmarkQueries,
  private val feedSearchFTSQueries: FeedSearchFTSQueries,
  private val feedGroupQueries: FeedGroupQueries,
  private val feedGroupFeedQueries: FeedGroupFeedQueries,
  private val sourceQueries: SourceQueries,
  private val dispatchersProvider: DispatchersProvider
) {

  companion object {
    private const val NUMBER_OF_FEATURED_POSTS = 6L
    private const val UPDATE_CHUNKS = 6
  }

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

              feedPayload.posts.forEach { post ->
                if (post.date < feedLastCleanUpAtEpochMilli) return@forEach

                postQueries.upsert(
                  id = nameBasedUuidOf(post.link).toString(),
                  sourceId = feedId,
                  title = post.title,
                  description = post.description,
                  imageUrl = post.imageUrl,
                  date = Instant.fromEpochMilliseconds(post.date),
                  link = post.link,
                  commnetsLink = post.commentsLink,
                  rawContent = post.rawContent,
                  isDateParsedCorrectly = post.isDateParsedCorrectly,
                )
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
        FeedAddResult.NetworkError(feedFetchResult.exception)
      }
      FeedFetchResult.TooManyRedirects -> {
        FeedAddResult.TooManyRedirects
      }
    }
  }

  suspend fun updateFeeds() {
    withContext(dispatchersProvider.io) {
      val feedsChunks = allFeedsBlocking().chunked(UPDATE_CHUNKS)

      feedsChunks.forEach { feeds ->
        val jobs =
          feeds.map { feed ->
            launch { fetchAndAddFeed(feedLink = feed.link, feedLastCleanUpAt = feed.lastCleanUpAt) }
          }
        jobs.joinAll()

        delay(1.seconds)
      }
    }
  }

  suspend fun updateFeed(selectedFeedId: String) {
    val feed =
      withContext(dispatchersProvider.databaseRead) {
        feedQueries.feed(selectedFeedId).executeAsOneOrNull()
      }

    if (feed != null) {
      fetchAndAddFeed(feedLink = feed.link, feedLastCleanUpAt = feed.lastCleanUpAt)
    }
  }

  suspend fun updateGroup(feedIds: List<String>) {
    feedIds.forEach { feedId ->
      val feed =
        withContext(dispatchersProvider.databaseRead) {
          feedQueries.feed(feedId).executeAsOneOrNull()
        }

      if (feed != null) {
        fetchAndAddFeed(feedLink = feed.link, feedLastCleanUpAt = feed.lastCleanUpAt)
      }
    }
  }

  fun featuredPosts(
    activeSourceIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST
  ): Flow<List<PostWithMetadata>> {
    return postQueries
      .featuredPosts(
        isSourceIdsEmpty = activeSourceIds.isEmpty(),
        sourceIds = activeSourceIds,
        unreadOnly = unreadOnly,
        postsAfter = after,
        limit = NUMBER_OF_FEATURED_POSTS,
        mapper = ::PostWithMetadata
      )
      .asFlow()
      .mapToList(dispatchersProvider.databaseRead)
  }

  fun posts(
    activeSourceIds: List<String>,
    featuredPostsIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
  ): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery =
        postQueries.count(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          featuredPosts = featuredPostsIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
        ),
      transacter = postQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        postQueries.posts(
          isSourceIdsEmpty = activeSourceIds.isEmpty(),
          sourceIds = activeSourceIds,
          featuredPosts = featuredPostsIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
          limit = limit,
          offset = offset,
          mapper = ::PostWithMetadata
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

  fun feed(feedId: String, postsAfter: Instant = Instant.DISTANT_PAST): Flow<Feed> {
    return feedQueries
      .feedWithUnreadPostsCount(
        id = feedId,
        postsAfter = postsAfter,
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

  suspend fun feedBlocking(feedId: String, postsAfter: Instant = Instant.DISTANT_PAST): Feed {
    return withContext(dispatchersProvider.databaseRead) {
      feedQueries
        .feedWithUnreadPostsCount(
          id = feedId,
          postsAfter = postsAfter,
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
        bookmarkQueries.bookmarks(limit, offset, ::PostWithMetadata)
      }
    )
  }

  suspend fun hasPost(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) { postQueries.hasPost(id).executeAsOne() }
  }

  suspend fun hasFeed(id: String): Boolean {
    return withContext(dispatchersProvider.databaseRead) { feedQueries.hasFeed(id).executeAsOne() }
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

  fun pinnedSources(postsAfter: Instant = Instant.DISTANT_PAST): Flow<List<Source>> {
    return sourceQueries
      .pinnedSources(
        postsAfter = postsAfter,
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
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Source> {
    return QueryPagingSource(
      countQuery = sourceQueries.sourcesCount(),
      transacter = sourceQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        sourceQueries.sources(
          postsAfter = postsAfter,
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

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace(Regex.fromLiteral("\""), "\"\"").run { "\"$this\"" }
  }
}
