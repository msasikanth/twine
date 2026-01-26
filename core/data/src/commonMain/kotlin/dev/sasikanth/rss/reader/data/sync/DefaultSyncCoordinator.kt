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

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class DefaultSyncCoordinator(
  private val localSyncCoordinator: LocalSyncCoordinator,
  private val dropboxSyncCoordinator: DropboxSyncCoordinator,
  private val dropboxSyncProvider: DropboxCloudServiceProvider,
  private val freshRSSSyncCoordinator: FreshRSSSyncCoordinator,
  private val freshRssSyncProvider: FreshRssSyncProvider,
  dispatchersProvider: DispatchersProvider,
) : SyncCoordinator {

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  @OptIn(ExperimentalCoroutinesApi::class)
  override val syncState: StateFlow<SyncState> =
    combine(dropboxSyncProvider.isSignedIn(), freshRssSyncProvider.isSignedIn()) {
        dropboxSignedIn,
        freshRssSignedIn ->
        when {
          freshRssSignedIn -> freshRSSSyncCoordinator
          dropboxSignedIn -> dropboxSyncCoordinator
          else -> localSyncCoordinator
        }
      }
      .flatMapLatest { it.syncState }
      .stateIn(scope, SharingStarted.WhileSubscribed(), SyncState.Idle)

  override suspend fun pull(): Boolean {
    return if (freshRssSyncProvider.isSignedInImmediate()) {
      freshRSSSyncCoordinator.pull()
    } else if (dropboxSyncProvider.isSignedInImmediate()) {
      dropboxSyncCoordinator.pull()
    } else {
      localSyncCoordinator.pull()
    }
  }

  override suspend fun pull(feedIds: List<String>): Boolean {
    return if (freshRssSyncProvider.isSignedInImmediate()) {
      freshRSSSyncCoordinator.pull(feedIds)
    } else if (dropboxSyncProvider.isSignedInImmediate()) {
      dropboxSyncCoordinator.pull(feedIds)
    } else {
      localSyncCoordinator.pull(feedIds)
    }
  }

  override suspend fun pull(feedId: String): Boolean {
    return if (freshRssSyncProvider.isSignedInImmediate()) {
      freshRSSSyncCoordinator.pull(feedId)
    } else if (dropboxSyncProvider.isSignedInImmediate()) {
      dropboxSyncCoordinator.pull(feedId)
    } else {
      localSyncCoordinator.pull(feedId)
    }
  }

  override suspend fun push(): Boolean {
    return when {
      freshRssSyncProvider.isSignedInImmediate() -> freshRSSSyncCoordinator.push()
      dropboxSyncProvider.isSignedInImmediate() -> dropboxSyncCoordinator.push()
      else -> localSyncCoordinator.push()
    }
  }
}
