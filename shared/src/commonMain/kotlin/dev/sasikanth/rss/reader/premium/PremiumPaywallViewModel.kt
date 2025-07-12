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

package dev.sasikanth.rss.reader.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject

@Inject
class PremiumPaywallViewModel(
  private val billingHandler: BillingHandler,
) : ViewModel() {

  private val _hasPremium = MutableStateFlow(false)
  val hasPremium =
    _hasPremium
      .onStart { checkSubscription() }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
      )

  private suspend fun checkSubscription() {
    val isSubscribed = billingHandler.customerResult() is SubscriptionResult.Subscribed
    _hasPremium.value = isSubscribed
  }
}
