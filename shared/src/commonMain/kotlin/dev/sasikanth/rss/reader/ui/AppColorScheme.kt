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
package dev.sasikanth.rss.reader.ui

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

@Stable
data class AppColorValues(
  val primary: Color,
  val onPrimary: Color,
  val secondary: Color,
  val outline: Color,
  val outlineVariant: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
  val surface: Color,
  val onSurface: Color,
  val onSurfaceVariant: Color,
  val surfaceContainer: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerLowest: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
  val inversePrimary: Color,
  val inverseSurface: Color,
  val inverseOnSurface: Color,
  val textEmphasisHigh: Color,
  val textEmphasisMed: Color,
  val backdrop: Color,
  val bottomSheet: Color,
  val bottomSheetInverse: Color,
  val bottomSheetBorder: Color,
  val tintedBackground: Color,
  val tintedSurface: Color,
  val tintedForeground: Color,
  val tintedHighlight: Color,
  val error: Color,
) {

  fun lerp(to: AppColorValues, fraction: Float): AppColorValues {
    return AppColorValues(
      primary = lerp(primary, to.primary, fraction),
      onPrimary = lerp(onPrimary, to.onPrimary, fraction),
      secondary = lerp(secondary, to.secondary, fraction),
      outline = lerp(outline, to.outline, fraction),
      outlineVariant = lerp(outlineVariant, to.outlineVariant, fraction),
      primaryContainer = lerp(primaryContainer, to.primaryContainer, fraction),
      onPrimaryContainer = lerp(onPrimaryContainer, to.onPrimaryContainer, fraction),
      surface = lerp(surface, to.surface, fraction),
      onSurface = lerp(onSurface, to.onSurface, fraction),
      onSurfaceVariant = lerp(onSurfaceVariant, to.onSurfaceVariant, fraction),
      surfaceContainer = lerp(surfaceContainer, to.surfaceContainer, fraction),
      surfaceContainerLow = lerp(surfaceContainerLow, to.surfaceContainerLow, fraction),
      surfaceContainerLowest = lerp(surfaceContainerLowest, to.surfaceContainerLowest, fraction),
      surfaceContainerHigh = lerp(surfaceContainerHigh, to.surfaceContainerHigh, fraction),
      surfaceContainerHighest = lerp(surfaceContainerHighest, to.surfaceContainerHighest, fraction),
      inversePrimary = lerp(inversePrimary, to.inversePrimary, fraction),
      inverseSurface = lerp(inverseSurface, to.inverseSurface, fraction),
      inverseOnSurface = lerp(inverseOnSurface, to.inverseOnSurface, fraction),
      textEmphasisHigh = lerp(textEmphasisHigh, to.textEmphasisHigh, fraction),
      textEmphasisMed = lerp(textEmphasisMed, to.textEmphasisMed, fraction),
      backdrop = lerp(backdrop, to.backdrop, fraction),
      bottomSheet = lerp(bottomSheet, to.bottomSheet, fraction),
      bottomSheetInverse = lerp(bottomSheetInverse, to.bottomSheetInverse, fraction),
      bottomSheetBorder = lerp(bottomSheetBorder, to.bottomSheetBorder, fraction),
      tintedBackground = lerp(tintedBackground, to.tintedBackground, fraction),
      tintedSurface = lerp(tintedSurface, to.tintedSurface, fraction),
      tintedForeground = lerp(tintedForeground, to.tintedForeground, fraction),
      tintedHighlight = lerp(tintedHighlight, to.tintedHighlight, fraction),
      error = lerp(error, to.error, fraction),
    )
  }
}

@Stable
class AppColorScheme(values: AppColorValues) {

  var primary by mutableStateOf(values.primary)
    internal set

  var onPrimary by mutableStateOf(values.onPrimary)
    internal set

  var secondary by mutableStateOf(values.secondary)
    internal set

  var outline by mutableStateOf(values.outline)
    internal set

  var outlineVariant by mutableStateOf(values.outlineVariant)
    internal set

  var primaryContainer by mutableStateOf(values.primaryContainer)
    internal set

  var onPrimaryContainer by mutableStateOf(values.onPrimaryContainer)
    internal set

