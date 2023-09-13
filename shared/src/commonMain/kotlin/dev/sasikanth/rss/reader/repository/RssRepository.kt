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
import dev.sasikanth.rss.reader.database.BookmarkQueries
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.models.local.Feed
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.network.feedFetcher
import dev.sasikanth.rss.reader.search.SearchSortOrder
import dev.sasikanth.rss.reader.utils.DispatchersProvider
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
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val postSearchFTSQueries: PostSearchFTSQueries,
  private val bookmarkQueries: BookmarkQueries,
  dispatchersProvider: DispatchersProvider
) {

  companion object {
    private const val NUMBER_OF_FEATURED_POSTS = 6L
  }

  private val ioDispatcher = dispatchersProvider.io
  private val feedFetcher = feedFetcher(ioDispatcher)

  suspend fun addFeed(feedLink: String) {
    withContext(ioDispatcher) {
      val feedPayload = feedFetcher.fetch(feedLink)
      feedQueries.upsert(
        name = feedPayload.name,
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
            feedLink = feedPayload.link
          )
        }
      }
    }
  }

  suspend fun updateFeeds() {
    val results =
      withContext(ioDispatcher) {
        val feeds = feedQueries.feeds().executeAsList()
        feeds.map { feed -> launch { addFeed(feed.link) } }
      }
    results.joinAll()
  }

  fun featuredPosts(selectedFeedLink: String?): Flow<List<PostWithMetadata>> {
    return postQueries
      .featuredPosts(
        feedLink = selectedFeedLink,
        limit = NUMBER_OF_FEATURED_POSTS,
        mapper = ::mapToPostWithMetadata
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
          mapper = ::mapToPostWithMetadata
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

  fun allFeeds(): Flow<List<Feed>> {
    return feedQueries.feeds(mapper = ::mapToFeed).asFlow().mapToList(ioDispatcher)
  }

  suspend fun removeFeed(feedLink: String) {
    withContext(ioDispatcher) { feedQueries.remove(feedLink) }
  }

  suspend fun updateFeedName(newFeedName: String, feedLink: String) {
    withContext(ioDispatcher) { feedQueries.updateFeedName(newFeedName, feedLink) }
  }

  fun search(searchQuery: String, sortOrder: SearchSortOrder): PagingSource<Int, PostWithMetadata> {
    return QueryPagingSource(
      countQuery = postSearchFTSQueries.countSearchResults(searchQuery),
      transacter = postSearchFTSQueries,
      context = ioDispatcher,
      queryProvider = { limit, offset ->
        postSearchFTSQueries.search(
          searchQuery = searchQuery,
          sortOrder = sortOrder.value,
          limit = limit,
          offset = offset,
          mapper = ::mapToPostWithMetadata
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
        bookmarkQueries.bookmarks(limit, offset, ::mapToPostWithMetadata)
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

  private fun mapToPostWithMetadata(
    title: String,
    description: String,
    imageUrl: String?,
    date: Instant,
    link: String,
    bookmarked: Boolean,
    feedName: String,
    feedIcon: String,
    feedLink: String
  ): PostWithMetadata {
    return PostWithMetadata(
      title = title,
      description = description,
      imageUrl = imageUrl,
      date = date,
      link = link,
      bookmarked = bookmarked,
      feedName = feedName,
      feedIcon = feedIcon,
      feedLink = feedLink
    )
  }

  private fun mapToFeed(
    name: String,
    icon: String,
    description: String,
    homepageLink: String,
    createdAt: Instant,
    link: String,
    pinnedAt: Instant?
  ): Feed {
    return Feed(
      name = name,
      icon = icon,
      description = description,
      homepageLink = homepageLink,
      createdAt = createdAt,
      link = link,
      pinnedAt = pinnedAt
    )
  }
}
