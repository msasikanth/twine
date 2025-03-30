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

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.requiredHeightIn
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import coil3.size.Size
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.ui.LocalDynamicColorState
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.getOffsetFractionForPage
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
      remember(systemBarsHorizontalPadding, paddingValues) {
        PaddingValues(
          start = systemBarsHorizontalPadding + 24.dp,
          top = 8.dp + paddingValues.calculateTopPadding(),
          end = systemBarsHorizontalPadding + 24.dp,
          bottom = 24.dp
        )
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      pageSpacing = 16.dp,
      flingBehavior =
        PagerDefaults.flingBehavior(
          state = pagerState,
          snapAnimationSpec = spring(stiffness = Spring.StiffnessVeryLow)
        ),
    ) { page ->
      val featuredPost = featuredPosts.getOrNull(page)
      if (featuredPost != null) {
        Box {
          FeaturedSectionBackground(
            modifier =
              Modifier.fillMaxWidth().layout { measurable, constraints ->
                val topPadding = contentPadding.calculateTopPadding().roundToPx()
                val startPadding = contentPadding.calculateStartPadding(layoutDirection)
                val endPadding = contentPadding.calculateEndPadding(layoutDirection)
                val horizontalContentPadding = startPadding + endPadding

                val fullWidth = constraints.maxWidth + horizontalContentPadding.roundToPx()
                val placeable = measurable.measure(constraints.copy(maxWidth = fullWidth))

                layout(placeable.width, placeable.height) {
                  placeable.place(0, topPadding.unaryMinus())
                }
              },
            state = pagerState,
            page = page,
            featuredPost = featuredPost,
            useDarkTheme = useDarkTheme,
          )

          val postWithMetadata = featuredPost.postWithMetadata
          FeaturedPostItem(
            item = postWithMetadata,
            page = page,
            pagerState = pagerState,
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
  }
}

@Composable
private fun FeaturedSectionBackground(
  state: PagerState,
  page: Int,
  featuredPost: FeaturedPostItem,
  useDarkTheme: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(modifier) {
    val gradientOverlayModifier =
      if (useDarkTheme) {
        Modifier.drawWithCache {
          val gradientColor = if (useDarkTheme) Color.Black else Color.White
          val radialGradient =
            Brush.radialGradient(
              colors =
                listOf(
                  gradientColor,
                  gradientColor.copy(alpha = 0.0f),
                  gradientColor.copy(alpha = 0.0f)
                ),
              center = Offset(x = this.size.width, y = 40f)
            )

          val linearGradient =
            Brush.verticalGradient(
              colors = listOf(gradientColor, gradientColor.copy(alpha = 0.0f)),
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

    val swipeTransitionModifier =
      Modifier.graphicsLayer {
          val pageOffset = state.getOffsetFractionForPage(page)
          val offsetAbsolute = minOf(1f, pageOffset.absoluteValue)
          val backgroundAlpha =
            CubicBezierEasing(0.4f, 0.0f, 0.2f, 1.0f).transform(1f - offsetAbsolute)

          alpha = backgroundAlpha
          translationX = size.width * pageOffset
        }
        .then(gradientOverlayModifier)

    if (canBlurImage) {
      FeaturedSectionBlurredBackground(
        post = featuredPost,
        darkTheme = useDarkTheme,
        modifier = swipeTransitionModifier
      )
    } else {
      FeaturedSectionGradientBackground(modifier = swipeTransitionModifier)
    }
  }
}

@Composable
private fun FeaturedSectionGradientBackground(modifier: Modifier = Modifier) {
  val colorStops =
    listOf(
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.0f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.33f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.50f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.70f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.60f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.33f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.10f),
      AppTheme.colorScheme.primaryContainer.copy(alpha = 0.0f),
    )

  Box(
    modifier =
      Modifier.then(modifier)
        .requiredHeightIn(max = 800.dp)
        .aspectRatio(1f)
        .background(Brush.verticalGradient(colorStops))
  )
}

@Composable
private fun FeaturedSectionBlurredBackground(
  post: FeaturedPostItem,
  darkTheme: Boolean,
  modifier: Modifier = Modifier
) {
  val overlayColor = AppTheme.colorScheme.inversePrimary
  val colorMatrix = remember {
    ColorMatrix().apply {
      val sat = if (darkTheme) 1f else 5f
      setToSaturation(sat)
    }
  }

  AsyncImage(
    url = post.postWithMetadata.imageUrl!!,
    modifier =
      Modifier.then(modifier)
        .requiredHeightIn(max = 800.dp)
        .aspectRatio(1f)
        .graphicsLayer {
          val blurRadiusInPx = 100.dp.toPx()
          renderEffect = BlurEffect(blurRadiusInPx, blurRadiusInPx, TileMode.Decal)
          shape = RectangleShape
          clip = false
        }
        .drawWithContent {
          drawContent()

          drawRect(
            color = overlayColor,
            blendMode = BlendMode.Luminosity,
          )
        },
    contentDescription = null,
    contentScale = ContentScale.Crop,
    size = Size(128, 128),
    backgroundColor = AppTheme.colorScheme.surface,
    colorFilter = ColorFilter.colorMatrix(colorMatrix)
  )
}
