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
package dev.sasikanth.rss.reader.utils

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import co.touchlab.kermit.Logger
import kotlin.math.abs

@Composable
@ReadOnlyComposable
internal fun Dp.toSp() = with(LocalDensity.current) { this@toSp.toSp() }

/**
 * Idea is to take a value of a animation/transition progress, like if progress is going from 0-1,
 * this function will switch the values to map to 1-0 instead.
 */
internal fun Float.inverse() = 1f - this

internal enum class KeyboardState {
  Opened,
  Closed
}

@Composable
internal fun keyboardVisibilityAsState(): State<KeyboardState> {
  return rememberUpdatedState(
    if (WindowInsets.ime.getBottom(LocalDensity.current) > 0) KeyboardState.Opened
    else KeyboardState.Closed
  )
}

internal class Ref(var value: Int)

// Note the inline function below which ensures that this function is essentially
// copied at the call site to ensure that its logging only recompositions from the
// original call site.
@Composable
internal fun LogCompositions(tag: String, msg: String) {
  val ref = remember { Ref(0) }
  SideEffect { ref.value++ }
  Logger.d(tag) { "Compositions: $msg ${ref.value}" }
}

fun PagerState.getOffsetFractionForPage(page: Int): Float {
  return (currentPage - page) + currentPageOffsetFraction
}

suspend fun <T> AnchoredDraggableState<T>.flingSettle(
  velocity: Float,
  positionalThreshold: (Float) -> Float,
  velocityThreshold: () -> Float,
) {
  val targetValue =
    anchors.computeTarget(
      currentOffset = requireOffset(),
      velocity = velocity,
      positionalThreshold,
      velocityThreshold
    )

  animateTo(targetValue, spring(stiffness = Spring.StiffnessMedium))
}

internal fun <T> DraggableAnchors<T>.computeTarget(
  currentOffset: Float,
  velocity: Float,
  positionalThreshold: (totalDistance: Float) -> Float,
  velocityThreshold: () -> Float
): T {
  val currentAnchors = this
  require(!currentOffset.isNaN()) { "The offset provided to computeTarget must not be NaN." }
  val isMoving = abs(velocity) > 0.0f
  val isMovingForward = isMoving && velocity > 0f
  // When we're not moving, pick the closest anchor and don't consider directionality
  return if (!isMoving) {
    currentAnchors.closestAnchor(currentOffset)!!
  } else if (abs(velocity) >= abs(velocityThreshold())) {
    currentAnchors.closestAnchor(currentOffset, searchUpwards = isMovingForward)!!
  } else {
    val left = currentAnchors.closestAnchor(currentOffset, false)!!
    val leftAnchorPosition = currentAnchors.positionOf(left)
    val right = currentAnchors.closestAnchor(currentOffset, true)!!
    val rightAnchorPosition = currentAnchors.positionOf(right)
    val distance = abs(leftAnchorPosition - rightAnchorPosition)
    val relativeThreshold = abs(positionalThreshold(distance))
    val closestAnchorFromStart = if (isMovingForward) leftAnchorPosition else rightAnchorPosition
    val relativePosition = abs(closestAnchorFromStart - currentOffset)
    when (relativePosition >= relativeThreshold) {
      true -> if (isMovingForward) right else left
      false -> if (isMovingForward) left else right
    }
  }
}
