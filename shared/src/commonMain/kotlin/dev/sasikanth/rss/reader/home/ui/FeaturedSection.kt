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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.lerp
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
            val offset = pagerState.getOffsetFractionForPage(settledPage).coerceIn(-1f, 1f)

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

      FeaturedSectionBlurredBackground(featuredPosts = featuredPosts, pagerState = pagerState)

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedSectionBlurredBackground(
  modifier: Modifier = Modifier,
  featuredPosts: ImmutableList<PostWithMetadata>,
  pagerState: PagerState
) {
  BoxWithConstraints(modifier = modifier) {
    if (canBlurImage) {
      featuredPosts.fastForEachReversed { post ->
        val actualIndex = featuredPosts.indexOf(post)

        AsyncImage(
          url = post.imageUrl!!,
          modifier =
            Modifier.aspectRatio(featuredImageBackgroundAspectRatio)
              .blur(100.dp, BlurredEdgeTreatment.Unbounded)
              .alpha(actualIndex, pagerState),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          size = IntSize(128, 128)
        )
      }
    } else {
      Box(
        modifier =
          Modifier.aspectRatio(featuredGradientBackgroundAspectRatio).composed {
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

            background(Brush.verticalGradient(colorStops))
          }
      )
    }

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.radialGradient(
                colors =
                  listOf(
                    Color.Black,
                    Color.Black.copy(alpha = 0.0f),
                    Color.Black.copy(alpha = 0.0f)
                  ),
                center = Offset(x = constraints.maxWidth.toFloat(), y = 40f)
              )
          )
    )

    Box(
      modifier =
        Modifier.matchParentSize()
          .background(
            brush =
              Brush.verticalGradient(
                colors = listOf(Color.Black, Color.Black.copy(alpha = 0.0f)),
              )
          )
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.alpha(index: Int, pagerState: PagerState): Modifier {
  val settledPage = pagerState.settledPage
  val offsetFraction = pagerState.getOffsetFractionForPage(settledPage)

  return graphicsLayer {
    alpha =
      when {
        index == settledPage -> lerp(1f, 0f, offsetFraction)
        index == settledPage - 1 && offsetFraction < -EPSILON ->
          lerp(0f, 1f, offsetFraction.absoluteValue)
        index < settledPage -> 0f
        else -> 1f
      }
  }
}
