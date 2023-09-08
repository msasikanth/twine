/*
 * Copyright 2023 Sasikanth Miriyampalli
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
package dev.sasikanth.rss.reader.bookmarks

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class BookmarksPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(dispatchersProvider = dispatchersProvider, rssRepository = rssRepository)
    }

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(BookmarksEvent.Init) }
  }

  val state = presenterInstance.state

  fun dispatch(event: BookmarksEvent) {
    when (event) {
      BookmarksEvent.BackClicked -> goBack()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(BookmarksState.DEFAULT)
    val state: StateFlow<BookmarksState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookmarksState.DEFAULT
      )

    fun dispatch(event: BookmarksEvent) {
      when (event) {
        BookmarksEvent.Init -> init()
        BookmarksEvent.BackClicked -> {
          /* no-op */
        }
        is BookmarksEvent.OnPostBookmarkClick -> onPostBookmarkClicked(event.post)
      }
    }

    private fun onPostBookmarkClicked(post: PostWithMetadata) {
      coroutineScope.launch {
        if (rssRepository.hasFeed(post.feedLink)) {
          rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, link = post.link)
        } else {
          rssRepository.deleteBookmark(link = post.link)
        }
      }
    }

    private fun init() {
      rssRepository
        .bookmarks()
        .onEach { bookmarks -> _state.update { it.copy(bookmarks = bookmarks.toImmutableList()) } }
        .launchIn(coroutineScope)
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
