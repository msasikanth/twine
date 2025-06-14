/*
 * Copyright 2025 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.data.sync

import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.time.CurrentDateTimeSource
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class SyncCoordinator(
  private val rssRepository: RssRepository,
  private val currentDateTimeSource: CurrentDateTimeSource,
  private val dispatchersProvider: DispatchersProvider,
) {

  companion object {
    private const val SYNC_CHUNK_SIZE = 6
  }

  private val syncMutex = Mutex()
  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  suspend fun refreshFeeds() {
    withContext(dispatchersProvider.databaseRead) {
      try {
        updateSyncState(SyncState.InProgress(0f))
        val feedsChunks = rssRepository.allFeedsBlocking().chunked(SYNC_CHUNK_SIZE)

        feedsChunks.forEachIndexed { index, feeds ->
          val jobs =
            feeds.map { feed ->
              launch {
                rssRepository.fetchAndAddFeed(
                  feedLink = feed.link,
                  feedLastCleanUpAt = feed.lastCleanUpAt,
                )
              }
            }

          jobs.joinAll()

          updateSyncState(SyncState.InProgress((index + 1).toFloat() / feedsChunks.size))
        }

        updateSyncState(SyncState.Complete)
        currentDateTimeSource.refresh()
      } catch (e: Exception) {
        BugsnagKotlin.logMessage("SyncCoordinator#refreshFeeds")
        BugsnagKotlin.sendFatalException(e)
        updateSyncState(SyncState.Error(e))
      }
    }
  }

  suspend fun refreshFeeds(feedIds: List<String>) {
    try {
      updateSyncState(SyncState.InProgress(0f))
      feedIds.forEachIndexed { index, feedId ->
        val feed =
          withContext(dispatchersProvider.databaseRead) { rssRepository.feed(feedId = feedId) }

        if (feed != null) {
          rssRepository.fetchAndAddFeed(
            feedLink = feed.link,
            feedLastCleanUpAt = feed.lastCleanUpAt
          )
        }

        updateSyncState(SyncState.InProgress((index + 1).toFloat() / feedIds.size))
      }

      updateSyncState(SyncState.Complete)
      currentDateTimeSource.refresh()
    } catch (e: Exception) {
      BugsnagKotlin.logMessage("SyncCoordinator#refreshFeeds")
      BugsnagKotlin.sendFatalException(e)
      updateSyncState(SyncState.Error(e))
    }
  }

  suspend fun refreshFeed(id: String) {
    try {
      updateSyncState(SyncState.InProgress(0f))
      val feed = withContext(dispatchersProvider.databaseRead) { rssRepository.feed(feedId = id) }

      if (feed != null) {
        updateSyncState(SyncState.InProgress(0.5f))
        rssRepository.fetchAndAddFeed(feedLink = feed.link, feedLastCleanUpAt = feed.lastCleanUpAt)
      }

      updateSyncState(SyncState.Complete)
      currentDateTimeSource.refresh()
    } catch (e: Exception) {
      BugsnagKotlin.logMessage("SyncCoordinator#refreshFeed")
      BugsnagKotlin.sendFatalException(e)
      updateSyncState(SyncState.Error(e))
    }
  }

  private suspend fun updateSyncState(newState: SyncState) {
    syncMutex.withLock { _syncState.value = newState }
  }
}

sealed interface SyncState {
  data object Idle : SyncState

  data class InProgress(val progress: Float) : SyncState

  data object Complete : SyncState

  data class Error(val exception: Exception) : SyncState
}
