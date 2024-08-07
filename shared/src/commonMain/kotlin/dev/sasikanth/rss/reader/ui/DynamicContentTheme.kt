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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
internal fun DynamicContentTheme(useDarkTheme: Boolean = false, content: @Composable () -> Unit) {
  val dynamicColorState =
    LocalDynamicColorState.current
      ?: rememberDynamicColorState(
        defaultLightAppColorScheme = lightAppColorScheme(),
        defaultDarkAppColorScheme = darkAppColorScheme(),
      )

  CompositionLocalProvider(LocalDynamicColorState provides dynamicColorState) {
    AppTheme(dynamicColorState = dynamicColorState, useDarkTheme = useDarkTheme, content = content)
  }
}

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

  private val cache = lruCache<String, DynamicColors>(maxSize = 4)

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
      defaultLightAppColorScheme.animate(
        startColors = startLightColors,
        endColors = endLightColors,
        progress = normalizedProgress
      )

    darkAppColorScheme =
      defaultDarkAppColorScheme.animate(
        startColors = startDarkColors,
        endColors = endDarkColors,
        progress = normalizedProgress,
      )
  }

  fun reset() {
    lightAppColorScheme = defaultLightAppColorScheme
    darkAppColorScheme = defaultDarkAppColorScheme
  }

  private fun generateDynamicColorsFromSeedColor(
    useDarkTheme: Boolean,
    seedColor: Color
  ): DynamicColors {
    val scheme =
      SchemeContent(
        sourceColorHct = Hct.fromInt(seedColor.toArgb()),
        isDark = useDarkTheme,
        contrastLevel = 0.0
      )
    val dynamicColors = MaterialDynamicColors()

    return DynamicColors(
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
    )
  }

  private fun DynamicColor.toColor(scheme: SchemeContent): Color {
    return Color(getArgb(scheme))
  }
}

@Immutable
data class DynamicColors(
  val tintedBackground: Color,
  val tintedSurface: Color,
  val tintedForeground: Color,
  val tintedHighlight: Color,
  val outline: Color,
  val outlineVariant: Color,
  val surface: Color,
  val onSurface: Color,
  val onSurfaceVariant: Color,
  val surfaceContainer: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerLowest: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
)

fun AppColorScheme.animate(
  startColors: DynamicColors?,
  endColors: DynamicColors?,
  progress: Float,
): AppColorScheme {
  if (startColors == null || endColors == null) return this

  return copy(
    tintedBackground =
      lerp(
        start = startColors.tintedBackground,
        stop = endColors.tintedBackground,
        fraction = progress
      ),
    tintedSurface =
      lerp(start = startColors.tintedSurface, stop = endColors.tintedSurface, fraction = progress),
    tintedForeground =
      lerp(
        start = startColors.tintedForeground,
        stop = endColors.tintedForeground,
        fraction = progress
      ),
    tintedHighlight =
      lerp(
        start = startColors.tintedHighlight,
        stop = endColors.tintedHighlight,
        fraction = progress
      ),
    outline = lerp(start = startColors.outline, stop = endColors.outline, fraction = progress),
    outlineVariant =
      lerp(
        start = startColors.outlineVariant,
        stop = endColors.outlineVariant,
        fraction = progress
      ),
    surface = lerp(start = startColors.surface, stop = endColors.surface, fraction = progress),
    onSurface =
      lerp(start = startColors.onSurface, stop = endColors.onSurface, fraction = progress),
    onSurfaceVariant =
      lerp(
        start = startColors.onSurfaceVariant,
        stop = endColors.onSurfaceVariant,
        fraction = progress
      ),
    surfaceContainer =
      lerp(
        start = startColors.surfaceContainer,
        stop = endColors.surfaceContainer,
        fraction = progress
      ),
    surfaceContainerLow =
      lerp(
        start = startColors.surfaceContainerLow,
        stop = endColors.surfaceContainerLow,
        fraction = progress
      ),
    surfaceContainerLowest =
      lerp(
        start = startColors.surfaceContainerLowest,
        stop = endColors.surfaceContainerLowest,
        fraction = progress
      ),
    surfaceContainerHigh =
      lerp(
        start = startColors.surfaceContainerHigh,
        stop = endColors.surfaceContainerHigh,
        fraction = progress
      ),
    surfaceContainerHighest =
      lerp(
        start = startColors.surfaceContainerHighest,
        stop = endColors.surfaceContainerHighest,
        fraction = progress
      ),
  )
}

internal val LocalDynamicColorState = compositionLocalOf<DynamicColorState?> { null }
