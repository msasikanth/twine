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
package dev.sasikanth.rss.reader.feeds.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.snapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.offset
import com.adamglin.composeshadow.dropShadow
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.ui.AppTheme
import dev.sasikanth.rss.reader.utils.computeTarget
import dev.sasikanth.rss.reader.utils.flingSettle
import dev.sasikanth.rss.reader.utils.inverse
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val BOTTOM_SHEET_CORNER_SIZE = 36.dp

@Composable
internal fun BoxWithConstraintsScope.FeedsBottomBar(
  darkTheme: Boolean,
  feedsSheetDragValue: FeedsSheetDragValue,
  feedsPresenter: FeedsPresenter,
  onBottomBarStateChanged: (FeedsSheetDragValue) -> Unit,
  modifier: Modifier = Modifier,
) {
  val coroutineScope = rememberCoroutineScope()
  val feedsState by feedsPresenter.state.collectAsState()

  val (shadowColor1, shadowColor2) =
    if (darkTheme) {
      Pair(Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.24f))
    } else {
      Pair(Color.Black.copy(alpha = 0.4f), Color.Black.copy(alpha = 0.16f))
    }

  val density = LocalDensity.current

  val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val bottomPadding = navigationBarPadding + 16.dp
  val collapsedSheetHeight = 100.dp
  val targetSheetHeight = with(density) { constraints.maxHeight.toDp() }

  val decayAnimationSpec = rememberSplineBasedDecay<Float>()
  val dragState = remember {
    AnchoredDraggableState(
      initialValue = FeedsSheetDragValue.Collapsed,
      anchors =
        DraggableAnchors {
          FeedsSheetDragValue.Collapsed at 0f
          FeedsSheetDragValue.Expanded at with(density) { targetSheetHeight.toPx() }
        },
    )
  }
  val dragProgress =
    dragState.progress(
      from = FeedsSheetDragValue.Collapsed,
      to = FeedsSheetDragValue.Expanded,
    )

  LaunchedEffect(feedsSheetDragValue) { dragState.animateTo(feedsSheetDragValue) }
  LaunchedEffect(dragState.settledValue) { onBottomBarStateChanged(dragState.settledValue) }

  val overshootAnimation =
    remember(bottomPadding, density) { Animatable(Pair(bottomPadding, 1f), OvershootToVector) }
  var isOvershootAnimationTriggered by remember { mutableStateOf(false) }

  LaunchedEffect(dragProgress) {
    if (isOvershootAnimationTriggered) return@LaunchedEffect

    if (dragState.settledValue == FeedsSheetDragValue.Collapsed) {
      overshootAnimation.snapTo(targetValue = Pair(bottomPadding * dragProgress.inverse(), 1f))
      isOvershootAnimationTriggered = false
    } else if (
      dragState.targetValue == FeedsSheetDragValue.Collapsed &&
        dragProgress < 0.1f &&
        !isOvershootAnimationTriggered
    ) {
      overshootAnimation.snapTo(targetValue = Pair(0.dp, dragProgress.coerceAtLeast(0.8f)))

      isOvershootAnimationTriggered = true
      coroutineScope.launch {
        overshootAnimation.animateTo(
          targetValue = Pair(bottomPadding, 1f),
          animationSpec =
            spring(
              stiffness = Spring.StiffnessLow,
              dampingRatio = Spring.DampingRatioMediumBouncy,
            ),
        )
      }

      isOvershootAnimationTriggered = false
    } else if (dragState.settledValue == FeedsSheetDragValue.Expanded) {
      overshootAnimation.snapTo(targetValue = Pair(0.dp, dragProgress.coerceAtLeast(0.8f)))
    }
  }

  val positionalThreshold: (totalDistance: Float) -> Float = { distance ->
    if (dragState.currentValue == FeedsSheetDragValue.Collapsed) {
      distance * 0.5f
    } else {
      distance * 0.25f
    }
  }
  val velocityThreshold = { with(density) { 64.dp.toPx() } }

  val bottomSheetColor = AppTheme.colorScheme.bottomSheet
  val bottomSheetBorderColor = AppTheme.colorScheme.bottomSheetBorder
  val snapFlingBehavior =
    remember(density) {
      snapFlingBehavior(
        decayAnimationSpec = decayAnimationSpec,
        snapAnimationSpec = spring(stiffness = Spring.StiffnessMedium),
        snapLayoutInfoProvider =
          AnchoredDraggableLayoutInfoProvider(
            state = dragState,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
          )
      )
    }

  Box(
    modifier =
      Modifier.fillMaxWidth()
        .layout { measurable, constraints ->
          val sheetHeight = dragState.requireOffset().coerceAtLeast(collapsedSheetHeight.toPx())
          val sheetHorizontalPadding =
            lerp(
              start = 32.dp,
              stop = 0.dp,
              fraction = dragProgress,
            )

          val overshootHeight = overshootAnimation.value.first
          val minTargetHeight = sheetHeight.roundToInt()
          val paddedConstraints =
            constraints
              .offset(
                horizontal = (sheetHorizontalPadding.roundToPx() * 2).unaryMinus(),
                vertical = overshootAnimation.value.first.roundToPx().unaryMinus()
              )
              .copy(minHeight = minTargetHeight, maxHeight = minTargetHeight)

          val placeable = measurable.measure(paddedConstraints)
          val layoutWidth = placeable.width + sheetHorizontalPadding.roundToPx() * 2
          val layoutHeight = placeable.height + overshootHeight.roundToPx()

          layout(layoutWidth, layoutHeight) {
            placeable.placeRelative(sheetHorizontalPadding.roundToPx(), 0)
          }
        }
        .nestedScroll(
          ConsumeBottomSheetContentNestedScrollConnection(
            dragState = dragState,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
          )
        )
        .anchoredDraggable(
          state = dragState,
          reverseDirection = true,
          orientation = Orientation.Vertical,
          flingBehavior = snapFlingBehavior,
        )
        .semantics {
          if (dragState.anchors.size > 1) {
            if (dragState.currentValue == FeedsSheetDragValue.Collapsed) {
              expand {
                coroutineScope.launch { dragState.animateTo(FeedsSheetDragValue.Expanded) }
                true
              }
            } else {
              collapse {
                coroutineScope.launch { dragState.animateTo(FeedsSheetDragValue.Collapsed) }
                true
              }
            }
          }
        }
        .then(modifier)
        .align(Alignment.BottomCenter)
        .graphicsLayer {
          val overshootScale = overshootAnimation.value.second
          scaleX = overshootScale
          scaleY = overshootScale
        }
        .dropShadow(
          shape = RoundedCornerShape(50),
          offsetY = 16.dp,
          blur = 32.dp,
          color = shadowColor1
        )
        .dropShadow(
          shape = RoundedCornerShape(50),
          offsetY = 4.dp,
          blur = 8.dp,
          color = shadowColor2
        )
        .graphicsLayer {
          shape =
            RoundedCornerShape(
              BOTTOM_SHEET_CORNER_SIZE * EaseInCirc.transform(dragProgress.inverse())
            )
          clip = true
        }
        .drawBehind {
          val cornerRadiusDp =
            BOTTOM_SHEET_CORNER_SIZE * EaseInCirc.transform(dragProgress.inverse())
          val cornerRadius =
            CornerRadius(
              x = cornerRadiusDp.toPx(),
              y = cornerRadiusDp.toPx(),
            )
          drawRoundRect(color = bottomSheetColor)

          val borderColor =
            lerp(
              start = bottomSheetBorderColor,
              stop = bottomSheetColor,
              fraction = dragProgress,
            )

          drawRoundRect(
            color = borderColor,
            style = Stroke(width = 2.dp.toPx()),
            cornerRadius = cornerRadius
          )
        },
  ) {
    Column {
      BottomSheetHandle(progress = dragProgress)

      //      BottomSheetExpandedContent(
      //        modifier = Modifier.graphicsLayer { alpha = dragProgress },
      //        feedsPresenter = feedsPresenter
      //      )
    }

    val contentBottomPadding = lerp(24.dp, 32.dp, dragProgress)
    val horizontalContentPadding = lerp(24.dp, 36.dp, dragProgress)
    LazyRow(
      modifier =
        Modifier.fillMaxWidth()
          .padding(bottom = contentBottomPadding)
          .align(Alignment.BottomCenter),
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(horizontal = horizontalContentPadding)
    ) {
      items(feedsState.pinnedSources) { source ->
        when (source) {
          is FeedGroup -> {
            FeedGroupBottomBarItem(
              feedGroup = source,
              canShowUnreadPostsCount = feedsState.canShowUnreadPostsCount,
              selected = feedsState.activeSource?.id == source.id,
              dragProgress = { dragProgress },
              onClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(source)) }
            )
          }
          is Feed -> {
            FeedBottomBarItem(
              badgeCount = source.numberOfUnreadPosts,
              homePageUrl = source.homepageLink,
              feedIconUrl = source.icon,
              canShowUnreadPostsCount = feedsState.canShowUnreadPostsCount,
              dragProgress = { dragProgress },
              onClick = { feedsPresenter.dispatch(FeedsEvent.OnSourceClick(source)) },
              selected = feedsState.activeSource?.id == source.id
            )
          }
        }
      }
    }
  }
}

