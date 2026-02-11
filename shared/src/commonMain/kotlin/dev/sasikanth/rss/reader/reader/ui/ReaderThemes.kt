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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.ui.AppColorScheme

internal data class ReaderThemeTokens(
  val primary: Color,
  val secondary: Color,
  val onSurface: Color,
  val onSurfaceVariant: Color,
  val outline: Color,
  val outlineVariant: Color,
  val primaryContainer: Color,
  val onPrimaryContainer: Color,
  val surface: Color,
  val surfaceContainerLowest: Color,
  val surfaceContainerHigh: Color,
  val inverseSurface: Color,
  val backdrop: Color,
  val bottomSheet: Color,
  val bottomSheetBorder: Color,
)

internal fun ReaderThemeTokens.toAppColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  val sourceValues = sourceColorScheme.toValues()
  return AppColorScheme(
    sourceValues.copy(
      primary = primary,
      secondary = secondary,
      outline = outline,
      outlineVariant = outlineVariant,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onPrimaryContainer,
      surface = surface,
      onSurface = onSurface,
      onSurfaceVariant = onSurfaceVariant,
      surfaceContainerLowest = surfaceContainerLowest,
      surfaceContainerHigh = surfaceContainerHigh,
      inverseSurface = inverseSurface,
      backdrop = backdrop,
      bottomSheet = bottomSheet,
      bottomSheetBorder = bottomSheetBorder,
    )
  )
}

internal fun sepiaColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  return ReaderThemeTokens(
      primary = Color(0xFF704F36),
      secondary = Color(0xFF5D4037),
      onSurface = Color(0xFF4E342E),
      onSurfaceVariant = Color(0xFF6D4C41),
      outline = Color(0xFF795548),
      outlineVariant = Color(0xFFD7CCC8),
      primaryContainer = Color(0xFF5D4037),
      onPrimaryContainer = Color(0xFFF5E6D3),
      surface = Color(0xFFF4ECD8),
      surfaceContainerLowest = Color(0xFFFFFBF5),
      surfaceContainerHigh = Color(0xFFE2D6C0),
      inverseSurface = Color(0xFF3E2723),
      backdrop = Color(0xFFEFE6D5),
      bottomSheet = Color(0xFF2D1E19),
      bottomSheetBorder = Color(0xFF4E342E),
    )
    .toAppColorScheme(sourceColorScheme)
}

internal fun parchmentColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  return ReaderThemeTokens(
      primary = Color(0xFF5A5243),
      secondary = Color(0xFF665E4D),
      onSurface = Color(0xFF231F20),
      onSurfaceVariant = Color(0xFF4B463C),
      outline = Color(0xFF857E70),
      outlineVariant = Color(0xFFD6D0C5),
      primaryContainer = Color(0xFFE8E3D9),
      onPrimaryContainer = Color(0xFF1F1B16),
      surface = Color(0xFFFBF8F1),
      surfaceContainerLowest = Color(0xFFFFFFFF),
      surfaceContainerHigh = Color(0xFFEBE6DD),
      inverseSurface = Color(0xFF303030),
      backdrop = Color(0xFFF5F2EA),
      bottomSheet = Color(0xFF1A1915),
      bottomSheetBorder = Color(0xFF322F2A),
    )
    .toAppColorScheme(sourceColorScheme)
}

