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

package dev.sasikanth.rss.reader.billing

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.ENTITLEMENT_PREMIUM
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class BillingHandler(private val dispatchersProvider: DispatchersProvider) {

  private val purchases by lazy { Purchases.sharedInstance }

  actual suspend fun isSubscribed(): Boolean {
    return customerResult() is SubscriptionResult.Subscribed
  }

  actual suspend fun canSubscribe(): Boolean {
    return true
  }

  actual suspend fun customerResult(): SubscriptionResult {
    try {
      val customerInfo =
        withContext(dispatchersProvider.io) {
          purchases.awaitCustomerInfo(fetchPolicy = CacheFetchPolicy.default())
        }

      val entitlementInfo = customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]
      val isPremium = entitlementInfo?.isActive

      if (isPremium == true) {
        return SubscriptionResult.Subscribed
      }
    } catch (e: Exception) {
      return SubscriptionResult.Error(e)
    }

    return SubscriptionResult.NotSubscribed
  }
}
