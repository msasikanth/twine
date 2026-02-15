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

package dev.sasikanth.rss.reader.accountselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.sync.APIServiceProvider
import dev.sasikanth.rss.reader.data.sync.CloudServiceProvider
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.auth.OAuthManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class AccountSelectionViewModel(
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val userRepository: UserRepository,
  private val syncCoordinator: SyncCoordinator,
  private val oAuthManager: OAuthManager,
  private val billingHandler: BillingHandler,
  val availableProviders: Set<CloudServiceProvider>,
) : ViewModel() {

  private val _state = MutableStateFlow(AccountSelectionState.DEFAULT)
  val state: StateFlow<AccountSelectionState> = _state.asStateFlow()

  private val _effects = MutableSharedFlow<AccountSelectionEffect>()
  val effects = _effects.asSharedFlow()

  init {
    viewModelScope.launch {
      val isSubscribed = billingHandler.isSubscribed()
      _state.update { it.copy(isSubscribed = isSubscribed) }
    }

    userRepository
      .user()
      .onEach { user ->
        _state.update { it.copy(user = user) }
        if (user != null) {
          settingsRepository.completeOnboarding()
        }
      }
      .launchIn(viewModelScope)
  }

  fun dispatch(event: AccountSelectionEvent) {
    when (event) {
      AccountSelectionEvent.LocalAccountClicked -> localAccountClicked()
      is AccountSelectionEvent.CloudServiceClicked -> cloudServiceClicked(event.provider)
      AccountSelectionEvent.ClearAuthUrl -> _state.update { it.copy(authUrlToOpen = null) }
      AccountSelectionEvent.Refresh -> refresh()
    }
  }

  private fun refresh() {
    viewModelScope.launch {
      val isSubscribed = billingHandler.isSubscribed()
      _state.update { it.copy(isSubscribed = isSubscribed) }
    }
  }

  private fun localAccountClicked() {
    viewModelScope.launch {
      settingsRepository.completeOnboarding()
      _effects.emit(AccountSelectionEffect.NavigateToDiscovery)
    }
  }

  private fun cloudServiceClicked(provider: CloudServiceProvider) {
    viewModelScope.launch {
      if (provider.isPremium && !_state.value.isSubscribed) {
        _effects.emit(AccountSelectionEffect.OpenPaywall)
        return@launch
      }

      if (provider is APIServiceProvider) {
        when (provider.cloudService) {
          ServiceType.FRESH_RSS -> _effects.emit(AccountSelectionEffect.OpenFreshRssLogin)
          ServiceType.MINIFLUX -> _effects.emit(AccountSelectionEffect.OpenMinifluxLogin)
          else -> {
            throw IllegalStateException("Unknown cloud service type: ${provider.cloudService}")
          }
        }
      } else {
        oAuthManager.setPendingProvider(provider.cloudService)
        val authUrl = oAuthManager.getAuthUrl(provider.cloudService)
        _state.update { it.copy(authUrlToOpen = authUrl) }
      }
    }
  }
}
