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

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass

@Composable
internal fun AppTheme(
  useDarkTheme: Boolean = false,
  fontFamily: FontFamily = OutfitFontFamily,
  fontScalingFactor: Float = 1f,
  lineHeightScalingFactor: Float = 1f,
  overriddenColorScheme: AppColorScheme? = null,
  content: @Composable () -> Unit,
) {
  val useAmoled = LocalAmoledSetting.current
  val windowSizeClass = LocalWindowSizeClass.current
  val baseFontScaleFactor = windowSizeClass.calculateFontScale()

  CompositionLocalProvider(
    LocalIsDarkTheme provides useDarkTheme,
    LocalAppFontScaleFactor provides baseFontScaleFactor,
  ) {
    val typography =
      remember(fontFamily, fontScalingFactor, lineHeightScalingFactor, baseFontScaleFactor) {
        typography(
          fontFamily = fontFamily,
          fontScalingFactor = fontScalingFactor * baseFontScaleFactor,
          lineHeightScalingFactor = lineHeightScalingFactor * baseFontScaleFactor,
        )
      }

    MaterialTheme(
      colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme(),
      typography = typography,
    ) {
      val dynamicColorState = LocalDynamicColorState.current
      val sourceColorScheme =
        overriddenColorScheme
          ?: if (useDarkTheme) dynamicColorState.darkAppColorScheme
          else dynamicColorState.lightAppColorScheme

      // We read a property from the source color scheme to ensure that AppTheme recomposes
      // whenever the dynamic colors change. This allows us to update the stable color scheme
      // instance that is provided to the rest of the app.
      val sourceColorValues = sourceColorScheme.toValues()

      val colorScheme =
        remember(useDarkTheme, overriddenColorScheme) {
          AppColorScheme(sourceColorValues).apply {
            updateFrom(sourceColorValues, amoled = useDarkTheme && useAmoled)
          }
        }

      SideEffect { colorScheme.updateFrom(sourceColorValues, amoled = useDarkTheme && useAmoled) }

      val onSurface = colorScheme.onSurface
      val localTranslucentStyles =
        TranslucentStyles(
          default =
            TranslucentStyle(
              background = onSurface.copy(alpha = 0.08f),
              outline = onSurface.copy(alpha = 0.16f),
              foreground = onSurface,
            ),
          prominent =
            TranslucentStyle(
              background = onSurface.copy(alpha = 0.16f),
              outline = onSurface.copy(alpha = 0.16f),
              foreground = onSurface,
            ),
        )

      CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalRippleConfiguration provides
          RippleConfiguration(color = colorScheme.secondary, rippleAlpha = DefaultRippleAlpha),
        LocalTranslucentStyles provides localTranslucentStyles,
      ) {
        content()
      }
    }
  }
}

@Composable
fun WindowSizeClass.calculateFontScale(): Float {
  return when {
    // Large/Extra Large
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> 1.2f

    // Expanded
    isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 1.15f

    else -> 1.0f
  }
}

internal object AppTheme {

  val colorScheme: AppColorScheme
    @Composable @ReadOnlyComposable get() = LocalAppColorScheme.current

  val isDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalIsDarkTheme.current
}

internal val DefaultRippleAlpha =
  RippleAlpha(
    pressedAlpha = 0.16f,
    focusedAlpha = 0.24f,
    draggedAlpha = 0.24f,
    hoveredAlpha = 0.08f,
  )

internal val SYSTEM_SCRIM = Color.Black.copy(alpha = 0.8f)

internal val LocalIsDarkTheme = staticCompositionLocalOf { false }

internal val LocalAppFontScaleFactor = staticCompositionLocalOf { 1f }
