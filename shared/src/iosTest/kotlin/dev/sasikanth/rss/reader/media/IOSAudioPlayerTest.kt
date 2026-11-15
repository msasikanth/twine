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

import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import platform.AVFoundation.AVPlayerTimeControlStatusPaused
import platform.AVFoundation.AVPlayerTimeControlStatusPlaying
import platform.AVFoundation.AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
import platform.Foundation.NSURL

@OptIn(ExperimentalCoroutinesApi::class)
class IOSAudioPlayerTest {

  @Test
  fun pauseDuringSwapToLocalCacheSeekDoesNotResumePlaybackAndSettlesPosition() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val fakePlayer = FakePlatformAudioPlayer()
    val audioPlayer = IOSAudioPlayer(TestDispatchersProvider(dispatcher), fakePlayer)

    val remoteUrl = "https://example.invalid/episode-1.mp3"
    val localUrl = NSURL.fileURLWithPath("/tmp/episode-1-cached.mp3")

    audioPlayer.play(
      url = remoteUrl,
      title = "Episode 1",
      artist = "Podcast",
      coverUrl = null,
      postId = "post-1",
      initialPosition = 0L,
    )
    runCurrent()

    fakePlayer.timeControlStatus = AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    fakePlayer.currentTimeSeconds = 30.0
    fakePlayer.events.clear()

    audioPlayer.onLocalCacheReady(remoteUrl = remoteUrl, localUrl = localUrl)

    val pauseIndex = fakePlayer.events.indexOf("pause")
    val replaceIndex = fakePlayer.events.indexOf("replaceItem")
    assertTrue(pauseIndex in 0..<replaceIndex)
    assertFalse(fakePlayer.events.any { it.startsWith("play:") })
    assertTrue(audioPlayer.playbackState.value.isPlaying)
    assertEquals(30_000L, audioPlayer.playbackState.value.currentPosition)

    audioPlayer.pause()
    assertFalse(audioPlayer.playbackState.value.isPlaying)
    assertEquals(30_000L, audioPlayer.playbackState.value.currentPosition)

    fakePlayer.events.clear()
    fakePlayer.completeLatestSeek(durationSeconds = 180.0)
    runCurrent()

    assertFalse(fakePlayer.events.any { it.startsWith("play:") })
    assertFalse(audioPlayer.playbackState.value.isPlaying)
    assertEquals(30_000L, audioPlayer.playbackState.value.currentPosition)
    assertEquals(180_000L, audioPlayer.playbackState.value.duration)

    audioPlayer.resume()
    fakePlayer.currentTimeSeconds = 42.0
    advanceTimeBy(1_000L)
    runCurrent()

    assertTrue(audioPlayer.playbackState.value.isPlaying)
    assertEquals(42_000L, audioPlayer.playbackState.value.currentPosition)

    audioPlayer.stop()
  }

  @Test
  fun swapToLocalCacheSeekStartsPlaybackAfterCompletionWhenNotPaused() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val fakePlayer = FakePlatformAudioPlayer()
    val audioPlayer = IOSAudioPlayer(TestDispatchersProvider(dispatcher), fakePlayer)

    val remoteUrl = "https://example.invalid/episode-2.mp3"
    val localUrl = NSURL.fileURLWithPath("/tmp/episode-2-cached.mp3")

    audioPlayer.play(
      url = remoteUrl,
      title = "Episode 2",
      artist = "Podcast",
      coverUrl = null,
      postId = "post-2",
      initialPosition = 0L,
    )
    runCurrent()

    fakePlayer.timeControlStatus = AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    fakePlayer.currentTimeSeconds = 15.0
    fakePlayer.events.clear()

    audioPlayer.onLocalCacheReady(remoteUrl = remoteUrl, localUrl = localUrl)
    assertFalse(fakePlayer.events.any { it.startsWith("play:") })

    fakePlayer.completeLatestSeek(durationSeconds = 90.0)
    runCurrent()

    assertTrue(fakePlayer.events.contains("play:1.0"))
    assertTrue(audioPlayer.playbackState.value.isPlaying)
    assertEquals(15_000L, audioPlayer.playbackState.value.currentPosition)
    assertEquals(90_000L, audioPlayer.playbackState.value.duration)

    audioPlayer.stop()
  }

  @Test
  fun seekDuringSwapToLocalCacheSeekPreservesAutoPlayAtNewPosition() = runTest {
    val dispatcher = StandardTestDispatcher(testScheduler)
    val fakePlayer = FakePlatformAudioPlayer()
    val audioPlayer = IOSAudioPlayer(TestDispatchersProvider(dispatcher), fakePlayer)

    val remoteUrl = "https://example.invalid/episode-3.mp3"
    val localUrl = NSURL.fileURLWithPath("/tmp/episode-3-cached.mp3")

    audioPlayer.play(
      url = remoteUrl,
      title = "Episode 3",
      artist = "Podcast",
      coverUrl = null,
      postId = "post-3",
      initialPosition = 0L,
    )
    runCurrent()

    fakePlayer.timeControlStatus = AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate
    fakePlayer.currentTimeSeconds = 20.0
    fakePlayer.events.clear()

    audioPlayer.onLocalCacheReady(remoteUrl = remoteUrl, localUrl = localUrl)
    val supersededSeek = fakePlayer.pendingSeeks.single()

    audioPlayer.seekTo(45_000L)
    supersededSeek.onComplete()
    runCurrent()

    assertFalse(fakePlayer.events.any { it.startsWith("play:") })
    assertEquals(45_000L, audioPlayer.playbackState.value.currentPosition)

    fakePlayer.completeLatestSeek(durationSeconds = 120.0)
    runCurrent()

    assertTrue(fakePlayer.events.contains("play:1.0"))
    assertTrue(audioPlayer.playbackState.value.isPlaying)
    assertEquals(45_000L, audioPlayer.playbackState.value.currentPosition)

    audioPlayer.stop()
  }
}

