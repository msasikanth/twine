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

import androidx.collection.lruCache
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberDynamicColorState(
  defaultLightAppColorScheme: AppColorValues,
  defaultDarkAppColorScheme: AppColorValues,
  scheme: TwineDynamicColors.Scheme = TwineDynamicColors.Scheme.TonalSpot,
): DynamicColorState {
  return remember(defaultLightAppColorScheme, defaultDarkAppColorScheme, scheme) {
    DynamicColorState(
      defaultLightValues = defaultLightAppColorScheme,
      defaultDarkValues = defaultDarkAppColorScheme,
      scheme = scheme,
    )
  }
}

@Stable
internal class DynamicColorState(
  private val defaultLightValues: AppColorValues,
  private val defaultDarkValues: AppColorValues,
  private val scheme: TwineDynamicColors.Scheme,
) {
  val lightAppColorScheme = AppColorScheme(defaultLightValues)
  val darkAppColorScheme = AppColorScheme(defaultDarkValues)

  private var lastFromSeedColor: Color? = null
  private var lastToSeedColor: Color? = null
  private var lastProgress: Float = 0f

  private var startLight: AppColorValues = defaultLightValues
  private var startDark: AppColorValues = defaultDarkValues
  private var endLight: AppColorValues = defaultLightValues
  private var endDark: AppColorValues = defaultDarkValues

  private val cache = lruCache<String, AppColorValues>(maxSize = 10)

  suspend fun animate(fromSeedColor: Color?, toSeedColor: Color?, progress: Float) {
    val seedColorsChanged = fromSeedColor != lastFromSeedColor || toSeedColor != lastToSeedColor
    if (!seedColorsChanged && (progress - lastProgress).absoluteValue < 0.02f) {
      return
    }

    lastFromSeedColor = fromSeedColor
    lastToSeedColor = toSeedColor
    lastProgress = progress

    if (seedColorsChanged) {
      withContext(Dispatchers.Default) {
        val defaultLightSeedColor = defaultLightValues.primary
        val defaultDarkSeedColor = defaultDarkValues.primary

        startLight =
          cache["light_$fromSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = fromSeedColor ?: defaultLightSeedColor,
                useDarkTheme = false,
                scheme = scheme,
              )
              .also { cache.put("light_$fromSeedColor", it) }

        startDark =
          cache["dark_$fromSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = fromSeedColor ?: defaultLightSeedColor,
                useDarkTheme = true,
                scheme = scheme,
              )
              .also { cache.put("dark_$fromSeedColor", it) }

        endLight =
          cache["light_$toSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = toSeedColor ?: defaultDarkSeedColor,
                useDarkTheme = false,
                scheme = scheme,
              )
              .also { cache.put("light_$toSeedColor", it) }

        endDark =
          cache["dark_$toSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = toSeedColor ?: defaultDarkSeedColor,
                useDarkTheme = true,
                scheme = scheme,
              )
              .also { cache.put("dark_$toSeedColor", it) }
      }
    }

    val (interpolatedLight, interpolatedDark) =
      withContext(Dispatchers.Default) {
        val normalizedProgress =
          ease(
            if (progress < -EPSILON) {
              progress.absoluteValue.inverse()
            } else {
              progress
            }
          )

        val light =
          when {
            normalizedProgress < EPSILON -> startLight
            normalizedProgress > 1f - EPSILON -> endLight
            else -> startLight.lerp(to = endLight, fraction = normalizedProgress)
          }

        val dark =
          when {
            normalizedProgress < EPSILON -> startDark
            normalizedProgress > 1f - EPSILON -> endDark
            else -> startDark.lerp(to = endDark, fraction = normalizedProgress)
          }

        light to dark
      }

    withContext(Dispatchers.Main.immediate) {
      lightAppColorScheme.updateFrom(interpolatedLight)
      darkAppColorScheme.updateFrom(interpolatedDark)
    }
  }

  suspend fun refresh() {
    animate(lastFromSeedColor, lastToSeedColor, lastProgress)
  }

  fun reset() {
    lastFromSeedColor = null
    lastToSeedColor = null
    lastProgress = 0f
    startLight = defaultLightValues
    startDark = defaultDarkValues
    endLight = defaultLightValues
    endDark = defaultDarkValues
    lightAppColorScheme.updateFrom(defaultLightValues)
    darkAppColorScheme.updateFrom(defaultDarkValues)
  }

  private fun ease(progress: Float): Float {
    return FastOutSlowInEasing.transform(progress)
  }
}

internal val LocalDynamicColorState =
  staticCompositionLocalOf<DynamicColorState> {
    throw NullPointerException("Please provide a dynamic color state")
  }
