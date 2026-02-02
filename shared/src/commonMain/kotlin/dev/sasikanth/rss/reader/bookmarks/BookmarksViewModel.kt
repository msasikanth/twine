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
package dev.sasikanth.rss.reader.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class BookmarksViewModel(private val rssRepository: RssRepository) : ViewModel() {

  private val _state = MutableStateFlow(BookmarksState.DEFAULT)
  val state: StateFlow<BookmarksState>
    get() = _state

  init {
    init()
  }

  fun dispatch(event: BookmarksEvent) {
    when (event) {
      is BookmarksEvent.OnPostBookmarkClick -> onPostBookmarkClicked(event.post)
      is BookmarksEvent.UpdatePostReadStatus ->
        updatePostReadStatus(event.postId, event.updatedReadStatus)
    }
  }

  private fun updatePostReadStatus(postId: String, updatedReadStatus: Boolean) {
    viewModelScope.launch {
      rssRepository.updatePostReadStatus(read = updatedReadStatus, id = postId)
    }
  }

  private fun onPostBookmarkClicked(post: ResolvedPost) {
    viewModelScope.launch {
      if (rssRepository.hasFeed(post.sourceId) && rssRepository.hasPost(post.id)) {
        rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
      } else {
        rssRepository.deleteBookmark(id = post.id)
      }
    }
  }

  private fun init() {
    val bookmarks =
      createPager(config = createPagingConfig(pageSize = 20)) { rssRepository.bookmarks() }
        .flow
        .cachedIn(viewModelScope)

    _state.update { it.copy(bookmarks = bookmarks) }
  }
}
