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

package dev.sasikanth.rss.reader.reader.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil3.compose.AsyncImage as CoilAsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import dev.sasikanth.rss.reader.components.AmbientBlurImageSize
import dev.sasikanth.rss.reader.components.AmbientBlurredBackdrop
import dev.sasikanth.rss.reader.components.AmbientTonalFallbackBackdrop
import dev.sasikanth.rss.reader.components.rememberAmbientBlurEffect
import dev.sasikanth.rss.reader.components.rememberAmbientImageColorFilter
import dev.sasikanth.rss.reader.components.shouldRenderBlurredBackdrop
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.network.utils.UrlUtils
import dev.sasikanth.rss.reader.home.ui.widthBiasedScale
import dev.sasikanth.rss.reader.reader.resolvePodcastHeroArtworkUrl
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.disableAnimation
import dev.sasikanth.rss.reader.utils.smoothVerticalFade
import kotlin.math.floor

internal val READER_BACKDROP_SCROLL_FADE_DISTANCE = 260.dp
private const val READER_MEDIA_DARK_MAX_ALPHA = 0.65f
private const val READER_MEDIA_LIGHT_MAX_ALPHA = 0.65f

@Immutable
internal data class ReaderAmbientBackdropSpec(
  val imageUrl: String?,
  val maxAlpha: Float,
  val allowTonalFallback: Boolean,
)

@Immutable
internal data class ReaderAmbientPageTarget(val blurredAlpha: Float, val fallbackAlpha: Float)

@Immutable
internal data class ReaderAmbientBlendState(
  val containerBlurredAlpha: Float,
  val fallbackAlpha: Float,
  val leftPage: Int,
  val leftImageAlpha: Float,
  val rightPage: Int,
  val rightImageAlpha: Float,
) {
  fun imageAlphaForPage(page: Int): Float =
    when (page) {
      leftPage -> leftImageAlpha
      rightPage -> rightImageAlpha
      else -> 0f
    }
}

private data class ReaderAmbientPageEntry(
  val page: Int,
  val postId: String,
  val spec: ReaderAmbientBackdropSpec,
)

internal fun ResolvedPost.resolveReaderAmbientBackdropSpec(
  isDarkTheme: Boolean,
  enrichedCoverArtUrl: String? = null,
): ReaderAmbientBackdropSpec {
  if (!audioUrl.isNullOrBlank()) {
    return ReaderAmbientBackdropSpec(
      imageUrl = resolvePodcastHeroArtworkUrl(enrichedCoverArtUrl),
      maxAlpha = 1f,
      allowTonalFallback = true,
    )
  }

  if (UrlUtils.videoProviderName(link) != null) {
    val thumbnailUrl = UrlUtils.youTubeThumbnail(link) ?: UrlUtils.upgradeYouTubeThumbnail(imageUrl)
    return ReaderAmbientBackdropSpec(
      imageUrl = thumbnailUrl,
      maxAlpha = if (isDarkTheme) READER_MEDIA_DARK_MAX_ALPHA else READER_MEDIA_LIGHT_MAX_ALPHA,
      allowTonalFallback = true,
    )
  }

  val articleImage = imageUrl?.trim()?.takeIf { it.isNotEmpty() }
  val articleAlpha =
    if (articleImage != null) {
      if (isDarkTheme) READER_MEDIA_DARK_MAX_ALPHA else READER_MEDIA_LIGHT_MAX_ALPHA
    } else {
      0f
    }

  return ReaderAmbientBackdropSpec(
    imageUrl = articleImage,
    maxAlpha = articleAlpha,
    allowTonalFallback = false,
  )
}

internal fun resolveReaderBackdropScrollVisibility(
  firstVisibleItemIndex: Int,
  firstVisibleItemScrollOffset: Int,
  fadeDistancePx: Float,
): Float {
  if (fadeDistancePx <= 0f || firstVisibleItemIndex > 0) return 0f
  return (1f - (firstVisibleItemScrollOffset / fadeDistancePx)).coerceIn(0f, 1f)
}

internal fun resolveReaderBackdropScrollVisibility(
  scrollOffsetPx: Int,
  fadeDistancePx: Float,
): Float {
  if (fadeDistancePx <= 0f) return 0f
  return (1f - (scrollOffsetPx / fadeDistancePx)).coerceIn(0f, 1f)
}

