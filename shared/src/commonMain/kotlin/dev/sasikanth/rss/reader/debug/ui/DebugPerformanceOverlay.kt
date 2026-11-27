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
package dev.sasikanth.rss.reader.debug.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.sasikanth.rss.reader.debug.FpsStatus
import dev.sasikanth.rss.reader.debug.ObserveFrameMetrics
import dev.sasikanth.rss.reader.debug.formatOneDecimal
import dev.sasikanth.rss.reader.utils.LocalRootWindowSizeClass
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val DOCK_MODE_IDLE = 0
private const val DOCK_MODE_DRAGGING = 1
private const val DOCK_MODE_SNAPPING = 2

private val OverlayBackgroundColor = Color(0xCC121212)
private val OverlayBorderColor = Color.White.copy(alpha = 0.16f)
private val FpsGoodColor = Color(0xFF4CAF50)
private val FpsWarningColor = Color(0xFFFFB300)
private val FpsPoorColor = Color(0xFFEF5350)
private val OverlayMultiLineShape = RoundedCornerShape(12.dp)
private val FpsReadoutMinWidth = 128.dp
private val FpsTextStyle =
  TextStyle(
    color = Color.White,
    fontSize = 11.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily.Monospace,
    fontFeatureSettings = "tnum",
  )
private val WindowSizeTextStyle =
  TextStyle(
    color = Color.White.copy(alpha = 0.85f),
    fontSize = 10.sp,
    fontFamily = FontFamily.Monospace,
    fontFeatureSettings = "tnum",
  )

private class OverlayDockState {
  var containerWidth: Int = 0
  var containerHeight: Int = 0
  var pillWidth: Int = 0
  var pillHeight: Int = 0
  var isDockedRight: Boolean = true
  var hasMovedY: Boolean = false
  var snapJob: Job? = null
  val mode = mutableIntStateOf(DOCK_MODE_IDLE)
  val dragOffsetX = mutableFloatStateOf(0f)
  val dragOffsetY = mutableFloatStateOf(0f)
  val snapOffsetX = Animatable(0f)
}

@Immutable
private data class FpsDisplayState(
  val text: String = "-- FPS",
  val status: FpsStatus = FpsStatus.Good,
)

