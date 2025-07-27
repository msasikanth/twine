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
package dev.sasikanth.rss.reader.home.ui

import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun FeaturedSection(
  paddingValues: PaddingValues,
  featuredPosts: ImmutableList<FeaturedPostItem>,
  pagerState: PagerState,
  useDarkTheme: Boolean,
  modifier: Modifier = Modifier,
  onItemClick: (PostWithMetadata, postIndex: Int) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
  onTogglePostReadClick: (String, Boolean) -> Unit,
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
      remember(systemBarsHorizontalPadding) {
        PaddingValues(
          start = systemBarsHorizontalPadding + 24.dp,
          end = systemBarsHorizontalPadding + 8.dp,
          bottom = 24.dp
        )
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      key = { page ->
        val post = featuredPosts.getOrNull(page)
        post?.let { post.postWithMetadata.id + post.postWithMetadata.sourceId } ?: page
      },
      flingBehavior =
        PagerDefaults.flingBehavior(
          state = pagerState,
          snapAnimationSpec = spring(stiffness = Spring.StiffnessVeryLow)
        ),
    ) { page ->
      val featuredPost = featuredPosts.getOrNull(page)
      if (featuredPost != null) {
        val postWithMetadata = featuredPost.postWithMetadata

        Box {
          if (canBlurImage) {
            FeaturedSectionBackground(
              featuredPost = featuredPost,
              useDarkTheme = useDarkTheme,
              pageOffset = { pagerState.getOffsetFractionForPage(page) },
            )
          }

          FeaturedPostItem(
            modifier =
              Modifier.padding(top = paddingValues.calculateTopPadding() + 8.dp, end = 16.dp),
            item = postWithMetadata,
            pageOffset = { pagerState.getOffsetFractionForPage(page) },
            onClick = { onItemClick(postWithMetadata, page) },
            onBookmarkClick = { onPostBookmarkClick(postWithMetadata) },
            onCommentsClick = { onPostCommentsClick(postWithMetadata.commentsLink!!) },
            onSourceClick = { onPostSourceClick(postWithMetadata.sourceId) },
            onTogglePostReadClick = {
              onTogglePostReadClick(postWithMetadata.id, postWithMetadata.read)
            }
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
        pageIndicatorState = pageIndicatorState
      )
    }
  }
}

@Composable
private fun FeaturedSectionBackground(
  featuredPost: FeaturedPostItem,
  useDarkTheme: Boolean,
  pageOffset: () -> Float,
  modifier: Modifier = Modifier,
) {
  val sizeClass = LocalWindowSizeClass.current.widthSizeClass
  val imageMaxHeight =
    if (sizeClass >= WindowWidthSizeClass.Medium) {
      198.dp
    } else {
      Dp.Unspecified
    }

  val gradientOverlayModifier =
    if (useDarkTheme) {
      Modifier.drawWithCache {
        val gradientColor = Color.Black
        val radialGradient =
          Brush.radialGradient(
            colors =
              listOf(
                gradientColor,
                gradientColor.copy(alpha = 0.0f),
                gradientColor.copy(alpha = 0.0f)
              ),
            center = Offset(x = this.size.width, y = 40f),
            tileMode = TileMode.Decal,
          )

        val linearGradient =
          Brush.verticalGradient(
            colors = listOf(gradientColor, gradientColor.copy(alpha = 0.0f)),
            tileMode = TileMode.Decal,
          )

        onDrawWithContent {
          drawContent()
          drawRect(radialGradient)
          drawRect(linearGradient)
        }
      }
    } else {
      Modifier
    }

  val overlayColor = AppTheme.colorScheme.inversePrimary
  val colorMatrix =
    remember(useDarkTheme) {
      ColorMatrix().apply {
        val sat = if (useDarkTheme) 1f else 5f
        setToSaturation(sat)
      }
    }

  AsyncImage(
    url = featuredPost.postWithMetadata.imageUrl!!,
    modifier =
      Modifier.then(modifier)
        .fillMaxWidth()
        .heightIn(max = imageMaxHeight)
        .aspectRatio(1f)
        .graphicsLayer { translationX = size.width * pageOffset.invoke() }
        .graphicsLayer {
          val blurRadius = 100.dp

          alpha = calculateAlpha(pageOffset)
          renderEffect =
            BlurEffect(
              radiusX = blurRadius.toPx(),
              radiusY = blurRadius.toPx(),
              edgeTreatment = TileMode.Decal,
            )
        }
        .then(gradientOverlayModifier)
        .drawWithContent {
          drawContent()
          drawRect(
            color = overlayColor,
            blendMode = BlendMode.Luminosity,
          )
        },
    contentDescription = null,
    colorFilter = ColorFilter.colorMatrix(colorMatrix),
    contentScale = ContentScale.Crop,
  )
}

private fun calculateAlpha(pageOffset: () -> Float): Float {
  val offsetAbsolute = minOf(1f, pageOffset().absoluteValue)
  return EaseInSine.transform(offsetAbsolute.inverse())
}
