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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import twine.shared.generated.resources.Res
import twine.shared.generated.resources.golos_medium
import twine.shared.generated.resources.golos_regular

@Composable
internal fun AppTheme(
  appColorScheme: AppColorScheme = AppTheme.colorScheme,
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = darkColorScheme(),
    typography = typography(GolosFontFamily),
  ) {
    CompositionLocalProvider(
      LocalAppColorScheme provides appColorScheme,
      LocalRippleConfiguration provides
        RippleConfiguration(
          color = AppTheme.colorScheme.tintedForeground,
          rippleAlpha = DefaultRippleAlpha
        )
    ) {
      content()
    }
  }
}

internal object AppTheme {

  val colorScheme: AppColorScheme
    @Composable @ReadOnlyComposable get() = LocalAppColorScheme.current
}

private val GolosFontFamily: FontFamily
  @Composable
  get() =
    FontFamily(
      Font(Res.font.golos_regular, weight = FontWeight.Normal),
      Font(Res.font.golos_medium, weight = FontWeight.Medium),
    )

internal val DefaultRippleAlpha =
  RippleAlpha(
    pressedAlpha = 0.16f,
    focusedAlpha = 0.24f,
    draggedAlpha = 0.24f,
    hoveredAlpha = 0.08f
  )

internal val SYSTEM_SCRIM = Color.Black.copy(alpha = 0.8f)
