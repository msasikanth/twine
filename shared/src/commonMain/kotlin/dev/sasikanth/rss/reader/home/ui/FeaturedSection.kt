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

import androidx.compose.animation.core.EaseInSine
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.computedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage as CoilAsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.components.AmbientBlurredBackdrop
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.components.rememberAmbientBlurEffect
import dev.sasikanth.rss.reader.components.rememberAmbientImageColorFilter
import dev.sasikanth.rss.reader.core.model.local.FeaturedPostItem
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.LocalBlockImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.ParallaxAlignment
import dev.sasikanth.rss.reader.utils.disableAnimation
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FeaturedSection(
  paddingValues: PaddingValues,
  featuredPosts: ImmutableList<FeaturedPostItem>,
  pagerState: PagerState,
  postsType: PostsType,
  markAsReadOn: MarkAsReadOn,
  modifier: Modifier = Modifier,
  markFeaturedPostAsReadOnScroll: (String) -> Unit,
  onItemClick: (ResolvedPost, postIndex: Int) -> Unit,
  onPostBookmarkClick: (ResolvedPost) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
  updateReadStatus: (id: String, updatedReadStatus: Boolean) -> Unit,
) {
  val layoutDirection = LocalLayoutDirection.current
  val density = LocalDensity.current
  val sizeClass = LocalWindowSizeClass.current

  val systemBars = WindowInsets.systemBars
  val systemBarsPaddingValues =
    remember(systemBars, density) {
      systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues(density)
    }
  val systemBarsHorizontalPadding =
    remember(systemBarsPaddingValues, layoutDirection) {
      val systemBarsStartPadding = systemBarsPaddingValues.calculateStartPadding(layoutDirection)
      val systemBarsEndPadding = systemBarsPaddingValues.calculateEndPadding(layoutDirection)

      if (systemBarsStartPadding > systemBarsEndPadding) {
        systemBarsStartPadding
      } else {
        systemBarsEndPadding
      }
    }

  val blurEffect = rememberAmbientBlurEffect()

  val imageAspectRatio =
    remember(sizeClass) {
      when {
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 2f
        sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 1.5f
        else -> 1f
      }
    }

  val shouldBlockImage = LocalBlockImage.current
  val backgroundImageColorFilter = rememberAmbientImageColorFilter()

  val isFeaturedVisibleState = remember { mutableStateOf(true) }

  Box(
    modifier.onVisibilityChanged(minFractionVisible = 0.05f) { isFeaturedVisibleState.value = it }
  ) {
    if (canBlurImage && blurEffect != null && featuredPosts.isNotEmpty()) {
      // Only the current page and its immediate neighbors can be visible during a swipe.
      val backgroundPageRange by
        remember(featuredPosts, pagerState) {
          computedStateOf {
            val currentPage = pagerState.currentPage
            val firstPage = (currentPage - 1).coerceAtLeast(0)
            val lastPage = (currentPage + 1).coerceAtMost(featuredPosts.lastIndex)
            firstPage..lastPage
          }
        }

      val hasBackgroundImage =
        backgroundPageRange.any { featuredPosts[it].resolvedPost.imageUrl != null }

      if (hasBackgroundImage) {
        val alphaForPage =
          remember(pagerState, postsType, markAsReadOn) {
            { page: Int ->
              val pageOffset = pagerState.getOffsetFractionForPage(page)
              if (postsType == PostsType.UNREAD && markAsReadOn == MarkAsReadOn.Scroll) {
                calculateContentAlpha(pageOffset)
              } else {
                calculateBackgroundAlpha(pageOffset)
              }
            }
          }

        val loadedBackgroundImages = remember { mutableStateMapOf<String, Unit>() }
        val hasVisibleBackgroundImageState =
          remember(featuredPosts, alphaForPage, shouldBlockImage) {
            computedStateOf {
              shouldBlockImage ||
                backgroundPageRange.any { page ->
                  loadedBackgroundImages.containsKey(featuredPosts[page].resolvedPost.id) &&
                    alphaForPage(page) > 0f
                }
            }
          }
        val hasVisibleBackgroundImage =
          remember(hasVisibleBackgroundImageState) { { hasVisibleBackgroundImageState.value } }

        AmbientBlurredBackdrop(
          blurEffect = blurEffect,
          hasVisibleContent = hasVisibleBackgroundImage,
          modifier = Modifier.aspectRatio(imageAspectRatio),
        ) {
          for (page in backgroundPageRange) {
            val featuredPost = featuredPosts[page]
            val postId = featuredPost.resolvedPost.id
            key(postId) {
              val alphaProvider = remember(page, alphaForPage) { { alphaForPage(page) } }

              DisposableEffect(postId) { onDispose { loadedBackgroundImages.remove(postId) } }

              FeaturedSectionBackgroundImage(
                imageUrl = featuredPost.resolvedPost.imageUrl,
                alphaProvider = alphaProvider,
                colorFilter = backgroundImageColorFilter,
                onLoad = { loadedBackgroundImages[postId] = Unit },
              )
            }
          }
        }
      }
    }

    val contentPadding =
      remember(systemBarsHorizontalPadding, featuredPosts.isNotEmpty()) {
        PaddingValues(
          start = systemBarsHorizontalPadding + 16.dp,
          end = systemBarsHorizontalPadding + 16.dp,
          bottom = if (featuredPosts.isNotEmpty()) 24.dp else 0.dp,
        )
      }

    val featuredPostKey =
      remember(featuredPosts) {
        { page: Int ->
          featuredPosts.getOrNull(page)?.let { PostListKey.encode(it.resolvedPost) } ?: page
        }
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      beyondViewportPageCount = 1,
      key = featuredPostKey,
    ) { page ->
      val featuredPost = featuredPosts.getOrNull(page)
      if (featuredPost != null) {
        val postWithMetadata = featuredPost.resolvedPost
        val imageAlignment =
          remember(page, pagerState) {
            ParallaxAlignment(
              horizontalBias = { pagerState.getOffsetFractionForPage(page) },
              multiplier = 2f,
            )
          }

        val contentAlphaProvider =
          remember(page, pagerState) {
            {
              val pageOffset = pagerState.getOffsetFractionForPage(page)
              calculateContentAlpha(pageOffset)
            }
          }

        val isActiveAndSettled =
          remember(page, pagerState) {
            {
              isFeaturedVisibleState.value &&
                !pagerState.isScrollInProgress &&
                pagerState.currentPage == page
            }
          }

        Box(
          modifier =
            Modifier.onVisibilityChanged(minDurationMs = 500) {
              val previousFeaturedPost =
                featuredPosts.getOrNull(page - 1) ?: return@onVisibilityChanged

              if (previousFeaturedPost.resolvedPost.read) return@onVisibilityChanged

              if (it) {
                markFeaturedPostAsReadOnScroll(previousFeaturedPost.resolvedPost.id)
              }
            }
        ) {
          FeaturedPostItem(
            item = postWithMetadata,
            contentAlphaProvider = contentAlphaProvider,
            onClick = { onItemClick(postWithMetadata, page) },
            onBookmarkClick = { onPostBookmarkClick(postWithMetadata) },
            onCommentsClick = { onPostCommentsClick(postWithMetadata.commentsLink!!) },
            onSourceClick = { onPostSourceClick(postWithMetadata.sourceId) },
            updateReadStatus = { updatedReadStatus ->
              updateReadStatus(postWithMetadata.id, updatedReadStatus)
            },
            modifier =
              Modifier.padding(top = paddingValues.calculateTopPadding())
                .padding(horizontal = 6.dp),
            {
              FeaturedImage(
                imageUrl = postWithMetadata.imageUrl,
                alignment = imageAlignment,
                isActiveAndSettled = isActiveAndSettled,
              )
            },
          )
        }
      }
    }

    if (pagerState.pageCount > 1) {
      val pageIndicatorState =
        remember(pagerState) {
          object : PageIndicatorState {
            override val pageOffset: Float
              get() = pagerState.currentPageOffsetFraction

            override val selectedPage: Int
              get() = pagerState.currentPage

            override val pageCount: Int
              get() = pagerState.pageCount
          }
        }

      HorizontalPageIndicators(
        modifier = Modifier.padding(vertical = 8.dp).align(Alignment.BottomCenter),
        pageIndicatorState = pageIndicatorState,
      )
    }
  }
}

