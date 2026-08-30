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

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter

@Inject
@AppScope
class JvmAudioPlayer(
  private val dispatchersProvider: DispatchersProvider,
  private val httpClient: HttpClient,
) : AudioPlayer {

  companion object {
    private const val MAX_REDIRECTS = 10
  }

  private val _playbackState = MutableStateFlow(PlaybackState.Idle)
  override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private val mediaPlayerFactory: MediaPlayerFactory? =
    try {
      MediaPlayerFactory()
    } catch (e: Throwable) {
      Logger.e(e) { "Failed to initialize MediaPlayerFactory" }
      null
    }
  private val mediaPlayer: MediaPlayer? = mediaPlayerFactory?.mediaPlayers()?.newMediaPlayer()

  override val isAvailable: Boolean
    get() = mediaPlayer != null

  override val installationHint: String
    get() {
      val os = System.getProperty("os.name").orEmpty().lowercase()
      return when {
        "mac" in os -> "brew install --cask vlc"
        "win" in os -> "https://www.videolan.org/vlc/"
        else -> "sudo apt install vlc"
      }
    }

  private var progressJob: Job? = null
  private var resolveJob: Job? = null
  private var playingUrl: String? = null
  private var playingPostId: String? = null
  private var playingTitle: String? = null
  private var playingArtist: String? = null
  private var playingCoverUrl: String? = null
  private var sleepTimerJob: Job? = null
  private var sleepTimerRemainingMillis: Long? = null
  private var selectedSleepTimerOption: SleepTimerOption = SleepTimerOption.None

  init {
    mediaPlayer
      ?.events()
      ?.addMediaPlayerEventListener(
        object : MediaPlayerEventAdapter() {
          override fun playing(mediaPlayer: MediaPlayer?) {
            updatePlaybackState()
            startProgressUpdate()
          }

          override fun paused(mediaPlayer: MediaPlayer?) {
            updatePlaybackState()
            stopProgressUpdate()
          }

          override fun stopped(mediaPlayer: MediaPlayer?) {
            updatePlaybackState()
            stopProgressUpdate()
          }

          override fun finished(mediaPlayer: MediaPlayer?) {
            if (_playbackState.value.sleepTimerRemaining == -1L) {
              setSleepTimer(SleepTimerOption.None)
            }
            stop()
          }

          override fun error(mediaPlayer: MediaPlayer?) {
            _playbackState.update { it.copy(buffering = false) }
            updatePlaybackState()
            stopProgressUpdate()
          }

          override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
            _playbackState.update { it.copy(buffering = newCache < 100f) }
          }
        }
      )
  }

  override fun play(
    url: String,
    title: String,
    artist: String,
    coverUrl: String?,
    postId: String?,
    initialPosition: Long,
  ) {
    resolveJob?.cancel()
    stopProgressUpdate()
    mediaPlayer?.controls()?.stop()

    playingUrl = url
    playingPostId = postId
    playingTitle = title
    playingArtist = artist
    playingCoverUrl = coverUrl
    _playbackState.update {
      it.copy(
        isPlaying = false,
        currentPosition = initialPosition,
        duration = 0,
        playingUrl = url,
        playingPostId = postId,
        title = title,
        artist = artist,
        coverUrl = coverUrl,
        buffering = true,
      )
    }

    resolveJob =
      scope.launch {
        val mrl = withContext(dispatchersProvider.io) { resolveRedirects(url) }
        mediaPlayer?.media()?.play(mrl)
        if (initialPosition > 0) {
          mediaPlayer?.controls()?.setTime(initialPosition)
        }
      }
  }

  /**
   * libVLC stops after five redirects, which podcast prefix chains (Podtrac, Chartable, Megaphone)
   * routinely exceed, so the final media URL is resolved before it is handed over.
   */
  private suspend fun resolveRedirects(url: String): String {
    var currentUrl = url
    repeat(MAX_REDIRECTS) {
      val location =
        try {
          redirectLocation(currentUrl)
        } catch (e: Exception) {
          Logger.e(e) { "Failed to resolve redirects for $currentUrl" }
          return currentUrl
        }

      val nextUrl = location?.let { redirectTarget(currentUrl, it) } ?: return currentUrl
      if (nextUrl == currentUrl) return currentUrl

      currentUrl = nextUrl
    }

    return currentUrl
  }

  private suspend fun redirectLocation(url: String): String? {
    val response = httpClient.head(url)
    if (response.status != HttpStatusCode.MethodNotAllowed) return response.redirectLocation()

    return httpClient
      .prepareGet(url) { header(HttpHeaders.Range, "bytes=0-0") }
      .execute { it.redirectLocation() }
  }

  private fun HttpResponse.redirectLocation(): String? =
    if (status.value in 300..399) headers[HttpHeaders.Location] else null

  /** Absolute targets are used as is, so signed CDN URLs are not re-encoded. */
  private fun redirectTarget(url: String, location: String): String? =
    try {
      if (location.startsWith("https://", ignoreCase = true)) {
        location
      } else {
        URLBuilder(url).takeFrom(location).buildString()
      }
    } catch (e: Exception) {
      Logger.e(e) { "Failed to resolve redirect target $location" }
      null
    }

  override fun pause() {
    mediaPlayer?.controls()?.pause()
  }

  override fun stop() {
    stopProgressUpdate()
    resolveJob?.cancel()
    resolveJob = null
    mediaPlayer?.controls()?.stop()
    sleepTimerJob?.cancel()
    sleepTimerRemainingMillis = null
    selectedSleepTimerOption = SleepTimerOption.None
    playingUrl = null
    playingPostId = null
    playingTitle = null
    playingArtist = null
    playingCoverUrl = null
    _playbackState.value = PlaybackState.Idle
  }

  override fun resume() {
    mediaPlayer?.controls()?.play()
  }

  override fun seekTo(position: Long) {
    mediaPlayer?.controls()?.setTime(position)
    updatePlaybackState()
  }

  override fun setPlaybackSpeed(speed: Float) {
    mediaPlayer?.controls()?.setRate(speed)
    updatePlaybackState()
  }

  override fun setSleepTimer(option: SleepTimerOption) {
    sleepTimerJob?.cancel()
    sleepTimerRemainingMillis = null
    selectedSleepTimerOption = option

    when (option) {
      SleepTimerOption.None -> {
        // No-op
      }
      SleepTimerOption.EndOfTrack -> {
        sleepTimerRemainingMillis = -1L
      }
      is SleepTimerOption.Minutes -> {
        val millis = option.minutes * 60 * 1000L
        sleepTimerRemainingMillis = millis
        sleepTimerJob =
          scope.launch {
            while (sleepTimerRemainingMillis!! > 0) {
              delay(1000.milliseconds)
              sleepTimerRemainingMillis = sleepTimerRemainingMillis!! - 1000
              updatePlaybackState()
            }
            pause()
            setSleepTimer(SleepTimerOption.None)
          }
      }
    }
    updatePlaybackState()
  }

  private fun updatePlaybackState() {
    _playbackState.update {
      it.copy(
        isPlaying = mediaPlayer?.status()?.isPlaying ?: false,
        currentPosition = mediaPlayer?.status()?.time()?.coerceAtLeast(0) ?: 0L,
        duration = mediaPlayer?.status()?.length()?.coerceAtLeast(0) ?: 0L,
        playingUrl = playingUrl,
        playingPostId = playingPostId,
        title = playingTitle,
        artist = playingArtist,
        coverUrl = playingCoverUrl,
        buffering = it.buffering,
        playbackSpeed = mediaPlayer?.status()?.rate() ?: 1f,
        sleepTimerRemaining = sleepTimerRemainingMillis,
        selectedSleepTimerOption = selectedSleepTimerOption,
      )
    }
  }

  private fun startProgressUpdate() {
    progressJob?.cancel()
    progressJob =
      scope.launch {
        while (true) {
          updatePlaybackState()
          delay(1000.milliseconds)
        }
      }
  }

  private fun stopProgressUpdate() {
    progressJob?.cancel()
    progressJob = null
  }
}
