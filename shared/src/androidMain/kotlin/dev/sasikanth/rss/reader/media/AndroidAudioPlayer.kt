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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
class AndroidAudioPlayer(private val context: Context) : AudioPlayer {

  private val _playbackState = MutableStateFlow(PlaybackState.Idle)
  override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var controller: MediaController? = null
  private var progressJob: Job? = null

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
          }

          override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
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

  private fun updatePlaybackState() {
    val player = controller ?: return
    _playbackState.update {
      it.copy(
        isPlaying = player.isPlaying,
        currentPosition = player.currentPosition,
        duration = player.duration.coerceAtLeast(0),
        playingUrl = player.currentMediaItem?.mediaId,
        buffering = player.playbackState == Player.STATE_BUFFERING,
        playbackSpeed = player.playbackParameters.speed
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
