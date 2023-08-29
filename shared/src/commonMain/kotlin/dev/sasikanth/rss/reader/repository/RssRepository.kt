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

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.sasikanth.rss.reader.database.BookmarkQueries
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.database.PostSearchFTSQueries
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.models.mappers.toFeed
import dev.sasikanth.rss.reader.network.feedFetcher
import dev.sasikanth.rss.reader.search.SearchSortOrder
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

  private val ioDispatcher = dispatchersProvider.io
  private val feedFetcher = feedFetcher(ioDispatcher)

  suspend fun addFeed(feedLink: String) {
    withContext(ioDispatcher) {
      val feedPayload = feedFetcher.fetch(feedLink)
      feedQueries.upsert(feed = feedPayload.toFeed())
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

  fun posts(selectedFeedLink: String?): Flow<List<PostWithMetadata>> {
    return postQueries.postWithMetadata(selectedFeedLink).asFlow().mapToList(ioDispatcher)
  }

  fun allFeeds(): Flow<List<Feed>> {
    return feedQueries.feeds().asFlow().mapToList(ioDispatcher)
  }

  suspend fun removeFeed(feedLink: String) {
    withContext(ioDispatcher) { feedQueries.remove(feedLink) }
  }

  suspend fun updateFeedName(newFeedName: String, feedLink: String) {
    withContext(ioDispatcher) { feedQueries.updateFeedName(newFeedName, feedLink) }
  }

  fun search(searchQuery: String, sortOrder: SearchSortOrder): Flow<List<PostWithMetadata>> {
    return postSearchFTSQueries
      .search(searchQuery, sortOrder.value, mapper = ::mapToPostWithMetadata)
      .asFlow()
      .mapToList(ioDispatcher)
  }

  fun bookmarks(): Flow<List<PostWithMetadata>> {
    return bookmarkQueries
      .bookmarks(
        mapper = { title, description, imageUrl, date, link, _, feedName, feedIcon, _ ->
          mapToPostWithMetadata(title, description, imageUrl, date, link, feedName, feedIcon)
        }
      )
      .asFlow()
      .mapToList(ioDispatcher)
  }

  private fun mapToPostWithMetadata(
    title: String,
    description: String,
    imageUrl: String?,
    date: Instant,
    link: String,
    feedName: String,
    feedIcon: String
  ): PostWithMetadata {
    return PostWithMetadata(
      title = title,
      description = description,
      imageUrl = imageUrl,
      date = date,
      link = link,
      feedName = feedName,
      feedIcon = feedIcon
    )
  }
}
