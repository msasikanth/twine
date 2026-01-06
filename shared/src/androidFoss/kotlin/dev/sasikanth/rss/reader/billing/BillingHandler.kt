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

import dev.sasikanth.rss.reader.di.scopes.AppScope
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
actual class BillingHandler {

  actual suspend fun isSubscribed(): Boolean {
    return true
  }

  actual suspend fun customerResult(): SubscriptionResult {
    return SubscriptionResult.Subscribed
  }
}
