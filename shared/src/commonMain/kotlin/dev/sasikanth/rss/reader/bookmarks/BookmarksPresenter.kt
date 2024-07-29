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

import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias BookmarksPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
    openReaderView: (PostWithMetadata) -> Unit,
  ) -> BookmarksPresenter

@Inject
class BookmarksPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
  @Assisted private val openReaderView: (post: PostWithMetadata) -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(dispatchersProvider = dispatchersProvider, rssRepository = rssRepository)
    }

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(BookmarksEvent.Init) }
  }

  internal val state = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()

  fun dispatch(event: BookmarksEvent) {
    when (event) {
      BookmarksEvent.BackClicked -> goBack()
      is BookmarksEvent.OnPostClicked ->
        presenterInstance.onPostClicked(
          post = event.post,
          openReaderView = { openReaderView(it) },
          openLink = { presenterInstance.openLink(it) }
        )
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
    val effects = MutableSharedFlow<BookmarksEffect>()

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
        is BookmarksEvent.OnPostClicked -> {
          // no-op
        }
        is BookmarksEvent.TogglePostReadStatus -> togglePostReadStatus(event.postId, event.postRead)
      }
    }

    private fun togglePostReadStatus(postId: String, postRead: Boolean) {
      coroutineScope.launch { rssRepository.updatePostReadStatus(read = !postRead, id = postId) }
    }

    fun onPostClicked(
      post: PostWithMetadata,
      openReaderView: (post: PostWithMetadata) -> Unit,
      openLink: (postLink: String) -> Unit
    ) {
      coroutineScope.launch {
        val hasPost = rssRepository.hasPost(post.id)
        val hasFeed = rssRepository.hasFeed(post.sourceId)

        if (hasPost && hasFeed) {
          openReaderView(post)
        } else {
          openLink(post.link)
        }
      }
    }

    private fun onPostBookmarkClicked(post: PostWithMetadata) {
      coroutineScope.launch {
        if (rssRepository.hasFeed(post.sourceId)) {
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
          .cachedIn(coroutineScope)

      _state.update { it.copy(bookmarks = bookmarks) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }

    fun openLink(link: String) {
      coroutineScope.launch { effects.emit(OpenLink(link)) }
    }
  }
}
