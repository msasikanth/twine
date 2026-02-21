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
  val backdrop: Color,
  val bottomSheet: Color,
  val bottomSheetInverse: Color,
  val bottomSheetBorder: Color,
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
      backdrop = lerp(backdrop, to.backdrop, fraction),
      bottomSheet = lerp(bottomSheet, to.bottomSheet, fraction),
      bottomSheetInverse = lerp(bottomSheetInverse, to.bottomSheetInverse, fraction),
      bottomSheetBorder = lerp(bottomSheetBorder, to.bottomSheetBorder, fraction),
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

  var backdrop by mutableStateOf(values.backdrop)
    internal set

  var bottomSheet by mutableStateOf(values.bottomSheet)
    internal set

  var bottomSheetInverse by mutableStateOf(values.bottomSheetInverse)
    internal set

  var bottomSheetBorder by mutableStateOf(values.bottomSheetBorder)
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
      backdrop = backdrop,
      bottomSheet = bottomSheet,
      bottomSheetInverse = bottomSheetInverse,
      bottomSheetBorder = bottomSheetBorder,
      error = error,
    )

  fun copy(): AppColorScheme = AppColorScheme(toValues())

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
    backdrop = if (amoled) Color.Black else values.backdrop
    bottomSheet = if (amoled) Color.Black else values.bottomSheet
    bottomSheetInverse = values.bottomSheetInverse
    bottomSheetBorder = values.bottomSheetBorder
    error = values.error
  }
}

fun lightAppColorScheme(): AppColorValues {
  return forestColorScheme(isDark = false).toValues()
}

fun darkAppColorScheme(): AppColorValues {
  return forestColorScheme(isDark = true).toValues()
}

internal val LocalAppColorScheme = compositionLocalOf { AppColorScheme(darkAppColorScheme()) }
