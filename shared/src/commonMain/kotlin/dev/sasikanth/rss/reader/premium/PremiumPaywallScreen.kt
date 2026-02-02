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

package dev.sasikanth.rss.reader.premium

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PremiumPaywallScreen(hasPremium: Boolean, goBack: () -> Unit, modifier: Modifier = Modifier) {
  Box(modifier = modifier) {
    if (!hasPremium) {
      PaywallComponent(onDismiss = goBack, modifier = modifier)
    } else {
      CustomerCenterComponent(onDismiss = goBack, modifier = modifier)
    }
  }
}