internal fun resolveReaderAmbientBlendState(
  exactPage: Float,
  itemCount: Int,
  targetForPage: (Int) -> ReaderAmbientPageTarget,
): ReaderAmbientBlendState {
  if (itemCount <= 0) {
    return ReaderAmbientBlendState(
      containerBlurredAlpha = 0f,
      fallbackAlpha = 0f,
      leftPage = -1,
      leftImageAlpha = 0f,
      rightPage = -1,
      rightImageAlpha = 0f,
    )
  }

  val clampedExact = exactPage.coerceIn(0f, (itemCount - 1).toFloat())
  val leftPage = floor(clampedExact).toInt().coerceIn(0, itemCount - 1)
  val rightPage = (leftPage + 1).coerceIn(0, itemCount - 1)

  if (leftPage == rightPage) {
    val target = targetForPage(leftPage)
    val blurred = target.blurredAlpha.coerceIn(0f, 1f)
    val fallback = target.fallbackAlpha.coerceIn(0f, 1f)
    return ReaderAmbientBlendState(
      containerBlurredAlpha = blurred,
      fallbackAlpha = fallback,
      leftPage = leftPage,
      leftImageAlpha = if (blurred > 0f) 1f else 0f,
      rightPage = rightPage,
      rightImageAlpha = 0f,
    )
  }

  val fraction = (clampedExact - leftPage).coerceIn(0f, 1f)
  val left = targetForPage(leftPage)
  val right = targetForPage(rightPage)

  val wL = left.blurredAlpha.coerceIn(0f, 1f) * (1f - fraction)
  val wR = right.blurredAlpha.coerceIn(0f, 1f) * fraction
  val total = wL + wR
  val containerBlurredAlpha = total.coerceIn(0f, 1f)
  val fallbackAlpha =
    (left.fallbackAlpha.coerceIn(0f, 1f) * (1f - fraction) +
        right.fallbackAlpha.coerceIn(0f, 1f) * fraction)
      .coerceIn(0f, 1f)

  val (leftImageAlpha, rightImageAlpha) =
    when {
      total <= 0f -> 0f to 0f
      wL <= 0f -> 0f to 1f
      wR <= 0f -> 1f to 0f
      else -> 1f to (wR / total)
    }

  return ReaderAmbientBlendState(
    containerBlurredAlpha = containerBlurredAlpha,
    fallbackAlpha = fallbackAlpha,
    leftPage = leftPage,
    leftImageAlpha = leftImageAlpha,
    rightPage = rightPage,
    rightImageAlpha = rightImageAlpha,
  )
}

@Composable
internal fun ReaderAmbientBackdrop(
  posts: LazyPagingItems<ResolvedPost>,
  pagerState: PagerState,
  isDarkTheme: Boolean,
  enrichedPodcastArtworkUrls: Map<String, String>,
  scrollVisibilityProviders: Map<String, () -> Float>,
  modifier: Modifier = Modifier,
) {
  val itemCount = posts.itemCount
  if (itemCount <= 0) return

  val shouldBlockImage = LocalBlockImage.current
  val blurEffect = rememberAmbientBlurEffect()
  val colorFilter = rememberAmbientImageColorFilter()

  val backgroundPageRange by
    remember(itemCount, pagerState) {
      computedStateOf {
        val currentPage = pagerState.currentPage.coerceIn(0, itemCount - 1)
        val firstPage = (currentPage - 1).coerceAtLeast(0)
        val lastPage = (currentPage + 1).coerceAtMost(itemCount - 1)
        firstPage..lastPage
      }
    }

  val pageEntries = buildList {
    for (page in backgroundPageRange) {
      val post = runCatching { posts.peek(page) }.getOrNull() ?: continue
      val enrichedUrl = enrichedPodcastArtworkUrls[post.id]
      val spec =
        post.resolveReaderAmbientBackdropSpec(
          isDarkTheme = isDarkTheme,
          enrichedCoverArtUrl = enrichedUrl,
        )
      add(ReaderAmbientPageEntry(page = page, postId = post.id, spec = spec))
    }
  }

  val pageEntriesState = remember { mutableStateOf(pageEntries) }
  SideEffect {
    if (pageEntriesState.value != pageEntries) {
      pageEntriesState.value = pageEntries
    }
  }

  val loadedPostIds = remember { mutableStateMapOf<String, Unit>() }
  val canBlur =
    shouldRenderBlurredBackdrop(
      canBlur = canBlurImage,
      hasBlurEffect = blurEffect != null,
      imageUrl = "placeholder",
      blockImages = shouldBlockImage,
    )

  val computeBlendState =
    remember(pagerState, scrollVisibilityProviders, canBlur) {
      {
        val itemCount = pagerState.pageCount
        val entries = pageEntriesState.value
        val exactPage = pagerState.currentPage + pagerState.currentPageOffsetFraction
        resolveReaderAmbientBlendState(exactPage = exactPage, itemCount = itemCount) { page ->
          val entry =
            entries.firstOrNull { it.page == page }
              ?: return@resolveReaderAmbientBlendState ReaderAmbientPageTarget(0f, 0f)
          val scrollVis = scrollVisibilityProviders[entry.postId]?.invoke() ?: 1f
          val effectiveMax = (entry.spec.maxAlpha * scrollVis).coerceIn(0f, 1f)
          val canRenderImage = canBlur && !entry.spec.imageUrl.isNullOrBlank()
          val isLoaded = canRenderImage && loadedPostIds.containsKey(entry.postId)
          val blurredAlpha = if (canRenderImage) effectiveMax else 0f
          val fallbackAlpha =
            if (entry.spec.allowTonalFallback && !isLoaded) {
              effectiveMax
            } else {
              0f
            }
          ReaderAmbientPageTarget(blurredAlpha = blurredAlpha, fallbackAlpha = fallbackAlpha)
        }
      }
    }

  val containerBlurredAlphaProvider =
    remember(computeBlendState) { { computeBlendState().containerBlurredAlpha } }
  val fallbackAlphaProvider = remember(computeBlendState) { { computeBlendState().fallbackAlpha } }
  val hasVisibleBlurredContent =
    remember(computeBlendState) {
      {
        val blend = computeBlendState()
        val entries = pageEntriesState.value
        val leftLoaded =
          blend.leftImageAlpha > 0f &&
            entries
              .firstOrNull { it.page == blend.leftPage }
              ?.let { loadedPostIds.containsKey(it.postId) } == true
        val rightLoaded =
          blend.rightImageAlpha > 0f &&
            entries
              .firstOrNull { it.page == blend.rightPage }
              ?.let { loadedPostIds.containsKey(it.postId) } == true
        blend.containerBlurredAlpha > 0f && (leftLoaded || rightLoaded)
      }
    }

  Box(modifier = modifier.clipToBounds()) {
    if (canBlur && blurEffect != null && pageEntries.any { !it.spec.imageUrl.isNullOrBlank() }) {
      AmbientBlurredBackdrop(
        blurEffect = blurEffect,
        hasVisibleContent = hasVisibleBlurredContent,
        applyBottomScrim = false,
        modifier = Modifier.matchParentSize(),
      ) {
        for (entry in pageEntries) {
          key(entry.postId) {
            val imageAlphaProvider =
              remember(entry.page, computeBlendState) {
                { computeBlendState().imageAlphaForPage(entry.page) }
              }
            val onLoad = remember(entry.postId) { { loadedPostIds[entry.postId] = Unit } }

            DisposableEffect(entry.postId) { onDispose { loadedPostIds.remove(entry.postId) } }

            ReaderAmbientBackdropImage(
              imageUrl = entry.spec.imageUrl,
              imageAlphaProvider = imageAlphaProvider,
              colorFilter = colorFilter,
              onLoad = onLoad,
            )
          }
        }
      }
    }

    ReaderAmbientScrimOverlay(
      alphaProvider = containerBlurredAlphaProvider,
      hasVisibleBlur = hasVisibleBlurredContent,
      modifier = Modifier.matchParentSize(),
    )

    if (pageEntries.any { it.spec.allowTonalFallback }) {
      AmbientTonalFallbackBackdrop(
        alphaProvider = fallbackAlphaProvider,
        modifier = Modifier.matchParentSize(),
      )
    }
  }
}

