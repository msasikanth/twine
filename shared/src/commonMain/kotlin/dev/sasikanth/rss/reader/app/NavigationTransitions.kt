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
 */
package dev.sasikanth.rss.reader.app

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

private const val PREDICTIVE_BACK_DURATION_MS = 350
private const val PREDICTIVE_BACK_COMMIT_FADE_MS = 140
private const val PREDICTIVE_BACK_SCRIM_FADE_MS = 180
private const val PREDICTIVE_BACK_EXIT_SCALE = 0.84f
private const val PREDICTIVE_BACK_COMMIT_EXIT_SCALE = 0.94f
private const val PREDICTIVE_BACK_SCRIM_ALPHA = 0.22f
private val PredictiveBackMaxCornerRadius = 32.dp
private val PredictiveBackMaxElevation = 24.dp
private val PredictiveBackMaxVerticalShift = 36.dp
private val PredictiveBackEasing = CubicBezierEasing(0.1f, 0.1f, 0f, 1f)

internal val AndroidNavEnterTransition = fadeIn(animationSpec = tween(durationMillis = 250))
internal val AndroidNavPopExitTransition = fadeOut(animationSpec = tween(durationMillis = 220))

internal fun androidPredictivePopTransitionSpec(
  @NavigationEvent.SwipeEdge swipeEdge: Int
): ContentTransform {
  val pivotX =
    when (swipeEdge) {
      NavigationEvent.EDGE_RIGHT -> 0.46f
      NavigationEvent.EDGE_LEFT -> 0.54f
      else -> 0.5f
    }
  val exit =
    scaleOut(
      targetScale = PREDICTIVE_BACK_EXIT_SCALE,
      transformOrigin = TransformOrigin(pivotFractionX = pivotX, pivotFractionY = 0.54f),
      animationSpec =
        tween(durationMillis = PREDICTIVE_BACK_DURATION_MS, easing = PredictiveBackEasing),
    )
  return EnterTransition.None togetherWith exit
}

@Stable
internal class PredictiveBackGestureState {
  var isInProgress by mutableStateOf(false)
    internal set

  var verticalDragOffsetPx by mutableFloatStateOf(0f)
    internal set
}

@Composable
internal fun rememberPredictiveBackGestureState(): PredictiveBackGestureState {
  val navigationEventDispatcherOwner = LocalNavigationEventDispatcherOwner.current
  val maxVerticalShiftPx = with(LocalDensity.current) { PredictiveBackMaxVerticalShift.toPx() }
  val gestureState = remember { PredictiveBackGestureState() }

  LaunchedEffect(navigationEventDispatcherOwner, maxVerticalShiftPx) {
    val transitionFlow =
      navigationEventDispatcherOwner?.navigationEventDispatcher?.transitionState ?: emptyFlow()
    var startTouchY = Float.NaN
    transitionFlow.collectLatest { state ->
      when (state) {
        is NavigationEventTransitionState.InProgress -> {
          val touchY = state.latestEvent.touchY
          if (startTouchY.isNaN()) {
            startTouchY = touchY
          }
          gestureState.isInProgress = true
          gestureState.verticalDragOffsetPx =
            ((touchY - startTouchY) * 0.18f).coerceIn(-maxVerticalShiftPx, maxVerticalShiftPx)
        }
        is NavigationEventTransitionState.Idle -> {
          startTouchY = Float.NaN
          gestureState.isInProgress = false
          gestureState.verticalDragOffsetPx = 0f
        }
      }
    }
  }

  return gestureState
}

