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
import dev.sasikanth.rss.reader.core.model.remote.freshrss.ArticlePayload
import dev.sasikanth.rss.reader.core.network.freshrss.FreshRssSource
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
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
class FreshRSSSyncCoordinator(
  private val freshRssSource: FreshRssSource,
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

      pushChanges(syncStartTime)

      // 1. Sync Subscriptions
      val hasNewSubscriptions = syncSubscriptions(syncStartTime)
      updateSyncState(SyncState.InProgress(0.3f))

      // 2. Sync Articles
      val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: syncStartTime.minus(4.hours)
      val newerThan =
        if (hasNewSubscriptions) {
          lastSyncedAt.minus(2.hours).toEpochMilliseconds()
        } else {
          lastSyncedAt.toEpochMilliseconds()
        }

      val hasNewArticles = syncArticles(newerThan = newerThan)
      syncArticles(streamId = FreshRssSource.USER_STATE_STARRED, newerThan = newerThan)
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
      Logger.e(e) { "FreshRSS pull failed" }
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
        syncArticles(streamId = feed.remoteId!!)
        updateSyncState(SyncState.Complete)
      } else {
        pullInternal()
      }

      true
    } catch (e: Exception) {
      Logger.e(e) { "FreshRSS pull failed for feed: $feedId" }
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
          Logger.e(e) { "FreshRSS push failed" }
          false
        }
      }
    }
  }

  private suspend fun pushChanges(syncStartTime: Instant = Clock.System.now()) {
    pushStatusChanges()
    pushGroupChanges(syncStartTime)
    pushFeedChanges(syncStartTime)
    purgeDeletedSource()
  }

  private suspend fun purgeDeletedSource() {
    val feedGroups = rssRepository.allFeedGroupsBlocking()
    val feeds = rssRepository.allFeedsBlocking()
    val localSources = feeds + feedGroups

    localSources.filter { it.isDeleted }.forEach { rssRepository.deleteSources(setOf(it)) }
  }

  private suspend fun pushFeedChanges(syncStartTime: Instant) {
    val localFeeds = rssRepository.allFeedsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // 1. Handle deleted feeds
    localFeeds
      .filter {
        it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed -> freshRssSource.deleteFeed(feed.remoteId!!) }

    // 2. Handle new feeds
    localFeeds
      .filter {
        !it.isDeleted &&
          it.remoteId == null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed -> freshRssSource.addFeed(feed.link) }

    // 3. Handle renamed feeds
    localFeeds
      .filter {
        !it.isDeleted &&
          it.remoteId != null &&
          (it.lastUpdatedAt ?: Instant.DISTANT_PAST) > lastSyncedAt
      }
      .forEach { feed ->
        freshRssSource.editFeedName(feed.remoteId!!, feed.name)
        rssRepository.updateFeedLastUpdatedAt(feed.id, syncStartTime)
      }
  }

  private suspend fun pushGroupChanges(syncStartTime: Instant) {
    val subscriptions = freshRssSource.subscriptions().subscriptions
    val localGroups = rssRepository.allFeedGroupsBlocking()
    val localFeeds = rssRepository.allFeedsBlocking()
    val lastSyncedAt = settingsRepository.lastSyncedAt.first() ?: Instant.DISTANT_PAST

    // 1. Handle deleted groups
    localGroups
      .filter { it.isDeleted && it.updatedAt > lastSyncedAt }
      .forEach { group ->
        if (group.remoteId != null) {
          freshRssSource.deleteTag(group.remoteId!!)
        }
      }

    // 2. Handle new/updated groups
    val localFeedToGroupsMap = mutableMapOf<String, MutableSet<String>>()
    localGroups
      .filter { !it.isDeleted }
      .forEach { group ->
        var remoteTagId = group.remoteId ?: "user/-/label/${group.name}"

        // If it's a new group, we might need to create it (though adding a feed is enough)
        if (group.remoteId == null && group.updatedAt > lastSyncedAt) {
          freshRssSource.addTag(group.name)
          rssRepository.updateFeedGroupRemoteId(remoteTagId, group.id, syncStartTime)
        } else if (group.remoteId != null && group.updatedAt > lastSyncedAt) {
          // Check for rename
          val remoteTagName = group.remoteId!!.replace("user/-/label/", "")
          if (remoteTagName != group.name) {
            freshRssSource.editTag(group.remoteId!!, group.name)
            remoteTagId = "user/-/label/${group.name}"
            rssRepository.updateFeedGroupRemoteId(remoteTagId, group.id, syncStartTime)
          }
        }

        group.feedIds.forEach { feedId ->
          localFeedToGroupsMap.getOrPut(feedId) { mutableSetOf() }.add(remoteTagId)
        }
      }

    // 3. Sync feeds tags
    val remoteFeedsMap = subscriptions.associateBy { it.id }
    localFeeds
      .filter { !it.isDeleted && it.remoteId != null }
      .forEach { localFeed ->
        val remoteFeedId = localFeed.remoteId!!
        val remoteSub = remoteFeedsMap[remoteFeedId]
        if (remoteSub != null) {
          val targetTags = localFeedToGroupsMap[localFeed.id].orEmpty()
          val currentTags =
            remoteSub.categories.map { it.id }.filter { it.startsWith("user/-/label/") }.toSet()

          // Add missing tags
          (targetTags - currentTags).forEach { tagId ->
            freshRssSource.addTagToFeed(remoteFeedId, tagId)
          }

          // Remove extra tags
          (currentTags - targetTags).forEach { tagId ->
            freshRssSource.removeTagFromFeed(remoteFeedId, tagId)
          }
        }
      }
  }

  private suspend fun syncSubscriptions(syncStartTime: Instant): Boolean {
    val subscriptions = freshRssSource.subscriptions().subscriptions
    val localFeeds = rssRepository.allFeedsBlocking()
    var hasNewSubscriptions = false

    // 1. Handle remote deletions
    val remoteIds = subscriptions.map { it.id }.toSet()
    val remoteUrls = subscriptions.map { it.url }.toSet()

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
    val remoteTagIds = freshRssSource.tags().tags.map { it.id }.toSet()
    val localGroups = rssRepository.allFeedGroupsBlocking()

    localGroups.forEach { localGroup ->
      if (
        !localGroup.isDeleted && localGroup.remoteId != null && localGroup.remoteId !in remoteTagIds
      ) {
        rssRepository.markSourcesAsDeleted(setOf(localGroup))
      }
    }

    // 3. Handle new/updated subscriptions from remote
    subscriptions.forEach { subscription ->
      val localFeed =
        localFeeds.find { it.link == subscription.url || it.remoteId == subscription.id }
      val feedId =
        if (localFeed != null) {
          if (
            localFeed.remoteId != subscription.id ||
              localFeed.name != subscription.title ||
              localFeed.homepageLink != subscription.htmlUrl
          ) {
            rssRepository.upsertFeeds(
              listOf(
                localFeed.copy(
                  name = subscription.title,
                  homepageLink = subscription.htmlUrl,
                  remoteId = subscription.id,
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
                  name = subscription.title,
                  icon = subscription.iconUrl,
                  description = "",
                  homepageLink = subscription.htmlUrl,
                  link = subscription.url,
                  posts = emptyFlow()
                ),
              updateFeed = true
            )
            .also {
              hasNewSubscriptions = true
              rssRepository.updateFeedRemoteId(subscription.id, it, syncStartTime)
            }
        }

      // Sync categories (groups)
      subscription.categories.forEach { category ->
        if (category.id.startsWith("user/-/label/")) {
          val tagName = category.id.replace("user/-/label/", "")
          val localGroup =
            rssRepository.feedGroupByRemoteId(category.id)
              ?: rssRepository.allFeedGroupsBlocking().find { it.name == tagName }

          val groupId =
            if (localGroup != null) {
              if (localGroup.remoteId != category.id || localGroup.name != tagName) {
                rssRepository.upsertGroup(
                  id = localGroup.id,
                  name = tagName,
                  pinnedAt = localGroup.pinnedAt,
                  updatedAt = syncStartTime,
                  isDeleted = false,
                  remoteId = category.id
                )
              }
              localGroup.id
            } else {
              rssRepository.upsertGroup(
                id = tagName.lowercase().replace(" ", "-"),
                name = tagName,
                pinnedAt = null,
                updatedAt = syncStartTime,
                isDeleted = false,
                remoteId = category.id
              )
              rssRepository.feedGroupByRemoteId(category.id)!!.id
            }

          val isFeedInGroup =
            rssRepository.feedGroupBlocking(groupId)?.feedIds?.contains(feedId) ?: false
          if (!isFeedInGroup) {
            rssRepository.addFeedIdsToGroups(setOf(groupId), listOf(feedId))
          }
        }
      }
    }

    return hasNewSubscriptions
  }

  private suspend fun syncArticles(
    streamId: String = "user/-/state/com.google/reading-list",
    newerThan: Long = Instant.DISTANT_PAST.toEpochMilliseconds()
  ): Boolean {
    var hasNewArticles = false
    var continuation: String? = null
    do {
      val articlesPayload =
        freshRssSource.articles(
          streamId = streamId,
          limit = 1000,
          newerThan = newerThan,
          continuation = continuation
        )
      val items = articlesPayload.items
      items.asReversed().forEach { item ->
        val isNewArticle = upsertArticle(item)
        if (isNewArticle) {
          hasNewArticles = true
        }
      }

      continuation = articlesPayload.continuation
    } while (continuation != null && articlesPayload.items.isNotEmpty())

    return hasNewArticles
  }

  private suspend fun upsertArticle(item: ArticlePayload): Boolean {
    val remoteId = item.id
    val localPost =
      rssRepository.postByRemoteId(remoteId)
        ?: rssRepository.postByLink(
          item.canonical.firstOrNull()?.href ?: item.alternate.firstOrNull()?.href ?: ""
        )

    if (localPost != null) {
      if (localPost.remoteId != remoteId) {
        rssRepository.updatePostRemoteId(remoteId, localPost.id)
      }
      return false
    } else {
      // Insert new post
      val feedRemoteId = item.origin.streamId
      val feed = rssRepository.feedByRemoteId(feedRemoteId)

      if (feed != null) {
        val htmlContent = articleHtmlParser.parse(item.summary.content)
        val postPayload =
          PostPayload(
            title = item.title,
            link = item.canonical.firstOrNull()?.href
                ?: item.alternate.firstOrNull()?.href ?: item.id,
            description = htmlContent?.textContent ?: "",
            rawContent = htmlContent?.cleanedHtml ?: item.summary.content,
            imageUrl = htmlContent?.heroImage,
            date = item.published * 1000, // FreshRSS uses seconds, we use millis
            commentsLink = null,
            fullContent = null,
            isDateParsedCorrectly = true
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

        // Link the newly created post with remoteId
        rssRepository.postByLink(postPayload.link)?.let {
          rssRepository.updatePostRemoteId(remoteId, it.id)
        }
        return true
      }
      return false
    }
  }

  private suspend fun syncStatuses() {
    val unreadIds = freshRssSource.unreadIds().toSet()
    val bookmarkIds = freshRssSource.bookmarkIds().toSet()
    val localPosts = rssRepository.postsWithRemoteId()

    localPosts.forEach { post ->
      val remoteRead = post.remoteId !in unreadIds
      val remoteBookmarked = post.remoteId in bookmarkIds

      // If local is synced (no pending changes), remote is source of truth
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

    val toMarkRead = dirtyPosts.filter { it.read }.mapNotNull { it.remoteId }
    val toMarkUnread = dirtyPosts.filter { !it.read }.mapNotNull { it.remoteId }
    val toBookmark = dirtyPosts.filter { it.bookmarked }.mapNotNull { it.remoteId }
    val toUnbookmark = dirtyPosts.filter { !it.bookmarked }.mapNotNull { it.remoteId }

    if (toMarkRead.isNotEmpty()) freshRssSource.markArticlesAsRead(toMarkRead)
    if (toMarkUnread.isNotEmpty()) freshRssSource.markArticlesAsUnRead(toMarkUnread)
    if (toBookmark.isNotEmpty()) freshRssSource.addBookmarks(toBookmark)
    if (toUnbookmark.isNotEmpty()) freshRssSource.removeBookmarks(toUnbookmark)

    dirtyPosts.forEach { post -> rssRepository.updatePostSyncedAt(post.id, post.updatedAt) }
  }

  private suspend fun updateSyncState(newState: SyncState) {
    _syncState.value = newState
  }
}
