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

package dev.sasikanth.rss.reader.miniflux

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.miniflux.MinifluxSyncCoordinator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class MinifluxLoginViewModel(
  private val minifluxSource: MinifluxSource,
  private val userRepository: UserRepository,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: MinifluxSyncCoordinator,
  private val refreshPolicy: RefreshPolicy,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(MinifluxLoginState.DEFAULT)
  val state: StateFlow<MinifluxLoginState> = _state.asStateFlow()

  private var verifiedUserInfo: VerifiedUserInfo? = null

  fun onEvent(event: MinifluxLoginEvent) {
    when (event) {
      is MinifluxLoginEvent.OnUrlChanged -> _state.update { it.copy(url = event.url) }
      is MinifluxLoginEvent.OnApiKeyChanged -> _state.update { it.copy(apiKey = event.apiKey) }
      MinifluxLoginEvent.OnLoginClicked -> login()
      MinifluxLoginEvent.OnConfirmClearDataClicked -> confirmClearData()
      MinifluxLoginEvent.OnConfirmationDismissed ->
        _state.update { it.copy(showConfirmationDialog = false) }
    }
  }

  private fun login() {
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, error = null) }
      try {
        val endpoint = state.value.url.trim()
        val apiKey = state.value.apiKey.trim()

        val userInfo = minifluxSource.verify(endpoint = endpoint, token = apiKey)

        if (userInfo != null) {
          verifiedUserInfo =
            VerifiedUserInfo(
              id = userInfo.id.toString(),
              name = userInfo.username,
              token = apiKey,
              serverUrl = endpoint
            )
          _state.update { it.copy(isLoading = false, showConfirmationDialog = true) }
        } else {
          _state.update { it.copy(isLoading = false, error = MinifluxLoginError.LoginFailed) }
        }
      } catch (e: Exception) {
        _state.update {
          it.copy(isLoading = false, error = MinifluxLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private fun confirmClearData() {
    val userInfo = verifiedUserInfo ?: return
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, showConfirmationDialog = false) }
      try {
        Logger.d { "Miniflux login: starting data clear and user save" }
        userRepository.deleteUser()
        rssRepository.deleteAllLocalData()
        refreshPolicy.clear()

        userRepository.saveUser(
          id = userInfo.id,
          name = userInfo.name,
          email = "", // Miniflux doesn't provide email in user info
          avatarUrl = null,
          token = userInfo.token,
          refreshToken = "",
          serverUrl = userInfo.serverUrl,
          serviceType = ServiceType.MINIFLUX
        )

        Logger.d { "Miniflux login: user saved, finishing login" }
        _state.update { it.copy(isLoading = false, loginSuccess = true) }
      } catch (e: Exception) {
        Logger.e(e) { "Miniflux login: failed to clear data and save user" }
        _state.update {
          it.copy(isLoading = false, error = MinifluxLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private data class VerifiedUserInfo(
    val id: String,
    val name: String,
    val token: String,
    val serverUrl: String
  )
}

data class MinifluxLoginState(
  val url: String,
  val apiKey: String,
  val isLoading: Boolean,
  val loginSuccess: Boolean,
  val showConfirmationDialog: Boolean,
  val error: MinifluxLoginError?
) {
  companion object {
    val DEFAULT =
      MinifluxLoginState(
        url = "",
        apiKey = "",
        isLoading = false,
        loginSuccess = false,
        showConfirmationDialog = false,
        error = null
      )
  }
}

sealed interface MinifluxLoginError {
  data object LoginFailed : MinifluxLoginError

  data class Unknown(val message: String) : MinifluxLoginError
}

sealed interface MinifluxLoginEvent {
  data class OnUrlChanged(val url: String) : MinifluxLoginEvent

  data class OnApiKeyChanged(val apiKey: String) : MinifluxLoginEvent

  data object OnLoginClicked : MinifluxLoginEvent

  data object OnConfirmClearDataClicked : MinifluxLoginEvent

  data object OnConfirmationDismissed : MinifluxLoginEvent
}
