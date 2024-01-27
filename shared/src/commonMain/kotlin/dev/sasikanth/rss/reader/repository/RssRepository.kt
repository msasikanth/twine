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
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetchResult
import dev.sasikanth.rss.reader.core.network.fetcher.FeedFetcher
import dev.sasikanth.rss.reader.database.BookmarkQueries
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.FeedSearchFTSQueries
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.search.SearchSortOrder
import dev.sasikanth.rss.reader.util.DispatchersProvider
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
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val postSearchFTSQueries: PostSearchFTSQueries,
  private val bookmarkQueries: BookmarkQueries,
  private val feedSearchFTSQueries: FeedSearchFTSQueries,
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
            feedQueries.upsert(
              name = title ?: feedPayload.name,
              icon = feedPayload.icon,
              description = feedPayload.description,
              homepageLink = feedPayload.homepageLink,
              createdAt = Clock.System.now(),
              link = feedPayload.link
            )

            postQueries.transaction {
              val feedLastCleanUpAtEpochMilli =
                feedLastCleanUpAt?.toEpochMilliseconds()
                  ?: Instant.DISTANT_PAST.toEpochMilliseconds()

              feedPayload.posts.forEach { post ->
                if (post.date > feedLastCleanUpAtEpochMilli) {
                  postQueries.upsert(
                    title = post.title,
                    description = post.description,
                    imageUrl = post.imageUrl,
                    date = Instant.fromEpochMilliseconds(post.date),
                    link = post.link,
                    commnetsLink = post.commentsLink,
                    feedLink = feedPayload.link,
                    rawContent = post.rawContent
                  )
                }
              }
            }

            FeedAddResult.Success
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

  suspend fun updateFeed(selectedFeedLink: String) {
    withContext(ioDispatcher) {
      val feed = feedQueries.feed(selectedFeedLink).executeAsOneOrNull()
      if (feed != null) {
        addFeed(feedLink = feed.link, transformUrl = false, feedLastCleanUpAt = feed.lastCleanUpAt)
      }
    }
  }

  fun featuredPosts(
    selectedFeedLink: String?,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST
  ): Flow<List<PostWithMetadata>> {
    return postQueries
      .featuredPosts(
        feedLink = selectedFeedLink,
        unreadOnly = unreadOnly,
        postsAfter = after,
        limit = NUMBER_OF_FEATURED_POSTS,
        mapper = ::PostWithMetadata
      )
      .asFlow()
      .mapToList(ioDispatcher)
  }

  fun posts(
    selectedFeedLink: String?,
    unreadOnly: Boolean? = null,
    after: Instant = Instant.DISTANT_PAST
  ): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery =
        postQueries.count(
          feedLink = selectedFeedLink,
          featuredPostsLimit = NUMBER_OF_FEATURED_POSTS
        ),
      transacter = postQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        postQueries.posts(
          feedLink = selectedFeedLink,
          featuredPostsLimit = NUMBER_OF_FEATURED_POSTS,
          unreadOnly = unreadOnly,
          postsAfter = after,
          limit = limit,
          offset = offset,
          mapper = ::PostWithMetadata
        )
      }
    )
  }

  suspend fun updateBookmarkStatus(bookmarked: Boolean, link: String) {
    withContext(ioDispatcher) { postQueries.updateBookmarkStatus(bookmarked, link) }
  }

  suspend fun updatePostReadStatus(read: Boolean, link: String) {
    withContext(ioDispatcher) { postQueries.updateReadStatus(read, link) }
  }

  suspend fun deleteBookmark(link: String) {
    withContext(ioDispatcher) { bookmarkQueries.deleteBookmark(link) }
  }

  fun allFeeds(postsAfter: Instant = Instant.DISTANT_PAST): PagingSource<Int, Feed> {
    return QueryPagingSource(
      countQuery = feedQueries.count(),
      transacter = feedQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        feedQueries.feedsPaginated(
          postsAfter = postsAfter,
          limit = limit,
          offset = offset,
          mapper = ::Feed
        )
      }
    )
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(ioDispatcher) { feedQueries.feeds(mapper = ::Feed).executeAsList() }
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

  suspend fun feed(feedLink: String): Feed {
    return withContext(ioDispatcher) {
      feedQueries.feed(link = feedLink, mapper = ::Feed).executeAsOne()
    }
  }

  suspend fun removeFeed(feedLink: String) {
    withContext(ioDispatcher) { feedQueries.remove(feedLink) }
  }

  suspend fun updateFeedName(newFeedName: String, feedLink: String) {
    withContext(ioDispatcher) { feedQueries.updateFeedName(newFeedName, feedLink) }
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

  suspend fun hasPost(link: String): Boolean {
    return withContext(ioDispatcher) { postQueries.hasPost(link).executeAsOne() }
  }

  suspend fun hasFeed(link: String): Boolean {
    return withContext(ioDispatcher) { feedQueries.hasFeed(link).executeAsOne() }
  }

  suspend fun toggleFeedPinStatus(feed: Feed) {
    val now =
      if (feed.pinnedAt == null) {
        Clock.System.now()
      } else {
        null
      }
    withContext(ioDispatcher) { feedQueries.updatePinnedAt(pinnedAt = now, link = feed.link) }
  }

  fun numberOfPinnedFeeds(): Flow<Long> {
    return feedQueries.numberOfPinnedFeeds().asFlow().mapToOne(ioDispatcher)
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

  suspend fun updateFeedsLastCleanUpAt(feeds: List<String>) {
    withContext(ioDispatcher) {
      feedQueries.transaction {
        feeds.forEach { feedLink ->
          feedQueries.updateLastCleanUpAt(lastCleanUpAt = Clock.System.now(), link = feedLink)
        }
      }
    }
  }

  suspend fun markPostsInFeedAsRead(feedLink: String) {
    withContext(ioDispatcher) { postQueries.markPostsInFeedAsRead(feedLink) }
  }

  suspend fun post(link: String): Post {
    return withContext(ioDispatcher) { postQueries.post(link, ::Post).executeAsOne() }
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace(Regex.fromLiteral("\""), "\"\"").run { "\"$this\"" }
  }
}
