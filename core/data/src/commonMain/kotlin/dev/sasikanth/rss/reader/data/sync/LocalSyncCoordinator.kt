/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */
package dev.sasikanth.rss.reader.data.sync

import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.FeedAddResult
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.time.LastRefreshedAt
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class LocalSyncCoordinator(
  private val rssRepository: RssRepository,
  private val dispatchersProvider: DispatchersProvider,
  private val observableActiveSource: ObservableActiveSource,
  private val settingsRepository: SettingsRepository,
  private val lastRefreshedAt: LastRefreshedAt,
) : SyncCoordinator {

  companion object {
    private const val SYNC_CHUNK_SIZE = 6
  }

  private val syncMutex = Mutex()
  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  override suspend fun pull() {
    withContext(dispatchersProvider.default) {
      try {
        updateSyncState(SyncState.InProgress(0f))
        checkAndRefreshLastRefreshTime {
          val allFeeds =
            withContext(dispatchersProvider.databaseRead) { rssRepository.allFeedsBlocking() }
          val now = Clock.System.now()
          val feedsToRefresh = allFeeds.filter { feed -> shouldRefreshFeed(feed, now) }
          val feedsChunks = feedsToRefresh.chunked(SYNC_CHUNK_SIZE)

          feedsChunks.forEachIndexed { index, feeds ->
            val jobs = feeds.map { feed -> launch { pullFeed(feed, now) } }

            jobs.joinAll()
            updateSyncState(SyncState.InProgress((index + 1).toFloat() / feedsChunks.size))
          }
        }

        updateSyncState(SyncState.Complete)
      } catch (e: Exception) {
        updateSyncState(SyncState.Error(e))
      }
    }
  }

  override suspend fun pull(feedIds: List<String>) {
    withContext(dispatchersProvider.default) {
      try {
        updateSyncState(SyncState.InProgress(0f))

        val now = Clock.System.now()
        feedIds.forEachIndexed { index, feedId ->
          val feed =
            withContext(dispatchersProvider.databaseRead) { rssRepository.feed(feedId = feedId) }

          if (feed != null) {
            checkAndRefreshLastRefreshTime { pullFeed(feed, now) }
          }

          updateSyncState(SyncState.InProgress((index + 1).toFloat() / feedIds.size))
        }

        updateSyncState(SyncState.Complete)
      } catch (e: Exception) {
        updateSyncState(SyncState.Error(e))
      }
    }
  }

  override suspend fun pull(feedId: String) {
    withContext(dispatchersProvider.default) {
      try {
        updateSyncState(SyncState.InProgress(0f))
        val feed =
          withContext(dispatchersProvider.databaseRead) { rssRepository.feed(feedId = feedId) }
        val now = Clock.System.now()

        if (feed != null) {
          checkAndRefreshLastRefreshTime {
            updateSyncState(SyncState.InProgress(0.5f))
            pullFeed(feed, now)
          }
        }

        updateSyncState(SyncState.Complete)
      } catch (e: Exception) {
        updateSyncState(SyncState.Error(e))
      }
    }
  }

  override suspend fun push() {
    // no-op
  }

  private suspend fun pullFeed(feed: Feed, now: Instant) {
    val initialPostCount = rssRepository.postsCountForFeed(feed.id)
    val result =
      rssRepository.fetchAndAddFeed(
        feedLink = feed.link,
        feedLastCleanUpAt = feed.lastCleanUpAt,
      )

    if (result is FeedAddResult.Success) {
      val finalPostCount = rssRepository.postsCountForFeed(feed.id)
      val hasNewContent = finalPostCount > initialPostCount

      withContext(dispatchersProvider.databaseWrite) {
        rssRepository.updateFeedLastUpdatedAt(feedId = feed.id, lastUpdatedAt = now)
      }

      adjustRefreshInterval(feed, hasNewContent)
    }
  }

  private suspend fun checkAndRefreshLastRefreshTime(block: suspend () -> Unit) {
    withContext(dispatchersProvider.default) {
      val activeSource = observableActiveSource.activeSource.firstOrNull()
      val postsType = settingsRepository.postsType.first()
      val lastRefreshedAtDateTime = lastRefreshedAt.dateTimeFlow.first()

      val unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(postsType)
      val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, lastRefreshedAtDateTime)
      val activeSourceIds = activeSourceIds(activeSource)

      val allPostsCount =
        withContext(dispatchersProvider.databaseRead) {
          rssRepository.allPostsCount(
            activeSourceIds = activeSourceIds,
            unreadOnly = unreadOnly,
            after = postsAfter,
            lastSyncedAt = lastRefreshedAtDateTime.toInstant(TimeZone.currentSystemDefault())
          )
        }

      block()

      if (allPostsCount == 0L) {
        lastRefreshedAt.refresh()
      }
    }
  }

  private fun activeSourceIds(activeSource: Source?) =
    when (activeSource) {
      is Feed -> listOf(activeSource.id)
      is FeedGroup -> activeSource.feedIds
      else -> emptyList()
    }

  private suspend fun updateSyncState(newState: SyncState) {
    syncMutex.withLock { _syncState.value = newState }
  }

  private fun shouldRefreshFeed(feed: Feed, now: Instant): Boolean {
    val lastUpdatedAt = feed.lastUpdatedAt ?: return true
    val timeSinceLastUpdate = (now - lastUpdatedAt)
    return timeSinceLastUpdate >= feed.refreshInterval
  }

  private suspend fun adjustRefreshInterval(feed: Feed, hasNewContent: Boolean) {
    val currentRefreshInterval = feed.refreshInterval

    val adjustmentFactor = if (hasNewContent) 0.8 else 1.2
    val newRefreshInterval =
      if (hasNewContent) {
        maxOf(15.minutes, (currentRefreshInterval * adjustmentFactor))
      } else {
        minOf(1.days, (currentRefreshInterval * adjustmentFactor))
      }

    if (newRefreshInterval != currentRefreshInterval) {
      withContext(dispatchersProvider.databaseWrite) {
        rssRepository.updateFeedRefreshInterval(
          feedId = feed.id,
          refreshInterval = newRefreshInterval
        )
      }
    }
  }
}
