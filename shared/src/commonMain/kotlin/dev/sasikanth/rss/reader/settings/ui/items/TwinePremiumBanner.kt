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

package dev.sasikanth.rss.reader.settings.ui.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.billing.SubscriptionResult
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.twinePremium
import twine.shared.generated.resources.twinePremiumDesc
import twine.shared.generated.resources.twinePremiumSubscribedDesc

@Composable
internal fun TwinePremiumBanner(
  subscriptionResult: SubscriptionResult?,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 24.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      modifier = Modifier.requiredSize(32.dp),
      imageVector = Icons.Rounded.WorkspacePremium,
      contentDescription = null,
      tint = AppTheme.colorScheme.primary,
    )

    Column {
      if (subscriptionResult != SubscriptionResult.Subscribed) {
        Text(
          text = stringResource(Res.string.twinePremium),
          style = MaterialTheme.typography.titleMedium,
          color = AppTheme.colorScheme.textEmphasisHigh,
        )
      }

      val subscriptionDescRes =
        if (subscriptionResult == SubscriptionResult.Subscribed) {
          Res.string.twinePremiumSubscribedDesc
        } else {
          Res.string.twinePremiumDesc
        }
      Text(
        text = stringResource(subscriptionDescRes),
        style = MaterialTheme.typography.labelLarge,
        color = AppTheme.colorScheme.textEmphasisMed,
      )
    }
  }
}
