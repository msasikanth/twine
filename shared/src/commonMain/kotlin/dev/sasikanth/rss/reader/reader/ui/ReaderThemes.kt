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

internal fun sepiaColorScheme(): AppColorScheme {
  return AppColorScheme(
    primary = Color(0xFF704F36),
    onPrimary = Color.White,
    secondary = Color(0xFF5D4037),
    outline = Color(0xFF795548),
    outlineVariant = Color(0xFFD7CCC8),
    primaryContainer = Color(0xFF5D4037),
    onPrimaryContainer = Color(0xFFF5E6D3),
    surface = Color(0xFFF4ECD8),
    onSurface = Color(0xFF4E342E),
    onSurfaceVariant = Color(0xFF6D4C41),
    surfaceContainer = Color(0xFFEBE0CC),
    surfaceContainerLow = Color(0xFFF8F1E5),
    surfaceContainerLowest = Color(0xFFFFFBF5),
    surfaceContainerHigh = Color(0xFFE2D6C0),
    surfaceContainerHighest = Color(0xFFD9CCB4),
    inversePrimary = Color(0xFFA1887F),
    inverseSurface = Color(0xFF3E2723),
    inverseOnSurface = Color(0xFFF4ECD8),
    textEmphasisHigh = Color(0xFF3E2723).copy(alpha = 0.95f),
    textEmphasisMed = Color(0xFF5D4037).copy(alpha = 0.8f),
    backdrop = Color(0xFFEFE6D5),
    bottomSheet = Color(0xFF2D1E19),
    bottomSheetInverse = Color(0xFF000000),
    bottomSheetBorder = Color(0xFF4E342E),
    tintedBackground = Color(0xFFEBE0CC),
    tintedSurface = Color(0xFFF4ECD8),
    tintedForeground = Color(0xFF704F36),
    tintedHighlight = Color(0xFF795548),
    error = Color(0xFFB3261E),
  )
}

internal fun parchmentColorScheme(): AppColorScheme {
  return AppColorScheme(
    primary = Color(0xFF5A5243),
    onPrimary = Color.White,
    secondary = Color(0xFF665E4D),
    outline = Color(0xFF857E70),
    outlineVariant = Color(0xFFD6D0C5),
    primaryContainer = Color(0xFFE8E3D9),
    onPrimaryContainer = Color(0xFF1F1B16),
    surface = Color(0xFFFBF8F1),
    onSurface = Color(0xFF231F20),
    onSurfaceVariant = Color(0xFF4B463C),
    surfaceContainer = Color(0xFFF3EFE7),
    surfaceContainerLow = Color(0xFFFDFBF7),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFEBE6DD),
    surfaceContainerHighest = Color(0xFFE3DED4),
    inversePrimary = Color(0xFF787060),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF5EEE4),
    textEmphasisHigh = Color(0xFF121212).copy(alpha = 0.95f),
    textEmphasisMed = Color(0xFF3C3C3C).copy(alpha = 0.8f),
    backdrop = Color(0xFFF5F2EA),
    bottomSheet = Color(0xFF1A1915),
    bottomSheetInverse = Color(0xFF000000),
    bottomSheetBorder = Color(0xFF322F2A),
    tintedBackground = Color(0xFFF3EFE7),
    tintedSurface = Color(0xFFFBF8F1),
    tintedForeground = Color(0xFF5A5243),
    tintedHighlight = Color(0xFF857E70),
    error = Color(0xFFBA1A1A),
  )
}

internal fun solarizedColorScheme(isDark: Boolean): AppColorScheme {
  return if (isDark) {
    AppColorScheme(
      primary = Color(0xFF268BD2),
      onPrimary = Color(0xFF003247),
      secondary = Color(0xFF2AA198),
      outline = Color(0xFF657B83),
      outlineVariant = Color(0xFF073642),
      primaryContainer = Color(0xFF004C6C),
      onPrimaryContainer = Color(0xFFC3E8FF),
      surface = Color(0xFF002B36),
      onSurface = Color(0xFF93A1A1),
      onSurfaceVariant = Color(0xFF839496),
      surfaceContainer = Color(0xFF073642),
      surfaceContainerLow = Color(0xFF00252E),
      surfaceContainerLowest = Color(0xFF001920),
      surfaceContainerHigh = Color(0xFF093E4A),
      surfaceContainerHighest = Color(0xFF0E4653),
      inversePrimary = Color(0xFF268BD2),
      inverseSurface = Color(0xFFFDF6E3),
      inverseOnSurface = Color(0xFF002B36),
      textEmphasisHigh = Color(0xFF93A1A1).copy(alpha = 0.95f),
      textEmphasisMed = Color(0xFF586E75).copy(alpha = 0.8f),
      backdrop = Color(0xFF002B36),
      bottomSheet = Color(0xFF000000),
      bottomSheetInverse = Color(0xFF000E14),
      bottomSheetBorder = Color(0xFF263033),
      tintedBackground = Color(0xFF002B36),
      tintedSurface = Color(0xFF073642),
      tintedForeground = Color(0xFF268BD2),
      tintedHighlight = Color(0xFF657B83),
      error = Color(0xFFDC322F),
    )
  } else {
    AppColorScheme(
      primary = Color(0xFF268BD2),
      onPrimary = Color.White,
      secondary = Color(0xFF2AA198),
      outline = Color(0xFF839496),
      outlineVariant = Color(0xFFD8D3C3),
      primaryContainer = Color(0xFFC3E8FF),
      onPrimaryContainer = Color(0xFF001E2D),
      surface = Color(0xFFFDF6E3),
      onSurface = Color(0xFF586E75),
      onSurfaceVariant = Color(0xFF657B83),
      surfaceContainer = Color(0xFFEEE8D5),
      surfaceContainerLow = Color(0xFFF5EFDC),
      surfaceContainerLowest = Color(0xFFFFFFFF),
      surfaceContainerHigh = Color(0xFFE8E2CF),
      surfaceContainerHighest = Color(0xFFE0DAC8),
      inversePrimary = Color(0xFF268BD2),
      inverseSurface = Color(0xFF073642),
      inverseOnSurface = Color(0xFFFDF6E3),
      textEmphasisHigh = Color(0xFF586E75).copy(alpha = 0.95f),
      textEmphasisMed = Color(0xFF93A1A1).copy(alpha = 0.8f),
      backdrop = Color(0xFFFDF6E3),
      bottomSheet = Color(0xFF000E14),
      bottomSheetInverse = Color(0xFF000000),
      bottomSheetBorder = Color(0xFF263033),
      tintedBackground = Color(0xFFFDF6E3),
      tintedSurface = Color(0xFFEEE8D5),
      tintedForeground = Color(0xFF268BD2),
      tintedHighlight = Color(0xFF839496),
      error = Color(0xFFDC322F),
    )
  }
}

