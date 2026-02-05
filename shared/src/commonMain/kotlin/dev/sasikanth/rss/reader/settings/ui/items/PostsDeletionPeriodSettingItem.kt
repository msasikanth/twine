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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.DropdownMenu
import dev.sasikanth.rss.reader.components.DropdownMenuItem
import dev.sasikanth.rss.reader.data.repository.Period
import dev.sasikanth.rss.reader.data.repository.Period.NEVER
import dev.sasikanth.rss.reader.ui.AppTheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.settingsPostsDeletionPeriodNever
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneMonth
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneWeek
import twine.shared.generated.resources.settingsPostsDeletionPeriodOneYear
import twine.shared.generated.resources.settingsPostsDeletionPeriodSixMonths
import twine.shared.generated.resources.settingsPostsDeletionPeriodThreeMonths
import twine.shared.generated.resources.settingsPostsDeletionPeriodTitle

@Composable
internal fun PostsDeletionPeriodSettingItem(
  postsDeletionPeriod: Period?,
  onValueChanged: (Period) -> Unit,
) {
  var showDropdown by remember { mutableStateOf(false) }

  Row(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f),
      text = stringResource(Res.string.settingsPostsDeletionPeriodTitle),
      style = MaterialTheme.typography.titleMedium,
      color = AppTheme.colorScheme.textEmphasisHigh,
    )

    Box {
      val density = LocalDensity.current
      var buttonHeight by remember { mutableStateOf(Dp.Unspecified) }

      TextButton(
        modifier =
          Modifier.onGloballyPositioned { coordinates ->
            buttonHeight = with(density) { coordinates.size.height.toDp() }
          },
        onClick = { showDropdown = true },
        shape = MaterialTheme.shapes.medium,
      ) {
        val period =
          when (postsDeletionPeriod) {
            Period.ONE_WEEK -> stringResource(Res.string.settingsPostsDeletionPeriodOneWeek)
            Period.ONE_MONTH -> stringResource(Res.string.settingsPostsDeletionPeriodOneMonth)
            Period.THREE_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodThreeMonths)
            Period.SIX_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodSixMonths)
            Period.ONE_YEAR -> stringResource(Res.string.settingsPostsDeletionPeriodOneYear)
            NEVER -> stringResource(Res.string.settingsPostsDeletionPeriodNever)
            null -> ""
          }

        Text(
          text = period,
          style = MaterialTheme.typography.labelLarge,
          color = AppTheme.colorScheme.tintedForeground,
        )

        Spacer(Modifier.requiredWidth(8.dp))

        Icon(
          imageVector = Icons.Filled.ExpandMore,
          contentDescription = null,
          tint = AppTheme.colorScheme.tintedForeground,
        )
      }

      DropdownMenu(
        offset = DpOffset(0.dp, buttonHeight.unaryMinus()),
        expanded = showDropdown,
        onDismissRequest = { showDropdown = false },
      ) {
        Period.entries.forEach { period ->
          val periodString =
            when (period) {
              Period.ONE_WEEK -> stringResource(Res.string.settingsPostsDeletionPeriodOneWeek)
              Period.ONE_MONTH -> stringResource(Res.string.settingsPostsDeletionPeriodOneMonth)
              Period.THREE_MONTHS ->
                stringResource(Res.string.settingsPostsDeletionPeriodThreeMonths)
              Period.SIX_MONTHS -> stringResource(Res.string.settingsPostsDeletionPeriodSixMonths)
              Period.ONE_YEAR -> stringResource(Res.string.settingsPostsDeletionPeriodOneYear)
              NEVER -> stringResource(Res.string.settingsPostsDeletionPeriodNever)
            }

          val backgroundColor =
            if (period == postsDeletionPeriod) {
              AppTheme.colorScheme.tintedHighlight
            } else {
              Color.Unspecified
            }

          DropdownMenuItem(
            onClick = {
              onValueChanged(period)
              showDropdown = false
            },
            modifier = Modifier.background(backgroundColor),
          ) {
            val textColor =
              if (period == postsDeletionPeriod) {
                AppTheme.colorScheme.inverseOnSurface
              } else {
                AppTheme.colorScheme.textEmphasisHigh
              }

            Text(text = periodString, style = MaterialTheme.typography.bodyLarge, color = textColor)
          }
        }
      }
    }
  }
}
