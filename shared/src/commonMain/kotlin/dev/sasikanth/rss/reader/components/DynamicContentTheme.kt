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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.size.Scale
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
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.inverse
import dev.sasikanth.rss.reader.utils.toComposeImageBitmap
import kotlin.math.absoluteValue
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
  dynamicColorState: DynamicColorState,
  content: @Composable () -> Unit
) {
  val colorScheme =
    AppTheme.colorScheme.copy(
      tintedBackground = dynamicColorState.tintedBackground,
      tintedSurface = dynamicColorState.tintedSurface,
      tintedForeground = dynamicColorState.tintedForeground,
      tintedHighlight = dynamicColorState.tintedHighlight,
      outline = dynamicColorState.outline,
      outlineVariant = dynamicColorState.outlineVariant,
      surface = dynamicColorState.surface,
      onSurface = dynamicColorState.onSurface,
      onSurfaceVariant = dynamicColorState.onSurfaceVariant,
      surfaceContainer = dynamicColorState.surfaceContainer,
      surfaceContainerLow = dynamicColorState.surfaceContainerLow,
      surfaceContainerLowest = dynamicColorState.surfaceContainerLowest,
      surfaceContainerHigh = dynamicColorState.surfaceContainerHigh,
      surfaceContainerHighest = dynamicColorState.surfaceContainerHighest,
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
  imageLoader: ImageLoader,
): DynamicColorState {
  val platformContext = LocalPlatformContext.current
  return rememberSaveable(saver = DynamicColorState.Saver) {
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
      )
    }
    .apply { setImageLoader(imageLoader, platformContext) }
}

/**
 * A class which stores and caches the result of any calculated colors from images.
 *
 * @param cacheSize The size of the [LruCache] used to store recent results. Pass `0` to disable the
 *   cache.
 */
