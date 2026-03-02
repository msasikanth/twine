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
package dev.sasikanth.rss.reader.data.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncCoordinator {

  val syncState: StateFlow<SyncState>

  /** Syncs feeds and posts from the remote source and updates the local database. */
  suspend fun pull(): Boolean

  /**
   * Triggers a sync from the remote source and updates the local database. This is a non-suspending
   * call and the sync will happen in the background.
   */
  fun triggerPull() {}

  /**
   * Fetches the latest posts for a list of specific feeds from the remote source and updates the
   * local database.
   *
   * @param feedIds A list of unique identifiers of the feeds to be updated.
   */
  suspend fun pull(feedIds: List<String>): Boolean

  /**
   * Triggers a fetch of the latest posts for a list of specific feeds from the remote source and
   * updates the local database. This is a non-suspending call and the sync will happen in the
   * background.
   *
   * @param feedIds A list of unique identifiers of the feeds to be updated.
   */
  fun triggerPull(feedIds: List<String>) {}

  /**
   * Fetches the latest posts for a specific feed from the remote source and updates the local
   * database.
   *
   * @param feedId The unique identifier of the feed to be updated.
   */
  suspend fun pull(feedId: String): Boolean

  /**
   * Triggers a fetch of the latest posts for a specific feed from the remote source and updates the
   * local database. This is a non-suspending call and the sync will happen in the background.
   *
   * @param feedId The unique identifier of the feed to be updated.
   */
  fun triggerPull(feedId: String) {}

  /** Pushes the local changes to the remote source. */
  suspend fun push(): Boolean

  /**
   * Triggers a push of local changes to the remote source. This is a non-suspending call and the
   * push will happen in the background.
   */
  fun triggerPush() {}
}

sealed interface SyncState {
  data object Idle : SyncState

  data class InProgress(val progress: Float) : SyncState

  data object Complete : SyncState

  data class Error(val exception: Exception) : SyncState
}