internal fun solarizedColorScheme(
  isDark: Boolean,
  sourceColorScheme: AppColorScheme,
): AppColorScheme {
  return if (isDark) {
    ReaderThemeTokens(
        primary = Color(0xFF268BD2),
        secondary = Color(0xFF2AA198),
        onSurface = Color(0xFF93A1A1),
        onSurfaceVariant = Color(0xFF839496),
        outline = Color(0xFF657B83),
        outlineVariant = Color(0xFF073642),
        primaryContainer = Color(0xFF004C6C),
        onPrimaryContainer = Color(0xFFC3E8FF),
        surface = Color(0xFF002B36),
        surfaceContainerLowest = Color(0xFF001920),
        surfaceContainerHigh = Color(0xFF093E4A),
        inverseSurface = Color(0xFFFDF6E3),
        backdrop = Color(0xFF002B36),
        bottomSheet = Color(0xFF000000),
        bottomSheetBorder = Color(0xFF263033),
      )
      .toAppColorScheme(sourceColorScheme)
  } else {
    ReaderThemeTokens(
        primary = Color(0xFF268BD2),
        secondary = Color(0xFF2AA198),
        onSurface = Color(0xFF586E75),
        onSurfaceVariant = Color(0xFF657B83),
        outline = Color(0xFF839496),
        outlineVariant = Color(0xFFD8D3C3),
        primaryContainer = Color(0xFFC3E8FF),
        onPrimaryContainer = Color(0xFF001E2D),
        surface = Color(0xFFFDF6E3),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerHigh = Color(0xFFE8E2CF),
        inverseSurface = Color(0xFF073642),
        backdrop = Color(0xFFFDF6E3),
        bottomSheet = Color(0xFF000E14),
        bottomSheetBorder = Color(0xFF263033),
      )
      .toAppColorScheme(sourceColorScheme)
  }
}

internal fun midnightColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  return ReaderThemeTokens(
      primary = Color(0xFFAEC6FF),
      secondary = Color(0xFFBFC6DC),
      onSurface = Color(0xFFC7C7C7),
      onSurfaceVariant = Color(0xFFC4C6D0),
      outline = Color(0xFF8A9199),
      outlineVariant = Color(0xFF44474E),
      primaryContainer = Color(0xFF004494),
      onPrimaryContainer = Color(0xFFD8E2FF),
      surface = Color(0xFF000000),
      surfaceContainerLowest = Color(0xFF000000),
      surfaceContainerHigh = Color(0xFF1C1C1C),
      inverseSurface = Color(0xFFE2E2E6),
      backdrop = Color(0xFF080808),
      bottomSheet = Color(0xFF000000),
      bottomSheetBorder = Color(0xFF222222),
    )
    .toAppColorScheme(sourceColorScheme)
}

internal fun forestColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  return ReaderThemeTokens(
      primary = Color(0xFF9CD67D),
      secondary = Color(0xFFBDCBB0),
      onSurface = Color(0xFFE2E3DE),
      onSurfaceVariant = Color(0xFFC2C8BD),
      outline = Color(0xFF8C9388),
      outlineVariant = Color(0xFF424940),
      primaryContainer = Color(0xFF265017),
      onPrimaryContainer = Color(0xFFB7F396),
      surface = Color(0xFF111C14),
      surfaceContainerLowest = Color(0xFF0E1610),
      surfaceContainerHigh = Color(0xFF232D25),
      inverseSurface = Color(0xFFE2E3DE),
      backdrop = Color(0xFF101912),
      bottomSheet = Color(0xFF0A120B),
      bottomSheetBorder = Color(0xFF253026),
    )
    .toAppColorScheme(sourceColorScheme)
}

internal fun slateColorScheme(sourceColorScheme: AppColorScheme): AppColorScheme {
  return ReaderThemeTokens(
      primary = Color(0xFFA8C7FA),
      secondary = Color(0xFFC2C7CF),
      onSurface = Color(0xFFE3E2E6),
      onSurfaceVariant = Color(0xFFC4C6D0),
      outline = Color(0xFF8C9199),
      outlineVariant = Color(0xFF44474E),
      primaryContainer = Color(0xFF1A437C),
      onPrimaryContainer = Color(0xFFD7E2FF),
      surface = Color(0xFF1C1E23),
      surfaceContainerLowest = Color(0xFF17191D),
      surfaceContainerHigh = Color(0xFF2D2F35),
      inverseSurface = Color(0xFFE3E2E6),
      backdrop = Color(0xFF181A1F),
      bottomSheet = Color(0xFF121316),
      bottomSheetBorder = Color(0xFF2F3138),
    )
    .toAppColorScheme(sourceColorScheme)
}
