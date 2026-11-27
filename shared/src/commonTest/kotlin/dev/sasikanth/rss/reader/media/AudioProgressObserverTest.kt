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

package dev.sasikanth.rss.reader.media

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class AudioProgressObserverTest {

  @Test
  fun periodicallySavesProgressEveryTenSecondsWhilePlaying() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val saved = mutableListOf<SavedProgress>()
    val observer =
      AudioProgressObserver(
        audioPlayer = fakePlayer,
        saveAudioProgress = { postId, progress, duration ->
          saved += SavedProgress(postId, progress, duration)
        },
        scope = scope,
      )

    observer.observe()
    runCurrent()
    assertTrue(saved.isEmpty())

    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 5_000L,
        duration = 60_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertTrue(saved.isEmpty())

    advanceTimeBy(9_999L)
    runCurrent()
    assertTrue(saved.isEmpty())

    advanceTimeBy(1L)
    runCurrent()
    assertEquals(listOf(SavedProgress("post-1", 5_000L, 60_000L)), saved)

    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 15_000L,
        duration = 60_000L,
        playingPostId = "post-1",
      )
    )
    advanceTimeBy(10_000L)
    runCurrent()
    assertEquals(
      listOf(SavedProgress("post-1", 5_000L, 60_000L), SavedProgress("post-1", 15_000L, 60_000L)),
      saved,
    )

    scope.cancel()
  }

  @Test
  fun immediatelySavesOnPauseAndStopsPeriodicSaves() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val saved = mutableListOf<SavedProgress>()
    val observer =
      AudioProgressObserver(
        audioPlayer = fakePlayer,
        saveAudioProgress = { postId, progress, duration ->
          saved += SavedProgress(postId, progress, duration)
        },
        scope = scope,
      )

    observer.observe()
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 12_000L,
        duration = 90_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()

    fakePlayer.emit(
      PlaybackState(
        isPlaying = false,
        currentPosition = 14_000L,
        duration = 90_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertEquals(listOf(SavedProgress("post-1", 14_000L, 90_000L)), saved)

    advanceTimeBy(30_000L)
    runCurrent()
    assertEquals(1, saved.size)

    scope.cancel()
  }

  @Test
  fun immediatelySavesPreviousTrackFinalPositionOnStopWithoutDuplicating() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val saved = mutableListOf<SavedProgress>()
    val observer =
      AudioProgressObserver(
        audioPlayer = fakePlayer,
        saveAudioProgress = { postId, progress, duration ->
          saved += SavedProgress(postId, progress, duration)
        },
        scope = scope,
      )

    observer.observe()
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 42_000L,
        duration = 120_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()

    fakePlayer.emit(PlaybackState.Idle)
    runCurrent()
    assertEquals(listOf(SavedProgress("post-1", 42_000L, 120_000L)), saved)

    scope.cancel()
  }

  @Test
  fun immediatelySavesPreviousPostWhenSwitchingToAnotherPost() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val saved = mutableListOf<SavedProgress>()
    val observer =
      AudioProgressObserver(
        audioPlayer = fakePlayer,
        saveAudioProgress = { postId, progress, duration ->
          saved += SavedProgress(postId, progress, duration)
        },
        scope = scope,
      )

    observer.observe()
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 25_000L,
        duration = 100_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()

    advanceTimeBy(9_000L)
    runCurrent()
    assertTrue(saved.isEmpty())

    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 3_000L,
        duration = 80_000L,
        playingPostId = "post-2",
      )
    )
    runCurrent()
    assertEquals(listOf(SavedProgress("post-1", 25_000L, 100_000L)), saved)

    // Periodic timer must have reset for post-2 instead of firing 1s later.
    advanceTimeBy(1_000L)
    runCurrent()
    assertEquals(1, saved.size)

    advanceTimeBy(9_000L)
    runCurrent()
    assertEquals(
      listOf(SavedProgress("post-1", 25_000L, 100_000L), SavedProgress("post-2", 3_000L, 80_000L)),
      saved,
    )

    scope.cancel()
  }

  @Test
  fun skipsUnplayedZeroDurationBufferingStubAndContinuesAfterSaveError() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    var shouldThrow = true
    val saved = mutableListOf<SavedProgress>()
    val observer =
      AudioProgressObserver(
        audioPlayer = fakePlayer,
        saveAudioProgress = { postId, progress, duration ->
          if (shouldThrow) {
            throw IllegalStateException("db busy")
          }
          saved += SavedProgress(postId, progress, duration)
        },
        scope = scope,
      )

    observer.observe()
    // Initial buffering stub with duration = 0 and isPlaying = false before playback begins.
    fakePlayer.emit(
      PlaybackState(
        isPlaying = false,
        currentPosition = 15_000L,
        duration = 0L,
        playingPostId = "post-stub",
        buffering = true,
      )
    )
    runCurrent()

    // Switch away before playback started -> should not overwrite DB with 0 duration.
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 10_000L,
        duration = 60_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertTrue(saved.isEmpty())

    advanceTimeBy(10_000L)
    runCurrent()
    assertTrue(saved.isEmpty())

    shouldThrow = false
    fakePlayer.emit(
      PlaybackState(
        isPlaying = false,
        currentPosition = 14_000L,
        duration = 60_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertEquals(listOf(SavedProgress("post-1", 14_000L, 60_000L)), saved)

    scope.cancel()
  }

  private data class SavedProgress(val postId: String, val progress: Long, val duration: Long)

  private class FakeAudioPlayer : AudioPlayer {
    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    fun emit(state: PlaybackState) {
      _playbackState.value = state
    }

    override fun play(
      url: String,
      title: String,
      artist: String,
      coverUrl: String?,
      postId: String?,
      initialPosition: Long,
    ) = Unit

    override fun pause() = Unit

    override fun stop() = Unit

    override fun resume() = Unit

    override fun seekTo(position: Long) = Unit

    override fun setPlaybackSpeed(speed: Float) = Unit

    override fun setSleepTimer(option: SleepTimerOption) = Unit
  }
}
