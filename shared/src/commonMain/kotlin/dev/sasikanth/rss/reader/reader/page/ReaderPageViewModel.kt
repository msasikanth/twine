/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.reader.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import dev.sasikanth.rss.reader.core.model.local.PostContent
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
  @Assisted private val postId: String,
) : ViewModel() {

  init {
    loadPostContent()
  }

  private val _postContent = MutableStateFlow<PostContent?>(null)
  val postContent: StateFlow<PostContent?> =
    _postContent.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

  fun onParsingComplete(readerContent: ReaderContent) {
    viewModelScope.launch {
      _contentState.value = readerContent.content.orEmpty()
      _excerptState.update { it -> it.ifBlank { readerContent.excerpt.orEmpty() } }
    }
  }

  fun loadFullArticle(postUrl: String) {
    if (_postContent.value?.fullArticleHtml != null) return

    viewModelScope.launch {
      _parsingProgress.value = ReaderProcessingProgress.Loading
      val article = fullArticleFetcher.fetch(postUrl).getOrNull() ?: return@launch
      postContentRepository.updateFullArticleContent(postId, article)
    }
  }

  private fun loadPostContent() {
    postContentRepository
      .postContent(postId)
      .onEach { _postContent.value = it }
      .launchIn(viewModelScope)
  }
}
