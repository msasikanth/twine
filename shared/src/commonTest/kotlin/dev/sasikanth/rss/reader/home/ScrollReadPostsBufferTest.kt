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

package dev.sasikanth.rss.reader.home

import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.home.ui.postItemContentType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ScrollReadPostsBufferTest {

  @Test
  fun batchesRapidScrollHiddenItemsIntoSingleFixedWindowWrite() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Scroll },
        markPostsAsRead = { batches += it },
      )

    buffer.onVisiblePostsChanged(mapOf("post-1" to 1, "post-2" to 2, "post-3" to 3), 1)
    advanceTimeBy(16)
    buffer.onVisiblePostsChanged(mapOf("post-2" to 2, "post-3" to 3, "post-4" to 4), 2)
    runCurrent()
    advanceTimeBy(16)
    buffer.onVisiblePostsChanged(mapOf("post-3" to 3, "post-4" to 4, "post-5" to 5), 3)
    advanceTimeBy(16)
    buffer.enqueue("featured-1")

    runCurrent()
    assertTrue(batches.isEmpty())

    advanceTimeBy(267)
    runCurrent()
    assertTrue(batches.isEmpty())

    advanceTimeBy(1)
    runCurrent()
    assertEquals(listOf(setOf("post-1", "post-2", "featured-1")), batches)

    scope.cancel()
  }

  @Test
  fun continuousScrollFlushesPeriodicallyWithoutStarving() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Scroll },
        markPostsAsRead = { batches += it },
      )

    buffer.enqueue("post-1")
    runCurrent()

    advanceTimeBy(100)
    buffer.enqueue("post-2")
    runCurrent()

    advanceTimeBy(100)
    buffer.enqueue("post-3")
    runCurrent()
    assertTrue(batches.isEmpty())

    advanceTimeBy(100)
    runCurrent()
    assertEquals(listOf(setOf("post-1", "post-2", "post-3")), batches)

    buffer.enqueue("post-4")
    runCurrent()
    advanceTimeBy(150)
    buffer.enqueue("post-5")
    runCurrent()
    assertEquals(1, batches.size)

    advanceTimeBy(150)
    runCurrent()
    assertEquals(listOf(setOf("post-1", "post-2", "post-3"), setOf("post-4", "post-5")), batches)

    scope.cancel()
  }

  @Test
  fun concurrentSynchronousFlushAwaitsInFlightWriteAndDrainsNewItems() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val firstWriteGate = CompletableDeferred<Unit>()
    var callCount = 0
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Scroll },
        markPostsAsRead = { ids ->
          callCount++
          if (callCount == 1) {
            firstWriteGate.await()
          }
          batches += ids
        },
      )

    buffer.enqueue("post-1")
    advanceTimeBy(300)
    runCurrent()
    assertTrue(batches.isEmpty())

    buffer.enqueue("post-2")
    var explicitFlushCompleted = false
    val flushCaller =
      scope.launch {
        buffer.flush()
        explicitFlushCompleted = true
      }
    runCurrent()
    assertFalse(explicitFlushCompleted)

    firstWriteGate.complete(Unit)
    runCurrent()

    assertTrue(explicitFlushCompleted)
    assertEquals(listOf(setOf("post-1"), setOf("post-2")), batches)

    flushCaller.cancel()
    scope.cancel()
  }

  @Test
  fun ignoresItemsHiddenBelowFirstVisibleIndexWhenScrollingUp() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Scroll },
        markPostsAsRead = { batches += it },
      )

    buffer.onVisiblePostsChanged(mapOf("post-3" to 3, "post-4" to 4), 3)
    buffer.onVisiblePostsChanged(mapOf("post-2" to 2, "post-3" to 3), 2)

    advanceTimeBy(500)
    runCurrent()
    assertTrue(batches.isEmpty())

    scope.cancel()
  }

  @Test
  fun drainsWithoutWritingWhenMarkAsReadOnIsOpen() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Open },
        markPostsAsRead = { batches += it },
      )

    buffer.enqueue(setOf("post-1", "post-2"))
    advanceTimeBy(500)
    runCurrent()

    assertTrue(batches.isEmpty())
    scope.cancel()
  }

  @Test
  fun flushAsyncWritesImmediatelyAndDoesNotDuplicateAfterWindow() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = TestScope(dispatcher)
    val batches = mutableListOf<Set<String>>()
    val buffer =
      ScrollReadPostsBuffer(
        scope = scope,
        flushInterval = 300.milliseconds,
        markAsReadOnProvider = { MarkAsReadOn.Scroll },
        markPostsAsRead = { batches += it },
      )

    buffer.enqueue(setOf("post-1", "post-2"))
    buffer.flushAsync()
    runCurrent()

    assertEquals(listOf(setOf("post-1", "post-2")), batches)

    advanceTimeBy(500)
    runCurrent()
    assertEquals(1, batches.size)

    scope.cancel()
  }

  @Test
  fun postItemContentTypeIncludesHomeViewMode() {
    val contentTypes = HomeViewMode.entries.map(::postItemContentType).toSet()
    assertEquals(HomeViewMode.entries.size, contentTypes.size)
  }
}
