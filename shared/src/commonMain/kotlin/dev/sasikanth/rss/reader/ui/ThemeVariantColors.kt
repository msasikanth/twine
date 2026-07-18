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

import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant

internal fun ThemeVariant.getOverriddenColorScheme(isDark: Boolean): AppColorScheme? {
  return when (this) {
    ThemeVariant.Dynamic -> null
    ThemeVariant.SystemDynamic -> null // Handled separately in App.kt via composable function
    ThemeVariant.Solarized -> solarizedColorScheme(isDark)
    ThemeVariant.Forest -> forestColorScheme(isDark)
    ThemeVariant.Amber -> amberColorScheme(isDark)
    ThemeVariant.Coral -> coralColorScheme(isDark)
    ThemeVariant.Raspberry -> raspberryColorScheme(isDark)
    ThemeVariant.Skyline -> skylineColorScheme(isDark)
    ThemeVariant.Parchment -> parchmentColorScheme(isDark)
    ThemeVariant.Sepia -> sepiaColorScheme(isDark)
    ThemeVariant.Slate -> slateColorScheme(isDark)
    ThemeVariant.Lavender -> lavenderColorScheme(isDark)
  }
}

internal fun parchmentColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = if (isDark) Color.White else Color.Black,
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Monochrome,
    )
  )
}

internal fun solarizedColorScheme(isDark: Boolean): AppColorScheme {
  return if (isDark) {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFFF8916A),
        onPrimary = Color(0xFF370F02),
        inversePrimary = Color(0xFF994627),
        secondary = Color(0xFFBCC9C8),
        onSurface = Color(0xFFF2FBFF),
        onSurfaceVariant = Color(0xFFA9CBD9),
        outline = Color(0xFF7495A2),
        outlineVariant = Color(0xFF2A4B56),
        primaryContainer = Color(0xFF994627),
        onPrimaryContainer = Color(0xFFFEEDE8),
        surface = Color(0xFF021E27),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF042731),
        surfaceContainer = Color(0xFF0C2F3B),
        surfaceContainerHigh = Color(0xFF163844),
        surfaceContainerHighest = Color(0xFF20414D),
        inverseSurface = Color(0xFFF2FBFF),
        inverseOnSurface = Color(0xFF021E27),
        backdrop = Color(0xFF021E27),
        bottomSheet = Color.Black,
        bottomSheetInverse = Color(0xFF042731),
        bottomSheetBorder = Color(0xFF2B3737),
        error = Color(0xFFFFB4AB),
      )
    )
  } else {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFF994627),
        onPrimary = Color.White,
        inversePrimary = Color(0xFFF8916A),
        secondary = Color(0xFF3D4949),
        onSurface = Color(0xFF1E1C11),
        onSurfaceVariant = Color(0xFF625E51),
        outline = Color(0xFF969181),
        outlineVariant = Color(0xFFCCC6B5),
        primaryContainer = Color(0xFFFCB79E),
        onPrimaryContainer = Color(0xFF370F02),
        surface = Color(0xFFFFF9EC),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFF9F3E1),
        surfaceContainer = Color(0xFFF4EEDC),
        surfaceContainerHigh = Color(0xFFEEE8D6),
        surfaceContainerHighest = Color(0xFFE8E2D1),
        inverseSurface = Color(0xFF333025),
        inverseOnSurface = Color(0xFFFFF9EC),
        backdrop = Color(0xFFF4EEDC),
        bottomSheet = Color(0xFF042731),
        bottomSheetInverse = Color.Black,
        bottomSheetBorder = Color(0xFF2B3737),
        error = Color(0xFFBA1A1A),
      )
    )
  }
}

internal fun forestColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFF73D995),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}

internal fun amberColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFFF5B83D),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}

internal fun coralColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFFFF8C61),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}

internal fun raspberryColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFFE87DC8),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}

internal fun skylineColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFF45ABF2),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}

