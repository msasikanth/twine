package dev.sasikanth.rss.reader.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.sasikanth.rss.reader.database.Feed
import dev.sasikanth.rss.reader.database.FeedQueries
import dev.sasikanth.rss.reader.database.Post
import dev.sasikanth.rss.reader.database.PostQueries
import dev.sasikanth.rss.reader.models.mappers.toFeed
import dev.sasikanth.rss.reader.models.mappers.toPost
import dev.sasikanth.rss.reader.network.feedFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class RssRepository(
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries
) {

  private val feedFetcher = feedFetcher()

  suspend fun addFeed(feedLink: String) {
    withContext(Dispatchers.IO) {
      val feedPayload = feedFetcher.fetch(feedLink)
      feedQueries.insert(feed = feedPayload.toFeed())
      feedPayload.posts.forEach {
        postQueries.insert(it.toPost(feedLink = feedPayload.link))
      }
    }
  }
  
  suspend fun updateFeeds() {
    withContext(Dispatchers.IO) {
      val feeds = feedQueries.feeds().executeAsList()
      feeds.forEach { feed ->
        addFeed(feed.link)
      }
    }
  }

  fun allPosts(): Flow<List<Post>> {
    return postQueries.allPosts().asFlow().mapToList(Dispatchers.IO)
  }

  fun postsOfFeed(feedLink: String): Flow<List<Post>> {
    return postQueries.postsOfFeed(feedLink).asFlow().mapToList(Dispatchers.IO)
  }

  suspend fun removePostsOfFeed(feedLink: String) {
    withContext(Dispatchers.IO) {
      postQueries.removePostsOfFeed(feedLink)
    }
  }

  fun allFeeds(): Flow<List<Feed>> {
    return feedQueries.feeds().asFlow().mapToList(Dispatchers.IO)
  }

  suspend fun removeFeed(feedLink: String) {
    withContext(Dispatchers.IO) {
      feedQueries.remove(feedLink)
    }
  }
}
