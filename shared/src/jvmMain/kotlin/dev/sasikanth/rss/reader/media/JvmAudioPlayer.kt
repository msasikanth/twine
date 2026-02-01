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

import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class JvmAudioPlayer : AudioPlayer {
  override val playbackState: StateFlow<PlaybackState> = MutableStateFlow(PlaybackState.Idle)

  override fun play(url: String, title: String, artist: String, coverUrl: String?) {}

  override fun pause() {}

  override fun resume() {}

  override fun seekTo(position: Long) {}
}
