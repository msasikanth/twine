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
import dev.sasikanth.rss.reader.ui.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TINTED_BACKGROUND = "tinted_background"
private const val TINTED_SURFACE = "tinted_surface"
private const val TINTED_FOREGROUND = "tinted_foreground"
private const val TINTED_HIGHLIGHT = "tinted_highlight"
private const val OUTLINE = "outline"
private const val OUTLINE_VARIANT = "outline_VARIANT"
private const val SURFACE = "surface"
private const val ON_SURFACE = "on_surface"
private const val ON_SURFACE_VARIANT = "on_surface_variant"
private const val SURFACE_CONTAINER = "surface_container"
private const val SURFACE_CONTAINER_LOW = "surface_container_low"
private const val SURFACE_CONTAINER_LOWEST = "surface_container_lowest"
private const val SURFACE_CONTAINER_HIGH = "surface_container_high"
private const val SURFACE_CONTAINER_HIGHEST = "surface_container_highest"

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
  val tintedHighlight by
    animateColorAsState(dynamicColorState.tintedForeground, spring(stiffness = Spring.StiffnessLow))
  val outline by
    animateColorAsState(dynamicColorState.outline, spring(stiffness = Spring.StiffnessLow))
  val outlineVariant by
    animateColorAsState(dynamicColorState.outlineVariant, spring(stiffness = Spring.StiffnessLow))
  val surface by
    animateColorAsState(dynamicColorState.surface, spring(stiffness = Spring.StiffnessLow))
  val onSurface by
    animateColorAsState(dynamicColorState.onSurface, spring(stiffness = Spring.StiffnessLow))
  val onSurfaceVariant by
    animateColorAsState(dynamicColorState.onSurfaceVariant, spring(stiffness = Spring.StiffnessLow))
  val surfaceContainer by
    animateColorAsState(dynamicColorState.surfaceContainer, spring(stiffness = Spring.StiffnessLow))
  val surfaceContainerLow by
    animateColorAsState(
      dynamicColorState.surfaceContainerLow,
      spring(stiffness = Spring.StiffnessLow)
    )
  val surfaceContainerLowest by
    animateColorAsState(
      dynamicColorState.surfaceContainerLowest,
      spring(stiffness = Spring.StiffnessLow)
    )
  val surfaceContainerHigh by
    animateColorAsState(
      dynamicColorState.surfaceContainerHigh,
      spring(stiffness = Spring.StiffnessLow)
    )
  val surfaceContainerHighest by
    animateColorAsState(
      dynamicColorState.surfaceContainerHighest,
      spring(stiffness = Spring.StiffnessLow)
    )

  val colorScheme =
    AppTheme.colorScheme.copy(
      tintedBackground = tintedBackground,
      tintedSurface = tintedSurface,
      tintedForeground = tintedForeground,
      tintedHighlight = tintedHighlight,
      outline = outline,
      outlineVariant = outlineVariant,
      surface = surface,
      onSurface = onSurface,
      onSurfaceVariant = onSurfaceVariant,
      surfaceContainer = surfaceContainer,
      surfaceContainerLow = surfaceContainerLow,
      surfaceContainerLowest = surfaceContainerLowest,
      surfaceContainerHigh = surfaceContainerHigh,
      surfaceContainerHighest = surfaceContainerHighest,
    )

  AppTheme(appColorScheme = colorScheme, content = content)
}

