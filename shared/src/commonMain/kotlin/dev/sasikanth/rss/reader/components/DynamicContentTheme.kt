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
package dev.sasikanth.rss.reader.components

import androidx.collection.LruCache
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import dev.sasikanth.material.color.utilities.dynamiccolor.DynamicColor
import dev.sasikanth.material.color.utilities.dynamiccolor.MaterialDynamicColors
import dev.sasikanth.material.color.utilities.dynamiccolor.ToneDeltaConstraint
import dev.sasikanth.material.color.utilities.dynamiccolor.TonePolarity
import dev.sasikanth.material.color.utilities.hct.Hct
import dev.sasikanth.material.color.utilities.quantize.QuantizerCelebi
import dev.sasikanth.material.color.utilities.scheme.DynamicScheme
import dev.sasikanth.material.color.utilities.scheme.SchemeContent
import dev.sasikanth.material.color.utilities.score.Score
import dev.sasikanth.material.color.utilities.utils.StringUtils
import dev.sasikanth.rss.reader.ui.AppTheme
import io.github.aakira.napier.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TINTED_BACKGROUND = "tinted_background"
private const val TINTED_SURFACE = "tinted_surface"
private const val TINTED_FOREGROUND = "tinted_foreground"
private const val SURFACE_CONTAINER = "surface_container"
private const val SURFACE_CONTAINER_LOWEST = "surface_container_lowest"

@Composable
internal fun DynamicContentTheme(
  dynamicColorState: DynamicColorState = rememberDynamicColorState(),
  content: @Composable () -> Unit
) {
  val tintedBackground by
    animateColorAsState(dynamicColorState.tintedBackground, spring(stiffness = Spring.StiffnessLow))
  val tintedSurface by
    animateColorAsState(dynamicColorState.tintedSurface, spring(stiffness = Spring.StiffnessLow))
  val tintedForeground by
    animateColorAsState(dynamicColorState.tintedForeground, spring(stiffness = Spring.StiffnessLow))
  val surfaceContainer by
    animateColorAsState(dynamicColorState.surfaceContainer, spring(stiffness = Spring.StiffnessLow))
  val surfaceContainerLowest by
    animateColorAsState(
      dynamicColorState.surfaceContainerLowest,
      spring(stiffness = Spring.StiffnessLow)
    )

  val colorScheme =
    AppTheme.colorScheme.copy(
      tintedBackground = tintedBackground,
      tintedSurface = tintedSurface,
      tintedForeground = tintedForeground,
      surfaceContainer = surfaceContainer,
      surfaceContainerLowest = surfaceContainerLowest
    )

  AppTheme(appColorScheme = colorScheme, content = content)
}

@Composable
internal fun rememberDynamicColorState(
  defaultTintedBackground: Color = AppTheme.colorScheme.tintedBackground,
  defaultTintedSurface: Color = AppTheme.colorScheme.tintedSurface,
  defaultTintedForeground: Color = AppTheme.colorScheme.tintedForeground,
  defaultSurfaceContainer: Color = AppTheme.colorScheme.surfaceContainer,
  defaultSurfaceContainerLowest: Color = AppTheme.colorScheme.surfaceContainerLowest,
  cacheSize: Int = 12
): DynamicColorState = remember {
  DynamicColorState(
    defaultTintedBackground,
    defaultTintedSurface,
    defaultTintedForeground,
    defaultSurfaceContainer,
    defaultSurfaceContainerLowest,
    cacheSize
  )
}

/**
 * A class which stores and caches the result of any calculated colors from images.
 *
 * @param cacheSize The size of the [LruCache] used to store recent results. Pass `0` to disable the
 *   cache.
 */
