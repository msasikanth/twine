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

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration
import dev.sasikanth.rss.reader.initializers.Initializer
import dev.sasikanth.rss.reader.resources.icons.Platform
import dev.sasikanth.rss.reader.resources.icons.platform
import dev.sasikanth.rss.reader.utils.Constants
import me.tatarka.inject.annotations.Inject

@Inject
actual class BillingInitializer : Initializer {

  actual override fun initialize() {
    val publicApiKey =
      if (platform is Platform.Android) {
        "goog_PjAVIPsVVoDfdsnLWfLOIVgwULm"
      } else {
        "appl_zvvXwxUBoQIpsOIOQeKPJDYzkNh"
      }

    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(
      PurchasesConfiguration.Builder(publicApiKey)
        .userDefaultsSuiteName(Constants.IOS_APP_GROUP)
        .build()
    )
  }
}
