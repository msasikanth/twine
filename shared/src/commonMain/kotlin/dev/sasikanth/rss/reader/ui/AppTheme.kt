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
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting
import dev.sasikanth.rss.reader.utils.LocalRelativeDateFormatter
import dev.sasikanth.rss.reader.utils.rememberRelativeDateFormatter

private val DarkMaterialColorScheme = darkColorScheme()
private val LightMaterialColorScheme = lightColorScheme()

@Composable
internal fun AppTheme(
  useDarkTheme: Boolean = false,
  typography: Typography = DefaultTypography,
  overriddenColorScheme: AppColorScheme? = null,
  content: @Composable () -> Unit,
) {
  val useAmoled = LocalAmoledSetting.current
  val isAmoled = useDarkTheme && useAmoled

  CompositionLocalProvider(LocalIsDarkTheme provides useDarkTheme) {
    MaterialTheme(
      colorScheme = if (useDarkTheme) DarkMaterialColorScheme else LightMaterialColorScheme,
      typography = typography,
    ) {
      val dynamicColorState = LocalDynamicColorState.current
      val sourceColorScheme =
        overriddenColorScheme
          ?: if (useDarkTheme) dynamicColorState.darkAppColorScheme
          else dynamicColorState.lightAppColorScheme

      val colorScheme =
        if (!isAmoled) {
          sourceColorScheme
        } else {
          val amoledColorScheme =
            remember(sourceColorScheme) {
              val initialValues = sourceColorScheme.peekValues()
              AppColorScheme(initialValues).apply {
                updateFromWithAnimationState(
                  values = initialValues,
                  amoled = true,
                  isAnimating = sourceColorScheme.isAnimating,
                )
              }
            }

          SideEffect {
            amoledColorScheme.updateFromWithAnimationState(
              values = sourceColorScheme.peekValues(),
              amoled = true,
              isAnimating = sourceColorScheme.isAnimating,
            )
          }

          DisposableEffect(sourceColorScheme, amoledColorScheme) {
            val disposeObserver =
              sourceColorScheme.observeValues { values, isAnimating ->
                amoledColorScheme.updateFromWithAnimationState(
                  values = values,
                  amoled = true,
                  isAnimating = isAnimating,
                )
              }
            onDispose { disposeObserver() }
          }

          amoledColorScheme
        }

      val localTranslucentStyles =
        remember(colorScheme) {
          TranslucentStyles(
            default =
              TranslucentStyle(
                backgroundGetter = { colorScheme.onSurface.copy(alpha = 0.08f) },
                foregroundGetter = { colorScheme.onSurface },
                outlineGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
              ),
            prominent =
              TranslucentStyle(
                backgroundGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
                foregroundGetter = { colorScheme.onSurface },
                outlineGetter = { colorScheme.onSurface.copy(alpha = 0.16f) },
              ),
          )
        }

      @Suppress("DEPRECATION")
      val rippleConfiguration =
        remember(colorScheme.settledSecondary) {
          RippleConfiguration(
            color = colorScheme.settledSecondary,
            rippleAlpha = DefaultRippleAlpha,
          )
        }

      CompositionLocalProvider(
        LocalAppColorScheme provides colorScheme,
        LocalRippleConfiguration provides rippleConfiguration,
        LocalTranslucentStyles provides localTranslucentStyles,
        LocalRelativeDateFormatter provides rememberRelativeDateFormatter(),
      ) {
        content()
      }
    }
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

internal val LocalIsDarkTheme = staticCompositionLocalOf { false }
