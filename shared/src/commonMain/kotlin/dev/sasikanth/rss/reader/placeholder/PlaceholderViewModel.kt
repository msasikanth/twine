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
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class PlaceholderViewModel(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository
) : ViewModel() {

  private val _navigateToHome = MutableStateFlow(false)
  val navigateToHome: StateFlow<Boolean>
    get() = _navigateToHome

  private val _navigateToOnboarding = MutableStateFlow(false)
  val navigateToOnboarding: StateFlow<Boolean>
    get() = _navigateToOnboarding

  init {
    viewModelScope.launch {
      val isOnboardingDone = settingsRepository.isOnboardingDone.first()
      if (isOnboardingDone) {
        _navigateToHome.value = true
      } else {
        val numberOfFeeds = numberOfFeeds() ?: 0
        if (numberOfFeeds > 0) {
          settingsRepository.completeOnboarding()
          _navigateToHome.value = true
        } else {
          _navigateToOnboarding.value = true
        }
      }
    }
  }

  private suspend fun numberOfFeeds(): Long? {
    return rssRepository.numberOfFeeds().firstOrNull()
  }

  fun markNavigateToHomeAsDone() {
    _navigateToHome.value = false
  }

  fun markNavigateToOnboardingAsDone() {
    _navigateToOnboarding.value = false
  }
}
