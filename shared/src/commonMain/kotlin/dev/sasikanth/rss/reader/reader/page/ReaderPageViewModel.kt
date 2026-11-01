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
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.media.AudioPlayer
import dev.sasikanth.rss.reader.reader.redability.ReadabilityRunner
import dev.sasikanth.rss.reader.reader.redability.fallbackReadabilityResult
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.coroutines.cancellation.CancellationException
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
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class ReaderPageViewModel(
  private val dispatchersProvider: DispatchersProvider,
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
            isParsingContent.value = false
          }
        }
      }
      .flowOn(dispatchersProvider.default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State.Loading())

  private val _excerptState = MutableStateFlow("")
  val excerptState: StateFlow<String> = _excerptState

  private val isParsingContent = MutableStateFlow(true)
  private val isFetchingFullArticle = MutableStateFlow(false)
  val parsingProgress: StateFlow<ReaderProcessingProgress> =
    combine(isParsingContent, isFetchingFullArticle) { isParsing, isFetching ->
        if (isParsing || isFetching) {
          ReaderProcessingProgress.Loading
        } else {
          ReaderProcessingProgress.Idle
        }
      }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ReaderProcessingProgress.Loading,
      )

  val videoLink: String? = readerPost.link.takeIf { UrlUtils.youTubeVideoId(it) != null }

  private val canLoadFullArticle = videoLink == null

  private val _showFullArticle =
    MutableStateFlow(readerPost.alwaysFetchFullArticle && canLoadFullArticle)
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
    if (!canLoadFullArticle) return

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
      val postContent = postContentRepository.postContent(readerPost.id).firstOrNull()
      if (postContent != null && !(postContent.articleContent.isNullOrBlank())) {
        return@launch
      }

      isFetchingFullArticle.value = true
      try {
        val article = fullArticleFetcher.fetch(readerPost.link, readerPost.remoteId).getOrNull()
        if (article.isNullOrBlank()) {
          return@launch
        }

        if (postContent == null) {
          postContentRepository.upsert(
            postId = readerPost.id,
            feedContent = readerPost.description,
            articleContent = article,
            createdAt = readerPost.createdAt,
          )
        } else {
          postContentRepository.updateFullArticleContent(readerPost.id, article)
        }
      } finally {
        isFetchingFullArticle.value = false
      }
    }
  }

  private fun loadPostContent() {
    combine(postContentRepository.postContent(readerPost.id), showFullArticle) {
        postContent,
        alwaysFetchFullArticle ->
        if (alwaysFetchFullArticle) {
          postContent?.articleContent?.takeUnless { it.isBlank() }
            ?: postContent?.feedContent?.takeUnless { it.isBlank() }
            ?: readerPost.description
        } else {
          postContent?.feedContent?.takeUnless { it.isBlank() } ?: readerPost.description
        }
      }
      .distinctUntilChanged()
      .onEach { content ->
        isParsingContent.value = true
        val readabilityResult =
          if (content.isBlank()) {
            ReadabilityResult(content = "")
          } else {
            try {
              readabilityRunner.parseHtml(
                link = readerPost.link,
                content = content,
                image = readerPost.imageUrl,
              )
            } catch (e: CancellationException) {
              throw e
            } catch (e: Exception) {
              Logger.e(e) { "Failed to parse reader content" }
              withContext(dispatchersProvider.default) { fallbackReadabilityResult(content) }
            }
          }

        onParsingComplete(readabilityResult)
      }
      .launchIn(viewModelScope)
  }

  private fun onParsingComplete(result: ReadabilityResult) {
    viewModelScope.launch {
      _contentState.update { it -> result.content ?: it }
      _excerptState.update { it -> result.excerpt ?: it }
      isParsingContent.value = false
    }
  }
}

private val COMPLETION_TOLERANCE = 5.seconds.inWholeMilliseconds

enum class ReaderProcessingProgress {
  Loading,
  Idle,
}
