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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun rememberDynamicColorState(
  defaultLightAppColorScheme: AppColorScheme,
  defaultDarkAppColorScheme: AppColorScheme,
  useTonalSpotScheme: Boolean = true,
): DynamicColorState {
  return remember {
    DynamicColorState(
      defaultLightAppColorScheme = defaultLightAppColorScheme,
      defaultDarkAppColorScheme = defaultDarkAppColorScheme,
      useTonalSpotScheme = useTonalSpotScheme,
    )
  }
}

@Stable
internal class DynamicColorState(
  private val defaultLightAppColorScheme: AppColorScheme,
  private val defaultDarkAppColorScheme: AppColorScheme,
  private val useTonalSpotScheme: Boolean,
) {
  val lightAppColorScheme = defaultLightAppColorScheme.copy()
  val darkAppColorScheme = defaultDarkAppColorScheme.copy()

  private var lastFromSeedColor: Color? = null
  private var lastToSeedColor: Color? = null
  private var lastProgress: Float = 0f

  private var startLight: AppColorScheme = defaultLightAppColorScheme
  private var startDark: AppColorScheme = defaultDarkAppColorScheme
  private var endLight: AppColorScheme = defaultLightAppColorScheme
  private var endDark: AppColorScheme = defaultDarkAppColorScheme

  private val cache = lruCache<String, AppColorScheme>(maxSize = 10)

  suspend fun animate(fromSeedColor: Color?, toSeedColor: Color?, progress: Float) {
    val seedColorsChanged = fromSeedColor != lastFromSeedColor || toSeedColor != lastToSeedColor

    lastFromSeedColor = fromSeedColor
    lastToSeedColor = toSeedColor
    lastProgress = progress

    if (seedColorsChanged) {
      withContext(Dispatchers.Default) {
        val defaultLightSeedColor = defaultLightAppColorScheme.primary
        val defaultDarkSeedColor = defaultDarkAppColorScheme.primary

        startLight =
          cache["light_$fromSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = fromSeedColor ?: defaultLightSeedColor,
                useDarkTheme = false,
                useTonalSpotScheme = useTonalSpotScheme,
                defaultColorScheme = defaultLightAppColorScheme,
              )
              .also { cache.put("light_$fromSeedColor", it) }

        startDark =
          cache["dark_$fromSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = fromSeedColor ?: defaultLightSeedColor,
                useDarkTheme = true,
                useTonalSpotScheme = useTonalSpotScheme,
                defaultColorScheme = defaultDarkAppColorScheme,
              )
              .also { cache.put("dark_$fromSeedColor", it) }

        endLight =
          cache["light_$toSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = toSeedColor ?: defaultDarkSeedColor,
                useDarkTheme = false,
                useTonalSpotScheme = useTonalSpotScheme,
                defaultColorScheme = defaultLightAppColorScheme,
              )
              .also { cache.put("light_$toSeedColor", it) }

        endDark =
          cache["dark_$toSeedColor"]
            ?: TwineDynamicColors.calculateColorScheme(
                seedColor = toSeedColor ?: defaultDarkSeedColor,
                useDarkTheme = true,
                useTonalSpotScheme = useTonalSpotScheme,
                defaultColorScheme = defaultDarkAppColorScheme,
              )
              .also { cache.put("dark_$toSeedColor", it) }
      }
    }

    withContext(Dispatchers.Default) {
      val normalizedProgress =
        ease(
          if (progress < -EPSILON) {
            progress.absoluteValue.inverse()
          } else {
            progress
          }
        )

      if (normalizedProgress < EPSILON) {
        lightAppColorScheme.updateFrom(startLight)
        darkAppColorScheme.updateFrom(startDark)
      } else if (normalizedProgress > 1f - EPSILON) {
        lightAppColorScheme.updateFrom(endLight)
        darkAppColorScheme.updateFrom(endDark)
      } else {
        lightAppColorScheme.updateFrom(
          startLight.lerp(to = endLight, fraction = normalizedProgress)
        )
        darkAppColorScheme.updateFrom(startDark.lerp(to = endDark, fraction = normalizedProgress))
      }
    }
  }

  suspend fun refresh() {
    animate(lastFromSeedColor, lastToSeedColor, lastProgress)
  }

  fun reset() {
    lastFromSeedColor = null
    lastToSeedColor = null
    lastProgress = 0f
    startLight = defaultLightAppColorScheme
    startDark = defaultDarkAppColorScheme
    endLight = defaultLightAppColorScheme
    endDark = defaultDarkAppColorScheme
    lightAppColorScheme.updateFrom(defaultLightAppColorScheme)
    darkAppColorScheme.updateFrom(defaultDarkAppColorScheme)
  }

  private fun ease(progress: Float): Float {
    return FastOutSlowInEasing.transform(progress)
  }
}

internal val LocalDynamicColorState =
  staticCompositionLocalOf<DynamicColorState> {
    throw NullPointerException("Please provide a dynamic color state")
  }
