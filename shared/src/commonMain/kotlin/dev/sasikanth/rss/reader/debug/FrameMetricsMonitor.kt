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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

@Composable internal expect fun ObserveFrameMetrics(onMetrics: (FpsMetrics) -> Unit)

internal suspend fun CoroutineScope.collectWithFrameNanos(onMetrics: (FpsMetrics) -> Unit) {
  val tracker = FpsMetricsTracker()
  var lastFps = -1
  var lastAvgTenths = -1
  var lastStatus: FpsStatus? = null
  while (isActive) {
    withFrameNanos { frameNanos ->
      val metrics = tracker.onFrame(frameNanos) ?: return@withFrameNanos
      val avgTenths = (metrics.avgFrameTimeMs * 10f).roundToInt()
      if (metrics.fps != lastFps || avgTenths != lastAvgTenths || metrics.status != lastStatus) {
        lastFps = metrics.fps
        lastAvgTenths = avgTenths
        lastStatus = metrics.status
        onMetrics(metrics)
      }
    }
  }
}
