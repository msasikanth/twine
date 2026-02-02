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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter

@Inject
@AppScope
class JvmAudioPlayer(
  private val dispatchersProvider: DispatchersProvider,
) : AudioPlayer {

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

  private var progressJob: Job? = null
  private var playingUrl: String? = null

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
            updatePlaybackState()
            stopProgressUpdate()
            seekTo(0)
          }

          override fun error(mediaPlayer: MediaPlayer?) {
            updatePlaybackState()
            stopProgressUpdate()
          }

          override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {
            _playbackState.update { it.copy(buffering = newCache < 100f) }
          }
        }
      )
  }

  override fun play(url: String, title: String, artist: String, coverUrl: String?) {
    playingUrl = url
    mediaPlayer?.media()?.play(url)
    updatePlaybackState()
  }

  override fun pause() {
    mediaPlayer?.controls()?.pause()
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

  private fun updatePlaybackState() {
    _playbackState.update {
      it.copy(
        isPlaying = mediaPlayer?.status()?.isPlaying() ?: false,
        currentPosition = mediaPlayer?.status()?.time()?.coerceAtLeast(0) ?: 0L,
        duration = mediaPlayer?.status()?.length()?.coerceAtLeast(0) ?: 0L,
        playingUrl = playingUrl,
        buffering = it.buffering,
        playbackSpeed = mediaPlayer?.status()?.rate() ?: 1f
      )
    }
  }

  private fun startProgressUpdate() {
    val mediaPlayer = mediaPlayer ?: return
    progressJob?.cancel()
    progressJob =
      scope.launch {
        while (true) {
          updatePlaybackState()
          delay(1000)
        }
      }
  }

  private fun stopProgressUpdate() {
    progressJob?.cancel()
    progressJob = null
  }
}
