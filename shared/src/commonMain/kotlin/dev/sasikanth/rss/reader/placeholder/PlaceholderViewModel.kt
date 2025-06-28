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

package dev.sasikanth.rss.reader.placeholder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.data.repository.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlaceholderViewModel(private val rssRepository: RssRepository) : ViewModel() {

  private val _navigateToHome = MutableStateFlow(false)
  val navigateToHome: MutableStateFlow<Boolean>
    get() = _navigateToHome

  init {
    viewModelScope.launch {
      val numberOfFeeds = numberOfFeeds()
      if (numberOfFeeds != null) {
        _navigateToHome.value = true
      }
    }
  }

  private suspend fun numberOfFeeds(): Long? {
    return rssRepository.numberOfFeeds().firstOrNull()
  }

  fun markNavigateToHomeAsDone() {
    _navigateToHome.value = false
  }
}
