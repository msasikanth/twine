/*
 * Copyright 2023 Sasikanth Miriyampalli
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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

@Stable
class AppColorScheme(
  tintedBackground: Color,
  tintedSurface: Color,
  tintedForeground: Color,
  tintedHighlight: Color,
  outline: Color,
  outlineVariant: Color,
  surface: Color,
  onSurface: Color,
  onSurfaceVariant: Color,
  surfaceContainer: Color,
  surfaceContainerLow: Color,
  surfaceContainerLowest: Color,
  surfaceContainerHigh: Color,
  surfaceContainerHighest: Color,
  textEmphasisHigh: Color,
  textEmphasisMed: Color,
) {

  var tintedBackground by mutableStateOf(tintedBackground, structuralEqualityPolicy())
    internal set

  var tintedSurface by mutableStateOf(tintedSurface, structuralEqualityPolicy())
    internal set

  var tintedForeground by mutableStateOf(tintedForeground, structuralEqualityPolicy())
    internal set

  var tintedHighlight by mutableStateOf(tintedHighlight, structuralEqualityPolicy())
    internal set

  var outline by mutableStateOf(outline, structuralEqualityPolicy())
    internal set

  var outlineVariant by mutableStateOf(outlineVariant, structuralEqualityPolicy())
    internal set

  var surface by mutableStateOf(surface, structuralEqualityPolicy())
    internal set

  var onSurface by mutableStateOf(onSurface, structuralEqualityPolicy())
    internal set

  var onSurfaceVariant by mutableStateOf(onSurfaceVariant, structuralEqualityPolicy())
    internal set

  var surfaceContainer by mutableStateOf(surfaceContainer, structuralEqualityPolicy())
    internal set

  var surfaceContainerLow by mutableStateOf(surfaceContainerLow, structuralEqualityPolicy())
    internal set

  var surfaceContainerLowest by mutableStateOf(surfaceContainerLowest, structuralEqualityPolicy())
    internal set

  var surfaceContainerHigh by mutableStateOf(surfaceContainerHigh, structuralEqualityPolicy())
    internal set

  var surfaceContainerHighest by mutableStateOf(surfaceContainerHighest, structuralEqualityPolicy())
    internal set

  var textEmphasisHigh by mutableStateOf(textEmphasisHigh, structuralEqualityPolicy())
    internal set

  var textEmphasisMed by mutableStateOf(textEmphasisMed, structuralEqualityPolicy())
    internal set

  fun copy(
    tintedBackground: Color = this.tintedBackground,
    tintedSurface: Color = this.surfaceContainer,
    tintedForeground: Color = this.tintedForeground,
    tintedHighlight: Color = this.tintedHighlight,
    outline: Color = this.outline,
    outlineVariant: Color = this.outlineVariant,
    surface: Color = this.surface,
    onSurface: Color = this.onSurface,
    onSurfaceVariant: Color = this.onSurfaceVariant,
    surfaceContainer: Color = this.surfaceContainer,
    surfaceContainerLow: Color = this.surfaceContainerLow,
    surfaceContainerLowest: Color = this.surfaceContainerLowest,
    surfaceContainerHigh: Color = this.surfaceContainerHigh,
    surfaceContainerHighest: Color = this.surfaceContainerHighest,
    textEmphasisHigh: Color = this.textEmphasisHigh,
    textEmphasisMed: Color = this.textEmphasisMed
  ): AppColorScheme =
    AppColorScheme(
      tintedBackground = tintedBackground,
      tintedSurface = tintedSurface,
      tintedForeground = tintedForeground,
      tintedHighlight = tintedHighlight,
      outline = outline,
      outlineVariant = outlineVariant,
      surface = surface,
      onSurface = onSurface,
      onSurfaceVariant = onSurfaceVariant,
      surfaceContainer = surfaceContainer,
      surfaceContainerLow = surfaceContainerLow,
      surfaceContainerLowest = surfaceContainerLowest,
      surfaceContainerHigh = surfaceContainerHigh,
      surfaceContainerHighest = surfaceContainerHighest,
      textEmphasisHigh = textEmphasisHigh,
      textEmphasisMed = textEmphasisMed
    )
}

fun lightAppColorScheme(): AppColorScheme {
  return AppColorScheme(
    tintedBackground = Color(0xFFF4FFF8),
    tintedSurface = Color(0xFFBAFFE4),
    tintedForeground = Color(0xFF006C53),
    tintedHighlight = Color(0xFF63DBB5),
    outline = Color(0xFF6A7771),
    outlineVariant = Color(0xFFBCCAC2),
    surface = Color(0xFFF5FBF6),
    onSurface = Color(0xFF171D1A),
    onSurfaceVariant = Color(0xFF3D4944),
    surfaceContainer = Color(0xFFE9EFEA),
    surfaceContainerLow = Color(0xFFEFF5F0),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFE4EAE5),
    surfaceContainerHighest = Color(0xFFDEE4DF),
    textEmphasisHigh = Color.Black.copy(alpha = 0.9f),
    textEmphasisMed = Color.Black.copy(alpha = 0.7f)
  )
}

fun darkAppColorScheme(): AppColorScheme {
  return AppColorScheme(
    tintedBackground = Color(0xFF002117),
    tintedSurface = Color(0xFF00382A),
    tintedForeground = Color(0xFF63DBB5),
    tintedHighlight = Color(0xFF006C53),
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3F4944),
    surface = Color(0xFF111412),
    onSurface = Color(0xFFE1E3E0),
    onSurfaceVariant = Color(0xFFBFC9C3),
    surfaceContainer = Color(0xFF1D201F),
    surfaceContainerLow = Color(0xFF191C1B),
    surfaceContainerLowest = Color(0xFF0B0F0D),
    surfaceContainerHigh = Color(0xFF272B29),
    surfaceContainerHighest = Color(0xFF323633),
    textEmphasisHigh = Color.White.copy(alpha = 0.9f),
    textEmphasisMed = Color.White.copy(alpha = 0.7f)
  )
}

internal val LocalAppColorScheme = compositionLocalOf { darkAppColorScheme() }