@Composable
internal fun rememberAndroidPredictiveBackDecorator(
  backStack: List<NavKey>,
  gestureState: PredictiveBackGestureState,
): NavEntryDecorator<NavKey> {
  return remember(backStack, gestureState) {
    NavEntryDecorator { entry ->
      if (entry.contentKey.toString().startsWith("ImageViewer(")) {
        entry.Content()
        return@NavEntryDecorator
      }

      val transition = LocalNavAnimatedContentScope.current.transition
      val isInProgress = gestureState.isInProgress
      var isPredictiveTransition by remember { mutableStateOf(false) }

      if (isInProgress) {
        isPredictiveTransition = true
      } else if (transition.currentState == transition.targetState && !transition.isRunning) {
        isPredictiveTransition = false
      }

      val exitProgress by
        transition.animateFloat(
          transitionSpec = {
            tween(durationMillis = PREDICTIVE_BACK_DURATION_MS, easing = PredictiveBackEasing)
          },
          label = "predictiveBackExitProgress",
        ) { state ->
          if (state == EnterExitState.PostExit) 1f else 0f
        }

      var isBackgroundPreview by remember {
        mutableStateOf(isInProgress && transition.currentState == EnterExitState.PreEnter)
      }
      if (isPredictiveTransition && transition.currentState == EnterExitState.PreEnter) {
        isBackgroundPreview = true
      }

      val scrimAlpha by
        animateFloatAsState(
          targetValue =
            if (isBackgroundPreview && isInProgress) PREDICTIVE_BACK_SCRIM_ALPHA else 0f,
          animationSpec = tween(durationMillis = PREDICTIVE_BACK_SCRIM_FADE_MS),
          label = "predictiveBackScrimAlpha",
        )

      if (!isInProgress && scrimAlpha <= 0f && !isPredictiveTransition) {
        isBackgroundPreview = false
      }

      val isEntryInBackStack =
        backStack.any { it == entry.contentKey || it.toString() == entry.contentKey.toString() }
      val isCommittedExit =
        isPredictiveTransition &&
          !isInProgress &&
          transition.targetState == EnterExitState.PostExit &&
          !isEntryInBackStack

      val commitExitAlpha by
        animateFloatAsState(
          targetValue = if (isCommittedExit) 0f else 1f,
          animationSpec =
            tween(durationMillis = PREDICTIVE_BACK_COMMIT_FADE_MS, easing = LinearEasing),
          label = "predictiveBackCommitFade",
        )

      val commitExitScale by
        animateFloatAsState(
          targetValue = if (isCommittedExit) PREDICTIVE_BACK_COMMIT_EXIT_SCALE else 1f,
          animationSpec =
            tween(durationMillis = PREDICTIVE_BACK_COMMIT_FADE_MS, easing = PredictiveBackEasing),
          label = "predictiveBackCommitScale",
        )

      val verticalOffsetPx by
        animateFloatAsState(
          targetValue = if (isInProgress) gestureState.verticalDragOffsetPx else 0f,
          animationSpec = tween(durationMillis = PREDICTIVE_BACK_COMMIT_FADE_MS),
          label = "predictiveBackVerticalShift",
        )

      val density = LocalDensity.current
      val maxRadiusPx = with(density) { PredictiveBackMaxCornerRadius.toPx() }
      val maxElevationPx = with(density) { PredictiveBackMaxElevation.toPx() }
      val borderStrokePx = with(density) { 1.dp.toPx() }
      val cardShape =
        remember(maxRadiusPx) {
          DynamicRoundedRectShape {
            if (isPredictiveTransition && exitProgress > 0f) {
              maxRadiusPx * exitProgress
            } else {
              0f
            }
          }
        }

      Box(
        modifier =
          Modifier.fillMaxSize()
            .graphicsLayer {
              val activeExit = isPredictiveTransition && exitProgress > 0f
              if (activeExit) {
                shape = cardShape
                clip = true
                shadowElevation = maxElevationPx * exitProgress
                translationY = verticalOffsetPx * exitProgress
                scaleX = commitExitScale
                scaleY = commitExitScale
                alpha = commitExitAlpha
              } else {
                clip = false
                shadowElevation = 0f
                translationY = 0f
                scaleX = 1f
                scaleY = 1f
                alpha = 1f
              }
            }
            .drawWithContent {
              drawContent()
              if (isPredictiveTransition && exitProgress > 0f) {
                val radius = maxRadiusPx * exitProgress
                drawRoundRect(
                  color = Color.Black.copy(alpha = 0.06f * exitProgress),
                  cornerRadius = CornerRadius(radius, radius),
                  style = Stroke(width = borderStrokePx),
                )
              } else if (scrimAlpha > 0f) {
                drawRect(Color.Black.copy(alpha = scrimAlpha))
              }
            }
      ) {
        entry.Content()
      }
    }
  }
}

private class DynamicRoundedRectShape(private val radiusPx: () -> Float) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    val radius = radiusPx().coerceIn(0f, minOf(size.width, size.height) / 2f)
    if (radius <= 0f) return Outline.Rectangle(Rect(Offset.Zero, size))
    return Outline.Rounded(
      RoundRect(
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
        cornerRadius = CornerRadius(radius, radius),
      )
    )
  }
}