@Composable
private fun ReaderAmbientScrimOverlay(
  alphaProvider: () -> Float,
  hasVisibleBlur: () -> Boolean,
  modifier: Modifier = Modifier,
) {
  val colorScheme = AppTheme.colorScheme

  Spacer(
    modifier =
      modifier.drawWithCache {
        val scrimMask =
          smoothVerticalFade(
            color = Color.White,
            startY = size.height * 0.45f,
            endY = size.height,
            reversed = true,
          )
        var lastBackdrop = Color.Unspecified
        var cachedScrimFilter: ColorFilter? = null
        onDrawBehind {
          val backdrop = colorScheme.backdrop
          val containerAlpha = alphaProvider().coerceIn(0f, 1f)
          if (containerAlpha <= 0f || !hasVisibleBlur()) {
            drawRect(color = backdrop)
            return@onDrawBehind
          }
          if (containerAlpha < 1f) {
            drawRect(color = backdrop, alpha = 1f - containerAlpha)
          }
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
  )
}

@Composable
private fun BoxScope.ReaderAmbientBackdropImage(
  imageUrl: String?,
  imageAlphaProvider: () -> Float,
  colorFilter: ColorFilter,
  onLoad: () -> Unit,
) {
  if (imageUrl.isNullOrBlank()) return

  val context = LocalPlatformContext.current
  val model =
    remember(context, imageUrl) {
      val memoryCacheKey = "$imageUrl#${AmbientBlurImageSize.width}x${AmbientBlurImageSize.height}"
      ImageRequest.Builder(context)
        .data(imageUrl)
        .size(AmbientBlurImageSize)
        .diskCacheKey(imageUrl)
        .memoryCacheKey(memoryCacheKey)
        .disableAnimation()
        .crossfade(false)
        .build()
    }
  val onState =
    remember(onLoad) {
      { state: AsyncImagePainter.State ->
        if (state is AsyncImagePainter.State.Success) {
          onLoad()
        }
      }
    }

  CoilAsyncImage(
    model = model,
    modifier =
      Modifier.matchParentSize().graphicsLayer {
        compositingStrategy = CompositingStrategy.ModulateAlpha
        alpha = imageAlphaProvider()
      },
    contentDescription = null,
    contentScale = widthBiasedScale,
    colorFilter = colorFilter,
    onState = onState,
  )
}
