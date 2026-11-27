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
import androidx.compose.ui.graphics.toArgb
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
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

  private val cache = lruCache<Long, AppColorValues>(maxSize = 64)

  suspend fun animate(fromSeedColor: Color?, toSeedColor: Color?, progress: Float) {
    val seedColorsChanged = fromSeedColor != lastFromSeedColor || toSeedColor != lastToSeedColor
    val isSettled = fromSeedColor == toSeedColor || progress <= EPSILON || progress >= 1f - EPSILON
    val wasSettled =
      lastFromSeedColor == lastToSeedColor ||
        lastProgress <= EPSILON ||
        lastProgress >= 1f - EPSILON
    if (
      !seedColorsChanged &&
        isSettled == wasSettled &&
        (progress - lastProgress).absoluteValue < 0.02f
    ) {
      return
    }

    lastFromSeedColor = fromSeedColor
    lastToSeedColor = toSeedColor
    lastProgress = progress

    if (seedColorsChanged) {
      val defaultLightSeedColor = defaultLightValues.primary
      val defaultDarkSeedColor = defaultDarkValues.primary
      val fromSeed = fromSeedColor ?: defaultLightSeedColor
      val toSeed = toSeedColor ?: defaultDarkSeedColor

      val startLightKey = cacheKey(fromSeed, useDarkTheme = false)
      val startDarkKey = cacheKey(fromSeed, useDarkTheme = true)
      val endLightKey = cacheKey(toSeed, useDarkTheme = false)
      val endDarkKey = cacheKey(toSeed, useDarkTheme = true)

      val cachedStartLight = cache[startLightKey]
      val cachedStartDark = cache[startDarkKey]
      val cachedEndLight = cache[endLightKey]
      val cachedEndDark = cache[endDarkKey]

      if (
        cachedStartLight != null &&
          cachedStartDark != null &&
          cachedEndLight != null &&
          cachedEndDark != null
      ) {
        startLight = cachedStartLight
        startDark = cachedStartDark
        endLight = cachedEndLight
        endDark = cachedEndDark
      } else {
        withContext(Dispatchers.Default) {
          startLight =
            cachedStartLight
              ?: TwineDynamicColors.calculateColorScheme(
                  seedColor = fromSeed,
                  useDarkTheme = false,
                  scheme = scheme,
                )
                .also { cache.put(startLightKey, it) }

          startDark =
            cachedStartDark
              ?: TwineDynamicColors.calculateColorScheme(
                  seedColor = fromSeed,
                  useDarkTheme = true,
                  scheme = scheme,
                )
                .also { cache.put(startDarkKey, it) }

          endLight =
            cachedEndLight
              ?: TwineDynamicColors.calculateColorScheme(
                  seedColor = toSeed,
                  useDarkTheme = false,
                  scheme = scheme,
                )
                .also { cache.put(endLightKey, it) }

          endDark =
            cachedEndDark
              ?: TwineDynamicColors.calculateColorScheme(
                  seedColor = toSeed,
                  useDarkTheme = true,
                  scheme = scheme,
                )
                .also { cache.put(endDarkKey, it) }
        }
      }
    }

    val easedProgress = ease(progress)
    val interpolatedLight =
      when {
        easedProgress < EPSILON -> startLight
        easedProgress > 1f - EPSILON -> endLight
        else -> startLight.lerp(to = endLight, fraction = easedProgress)
      }
    val interpolatedDark =
      when {
        easedProgress < EPSILON -> startDark
        easedProgress > 1f - EPSILON -> endDark
        else -> startDark.lerp(to = endDark, fraction = easedProgress)
      }

    val isAnimating = !isSettled
    withContext(Dispatchers.Main.immediate) {
      lightAppColorScheme.updateFromWithAnimationState(
        values = interpolatedLight,
        isAnimating = isAnimating,
      )
      darkAppColorScheme.updateFromWithAnimationState(
        values = interpolatedDark,
        isAnimating = isAnimating,
      )
    }
  }

  private fun cacheKey(seedColor: Color, useDarkTheme: Boolean): Long {
    val argb = seedColor.toArgb().toLong() and 0xFFFFFFFFL
    return if (useDarkTheme) argb or (1L shl 32) else argb
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
    DynamicColorState(
      defaultLightValues = lightAppColorScheme(),
      defaultDarkValues = darkAppColorScheme(),
      scheme = TwineDynamicColors.Scheme.TonalSpot,
    )
  }