@Stable
internal class DynamicColorState(
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
  cacheSize: Int = 15
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
  private var images = emptyList<String>()

  private lateinit var imageLoader: ImageLoader
  private lateinit var platformContext: PlatformContext

  companion object {
    val Saver: Saver<DynamicColorState, *> =
      mapSaver(
        save = {
          mapOf(
            TINTED_BACKGROUND to it.tintedBackground.value.toString(),
            TINTED_SURFACE to it.tintedSurface.value.toString(),
            TINTED_FOREGROUND to it.tintedForeground.value.toString(),
            TINTED_HIGHLIGHT to it.tintedHighlight.value.toString(),
            OUTLINE to it.outline.value.toString(),
            OUTLINE_VARIANT to it.outlineVariant.value.toString(),
            SURFACE to it.surface.value.toString(),
            ON_SURFACE to it.onSurface.value.toString(),
            ON_SURFACE_VARIANT to it.onSurfaceVariant.value.toString(),
            SURFACE_CONTAINER to it.surfaceContainer.value.toString(),
            SURFACE_CONTAINER_LOW to it.surfaceContainerLow.value.toString(),
            SURFACE_CONTAINER_LOWEST to it.surfaceContainerLowest.value.toString(),
            SURFACE_CONTAINER_HIGH to it.surfaceContainerHigh.value.toString(),
            SURFACE_CONTAINER_HIGHEST to it.surfaceContainerHighest.value.toString(),
          )
        },
        restore = {
          DynamicColorState(
            defaultTintedBackground = Color(it[TINTED_BACKGROUND].toString().toULong()),
            defaultTintedSurface = Color(it[TINTED_SURFACE].toString().toULong()),
            defaultTintedForeground = Color(it[TINTED_FOREGROUND].toString().toULong()),
            defaultTintedHighlight = Color(it[TINTED_HIGHLIGHT].toString().toULong()),
            defaultOutline = Color(it[OUTLINE].toString().toULong()),
            defaultOutlineVariant = Color(it[OUTLINE_VARIANT].toString().toULong()),
            defaultSurface = Color(it[SURFACE].toString().toULong()),
            defaultOnSurface = Color(it[ON_SURFACE].toString().toULong()),
            defaultOnSurfaceVariant = Color(it[ON_SURFACE_VARIANT].toString().toULong()),
            defaultSurfaceContainer = Color(it[SURFACE_CONTAINER].toString().toULong()),
            defaultSurfaceContainerLow = Color(it[SURFACE_CONTAINER_LOW].toString().toULong()),
            defaultSurfaceContainerLowest =
              Color(it[SURFACE_CONTAINER_LOWEST].toString().toULong()),
            defaultSurfaceContainerHigh = Color(it[SURFACE_CONTAINER_HIGH].toString().toULong()),
            defaultSurfaceContainerHighest =
              Color(it[SURFACE_CONTAINER_HIGHEST].toString().toULong()),
          )
        }
      )
  }

  fun setImageLoader(imageLoader: ImageLoader, platformContext: PlatformContext) {
    this.imageLoader = imageLoader
    this.platformContext = platformContext
  }

  suspend fun onContentChange(newImages: List<String>) {
    if (images.isEmpty()) {
      images = newImages
    }
    images.forEach { imageUrl -> fetchDynamicColors(imageUrl) }
  }

  fun updateOffset(
    previousImageUrl: String?,
    currentImageUrl: String,
    nextImageUrl: String?,
    offset: Float
  ) {

    val previousDynamicColors = previousImageUrl?.let { cache?.get(it) }
    val currentDynamicColors = cache?.get(currentImageUrl)
    val nextDynamicColors = nextImageUrl?.let { cache?.get(it) }

    tintedBackground =
      interpolateColors(
        previous = previousDynamicColors?.tintedBackground,
        current = currentDynamicColors?.tintedBackground,
        next = nextDynamicColors?.tintedBackground,
        default = defaultTintedBackground,
        fraction = offset
      )
    tintedSurface =
      interpolateColors(
        previous = previousDynamicColors?.tintedSurface,
        current = currentDynamicColors?.tintedSurface,
        next = nextDynamicColors?.tintedSurface,
        default = defaultTintedSurface,
        fraction = offset
      )
    tintedForeground =
      interpolateColors(
        previous = previousDynamicColors?.tintedForeground,
        current = currentDynamicColors?.tintedForeground,
        next = nextDynamicColors?.tintedForeground,
        default = defaultTintedForeground,
        fraction = offset
      )
    tintedHighlight =
      interpolateColors(
        previous = previousDynamicColors?.tintedHighlight,
        current = currentDynamicColors?.tintedHighlight,
        next = nextDynamicColors?.tintedHighlight,
        default = defaultTintedHighlight,
        fraction = offset
      )
    outline =
      interpolateColors(
        previous = previousDynamicColors?.outline,
        current = currentDynamicColors?.outline,
        next = nextDynamicColors?.outline,
        default = defaultOutline,
        fraction = offset
      )
    outlineVariant =
      interpolateColors(
        previous = previousDynamicColors?.outlineVariant,
        current = currentDynamicColors?.outlineVariant,
        next = nextDynamicColors?.outlineVariant,
        default = defaultOutlineVariant,
        fraction = offset
      )
    surface =
      interpolateColors(
        previous = previousDynamicColors?.surface,
        current = currentDynamicColors?.surface,
        next = nextDynamicColors?.surface,
        default = defaultSurface,
        fraction = offset
      )
    onSurface =
      interpolateColors(
        previous = previousDynamicColors?.onSurface,
        current = currentDynamicColors?.onSurface,
        next = nextDynamicColors?.onSurface,
        default = defaultOnSurface,
        fraction = offset
      )
    onSurfaceVariant =
      interpolateColors(
        previous = previousDynamicColors?.onSurfaceVariant,
        current = currentDynamicColors?.onSurfaceVariant,
        next = nextDynamicColors?.onSurfaceVariant,
        default = defaultOnSurfaceVariant,
        fraction = offset
      )
    surfaceContainer =
      interpolateColors(
        previous = previousDynamicColors?.surfaceContainer,
        current = currentDynamicColors?.surfaceContainer,
        next = nextDynamicColors?.surfaceContainer,
        default = defaultSurfaceContainer,
        fraction = offset
      )
    surfaceContainerLow =
      interpolateColors(
        previous = previousDynamicColors?.surfaceContainerLow,
        current = currentDynamicColors?.surfaceContainerLow,
        next = nextDynamicColors?.surfaceContainerLow,
        default = defaultSurfaceContainerLow,
        fraction = offset
      )
    surfaceContainerLowest =
      interpolateColors(
        previous = previousDynamicColors?.surfaceContainerLowest,
        current = currentDynamicColors?.surfaceContainerLowest,
        next = nextDynamicColors?.surfaceContainerLowest,
        default = defaultSurfaceContainerLowest,
        fraction = offset
      )
    surfaceContainerHigh =
      interpolateColors(
        previous = previousDynamicColors?.surfaceContainerHigh,
        current = currentDynamicColors?.surfaceContainerHigh,
        next = nextDynamicColors?.surfaceContainerHigh,
        default = defaultSurfaceContainerHigh,
        fraction = offset
      )
    surfaceContainerHighest =
      interpolateColors(
        previous = previousDynamicColors?.surfaceContainerHighest,
        current = currentDynamicColors?.surfaceContainerHighest,
        next = nextDynamicColors?.surfaceContainerHighest,
        default = defaultSurfaceContainerHighest,
        fraction = offset
      )
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

  @OptIn(ExperimentalCoilApi::class)
  private suspend fun fetchDynamicColors(url: String): DynamicColors? {
    val cached = cache?.get(url)
    if (cached != null) {
      // If we already have the result cached, return early now...
      return cached
    }

    val imageRequest =
      ImageRequest.Builder(platformContext)
        .data(url)
        .scale(Scale.FILL)
        .size(64)
        .memoryCacheKey("$url.dynamic_colors")
        .build()

    val image = imageLoader.execute(imageRequest).image?.toComposeImageBitmap(platformContext)

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
              tone = { _: DynamicScheme -> 40.0 },
              background = { s: DynamicScheme -> dynamicColors.highestSurface(s) },
              toneDeltaConstraint = { _: DynamicScheme ->
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

  private fun interpolateColors(
    previous: Color?,
    current: Color?,
    next: Color?,
    default: Color,
    fraction: Float
  ): Color {
    val startColor =
      if (fraction < -EPSILON) {
        previous
      } else {
        current
      }

    val endColor =
      if (fraction > EPSILON) {
        next
      } else {
        current
      }

    if (startColor == null || endColor == null) {
      return default
    }

    val normalizedOffset =
      if (fraction < -EPSILON) {
        fraction.absoluteValue.inverse()
      } else {
        fraction
      }

    return lerp(startColor, endColor, normalizedOffset)
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

internal val LocalDynamicColorState =
  staticCompositionLocalOf<DynamicColorState> {
    throw NullPointerException("Please provide a dynamic color state")
  }