internal fun sepiaColorScheme(isDark: Boolean): AppColorScheme {
  return if (isDark) {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFFDCB68A),
        onPrimary = Color(0xFF3E2A15),
        inversePrimary = Color(0xFF7A5330),
        secondary = Color(0xFFCBB89C),
        onSurface = Color(0xFFF1E4D2),
        onSurfaceVariant = Color(0xFFD3C0A4),
        outline = Color(0xFF9C8A6E),
        outlineVariant = Color(0xFF4C3E28),
        primaryContainer = Color(0xFF7A5330),
        onPrimaryContainer = Color(0xFFF7E6CE),
        surface = Color(0xFF241A0F),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF2C2013),
        surfaceContainer = Color(0xFF352718),
        surfaceContainerHigh = Color(0xFF40301D),
        surfaceContainerHighest = Color(0xFF4C3A24),
        inverseSurface = Color(0xFFF1E4D2),
        inverseOnSurface = Color(0xFF241A0F),
        backdrop = Color(0xFF241A0F),
        bottomSheet = Color.Black,
        bottomSheetInverse = Color(0xFF2C2013),
        bottomSheetBorder = Color(0xFF4C3E28),
        error = Color(0xFFFFB4AB),
      )
    )
  } else {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFF5C3A21),
        onPrimary = Color.White,
        inversePrimary = Color(0xFFDCB68A),
        secondary = Color(0xFF6B5C46),
        onSurface = Color(0xFF3B2A1E),
        onSurfaceVariant = Color(0xFF6E5C44),
        outline = Color(0xFFA08D6E),
        outlineVariant = Color(0xFFD9C6A6),
        primaryContainer = Color(0xFFE9CCA3),
        onPrimaryContainer = Color(0xFF2E1B08),
        surface = Color(0xFFF4E8D8),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFEFE1CB),
        surfaceContainer = Color(0xFFE9DABF),
        surfaceContainerHigh = Color(0xFFE2D2B4),
        surfaceContainerHighest = Color(0xFFDCC9A8),
        inverseSurface = Color(0xFF362A1C),
        inverseOnSurface = Color(0xFFF4E8D8),
        backdrop = Color(0xFFE9DABF),
        bottomSheet = Color(0xFF2C2013),
        bottomSheetInverse = Color.Black,
        bottomSheetBorder = Color(0xFF4C3E28),
        error = Color(0xFFBA1A1A),
      )
    )
  }
}

internal fun slateColorScheme(isDark: Boolean): AppColorScheme {
  return if (isDark) {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFFB7C6D3),
        onPrimary = Color(0xFF213540),
        inversePrimary = Color(0xFF546878),
        secondary = Color(0xFFA9B6BF),
        onSurface = Color(0xFFE4EBEF),
        onSurfaceVariant = Color(0xFFC0CCD4),
        outline = Color(0xFF8A99A3),
        outlineVariant = Color(0xFF39454D),
        primaryContainer = Color(0xFF3E5361),
        onPrimaryContainer = Color(0xFFD6E4EC),
        surface = Color(0xFF161C20),
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color(0xFF1B2226),
        surfaceContainer = Color(0xFF212A2F),
        surfaceContainerHigh = Color(0xFF2C363C),
        surfaceContainerHighest = Color(0xFF374148),
        inverseSurface = Color(0xFFE4EBEF),
        inverseOnSurface = Color(0xFF161C20),
        backdrop = Color(0xFF161C20),
        bottomSheet = Color.Black,
        bottomSheetInverse = Color(0xFF1B2226),
        bottomSheetBorder = Color(0xFF39454D),
        error = Color(0xFFFFB4AB),
      )
    )
  } else {
    AppColorScheme(
      AppColorValues(
        primary = Color(0xFF3E5866),
        onPrimary = Color.White,
        inversePrimary = Color(0xFFB7C6D3),
        secondary = Color(0xFF54626B),
        onSurface = Color(0xFF1D2529),
        onSurfaceVariant = Color(0xFF454F55),
        outline = Color(0xFF757F86),
        outlineVariant = Color(0xFFC0CBD1),
        primaryContainer = Color(0xFFCADCE6),
        onPrimaryContainer = Color(0xFF102630),
        surface = Color(0xFFEEF1F3),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = Color(0xFFE7ECEF),
        surfaceContainer = Color(0xFFDFE6E9),
        surfaceContainerHigh = Color(0xFFD8E0E3),
        surfaceContainerHighest = Color(0xFFD1DADD),
        inverseSurface = Color(0xFF2E373C),
        inverseOnSurface = Color(0xFFEEF1F3),
        backdrop = Color(0xFFDFE6E9),
        bottomSheet = Color(0xFF1B2226),
        bottomSheetInverse = Color.Black,
        bottomSheetBorder = Color(0xFF39454D),
        error = Color(0xFFBA1A1A),
      )
    )
  }
}

internal fun lavenderColorScheme(isDark: Boolean): AppColorScheme {
  return AppColorScheme(
    TwineDynamicColors.calculateColorScheme(
      seedColor = Color(0xFF9C89E8),
      useDarkTheme = isDark,
      scheme = TwineDynamicColors.Scheme.Vibrant,
    )
  )
}
