/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.sync

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxEntry
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class MinifluxSyncCoordinator(
  private val minifluxSource: MinifluxSource,
  private val rssRepository: RssRepository,
  private val dispatchersProvider: DispatchersProvider,
  private val settingsRepository: SettingsRepository,
  private val articleHtmlParser: ArticleHtmlParser,
) : SyncCoordinator {

  private val syncMutex = Mutex()
  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  override suspend fun pull(): Boolean {
    return syncMutex.withLock { pullInternal() }
  }

  private suspend fun pullInternal(): Boolean {
    return try {
      val syncStartTime = Clock.System.now()
      updateSyncState(SyncState.InProgress(0f))

      pushChanges()

      // 1. Sync Subscriptions
      syncSubscriptions()
      updateSyncState(SyncState.InProgress(0.3f))

      // 2. Sync Articles
      val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST
      val hasNewArticles = syncArticles(after = lastSyncedAt.toEpochMilliseconds() / 1000)
      syncArticles(starred = true)
      updateSyncState(SyncState.InProgress(0.7f))

      // 3. Sync Statuses (Read/Bookmark)
      syncStatuses()
      updateSyncState(SyncState.InProgress(0.9f))

      if (hasNewArticles) {
        settingsRepository.updateLastSyncedAt(syncStartTime)
      }
      updateSyncState(SyncState.Complete)

      true
    } catch (e: Exception) {
      Logger.e(e) { "Miniflux pull failed" }
      updateSyncState(SyncState.Error(e))
      false
    }
  }

  override suspend fun pull(feedIds: List<String>): Boolean {
    return withContext(dispatchersProvider.io) {
      syncMutex.withLock {
        feedIds.forEach { feedId -> pullFeedInternal(feedId) }
        true
      }
    }
  }

  override suspend fun pull(feedId: String): Boolean {
    return withContext(dispatchersProvider.io) { syncMutex.withLock { pullFeedInternal(feedId) } }
  }

  private suspend fun pullFeedInternal(feedId: String): Boolean {
    return try {
      updateSyncState(SyncState.InProgress(0f))
      val feed = rssRepository.feed(feedId)
      if (feed?.remoteId != null) {
        syncArticles(feedId = feed.remoteId!!.toLong())
        updateSyncState(SyncState.Complete)
      } else {
        pullInternal()
      }

      true
    } catch (e: Exception) {
      Logger.e(e) { "Miniflux pull failed for feed: $feedId" }
      updateSyncState(SyncState.Error(e))
      false
    }
  }

  override suspend fun push(): Boolean {
    return withContext(dispatchersProvider.io) {
      syncMutex.withLock {
        try {
          pushChanges()
          true
        } catch (e: Exception) {
          Logger.e(e) { "Miniflux push failed" }
          false
        }
      }
    }
  }

  private suspend fun pushChanges() {
    pushStatusChanges()
    pushCategoryChanges()
    pushFeedChanges()
  }

  private suspend fun pushFeedChanges() {
    val localFeeds = rssRepository.allFeedsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // 1. Handle deleted feeds
    localFeeds
      .filter {
        it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed -> minifluxSource.deleteFeed(feed.remoteId!!.toLong()) }

    // 2. Handle new feeds
    localFeeds
      .filter {
        !it.isDeleted &&
          it.remoteId == null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed ->
        // We need a category ID to add a feed in Miniflux.
        // For now, we'll use the default category or find/create one.
        val categories = minifluxSource.categories()
        val defaultCategory = categories.firstOrNull()
        if (defaultCategory != null) {
          val remoteFeed = minifluxSource.addFeed(feed.link, defaultCategory.id)
          rssRepository.updateFeedRemoteId(remoteFeed.id.toString(), feed.id)
        }
      }

    // 3. Handle renamed feeds
    localFeeds
      .filter {
        !it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed ->
        val remoteFeed = minifluxSource.feeds().find { it.id == feed.remoteId!!.toLong() }
        if (remoteFeed != null) {
          minifluxSource.updateFeed(remoteFeed.id, feed.name, remoteFeed.category.id)
        }
      }
  }

  private suspend fun pushCategoryChanges() {
    val localGroups = rssRepository.allFeedGroupsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // 1. Handle deleted groups
    localGroups
      .filter { it.isDeleted && it.updatedAt > lastSyncedAt }
      .forEach { group ->
        if (group.remoteId != null) {
          minifluxSource.deleteCategory(group.remoteId!!.toLong())
        }
      }

    // 2. Handle new/updated groups
    localGroups
      .filter { !it.isDeleted && it.updatedAt > lastSyncedAt }
      .forEach { group ->
        if (group.remoteId == null) {
          val remoteCategory = minifluxSource.addCategory(group.name)
          rssRepository.updateFeedGroupRemoteId(remoteCategory.id.toString(), group.id)
        } else {
          minifluxSource.updateCategory(group.remoteId!!.toLong(), group.name)
        }
      }
  }

  private suspend fun syncSubscriptions() {
    val remoteFeeds = minifluxSource.feeds()
    val localFeeds = rssRepository.allFeedsBlocking()

    // 1. Handle remote deletions
    val remoteIds = remoteFeeds.map { it.id.toString() }.toSet()
    val remoteUrls = remoteFeeds.map { it.feedUrl }.toSet()

    localFeeds.forEach { localFeed ->
      if (
        !localFeed.isDeleted &&
          localFeed.remoteId != null &&
          localFeed.remoteId !in remoteIds &&
          localFeed.link !in remoteUrls
      ) {
        rssRepository.removeFeed(localFeed.id)
      }
    }

    // 2. Handle remote group deletions
    val remoteCategories = minifluxSource.categories()
    val remoteCategoryIds = remoteCategories.map { it.id.toString() }.toSet()
    val localGroups = rssRepository.allFeedGroupsBlocking()

    localGroups.forEach { localGroup ->
      if (
        !localGroup.isDeleted &&
          localGroup.remoteId != null &&
          localGroup.remoteId !in remoteCategoryIds
      ) {
        rssRepository.deleteSources(setOf(localGroup))
      }
    }

    // 3. Handle new/updated subscriptions from remote
    remoteFeeds.forEach { remoteFeed ->
      val localFeed =
        localFeeds.find { it.link == remoteFeed.feedUrl || it.remoteId == remoteFeed.id.toString() }
      val feedId =
        if (localFeed != null) {
          if (localFeed.remoteId != remoteFeed.id.toString()) {
            rssRepository.updateFeedRemoteId(remoteFeed.id.toString(), localFeed.id)
          }
          localFeed.id
        } else {
          rssRepository
            .upsertFeedWithPosts(
              feedPayload =
                FeedPayload(
                  name = remoteFeed.title,
                  icon = "", // Miniflux doesn't provide icon URL directly in feed object
                  description = "",
                  homepageLink = remoteFeed.siteUrl,
                  link = remoteFeed.feedUrl,
                  posts = emptyFlow()
                ),
              updateFeed = true
            )
            .also { rssRepository.updateFeedRemoteId(remoteFeed.id.toString(), it) }
        }

      // Sync category
      val category = remoteFeed.category
      val localGroup =
        rssRepository.feedGroupByRemoteId(category.id.toString())
          ?: rssRepository.allFeedGroupsBlocking().find { it.name == category.title }

      val groupId =
        if (localGroup != null) {
          if (localGroup.remoteId != category.id.toString()) {
            rssRepository.updateFeedGroupRemoteId(category.id.toString(), localGroup.id)
          }
          localGroup.id
        } else {
          rssRepository.upsertGroup(
            id = category.title.lowercase().replace(" ", "-"),
            name = category.title,
            pinnedAt = null,
            updatedAt = Clock.System.now(),
            isDeleted = false,
            remoteId = category.id.toString()
          )
          rssRepository.feedGroupByRemoteId(category.id.toString())!!.id
        }

      val isFeedInGroup =
        rssRepository.feedGroupBlocking(groupId)?.feedIds?.contains(feedId) ?: false
      if (!isFeedInGroup) {
        rssRepository.addFeedIdsToGroups(setOf(groupId), listOf(feedId))
      }
    }
  }

  private suspend fun syncArticles(
    feedId: Long? = null,
    after: Long? = null,
    starred: Boolean? = null,
  ): Boolean {
    var hasNewArticles = false
    var offset = 0
    val limit = 100
    do {
      val entriesPayload =
        minifluxSource.entries(
          limit = limit,
          offset = offset,
          after = after,
          starred = starred,
        )
      val entries = entriesPayload.entries
      entries.forEach { entry ->
        if (feedId == null || entry.feedId == feedId) {
          val isNewArticle = upsertArticle(entry)
          if (isNewArticle) {
            hasNewArticles = true
          }
        }
      }

      offset += limit
    } while (entries.size >= limit)

    return hasNewArticles
  }

  private suspend fun upsertArticle(entry: MinifluxEntry): Boolean {
    val remoteId = entry.id.toString()
    val localPost = rssRepository.postByRemoteId(remoteId) ?: rssRepository.postByLink(entry.url)

    if (localPost != null) {
      if (localPost.remoteId != remoteId) {
        rssRepository.updatePostRemoteId(remoteId, localPost.id)
      }
      return false
    } else {
      val feed = rssRepository.feedByRemoteId(entry.feedId.toString())
      if (feed != null) {
        val htmlContent = articleHtmlParser.parse(entry.content)
        val postPubDateInMillis = entry.publishedAt.dateStringToEpochMillis()
        val postPayload =
          PostPayload(
            title = entry.title,
            link = entry.url,
            description = htmlContent?.textContent ?: "",
            rawContent = htmlContent?.cleanedHtml ?: entry.content,
            imageUrl = htmlContent?.heroImage,
            date = postPubDateInMillis ?: Clock.System.now().toEpochMilliseconds(),
            commentsLink = null,
            fullContent = null,
            isDateParsedCorrectly = postPubDateInMillis != null
          )

        rssRepository.upsertFeedWithPosts(
          feedPayload =
            FeedPayload(
              name = feed.name,
              icon = feed.icon,
              description = feed.description,
              homepageLink = feed.homepageLink,
              link = feed.link,
              posts = flowOf(postPayload)
            ),
          feedId = feed.id,
          updateFeed = false
        )

        rssRepository.postByLink(postPayload.link)?.let {
          rssRepository.updatePostRemoteId(remoteId, it.id)
        }
        return true
      }
      return false
    }
  }

  private suspend fun syncStatuses() {
    // Paginate through unread entries to get IDs
    val unreadIds = mutableSetOf<String>()
    var offset = 0
    val limit = 1000
    do {
      val entries =
        minifluxSource.entries(status = "unread", limit = limit, offset = offset).entries
      unreadIds.addAll(entries.map { it.id.toString() })
      offset += limit
    } while (entries.size >= limit)

    // Paginate through starred entries to get IDs
    val bookmarkIds = mutableSetOf<String>()
    offset = 0
    do {
      val entries = minifluxSource.entries(starred = true, limit = limit, offset = offset).entries
      bookmarkIds.addAll(entries.map { it.id.toString() })
      offset += limit
    } while (entries.size >= limit)

    val localPosts = rssRepository.postsWithRemoteId()
    localPosts.forEach { post ->
      val remoteRead = post.remoteId !in unreadIds
      val remoteBookmarked = post.remoteId in bookmarkIds

      if (post.syncedAt >= post.updatedAt) {
        if (post.read != remoteRead) {
          rssRepository.updatePostReadStatus(read = remoteRead, id = post.id)
          rssRepository.updatePostSyncedAt(post.id, Clock.System.now())
        }
        if (post.bookmarked != remoteBookmarked) {
          rssRepository.updateBookmarkStatus(bookmarked = remoteBookmarked, id = post.id)
          rssRepository.updatePostSyncedAt(post.id, Clock.System.now())
        }
      }
    }
  }

  private suspend fun pushStatusChanges() {
    val dirtyPosts = rssRepository.postsWithLocalChanges()
    if (dirtyPosts.isEmpty()) return

    val toMarkRead = dirtyPosts.filter { it.read }.mapNotNull { it.remoteId?.toLong() }
    val toMarkUnread = dirtyPosts.filter { !it.read }.mapNotNull { it.remoteId?.toLong() }

    if (toMarkRead.isNotEmpty()) minifluxSource.markEntriesAsRead(toMarkRead)
    if (toMarkUnread.isNotEmpty()) minifluxSource.markEntriesAsUnread(toMarkUnread)

    dirtyPosts.forEach { post ->
      if (post.remoteId != null) {
        minifluxSource.toggleBookmark(post.remoteId!!.toLong())
      }
      rssRepository.updatePostSyncedAt(post.id, post.updatedAt)
    }
  }

  private suspend fun updateSyncState(newState: SyncState) {
    _syncState.value = newState
  }
}
