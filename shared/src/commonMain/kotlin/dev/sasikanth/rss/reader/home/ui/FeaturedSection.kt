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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
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
  val dynamicColorState = LocalDynamicColorState.current

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

  val defaultSeedColor = AppTheme.colorScheme.tintedForeground

  LaunchedEffect(pagerState, featuredPosts) {
    snapshotFlow {
        val settledPage = pagerState.settledPage
        try {
          pagerState.getOffsetFractionForPage(settledPage)
        } catch (e: Throwable) {
          0f
        }
      }
      .collect { offset ->
        if (featuredPosts.isEmpty()) return@collect

        // The default snap position of the pager is 0.5f, that means the targetPage
        // state only changes after reaching half way point. We instead want it to scale
        // as we start swiping.
        //
        // Instead of using EPSILON for snap threshold, we are doing that calculation
        // as the page offset changes
        //
        val currentItem = featuredPosts[pagerState.settledPage]
        val fromItem =
          if (offset < -EPSILON) {
            featuredPosts[pagerState.settledPage - 1]
          } else {
            currentItem
          }

        val toItem =
          if (offset > EPSILON) {
            featuredPosts[pagerState.settledPage + 1]
          } else {
            currentItem
          }

        val fromSeedColor =
          fromItem.seedColor.run { if (this != null) Color(this) else defaultSeedColor }
        val toSeedColor =
          toItem.seedColor.run { if (this != null) Color(this) else defaultSeedColor }

        dynamicColorState.animate(
          fromSeedColor = fromSeedColor,
          toSeedColor = toSeedColor,
          progress = offset
        )
      }
  }

  Box(modifier) {
    val contentPadding =
      remember(systemBarsHorizontalPadding) {
        PaddingValues(
          start = systemBarsHorizontalPadding,
          end = systemBarsHorizontalPadding,
          bottom = 24.dp
        )
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      key = { page -> featuredPosts.getOrNull(page)?.postWithMetadata?.id ?: page },
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
              Modifier.padding(horizontal = 24.dp)
                .padding(top = paddingValues.calculateTopPadding() + 8.dp),
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
