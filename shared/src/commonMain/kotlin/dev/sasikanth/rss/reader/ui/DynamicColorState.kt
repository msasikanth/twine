/*
 * Copyright 2024 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.sasikanth.rss.reader.ui

import androidx.collection.lruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import dev.sasikanth.material.color.utilities.dynamiccolor.DynamicColor
import dev.sasikanth.material.color.utilities.dynamiccolor.MaterialDynamicColors
import dev.sasikanth.material.color.utilities.dynamiccolor.ToneDeltaConstraint
import dev.sasikanth.material.color.utilities.dynamiccolor.TonePolarity
import dev.sasikanth.material.color.utilities.hct.Hct
import dev.sasikanth.material.color.utilities.scheme.SchemeContent
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue

@Composable
internal fun rememberDynamicColorState(
  defaultLightAppColorScheme: AppColorScheme,
  defaultDarkAppColorScheme: AppColorScheme,
): DynamicColorState {
  return remember {
    DynamicColorState(
      defaultLightAppColorScheme = defaultLightAppColorScheme,
      defaultDarkAppColorScheme = defaultDarkAppColorScheme,
    )
  }
}

@Stable
internal class DynamicColorState(
  private val defaultLightAppColorScheme: AppColorScheme,
  private val defaultDarkAppColorScheme: AppColorScheme,
) {
  var lightAppColorScheme by mutableStateOf(defaultLightAppColorScheme)
    private set

  var darkAppColorScheme by mutableStateOf(defaultDarkAppColorScheme)
    private set

  private val cache = lruCache<String, AppColorScheme>(maxSize = 4)

  fun animate(fromSeedColor: Color, toSeedColor: Color, progress: Float) {
    val normalizedProgress =
      if (progress < -EPSILON) {
        progress.absoluteValue.inverse()
      } else {
        progress
      }

    val startLightColors =
      cache["light_$fromSeedColor"]
        ?: generateDynamicColorsFromSeedColor(useDarkTheme = false, seedColor = fromSeedColor)

    val startDarkColors =
      cache["dark_$fromSeedColor"]
        ?: generateDynamicColorsFromSeedColor(useDarkTheme = true, seedColor = fromSeedColor)

    val endLightColors =
      cache["light_$toSeedColor"]
        ?: generateDynamicColorsFromSeedColor(useDarkTheme = false, seedColor = toSeedColor)

    val endDarkColors =
      cache["dark_$toSeedColor"]
        ?: generateDynamicColorsFromSeedColor(useDarkTheme = true, seedColor = toSeedColor)

    lightAppColorScheme =
      if (startLightColors == endLightColors) {
        startLightColors
      } else {
        startLightColors.animate(to = endLightColors, progress = normalizedProgress)
      }

    darkAppColorScheme =
      if (startDarkColors == endDarkColors) {
        startDarkColors
      } else {
        startDarkColors.animate(
          to = endDarkColors,
          progress = normalizedProgress,
        )
      }
  }

  fun reset() {
    lightAppColorScheme = defaultLightAppColorScheme
    darkAppColorScheme = defaultDarkAppColorScheme
  }

  private fun generateDynamicColorsFromSeedColor(
    useDarkTheme: Boolean,
    seedColor: Color
  ): AppColorScheme {
    val scheme =
      SchemeContent(
        sourceColorHct = Hct.fromInt(seedColor.toArgb()),
        isDark = useDarkTheme,
        contrastLevel = 0.0
      )
    val dynamicColors = MaterialDynamicColors()
    val defaultColorScheme =
      if (useDarkTheme) {
        defaultDarkAppColorScheme
      } else {
        defaultLightAppColorScheme
      }

    return AppColorScheme(
      tintedBackground =
        DynamicColor.fromPalette(
            palette = { s -> s.primaryPalette },
            tone = { s -> if (s.isDark) 10.0 else 99.0 },
            background = { s -> dynamicColors.highestSurface(s) },
            toneDeltaConstraint = { s ->
              ToneDeltaConstraint(
                MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                dynamicColors.primaryContainer(),
                if (s.isDark) TonePolarity.DARKER else TonePolarity.LIGHTER
              )
            }
          )
          .toColor(scheme),
      tintedSurface =
        DynamicColor.fromPalette(
            palette = { s -> s.primaryPalette },
            tone = { s -> if (s.isDark) 20.0 else 95.0 },
            background = { s -> dynamicColors.highestSurface(s) },
            toneDeltaConstraint = { s ->
              ToneDeltaConstraint(
                MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                dynamicColors.primaryContainer(),
                if (s.isDark) TonePolarity.DARKER else TonePolarity.LIGHTER
              )
            }
          )
          .toColor(scheme),
      tintedForeground =
        DynamicColor.fromPalette(
            palette = { s -> s.primaryPalette },
            tone = { s -> if (s.isDark) 80.0 else 40.0 },
            background = { s -> dynamicColors.highestSurface(s) },
            toneDeltaConstraint = { s ->
              ToneDeltaConstraint(
                MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                dynamicColors.primaryContainer(),
                if (s.isDark) TonePolarity.DARKER else TonePolarity.LIGHTER
              )
            }
          )
          .toColor(scheme),
      tintedHighlight =
        DynamicColor.fromPalette(
            palette = { s -> s.primaryPalette },
            tone = { s -> if (s.isDark) 40.0 else 80.0 },
            background = { s -> dynamicColors.highestSurface(s) },
            toneDeltaConstraint = { s ->
              ToneDeltaConstraint(
                MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                dynamicColors.primaryContainer(),
                if (s.isDark) TonePolarity.DARKER else TonePolarity.LIGHTER
              )
            }
          )
          .toColor(scheme),
      outline = dynamicColors.outline().toColor(scheme),
      outlineVariant = dynamicColors.outlineVariant().toColor(scheme),
      surface = dynamicColors.surface().toColor(scheme),
      onSurface = dynamicColors.onSurface().toColor(scheme),
      onSurfaceVariant = dynamicColors.onSurfaceVariant().toColor(scheme),
      surfaceContainer = dynamicColors.surfaceContainer().toColor(scheme),
      surfaceContainerLow = dynamicColors.surfaceContainerLow().toColor(scheme),
      surfaceContainerLowest = dynamicColors.surfaceContainerLowest().toColor(scheme),
      surfaceContainerHigh = dynamicColors.surfaceContainerHigh().toColor(scheme),
      surfaceContainerHighest = dynamicColors.surfaceContainerHighest().toColor(scheme),
      textEmphasisHigh = defaultColorScheme.textEmphasisHigh,
      textEmphasisMed = defaultColorScheme.textEmphasisMed,
    )
  }

  private fun DynamicColor.toColor(scheme: SchemeContent): Color {
    return Color(getArgb(scheme))
  }
}

fun AppColorScheme.animate(
  to: AppColorScheme?,
  progress: Float,
): AppColorScheme {
  if (to == null) return this

  return copy(
    tintedBackground =
      lerp(start = tintedBackground, stop = to.tintedBackground, fraction = progress),
    tintedSurface = lerp(start = tintedSurface, stop = to.tintedSurface, fraction = progress),
    tintedForeground =
      lerp(start = tintedForeground, stop = to.tintedForeground, fraction = progress),
    tintedHighlight = lerp(start = tintedHighlight, stop = to.tintedHighlight, fraction = progress),
    outline = lerp(start = outline, stop = to.outline, fraction = progress),
    outlineVariant = lerp(start = outlineVariant, stop = to.outlineVariant, fraction = progress),
    surface = lerp(start = surface, stop = to.surface, fraction = progress),
    onSurface = lerp(start = onSurface, stop = to.onSurface, fraction = progress),
    onSurfaceVariant =
      lerp(start = onSurfaceVariant, stop = to.onSurfaceVariant, fraction = progress),
    surfaceContainer =
      lerp(start = surfaceContainer, stop = to.surfaceContainer, fraction = progress),
    surfaceContainerLow =
      lerp(start = surfaceContainerLow, stop = to.surfaceContainerLow, fraction = progress),
    surfaceContainerLowest =
      lerp(start = surfaceContainerLowest, stop = to.surfaceContainerLowest, fraction = progress),
    surfaceContainerHigh =
      lerp(start = surfaceContainerHigh, stop = to.surfaceContainerHigh, fraction = progress),
    surfaceContainerHighest =
      lerp(start = surfaceContainerHighest, stop = to.surfaceContainerHighest, fraction = progress),
  )
}

internal val LocalDynamicColorState =
  staticCompositionLocalOf<DynamicColorState> {
    throw NullPointerException("Please provide a dynamic color state")
  }
