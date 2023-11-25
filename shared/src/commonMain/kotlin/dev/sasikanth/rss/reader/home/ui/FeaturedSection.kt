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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import dev.sasikanth.rss.reader.components.LocalDynamicColorState
import dev.sasikanth.rss.reader.components.image.AsyncImage
import dev.sasikanth.rss.reader.models.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.Constants.EPSILON
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
import dev.sasikanth.rss.reader.utils.canBlurImage
import kotlin.math.absoluteValue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest

private val featuredImageBackgroundAspectRatio: Float
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 1.1f
      WindowWidthSizeClass.Medium -> 1.55f
      WindowWidthSizeClass.Expanded -> 3.1f
      else -> 1.1f
    }

private val featuredGradientBackgroundAspectRatio: Float
  @Composable
  @ReadOnlyComposable
  get() =
    when (LocalWindowSizeClass.current.widthSizeClass) {
      WindowWidthSizeClass.Compact -> 0.8f
      WindowWidthSizeClass.Medium -> 1.11f
      WindowWidthSizeClass.Expanded -> 2.3f
      else -> 0.8f
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FeaturedSection(
  paddingValues: PaddingValues,
  featuredPosts: ImmutableList<PostWithMetadata>,
  pagerState: PagerState,
  featuredItemBlurEnabled: Boolean,
  modifier: Modifier = Modifier,
  onItemClick: (PostWithMetadata) -> Unit,
  onPostBookmarkClick: (PostWithMetadata) -> Unit,
  onPostCommentsClick: (String) -> Unit,
  onPostSourceClick: (String) -> Unit,
) {
  Box(modifier = modifier) {
    if (featuredPosts.isNotEmpty()) {
      val layoutDirection = LocalLayoutDirection.current
      val dynamicColorState = LocalDynamicColorState.current

      val systemBarsPaddingValues =
        WindowInsets.systemBars.only(WindowInsetsSides.Horizontal).asPaddingValues()
      val startPadding = systemBarsPaddingValues.calculateStartPadding(layoutDirection)
      val endPadding = systemBarsPaddingValues.calculateEndPadding(layoutDirection)

      val horizontalPadding =
        if (startPadding > endPadding) {
          startPadding
        } else {
          endPadding
        }

      val pagerContentPadding =
        PaddingValues(
          start = horizontalPadding + 24.dp,
          top = 8.dp + paddingValues.calculateTopPadding(),
          end = horizontalPadding + 24.dp,
          bottom = 24.dp
        )

      LaunchedEffect(pagerState, featuredPosts) {
        dynamicColorState.onContentChange(featuredPosts.map { it.imageUrl!! })

        snapshotFlow {
            val settledPage = pagerState.settledPage
            val offset =
              if (settledPage in 0..pagerState.pageCount) {
                pagerState.getOffsetFractionForPage(settledPage).coerceIn(-1f, 1f)
              } else {
                0f
              }

            settledPage to
              when {
                (settledPage == 0 && offset < -EPSILON) ||
                  (settledPage == featuredPosts.lastIndex && offset > EPSILON) -> {
                  offset.coerceAtMost(0f)
                }
                else -> offset
              }
          }
          .collectLatest { (settledPage, offset) ->
            val previousImageUrl = featuredPosts.getOrNull(settledPage - 1)?.imageUrl
            val currentImageUrl = featuredPosts[settledPage].imageUrl!!
            val nextImageUrl = featuredPosts.getOrNull(settledPage + 1)?.imageUrl

            dynamicColorState.updateOffset(previousImageUrl, currentImageUrl, nextImageUrl, offset)
          }
      }

      FeaturedSectionBackground(
        featuredPosts = featuredPosts,
        pagerState = pagerState,
        featuredItemBlurEnabled = featuredItemBlurEnabled
      )

      HorizontalPager(
        state = pagerState,
        contentPadding = pagerContentPadding,
        pageSpacing = 16.dp,
        verticalAlignment = Alignment.Top,
        flingBehavior =
          PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessVeryLow)
          )
      ) { page ->
        val featuredPost = featuredPosts[page]

        FeaturedPostItem(
          item = featuredPost,
          page = page,
          pagerState = pagerState,
          onClick = { onItemClick(featuredPost) },
          onBookmarkClick = { onPostBookmarkClick(featuredPost) },
          onCommentsClick = { onPostCommentsClick(featuredPost.commentsLink!!) },
          onSourceClick = { onPostSourceClick(featuredPost.feedLink) }
        )
      }
    }
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun FeaturedSectionBackground(
  pagerState: PagerState,
  featuredPosts: ImmutableList<PostWithMetadata>,
  featuredItemBlurEnabled: Boolean,
  modifier: Modifier = Modifier,
) {
  val gradientOverlayModifier =
    Modifier.drawWithCache {
      val radialGradient =
        Brush.radialGradient(
          colors =
            listOf(Color.Black, Color.Black.copy(alpha = 0.0f), Color.Black.copy(alpha = 0.0f)),
          center = Offset(x = this.size.width, y = 40f)
        )

      val linearGradient =
        Brush.verticalGradient(
          colors = listOf(Color.Black, Color.Black.copy(alpha = 0.0f)),
        )

      onDrawWithContent {
        drawContent()
        drawRect(radialGradient)
        drawRect(linearGradient)
      }
    }

  Box(modifier = modifier.then(gradientOverlayModifier)) {
    if (canBlurImage && featuredItemBlurEnabled) {
      FeaturedSectionBlurredBackground(featuredPosts = featuredPosts, pagerState = pagerState)
    } else {
      FeaturedSectionGradientBackground()
    }
  }
}

@Composable
private fun FeaturedSectionGradientBackground(modifier: Modifier = Modifier) {
  val colorStops =
    listOf(
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.0f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.33f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.50f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.70f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.60f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.33f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.10f),
      AppTheme.colorScheme.tintedHighlight.copy(alpha = 0.0f),
    )

  Box(
    modifier =
      modifier
        .aspectRatio(featuredGradientBackgroundAspectRatio)
        .background(Brush.verticalGradient(colorStops))
  )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun FeaturedSectionBlurredBackground(
  featuredPosts: ImmutableList<PostWithMetadata>,
  pagerState: PagerState,
  modifier: Modifier = Modifier
) {
  // We are loading all featured posts images at once to avoid blinking issues that can occur
  // due to state changes when we try to do this lazily. Since the alpha is set to 0 for images that
  // don't need to be rendered, they are not drawn. If need more featured posts, we can convert this
  // to a proper lazy layout. But for 6 items, this is the simplest approach to take.
  featuredPosts.fastForEachIndexed { index, post ->
    AsyncImage(
      url = post.imageUrl!!,
      modifier =
        modifier.aspectRatio(featuredImageBackgroundAspectRatio).graphicsLayer {
          val offsetFraction =
            if (index in 0..pagerState.pageCount) {
              pagerState.getOffsetFractionForPage(index).absoluteValue.coerceIn(0f, 1f)
            } else {
              0f
            }
          alpha = ((1f - offsetFraction) / 1f)

          val blurRadiusInPx = 100.dp.toPx()
          // Since blur can be expensive memory wise, there is no point blurring images when not
          // needed.
          renderEffect =
            if (index in pagerState.settledPage - 1..pagerState.settledPage + 1) {
              BlurEffect(blurRadiusInPx, blurRadiusInPx, TileMode.Decal)
            } else {
              null
            }
          shape = RectangleShape
          clip = false
        },
      contentDescription = null,
      contentScale = ContentScale.Crop,
      size = IntSize(128, 128),
      backgroundColor = AppTheme.colorScheme.surfaceContainerLowest
    )
  }
}
