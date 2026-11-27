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
import dev.sasikanth.rss.reader.data.repository.PostRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@AppScope
class AudioProgressObserver
internal constructor(
  private val audioPlayer: AudioPlayer,
  private val saveAudioProgress: suspend (postId: String, progress: Long, duration: Long) -> Unit,
  private val scope: CoroutineScope,
) {

  @Inject
  constructor(
    audioPlayer: AudioPlayer,
    postRepository: PostRepository,
    dispatchersProvider: DispatchersProvider,
  ) : this(
    audioPlayer = audioPlayer,
    saveAudioProgress = { postId, progress, duration ->
      postRepository.updateAudioProgress(
        postId = postId,
        audioProgress = progress,
        audioDuration = duration,
      )
    },
    scope = CoroutineScope(SupervisorJob() + dispatchersProvider.io),
  )

  private var observeJob: Job? = null
  private var periodicSaveJob: Job? = null

  fun observe() {
    if (observeJob?.isActive == true) return

    observeJob =
      scope.launch {
        var lastActive: ActiveTrack? = null
        var wasPlaying = false
        var hasPlayedCurrentTrack = false

        audioPlayer.playbackState.collect { state ->
          val previous = lastActive
          val currentPostId = state.playingPostId
          val switchedPost = previous != null && previous.postId != currentPostId

          val trackToSaveBeforeSwitch =
            if (switchedPost && (previous.duration > 0L || hasPlayedCurrentTrack)) {
              previous
            } else {
              null
            }

          if (switchedPost) {
            hasPlayedCurrentTrack = false
          }
          if (state.isPlaying && currentPostId != null) {
            hasPlayedCurrentTrack = true
          }

          val currentTrack =
            if (currentPostId != null) {
              val samePost = previous?.postId == currentPostId
              val resolvedDuration =
                when {
                  state.duration > 0L -> state.duration
                  samePost -> previous.duration
                  else -> 0L
                }
              val resolvedPosition =
                when {
                  state.currentPosition > 0L || state.duration > 0L -> state.currentPosition
                  samePost -> previous.position
                  else -> 0L
                }
              ActiveTrack(
                postId = currentPostId,
                position = resolvedPosition,
                duration = resolvedDuration,
              )
            } else {
              null
            }

          val isPlayingNow = state.isPlaying && currentPostId != null
          if (!isPlayingNow || switchedPost) {
            periodicSaveJob?.cancel()
            periodicSaveJob = null
          }

          if (trackToSaveBeforeSwitch != null) {
            saveTrackProgressAsync(trackToSaveBeforeSwitch)
          } else if (wasPlaying && !state.isPlaying && currentTrack != null) {
            saveTrackProgressAsync(currentTrack)
          }

          lastActive = currentTrack

          if (isPlayingNow && (periodicSaveJob == null || !wasPlaying || switchedPost)) {
            periodicSaveJob =
              scope.launch {
                while (true) {
                  delay(PROGRESS_SAVE_INTERVAL)
                  val latestState = audioPlayer.playbackState.value
                  val latestPostId = latestState.playingPostId ?: continue
                  saveTrackProgress(
                    ActiveTrack(
                      postId = latestPostId,
                      position = latestState.currentPosition,
                      duration = latestState.duration,
                    )
                  )
                }
              }
          }

          wasPlaying = isPlayingNow
        }
      }
  }

  private fun saveTrackProgressAsync(track: ActiveTrack) {
    if (track.position <= 0L) return
    scope.launch { saveTrackProgress(track) }
  }

  private suspend fun saveTrackProgress(track: ActiveTrack) {
    if (track.position <= 0L) return
    try {
      saveAudioProgress(track.postId, track.position, track.duration)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Logger.e(e) { "Failed to save audio progress for post=${track.postId}" }
    }
  }

  private data class ActiveTrack(val postId: String, val position: Long, val duration: Long)

  companion object {
    private val PROGRESS_SAVE_INTERVAL = 10.seconds
  }
}