private class TestDispatchersProvider(dispatcher: CoroutineDispatcher) : DispatchersProvider {
  override val main: CoroutineDispatcher = dispatcher
  override val io: CoroutineDispatcher = dispatcher
  override val default: CoroutineDispatcher = dispatcher
  override val databaseRead: CoroutineDispatcher = dispatcher
  override val databaseWrite: CoroutineDispatcher = dispatcher
}

private class FakePlatformAudioPlayer : PlatformAudioPlayer {
  data class PendingSeek(
    val positionMillis: Long,
    val zeroTolerance: Boolean,
    val onComplete: () -> Unit,
  )

  val events = mutableListOf<String>()
  val pendingSeeks = mutableListOf<PendingSeek>()

  override var hasCurrentItem: Boolean = false
  override var isCurrentItemFailed: Boolean = false
  override var timeControlStatus: Long = AVPlayerTimeControlStatusPaused
  override var currentItemDurationSeconds: Double = 0.0
  override var currentTimeSeconds: Double = 0.0

  private var nowPlayingInfo: Map<Any?, Any?>? = null

  override fun activatePlaybackSession() {
    events.add("activateSession")
  }

  override fun replaceCurrentItemWithUrl(url: NSURL?) {
    events.add("replaceItem")
    hasCurrentItem = url != null
    currentTimeSeconds = 0.0
    currentItemDurationSeconds = 0.0
    if (url == null) {
      timeControlStatus = AVPlayerTimeControlStatusPaused
    }
  }

  override fun pause() {
    events.add("pause")
    timeControlStatus = AVPlayerTimeControlStatusPaused
  }

  override fun playImmediatelyAtRate(rate: Float) {
    events.add("play:$rate")
    timeControlStatus = AVPlayerTimeControlStatusPlaying
  }

  override fun seekTo(positionMillis: Long, zeroTolerance: Boolean, onComplete: () -> Unit) {
    events.add("seek:$positionMillis:$zeroTolerance")
    pendingSeeks.add(PendingSeek(positionMillis, zeroTolerance, onComplete))
  }

  fun completeLatestSeek(durationSeconds: Double) {
    val seek = pendingSeeks.removeLast()
    currentTimeSeconds = seek.positionMillis.toDouble() / 1000.0
    currentItemDurationSeconds = durationSeconds
    seek.onComplete()
  }

  override fun observePlaybackEnd(
    onNotification: (finishedItemMatchesCurrent: () -> Boolean) -> Unit
  ) = Unit

  override fun setupRemoteCommands(
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onSeekToMillis: (Long) -> Unit,
  ) = Unit

  override fun getNowPlayingInfo(): Map<Any?, Any?>? = nowPlayingInfo

  override fun setNowPlayingInfo(info: Map<Any?, Any?>?) {
    nowPlayingInfo = info
  }
}
