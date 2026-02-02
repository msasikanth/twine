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

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
  val playbackState: StateFlow<PlaybackState>

  fun play(url: String, title: String, artist: String, coverUrl: String?)

  fun pause()

  fun resume()

  fun seekTo(position: Long)

  fun setPlaybackSpeed(speed: Float)
}

data class PlaybackState(
  val isPlaying: Boolean,
  val currentPosition: Long,
  val duration: Long,
  val playingUrl: String? = null,
  val buffering: Boolean = false,
  val playbackSpeed: Float = 1f,
) {
  companion object {
    val Idle =
      PlaybackState(
        isPlaying = false,
        currentPosition = 0,
        duration = 0,
        buffering = false,
        playbackSpeed = 1f,
      )
  }
}
