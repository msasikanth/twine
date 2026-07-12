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
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.PackageType as RCPackageType
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.ENTITLEMENT_PREMIUM
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.getString
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.premiumPaywallPackageLifetime
import twine.shared.generated.resources.premiumPaywallPackageMonthly
import twine.shared.generated.resources.premiumPaywallPackageYearly

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

  actual suspend fun getPackages(): List<TwinePackage> {
    return try {
      val offerings = withContext(dispatchersProvider.io) { purchases.awaitOfferings() }
      offerings.current?.availablePackages?.map { rcPackage ->
        val type =
          when (rcPackage.packageType) {
            RCPackageType.MONTHLY -> PackageType.MONTHLY
            RCPackageType.ANNUAL -> PackageType.ANNUAL
            RCPackageType.LIFETIME -> PackageType.LIFETIME
            else -> PackageType.UNKNOWN
          }
        val periodString =
          when (type) {
            PackageType.MONTHLY -> getString(Res.string.premiumPaywallPackageMonthly)
            PackageType.ANNUAL -> getString(Res.string.premiumPaywallPackageYearly)
            PackageType.LIFETIME -> getString(Res.string.premiumPaywallPackageLifetime)
            PackageType.UNKNOWN -> "Unknown"
          }
        TwinePackage(
          id = rcPackage.identifier,
          packageType = type,
          priceString = rcPackage.storeProduct.price.formatted,
          period = periodString,
        )
      } ?: emptyList()
    } catch (e: Exception) {
      emptyList()
    }
  }

  actual suspend fun purchasePackage(packageId: String): SubscriptionResult {
    return try {
      val offerings = withContext(dispatchersProvider.io) { purchases.awaitOfferings() }
      val packageToBuy = offerings.current?.availablePackages?.find { it.identifier == packageId }
      if (packageToBuy != null) {
        val result = withContext(dispatchersProvider.io) { purchases.awaitPurchase(packageToBuy) }
        val isPremium = result.customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]?.isActive == true
        if (isPremium) {
          SubscriptionResult.Subscribed
        } else {
          SubscriptionResult.NotSubscribed
        }
      } else {
        SubscriptionResult.Error(Exception("Package not found"))
      }
    } catch (e: Exception) {
      SubscriptionResult.Error(e)
    }
  }

  actual suspend fun restorePurchases(): SubscriptionResult {
    return try {
      val customerInfo = withContext(dispatchersProvider.io) { purchases.awaitRestore() }
      val isPremium = customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]?.isActive == true
      if (isPremium) {
        SubscriptionResult.Subscribed
      } else {
        SubscriptionResult.NotSubscribed
      }
    } catch (e: Exception) {
      SubscriptionResult.Error(e)
    }
  }
}
