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
import androidx.compose.animation.core.FastOutSlowInEasing
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
import dev.sasikanth.material.color.utilities.hct.Hct
import dev.sasikanth.material.color.utilities.scheme.DynamicScheme
import dev.sasikanth.material.color.utilities.scheme.SchemeContent
import dev.sasikanth.material.color.utilities.scheme.SchemeTonalSpot
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.NTuple4
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberDynamicColorState(
  defaultLightAppColorScheme: AppColorScheme,
  defaultDarkAppColorScheme: AppColorScheme,
  useTonalSpotScheme: Boolean = true,
  useAmoled: Boolean = false,
): DynamicColorState {
  return remember {
    DynamicColorState(
      defaultLightAppColorScheme = defaultLightAppColorScheme,
      defaultDarkAppColorScheme = defaultDarkAppColorScheme,
      useTonalSpotScheme = useTonalSpotScheme,
      useAmoled = useAmoled
    )
  }
}

@Stable
internal class DynamicColorState(
  private val defaultLightAppColorScheme: AppColorScheme,
  private val defaultDarkAppColorScheme: AppColorScheme,
  private val useTonalSpotScheme: Boolean,
  useAmoled: Boolean,
) {
  var lightAppColorScheme by mutableStateOf(defaultLightAppColorScheme)
    private set

  var darkAppColorScheme by mutableStateOf(defaultDarkAppColorScheme)
    private set

  var useAmoled by mutableStateOf(useAmoled)

  private var lastFromSeedColor: Color? = null
  private var lastToSeedColor: Color? = null
  private var lastProgress: Float = 0f

  private val cache = lruCache<String, AppColorScheme>(maxSize = 10)

  suspend fun animate(fromSeedColor: Color?, toSeedColor: Color?, progress: Float) {
    lastFromSeedColor = fromSeedColor
    lastToSeedColor = toSeedColor
    lastProgress = progress

    val normalizedProgress =
      ease(
        if (progress < -EPSILON) {
          progress.absoluteValue.inverse()
        } else {
          progress
        }
      )

    val (startLight, startDark, endLight, endDark) =
      withContext(Dispatchers.Default) {
        val defaultLightSeedColor = defaultLightAppColorScheme.primary
        val defaultDarkSeedColor = defaultDarkAppColorScheme.primary

        val startLight =
          cache["light_${useAmoled}_$fromSeedColor"]
            ?: generateDynamicColorsFromSeedColor(
                useDarkTheme = false,
                seedColor = fromSeedColor ?: defaultLightSeedColor
              )
              .also { cache.put("light_${useAmoled}_$fromSeedColor", it) }

        val startDark =
          cache["dark_${useAmoled}_$fromSeedColor"]
            ?: generateDynamicColorsFromSeedColor(
                useDarkTheme = true,
                seedColor = fromSeedColor ?: defaultLightSeedColor
              )
              .also { cache.put("dark_${useAmoled}_$fromSeedColor", it) }

        val endLight =
          cache["light_${useAmoled}_$toSeedColor"]
            ?: generateDynamicColorsFromSeedColor(
                useDarkTheme = false,
                seedColor = toSeedColor ?: defaultDarkSeedColor
              )
              .also { cache.put("light_${useAmoled}_$toSeedColor", it) }

        val endDark =
          cache["dark_${useAmoled}_$toSeedColor"]
            ?: generateDynamicColorsFromSeedColor(
                useDarkTheme = true,
                seedColor = toSeedColor ?: defaultDarkSeedColor
              )
              .also { cache.put("dark_${useAmoled}_$toSeedColor", it) }

        NTuple4(startLight, startDark, endLight, endDark)
      }

    lightAppColorScheme =
      if (startLight == endLight) {
        startLight
      } else {
        startLight.animate(to = endLight, progress = normalizedProgress)
      }

    darkAppColorScheme =
      if (startDark == endDark) {
        startDark
      } else {
        startDark.animate(
          to = endDark,
          progress = normalizedProgress,
        )
      }
  }

  suspend fun refresh() {
    animate(lastFromSeedColor, lastToSeedColor, lastProgress)
  }

  fun reset() {
    lastFromSeedColor = null
    lastToSeedColor = null
    lastProgress = 0f
    lightAppColorScheme = defaultLightAppColorScheme
    darkAppColorScheme =
      if (useAmoled) defaultDarkAppColorScheme.amoled() else defaultDarkAppColorScheme
  }

  private fun generateDynamicColorsFromSeedColor(
    useDarkTheme: Boolean,
    seedColor: Color,
  ): AppColorScheme {
    val sourceColorHct = Hct.fromInt(seedColor.toArgb())
    val scheme =
      if (useTonalSpotScheme) {
        SchemeTonalSpot(sourceColorHct = sourceColorHct, isDark = useDarkTheme, contrastLevel = 0.0)
      } else {
        SchemeContent(sourceColorHct = sourceColorHct, isDark = useDarkTheme, contrastLevel = 0.0)
      }
    val dynamicColors = MaterialDynamicColors()
    val defaultColorScheme =
      if (useDarkTheme) {
        defaultDarkAppColorScheme
      } else {
        defaultLightAppColorScheme
      }

    val bottomSheet =
      DynamicColor.fromPalette(
          palette = { s -> s.primaryPalette },
          tone = { s -> if (s.isDark) 0.0 else 5.0 },
          background = { s -> dynamicColors.highestSurface(s) },
        )
        .toColor(scheme)

    val colorScheme =
      AppColorScheme(
        primary = dynamicColors.primary().toColor(scheme),
        secondary = dynamicColors.secondary().toColor(scheme),
        outline = dynamicColors.outline().toColor(scheme),
        outlineVariant = dynamicColors.outlineVariant().toColor(scheme),
        primaryContainer = primaryContainer(dynamicColors).toColor(scheme),
        onPrimaryContainer =
          DynamicColor.fromPalette(
              { s -> s.primaryPalette },
              { s -> if (s.isDark) 90.0 else 10.0 },
              { primaryContainer(dynamicColors) },
              null
            )
            .toColor(scheme),
        surface = dynamicColors.surface().toColor(scheme),
        onSurface = dynamicColors.onSurface().toColor(scheme),
        onSurfaceVariant = dynamicColors.onSurfaceVariant().toColor(scheme),
        surfaceContainer = dynamicColors.surfaceContainer().toColor(scheme),
        surfaceContainerLow = dynamicColors.surfaceContainerLow().toColor(scheme),
        surfaceContainerLowest = dynamicColors.surfaceContainerLowest().toColor(scheme),
        surfaceContainerHigh = dynamicColors.surfaceContainerHigh().toColor(scheme),
        surfaceContainerHighest = dynamicColors.surfaceContainerHighest().toColor(scheme),
        inversePrimary = dynamicColors.inversePrimary().toColor(scheme),
        inverseSurface = dynamicColors.inverseSurface().toColor(scheme),
        inverseOnSurface = dynamicColors.inverseOnSurface().toColor(scheme),
        textEmphasisHigh = defaultColorScheme.textEmphasisHigh,
        textEmphasisMed = defaultColorScheme.textEmphasisMed,
        backdrop =
          DynamicColor.fromPalette(
              palette = { s -> s.neutralPalette },
              tone = { s -> if (s.isDark) 5.0 else 95.0 },
            )
            .toColor(scheme),
        bottomSheet = bottomSheet,
        bottomSheetBorder =
          DynamicColor.fromPalette(
              palette = { s -> s.neutralPalette },
              tone = { s -> 20.0 },
            )
            .toColor(scheme),
        tintedBackground = bottomSheet,
        tintedSurface = dynamicColors.surfaceContainerLow().toColor(scheme),
        tintedForeground = dynamicColors.primary().toColor(scheme),
        tintedHighlight = dynamicColors.outline().toColor(scheme),
      )

    return if (useDarkTheme && useAmoled) {
      colorScheme.amoled()
    } else {
      colorScheme
    }
  }

  private fun primaryContainer(dynamicColors: MaterialDynamicColors) =
    DynamicColor.fromPalette({ s -> s.primaryPalette }, { s -> if (s.isDark) 40.0 else 90.0 }) {
      s: DynamicScheme ->
      dynamicColors.highestSurface(s)
    }

  private fun DynamicColor.toColor(scheme: DynamicScheme): Color {
    return Color(getArgb(scheme))
  }

  private fun AppColorScheme.animate(
    to: AppColorScheme?,
    progress: Float,
  ): AppColorScheme {
    if (to == null) return this

    return copy(
      primary = lerp(start = primary, stop = to.primary, fraction = progress),
      secondary = lerp(start = secondary, stop = to.secondary, fraction = progress),
      outline = lerp(start = outline, stop = to.outline, fraction = progress),
      outlineVariant = lerp(start = outlineVariant, stop = to.outlineVariant, fraction = progress),
      primaryContainer =
        lerp(start = primaryContainer, stop = to.primaryContainer, fraction = progress),
      onPrimaryContainer =
        lerp(start = onPrimaryContainer, stop = to.onPrimaryContainer, fraction = progress),
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
        lerp(
          start = surfaceContainerHighest,
          stop = to.surfaceContainerHighest,
          fraction = progress
        ),
      inversePrimary = lerp(start = inversePrimary, stop = to.inversePrimary, fraction = progress),
      inverseSurface = lerp(start = inverseSurface, stop = to.inverseSurface, fraction = progress),
      inverseOnSurface =
        lerp(start = inverseOnSurface, stop = to.inverseOnSurface, fraction = progress),
      backdrop = lerp(start = backdrop, stop = to.backdrop, fraction = progress),
      bottomSheet = lerp(start = bottomSheet, stop = to.bottomSheet, fraction = progress),
      bottomSheetBorder =
        lerp(start = bottomSheetBorder, stop = to.bottomSheetBorder, fraction = progress),
      tintedBackground =
        lerp(start = tintedBackground, stop = to.tintedBackground, fraction = progress),
      tintedSurface = lerp(start = tintedSurface, stop = to.tintedSurface, fraction = progress),
      tintedForeground =
        lerp(start = tintedForeground, stop = to.tintedForeground, fraction = progress),
      tintedHighlight =
        lerp(start = tintedHighlight, stop = to.tintedHighlight, fraction = progress),
    )
  }

  private fun ease(progress: Float): Float {
    return FastOutSlowInEasing.transform(progress)
  }
}

internal val LocalDynamicColorState =
  staticCompositionLocalOf<DynamicColorState> {
    throw NullPointerException("Please provide a dynamic color state")
  }
