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
import dev.sasikanth.rss.reader.data.repository.ReaderColorScheme
import dev.sasikanth.rss.reader.ui.AppColorScheme
import dev.sasikanth.rss.reader.ui.AppColorValues
import dev.sasikanth.rss.reader.ui.TwineDynamicColors

internal fun ReaderColorScheme.getOverriddenColorScheme(isDark: Boolean): AppColorScheme? {
  return when (this) {
    ReaderColorScheme.Dynamic -> null
    ReaderColorScheme.Solarized -> solarizedColorScheme(isDark)
    ReaderColorScheme.Forest -> forestColorScheme(isDark)
    ReaderColorScheme.Amber -> amberColorScheme(isDark)
    ReaderColorScheme.Raspberry -> raspberryColorScheme(isDark)
    ReaderColorScheme.Skyline -> skylineColorScheme(isDark)
  }
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
