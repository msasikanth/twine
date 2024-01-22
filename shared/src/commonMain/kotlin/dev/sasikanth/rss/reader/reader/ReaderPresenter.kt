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
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.readability.Readability
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.relativeDurationString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  @Assisted private val postLink: String,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        postLink = postLink
      )
    }

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(ReaderEvent.Init(postLink)) }
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
    private val dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    postLink: String,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(ReaderState.default(postLink))
    val state: StateFlow<ReaderState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderState.default(postLink)
      )

    fun dispatch(event: ReaderEvent) {
      when (event) {
        is ReaderEvent.Init -> init(event.postLink)
        ReaderEvent.BackClicked -> {
          /* no-op */
        }
      }
    }

    private fun init(postLink: String) {
      coroutineScope.launch {
        val post = rssRepository.post(postLink)
        val feed = rssRepository.feed(post.feedLink)

        if (!post.read) {
          rssRepository.updatePostReadStatus(read = true, link = postLink)
        }

        // This is done for backward compatibility
        val content = post.rawContent ?: post.description

        val htmlContent =
          if (content.isNotBlank()) {
            // If the content parsed by readability is not an HTML, it will return
            // null. In that scenario we simply pass the original content
            extractArticleHtmlContent(postLink, content) ?: content
          } else {
            null
          }

        _state.update {
          it.copy(
            title = post.title,
            content = htmlContent,
            publishedAt = post.date.relativeDurationString(),
            feed = feed
          )
        }
      }
    }

    private suspend fun extractArticleHtmlContent(feedLink: String, content: String) =
      withContext(dispatchersProvider.io) { Readability(feedLink, content) }.parse().content
  }
}
