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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.resources.icons.DarkMode
import dev.sasikanth.rss.reader.resources.icons.LightAndDarkMode
import dev.sasikanth.rss.reader.resources.icons.LightMode
import dev.sasikanth.rss.reader.resources.icons.StarShine
import dev.sasikanth.rss.reader.resources.icons.TwineIcons
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.ui.amberColorScheme
import dev.sasikanth.rss.reader.ui.forestColorScheme
import dev.sasikanth.rss.reader.ui.raspberryColorScheme
import dev.sasikanth.rss.reader.ui.skylineColorScheme
import dev.sasikanth.rss.reader.ui.solarizedColorScheme

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

  val backgroundColor = colorScheme.primary
  val contentColor = colorScheme.onPrimary

  val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp)
  val borderColor by
    animateColorAsState(
      if (selected) AppTheme.colorScheme.outline else AppTheme.colorScheme.outlineVariant
    )

  Box(
    modifier =
      Modifier.then(
        if (selected) {
          Modifier.border(borderWidth, borderColor, MaterialTheme.shapes.largeIncreased)
        } else {
          Modifier
        }
      ),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        modifier
          .requiredSize(width = 64.dp, height = 96.dp)
          .padding(4.dp)
          .clip(MaterialTheme.shapes.large)
          .background(backgroundColor)
          .then(
            if (!selected) {
              Modifier.border(borderWidth, borderColor, MaterialTheme.shapes.large)
            } else {
              Modifier
            }
          )
          .clickable { onClick() },
      contentAlignment = Alignment.Center,
    ) {
      val icon =
        when {
          themeVariant.isPremium && !isSubscribed -> TwineIcons.StarShine
          themeVariant.isDarkModeOnly -> TwineIcons.DarkMode
          themeVariant.isLightModeOnly -> TwineIcons.LightMode
          else -> TwineIcons.LightAndDarkMode
        }

      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.requiredSize(24.dp),
      )
    }
  }
}
