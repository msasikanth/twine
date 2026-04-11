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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
import dev.sasikanth.rss.reader.ui.coralColorScheme
import dev.sasikanth.rss.reader.ui.forestColorScheme
import dev.sasikanth.rss.reader.ui.parchmentColorScheme
import dev.sasikanth.rss.reader.ui.raspberryColorScheme
import dev.sasikanth.rss.reader.ui.skylineColorScheme
import dev.sasikanth.rss.reader.ui.solarizedColorScheme
import dev.sasikanth.rss.reader.ui.systemDynamicColorScheme
import dev.sasikanth.rss.reader.util.canBlurImage
import org.jetbrains.compose.resources.stringResource
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.themeVariantAmber
import twine.shared.generated.resources.themeVariantCoral
import twine.shared.generated.resources.themeVariantDynamic
import twine.shared.generated.resources.themeVariantForest
import twine.shared.generated.resources.themeVariantParchment
import twine.shared.generated.resources.themeVariantRaspberry
import twine.shared.generated.resources.themeVariantSkyline
import twine.shared.generated.resources.themeVariantSolarized
import twine.shared.generated.resources.themeVariantSystemDynamic

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeVariantIconButton(
  themeVariant: ThemeVariant,
  selected: Boolean,
  isSubscribed: Boolean,
  useDarkTheme: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  showLabel: Boolean = false,
) {
  val appDynamicColorState = LocalDynamicColorState.current
  val isDark = themeVariant.isDark(useDarkTheme)
  val colorScheme =
    when (themeVariant) {
      ThemeVariant.Dynamic -> {
        if (isDark) appDynamicColorState.darkAppColorScheme
        else appDynamicColorState.lightAppColorScheme
      }
      ThemeVariant.SystemDynamic -> systemDynamicColorScheme(isDark)
      ThemeVariant.Solarized -> solarizedColorScheme(isDark)
      ThemeVariant.Forest -> forestColorScheme(isDark)
      ThemeVariant.Amber -> amberColorScheme(isDark)
      ThemeVariant.Coral -> coralColorScheme(isDark)
      ThemeVariant.Raspberry -> raspberryColorScheme(isDark)
      ThemeVariant.Skyline -> skylineColorScheme(isDark)
      ThemeVariant.Parchment -> parchmentColorScheme(isDark)
    }

  val (backgroundColor, contentColor) =
    when (themeVariant) {
      ThemeVariant.Dynamic -> {
        Pair(Color.Transparent, colorScheme.inverseOnSurface)
      }

      ThemeVariant.SystemDynamic -> {
        Pair(colorScheme.primaryContainer, colorScheme.onPrimaryContainer)
      }

      ThemeVariant.Solarized -> {
        Pair(colorScheme.surfaceContainerHighest, colorScheme.onSurface)
      }

      ThemeVariant.Forest,
      ThemeVariant.Amber,
      ThemeVariant.Coral,
      ThemeVariant.Raspberry,
      ThemeVariant.Skyline -> {
        Pair(colorScheme.primary, colorScheme.onPrimary)
      }

      ThemeVariant.Parchment -> {
        Pair(colorScheme.surface, colorScheme.onSurface)
      }
    }

  val borderWidth by animateDpAsState(if (selected) 2.dp else 1.dp)
  val borderColor by
    animateColorAsState(
      if (selected) AppTheme.colorScheme.outline else AppTheme.colorScheme.outlineVariant
    )

  Column(
    modifier = Modifier.width(IntrinsicSize.Min),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
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
        if (themeVariant == ThemeVariant.Dynamic) {
          val coralColor = coralColorScheme(isDark = isDark).primary
          val forestColor = forestColorScheme(isDark = isDark).primary
          val raspberryColor = raspberryColorScheme(isDark = isDark).primary
          val skylineColor = skylineColorScheme(isDark = isDark).primary

          Canvas(
            modifier =
              Modifier.matchParentSize().then(if (canBlurImage) Modifier.blur(24.dp) else Modifier)
          ) {
            drawRect(colorScheme.surfaceContainerHighest)

            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(coralColor, Color.Transparent),
                  center = Offset(0f, 0f),
                  radius = size.width * 1.5f,
                )
            )

            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(forestColor, Color.Transparent),
                  center = Offset(size.width, size.height),
                  radius = size.width * 1.5f,
                )
            )

            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(raspberryColor, Color.Transparent),
                  center = Offset(0f, size.height),
                  radius = size.width * 1.5f,
                )
            )

            drawRect(
              brush =
                Brush.radialGradient(
                  colors = listOf(skylineColor, Color.Transparent),
                  center = Offset(size.width, 0f),
                  radius = size.width * 1.5f,
                )
            )
          }
        }

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

    if (showLabel) {
      Spacer(Modifier.height(8.dp))

      val themeName =
        when (themeVariant) {
          ThemeVariant.Dynamic -> stringResource(Res.string.themeVariantDynamic)
          ThemeVariant.SystemDynamic -> stringResource(Res.string.themeVariantSystemDynamic)
          ThemeVariant.Solarized -> stringResource(Res.string.themeVariantSolarized)
          ThemeVariant.Forest -> stringResource(Res.string.themeVariantForest)
          ThemeVariant.Amber -> stringResource(Res.string.themeVariantAmber)
          ThemeVariant.Coral -> stringResource(Res.string.themeVariantCoral)
          ThemeVariant.Raspberry -> stringResource(Res.string.themeVariantRaspberry)
          ThemeVariant.Skyline -> stringResource(Res.string.themeVariantSkyline)
          ThemeVariant.Parchment -> stringResource(Res.string.themeVariantParchment)
        }

      Text(
        modifier = Modifier.fillMaxWidth(),
        text = themeName,
        style = MaterialTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurfaceVariant,
        minLines = 2,
        maxLines = 2,
        textAlign = TextAlign.Center,
      )
    }
  }
}
