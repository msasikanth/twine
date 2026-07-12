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

package dev.sasikanth.rss.reader.premium

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.billing.TwinePackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class PremiumPaywallViewModel(private val billingHandler: BillingHandler) : ViewModel() {

  private val _hasPremium = MutableStateFlow(false)
  val hasPremium =
    _hasPremium
      .onStart { checkSubscription() }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
      )

  private val _packages = MutableStateFlow<List<TwinePackage>>(emptyList())
  val packages =
    _packages
      .onStart { loadPackages() }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
      )

  private val _inProgress = MutableStateFlow(false)
  val inProgress = _inProgress.asStateFlow()

  private suspend fun checkSubscription() {
    _hasPremium.value = billingHandler.isSubscribed()
  }

  private suspend fun loadPackages() {
    _packages.value = billingHandler.getPackages()
  }

  fun purchasePackage(packageId: String) {
    viewModelScope.launch {
      _inProgress.value = true
      val result = billingHandler.purchasePackage(packageId)
      if (result is SubscriptionResult.Subscribed) {
        _hasPremium.value = true
      }
      _inProgress.value = false
    }
  }

  fun restorePurchases() {
    viewModelScope.launch {
      _inProgress.value = true
      val result = billingHandler.restorePurchases()
      if (result is SubscriptionResult.Subscribed) {
        _hasPremium.value = true
      }
      _inProgress.value = false
    }
  }
}
