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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
fun PremiumPaywallScreen(
  hasPremium: Boolean,
  goBack: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    if (!hasPremium) {
      val paywallOptions = remember {
        PaywallOptions.Builder(dismissRequest = { goBack() })
          .apply { shouldDisplayDismissButton = true }
          .build()
      }

      Paywall(paywallOptions)
    } else {
      CustomerCenter(modifier = Modifier.fillMaxSize(), onDismiss = { goBack() })
    }
  }
}
