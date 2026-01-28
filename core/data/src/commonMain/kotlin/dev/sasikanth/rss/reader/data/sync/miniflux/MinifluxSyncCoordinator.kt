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

package dev.sasikanth.rss.reader.data.sync.miniflux

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxCategory
import dev.sasikanth.rss.reader.core.model.remote.miniflux.MinifluxEntry
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.dateStringToEpochMillis
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
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

  companion object {
    private const val DEFAULT_CATEGORY_TITLE = "All"
  }

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

      // 1. Push local changes
      pushChanges(syncStartTime)

      // 2. Sync Subscriptions
      val hasNewSubscriptions = syncSubscriptions(syncStartTime)
      updateSyncState(SyncState.InProgress(0.3f))

      // 3. Sync Articles
      val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: syncStartTime.minus(4.hours)
      val after =
        if (hasNewSubscriptions) lastSyncedAt.minus(2.hours).epochSeconds
        else lastSyncedAt.epochSeconds

      val hasNewArticles = syncArticles(after = after)
      syncArticles(starred = true, after = after)
      updateSyncState(SyncState.InProgress(0.7f))

      // 4. Sync Statuses (Read/Bookmark)
      syncStatuses()
      updateSyncState(SyncState.InProgress(0.9f))

      // Only update lastSyncedAt if we found new articles to avoid missing articles
      // that were added to the server between syncs with older timestamps
      if (hasNewArticles) {
        settingsRepository.updateLastSyncedAt(syncStartTime)
      }
      updateSyncState(SyncState.Complete)

      true
    } catch (e: Exception) {
      Logger.e(e) { "Miniflux pull failed: $e" }
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

  private suspend fun pushChanges(syncStartTime: Instant = Clock.System.now()) {
    pushStatusChanges()
    pushCategoryChanges(syncStartTime)
    pushFeedChanges(syncStartTime)
    purgeDeletedSources()
  }

  private suspend fun purgeDeletedSources() {
    val feedGroups = rssRepository.allFeedGroupsBlocking()
    val feeds = rssRepository.allFeedsBlocking()
    val localSources = feeds + feedGroups

    localSources.filter { it.isDeleted }.forEach { rssRepository.deleteSources(setOf(it)) }
  }

  private suspend fun pushFeedChanges(syncStartTime: Instant) {
    val localFeeds = rssRepository.allFeedsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // Early return if no feeds have been updated since last sync
    val hasUpdatedFeeds =
      localFeeds.any { (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt }
    if (!hasUpdatedFeeds) return

    // 1. Handle deleted feeds
    localFeeds
      .filter {
        it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed -> minifluxSource.deleteFeed(feed.remoteId!!.toLong()) }

    // 2. Handle new feeds
    val newFeeds =
      localFeeds.filter {
        !it.isDeleted &&
          it.remoteId == null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
    if (newFeeds.isNotEmpty()) {
      val categories = minifluxSource.categories()
      val defaultCategory = findOrCreateDefaultCategory(categories, syncStartTime)
      newFeeds.forEach { feed ->
        val remoteFeed = minifluxSource.addFeed(feed.link, defaultCategory.id)
        rssRepository.updateFeedRemoteId(remoteFeed.id.toString(), feed.id, syncStartTime)
      }
    }

    // 3. Handle renamed feeds
    val renamedFeeds =
      localFeeds.filter {
        !it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
    if (renamedFeeds.isNotEmpty()) {
      val remoteFeeds = minifluxSource.feeds()
      renamedFeeds.forEach { feed ->
        val remoteFeed = remoteFeeds.find { it.id == feed.remoteId!!.toLong() }
        if (remoteFeed != null && remoteFeed.title != feed.name) {
          minifluxSource.updateFeed(remoteFeed.id, feed.name, remoteFeed.category.id)
          rssRepository.updateFeedLastUpdatedAt(feed.id, syncStartTime)
        }
      }
    }

    // Update lastSyncedAt after successful push to prevent redundant push attempts
    // This ensures early returns work correctly on subsequent syncs when no new articles
    settingsRepository.updateLastSyncedAt(syncStartTime)
  }

  private suspend fun pushCategoryChanges(syncStartTime: Instant) {
    val localGroups = rssRepository.allFeedGroupsBlocking()
    val localFeeds = rssRepository.allFeedsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // Early return if no groups have been updated since last sync
    val hasUpdatedGroups = localGroups.any { it.updatedAt > lastSyncedAt }
    if (!hasUpdatedGroups) return

    val subscriptions = minifluxSource.feeds()
    val categories = minifluxSource.categories()
    val defaultCategory = findOrCreateDefaultCategory(categories, syncStartTime)
    val defaultCategoryLocalId =
      rssRepository.feedGroupByRemoteId(defaultCategory.id.toString())!!.id

    // 1. Handle deleted groups
    localGroups
      .filter { it.isDeleted && it.updatedAt > lastSyncedAt }
      .forEach { group ->
        if (group.remoteId != null) {
          val remoteCategoryId = group.remoteId!!.toLong()
          val feedsInCategory = subscriptions.filter { it.category.id == remoteCategoryId }
          if (feedsInCategory.isNotEmpty() && defaultCategory.id != remoteCategoryId) {
            feedsInCategory.forEach { feed ->
              minifluxSource.updateFeed(feed.id, feed.title, defaultCategory.id)
              rssRepository.addFeedIdsToGroups(setOf(defaultCategoryLocalId), group.feedIds)
            }
          }
          minifluxSource.deleteCategory(remoteCategoryId)
        }
      }

    // 2. Handle new/updated groups
    val localFeedToCategoryMap = mutableMapOf<String, Long>()
    localGroups
      .filter { !it.isDeleted }
      .forEach { group ->
        var remoteCategoryId = group.remoteId

        // If it's a new group, we might need to create it (though adding a feed is enough)
        if (group.remoteId == null && group.updatedAt > lastSyncedAt) {
          val remoteCategory = minifluxSource.addCategory(group.name)
          remoteCategoryId = remoteCategory.id.toString()
          rssRepository.updateFeedGroupRemoteId(remoteCategoryId, group.id, syncStartTime)
        } else if (group.remoteId != null && group.updatedAt > lastSyncedAt) {
          minifluxSource.updateCategory(group.remoteId!!.toLong(), group.name)
          rssRepository.updateFeedGroupUpdatedAt(group.id, syncStartTime)
        }

        // Build complete feed-to-category map for all non-deleted groups
        if (remoteCategoryId != null) {
          val remoteCategoryIdLong = remoteCategoryId.toLong()
          group.feedIds.forEach { feedId -> localFeedToCategoryMap[feedId] = remoteCategoryIdLong }
        }
      }

    // 3. Sync feeds categories for feeds that have been updated
    val remoteFeedsMap = subscriptions.associateBy { it.id }
    localFeeds
      .filter {
        !it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { localFeed ->
        val remoteFeedId = localFeed.remoteId!!.toLong()
        val remoteFeed = remoteFeedsMap[remoteFeedId]
        if (remoteFeed != null) {
          val targetCategoryId = localFeedToCategoryMap[localFeed.id] ?: defaultCategory.id
          if (remoteFeed.category.id != targetCategoryId) {
            minifluxSource.updateFeed(remoteFeedId, localFeed.name, targetCategoryId)
            rssRepository.updateFeedLastUpdatedAt(localFeed.id, syncStartTime)

            // If feed was removed from all groups, add it to the default category locally
            if (localFeedToCategoryMap[localFeed.id] == null) {
              rssRepository.addFeedIdsToGroups(setOf(defaultCategoryLocalId), listOf(localFeed.id))
            }
          }
        }
      }

    // Update lastSyncedAt after successful push to prevent redundant push attempts
    // This ensures early returns work correctly on subsequent syncs when no new articles
    settingsRepository.updateLastSyncedAt(syncStartTime)
  }

  private suspend fun syncSubscriptions(syncStartTime: Instant): Boolean {
    val remoteFeeds = minifluxSource.feeds()
    val localFeeds = rssRepository.allFeedsBlocking()
    var hasNewSubscriptions = false

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
        rssRepository.markSourcesAsDeleted(setOf(localGroup))
      }
    }

    // 3. Handle new/updated subscriptions from remote
    remoteFeeds.forEach { remoteFeed ->
      val localFeed =
        localFeeds.find { it.link == remoteFeed.feedUrl || it.remoteId == remoteFeed.id.toString() }
      val feedId =
        if (localFeed != null) {
          if (
            localFeed.remoteId != remoteFeed.id.toString() ||
              localFeed.name != remoteFeed.title ||
              localFeed.homepageLink != remoteFeed.siteUrl
          ) {
            rssRepository.upsertFeeds(
              listOf(
                localFeed.copy(
                  name = remoteFeed.title,
                  homepageLink = remoteFeed.siteUrl,
                  remoteId = remoteFeed.id.toString(),
                  lastUpdatedAt = syncStartTime,
                  isDeleted = false,
                )
              )
            )
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
            .also {
              hasNewSubscriptions = true
              rssRepository.updateFeedRemoteId(remoteFeed.id.toString(), it, syncStartTime)
            }
        }

      // Sync category
      val category = remoteFeed.category

      val localGroup =
        rssRepository.feedGroupByRemoteId(category.id.toString())
          ?: rssRepository.allFeedGroupsBlocking().find { it.name == category.title }

      val groupId =
        if (localGroup != null) {
          if (localGroup.remoteId != category.id.toString() || localGroup.name != category.title) {
            rssRepository.upsertGroup(
              id = localGroup.id,
              name = category.title,
              pinnedAt = localGroup.pinnedAt,
              updatedAt = syncStartTime,
              isDeleted = false,
              remoteId = category.id.toString()
            )
          }
          localGroup.id
        } else {
          rssRepository.upsertGroup(
            id = nameBasedUuidOf(category.title).toString(),
            name = category.title,
            pinnedAt = null,
            updatedAt = syncStartTime,
            isDeleted = false,
            remoteId = category.id.toString()
          )
          rssRepository.feedGroupByRemoteId(category.id.toString())!!.id
        }

      // Remove feed from all groups except the target group
      val allGroups = rssRepository.allFeedGroupsBlocking()
      val groupsContainingFeed =
        allGroups.filter { it.feedIds.contains(feedId) && it.id != groupId }
      if (groupsContainingFeed.isNotEmpty()) {
        rssRepository.removeFeedIdsFromGroups(
          groupIds = groupsContainingFeed.map { it.id }.toSet(),
          feedIds = listOf(feedId)
        )
      }

      // Add feed to the correct group
      val isFeedInGroup =
        rssRepository.feedGroupBlocking(groupId)?.feedIds?.contains(feedId) ?: false
      if (!isFeedInGroup) {
        rssRepository.addFeedIdsToGroups(setOf(groupId), listOf(feedId))
      }
    }

    purgeDeletedSources()

    return hasNewSubscriptions
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
          status = listOf("read", "unread"),
          limit = limit,
          offset = offset,
          after = after,
          starred = starred,
          feedId = feedId,
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
        minifluxSource.entries(status = listOf("unread"), limit = limit, offset = offset).entries
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
    val toBookmark = dirtyPosts.filter { it.bookmarked }.mapNotNull { it.remoteId?.toLong() }
    val toUnbookmark = dirtyPosts.filter { !it.bookmarked }.mapNotNull { it.remoteId?.toLong() }

    if (toMarkRead.isNotEmpty()) minifluxSource.markEntriesAsRead(toMarkRead)
    if (toMarkUnread.isNotEmpty()) minifluxSource.markEntriesAsUnread(toMarkUnread)
    if (toBookmark.isNotEmpty()) minifluxSource.addBookmarks(toBookmark)
    if (toUnbookmark.isNotEmpty()) minifluxSource.removeBookmarks(toUnbookmark)

    dirtyPosts.forEach { post -> rssRepository.updatePostSyncedAt(post.id, post.updatedAt) }
  }

  private suspend fun updateSyncState(newState: SyncState) {
    _syncState.value = newState
  }

  private suspend fun findOrCreateDefaultCategory(
    categories: List<MinifluxCategory>,
    syncStartTime: Instant
  ): MinifluxCategory {
    val category =
      categories.find { it.title.equals(DEFAULT_CATEGORY_TITLE, ignoreCase = true) }
        ?: minifluxSource.addCategory(DEFAULT_CATEGORY_TITLE)

    rssRepository.upsertGroup(
      id = nameBasedUuidOf(category.title).toString(),
      name = category.title,
      pinnedAt = null,
      updatedAt = syncStartTime,
      isDeleted = false,
      remoteId = category.id.toString(),
    )

    return category
  }
}
