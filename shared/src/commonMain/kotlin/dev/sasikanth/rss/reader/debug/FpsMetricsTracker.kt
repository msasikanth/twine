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
package dev.sasikanth.rss.reader.debug

import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.roundToInt

enum class FpsStatus {
  Good,
  Warning,
  Poor,
}

@Immutable
data class FpsMetrics(
  val fps: Int = 0,
  val avgFrameTimeMs: Float = 0f,
  val maxFrameTimeMs: Float = 0f,
  val status: FpsStatus = FpsStatus.Good,
)

class FpsMetricsTracker(
  private val sampleWindowNanos: Long = DEFAULT_SAMPLE_WINDOW_NANOS,
  private val maxValidFrameDeltaNanos: Long = DEFAULT_MAX_VALID_FRAME_DELTA_NANOS,
  private val targetRefreshRateHz: Int? = null,
) {
  private var lastFrameNanos = -1L
  private var windowStartNanos = -1L
  private var frameCount = 0
  private var maxFrameDeltaNanos = 0L
  private var minFrameDeltaNanos = Long.MAX_VALUE
  private var secondMinFrameDeltaNanos = Long.MAX_VALUE

  fun onFrame(frameTimeNanos: Long, frameDurationNanos: Long? = null): FpsMetrics? {
    if (lastFrameNanos < 0L) {
      lastFrameNanos = frameTimeNanos
      windowStartNanos = frameTimeNanos
      return null
    }

    val deltaNanos = frameTimeNanos - lastFrameNanos
    if (deltaNanos <= 0L) {
      return null
    }
    lastFrameNanos = frameTimeNanos

    if (deltaNanos > maxValidFrameDeltaNanos) {
      resetWindow(frameTimeNanos)
      return null
    }

    if (
      frameDurationNanos != null &&
        deltaNanos > IDLE_GAP_THRESHOLD_NANOS &&
        frameDurationNanos < deltaNanos / 2L
    ) {
      resetWindow(frameTimeNanos)
      return null
    }

    frameCount++
    val effectiveFrameDeltaNanos =
      if (frameDurationNanos != null && frameDurationNanos > deltaNanos) {
        frameDurationNanos
      } else {
        deltaNanos
      }
    if (effectiveFrameDeltaNanos > maxFrameDeltaNanos) {
      maxFrameDeltaNanos = effectiveFrameDeltaNanos
    }
    if (deltaNanos < minFrameDeltaNanos) {
      secondMinFrameDeltaNanos = minFrameDeltaNanos
      minFrameDeltaNanos = deltaNanos
    } else if (deltaNanos < secondMinFrameDeltaNanos) {
      secondMinFrameDeltaNanos = deltaNanos
    }

    val elapsedNanos = frameTimeNanos - windowStartNanos
    if (elapsedNanos < sampleWindowNanos) {
      return null
    }

    val fps = ((frameCount * NANOS_PER_SECOND) / elapsedNanos).roundToInt().coerceAtLeast(0)
    val avgFrameTimeMs = (elapsedNanos.toDouble() / frameCount / NANOS_PER_MILLI).toFloat()
    val maxFrameTimeMs = (maxFrameDeltaNanos.toDouble() / NANOS_PER_MILLI).toFloat()
    val referenceDeltaNanos = if (frameCount >= 3) secondMinFrameDeltaNanos else minFrameDeltaNanos
    val refreshRateHz =
      targetRefreshRateHz ?: detectRefreshRateHz(referenceDeltaNanos / NANOS_PER_MILLI)
    val status = classify(fps = fps, maxFrameTimeMs = maxFrameTimeMs, refreshRateHz = refreshRateHz)

    resetWindow(frameTimeNanos)

    return FpsMetrics(
      fps = fps,
      avgFrameTimeMs = avgFrameTimeMs,
      maxFrameTimeMs = maxFrameTimeMs,
      status = status,
    )
  }

  fun reset() {
    lastFrameNanos = -1L
    resetWindow(-1L)
  }

  private fun resetWindow(frameTimeNanos: Long) {
    windowStartNanos = frameTimeNanos
    frameCount = 0
    maxFrameDeltaNanos = 0L
    minFrameDeltaNanos = Long.MAX_VALUE
    secondMinFrameDeltaNanos = Long.MAX_VALUE
  }

  private fun detectRefreshRateHz(referenceDeltaMs: Double): Int {
    return when {
      referenceDeltaMs <= REFRESH_120HZ_MAX_FRAME_MS -> 120
      referenceDeltaMs <= REFRESH_90HZ_MAX_FRAME_MS -> 90
      else -> 60
    }
  }

  private fun classify(fps: Int, maxFrameTimeMs: Float, refreshRateHz: Int): FpsStatus {
    return when {
      refreshRateHz >= 110 ->
        when {
          fps >= GOOD_120HZ_FPS_THRESHOLD && maxFrameTimeMs <= GOOD_120HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Good
          fps >= WARNING_120HZ_FPS_THRESHOLD && maxFrameTimeMs <= WARNING_120HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Warning
          else -> FpsStatus.Poor
        }
      refreshRateHz >= 80 ->
        when {
          fps >= GOOD_90HZ_FPS_THRESHOLD && maxFrameTimeMs <= GOOD_90HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Good
          fps >= WARNING_90HZ_FPS_THRESHOLD && maxFrameTimeMs <= WARNING_90HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Warning
          else -> FpsStatus.Poor
        }
      else ->
        when {
          fps >= GOOD_60HZ_FPS_THRESHOLD && maxFrameTimeMs <= GOOD_60HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Good
          fps >= WARNING_60HZ_FPS_THRESHOLD && maxFrameTimeMs <= WARNING_60HZ_MAX_FRAME_TIME_MS ->
            FpsStatus.Warning
          else -> FpsStatus.Poor
        }
    }
  }

  private companion object {
    const val DEFAULT_SAMPLE_WINDOW_NANOS = 300_000_000L
    const val DEFAULT_MAX_VALID_FRAME_DELTA_NANOS = 500_000_000L
    const val IDLE_GAP_THRESHOLD_NANOS = 120_000_000L
    const val NANOS_PER_SECOND = 1_000_000_000.0
    const val NANOS_PER_MILLI = 1_000_000.0
    const val REFRESH_120HZ_MAX_FRAME_MS = 9.5
    const val REFRESH_90HZ_MAX_FRAME_MS = 13.0
    const val GOOD_60HZ_FPS_THRESHOLD = 55
    const val WARNING_60HZ_FPS_THRESHOLD = 40
    const val GOOD_60HZ_MAX_FRAME_TIME_MS = 22f
    const val WARNING_60HZ_MAX_FRAME_TIME_MS = 36f
    const val GOOD_90HZ_FPS_THRESHOLD = 81
    const val WARNING_90HZ_FPS_THRESHOLD = 60
    const val GOOD_90HZ_MAX_FRAME_TIME_MS = 15.5f
    const val WARNING_90HZ_MAX_FRAME_TIME_MS = 25f
    const val GOOD_120HZ_FPS_THRESHOLD = 108
    const val WARNING_120HZ_FPS_THRESHOLD = 80
    const val GOOD_120HZ_MAX_FRAME_TIME_MS = 12.5f
    const val WARNING_120HZ_MAX_FRAME_TIME_MS = 20f
  }
}

internal fun formatOneDecimal(value: Float): String {
  val tenths = (value * 10f).roundToInt()
  val sign = if (tenths < 0) "-" else ""
  val absTenths = abs(tenths)
  return "$sign${absTenths / 10}.${absTenths % 10}"
}