@Composable
private fun BoxScope.FeaturedSectionBackgroundImage(
  imageUrl: String?,
  alphaProvider: () -> Float,
  colorFilter: ColorFilter?,
  onLoad: () -> Unit,
) {
  if (imageUrl == null || LocalBlockImage.current) return

  val context = LocalPlatformContext.current
  val model =
    remember(context, imageUrl) {
      val memoryCacheKey =
        "$imageUrl#${BackgroundBlurImageSize.width}x${BackgroundBlurImageSize.height}"
      ImageRequest.Builder(context)
        .data(imageUrl)
        .size(BackgroundBlurImageSize)
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
        alpha = alphaProvider()
      },
    contentDescription = null,
    contentScale = widthBiasedScale,
    colorFilter = colorFilter,
    onState = onState,
  )
}

private fun calculateBackgroundAlpha(pageOffset: Float): Float {
  return when {
    // Fully swiped-past pages sit behind the current page's opaque background;
    // hide them instead of drawing invisible blurred layers.
    pageOffset >= 1f -> 0f
    pageOffset >= 0f -> 1f
    else -> calculateContentAlpha(pageOffset)
  }
}

private fun calculateContentAlpha(pageOffset: Float): Float {
  val offsetAbsolute = minOf(1f, pageOffset.absoluteValue)
  return EaseInSine.transform(offsetAbsolute.inverse())
}

private val BackgroundBlurImageSize = Size(Dimension.Undefined, height = 100) // Downscaled for blur
