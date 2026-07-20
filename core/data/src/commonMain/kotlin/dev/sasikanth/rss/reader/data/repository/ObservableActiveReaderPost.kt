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

package dev.sasikanth.rss.reader.data.repository

import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject

/**
 * Post currently open in the reader, if any. Unlike [ObservableSelectedPost] this is not updated by
 * list scrolling, so lists beside the reader in a split layout can highlight the open post.
 *
 * Also tracks every post visited during the current reader session, so a split-layout list filtered
 * to unread can keep showing posts the reader has already marked read instead of yanking them out
 * from under the user mid-session. The set is cleared once the reader closes.
 */
@Inject
@AppScope
class ObservableActiveReaderPost {

  private val _activePostId = MutableStateFlow<String?>(null)
  val activePostId: StateFlow<String?> = _activePostId

  private val _openedPostIds = MutableStateFlow<Set<String>>(emptySet())
  val openedPostIds: StateFlow<Set<String>> = _openedPostIds

  fun updateActivePost(postId: String?) {
    _activePostId.value = postId
  }

  fun addOpenedPost(postId: String) {
    _openedPostIds.update { it + postId }
  }

  // A replaced reader can be cleared after its replacement has already published, so only
  // the owner may clear - otherwise the replacement's opened posts would be wiped too.
  fun clearIfActive(postId: String?) {
    if (_activePostId.compareAndSet(postId, null)) {
      _openedPostIds.value = emptySet()
    }
  }
}
