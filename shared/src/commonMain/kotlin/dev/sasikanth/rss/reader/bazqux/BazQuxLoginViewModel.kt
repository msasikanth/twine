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

package dev.sasikanth.rss.reader.bazqux

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.network.freshrss.FreshRssSource
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class BazQuxLoginViewModel(
  private val freshRssSource: FreshRssSource,
  private val userRepository: UserRepository,
  private val rssRepository: RssRepository,
  private val refreshPolicy: RefreshPolicy,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  companion object {
    const val BAZQUX_ENDPOINT = "https://bazqux.com/"
  }

  private val _state = MutableStateFlow(BazQuxLoginState.DEFAULT)
  val state: StateFlow<BazQuxLoginState> = _state.asStateFlow()

  private var verifiedUserInfo: VerifiedUserInfo? = null

  fun onEvent(event: BazQuxLoginEvent) {
    when (event) {
      is BazQuxLoginEvent.OnUsernameChanged -> _state.update { it.copy(username = event.username) }
      is BazQuxLoginEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password) }
      BazQuxLoginEvent.OnLoginClicked -> login()
      BazQuxLoginEvent.OnConfirmClearDataClicked -> confirmClearData()
      BazQuxLoginEvent.OnConfirmationDismissed ->
        _state.update { it.copy(showConfirmationDialog = false) }
    }
  }

  private fun login() {
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, error = null) }
      try {
        val username = state.value.username.trim()
        val password = state.value.password.trim()

        val token =
          freshRssSource
            .login(endpoint = BAZQUX_ENDPOINT, username = username, password = password)
            ?.trim()

        if (token != null) {
          val userInfo = freshRssSource.userInfo(BAZQUX_ENDPOINT, token)
          verifiedUserInfo =
            VerifiedUserInfo(
              id = userInfo.userId.ifBlank { username },
              name = userInfo.userName.ifBlank { username },
              email = userInfo.userEmail,
              token = token,
            )
          _state.update { it.copy(isLoading = false, showConfirmationDialog = true) }
        } else {
          _state.update { it.copy(isLoading = false, error = BazQuxLoginError.LoginFailed) }
        }
      } catch (e: Exception) {
        _state.update {
          it.copy(isLoading = false, error = BazQuxLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private fun confirmClearData() {
    val userInfo = verifiedUserInfo ?: return
    viewModelScope.launch(dispatchersProvider.io) {
      _state.update { it.copy(isLoading = true, showConfirmationDialog = false) }
      try {
        Logger.d { "BazQux login: starting data clear and user save" }
        userRepository.deleteUser()
        rssRepository.deleteAllLocalData()
        refreshPolicy.clear()

        userRepository.saveUser(
          id = userInfo.id,
          name = userInfo.name,
          email = userInfo.email,
          avatarUrl = null,
          token = userInfo.token,
          refreshToken = "",
          serverUrl = BAZQUX_ENDPOINT,
          serviceType = ServiceType.BAZQUX,
        )

        Logger.d { "BazQux login: user saved, finishing login" }
        _state.update { it.copy(isLoading = false, loginSuccess = true) }
      } catch (e: Exception) {
        Logger.e(e) { "BazQux login: failed to clear data and save user" }
        _state.update {
          it.copy(isLoading = false, error = BazQuxLoginError.Unknown(e.message ?: ""))
        }
      }
    }
  }

  private data class VerifiedUserInfo(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
  )
}

data class BazQuxLoginState(
  val username: String,
  val password: String,
  val isLoading: Boolean,
  val loginSuccess: Boolean,
  val showConfirmationDialog: Boolean,
  val error: BazQuxLoginError?,
) {
  companion object {
    val DEFAULT =
      BazQuxLoginState(
        username = "",
        password = "",
        isLoading = false,
        loginSuccess = false,
        showConfirmationDialog = false,
        error = null,
      )
  }
}

sealed interface BazQuxLoginError {
  data object LoginFailed : BazQuxLoginError

  data class Unknown(val message: String) : BazQuxLoginError
}

sealed interface BazQuxLoginEvent {
  data class OnUsernameChanged(val username: String) : BazQuxLoginEvent

  data class OnPasswordChanged(val password: String) : BazQuxLoginEvent

  data object OnLoginClicked : BazQuxLoginEvent

  data object OnConfirmClearDataClicked : BazQuxLoginEvent

  data object OnConfirmationDismissed : BazQuxLoginEvent
}
