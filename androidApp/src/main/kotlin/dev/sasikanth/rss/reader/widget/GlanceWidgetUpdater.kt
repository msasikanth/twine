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

package dev.sasikanth.rss.reader.widget

import android.content.Context
import android.os.SystemClock
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object GlanceWidgetUpdater {

  private const val COALESCE_WINDOW_MS = 2_000L

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val lock = Any()
  private var lastUpdateElapsedMs: Long = -1L
  private var trailingJob: Job? = null

  fun update(context: Context) {
    val appContext = context.applicationContext
    var fireImmediately = false

    synchronized(lock) {
      val now = SystemClock.elapsedRealtime()
      val elapsed = if (lastUpdateElapsedMs < 0L) Long.MAX_VALUE else now - lastUpdateElapsedMs
      if (elapsed >= COALESCE_WINDOW_MS) {
        lastUpdateElapsedMs = now
        trailingJob?.cancel()
        trailingJob = null
        fireImmediately = true
      } else if (trailingJob?.isActive != true) {
        val remaining = (COALESCE_WINDOW_MS - elapsed).coerceAtLeast(0L)
        trailingJob =
          scope.launch {
            delay(remaining)
            if (!isActive) return@launch
            synchronized(lock) {
              lastUpdateElapsedMs = SystemClock.elapsedRealtime()
              trailingJob = null
            }
            performUpdate(appContext)
          }
      }
    }

    if (fireImmediately) {
      scope.launch { performUpdate(appContext) }
    }
  }

  private suspend fun performUpdate(context: Context) {
    TwineUnreadSmallWidget().updateAll(context)
    TwineUnreadMediumWidget().updateAll(context)
    TwineUnreadLargeWidget().updateAll(context)
  }
}
