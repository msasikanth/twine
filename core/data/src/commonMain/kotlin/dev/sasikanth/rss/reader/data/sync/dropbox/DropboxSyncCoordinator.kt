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
package dev.sasikanth.rss.reader.data.sync.dropbox

import dev.sasikanth.rss.reader.data.sync.FileCloudSyncService
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.data.sync.local.LocalSyncCoordinator
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class DropboxSyncCoordinator(
  private val fileCloudSyncService: FileCloudSyncService,
  private val dropboxSyncProvider: DropboxCloudServiceProvider,
  private val localSyncCoordinator: LocalSyncCoordinator,
) : SyncCoordinator {

  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

  override suspend fun pull(): Boolean {
    _syncState.value = SyncState.InProgress(0f)
    val syncResult = fileCloudSyncService.sync(dropboxSyncProvider)
    _syncState.value = SyncState.InProgress(0.5f)
    localSyncCoordinator.pull()
    _syncState.value = SyncState.Complete
    return syncResult
  }

  override suspend fun pull(feedIds: List<String>): Boolean {
    _syncState.value = SyncState.InProgress(0f)
    val result = localSyncCoordinator.pull(feedIds)
    _syncState.value = SyncState.Complete
    return result
  }

  override suspend fun pull(feedId: String): Boolean {
    _syncState.value = SyncState.InProgress(0f)
    val result = localSyncCoordinator.pull(feedId)
    _syncState.value = SyncState.Complete
    return result
  }

  override suspend fun push(): Boolean {
    _syncState.value = SyncState.InProgress(0f)
    val result = fileCloudSyncService.sync(dropboxSyncProvider)
    _syncState.value = SyncState.Complete
    return result
  }
}
