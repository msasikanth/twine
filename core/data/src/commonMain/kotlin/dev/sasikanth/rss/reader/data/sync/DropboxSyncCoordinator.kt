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
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class DropboxSyncCoordinator(
  private val fileCloudSyncService: FileCloudSyncService,
  private val dropboxSyncProvider: DropboxCloudServiceProvider,
  private val localSyncCoordinator: LocalSyncCoordinator,
) : SyncCoordinator {

  override val syncState: StateFlow<SyncState> = localSyncCoordinator.syncState

  override suspend fun pull() {
    fileCloudSyncService.sync(dropboxSyncProvider)
    localSyncCoordinator.pull()
  }

  override suspend fun pull(feedIds: List<String>) {
    localSyncCoordinator.pull(feedIds)
  }

  override suspend fun pull(feedId: String) {
    localSyncCoordinator.pull(feedId)
  }

  override suspend fun push(): Boolean {
    return fileCloudSyncService.sync(dropboxSyncProvider)
  }
}
