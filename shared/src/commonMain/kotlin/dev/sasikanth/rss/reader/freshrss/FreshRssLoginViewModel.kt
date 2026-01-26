/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.freshrss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.network.freshrss.FreshRssSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.FreshRSSSyncCoordinator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class FreshRssLoginViewModel(
  private val freshRssSource: FreshRssSource,
  private val userRepository: UserRepository,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: FreshRSSSyncCoordinator,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(FreshRssLoginState.DEFAULT)
  val state: StateFlow<FreshRssLoginState> = _state.asStateFlow()

  private var verifiedUserInfo: VerifiedUserInfo? = null

  fun onEvent(event: FreshRssLoginEvent) {
    when (event) {
      is FreshRssLoginEvent.OnUrlChanged -> _state.update { it.copy(url = event.url) }
      is FreshRssLoginEvent.OnUsernameChanged ->
        _state.update { it.copy(username = event.username) }
      is FreshRssLoginEvent.OnPasswordChanged ->
        _state.update { it.copy(password = event.password) }
      FreshRssLoginEvent.OnLoginClicked -> login()
      FreshRssLoginEvent.OnConfirmClearDataClicked -> confirmClearData()
      FreshRssLoginEvent.OnConfirmationDismissed ->
        _state.update { it.copy(showConfirmationDialog = false) }
    }
  }

  private fun login() {
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, error = null) }
      try {
        val endpoint = state.value.url.trim()
        val username = state.value.username.trim()
        val password = state.value.password.trim()

        val token =
          freshRssSource
            .login(endpoint = endpoint, username = username, password = password)
            ?.trim()

        if (token != null) {
          val userInfo = freshRssSource.userInfo(endpoint, token)
          verifiedUserInfo =
            VerifiedUserInfo(
              id = userInfo.userId,
              name = userInfo.userName,
              email = userInfo.userEmail,
              token = token,
              serverUrl = endpoint
            )
          _state.update { it.copy(isLoading = false, showConfirmationDialog = true) }
        } else {
          _state.update { it.copy(isLoading = false, error = FreshRssLoginError.LoginFailed) }
        }
      } catch (e: Exception) {
        _state.update {
          it.copy(isLoading = false, error = FreshRssLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private fun confirmClearData() {
    val userInfo = verifiedUserInfo ?: return
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, showConfirmationDialog = false) }
      try {
        Logger.d { "FreshRSS login: starting data clear and user save" }
        userRepository.deleteUser()
        rssRepository.deleteAllLocalData()
        settingsRepository.updateLastSyncedAt(Instant.DISTANT_PAST)

        userRepository.saveUser(
          id = userInfo.id,
          name = userInfo.name,
          email = userInfo.email,
          avatarUrl = null,
          token = userInfo.token,
          refreshToken = "",
          serverUrl = userInfo.serverUrl,
          serviceType = ServiceType.FRESH_RSS
        )

        Logger.d { "FreshRSS login: user saved, finishing login" }
        _state.update { it.copy(isLoading = false, loginSuccess = true) }
      } catch (e: Exception) {
        Logger.e(e) { "FreshRSS login: failed to clear data and save user" }
        _state.update {
          it.copy(isLoading = false, error = FreshRssLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private data class VerifiedUserInfo(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
    val serverUrl: String
  )
}

data class FreshRssLoginState(
  val url: String,
  val username: String,
  val password: String,
  val isLoading: Boolean,
  val loginSuccess: Boolean,
  val showConfirmationDialog: Boolean,
  val error: FreshRssLoginError?
) {
  companion object {
    val DEFAULT =
      FreshRssLoginState(
        url = "",
        username = "",
        password = "",
        isLoading = false,
        loginSuccess = false,
        showConfirmationDialog = false,
        error = null
      )
  }
}

sealed interface FreshRssLoginError {
  data object LoginFailed : FreshRssLoginError

  data class Unknown(val message: String) : FreshRssLoginError
}

sealed interface FreshRssLoginEvent {
  data class OnUrlChanged(val url: String) : FreshRssLoginEvent

  data class OnUsernameChanged(val username: String) : FreshRssLoginEvent

  data class OnPasswordChanged(val password: String) : FreshRssLoginEvent

  data object OnLoginClicked : FreshRssLoginEvent

  data object OnConfirmClearDataClicked : FreshRssLoginEvent

  data object OnConfirmationDismissed : FreshRssLoginEvent
}
