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

package dev.sasikanth.rss.reader.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class BillingHandler(private val dispatchersProvider: DispatchersProvider) {

  companion object {
    private const val ENTITLEMENT_PREMIUM = "Premium"
  }

  private val purchases by lazy { Purchases.sharedInstance }

  suspend fun customerResult(): SubscriptionResult {
    try {
      val customerInfo =
        withContext(dispatchersProvider.io) {
          purchases.awaitCustomerInfo(fetchPolicy = CacheFetchPolicy.NOT_STALE_CACHED_OR_CURRENT)
        }

      val entitlementInfo = customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]
      val isPremium = entitlementInfo?.isActive

      if (isPremium == true) {
        return SubscriptionResult.Subscribed
      }
    } catch (e: Exception) {
      return SubscriptionResult.Error
    }

    return SubscriptionResult.NotSubscribed
  }

  sealed interface SubscriptionResult {

    data object Subscribed : SubscriptionResult

    data object NotSubscribed : SubscriptionResult

    data object Error : SubscriptionResult
  }
}