@Suppress("FunctionName")
private fun ConsumeBottomSheetContentNestedScrollConnection(
  dragState: AnchoredDraggableState<FeedsSheetDragValue>,
  positionalThreshold: (Float) -> Float,
  velocityThreshold: () -> Float,
) =
  object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
      val delta = available.y
      return if (delta < 0 && source == NestedScrollSource.UserInput) {
        Offset(x = 0f, y = dragState.dispatchRawDelta(delta))
      } else {
        Offset.Zero
      }
    }

    override fun onPostScroll(
      consumed: Offset,
      available: Offset,
      source: NestedScrollSource
    ): Offset {
      return if (source == NestedScrollSource.UserInput) {
        Offset(x = 0f, y = dragState.dispatchRawDelta(available.y))
      } else {
        Offset.Zero
      }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
      val toFling = available.y
      val currentOffset = dragState.requireOffset()
      val minAnchor = dragState.anchors.minPosition()
      return if (toFling < 0 && currentOffset > minAnchor) {
        dragState.flingSettle(
          velocity = toFling,
          positionalThreshold = positionalThreshold,
          velocityThreshold = velocityThreshold
        )
        available
      } else {
        Velocity.Zero
      }
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
      dragState.flingSettle(
        velocity = available.y,
        positionalThreshold = positionalThreshold,
        velocityThreshold = velocityThreshold
      )
      return available
    }
  }

enum class FeedsSheetDragValue {
  Collapsed,
  Expanded
}

private val OvershootToVector: TwoWayConverter<Pair<Dp, Float>, AnimationVector2D> =
  TwoWayConverter(
    convertToVector = { AnimationVector2D(it.first.value, it.second) },
    convertFromVector = { Pair(Dp(it.v1), it.v2) }
  )

@Suppress("FunctionName")
private fun <T> AnchoredDraggableLayoutInfoProvider(
  state: AnchoredDraggableState<T>,
  positionalThreshold: (totalDistance: Float) -> Float,
  velocityThreshold: () -> Float
): SnapLayoutInfoProvider =
  object : SnapLayoutInfoProvider {

    override fun calculateSnapOffset(velocity: Float): Float {
      val currentOffset = state.requireOffset()
      val target =
        state.anchors.computeTarget(
          currentOffset = currentOffset,
          velocity = velocity,
          positionalThreshold = positionalThreshold,
          velocityThreshold = velocityThreshold
        )
      return state.anchors.positionOf(target) - currentOffset
    }
  }