internal fun midnightColorScheme(): AppColorScheme {
  return AppColorScheme(
    primary = Color(0xFFAEC6FF),
    onPrimary = Color(0xFF002E6C),
    secondary = Color(0xFFBFC6DC),
    outline = Color(0xFF8A9199),
    outlineVariant = Color(0xFF44474E),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = Color(0xFFD8E2FF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFC7C7C7),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainer = Color(0xFF101010),
    surfaceContainerLow = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerHigh = Color(0xFF1C1C1C),
    surfaceContainerHighest = Color(0xFF2B2B2B),
    inversePrimary = Color(0xFF2A5BB1),
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF000000),
    textEmphasisHigh = Color(0xFFE2E2E6).copy(alpha = 0.90f),
    textEmphasisMed = Color(0xFFC4C6D0).copy(alpha = 0.75f),
    backdrop = Color(0xFF080808),
    bottomSheet = Color(0xFF000000),
    bottomSheetInverse = Color(0xFF001026),
    bottomSheetBorder = Color(0xFF222222),
    tintedBackground = Color(0xFF000000),
    tintedSurface = Color(0xFF101010),
    tintedForeground = Color(0xFFAEC6FF),
    tintedHighlight = Color(0xFF8A9199),
    error = Color(0xFFFFB4AB),
  )
}

internal fun forestColorScheme(): AppColorScheme {
  return AppColorScheme(
    primary = Color(0xFF9CD67D),
    onPrimary = Color(0xFF0C3900),
    secondary = Color(0xFFBDCBB0),
    outline = Color(0xFF8C9388),
    outlineVariant = Color(0xFF424940),
    primaryContainer = Color(0xFF265017),
    onPrimaryContainer = Color(0xFFB7F396),
    surface = Color(0xFF111C14),
    onSurface = Color(0xFFE2E3DE),
    onSurfaceVariant = Color(0xFFC2C8BD),
    surfaceContainer = Color(0xFF18221A),
    surfaceContainerLow = Color(0xFF141F17),
    surfaceContainerLowest = Color(0xFF0E1610),
    surfaceContainerHigh = Color(0xFF232D25),
    surfaceContainerHighest = Color(0xFF2D382F),
    inversePrimary = Color(0xFF386A20),
    inverseSurface = Color(0xFFE2E3DE),
    inverseOnSurface = Color(0xFF111C14),
    textEmphasisHigh = Color(0xFFE2E3DE).copy(alpha = 0.95f),
    textEmphasisMed = Color(0xFFC2C8BD).copy(alpha = 0.8f),
    backdrop = Color(0xFF101912),
    bottomSheet = Color(0xFF0A120B),
    bottomSheetInverse = Color(0xFF000F02),
    bottomSheetBorder = Color(0xFF253026),
    tintedBackground = Color(0xFF111C14),
    tintedSurface = Color(0xFF18221A),
    tintedForeground = Color(0xFF9CD67D),
    tintedHighlight = Color(0xFF8C9388),
    error = Color(0xFFFFB4AB),
  )
}

internal fun slateColorScheme(): AppColorScheme {
  return AppColorScheme(
    primary = Color(0xFFA8C7FA),
    onPrimary = Color(0xFF003062),
    secondary = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF44474E),
    primaryContainer = Color(0xFF1A437C),
    onPrimaryContainer = Color(0xFFD7E2FF),
    surface = Color(0xFF1C1E23),
    onSurface = Color(0xFFE3E2E6),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainer = Color(0xFF23252A),
    surfaceContainerLow = Color(0xFF1E2025),
    surfaceContainerLowest = Color(0xFF17191D),
    surfaceContainerHigh = Color(0xFF2D2F35),
    surfaceContainerHighest = Color(0xFF383A40),
    inversePrimary = Color(0xFF3F6090),
    inverseSurface = Color(0xFFE3E2E6),
    inverseOnSurface = Color(0xFF1C1E23),
    textEmphasisHigh = Color(0xFFE3E2E6).copy(alpha = 0.95f),
    textEmphasisMed = Color(0xFFC4C6D0).copy(alpha = 0.8f),
    backdrop = Color(0xFF181A1F),
    bottomSheet = Color(0xFF121316),
    bottomSheetInverse = Color(0xFF0B111A),
    bottomSheetBorder = Color(0xFF2F3138),
    tintedBackground = Color(0xFF1C1E23),
    tintedSurface = Color(0xFF23252A),
    tintedForeground = Color(0xFFA8C7FA),
    tintedHighlight = Color(0xFF8C9199),
    error = Color(0xFFFFB4AB),
  )
}
