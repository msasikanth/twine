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
    transformUrl: Boolean = true
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
              feedPayload.posts.forEach { post ->
                postQueries.upsert(
                  title = post.title,
                  description = post.description,
                  imageUrl = post.imageUrl,
                  date = Instant.fromEpochMilliseconds(post.date),
                  link = post.link,
                  commnetsLink = post.commentsLink,
                  feedLink = feedPayload.link,
                )
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
          feeds.map { feed -> launch { addFeed(feedLink = feed.link, transformUrl = false) } }
        }
      }

    results.flatten().joinAll()
  }

  suspend fun updateFeed(selectedFeedLink: String) {
    withContext(ioDispatcher) { addFeed(feedLink = selectedFeedLink, transformUrl = false) }
  }

  fun featuredPosts(selectedFeedLink: String?): Flow<List<PostWithMetadata>> {
    return postQueries
      .featuredPosts(
        feedLink = selectedFeedLink,
        limit = NUMBER_OF_FEATURED_POSTS,
        mapper = ::PostWithMetadata
      )
      .asFlow()
      .mapToList(ioDispatcher)
  }

  fun posts(selectedFeedLink: String?): PagingSource<Int, PostWithMetadata> {
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

  suspend fun deleteBookmark(link: String) {
    withContext(ioDispatcher) { bookmarkQueries.deleteBookmark(link) }
  }

  fun allFeeds(): PagingSource<Int, Feed> {
    return QueryPagingSource(
      countQuery = feedQueries.count(),
      transacter = feedQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset -> feedQueries.feedsPaginated(limit, offset, ::Feed) }
    )
  }

  suspend fun allFeedsBlocking(): List<Feed> {
    return withContext(ioDispatcher) { feedQueries.feeds(mapper = ::Feed).executeAsList() }
  }

  /** Search feeds, returns all feeds if [searchQuery] is empty */
  fun searchFeed(searchQuery: String): PagingSource<Int, Feed> {
    val sanitizedSearchQuery = sanitizeSearchQuery(searchQuery)

    return QueryPagingSource(
      countQuery = feedSearchFTSQueries.countSearchResults(searchQuery = sanitizedSearchQuery),
      transacter = feedSearchFTSQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        feedSearchFTSQueries.search(searchQuery = sanitizedSearchQuery, limit, offset, ::Feed)
      }
    )
  }

  fun feed(feedLink: String): Feed {
    return feedQueries.feed(link = feedLink, mapper = ::Feed).executeAsOne()
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

  fun numberOfFeeds(): Flow<Long> {
    return feedQueries.numberOfFeeds().asFlow().mapToOne(ioDispatcher)
  }

  private fun sanitizeSearchQuery(searchQuery: String): String {
    return searchQuery.replace(Regex.fromLiteral("\""), "\"\"").run { "\"$this\"" }
  }
}
