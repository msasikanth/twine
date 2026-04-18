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

import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.data.database.FeedQueries
import dev.sasikanth.rss.reader.data.database.PostQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class SyncRepository(
  private val feedQueries: FeedQueries,
  private val postQueries: PostQueries,
  private val transactionRunner: TransactionRunner,
  private val widgetUpdater: WidgetUpdater,
  private val dispatchersProvider: DispatchersProvider,
) {

  private companion object {
    private const val SQLITE_BATCH_SIZE = 990
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
      postQueries.updatePostRemoteId(remoteId = remoteId, id = postId)
    }
  }

  suspend fun updatePostSyncedAt(postId: String, syncedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      postQueries.updatePostSyncedAt(syncedAt = syncedAt, id = postId)
    }
  }

  suspend fun updatePostSyncedAt(postIds: Set<String>, syncedAt: Instant) {
    val postIdsSnapshot = postIds.toList()
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIdsSnapshot.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
          postQueries.updatePostsSyncedAt(syncedAt = syncedAt, ids = chunk)
        }
      }
    }
  }

  suspend fun updatePostSyncedAt(posts: List<Post>) {
    val postIds = posts.map { it.id }
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        postIds.chunked(SQLITE_BATCH_SIZE).forEach { chunk ->
          postQueries.updatePostsSyncedAtToUpdatedAt(ids = chunk)
        }
      }
    }
  }

  suspend fun latestPostRemoteId(): String? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.latestPostRemoteId().executeAsOneOrNull()
    }
  }

  suspend fun latestPostRemoteIdForFeed(feedId: String): String? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.latestPostRemoteIdForFeed(feedId).executeAsOneOrNull()
    }
  }

  suspend fun postsWithRemoteId(): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithRemoteId(::Post).executeAsList()
    }
  }

  suspend fun postsWithRemoteIdPaged(limit: Long, offset: Long): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithRemoteIdPaged(limit, offset, ::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChanges(): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChanges(::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChangesPaged(limit: Long, offset: Long): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChangesPaged(limit, offset, ::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChangesForFeed(feedId: String): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChangesForFeed(feedId, ::Post).executeAsList()
    }
  }

  suspend fun postsWithLocalChangesForFeedPaged(
    feedId: String,
    limit: Long,
    offset: Long,
  ): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsWithLocalChangesForFeedPaged(feedId, limit, offset, ::Post).executeAsList()
    }
  }

  suspend fun postByRemoteId(remoteId: String): Post? {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postByRemoteId(remoteId, ::Post).executeAsOneOrNull()
    }
  }

  suspend fun postsByRemoteIds(remoteIds: Set<String>): List<Post> {
    return withContext(dispatchersProvider.databaseRead) {
      postQueries.postsByRemoteIds(remoteIds, ::Post).executeAsList()
    }
  }

  fun feedByRemoteId(remoteId: String): Feed? {
    return feedQueries.feedByRemoteId(remoteId, mapper = ::mapToFeed).executeAsOneOrNull()
  }

  fun feedsByRemoteIds(remoteIds: Set<String>): List<Feed> {
    return feedQueries.feedsByRemoteIds(remoteIds, mapper = ::mapToFeed).executeAsList()
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
            audioUrl = post.audioUrl,
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
    widgetUpdater.updateUnreadWidget()
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
    widgetUpdater.updateUnreadWidget()
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
      isDeleted = isDeleted,
      remoteId = remoteId,
    )
  }
}
