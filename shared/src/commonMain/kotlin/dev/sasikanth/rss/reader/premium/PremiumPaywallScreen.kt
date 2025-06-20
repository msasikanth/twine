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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
fun PremiumPaywallScreen(presenter: PremiumPaywallPresenter, modifier: Modifier = Modifier) {
  val paywallOptions = remember {
    PaywallOptions.Builder(dismissRequest = { presenter.dispatch(PremiumPaywallEvent.GoBack) })
      .apply { shouldDisplayDismissButton = true }
      .build()
  }

  Box(modifier = modifier) { Paywall(paywallOptions) }
}
