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

package dev.sasikanth.rss.reader.reader.page.ui

import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage as CoilAsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size as CoilSize
import dev.sasikanth.rss.reader.components.image.FeedIcon
import dev.sasikanth.rss.reader.reader.ui.ImageClickEvent
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.ImageViewerBoundsTransform
import dev.sasikanth.rss.reader.ui.ImageViewerCornerRadiusSpec
import dev.sasikanth.rss.reader.ui.LocalNavAnimatedContentScopeOrNull
import dev.sasikanth.rss.reader.ui.LocalSharedTransitionScope
import dev.sasikanth.rss.reader.utils.LocalBlockImage

private val HeroCornerRadius = 24.dp
private val HeroShape = RoundedCornerShape(HeroCornerRadius)
private val FallbackIconShape = RoundedCornerShape(16.dp)
private val HeroRequestSize = CoilSize(800, 800)
private const val MIN_HIGH_RES_ARTWORK_PX = 128

@Composable
internal fun PodcastArtworkHero(
  postId: String,
  artworkUrl: String?,
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
  modifier: Modifier = Modifier,
  size: Dp = 224.dp,
  onImageClick: ((ImageClickEvent) -> Unit)? = null,
) {
  val colorScheme = AppTheme.colorScheme
  val shouldBlockImage = LocalBlockImage.current
  var isHighResLoaded by remember(artworkUrl) { mutableStateOf(false) }

  val canDisplayArtwork = !artworkUrl.isNullOrBlank() && !shouldBlockImage
  val artworkAlpha by
    animateFloatAsState(
      targetValue = if (canDisplayArtwork && isHighResLoaded) 1f else 0f,
      animationSpec = tween(durationMillis = 220),
      label = "podcastHeroArtworkAlpha",
    )

  val memoryCacheKey =
    remember(artworkUrl) {
      artworkUrl?.let { "$it#${HeroRequestSize.width}x${HeroRequestSize.height}" }
    }
  val sharedElementKey =
    remember(postId, artworkUrl) { artworkUrl?.let { "podcast-hero:$postId:$it" } }

  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = LocalNavAnimatedContentScopeOrNull.current
  val (sharedElementModifier, clipShape) =
    if (
      canDisplayArtwork &&
        isHighResLoaded &&
        sharedElementKey != null &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null
    ) {
      with(sharedTransitionScope) {
        val sharedContentState = rememberSharedContentState(key = sharedElementKey)
        val cornerRadius by
          animatedVisibilityScope.transition.animateDp(
            transitionSpec = { ImageViewerCornerRadiusSpec },
            label = "podcastHeroCornerRadius",
          ) { state ->
            if (state != EnterExitState.Visible && sharedContentState.isMatchFound) {
              0.dp
            } else {
              HeroCornerRadius
            }
          }
        val shape =
          if (cornerRadius == HeroCornerRadius) {
            HeroShape
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
      Modifier to HeroShape
    }

  val clickModifier =
    if (
      artworkUrl != null &&
        !shouldBlockImage &&
        isHighResLoaded &&
        onImageClick != null &&
        sharedElementKey != null
    ) {
      Modifier.clickable(role = Role.Image) {
        onImageClick(
          ImageClickEvent(
            imageUrl = artworkUrl,
            sharedElementKey = sharedElementKey,
            memoryCacheKey = memoryCacheKey,
            sourceCornerRadiusDp = HeroCornerRadius.value,
            sourceAspectRatio = 1f,
          )
        )
      }
    } else {
      Modifier
    }

  Box(
    modifier =
      modifier
        .size(size)
        .shadow(elevation = 12.dp, shape = clipShape, clip = false)
        .then(sharedElementModifier)
        .clip(clipShape)
        .border(
          width = 1.dp,
          color = colorScheme.outlineVariant.copy(alpha = 0.4f),
          shape = clipShape,
        )
        .then(clickModifier),
    contentAlignment = Alignment.Center,
  ) {
    StudioTonalFallbackCard(
      feedIcon = feedIcon,
      feedHomepageLink = feedHomepageLink,
      showFeedFavIcon = showFeedFavIcon,
      modifier = Modifier.fillMaxSize(),
    )

    if (canDisplayArtwork) {
      val context = LocalPlatformContext.current
      val model =
        remember(context, artworkUrl, memoryCacheKey) {
          ImageRequest.Builder(context)
            .data(artworkUrl)
            .size(HeroRequestSize)
            .diskCacheKey(artworkUrl)
            .apply { memoryCacheKey?.let { memoryCacheKey(it) } }
            .crossfade(false)
            .build()
        }

      CoilAsyncImage(
        model = model,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize().graphicsLayer { alpha = artworkAlpha },
        onState = { state ->
          when (state) {
            is AsyncImagePainter.State.Success -> {
              val image = state.result.image
              isHighResLoaded = maxOf(image.width, image.height) >= MIN_HIGH_RES_ARTWORK_PX
            }
            is AsyncImagePainter.State.Error -> {
              isHighResLoaded = false
            }
            else -> Unit
          }
        },
      )
    }
  }
}

@Composable
private fun StudioTonalFallbackCard(
  feedIcon: String,
  feedHomepageLink: String,
  showFeedFavIcon: Boolean,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme

  Box(
    modifier =
      modifier.drawWithCache {
        val gradient =
          Brush.linearGradient(
            colors = listOf(colorScheme.primaryContainer, colorScheme.surfaceContainerHigh),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
          )
        val ringColor = colorScheme.onPrimaryContainer.copy(alpha = 0.08f)
        val ringStroke = Stroke(width = 1.5.dp.toPx())
        val minDim = size.minDimension
        val ringRadii = floatArrayOf(minDim * 0.24f, minDim * 0.36f, minDim * 0.48f, minDim * 0.60f)

        onDrawBehind {
          drawRect(brush = gradient)
          for (radius in ringRadii) {
            drawCircle(color = ringColor, radius = radius, center = center, style = ringStroke)
          }
        }
      },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier.background(colorScheme.surface, FallbackIconShape).padding(2.dp),
      contentAlignment = Alignment.Center,
    ) {
      FeedIcon(
        icon = feedIcon,
        homepageLink = feedHomepageLink,
        showFeedFavIcon = showFeedFavIcon,
        contentDescription = null,
        modifier = Modifier.size(56.dp),
      )
    }
  }
}
