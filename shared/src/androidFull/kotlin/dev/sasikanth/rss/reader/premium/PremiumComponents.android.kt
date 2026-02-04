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

package dev.sasikanth.rss.reader.premium

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import dev.sasikanth.rss.reader.utils.Constants.ENTITLEMENT_PREMIUM

@Composable
actual fun PaywallComponent(onDismiss: () -> Unit, modifier: Modifier) {
  val paywallListener = remember {
    object : PaywallListener {
      override fun onRestoreCompleted(customerInfo: CustomerInfo) {
        super.onRestoreCompleted(customerInfo)
        val entitlementInfo = customerInfo.entitlements.all[ENTITLEMENT_PREMIUM]
        val isPremium = entitlementInfo?.isActive

        if (isPremium == true) onDismiss()
      }
    }
  }

  val paywallOptions = remember {
    PaywallOptions.Builder(dismissRequest = onDismiss)
      .apply {
        shouldDisplayDismissButton = true
        listener = paywallListener
      }
      .build()
  }

  Paywall(paywallOptions)
}

@Composable
actual fun CustomerCenterComponent(onDismiss: () -> Unit, modifier: Modifier) {
  CustomerCenter(modifier = modifier.fillMaxSize(), onDismiss = onDismiss)
}
