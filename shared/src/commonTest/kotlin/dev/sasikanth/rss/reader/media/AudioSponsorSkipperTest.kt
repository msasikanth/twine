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

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class AudioSponsorSkipperTest {

  private val sampleChapters =
    listOf(
      MediaChapter(timestampMs = 0L, title = "Intro"),
      MediaChapter(timestampMs = 30_000L, title = "Sponsor: Linear", isSponsor = true),
      MediaChapter(timestampMs = 60_000L, title = "Sponsor: Vanta", isSponsor = true),
      MediaChapter(timestampMs = 90_000L, title = "Main Interview"),
      MediaChapter(
        timestampMs = 150_000L,
        endTimestampMs = 175_000L,
        title = "Outro Ad",
        isSponsor = true,
      ),
    )

  @Test
  fun skipsConsecutiveSponsorChaptersOnlyWhenEnabledAndAvoidsSeekLoops() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val skipEnabled = MutableStateFlow(false)
    val skipper =
      AudioSponsorSkipper(
        audioPlayer = fakePlayer,
        skipSponsorChaptersFlow = skipEnabled,
        loadCachedChapters = { emptyList() },
        scope = scope,
      )

    skipper.registerChapters("post-1", sampleChapters)
    skipper.observe()

    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 35_000L,
        duration = 200_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertTrue(fakePlayer.seekPositions.isEmpty())

    // Enabling setting skips across chained sponsor chapters directly to 90_000L
    skipEnabled.value = true
    runCurrent()
    assertEquals(listOf(90_000L), fakePlayer.seekPositions)

    // Repeated stale position inside same sponsor chapter does not issue duplicate seeks
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 36_000L,
        duration = 200_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertEquals(listOf(90_000L), fakePlayer.seekPositions)

    scope.cancel()
  }

  @Test
  fun respectsManualSponsorSelectionAndNeverSkipsTrailingOpenEndedSponsor() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val scope = CoroutineScope(SupervisorJob() + dispatcher)
    val fakePlayer = FakeAudioPlayer()
    val skipEnabled = MutableStateFlow(true)
    val openEndedChapters =
      listOf(
        MediaChapter(timestampMs = 0L, title = "Intro"),
        MediaChapter(timestampMs = 30_000L, title = "Sponsor: Linear", isSponsor = true),
        MediaChapter(timestampMs = 60_000L, title = "Main Topic"),
        MediaChapter(timestampMs = 150_000L, title = "Final Ad", isSponsor = true),
      )

    val skipper =
      AudioSponsorSkipper(
        audioPlayer = fakePlayer,
        skipSponsorChaptersFlow = skipEnabled,
        loadCachedChapters = { emptyList() },
        scope = scope,
      )

    skipper.registerChapters("post-1", openEndedChapters)
    skipper.observe()

    // Playback starts in Intro; user manually clicks Sponsor chapter at 30_000ms
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 10_000L,
        duration = 180_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()

    skipper.allowSponsorChapter("post-1", 30_000L)
    runCurrent()

    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 31_000L,
        duration = 180_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertTrue(fakePlayer.seekPositions.isEmpty())

    // Moving into trailing sponsor without endTimestampMs must not seek
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 155_000L,
        duration = 180_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertTrue(fakePlayer.seekPositions.isEmpty())

    // Moving back to Intro and then into Sponsor later skips again since suppression cleared
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 65_000L,
        duration = 180_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    fakePlayer.emit(
      PlaybackState(
        isPlaying = true,
        currentPosition = 32_000L,
        duration = 180_000L,
        playingPostId = "post-1",
      )
    )
    runCurrent()
    assertEquals(listOf(60_000L), fakePlayer.seekPositions)

    scope.cancel()
  }

  private class FakeAudioPlayer : AudioPlayer {
    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    val seekPositions = mutableListOf<Long>()

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

    override fun seekTo(position: Long) {
      seekPositions += position
    }

    override fun setPlaybackSpeed(speed: Float) = Unit

    override fun setSleepTimer(option: SleepTimerOption) = Unit
  }
}