@Composable
internal fun DebugPerformanceOverlay(
  showFps: Boolean,
  showWindowSize: Boolean,
  modifier: Modifier = Modifier,
) {
  val dockState = remember { OverlayDockState() }
  val coroutineScope = rememberCoroutineScope()
  val edgeMarginPx = with(LocalDensity.current) { 8.dp.toPx() }
  val pillShape = if (showFps && showWindowSize) OverlayMultiLineShape else CircleShape

  Layout(
    modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing),
    content = {
      Column(
        modifier =
          Modifier.clip(pillShape)
            .background(OverlayBackgroundColor, pillShape)
            .border(width = 1.dp, color = OverlayBorderColor, shape = pillShape)
            .pointerInput(edgeMarginPx) {
              fun snapToEdge(dockRight: Boolean) {
                val minX = edgeMarginPx
                val maxX =
                  (dockState.containerWidth - dockState.pillWidth - edgeMarginPx).coerceAtLeast(
                    minX
                  )
                val startX = dockState.dragOffsetX.floatValue.coerceIn(minX, maxX)
                dockState.isDockedRight = dockRight
                val targetX = if (dockRight) maxX else minX
                dockState.snapJob?.cancel()
                dockState.snapJob =
                  coroutineScope.launch {
                    dockState.snapOffsetX.snapTo(startX)
                    dockState.mode.intValue = DOCK_MODE_SNAPPING
                    try {
                      dockState.snapOffsetX.animateTo(
                        targetValue = targetX,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                      )
                    } finally {
                      if (dockState.mode.intValue == DOCK_MODE_SNAPPING) {
                        dockState.mode.intValue = DOCK_MODE_IDLE
                      }
                    }
                  }
              }

              detectDragGestures(
                onDragStart = {
                  dockState.snapJob?.cancel()
                  dockState.snapJob = null
                  val minX = edgeMarginPx
                  val maxX =
                    (dockState.containerWidth - dockState.pillWidth - edgeMarginPx).coerceAtLeast(
                      minX
                    )
                  val minY = edgeMarginPx
                  val maxY =
                    (dockState.containerHeight - dockState.pillHeight - edgeMarginPx).coerceAtLeast(
                      minY
                    )
                  val currentX =
                    when (dockState.mode.intValue) {
                      DOCK_MODE_SNAPPING -> dockState.snapOffsetX.value.coerceIn(minX, maxX)
                      DOCK_MODE_DRAGGING -> dockState.dragOffsetX.floatValue.coerceIn(minX, maxX)
                      else -> if (dockState.isDockedRight) maxX else minX
                    }
                  val currentY =
                    if (dockState.hasMovedY) {
                      dockState.dragOffsetY.floatValue.coerceIn(minY, maxY)
                    } else {
                      minY
                    }
                  dockState.dragOffsetX.floatValue = currentX
                  dockState.dragOffsetY.floatValue = currentY
                  dockState.hasMovedY = true
                  dockState.mode.intValue = DOCK_MODE_DRAGGING
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  val minX = edgeMarginPx
                  val maxX =
                    (dockState.containerWidth - dockState.pillWidth - edgeMarginPx).coerceAtLeast(
                      minX
                    )
                  val minY = edgeMarginPx
                  val maxY =
                    (dockState.containerHeight - dockState.pillHeight - edgeMarginPx).coerceAtLeast(
                      minY
                    )
                  dockState.dragOffsetX.floatValue =
                    (dockState.dragOffsetX.floatValue + dragAmount.x).coerceIn(minX, maxX)
                  dockState.dragOffsetY.floatValue =
                    (dockState.dragOffsetY.floatValue + dragAmount.y).coerceIn(minY, maxY)
                },
                onDragCancel = { snapToEdge(dockState.isDockedRight) },
                onDragEnd = {
                  val minX = edgeMarginPx
                  val maxX =
                    (dockState.containerWidth - dockState.pillWidth - edgeMarginPx).coerceAtLeast(
                      minX
                    )
                  val currentX = dockState.dragOffsetX.floatValue.coerceIn(minX, maxX)
                  val centerX = currentX + dockState.pillWidth / 2f
                  snapToEdge(dockRight = centerX >= dockState.containerWidth / 2f)
                },
              )
            }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        if (showFps) {
          FpsReadout()
        }
        if (showWindowSize) {
          WindowSizeReadout()
        }
      }
    },
  ) { measurables, constraints ->
    val containerWidth = constraints.maxWidth
    val containerHeight = constraints.maxHeight
    val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
    val placeable = measurables.first().measure(childConstraints)
    dockState.containerWidth = containerWidth
    dockState.containerHeight = containerHeight
    dockState.pillWidth = placeable.width
    dockState.pillHeight = placeable.height

    layout(containerWidth, containerHeight) {
      val minX = edgeMarginPx
      val maxX = (containerWidth - placeable.width - edgeMarginPx).coerceAtLeast(minX)
      val minY = edgeMarginPx
      val maxY = (containerHeight - placeable.height - edgeMarginPx).coerceAtLeast(minY)
      val placedX =
        when (dockState.mode.intValue) {
          DOCK_MODE_DRAGGING -> dockState.dragOffsetX.floatValue.coerceIn(minX, maxX)
          DOCK_MODE_SNAPPING -> dockState.snapOffsetX.value.coerceIn(minX, maxX)
          else -> if (dockState.isDockedRight) maxX else minX
        }
      val placedY =
        if (dockState.hasMovedY) {
          dockState.dragOffsetY.floatValue.coerceIn(minY, maxY)
        } else {
          minY
        }
      placeable.place(placedX.roundToInt(), placedY.roundToInt())
    }
  }
}

@Composable
private fun FpsReadout() {
  var displayState by remember { mutableStateOf(FpsDisplayState()) }

  ObserveFrameMetrics { metrics ->
    displayState =
      FpsDisplayState(
        text = "${metrics.fps} FPS · ${formatOneDecimal(metrics.avgFrameTimeMs)}ms",
        status = metrics.status,
      )
  }

  Row(
    modifier = Modifier.widthIn(min = FpsReadoutMinWidth),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier =
        Modifier.size(8.dp).drawBehind {
          val indicatorColor =
            when (displayState.status) {
              FpsStatus.Good -> FpsGoodColor
              FpsStatus.Warning -> FpsWarningColor
              FpsStatus.Poor -> FpsPoorColor
            }
          drawCircle(indicatorColor)
        }
    )
    Text(text = displayState.text, style = FpsTextStyle, maxLines = 1)
  }
}

@Composable
private fun WindowSizeReadout() {
  val sizeClass = LocalRootWindowSizeClass.current
  val containerSize = LocalWindowInfo.current.containerSize
  val density = LocalDensity.current
  val widthDp = with(density) { containerSize.width.toDp().value.roundToInt() }
  val heightDp = with(density) { containerSize.height.toDp().value.roundToInt() }

  val widthClass =
    when {
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) -> "XL"
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) -> "Large"
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
        "Expanded"
      sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) -> "Medium"
      else -> "Compact"
    }
  val heightClass =
    when {
      sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND) -> "Exp"
      sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> "Med"
      else -> "Cmp"
    }

  Text(
    text = "$widthClass/$heightClass · ${widthDp}×${heightDp}dp",
    style = WindowSizeTextStyle,
    maxLines = 1,
  )
}
