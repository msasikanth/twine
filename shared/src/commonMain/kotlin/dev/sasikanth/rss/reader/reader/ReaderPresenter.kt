/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.reader

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnDestroy
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  @Assisted private val readerScreenArgs: ReaderScreenArgs,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        readerScreenArgs = readerScreenArgs,
        rssRepository = rssRepository,
      )
    }

  init {
    lifecycle.doOnDestroy { presenterInstance.dispatch(ReaderEvent.MarkPostAsRead) }
  }

  internal val state = presenterInstance.state

  fun dispatch(event: ReaderEvent) {
    when (event) {
      ReaderEvent.BackClicked -> goBack()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val readerScreenArgs: ReaderScreenArgs,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(ReaderState.default(readerScreenArgs.post))
    val state: StateFlow<ReaderState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderState.default(readerScreenArgs.post)
      )

    fun dispatch(event: ReaderEvent) {
      when (event) {
        ReaderEvent.BackClicked -> {
          /* no-op */
        }
        ReaderEvent.TogglePostBookmark -> togglePostBookmark(readerScreenArgs.post.id)
        ReaderEvent.MarkPostAsRead -> markPostAsRead(readerScreenArgs.post.id)
        ReaderEvent.ArticleShortcutClicked -> articleShortcutClicked()
      }
    }

    private fun articleShortcutClicked() {
      _state.update { it.copy(fetchFullArticle = !it.fetchFullArticle) }
    }

    private fun markPostAsRead(postId: String) {
      coroutineScope.launch { rssRepository.updatePostReadStatus(read = true, id = postId) }
    }

    private fun togglePostBookmark(postId: String) {
      coroutineScope.launch {
        val isBookmarked = state.value.isBookmarked ?: false
        rssRepository.updateBookmarkStatus(bookmarked = !isBookmarked, id = postId)
        _state.update { it.copy(isBookmarked = !isBookmarked) }
      }
    }
  }
}

internal typealias ReaderPresenterFactory =
  (
    args: ReaderScreenArgs,
    ComponentContext,
    goBack: () -> Unit,
  ) -> ReaderPresenter

data class ReaderScreenArgs(
  val postIndex: Int,
  val post: PostWithMetadata,
  val fromScreen: FromScreen,
) {

  @Serializable
  enum class FromScreen {
    Home,
    Search,
    Bookmarks
  }
}
