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

package dev.sasikanth.rss.reader.reader

import dev.sasikanth.rss.reader.data.repository.ObservableActiveReaderPost
import dev.sasikanth.rss.reader.data.repository.ObservableSelectedPost
import dev.sasikanth.rss.reader.data.repository.SelectedPost
import dev.sasikanth.rss.reader.reader.ui.resolveInitialReaderPage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderPostSessionCoordinatorTest {

  @Test
  fun defersSelectedPostAndReadWriteUntilInitialTransitionSettles() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val coordinator =
      ReaderPostSessionCoordinator(sessionScope = sessionScope, exitDispatcher = dispatcher)

    var settledCalls = 0
    val markedReadIds = mutableListOf<String>()

    coordinator.onPostPageChanged(
      postId = "post-1",
      shouldMarkReadImmediately = true,
      onSettled = { settledCalls++ },
      markPostRead = { markedReadIds += "post-1" },
    )

    assertEquals(setOf("post-1"), coordinator.openedPostItems.value)

    advanceTimeBy(499)
    runCurrent()
    assertEquals(0, settledCalls)
    assertTrue(markedReadIds.isEmpty())

    coordinator.onInitialTransitionSettled()
    runCurrent()
    assertEquals(1, settledCalls)
    assertEquals(listOf("post-1"), markedReadIds)

    sessionScope.cancel()
    coordinator.onClear {}
    runCurrent()
    assertFalse(coordinator.exitScope.isActive)
  }

  @Test
  fun splitPaneSettlesImmediatelyWithZeroArtificialDelay() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val coordinator =
      ReaderPostSessionCoordinator(
        sessionScope = sessionScope,
        exitDispatcher = dispatcher,
        initialTransitionSettled = true,
      )

    var settledCalls = 0
    val markedReadIds = mutableListOf<String>()

    coordinator.onPostPageChanged(
      postId = "post-1",
      shouldMarkReadImmediately = true,
      onSettled = { settledCalls++ },
      markPostRead = { markedReadIds += "post-1" },
    )
    runCurrent()

    assertEquals(1, settledCalls)
    assertEquals(listOf("post-1"), markedReadIds)

    sessionScope.cancel()
    coordinator.onClear {}
    runCurrent()
    assertFalse(coordinator.exitScope.isActive)
  }

  @Test
  fun accidentalOpenAndQuickBackBeforePageSettleLeavesPostUnread() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val coordinator =
      ReaderPostSessionCoordinator(sessionScope = sessionScope, exitDispatcher = dispatcher)

    var exitMarkedIds: Set<String>? = null

    advanceTimeBy(100)
    runCurrent()
    assertTrue(coordinator.openedPostItems.value.isEmpty())

    sessionScope.cancel()
    coordinator.onClear { openedIds -> exitMarkedIds = openedIds }
    runCurrent()

    assertNull(exitMarkedIds)
    assertFalse(coordinator.exitScope.isActive)
  }

  @Test
  fun quickBackAfterPageSettleBeforeTransitionEndsStillMarksPostReadAndCancelsExitScope() =
    runTest {
      val dispatcher = StandardTestDispatcher(testScheduler)
      val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
      val coordinator =
        ReaderPostSessionCoordinator(sessionScope = sessionScope, exitDispatcher = dispatcher)

      coordinator.onPostPageChanged(
        postId = "post-1",
        shouldMarkReadImmediately = true,
        onSettled = {},
        markPostRead = {},
      )

      val exitWriteGate = CompletableDeferred<Unit>()
      var exitMarkedIds: Set<String>? = null

      advanceTimeBy(300)
      runCurrent()

      sessionScope.cancel()
      coordinator.onClear { openedIds ->
        exitWriteGate.await()
        exitMarkedIds = openedIds
      }
      runCurrent()

      assertTrue(coordinator.exitScope.isActive)
      assertNull(exitMarkedIds)

      exitWriteGate.complete(Unit)
      runCurrent()

      assertEquals(setOf("post-1"), exitMarkedIds)
      assertFalse(coordinator.exitScope.isActive)
    }

  @Test
  fun markAsUnreadBeforePageSettleIgnoresLatePageChangeAndNeverRemarksReadOnExit() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val coordinator =
      ReaderPostSessionCoordinator(sessionScope = sessionScope, exitDispatcher = dispatcher)

    val markedReadIds = mutableListOf<String>()
    var markedUnread = false
    var exitMarkedIds: Set<String>? = null

    advanceTimeBy(100)
    coordinator.onMarkAsUnread("post-1") { markedUnread = true }
    runCurrent()

    assertTrue(markedUnread)
    assertTrue(coordinator.shouldIgnorePageChange("post-1"))

    advanceTimeBy(150)
    coordinator.onPostPageChanged(
      postId = "post-1",
      shouldMarkReadImmediately = true,
      onSettled = {},
      markPostRead = { markedReadIds += "post-1" },
    )
    coordinator.onInitialTransitionSettled()
    runCurrent()

    assertTrue(coordinator.openedPostItems.value.isEmpty())
    assertTrue(markedReadIds.isEmpty())

    sessionScope.cancel()
    coordinator.onClear { openedIds -> exitMarkedIds = openedIds }
    runCurrent()

    assertNull(exitMarkedIds)
    assertFalse(coordinator.exitScope.isActive)
  }

  @Test
  fun markAsUnreadCancelsPendingReadWriteAndPreservesEarlierVisitedPosts() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val coordinator =
      ReaderPostSessionCoordinator(sessionScope = sessionScope, exitDispatcher = dispatcher)

    val markedReadIds = mutableListOf<String>()
    var markedUnread = false
    var exitMarkedIds: Set<String>? = null

    coordinator.onPostPageChanged(
      postId = "post-1",
      shouldMarkReadImmediately = true,
      onSettled = {},
      markPostRead = { markedReadIds += "post-1" },
    )
    coordinator.onInitialTransitionSettled()
    runCurrent()
    assertEquals(listOf("post-1"), markedReadIds)

    coordinator.onPostPageChanged(
      postId = "post-2",
      shouldMarkReadImmediately = true,
      onSettled = {},
      markPostRead = { markedReadIds += "post-2" },
    )
    coordinator.onMarkAsUnread("post-2") { markedUnread = true }
    runCurrent()

    assertTrue(markedUnread)
    assertEquals(setOf("post-1"), coordinator.openedPostItems.value)
    assertEquals(listOf("post-1"), markedReadIds)

    sessionScope.cancel()
    coordinator.onClear { openedIds -> exitMarkedIds = openedIds }
    runCurrent()

    assertEquals(setOf("post-1"), exitMarkedIds)
    assertFalse(coordinator.exitScope.isActive)
  }

  @Test
  fun resolveInitialReaderPageVerifiesAndScansLoadedSnapshotFallback() {
    val snapshot = listOf("post-0", "post-3", "post-4", "post-5")

    assertEquals(
      2,
      resolveInitialReaderPage(
        initialPostIndex = 2,
        initialPostId = "post-4",
        itemCount = snapshot.size,
        peekPostId = snapshot::getOrNull,
      ),
    )

    assertEquals(
      1,
      resolveInitialReaderPage(
        initialPostIndex = 3,
        initialPostId = "post-3",
        itemCount = snapshot.size,
        peekPostId = snapshot::getOrNull,
      ),
    )

    assertEquals(
      1,
      resolveInitialReaderPage(
        initialPostIndex = 8,
        initialPostId = "post-3",
        itemCount = snapshot.size,
        peekPostId = snapshot::getOrNull,
      ),
    )

    assertEquals(
      3,
      resolveInitialReaderPage(
        initialPostIndex = 8,
        initialPostId = "unloaded-post",
        itemCount = snapshot.size,
        peekPostId = { null },
      ),
    )
  }

  @Test
  fun ownedSessionPublishesSelectedPostAndClearsActivePostOnClear() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val shared = SharedReaderObservables()

    val session = shared.session(sessionScope, dispatcher, initialPostIndex = 3, "post-3")

    assertEquals("post-3", shared.activeReaderPost.activePostId.value)
    assertEquals(setOf("post-3"), shared.activeReaderPost.openedPostIds.value)

    session.visit("post-4", 4)
    runCurrent()

    assertEquals("post-4", shared.activeReaderPost.activePostId.value)
    assertEquals(setOf("post-3", "post-4"), shared.activeReaderPost.openedPostIds.value)
    assertEquals(SelectedPost(index = 4, id = "post-4"), shared.selectedPost.selectedPost.value)

    shared.selectedPost.updateSelectedPost(index = 0, postId = "scrolled-away", scrollOffset = 120)

    sessionScope.cancel()
    session.onClear {}
    runCurrent()

    assertNull(shared.activeReaderPost.activePostId.value)
    assertTrue(shared.activeReaderPost.openedPostIds.value.isEmpty())
    assertEquals(SelectedPost(index = 4, id = "post-4"), shared.selectedPost.selectedPost.value)
  }

  @Test
  fun supersededSessionInTwoPaneDoesNotOverwriteSelectedPostOrClearReplacementSession() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scopeA = CoroutineScope(SupervisorJob() + dispatcher)
    val scopeB = CoroutineScope(SupervisorJob() + dispatcher)
    val shared = SharedReaderObservables()

    val sessionA = shared.session(scopeA, dispatcher, initialPostIndex = 2, "post-a")
    sessionA.visit("post-a", 2)
    runCurrent()
    assertEquals(SelectedPost(index = 2, id = "post-a"), shared.selectedPost.selectedPost.value)

    val sessionB = shared.session(scopeB, dispatcher, initialPostIndex = 9, "post-b")

    assertFalse(sessionA.ownsActiveReaderPost())
    assertTrue(sessionB.ownsActiveReaderPost())
    assertEquals("post-b", shared.activeReaderPost.activePostId.value)
    assertEquals(setOf("post-a", "post-b"), shared.activeReaderPost.openedPostIds.value)

    sessionB.visit("post-b", 9)
    runCurrent()
    assertEquals(SelectedPost(index = 9, id = "post-b"), shared.selectedPost.selectedPost.value)

    sessionA.visit("post-a-late", 3)
    runCurrent()
    assertEquals("post-b", shared.activeReaderPost.activePostId.value)
    assertEquals(setOf("post-a", "post-b"), shared.activeReaderPost.openedPostIds.value)
    assertEquals(SelectedPost(index = 9, id = "post-b"), shared.selectedPost.selectedPost.value)

    var clearedIdsA: Set<String>? = null
    scopeA.cancel()
    sessionA.onClear { openedIds -> clearedIdsA = openedIds }
    runCurrent()

    assertEquals(setOf("post-a", "post-a-late"), clearedIdsA)
    assertEquals("post-b", shared.activeReaderPost.activePostId.value)
    assertEquals(setOf("post-a", "post-b"), shared.activeReaderPost.openedPostIds.value)
    assertEquals(SelectedPost(index = 9, id = "post-b"), shared.selectedPost.selectedPost.value)

    scopeB.cancel()
    sessionB.onClear {}
    runCurrent()

    assertNull(shared.activeReaderPost.activePostId.value)
    assertTrue(shared.activeReaderPost.openedPostIds.value.isEmpty())
    assertEquals(SelectedPost(index = 9, id = "post-b"), shared.selectedPost.selectedPost.value)
  }

  @Test
  fun replacementOpenedOnSupersededSessionsActivePostKeepsOwnershipAfterOldSessionClears() =
    runTest {
      val dispatcher = StandardTestDispatcher(testScheduler)
      val scopeA = CoroutineScope(SupervisorJob() + dispatcher)
      val scopeB = CoroutineScope(SupervisorJob() + dispatcher)
      val shared = SharedReaderObservables()

      val sessionA = shared.session(scopeA, dispatcher, initialPostIndex = 1, "post-x")
      sessionA.visit("post-y", 2)
      runCurrent()
      assertEquals("post-y", shared.activeReaderPost.activePostId.value)

      val sessionB = shared.session(scopeB, dispatcher, initialPostIndex = 2, "post-y")
      runCurrent()

      assertFalse(sessionA.ownsActiveReaderPost())
      assertTrue(sessionB.ownsActiveReaderPost())

      var markedReadByB = false
      sessionB.visit("post-y", 2, markPostRead = { markedReadByB = true })
      runCurrent()
      assertTrue(markedReadByB)

      scopeA.cancel()
      sessionA.onClear {}
      runCurrent()

      assertEquals("post-y", shared.activeReaderPost.activePostId.value)
      assertEquals(setOf("post-x", "post-y"), shared.activeReaderPost.openedPostIds.value)
      assertEquals(SelectedPost(index = 2, id = "post-y"), shared.selectedPost.selectedPost.value)
      assertTrue(sessionB.ownsActiveReaderPost())

      sessionB.visit("post-z", 3)
      runCurrent()

      assertEquals("post-z", shared.activeReaderPost.activePostId.value)
      assertEquals(setOf("post-x", "post-y", "post-z"), shared.activeReaderPost.openedPostIds.value)
      assertEquals(SelectedPost(index = 3, id = "post-z"), shared.selectedPost.selectedPost.value)
      assertEquals(setOf("post-y", "post-z"), sessionB.openedPostItems.value)

      var clearedIdsB: Set<String>? = null
      scopeB.cancel()
      sessionB.onClear { openedIds -> clearedIdsB = openedIds }
      runCurrent()

      assertEquals(setOf("post-y", "post-z"), clearedIdsB)
      assertNull(shared.activeReaderPost.activePostId.value)
      assertTrue(shared.activeReaderPost.openedPostIds.value.isEmpty())
      assertEquals(SelectedPost(index = 3, id = "post-z"), shared.selectedPost.selectedPost.value)
    }

  @Test
  fun stackedSessionRegainsOwnershipOnceNewerSessionCloses() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scopeA = CoroutineScope(SupervisorJob() + dispatcher)
    val scopeB = CoroutineScope(SupervisorJob() + dispatcher)
    val shared = SharedReaderObservables()

    val sessionA = shared.session(scopeA, dispatcher, initialPostIndex = 1, "post-1")
    val sessionB = shared.session(scopeB, dispatcher, initialPostIndex = 7, "post-7")
    sessionB.visit("post-7", 7)
    runCurrent()

    scopeB.cancel()
    sessionB.onClear {}
    runCurrent()

    assertNull(shared.activeReaderPost.activePostId.value)
    assertEquals(SelectedPost(index = 7, id = "post-7"), shared.selectedPost.selectedPost.value)
    assertTrue(sessionA.ownsActiveReaderPost())

    sessionA.visit("post-2", 2)
    runCurrent()

    assertEquals("post-2", shared.activeReaderPost.activePostId.value)
    assertEquals(SelectedPost(index = 2, id = "post-2"), shared.selectedPost.selectedPost.value)

    scopeA.cancel()
    sessionA.onClear {}
    runCurrent()

    assertNull(shared.activeReaderPost.activePostId.value)
    assertTrue(shared.activeReaderPost.openedPostIds.value.isEmpty())
    assertEquals(SelectedPost(index = 2, id = "post-2"), shared.selectedPost.selectedPost.value)
  }

  @Test
  fun ownedSessionWithPublishDisabledClearsActivePostWithoutUpdatingSelectedPost() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val sessionScope = CoroutineScope(SupervisorJob() + dispatcher)
    val shared = SharedReaderObservables()
    shared.selectedPost.updateSelectedPost(index = 6, postId = "home-post", scrollOffset = 42)

    val session =
      shared.session(
        sessionScope,
        dispatcher,
        initialPostIndex = 0,
        initialPostId = "bookmark-post",
        canPublishSelectedPost = false,
      )

    assertEquals("bookmark-post", shared.activeReaderPost.activePostId.value)
    assertTrue(shared.activeReaderPost.openedPostIds.value.isEmpty())

    session.visit("bookmark-post", 0)
    runCurrent()

    sessionScope.cancel()
    session.onClear {}
    runCurrent()

    assertNull(shared.activeReaderPost.activePostId.value)
    assertEquals(
      SelectedPost(index = 6, id = "home-post", scrollOffset = 42),
      shared.selectedPost.selectedPost.value,
    )
  }

  private class SharedReaderObservables {
    val activeReaderPost = ObservableActiveReaderPost()
    val selectedPost = ObservableSelectedPost()
    val registry = ReaderSessionRegistry()

    fun session(
      sessionScope: CoroutineScope,
      dispatcher: CoroutineDispatcher,
      initialPostIndex: Int,
      initialPostId: String,
      canPublishSelectedPost: Boolean = true,
    ) =
      ReaderPostSessionCoordinator(
        sessionScope = sessionScope,
        exitDispatcher = dispatcher,
        initialPostIndex = initialPostIndex,
        initialPostId = initialPostId,
        observableActiveReaderPost = activeReaderPost,
        observableSelectedPost = selectedPost,
        sessionRegistry = registry,
        canPublishSelectedPost = { canPublishSelectedPost },
        initialTransitionSettled = true,
      )
  }

  private fun ReaderPostSessionCoordinator.visit(
    postId: String,
    postIndex: Int,
    markPostRead: suspend () -> Unit = {},
  ) {
    onPostPageChanged(
      postId = postId,
      postIndex = postIndex,
      shouldMarkReadImmediately = true,
      markPostRead = markPostRead,
    )
  }
}
