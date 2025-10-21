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
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import dev.sasikanth.rss.reader.components.HorizontalPageIndicators
import dev.sasikanth.rss.reader.components.PageIndicatorState
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.util.canBlurImage
import dev.sasikanth.rss.reader.utils.LocalWindowSizeClass
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
          end = systemBarsHorizontalPadding + 24.dp,
          bottom = 24.dp
        )
      }

    HorizontalPager(
      state = pagerState,
      verticalAlignment = Alignment.Top,
      contentPadding = contentPadding,
      pageSpacing = 16.dp,
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
        var isImageRecorded by remember { mutableStateOf(false) }
        val imageGraphicsLayer = rememberGraphicsLayer()
        val blurRadius = 100.dp

        Box {
          if (canBlurImage) {
            FeaturedSectionBackground(
              modifier =
                Modifier.ignoreHorizontalParentPadding(horizontal = 24.dp).graphicsLayer {
                  val pageOffset = pagerState.getOffsetFractionForPage(page)

                  translationX = size.width * pageOffset
                  alpha = calculateAlpha(pageOffset)
                  renderEffect =
                    BlurEffect(
                      radiusX = blurRadius.toPx(),
                      radiusY = blurRadius.toPx(),
                      edgeTreatment = TileMode.Decal,
                    )
                },
              useDarkTheme = useDarkTheme,
            ) {
              if (isImageRecorded) {
                val imageWidth = imageGraphicsLayer.size.width
                val imageHeight = imageGraphicsLayer.size.height

                val canvasWidth = this.size.width
                val canvasHeight = this.size.height

                val scaleX = canvasWidth / imageWidth
                val scaleY = canvasHeight / imageHeight

                scale(
                  scaleX = scaleX,
                  scaleY = scaleY,
                  pivot = Offset.Zero,
                ) {
                  drawLayer(imageGraphicsLayer)
                }
              }
            }
          }

          FeaturedPostItem(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding() + 8.dp),
            item = postWithMetadata,
            darkTheme = useDarkTheme,
            onClick = { onItemClick(postWithMetadata, page) },
            onBookmarkClick = { onPostBookmarkClick(postWithMetadata) },
            onCommentsClick = { onPostCommentsClick(postWithMetadata.commentsLink!!) },
            onSourceClick = { onPostSourceClick(postWithMetadata.sourceId) },
            onTogglePostReadClick = {
              onTogglePostReadClick(postWithMetadata.id, postWithMetadata.read)
            }
          ) {
            FeaturedImage(
              modifier =
                Modifier.graphicsLayer {
                    val pageOffset = pagerState.getOffsetFractionForPage(page)
                    translationX = pageOffset * 350f
                    scaleX = 1.15f
                    scaleY = 1.15f
                  }
                  .drawWithContent {
                    imageGraphicsLayer.record { this@drawWithContent.drawContent() }
                    isImageRecorded = true

                    drawLayer(imageGraphicsLayer)
                  },
              image = postWithMetadata.imageUrl,
            )
          }
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
  useDarkTheme: Boolean,
  modifier: Modifier = Modifier,
  drawImage: DrawScope.() -> Unit,
) {
  val sizeClass = LocalWindowSizeClass.current.widthSizeClass
  val overlayColor = AppTheme.colorScheme.inversePrimary
  val imageAspectRatio =
    when {
      sizeClass >= WindowWidthSizeClass.Expanded -> 2f
      sizeClass >= WindowWidthSizeClass.Medium -> 1.5f
      else -> 1f
    }
  val colorFilter =
    remember(useDarkTheme) {
      ColorFilter.colorMatrix(
        ColorMatrix().apply {
          val saturation = if (useDarkTheme) 1f else 5f
          setToSaturation(saturation)
        }
      )
    }

  Canvas(
    modifier.aspectRatio(imageAspectRatio).drawWithCache {
      val graphicsLayer = obtainGraphicsLayer()
      graphicsLayer.apply {
        record { drawContent() }
        this.colorFilter = colorFilter
      }

      onDrawWithContent {
        drawLayer(graphicsLayer)
        drawRect(color = overlayColor, blendMode = BlendMode.Luminosity)
      }
    }
  ) {
    drawImage()
  }
}

private fun calculateAlpha(pageOffset: Float): Float {
  val offsetAbsolute = minOf(1f, pageOffset.absoluteValue)
  return EaseInSine.transform(offsetAbsolute.inverse())
}
