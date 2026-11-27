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

package dev.sasikanth.rss.reader.billing

import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.data.repository.DebugSettingsRepository
import dev.sasikanth.rss.reader.data.repository.DebugSubscriptionOverride
import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class BillingHandler(
  private val appInfo: AppInfo,
  private val debugSettingsRepository: DebugSettingsRepository,
) {

  actual suspend fun isSubscribed(): Boolean {
    return customerResult() is SubscriptionResult.Subscribed
  }

  actual suspend fun canSubscribe(): Boolean {
    return false
  }

  actual suspend fun customerResult(): SubscriptionResult {
    if (appInfo.isDebugBuild) {
      return when (debugSettingsRepository.subscriptionOverrideImmediate()) {
        DebugSubscriptionOverride.ForceFree -> SubscriptionResult.NotSubscribed
        DebugSubscriptionOverride.ForcePro,
        DebugSubscriptionOverride.Default -> SubscriptionResult.Subscribed
      }
    }
    return SubscriptionResult.Subscribed
  }

  actual suspend fun getPackages(): List<TwinePackage> {
    return emptyList()
  }

  actual suspend fun purchasePackage(packageId: String): SubscriptionResult {
    return SubscriptionResult.Subscribed
  }

  actual suspend fun restorePurchases(): SubscriptionResult {
    return SubscriptionResult.Subscribed
  }
}