  var surface by mutableStateOf(values.surface)
    internal set

  var onSurface by mutableStateOf(values.onSurface)
    internal set

  var onSurfaceVariant by mutableStateOf(values.onSurfaceVariant)
    internal set

  var surfaceContainer by mutableStateOf(values.surfaceContainer)
    internal set

  var surfaceContainerLow by mutableStateOf(values.surfaceContainerLow)
    internal set

  var surfaceContainerLowest by mutableStateOf(values.surfaceContainerLowest)
    internal set

  var surfaceContainerHigh by mutableStateOf(values.surfaceContainerHigh)
    internal set

  var surfaceContainerHighest by mutableStateOf(values.surfaceContainerHighest)
    internal set

  var inversePrimary by mutableStateOf(values.inversePrimary)
    internal set

  var inverseSurface by mutableStateOf(values.inverseSurface)
    internal set

  var inverseOnSurface by mutableStateOf(values.inverseOnSurface)
    internal set

  var textEmphasisHigh by mutableStateOf(values.textEmphasisHigh)
    internal set

  var textEmphasisMed by mutableStateOf(values.textEmphasisMed)
    internal set

  var backdrop by mutableStateOf(values.backdrop)
    internal set

  var bottomSheet by mutableStateOf(values.bottomSheet)
    internal set

  var bottomSheetInverse by mutableStateOf(values.bottomSheetInverse)
    internal set

  var bottomSheetBorder by mutableStateOf(values.bottomSheetBorder)
    internal set

  var tintedBackground by mutableStateOf(values.tintedBackground)
    internal set

  var tintedSurface by mutableStateOf(values.tintedSurface)
    internal set

  var tintedForeground by mutableStateOf(values.tintedForeground)
    internal set

  var tintedHighlight by mutableStateOf(values.tintedHighlight)
    internal set

  var error by mutableStateOf(values.error)
    internal set

  fun toValues(): AppColorValues =
    AppColorValues(
      primary = primary,
      onPrimary = onPrimary,
      secondary = secondary,
      outline = outline,
      outlineVariant = outlineVariant,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      surface = surface,
      onSurface = onSurface,
      onSurfaceVariant = onSurfaceVariant,
      surfaceContainer = surfaceContainer,
      surfaceContainerLow = surfaceContainerLow,
      surfaceContainerLowest = surfaceContainerLowest,
      surfaceContainerHigh = surfaceContainerHigh,
      surfaceContainerHighest = surfaceContainerHighest,
      inversePrimary = inversePrimary,
      inverseSurface = inverseSurface,
      inverseOnSurface = inverseOnSurface,
      textEmphasisHigh = textEmphasisHigh,
      textEmphasisMed = textEmphasisMed,
      backdrop = backdrop,
      bottomSheet = bottomSheet,
      bottomSheetInverse = bottomSheetInverse,
      bottomSheetBorder = bottomSheetBorder,
      tintedBackground = tintedBackground,
      tintedSurface = tintedSurface,
      tintedForeground = tintedForeground,
      tintedHighlight = tintedHighlight,
      error = error,
    )

  fun copy(): AppColorScheme = AppColorScheme(toValues())

  fun updateFrom(other: AppColorScheme, amoled: Boolean = false) {
    primary = other.primary
    onPrimary = other.onPrimary
    secondary = other.secondary
    outline = other.outline
    outlineVariant = other.outlineVariant
    primaryContainer = other.primaryContainer
    onPrimaryContainer = other.onPrimaryContainer
    surface = if (amoled) Color.Black else other.surface
    onSurface = other.onSurface
    onSurfaceVariant = other.onSurfaceVariant
    surfaceContainer = if (amoled) Color.Black else other.surfaceContainer
    surfaceContainerLow = if (amoled) Color.Black else other.surfaceContainerLow
    surfaceContainerLowest = if (amoled) Color.Black else other.surfaceContainerLowest
    surfaceContainerHigh = if (amoled) Color(0xFF1D201F) else other.surfaceContainerHigh
    surfaceContainerHighest = if (amoled) Color(0xFF272B29) else other.surfaceContainerHighest
    inversePrimary = other.inversePrimary
    inverseSurface = other.inverseSurface
    inverseOnSurface = other.inverseOnSurface
    textEmphasisHigh = other.textEmphasisHigh
    textEmphasisMed = other.textEmphasisMed
    backdrop = if (amoled) Color.Black else other.backdrop
    bottomSheet = if (amoled) Color.Black else other.bottomSheet
    bottomSheetInverse = other.bottomSheetInverse
    bottomSheetBorder = other.bottomSheetBorder
    tintedBackground = if (amoled) Color.Black else other.tintedBackground
    tintedSurface = other.tintedSurface
    tintedForeground = other.tintedForeground
    tintedHighlight = other.tintedHighlight
    error = other.error
  }

