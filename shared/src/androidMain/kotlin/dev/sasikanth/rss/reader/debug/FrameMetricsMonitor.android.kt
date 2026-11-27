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

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.FrameMetrics
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

@Composable
internal actual fun ObserveFrameMetrics(onMetrics: (FpsMetrics) -> Unit) {
  val view = LocalView.current
  val window = remember(view) { view.context.findActivity()?.window }
  val latestOnMetrics by rememberUpdatedState(onMetrics)

  DisposableEffect(window) {
    if (window == null) {
      return@DisposableEffect onDispose {}
    }

    val disposed = AtomicBoolean(false)
    val handlerThread = HandlerThread("TwineFrameMetrics").apply { start() }
    val metricsHandler = Handler(handlerThread.looper)
    val mainHandler = Handler(Looper.getMainLooper())
    val tracker = FpsMetricsTracker()
    var lastFps = -1
    var lastAvgTenths = -1
    var lastStatus: FpsStatus? = null

    val listener =
      Window.OnFrameMetricsAvailableListener { _, frameMetrics, dropCountSinceLastInvocation ->
        if (disposed.get()) return@OnFrameMetricsAvailableListener
        if (dropCountSinceLastInvocation > 0) {
          tracker.reset()
          return@OnFrameMetricsAvailableListener
        }

        val vsyncNanos =
          frameMetrics.getMetric(FrameMetrics.VSYNC_TIMESTAMP).takeIf { it > 0L }
            ?: frameMetrics.getMetric(FrameMetrics.INTENDED_VSYNC_TIMESTAMP)
        if (vsyncNanos <= 0L) return@OnFrameMetricsAvailableListener

        val totalDurationNanos =
          frameMetrics.getMetric(FrameMetrics.TOTAL_DURATION).coerceAtLeast(0L)
        val metrics =
          tracker.onFrame(frameTimeNanos = vsyncNanos, frameDurationNanos = totalDurationNanos)
            ?: return@OnFrameMetricsAvailableListener

        val avgTenths = (metrics.avgFrameTimeMs * 10f).roundToInt()
        if (metrics.fps != lastFps || avgTenths != lastAvgTenths || metrics.status != lastStatus) {
          lastFps = metrics.fps
          lastAvgTenths = avgTenths
          lastStatus = metrics.status
          mainHandler.post {
            if (!disposed.get()) {
              latestOnMetrics(metrics)
            }
          }
        }
      }

    window.addOnFrameMetricsAvailableListener(listener, metricsHandler)

    onDispose {
      disposed.set(true)
      runCatching { window.removeOnFrameMetricsAvailableListener(listener) }
      mainHandler.removeCallbacksAndMessages(null)
      handlerThread.quitSafely()
    }
  }
}

private tailrec fun Context.findActivity(): Activity? {
  return when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext?.findActivity()
    else -> null
  }
}
