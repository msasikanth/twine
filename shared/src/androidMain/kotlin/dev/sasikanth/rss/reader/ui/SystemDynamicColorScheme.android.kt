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

import android.graphics.Color.toArgb
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.getColor
import com.materialkolor.scheme.SchemeTonalSpot

internal actual val isSystemDynamicColorSupported: Boolean
  get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

@Composable
internal actual fun systemDynamicColorScheme(isDark: Boolean): AppColorScheme {
  val context = LocalContext.current

  // Dynamic colors are available on Android 12+ (API 31+)
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val colorScheme =
      remember(context, isDark) {
        if (isDark) {
          dynamicDarkColorScheme(context)
        } else {
          dynamicLightColorScheme(context)
        }
      }
    val sourceColorHct = remember(colorScheme) { Hct.fromInt(colorScheme.primary.toArgb()) }
    val dynamicColorScheme =
      remember(colorScheme) {
        SchemeTonalSpot(sourceColorHct = sourceColorHct, isDark = isDark, contrastLevel = 0.0)
      }

    val appColorValues =
      remember(colorScheme, dynamicColorScheme) {
        AppColorValues(
          primary = colorScheme.primary,
          onPrimary = colorScheme.onPrimary,
          inversePrimary = colorScheme.inversePrimary,
          secondary = colorScheme.secondary,
          onSurface = colorScheme.onSurface,
          onSurfaceVariant = colorScheme.onSurfaceVariant,
          outline = colorScheme.outline,
          outlineVariant = colorScheme.outlineVariant,
          primaryContainer = colorScheme.primaryContainer,
          onPrimaryContainer = colorScheme.onPrimaryContainer,
          surface = colorScheme.surface,
          surfaceContainerLowest = colorScheme.surfaceContainerLowest,
          surfaceContainerLow = colorScheme.surfaceContainerLow,
          surfaceContainer = colorScheme.surfaceContainer,
          surfaceContainerHigh = colorScheme.surfaceContainerHigh,
          surfaceContainerHighest = colorScheme.surfaceContainerHighest,
          inverseSurface = colorScheme.inverseSurface,
          inverseOnSurface = colorScheme.inverseOnSurface,
          backdrop = TwineDynamicColors.backdrop.getColor(dynamicColorScheme),
          bottomSheet = TwineDynamicColors.bottomSheet.getColor(dynamicColorScheme),
          bottomSheetInverse = TwineDynamicColors.bottomSheetInverse.getColor(dynamicColorScheme),
          bottomSheetBorder = TwineDynamicColors.bottomSheetBorder.getColor(dynamicColorScheme),
          error = colorScheme.error,
        )
      }

    remember(appColorValues) { AppColorScheme(appColorValues) }
  } else {
    // Fallback for older Android versions - use Forest theme as default
    val appColorValues =
      remember(isDark) {
        TwineDynamicColors.calculateColorScheme(
          seedColor = Color(0xFF73D995),
          useDarkTheme = isDark,
          scheme = TwineDynamicColors.Scheme.Vibrant,
        )
      }

    remember(appColorValues) { AppColorScheme(appColorValues) }
  }
}