  fun updateFrom(values: AppColorValues, amoled: Boolean = false) {
    primary = values.primary
    onPrimary = values.onPrimary
    secondary = values.secondary
    outline = values.outline
    outlineVariant = values.outlineVariant
    primaryContainer = values.primaryContainer
    onPrimaryContainer = values.onPrimaryContainer
    surface = if (amoled) Color.Black else values.surface
    onSurface = values.onSurface
    onSurfaceVariant = values.onSurfaceVariant
    surfaceContainer = if (amoled) Color.Black else values.surfaceContainer
    surfaceContainerLow = if (amoled) Color.Black else values.surfaceContainerLow
    surfaceContainerLowest = if (amoled) Color.Black else values.surfaceContainerLowest
    surfaceContainerHigh = if (amoled) Color(0xFF1D201F) else values.surfaceContainerHigh
    surfaceContainerHighest = if (amoled) Color(0xFF272B29) else values.surfaceContainerHighest
    inversePrimary = values.inversePrimary
    inverseSurface = values.inverseSurface
    inverseOnSurface = values.inverseOnSurface
    textEmphasisHigh = values.textEmphasisHigh
    textEmphasisMed = values.textEmphasisMed
    backdrop = if (amoled) Color.Black else values.backdrop
    bottomSheet = if (amoled) Color.Black else values.bottomSheet
    bottomSheetInverse = values.bottomSheetInverse
    bottomSheetBorder = values.bottomSheetBorder
    tintedBackground = if (amoled) Color.Black else values.tintedBackground
    tintedSurface = values.tintedSurface
    tintedForeground = values.tintedForeground
    tintedHighlight = values.tintedHighlight
    error = values.error
  }

  fun lerp(to: AppColorScheme, fraction: Float): AppColorScheme {
    return AppColorScheme(
      AppColorValues(
        primary = lerp(primary, to.primary, fraction),
        onPrimary = lerp(onPrimary, to.onPrimary, fraction),
        secondary = lerp(secondary, to.secondary, fraction),
        outline = lerp(outline, to.outline, fraction),
        outlineVariant = lerp(outlineVariant, to.outlineVariant, fraction),
        primaryContainer = lerp(primaryContainer, to.primaryContainer, fraction),
        onPrimaryContainer = lerp(onPrimaryContainer, to.onPrimaryContainer, fraction),
        surface = lerp(surface, to.surface, fraction),
        onSurface = lerp(onSurface, to.onSurface, fraction),
        onSurfaceVariant = lerp(onSurfaceVariant, to.onSurfaceVariant, fraction),
        surfaceContainer = lerp(surfaceContainer, to.surfaceContainer, fraction),
        surfaceContainerLow = lerp(surfaceContainerLow, to.surfaceContainerLow, fraction),
        surfaceContainerLowest = lerp(surfaceContainerLowest, to.surfaceContainerLowest, fraction),
        surfaceContainerHigh = lerp(surfaceContainerHigh, to.surfaceContainerHigh, fraction),
        surfaceContainerHighest =
          lerp(surfaceContainerHighest, to.surfaceContainerHighest, fraction),
        inversePrimary = lerp(inversePrimary, to.inversePrimary, fraction),
        inverseSurface = lerp(inverseSurface, to.inverseSurface, fraction),
        inverseOnSurface = lerp(inverseOnSurface, to.inverseOnSurface, fraction),
        textEmphasisHigh = lerp(textEmphasisHigh, to.textEmphasisHigh, fraction),
        textEmphasisMed = lerp(textEmphasisMed, to.textEmphasisMed, fraction),
        backdrop = lerp(backdrop, to.backdrop, fraction),
        bottomSheet = lerp(bottomSheet, to.bottomSheet, fraction),
        bottomSheetInverse = lerp(bottomSheetInverse, to.bottomSheetInverse, fraction),
        bottomSheetBorder = lerp(bottomSheetBorder, to.bottomSheetBorder, fraction),
        tintedBackground = lerp(tintedBackground, to.tintedBackground, fraction),
        tintedSurface = lerp(tintedSurface, to.tintedSurface, fraction),
        tintedForeground = lerp(tintedForeground, to.tintedForeground, fraction),
        tintedHighlight = lerp(tintedHighlight, to.tintedHighlight, fraction),
        error = lerp(error, to.error, fraction),
      )
    )
  }
}

