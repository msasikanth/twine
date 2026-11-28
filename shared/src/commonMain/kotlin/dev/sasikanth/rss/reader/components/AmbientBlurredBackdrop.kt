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

package dev.sasikanth.rss.reader.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.smoothVerticalFade
import kotlin.math.roundToInt

private const val BLUR_DOWNSCALE_FACTOR = 8f
private val AMBIENT_BLUR_RADIUS = 70.dp
internal val AmbientBlurImageSize = Size(Dimension.Undefined, height = 100)
private val LightAmbientLuminosityColor = Color(0xFFC6C6C6)
private val DarkAmbientLuminosityColor = Color(0xFF5E5E5E)

@Composable
internal fun rememberAmbientBlurEffect(): BlurEffect? {
  val density = LocalDensity.current
  return remember(density) {
    if (canBlurImage) {
      val blurRadius = AMBIENT_BLUR_RADIUS / BLUR_DOWNSCALE_FACTOR
      val blurRadiusPx = with(density) { blurRadius.toPx() }
      BlurEffect(radiusX = blurRadiusPx, radiusY = blurRadiusPx, edgeTreatment = TileMode.Decal)
    } else {
      null
    }
  }
}

@Composable
internal fun rememberAmbientImageColorFilter(): ColorFilter {
  val isDarkTheme = AppTheme.isDark
  return remember(isDarkTheme) {
    ColorFilter.colorMatrix(
      ColorMatrix().apply {
        val saturation = if (isDarkTheme) 1f else 1.1f
        setToSaturation(saturation)
      }
    )
  }
}

// Blurs and tints the content as one layer so overlapping children don't stack decal edge fades
// or luminosity overlays.
@Composable
internal fun AmbientBlurredBackdrop(
  blurEffect: BlurEffect,
  hasVisibleContent: () -> Boolean,
  modifier: Modifier = Modifier,
  tintAlpha: Float = 1f,
  applyBottomScrim: Boolean = true,
  content: @Composable BoxScope.() -> Unit,
) {
  val colorScheme = AppTheme.colorScheme
  val luminosityColor = resolveAmbientLuminosityColor(isDarkTheme = AppTheme.isDark)
  val clampedTintAlpha = tintAlpha.coerceIn(0f, 1f)

  Box(
    modifier =
      modifier
        .layout { measurable, constraints ->
          val fullWidth = constraints.maxWidth
          val fullHeight =
            if (constraints.hasBoundedHeight) {
              constraints.maxHeight
            } else {
              constraints.minHeight
            }
          if (fullWidth <= 0 || fullHeight <= 0) {
            val placeable = measurable.measure(Constraints.fixed(0, 0))
            layout(0, 0) { placeable.place(0, 0) }
          } else {
            val scaledWidth = (fullWidth / BLUR_DOWNSCALE_FACTOR).roundToInt().coerceAtLeast(1)
            val scaledHeight = (fullHeight / BLUR_DOWNSCALE_FACTOR).roundToInt().coerceAtLeast(1)
            val placeable = measurable.measure(Constraints.fixed(scaledWidth, scaledHeight))
            val scaleXFactor = fullWidth.toFloat() / scaledWidth.toFloat()
            val scaleYFactor = fullHeight.toFloat() / scaledHeight.toFloat()

            layout(fullWidth, fullHeight) {
              placeable.placeWithLayer(0, 0) {
                transformOrigin = TransformOrigin(0f, 0f)
                scaleX = scaleXFactor
                scaleY = scaleYFactor
                renderEffect = blurEffect
              }
            }
          }
        }
        .drawWithCache {
          val scrimMask =
            if (applyBottomScrim) {
              Brush.verticalGradient(
                0.7f to Color.Transparent,
                1f to Color.White,
                startY = 0f,
                endY = size.height,
              )
            } else {
              null
            }
          var lastBackdrop = Color.Unspecified
          var cachedScrimFilter: ColorFilter? = null
          onDrawWithContent {
            drawContent()
            if (hasVisibleContent()) {
              drawRect(
                color = luminosityColor,
                alpha = clampedTintAlpha,
                blendMode = BlendMode.Luminosity,
              )
              if (scrimMask != null) {
                val backdrop = colorScheme.backdrop
                val scrimFilter =
                  if (backdrop == lastBackdrop) {
                    cachedScrimFilter
                  } else {
                    lastBackdrop = backdrop
                    ColorFilter.tint(backdrop).also { cachedScrimFilter = it }
                  }
                drawRect(brush = scrimMask, colorFilter = scrimFilter)
              }
            }
          }
        },
    content = content,
  )
}

@Composable
internal fun AmbientTonalFallbackBackdrop(
  alphaProvider: () -> Float,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme
  val isDarkTheme = AppTheme.isDark

  Spacer(
    modifier =
      modifier
        .graphicsLayer {
          compositingStrategy = CompositingStrategy.Offscreen
          alpha = alphaProvider()
        }
        .drawWithCache {
          val primaryGlow =
            colorScheme.primaryContainer.copy(alpha = if (isDarkTheme) 0.55f else 0.45f)
          val secondaryGlow =
            colorScheme.inversePrimary.copy(alpha = if (isDarkTheme) 0.18f else 0.22f)
          val radialBrush =
            Brush.radialGradient(
              0f to primaryGlow,
              0.55f to secondaryGlow,
              1f to Color.Transparent,
              center = Offset(size.width * 0.5f, size.height * 0.25f),
              radius = maxOf(size.width, size.height).coerceAtLeast(1f) * 0.85f,
            )
          val fadeEndY = (size.height * 0.96f).coerceAtLeast(1f)
          val verticalGlow =
            smoothVerticalFade(
              color = primaryGlow.copy(alpha = primaryGlow.alpha * 0.45f),
              startY = 0f,
              endY = fadeEndY,
            )
          val maskStartY = size.height * 0.25f
          val fadeMask =
            smoothVerticalFade(
              color = Color.Black,
              startY = maskStartY,
              endY = fadeEndY.coerceAtLeast(maskStartY + 1f),
            )
          onDrawBehind {
            drawRect(brush = radialBrush)
            drawRect(brush = verticalGlow)
            drawRect(brush = fadeMask, blendMode = BlendMode.DstIn)
          }
        }
  )
}

internal fun resolveAmbientLuminosityColor(isDarkTheme: Boolean): Color =
  if (isDarkTheme) DarkAmbientLuminosityColor else LightAmbientLuminosityColor

internal fun shouldRenderBlurredBackdrop(
  canBlur: Boolean,
  hasBlurEffect: Boolean,
  imageUrl: String?,
  blockImages: Boolean,
): Boolean = canBlur && hasBlurEffect && !imageUrl.isNullOrBlank() && !blockImages

internal fun resolveAmbientBackdropLayerAlphas(
  maxAlpha: Float,
  imageLayerAlpha: Float,
): Pair<Float, Float> {
  val clampedMax = maxAlpha.coerceIn(0f, 1f)
  val clampedLayer = imageLayerAlpha.coerceIn(0f, 1f)
  val blurredAlpha = clampedMax * clampedLayer
  val fallbackAlpha = clampedMax * (1f - clampedLayer)
  return blurredAlpha to fallbackAlpha
}
