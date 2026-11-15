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

import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.concurrent.AtomicInt
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class IosWidgetUpdaterTest {

  private val dispatchersProvider =
    object : DispatchersProvider {
      override val main: CoroutineDispatcher = Dispatchers.Unconfined
      override val io: CoroutineDispatcher = Dispatchers.Default
      override val default: CoroutineDispatcher = Dispatchers.Default
      override val databaseRead: CoroutineDispatcher = Dispatchers.Default
      override val databaseWrite: CoroutineDispatcher = Dispatchers.Default
    }

  @AfterTest
  fun tearDown() {
    IosWidgetUpdateBridge.unregister()
  }

  @Test
  fun firesLeadingUpdateImmediatelyAndCoalescesBurstIntoSingleTrailingUpdate() = runBlocking {
    val timeSource = TestTimeSource()
    val reloadCount = AtomicInt(0)
    IosWidgetUpdateBridge.register { reloadCount.incrementAndGet() }

    val updater = IosWidgetUpdater(dispatchersProvider, timeSource)

    updater.updateUnreadWidget()
    assertEquals(1, reloadCount.value)

    timeSource += 200.milliseconds
    updater.updateUnreadWidget()
    updater.updateUnreadWidget()
    updater.updateUnreadWidget()
    assertEquals(1, reloadCount.value)

    timeSource += 2.seconds
    delay(2100)
    assertEquals(2, reloadCount.value)

    timeSource += 3.seconds
    updater.updateUnreadWidget()
    assertEquals(3, reloadCount.value)
  }
}
