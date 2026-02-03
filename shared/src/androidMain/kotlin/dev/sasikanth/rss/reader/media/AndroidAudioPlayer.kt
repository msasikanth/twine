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

import android.content.ComponentName
import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
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
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AndroidAudioPlayer(
  private val context: Context,
  private val dispatchersProvider: DispatchersProvider,
) : AudioPlayer {

  private val _playbackState = MutableStateFlow(PlaybackState.Idle)
  override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

  private val scope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
  private var controller: MediaController? = null
  private var progressJob: Job? = null
  private var sleepTimerJob: Job? = null
  private var sleepTimerRemainingMillis: Long? = null
  private var selectedSleepTimerOption: SleepTimerOption = SleepTimerOption.None

  init {
    scope.launch {
      val sessionToken = SessionToken(context, ComponentName(context, MediaService::class.java))
      controller = MediaController.Builder(context, sessionToken).buildAsync().await()
      controller?.addListener(
        object : Player.Listener {
          override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePlaybackState()
            if (isPlaying) {
              startProgressUpdate()
            } else {
              stopProgressUpdate()
            }
          }

          override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
            if (
              state == Player.STATE_ENDED &&
                _playbackState.value.sleepTimerRemaining == -1L // End of track marker
            ) {
              pause()
              setSleepTimer(SleepTimerOption.None)
            }
          }

          override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int,
          ) {
            updatePlaybackState()
          }
        }
      )
      updatePlaybackState()
    }
  }

  override fun play(url: String, title: String, artist: String, coverUrl: String?) {
    scope.launch {
      val mediaItem =
        MediaItem.Builder()
          .setMediaId(url)
          .setUri(url)
          .setMediaMetadata(
            MediaMetadata.Builder()
              .setTitle(title)
              .setArtist(artist)
              .setArtworkUri(coverUrl?.toUri())
              .build()
          )
          .build()

      controller?.setMediaItem(mediaItem)
      controller?.prepare()
      controller?.play()
    }
  }

  override fun pause() {
    controller?.pause()
  }

  override fun resume() {
    controller?.play()
  }

  override fun seekTo(position: Long) {
    controller?.seekTo(position)
  }

  override fun setPlaybackSpeed(speed: Float) {
    controller?.setPlaybackSpeed(speed)
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
        sleepTimerRemainingMillis = -1L // Use -1 as a marker for end of track
      }
      is SleepTimerOption.Minutes -> {
        val millis = option.minutes * 60 * 1000L
        sleepTimerRemainingMillis = millis
        sleepTimerJob =
          scope.launch {
            while (sleepTimerRemainingMillis!! > 0) {
              delay(1000)
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
    val player = controller ?: return
    _playbackState.update {
      it.copy(
        isPlaying = player.isPlaying,
        currentPosition = player.currentPosition,
        duration = player.duration.coerceAtLeast(0),
        playingUrl = player.currentMediaItem?.mediaId,
        buffering = player.playbackState == Player.STATE_BUFFERING,
        playbackSpeed = player.playbackParameters.speed,
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
          delay(1000)
        }
      }
  }

  private fun stopProgressUpdate() {
    progressJob?.cancel()
    progressJob = null
  }
}
