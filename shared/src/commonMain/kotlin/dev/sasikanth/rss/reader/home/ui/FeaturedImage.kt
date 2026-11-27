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

package dev.sasikanth.rss.reader.home.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.BitmapImage
import coil3.compose.AsyncImage as CoilAsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.memory.MemoryCache
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Size as CoilSize
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ImageViewerBoundsTransform
import dev.sasikanth.rss.reader.ui.ImageViewerCornerRadiusSpec
import dev.sasikanth.rss.reader.ui.LocalNavAnimatedContentScopeOrNull
import dev.sasikanth.rss.reader.ui.LocalSharedTransitionScope
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.disableAnimation

private val FeaturedCardImageSize = CoilSize(960, 540)
private val UnlockedFeaturedImageSize = CoilSize(Dimension.Undefined, 500)

internal fun featuredImageMemoryCacheKey(url: String, unlockAspectRatio: Boolean): String {
  val imageSize = if (unlockAspectRatio) UnlockedFeaturedImageSize else FeaturedCardImageSize
  return "$url#${imageSize.width}x${imageSize.height}"
}

private class MemoryCacheKeyRef {
  var key: MemoryCache.Key? = null
}

@Composable
fun FeaturedImage(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
  unlockAspectRatio: Boolean = false,
  isActiveAndSettled: () -> Boolean = { true },
  sharedElementKey: String? = null,
) {
  val sizeClass = LocalWindowSizeClass.current
  val imageMaxHeight =
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 360.dp
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 250.dp
      else -> Dp.Unspecified
    }

  val adaptiveSizeModifier =
    if (unlockAspectRatio) {
      Modifier.fillMaxWidth()
    } else {
      Modifier.aspectRatio(16f / 9f).heightIn(max = imageMaxHeight)
    }
  val targetShape =
    if (unlockAspectRatio) {
      MaterialTheme.shapes.small
    } else {
      MaterialTheme.shapes.extraLarge
    }
  val targetCornerRadius = if (unlockAspectRatio) 8.dp else 28.dp
  val adaptiveContentScale =
    if (unlockAspectRatio) {
      ContentScale.FillWidth
    } else {
      widthBiasedScale
    }

  imageUrl?.let { url ->
    if (LocalBlockImage.current) return@let

    val context = LocalPlatformContext.current
    val colorScheme = AppTheme.colorScheme
    val imageSize = if (unlockAspectRatio) UnlockedFeaturedImageSize else FeaturedCardImageSize
    val urlMayBeAnimated = remember(url) { mayBeAnimatedUrl(url) }
    var confirmedStatic by remember(url) { mutableStateOf(false) }
    val lastLoadedCacheKey = remember(url, imageSize) { MemoryCacheKeyRef() }

    val shouldDisableAnimation =
      if (!urlMayBeAnimated) {
        true
      } else if (confirmedStatic) {
        false
      } else {
        val activeAndSettled by
          remember(isActiveAndSettled) { computedStateOf { isActiveAndSettled() } }
        !activeAndSettled
      }

    val model =
      remember(context, url, imageSize, unlockAspectRatio, shouldDisableAnimation) {
        val memoryCacheKey = featuredImageMemoryCacheKey(url, unlockAspectRatio)
        val placeholderKey = lastLoadedCacheKey.key
        ImageRequest.Builder(context)
          .data(url)
          .size(imageSize)
          .diskCacheKey(url)
          .memoryCacheKey(memoryCacheKey)
          .apply {
            if (shouldDisableAnimation) {
              disableAnimation()
            }
            placeholderKey?.let { placeholderMemoryCacheKey(it) }
          }
          .crossfade(placeholderKey == null)
          .build()
      }

    val onState: ((AsyncImagePainter.State) -> Unit)? =
      remember(urlMayBeAnimated, shouldDisableAnimation, lastLoadedCacheKey) {
        if (!urlMayBeAnimated) {
          null
        } else {
          { state ->
            if (state is AsyncImagePainter.State.Success) {
              lastLoadedCacheKey.key = state.result.memoryCacheKey
              if (!shouldDisableAnimation && state.result.image is BitmapImage) {
                confirmedStatic = true
              }
            }
          }
        }
      }

    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedContentScopeOrNull.current
    val (sharedElementModifier, clipShape) =
      if (
        sharedElementKey != null && sharedTransitionScope != null && animatedVisibilityScope != null
      ) {
        with(sharedTransitionScope) {
          val sharedContentState = rememberSharedContentState(key = sharedElementKey)
          val cornerRadius by
            animatedVisibilityScope.transition.animateDp(
              transitionSpec = { ImageViewerCornerRadiusSpec },
              label = "featuredImageCornerRadius",
            ) { state ->
              if (state != EnterExitState.Visible && sharedContentState.isMatchFound) {
                0.dp
              } else {
                targetCornerRadius
              }
            }
          val shape =
            if (cornerRadius == targetCornerRadius) {
              targetShape
            } else {
              RoundedCornerShape(cornerRadius)
            }
          val sharedModifier =
            Modifier.sharedElement(
              sharedContentState = sharedContentState,
              animatedVisibilityScope = animatedVisibilityScope,
              boundsTransform = ImageViewerBoundsTransform,
            )
          sharedModifier to shape
        }
      } else {
        Modifier to targetShape
      }

    CoilAsyncImage(
      model = model,
      modifier =
        Modifier.then(adaptiveSizeModifier)
          .then(sharedElementModifier)
          .clip(clipShape)
          .drawBehind { drawRect(colorScheme.settledOnSurface.copy(alpha = 0.16f)) }
          .then(modifier),
      contentDescription = null,
      contentScale = adaptiveContentScale,
      alignment = alignment,
      onState = onState,
    )
  }
}

private fun mayBeAnimatedUrl(url: String): Boolean {
  val lowerUrl = url.lowercase()
  if (lowerUrl.contains(".gif") || lowerUrl.contains("format=gif")) return true
  val path = lowerUrl.substringBefore('?').substringBefore('#')
  return !(path.endsWith(".jpg") ||
    path.endsWith(".jpeg") ||
    path.endsWith(".png") ||
    path.endsWith(".svg") ||
    path.endsWith(".bmp") ||
    path.endsWith(".avif"))
}

internal val widthBiasedScale =
  object : ContentScale {
    override fun computeScaleFactor(srcSize: Size, dstSize: Size): ScaleFactor {
      val heightScale = dstSize.height / srcSize.height
      val widthScale = dstSize.width / srcSize.width
      val scale = maxOf(heightScale, widthScale * 1.2f)

      return ScaleFactor(scale, scale)
    }
  }