@Stable
class DynamicColorState(
  private val defaultTintedBackground: Color,
  private val defaultTintedSurface: Color,
  private val defaultTintedForeground: Color,
  private val defaultSurfaceContainer: Color,
  private val defaultSurfaceContainerLowest: Color,
  cacheSize: Int
) {
  var tintedBackground by mutableStateOf(defaultTintedBackground)
    private set

  var tintedSurface by mutableStateOf(defaultTintedSurface)
    private set

  var tintedForeground by mutableStateOf(defaultTintedForeground)
    private set

  var surfaceContainer by mutableStateOf(defaultSurfaceContainer)
    private set

  var surfaceContainerLowest by mutableStateOf(defaultSurfaceContainerLowest)
    private set

  private val cache =
    when {
      cacheSize > 0 -> LruCache<String, DynamicColors>(cacheSize)
      else -> null
    }

  suspend fun updateColorsFromImageUrl(url: String) {
    val result = fetchDynamicColors(url)
    tintedBackground = result?.tintedBackground ?: defaultTintedBackground
    tintedSurface = result?.tintedSurface ?: defaultTintedSurface
    tintedForeground = result?.tintedForeground ?: defaultTintedForeground
    surfaceContainer = result?.surfaceContainer ?: defaultSurfaceContainer
    surfaceContainerLowest = result?.surfaceContainerLowest ?: defaultSurfaceContainerLowest
  }

  fun reset() {
    tintedBackground = defaultTintedBackground
    tintedSurface = defaultTintedSurface
    tintedForeground = defaultTintedForeground
    surfaceContainer = defaultSurfaceContainer
    surfaceContainerLowest = surfaceContainerLowest
  }

  private suspend fun fetchDynamicColors(url: String): DynamicColors? {
    val cached = cache?.get(url)
    if (cached != null) {
      // If we already have the result cached, return early now...
      return cached
    }

    val image = fetchImageBitmapFromUrl(url)
    return if (image != null) {
      extractColorsFromImage(image)
        .let { colorsMap ->
          return@let if (colorsMap.isNotEmpty()) {
            DynamicColors(
              tintedBackground = Color(colorsMap[TINTED_BACKGROUND]!!),
              tintedSurface = Color(colorsMap[TINTED_SURFACE]!!),
              tintedForeground = Color(colorsMap[TINTED_FOREGROUND]!!),
              surfaceContainer = Color(colorsMap[SURFACE_CONTAINER]!!),
              surfaceContainerLowest = Color(colorsMap[SURFACE_CONTAINER_LOWEST]!!)
            )
          } else {
            null
          }
        }
        ?.also { result -> cache?.put(url, result) }
    } else null
  }

  private suspend fun extractColorsFromImage(image: ImageBitmap): Map<String, Int> {
    val colorMap: MutableMap<String, Int> = mutableMapOf()
    withContext(Dispatchers.Default) {
      val bitmapPixels = IntArray(image.width * image.height)
      image.readPixels(buffer = bitmapPixels)

      val seedColor = Score.score(QuantizerCelebi.quantize(bitmapPixels, 128)).first()
      val scheme =
        SchemeContent(sourceColorHct = Hct.fromInt(seedColor), isDark = true, contrastLevel = 0.0)

      val dynamicColors = MaterialDynamicColors()
      val tokens =
        mapOf(
          TINTED_BACKGROUND to
            DynamicColor.fromPalette(
              palette = { s: DynamicScheme -> s.primaryPalette },
              tone = { s: DynamicScheme -> 10.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { s: DynamicScheme ->
                ToneDeltaConstraint(
                  MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                  dynamicColors.primaryContainer(),
                  TonePolarity.DARKER
                )
              }
            ),
          TINTED_SURFACE to
            DynamicColor.fromPalette(
              palette = { s: DynamicScheme -> s.primaryPalette },
              tone = { s: DynamicScheme -> 20.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { s: DynamicScheme ->
                ToneDeltaConstraint(
                  MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                  dynamicColors.primaryContainer(),
                  TonePolarity.DARKER
                )
              }
            ),
          TINTED_FOREGROUND to
            DynamicColor.fromPalette(
              palette = { s: DynamicScheme -> s.primaryPalette },
              tone = { s: DynamicScheme -> 80.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { s: DynamicScheme ->
                ToneDeltaConstraint(
                  MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                  dynamicColors.primaryContainer(),
                  TonePolarity.DARKER
                )
              }
            ),
          SURFACE_CONTAINER to dynamicColors.surfaceContainer(),
          SURFACE_CONTAINER_LOWEST to dynamicColors.surfaceContainerLowest()
        )

      for (token in tokens) {
        colorMap[token.key] = token.value.getArgb(scheme)
      }
    }
    return colorMap
  }
}

@Immutable
private data class DynamicColors(
  val tintedBackground: Color,
  val tintedSurface: Color,
  val tintedForeground: Color,
  val surfaceContainer: Color,
  val surfaceContainerLowest: Color
)

expect suspend fun fetchImageBitmapFromUrl(url: String): ImageBitmap?
