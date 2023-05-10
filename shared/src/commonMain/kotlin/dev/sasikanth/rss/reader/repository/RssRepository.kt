package dev.sasikanth.rss.reader.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.models.mappers.toFeed
import dev.sasikanth.rss.reader.models.mappers.toPost
import dev.sasikanth.rss.reader.network.feedFetcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RssRepository(
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val ioDispatcher: CoroutineDispatcher
) {

  private val feedFetcher = feedFetcher(ioDispatcher)

  suspend fun addFeed(feedLink: String) {
    withContext(ioDispatcher) {
      val feedPayload = feedFetcher.fetch(feedLink)
      feedQueries.transaction {
        feedQueries.insert(feed = feedPayload.toFeed())
        feedPayload.posts.forEach {
          postQueries.insert(it.toPost(feedLink = feedPayload.link))
        }
      }
    }
  }

  suspend fun updateFeeds() {
    withContext(ioDispatcher) {
      val feeds = feedQueries.feeds().executeAsList()
      feeds.forEach { feed ->
        addFeed(feed.link)
      }
    }
  }

  fun allPosts(): Flow<List<PostWithMetadata>> {
    return postQueries.postWithMetadata(null).asFlow().mapToList(ioDispatcher)
  }

  fun postsOfFeed(feedLink: String): Flow<List<PostWithMetadata>> {
    return postQueries.postWithMetadata(feedLink).asFlow().mapToList(ioDispatcher)
  }

  suspend fun removePostsOfFeed(feedLink: String) {
    withContext(ioDispatcher) {
      postQueries.removePostsOfFeed(feedLink)
    }
  }

  fun allFeeds(): Flow<List<Feed>> {
    return feedQueries.feeds().asFlow().mapToList(ioDispatcher)
  }

  suspend fun removeFeed(feedLink: String) {
    withContext(ioDispatcher) {
      feedQueries.remove(feedLink)
    }
  }
}