@Composable
internal fun rememberDynamicColorState(
  defaultTintedBackground: Color = AppTheme.colorScheme.tintedBackground,
  defaultTintedSurface: Color = AppTheme.colorScheme.tintedSurface,
  defaultTintedForeground: Color = AppTheme.colorScheme.tintedForeground,
  defaultTintedHighlight: Color = AppTheme.colorScheme.tintedHighlight,
  defaultOutline: Color = AppTheme.colorScheme.outline,
  defaultOutlineVariant: Color = AppTheme.colorScheme.outlineVariant,
  defaultSurface: Color = AppTheme.colorScheme.surface,
  defaultOnSurface: Color = AppTheme.colorScheme.onSurface,
  defaultOnSurfaceVariant: Color = AppTheme.colorScheme.onSurfaceVariant,
  defaultSurfaceContainer: Color = AppTheme.colorScheme.surfaceContainer,
  defaultSurfaceContainerLow: Color = AppTheme.colorScheme.surfaceContainerLow,
  defaultSurfaceContainerLowest: Color = AppTheme.colorScheme.surfaceContainerLowest,
  defaultSurfaceContainerHigh: Color = AppTheme.colorScheme.surfaceContainerHigh,
  defaultSurfaceContainerHighest: Color = AppTheme.colorScheme.surfaceContainerHighest,
  cacheSize: Int = 15
): DynamicColorState {
  val imageLoader = LocalImageLoader.current
  return remember {
    DynamicColorState(
      defaultTintedBackground,
      defaultTintedSurface,
      defaultTintedForeground,
      defaultTintedHighlight,
      defaultOutline,
      defaultOutlineVariant,
      defaultSurface,
      defaultOnSurface,
      defaultOnSurfaceVariant,
      defaultSurfaceContainer,
      defaultSurfaceContainerLow,
      defaultSurfaceContainerLowest,
      defaultSurfaceContainerHigh,
      defaultSurfaceContainerHighest,
      imageLoader,
      cacheSize
    )
  }
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
  private val defaultTintedHighlight: Color,
  private val defaultOutline: Color,
  private val defaultOutlineVariant: Color,
  private val defaultSurface: Color,
  private val defaultOnSurface: Color,
  private val defaultOnSurfaceVariant: Color,
  private val defaultSurfaceContainer: Color,
  private val defaultSurfaceContainerLow: Color,
  private val defaultSurfaceContainerLowest: Color,
  private val defaultSurfaceContainerHigh: Color,
  private val defaultSurfaceContainerHighest: Color,
  private val imageLoader: ImageLoader?,
  cacheSize: Int
) {
  var tintedBackground by mutableStateOf(defaultTintedBackground)
    private set

  var tintedSurface by mutableStateOf(defaultTintedSurface)
    private set

  var tintedForeground by mutableStateOf(defaultTintedForeground)
    private set

  var tintedHighlight by mutableStateOf(defaultTintedHighlight)
    private set

  var outline by mutableStateOf(defaultOutline)
    private set

  var outlineVariant by mutableStateOf(defaultOutlineVariant)
    private set

  var surface by mutableStateOf(defaultSurfaceContainer)
    private set

  var onSurface by mutableStateOf(defaultOnSurface)
    private set

  var onSurfaceVariant by mutableStateOf(defaultOnSurfaceVariant)
    private set

  var surfaceContainer by mutableStateOf(defaultSurfaceContainer)
    private set

  var surfaceContainerLow by mutableStateOf(defaultSurfaceContainerLow)
    private set

  var surfaceContainerLowest by mutableStateOf(defaultSurfaceContainerLowest)
    private set

  var surfaceContainerHigh by mutableStateOf(defaultSurfaceContainerHigh)
    private set

  var surfaceContainerHighest by mutableStateOf(defaultSurfaceContainerHighest)
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
    tintedHighlight = result?.tintedHighlight ?: defaultTintedHighlight
    outline = result?.outline ?: defaultOutline
    outlineVariant = result?.outlineVariant ?: defaultOutlineVariant
    surface = result?.surface ?: defaultSurface
    onSurface = result?.onSurface ?: defaultOnSurface
    onSurfaceVariant = result?.onSurfaceVariant ?: defaultOnSurfaceVariant
    surfaceContainer = result?.surfaceContainer ?: defaultSurfaceContainer
    surfaceContainerLow = result?.surfaceContainerLow ?: defaultSurfaceContainerLow
    surfaceContainerLowest = result?.surfaceContainerLowest ?: defaultSurfaceContainerLowest
    surfaceContainerHigh = result?.surfaceContainerHigh ?: defaultSurfaceContainerHigh
    surfaceContainerHighest = result?.surfaceContainerHighest ?: defaultSurfaceContainerHighest
  }

  fun reset() {
    tintedBackground = defaultTintedBackground
    tintedSurface = defaultTintedSurface
    tintedForeground = defaultTintedForeground
    tintedHighlight = defaultTintedHighlight
    outline = defaultOutline
    outlineVariant = defaultOutlineVariant
    surface = defaultSurface
    onSurface = defaultOnSurface
    onSurfaceVariant = defaultOnSurfaceVariant
    surfaceContainer = defaultSurfaceContainer
    surfaceContainerLow = defaultSurfaceContainerLow
    surfaceContainerLowest = defaultSurfaceContainerLowest
    surfaceContainerHigh = defaultSurfaceContainerHigh
    surfaceContainerHighest = defaultSurfaceContainerHighest
  }

  private suspend fun fetchDynamicColors(url: String): DynamicColors? {
    val cached = cache?.get(url)
    if (cached != null) {
      // If we already have the result cached, return early now...
      return cached
    }

    val image = imageLoader?.getImage(url, size = 128)
    return if (image != null) {
      extractColorsFromImage(image)
        .let { colorsMap ->
          return@let if (colorsMap.isNotEmpty()) {
            DynamicColors(
              tintedBackground = Color(colorsMap[TINTED_BACKGROUND]!!),
              tintedSurface = Color(colorsMap[TINTED_SURFACE]!!),
              tintedForeground = Color(colorsMap[TINTED_FOREGROUND]!!),
              tintedHighlight = Color(colorsMap[TINTED_HIGHLIGHT]!!),
              outline = Color(colorsMap[OUTLINE]!!),
              outlineVariant = Color(colorsMap[OUTLINE_VARIANT]!!),
              surface = Color(colorsMap[SURFACE]!!),
              onSurface = Color(colorsMap[ON_SURFACE]!!),
              onSurfaceVariant = Color(colorsMap[ON_SURFACE_VARIANT]!!),
              surfaceContainer = Color(colorsMap[SURFACE_CONTAINER]!!),
              surfaceContainerLow = Color(colorsMap[SURFACE_CONTAINER_LOW]!!),
              surfaceContainerLowest = Color(colorsMap[SURFACE_CONTAINER_LOWEST]!!),
              surfaceContainerHigh = Color(colorsMap[SURFACE_CONTAINER_HIGH]!!),
              surfaceContainerHighest = Color(colorsMap[SURFACE_CONTAINER_HIGHEST]!!)
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
              tone = { _ -> 10.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { _ ->
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
              tone = { _ -> 20.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { _ ->
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
              tone = { _ -> 80.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { _ ->
                ToneDeltaConstraint(
                  MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                  dynamicColors.primaryContainer(),
                  TonePolarity.DARKER
                )
              }
            ),
          TINTED_HIGHLIGHT to
            DynamicColor.fromPalette(
              palette = { s: DynamicScheme -> s.primaryPalette },
              tone = { s: DynamicScheme -> 40.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { s: DynamicScheme ->
                ToneDeltaConstraint(
                  MaterialDynamicColors.CONTAINER_ACCENT_TONE_DELTA,
                  dynamicColors.primaryContainer(),
                  TonePolarity.DARKER
                )
              }
            ),
          OUTLINE to dynamicColors.outline(),
          OUTLINE_VARIANT to dynamicColors.outlineVariant(),
          SURFACE to dynamicColors.surface(),
          ON_SURFACE to dynamicColors.onSurface(),
          ON_SURFACE_VARIANT to dynamicColors.onSurfaceVariant(),
          SURFACE_CONTAINER to dynamicColors.surfaceContainer(),
          SURFACE_CONTAINER_LOW to dynamicColors.surfaceContainerLow(),
          SURFACE_CONTAINER_LOWEST to dynamicColors.surfaceContainerLowest(),
          SURFACE_CONTAINER_HIGH to dynamicColors.surfaceContainerHigh(),
          SURFACE_CONTAINER_HIGHEST to dynamicColors.surfaceContainerHighest(),
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
  val tintedHighlight: Color,
  val outline: Color,
  val outlineVariant: Color,
  val surface: Color,
  val onSurface: Color,
  val onSurfaceVariant: Color,
  val surfaceContainer: Color,
  val surfaceContainerLow: Color,
  val surfaceContainerLowest: Color,
  val surfaceContainerHigh: Color,
  val surfaceContainerHighest: Color,
)
