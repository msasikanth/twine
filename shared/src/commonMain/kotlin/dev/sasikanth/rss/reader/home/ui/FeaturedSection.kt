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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onVisibilityChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.size.Dimension
import coil3.size.Size
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.FeaturedPostItem
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.ParallaxAlignment
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.sasikanth.rss.reader.utils.ignoreHorizontalParentPadding
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

  val systemBarsPaddingValues =
    WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues()
  val systemBarsStartPadding = systemBarsPaddingValues.calculateStartPadding(layoutDirection)
  val systemBarsEndPadding = systemBarsPaddingValues.calculateEndPadding(layoutDirection)

  val systemBarsHorizontalPadding =
    if (systemBarsStartPadding > systemBarsEndPadding) {
      systemBarsStartPadding
    } else {
      systemBarsEndPadding
    }

  Box(modifier) {
    val contentPadding =
      remember(systemBarsHorizontalPadding, featuredPosts.isNotEmpty()) {
        PaddingValues(
          start = systemBarsHorizontalPadding + 16.dp,
          end = systemBarsHorizontalPadding + 16.dp,
          bottom = if (featuredPosts.isNotEmpty()) 24.dp else 0.dp,
        )
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      beyondViewportPageCount = 2,
      key = { page ->
        val post = featuredPosts.getOrNull(page)
        post?.let { PostListKey.from(post.resolvedPost).encode() } ?: page
      },
    ) { page ->
      val featuredPost = featuredPosts.getOrNull(page)
      if (featuredPost != null) {
        val postWithMetadata = featuredPost.resolvedPost
        val blurRadius = 100.dp
        val blurRadiusPx = with(LocalDensity.current) { blurRadius.toPx() }
        val blurEffect =
          remember(blurRadiusPx) {
            BlurEffect(
              radiusX = blurRadiusPx,
              radiusY = blurRadiusPx,
              edgeTreatment = TileMode.Decal,
            )
          }
        val imageAlignment =
          remember(page) {
            ParallaxAlignment(
              horizontalBias = { pagerState.getOffsetFractionForPage(page) },
              multiplier = 2f,
            )
          }

        Box(
          modifier =
            Modifier.onVisibilityChanged(minDurationMs = 500) {
              val previousFeaturedPost = featuredPosts.getOrNull(page - 1)

              if (previousFeaturedPost == null) return@onVisibilityChanged
              if (previousFeaturedPost.resolvedPost.read) return@onVisibilityChanged

              if (it) {
                markFeaturedPostAsReadOnScroll(previousFeaturedPost.resolvedPost.id)
              }
            }
        ) {
          if (canBlurImage) {
            FeaturedSectionBackground(
              imageUrl = postWithMetadata.imageUrl,
              modifier =
                Modifier.ignoreHorizontalParentPadding(horizontal = 24.dp).graphicsLayer {
                  val pageOffset = pagerState.getOffsetFractionForPage(page)

                  translationX = size.width * pageOffset
                  alpha =
                    if (postsType == PostsType.UNREAD && markAsReadOn == MarkAsReadOn.Scroll) {
                      calculateContentAlpha(pageOffset)
                    } else {
                      calculateBackgroundAlpha(pageOffset)
                    }
                  renderEffect = blurEffect
                },
            )
          }

          FeaturedPostItem(
            item = postWithMetadata,
            contentAlphaProvider = {
              val pageOffset = pagerState.getOffsetFractionForPage(page)
              calculateContentAlpha(pageOffset)
            },
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
            { FeaturedImage(imageUrl = postWithMetadata.imageUrl, alignment = imageAlignment) },
          )
        }
      }
    }

    if (pagerState.pageCount > 1) {
      val pageIndicatorState = remember {
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
private fun FeaturedSectionBackground(
  imageUrl: String?,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
) {
  val sizeClass = LocalWindowSizeClass.current
  val overlayColor = AppTheme.colorScheme.inversePrimary
  val imageAspectRatio =
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 2f
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 1.5f
      else -> 1f
    }
  val isDarkTheme = AppTheme.isDark
  val colorFilter =
    remember(isDarkTheme) {
      ColorFilter.colorMatrix(
        ColorMatrix().apply {
          val saturation = if (isDarkTheme) 1f else 1.1f
          setToSaturation(saturation)
        }
      )
    }

  if (imageUrl == null) return

  val downsampleFactor = 0.5f
  val baseHeight =
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) -> 360.dp
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> 250.dp
      else -> 200.dp
    }
  val targetHeight = (baseHeight * downsampleFactor).coerceAtLeast(120.dp)
  val density = LocalDensity.current
  val targetHeightPx =
    remember(targetHeight, density) { with(density) { targetHeight.roundToPx() } }
  val backgroundImageSize = remember(targetHeightPx) { Size(Dimension.Undefined, targetHeightPx) }

  AsyncImage(
    url = imageUrl,
    modifier =
      modifier.aspectRatio(imageAspectRatio).drawWithCache {
        onDrawWithContent {
          drawContent()
          drawRect(color = overlayColor, blendMode = BlendMode.Luminosity)
        }
      },
    contentDescription = null,
    contentScale = widthBiasedScale,
    alignment = alignment,
    colorFilter = colorFilter,
    size = backgroundImageSize,
  )
}

private fun calculateBackgroundAlpha(pageOffset: Float): Float {
  return if (pageOffset >= 0f) {
    1f
  } else {
    calculateContentAlpha(pageOffset)
  }
}

private fun calculateContentAlpha(pageOffset: Float): Float {
  val offsetAbsolute = minOf(1f, pageOffset.absoluteValue)
  return EaseInSine.transform(offsetAbsolute.inverse())
}
