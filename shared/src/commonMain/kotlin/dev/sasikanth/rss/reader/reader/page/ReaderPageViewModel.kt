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

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import dev.sasikanth.rss.reader.core.model.local.ReadabilityResult
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.network.FullArticleFetcher
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class ReaderPageViewModel(
  dispatchersProvider: DispatchersProvider,
  private val postContentRepository: PostContentRepository,
  private val fullArticleFetcher: FullArticleFetcher,
  private val readabilityRunner: ReadabilityRunner,
  val audioPlayer: AudioPlayer,
  @Assisted private val readerPost: ResolvedPost,
) : ViewModel() {

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
    // Only prefetch the full article when it will actually be shown; fetching it
    // eagerly here costs a network request + DB write for every page the pager
    // preloads. toggleFullArticle() fetches it on demand otherwise.
    if (_showFullArticle.value) {
      loadFullArticle()
    }
  }

  fun toggleFullArticle() {
    _showFullArticle.value = !(_showFullArticle.value)
    if (_showFullArticle.value) {
      loadFullArticle()
    }
  }

  fun playAudio() {
    val audioUrl = readerPost.audioUrl ?: return
    val currentPlayingUrl = audioPlayer.playbackState.value.playingUrl
    if (currentPlayingUrl == audioUrl) {
      audioPlayer.resume()
      return
    }

    val coverUrl =
      if (readerPost.imageUrl.isNullOrBlank()) {
        readerPost.feedIcon
      } else {
        readerPost.imageUrl
      }
    audioPlayer.play(
      url = audioUrl,
      title = readerPost.title,
      artist = readerPost.feedName,
      coverUrl = coverUrl,
      postId = readerPost.id,
      initialPosition = resumePosition(),
    )
  }

  private fun resumePosition(): Long {
    val progress = readerPost.audioProgress
    val duration = readerPost.audioDuration
    val isFinished = duration > 0 && progress >= duration - COMPLETION_TOLERANCE
    return if (isFinished) 0L else progress
  }

  private fun loadFullArticle() {
    viewModelScope.launch {
      _parsingProgress.value = ReaderProcessingProgress.Loading

      val fullArticle = postContentRepository.postContent(readerPost.id).firstOrNull()
      if (fullArticle != null && !(fullArticle.articleContent.isNullOrBlank())) {
        return@launch
      }

      val article = fullArticleFetcher.fetch(readerPost.link, readerPost.remoteId).getOrNull()
      if (article == null) {
        _parsingProgress.value = ReaderProcessingProgress.Idle
        return@launch
      }
      postContentRepository.updateFullArticleContent(readerPost.id, article)
    }
  }

  private fun loadPostContent() {
    combine(postContentRepository.postContent(readerPost.id), showFullArticle) {
        postContent,
        alwaysFetchFullArticle ->
        val content =
          if (alwaysFetchFullArticle) {
            postContent?.articleContent ?: postContent?.feedContent
          } else {
            postContent?.feedContent ?: readerPost.description
          }
        content
      }
      .distinctUntilChanged()
      .onEach { content ->
        val readabilityResult =
          try {
            readabilityRunner.parseHtml(
              link = readerPost.link,
              content = content ?: "",
              image = readerPost.imageUrl,
            )
          } catch (e: Exception) {
            Logger.e(e) { "Failed to parse reader content" }
            ReadabilityResult()
          }

        onParsingComplete(readabilityResult)
      }
      .launchIn(viewModelScope)
  }

  private fun onParsingComplete(result: ReadabilityResult) {
    viewModelScope.launch {
      _contentState.update { it -> result.content ?: it }
      _excerptState.update { it -> result.excerpt ?: it }
      _parsingProgress.value = ReaderProcessingProgress.Idle
    }
  }
}

private val COMPLETION_TOLERANCE = 5.seconds.inWholeMilliseconds

enum class ReaderProcessingProgress {
  Loading,
  Idle,
}
