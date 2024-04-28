/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.repository

import app.cash.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import com.benasher44.uuid.uuid4
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.database.BookmarkQueries
import dev.sasikanth.rss.reader.database.FeedGroupQueries
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.FeedSearchFTSQueries
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.database.SourceQueries
import dev.sasikanth.rss.reader.database.TransactionRunner
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.search.SearchSortOrder
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.nameBasedUuidOf
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
  private val sourceQueries: SourceQueries,
  dispatchersProvider: DispatchersProvider
) {

  companion object {
    private const val NUMBER_OF_FEATURED_POSTS = 6L
    private const val UPDATE_CHUNKS = 20
  }

  private val ioDispatcher = dispatchersProvider.io

  suspend fun addFeed(
    feedLink: String,
    title: String? = null,
    feedLastCleanUpAt: Instant? = null,
    transformUrl: Boolean = true,
  ): FeedAddResult {
    return withContext(ioDispatcher) {
      when (val feedFetchResult = feedFetcher.fetch(url = feedLink, transformUrl = transformUrl)) {
        is FeedFetchResult.Success -> {
          return@withContext try {
            val feedPayload = feedFetchResult.feedPayload
            val feedId = nameBasedUuidOf(feedPayload.link).toString()
            val name = if (title.isNullOrBlank()) feedPayload.name else title

            feedQueries.upsert(
              name = name,
              icon = feedPayload.icon,
              description = feedPayload.description,
              homepageLink = feedPayload.homepageLink,
              createdAt = Clock.System.now(),
              link = feedPayload.link,
              id = feedId
            )

            postQueries.transaction {
              val feedLastCleanUpAtEpochMilli =
                feedLastCleanUpAt?.toEpochMilliseconds()
                  ?: Instant.DISTANT_PAST.toEpochMilliseconds()

              feedPayload.posts.forEach { post ->
                if (post.date > feedLastCleanUpAtEpochMilli) {
                  postQueries.upsert(
                    id = nameBasedUuidOf(post.link).toString(),
                    sourceId = feedId,
                    title = post.title,
                    description = post.description,
                    imageUrl = post.imageUrl,
                    date = Instant.fromEpochMilliseconds(post.date),
                    link = post.link,
                    commnetsLink = post.commentsLink,
                    rawContent = post.rawContent
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
          return@withContext FeedAddResult.HttpStatusError(feedFetchResult.statusCode)
        }
        is FeedFetchResult.Error -> {
          return@withContext FeedAddResult.NetworkError(feedFetchResult.exception)
        }
        FeedFetchResult.TooManyRedirects -> {
          return@withContext FeedAddResult.TooManyRedirects
        }
      }
    }
  }

  suspend fun updateFeeds() {
    val results =
      withContext(ioDispatcher) {
        val feedsChunk = feedQueries.feeds().executeAsList().chunked(UPDATE_CHUNKS)
        feedsChunk.map { feeds ->
          feeds.map { feed ->
            launch {
              addFeed(
                feedLink = feed.link,
                transformUrl = false,
                feedLastCleanUpAt = feed.lastCleanUpAt
              )
            }
          }
        }
      }

    results.flatten().joinAll()
  }

  suspend fun updateFeed(selectedFeedId: String) {
    withContext(ioDispatcher) {
      val feed = feedQueries.feed(selectedFeedId).executeAsOneOrNull()
      if (feed != null) {
        addFeed(feedLink = feed.link, transformUrl = false, feedLastCleanUpAt = feed.lastCleanUpAt)
      }
    }
  }

  suspend fun updateGroup(feedIds: List<String>) {
    withContext(ioDispatcher) {
      feedIds.forEach { feedId ->
        val feed = feedQueries.feed(feedId).executeAsOneOrNull()
        if (feed != null) {
          addFeed(
            feedLink = feed.link,
            transformUrl = false,
            feedLastCleanUpAt = feed.lastCleanUpAt
          )
        }
      }
    }
  }

  fun featuredPosts(
    selectedFeedId: String?,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST
  ): Flow<List<PostWithMetadata>> {
    return postQueries
      .featuredPosts(
        sourceId = selectedFeedId,
        unreadOnly = unreadOnly,
        postsAfter = after,
        limit = NUMBER_OF_FEATURED_POSTS,
        mapper = ::PostWithMetadata
      )
      .asFlow()
      .mapToList(ioDispatcher)
  }

  fun posts(
    selectedFeedId: String?,
    featuredPostsIds: List<String>,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST,
  ): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery =
        postQueries.count(
          sourceId = selectedFeedId,
          featuredPosts = featuredPostsIds,
          unreadOnly = unreadOnly,
          postsAfter = after,
        ),
      transacter = postQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        postQueries.posts(
          sourceId = selectedFeedId,
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
    withContext(ioDispatcher) { postQueries.updateBookmarkStatus(bookmarked = bookmarked, id = id) }
  }

  suspend fun updatePostReadStatus(read: Boolean, id: String) {
    withContext(ioDispatcher) { postQueries.updateReadStatus(read, id) }
  }

  suspend fun deleteBookmark(id: String) {
    withContext(ioDispatcher) { bookmarkQueries.deleteBookmark(id) }
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(ioDispatcher) {
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
            alwaysFetchSourceArticle: Boolean ->
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
              alwaysFetchSourceArticle = alwaysFetchSourceArticle
            )
          }
        )
        .executeAsList()
    }
  }

  fun numberOfFeeds(): Flow<Long> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(ioDispatcher)
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
      context = ioDispatcher,
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

  suspend fun feed(feedId: String, postsAfter: Instant = Instant.DISTANT_PAST): Flow<Feed> {
    return withContext(ioDispatcher) {
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
            numberOfUnreadPosts: Long ->
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
              numberOfUnreadPosts = numberOfUnreadPosts
            )
          }
        )
        .asFlow()
        .mapToOne(ioDispatcher)
    }
  }

  suspend fun feedBlocking(feedId: String, postsAfter: Instant = Instant.DISTANT_PAST): Feed {
    return withContext(ioDispatcher) {
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
            numberOfUnreadPosts: Long ->
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
              numberOfUnreadPosts = numberOfUnreadPosts
            )
          }
        )
        .executeAsOne()
    }
  }

  suspend fun removeFeed(feedId: String) {
    withContext(ioDispatcher) {
      transactionRunner.invoke {
        feedQueries.remove(feedId)
        val feedGroups = feedGroupQueries.groupByFeedId(feedId).executeAsList()
        feedGroups.forEach { feedGroup ->
          feedGroupQueries.updateFeedIds(
            feedIds = feedGroup.feedIds - setOf(feedId),
            id = feedGroup.id
          )
        }
      }
    }
  }

  suspend fun updateFeedName(newFeedName: String, feedId: String) {
    withContext(ioDispatcher) { feedQueries.updateFeedName(newFeedName, feedId) }
  }

  fun search(searchQuery: String, sortOrder: SearchSortOrder): PagingSource<Int, PostWithMetadata> {
    val sanitizedSearchQuery = sanitizeSearchQuery(searchQuery)

    return QueryPagingSource(
      countQuery = postSearchFTSQueries.countSearchResults(sanitizedSearchQuery),
      transacter = postSearchFTSQueries,
      context = ioDispatcher,
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
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        bookmarkQueries.bookmarks(limit, offset, ::PostWithMetadata)
      }
    )
  }

  suspend fun hasPost(id: String): Boolean {
    return withContext(ioDispatcher) { postQueries.hasPost(id).executeAsOne() }
  }

  suspend fun hasFeed(id: String): Boolean {
    return withContext(ioDispatcher) { feedQueries.hasFeed(id).executeAsOne() }
  }

  suspend fun toggleFeedPinStatus(feed: Feed) {
    val now =
      if (feed.pinnedAt == null) {
        Clock.System.now()
      } else {
        null
      }
    withContext(ioDispatcher) { feedQueries.updatePinnedAt(pinnedAt = now, id = feed.id) }
  }

  fun hasFeeds(): Flow<Boolean> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(ioDispatcher).map { it > 0 }
  }

  /** @return list of feeds from which posts are deleted from */
  suspend fun deleteReadPosts(before: Instant): List<String> {
    return withContext(ioDispatcher) {
        postQueries.transactionWithResult {
          postQueries.deleteReadPosts(before = before).executeAsList()
        }
      }
      .distinct()
  }

  suspend fun updateFeedsLastCleanUpAt(feedIds: List<String>) {
    withContext(ioDispatcher) {
      feedQueries.transaction {
        feedIds.forEach { feedId ->
          feedQueries.updateLastCleanUpAt(lastCleanUpAt = Clock.System.now(), id = feedId)
        }
      }
    }
  }

  suspend fun markPostsInFeedAsRead(feedId: String, postsAfter: Instant = Instant.DISTANT_PAST) {
    withContext(ioDispatcher) { postQueries.markPostsInFeedAsRead(feedId, postsAfter) }
  }

  suspend fun post(postId: String): Post {
    return withContext(ioDispatcher) { postQueries.post(postId, ::Post).executeAsOne() }
  }

  suspend fun updateFeedAlwaysFetchSource(feedId: String, newValue: Boolean) {
    return withContext(ioDispatcher) {
      feedQueries.updateAlwaysFetchSourceArticle(newValue, feedId)
    }
  }

  suspend fun createGroup(name: String) {
    withContext(ioDispatcher) {
      feedGroupQueries.createGroup(
        id = uuid4().toString(),
        name = name,
        feedIds = emptyList(),
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now()
      )
    }
  }

  suspend fun updateGroupName(groupId: String, name: String) {
    withContext(ioDispatcher) { feedGroupQueries.updateGroupName(name, groupId) }
  }

  suspend fun addFeedIdsToGroups(groupIds: Set<String>, feedIds: List<String>) {
    withContext(ioDispatcher) {
      transactionRunner.invoke {
        groupIds.forEach { groupId ->
          val group = feedGroupQueries.group(groupId).executeAsOne()

          feedGroupQueries.updateFeedIds(id = groupId, feedIds = group.feedIds + feedIds)
        }
      }
    }
  }

  suspend fun pinSources(sources: Set<Source>) {
    withContext(ioDispatcher) {
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
    withContext(ioDispatcher) {
      transactionRunner.invoke {
        sources.forEach { source ->
          feedQueries.updatePinnedAt(id = source.id, pinnedAt = null)
          feedGroupQueries.updatePinnedAt(id = source.id, pinnedAt = null)
        }
      }
    }
  }

  suspend fun deleteSources(sources: Set<Source>) {
    withContext(ioDispatcher) {
      transactionRunner.invoke {
        sources.forEach { source ->
          feedQueries.remove(id = source.id)
          feedGroupQueries.deleteGroup(id = source.id)

          if (source is Feed) {
            val feedGroups = feedGroupQueries.groupByFeedId(feedId = source.id).executeAsList()
            feedGroups.forEach { feedGroup ->
              feedGroupQueries.updateFeedIds(
                feedIds = feedGroup.feedIds - setOf(source.id),
                id = feedGroup.id
              )
            }
          }
        }
      }
    }
  }

  fun pinnedSources(postsAfter: Instant = Instant.DISTANT_PAST): PagingSource<Int, Source> {
    return QueryPagingSource(
      countQuery = sourceQueries.pinnedSourcesCount(),
      transacter = sourceQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        sourceQueries.pinnedSources(
          postsAfter = postsAfter,
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
            createdAt: Instant?,
            pinnedAt: Instant?,
            lastCleanUpAt: Instant?,
            numberOfUnreadPosts: Long,
            feedIds: List<String>?,
            feedIcons: String?,
            updatedAt: Instant? ->
            if (type == "group") {
              FeedGroup(
                id = id,
                name = name,
                feedIds = feedIds?.filterNot { it.isBlank() }.orEmpty(),
                feedIcons = feedIcons?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
                createdAt = createdAt!!,
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
                createdAt = createdAt!!,
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

  fun sources(
    postsAfter: Instant = Instant.DISTANT_PAST,
    orderBy: FeedsOrderBy = FeedsOrderBy.Latest,
  ): PagingSource<Int, Source> {
    return QueryPagingSource(
      countQuery = sourceQueries.sourcesCount(),
      transacter = sourceQueries,
      context = ioDispatcher,
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
            feedIds: List<String>?,
            feedIcons: String?,
            updatedAt: Instant? ->
            if (type == "group") {
              FeedGroup(
                id = id,
                name = name,
                feedIds = feedIds?.filterNot { it.isBlank() }.orEmpty(),
                feedIcons = feedIcons?.split(",")?.filterNot { it.isBlank() }.orEmpty(),
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
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        feedGroupQueries.groups(
          limit = limit,
          offset = offset,
          mapper = {
            id: String,
            name: String,
            feedIds: List<String>,
            feedIcons: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.filterNot { it.isBlank() },
              feedIcons = feedIcons.split(",").filterNot { it.isBlank() },
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
            )
          }
        )
      }
    )
  }

  fun numberOfFeedGroups(): Flow<Long> {
    return feedGroupQueries.count().asFlow().mapToOne(ioDispatcher)
  }

  suspend fun groupByIds(ids: Set<String>): List<FeedGroup> {
    return withContext(ioDispatcher) {
      feedGroupQueries
        .groupsByIds(
          ids = ids,
          mapper = {
            id: String,
            name: String,
            feedIds: List<String>,
            feedIcons: String,
            createdAt: Instant,
            updatedAt: Instant,
            pinnedAt: Instant? ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = feedIds.filterNot { it.isBlank() },
              feedIcons = feedIcons.split(",").filterNot { it.isBlank() },
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
            )
          }
        )
        .executeAsList()
    }
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace(Regex.fromLiteral("\""), "\"\"").run { "\"$this\"" }
  }
}
