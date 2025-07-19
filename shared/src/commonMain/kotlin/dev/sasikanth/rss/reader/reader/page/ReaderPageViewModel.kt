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
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPageViewModel(
  dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _contentState = MutableStateFlow("")
  val contentState =
    _contentState
      .filter { it.isNotBlank() }
      .distinctUntilChanged()
      .flatMapLatest { parseMarkdownFlow(it) }
      .flowOn(dispatchersProvider.default)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), State.Loading())

  private val _excerptState = MutableStateFlow("")
  val excerptState: StateFlow<String> = _excerptState

  private val _parsingProgress = MutableStateFlow(ReaderProcessingProgress.Idle)
  val parsingProgress: StateFlow<ReaderProcessingProgress> = _parsingProgress

  fun onParsingComplete(readerContent: ReaderContent) {
    _parsingProgress.value = ReaderProcessingProgress.Idle
    _contentState.value = readerContent.content.orEmpty()
    _excerptState.value = readerContent.excerpt.orEmpty()
  }
}
