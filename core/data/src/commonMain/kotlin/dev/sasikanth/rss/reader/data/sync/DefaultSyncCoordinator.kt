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

import dev.sasikanth.rss.reader.data.sync.dropbox.DropboxCloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.dropbox.DropboxSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.freshrss.FreshRSSSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.freshrss.FreshRssSyncProvider
import dev.sasikanth.rss.reader.data.sync.local.LocalSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.miniflux.MinifluxSyncCoordinator
import dev.sasikanth.rss.reader.data.sync.miniflux.MinifluxSyncProvider
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
  private val minifluxSyncCoordinator: MinifluxSyncCoordinator,
  private val minifluxSyncProvider: MinifluxSyncProvider,
  dispatchersProvider: DispatchersProvider,
) : SyncCoordinator {

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  @OptIn(ExperimentalCoroutinesApi::class)
  override val syncState: StateFlow<SyncState> =
    combine(
        dropboxSyncProvider.isSignedIn(),
        freshRssSyncProvider.isSignedIn(),
        minifluxSyncProvider.isSignedIn()
      ) { dropboxSignedIn, freshRssSignedIn, minifluxSignedIn ->
        when {
          freshRssSignedIn -> freshRSSSyncCoordinator
          minifluxSignedIn -> minifluxSyncCoordinator
          dropboxSignedIn -> dropboxSyncCoordinator
          else -> localSyncCoordinator
        }
      }
      .flatMapLatest { it.syncState }
      .stateIn(scope, SharingStarted.WhileSubscribed(), SyncState.Idle)

  override suspend fun pull(): Boolean {
    return when {
      freshRssSyncProvider.isSignedInImmediate() -> freshRSSSyncCoordinator.pull()
      minifluxSyncProvider.isSignedInImmediate() -> minifluxSyncCoordinator.pull()
      dropboxSyncProvider.isSignedInImmediate() -> dropboxSyncCoordinator.pull()
      else -> localSyncCoordinator.pull()
    }
  }

  override suspend fun pull(feedIds: List<String>): Boolean {
    return when {
      freshRssSyncProvider.isSignedInImmediate() -> freshRSSSyncCoordinator.pull(feedIds)
      minifluxSyncProvider.isSignedInImmediate() -> minifluxSyncCoordinator.pull(feedIds)
      dropboxSyncProvider.isSignedInImmediate() -> dropboxSyncCoordinator.pull(feedIds)
      else -> localSyncCoordinator.pull(feedIds)
    }
  }

  override suspend fun pull(feedId: String): Boolean {
    return when {
      freshRssSyncProvider.isSignedInImmediate() -> freshRSSSyncCoordinator.pull(feedId)
      minifluxSyncProvider.isSignedInImmediate() -> minifluxSyncCoordinator.pull(feedId)
      dropboxSyncProvider.isSignedInImmediate() -> dropboxSyncCoordinator.pull(feedId)
      else -> localSyncCoordinator.pull(feedId)
    }
  }

  override suspend fun push(): Boolean {
    return when {
      freshRssSyncProvider.isSignedInImmediate() -> freshRSSSyncCoordinator.push()
      minifluxSyncProvider.isSignedInImmediate() -> minifluxSyncCoordinator.push()
      dropboxSyncProvider.isSignedInImmediate() -> dropboxSyncCoordinator.push()
      else -> localSyncCoordinator.push()
    }
  }
}