fun lightAppColorScheme(): AppColorValues {
  return AppColorValues(
    primary = Color(0xFF006B54),
    onPrimary = Color.White,
    secondary = Color(0xFF4C635D),
    outline = Color(0xFF707976),
    outlineVariant = Color(0xFFBFC9C5),
    primaryContainer = Color(0xFF9CF2D6),
    onPrimaryContainer = Color(0xFF002118),
    surface = Color(0xFFF4FBF9),
    onSurface = Color(0xFF161D1B),
    onSurfaceVariant = Color(0xFF3F4946),
    surfaceContainer = Color(0xFFE8EFED),
    surfaceContainerLow = Color(0xFFEEF5F3),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE2E9E7),
    surfaceContainerHighest = Color(0xFFDCE5E1),
    inversePrimary = Color(0xFF51DEB8),
    inverseSurface = Color(0xFF2B322F),
    inverseOnSurface = Color(0xFFEFF2F0),
    textEmphasisHigh = Color.Black.copy(alpha = 0.9f),
    textEmphasisMed = Color.Black.copy(alpha = 0.7f),
    backdrop = Color(0xFFE5EEEB),
    bottomSheet = Color(0xFF002118),
    bottomSheetInverse = Color.Black,
    bottomSheetBorder = Color(0xFF303E39),
    tintedBackground = Color(0xFF002118),
    tintedSurface = Color(0xFFEEF5F3),
    tintedForeground = Color(0xFF006B54),
    tintedHighlight = Color(0xFF707976),
    error = Color(0xFFBA1A1A),
  )
}

fun darkAppColorScheme(): AppColorValues {
  return AppColorValues(
    primary = Color(0xFF51DEB8),
    onPrimary = Color(0xFF00382B),
    secondary = Color(0xFFB3CCC3),
    outline = Color(0xFF89938F),
    outlineVariant = Color(0xFF3F4946),
    primaryContainer = Color(0xFF00513F),
    onPrimaryContainer = Color(0xFF9CF2D6),
    surface = Color(0xFF0E1512),
    onSurface = Color(0xFFDEE4E1),
    onSurfaceVariant = Color(0xFFBFC9C5),
    surfaceContainer = Color(0xFF1A211E),
    surfaceContainerLow = Color(0xFF161D1A),
    surfaceContainerLowest = Color(0xFF080F0D),
    surfaceContainerHigh = Color(0xFF242B28),
    surfaceContainerHighest = Color(0xFF2F3633),
    inversePrimary = Color(0xFF006B54),
    inverseSurface = Color(0xFFF4FBF9),
    inverseOnSurface = Color(0xFF161D1B),
    textEmphasisHigh = Color.White.copy(alpha = 0.9f),
    textEmphasisMed = Color.White.copy(alpha = 0.7f),
    backdrop = Color(0xFF090F0D),
    bottomSheet = Color.Black,
    bottomSheetInverse = Color(0xFF002118),
    bottomSheetBorder = Color(0xFF303E39),
    tintedBackground = Color.Black,
    tintedSurface = Color(0xFF1A211E),
    tintedForeground = Color(0xFF51DEB8),
    tintedHighlight = Color(0xFF89938F),
    error = Color(0xFFFFB4AB),
  )
}

internal val LocalAppColorScheme = compositionLocalOf { AppColorScheme(darkAppColorScheme()) }
