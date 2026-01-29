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

package dev.sasikanth.rss.reader.reader.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import dev.sasikanth.rss.reader.core.model.local.PostContent
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.network.FullArticleFetcher
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.reader.page.ui.ReaderContent
import dev.sasikanth.rss.reader.reader.page.ui.ReaderProcessingProgress
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPageViewModel(
  dispatchersProvider: DispatchersProvider,
  private val postContentRepository: PostContentRepository,
  private val fullArticleFetcher: FullArticleFetcher,
  @Assisted private val readerPost: PostWithMetadata,
) : ViewModel() {

  private val _postContent = MutableStateFlow<PostContent?>(null)
  val postContent: StateFlow<PostContent?> = _postContent

  private val _contentState = MutableStateFlow("")
  val contentState =
    _contentState
      .filter { it.isNotBlank() }
      .distinctUntilChanged()
      .flatMapLatest {
        parseMarkdownFlow(it).onEach { state ->
          if (state !is State.Loading) {
            _parsingProgress.value = ReaderProcessingProgress.Idle
          }
        }
      }
      .flowOn(dispatchersProvider.default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State.Loading())

  private val _excerptState = MutableStateFlow("")
  val excerptState: StateFlow<String> = _excerptState

  private val _parsingProgress = MutableStateFlow(ReaderProcessingProgress.Loading)
  val parsingProgress: StateFlow<ReaderProcessingProgress> = _parsingProgress

  private val _showFullArticle = MutableStateFlow(readerPost.alwaysFetchFullArticle)
  val showFullArticle: StateFlow<Boolean> = _showFullArticle

  init {
    loadPostContent()
    loadFullArticle()
  }

  fun onParsingComplete(readerContent: ReaderContent) {
    viewModelScope.launch {
      _contentState.update { it -> readerContent.content ?: it }
      _excerptState.update { it -> readerContent.excerpt ?: it }
    }
  }

  fun loadFullArticle() {
    if (_postContent.value?.fullArticleHtml != null) return

    viewModelScope.launch {
      _parsingProgress.value = ReaderProcessingProgress.Loading

      val article = fullArticleFetcher.fetch(readerPost.link, readerPost.remoteId).getOrNull()
      if (article == null) {
        _parsingProgress.value = ReaderProcessingProgress.Idle
        return@launch
      }
      postContentRepository.updateFullArticleContent(readerPost.id, article)
    }
  }

  fun toggleFullArticle() {
    _showFullArticle.value = !(_showFullArticle.value)
    if (_showFullArticle.value) {
      loadFullArticle()
    }
  }

  private fun loadPostContent() {
    postContentRepository
      .postContent(readerPost.id)
      .onEach { postContent ->
        _postContent.value = postContent
        if (postContent?.postContent.isNullOrBlank()) {
          _parsingProgress.value = ReaderProcessingProgress.Idle
        }
      }
      .launchIn(viewModelScope)
  }
}
