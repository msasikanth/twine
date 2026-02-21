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

package dev.sasikanth.rss.reader.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.amberColorScheme
import dev.sasikanth.rss.reader.ui.forestColorScheme
import dev.sasikanth.rss.reader.ui.raspberryColorScheme
import dev.sasikanth.rss.reader.ui.skylineColorScheme
import dev.sasikanth.rss.reader.ui.solarizedColorScheme
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.themeVariantAmber
import twine.shared.generated.resources.themeVariantDynamic
import twine.shared.generated.resources.themeVariantForest
import twine.shared.generated.resources.themeVariantRaspberry
import twine.shared.generated.resources.themeVariantSkyline
import twine.shared.generated.resources.themeVariantSolarized

@Composable
fun ThemeVariantIconButton(
  themeVariant: ThemeVariant,
  selected: Boolean,
  isSubscribed: Boolean,
  useDarkTheme: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val appDynamicColorState = LocalDynamicColorState.current
  val isDark = themeVariant.isDark(useDarkTheme)
  val colorScheme =
    when (themeVariant) {
      ThemeVariant.Dynamic -> {
        if (isDark) appDynamicColorState.darkAppColorScheme
        else appDynamicColorState.lightAppColorScheme
      }
      ThemeVariant.Solarized -> solarizedColorScheme(isDark)
      ThemeVariant.Forest -> forestColorScheme(isDark)
      ThemeVariant.Amber -> amberColorScheme(isDark)
      ThemeVariant.Raspberry -> raspberryColorScheme(isDark)
      ThemeVariant.Skyline -> skylineColorScheme(isDark)
    }

  val backgroundColor = colorScheme.surface
  val onSurfaceColor = colorScheme.onSurface
  val primaryColor = colorScheme.primary

  val label =
    when (themeVariant) {
      ThemeVariant.Dynamic -> stringResource(Res.string.themeVariantDynamic)
      ThemeVariant.Solarized -> stringResource(Res.string.themeVariantSolarized)
      ThemeVariant.Forest -> stringResource(Res.string.themeVariantForest)
      ThemeVariant.Amber -> stringResource(Res.string.themeVariantAmber)
      ThemeVariant.Raspberry -> stringResource(Res.string.themeVariantRaspberry)
      ThemeVariant.Skyline -> stringResource(Res.string.themeVariantSkyline)
    }

  val borderWidth by animateDpAsState(if (selected) 4.dp else 1.dp)
  val borderColor =
    if (selected) {
      AppTheme.colorScheme.primary
    } else {
      AppTheme.colorScheme.outlineVariant
    }

  Box(
    modifier =
      modifier
        .requiredWidth(160.dp)
        .heightIn(min = 100.dp)
        .clip(MaterialTheme.shapes.large)
        .background(backgroundColor)
        .border(borderWidth, borderColor, MaterialTheme.shapes.large)
        .clickable { onClick() }
        .padding(12.dp)
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = label,
          style = MaterialTheme.typography.titleSmall,
          color = onSurfaceColor,
          maxLines = 1,
          modifier = Modifier.weight(1f),
        )

        if (themeVariant.isPremium && !isSubscribed) {
          Icon(
            modifier = Modifier.requiredSize(16.dp),
            imageVector = TwineIcons.StarShine,
            contentDescription = null,
            tint = onSurfaceColor.copy(alpha = 0.6f),
          )
        }
      }

      Spacer(Modifier.height(8.dp))

      val previewText = buildAnnotatedString {
        withStyle(style = SpanStyle(color = onSurfaceColor.copy(alpha = 0.6f))) {
          append("Lorem ipsum ")
        }
        withStyle(style = SpanStyle(color = onSurfaceColor, fontWeight = FontWeight.Bold)) {
          append("dolor sit amet")
        }
        withStyle(style = SpanStyle(color = onSurfaceColor.copy(alpha = 0.6f))) {
          append(", consectetur adipiscing elit. Mauris iaculis ")
        }
        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
          append("semper")
        }
        withStyle(style = SpanStyle(color = onSurfaceColor.copy(alpha = 0.6f))) {
          append(" pharetra.")
        }
      }

      Text(text = previewText, style = MaterialTheme.typography.bodySmall, maxLines = 5)
    }
  }
}
