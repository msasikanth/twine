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

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.utils.LocalAmoledSetting

@Composable
internal fun AppTheme(
  useDarkTheme: Boolean = false,
  typography: Typography = typography(RethinkSansFontFamily),
  content: @Composable () -> Unit
) {
  val useAmoled = LocalAmoledSetting.current

  MaterialTheme(
    colorScheme = if (useDarkTheme) darkColorScheme() else lightColorScheme(),
    typography = typography,
  ) {
    val dynamicColorState = LocalDynamicColorState.current
    val colorScheme =
      if (useDarkTheme) {
        if (useAmoled) dynamicColorState.darkAppColorScheme.amoled()
        else dynamicColorState.darkAppColorScheme
      } else {
        dynamicColorState.lightAppColorScheme
      }

    val secondary = colorScheme.secondary
    val onSurface = colorScheme.onSurface
    val localTranslucentStyles =
      TranslucentStyles(
        default =
          TranslucentStyle(
            background = secondary.copy(alpha = 0.08f),
            outline = secondary.copy(alpha = 0.16f),
            foreground = onSurface,
          ),
        prominent =
          TranslucentStyle(
            background = secondary.copy(alpha = 0.16f),
            outline = secondary.copy(alpha = 0.16f),
            foreground = onSurface,
          )
      )

    CompositionLocalProvider(
      LocalAppColorScheme provides colorScheme,
      LocalRippleConfiguration provides
        RippleConfiguration(
          color = colorScheme.secondary,
          rippleAlpha = DefaultRippleAlpha,
        ),
      LocalTranslucentStyles provides localTranslucentStyles
    ) {
      content()
    }
  }
}

internal object AppTheme {

  val colorScheme: AppColorScheme
    @Composable @ReadOnlyComposable get() = LocalAppColorScheme.current
}

internal val DefaultRippleAlpha =
  RippleAlpha(
    pressedAlpha = 0.16f,
    focusedAlpha = 0.24f,
    draggedAlpha = 0.24f,
    hoveredAlpha = 0.08f
  )

internal val SYSTEM_SCRIM = Color.Black.copy(alpha = 0.8f)
