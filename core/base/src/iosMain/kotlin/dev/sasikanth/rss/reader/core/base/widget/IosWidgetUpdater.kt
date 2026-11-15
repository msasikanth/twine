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

package dev.sasikanth.rss.reader.core.base.widget

import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSLock

@AppScope
class IosWidgetUpdater
internal constructor(dispatchersProvider: DispatchersProvider, private val timeSource: TimeSource) :
  WidgetUpdater {

  @Inject
  constructor(
    dispatchersProvider: DispatchersProvider
  ) : this(dispatchersProvider, TimeSource.Monotonic)

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.default)
  private val lock = NSLock()
  private var lastReloadMark: TimeMark? = null
  private var trailingJob: Job? = null

  override fun updateUnreadWidget() {
    var fireImmediately = false

    lock.lock()
    try {
      val previousMark = lastReloadMark
      val elapsed = previousMark?.elapsedNow()
      if (elapsed == null || elapsed >= COALESCE_WINDOW) {
        lastReloadMark = timeSource.markNow()
        trailingJob?.cancel()
        trailingJob = null
        fireImmediately = true
      } else if (trailingJob?.isActive != true) {
        val remaining = (COALESCE_WINDOW - elapsed).coerceAtLeast(Duration.ZERO)
        trailingJob =
          scope.launch {
            delay(remaining)
            onTrailingWindowElapsed(isStillActive = { isActive })
          }
      }
    } finally {
      lock.unlock()
    }

    if (fireImmediately) {
      IosWidgetUpdateBridge.update()
    }
  }

  private fun onTrailingWindowElapsed(isStillActive: () -> Boolean) {
    var shouldFire = false
    lock.lock()
    try {
      if (isStillActive()) {
        lastReloadMark = timeSource.markNow()
        trailingJob = null
        shouldFire = true
      }
    } finally {
      lock.unlock()
    }
    if (shouldFire) {
      IosWidgetUpdateBridge.update()
    }
  }

  private companion object {
    val COALESCE_WINDOW = 2.seconds
  }
}
