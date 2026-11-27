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
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.PodcastChaptersCoordinator
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@AppScope
class AudioSponsorSkipper
internal constructor(
  private val audioPlayer: AudioPlayer,
  private val skipSponsorChaptersFlow: Flow<Boolean>,
  private val loadCachedChapters: suspend (postId: String) -> List<MediaChapter>,
  private val scope: CoroutineScope,
) {

  @Inject
  constructor(
    audioPlayer: AudioPlayer,
    settingsRepository: SettingsRepository,
    postContentRepository: PostContentRepository,
    dispatchersProvider: DispatchersProvider,
  ) : this(
    audioPlayer = audioPlayer,
    skipSponsorChaptersFlow = settingsRepository.skipSponsorChapters,
    loadCachedChapters = { postId ->
      val content = postContentRepository.postContent(postId).firstOrNull()
      PodcastChaptersCoordinator.decodeChaptersJson(content?.chaptersJson)
    },
    scope = CoroutineScope(SupervisorJob() + dispatchersProvider.default),
  )

  private val chaptersByPostId = MutableStateFlow<Map<String, List<MediaChapter>>>(emptyMap())
  private val allowedSponsorChapter = MutableStateFlow<AllowedSponsorChapter?>(null)
  private val dbAttemptedPostIds = MutableStateFlow<Set<String>>(emptySet())
  private var observeJob: Job? = null

  fun registerChapters(postId: String, chapters: List<MediaChapter>) {
    if (postId.isBlank()) return
    chaptersByPostId.update { current -> current + (postId to chapters) }
    dbAttemptedPostIds.update { current -> current + postId }
  }

  fun allowSponsorChapter(postId: String, sponsorTimestampMs: Long) {
    if (postId.isBlank()) return
    allowedSponsorChapter.value =
      AllowedSponsorChapter(postId = postId, timestampMs = sponsorTimestampMs, entered = false)
  }

  fun observe() {
    if (observeJob?.isActive == true) return

    observeJob =
      scope.launch {
        var lastSkippedChapter: Pair<String, Long>? = null

        combine(audioPlayer.playbackState, skipSponsorChaptersFlow, chaptersByPostId) {
            state,
            skipEnabled,
            chaptersMap ->
            SkipperSnapshot(state, skipEnabled, chaptersMap)
          }
          .collect { (state, skipEnabled, chaptersMap) ->
            val postId = state.playingPostId
            if (postId.isNullOrBlank()) {
              allowedSponsorChapter.value = null
              lastSkippedChapter = null
              return@collect
            }

            allowedSponsorChapter.value?.let { allowed ->
              if (allowed.postId != postId) {
                allowedSponsorChapter.value = null
              }
            }
            if (lastSkippedChapter != null && lastSkippedChapter!!.first != postId) {
              lastSkippedChapter = null
            }

            val chapters =
              chaptersMap[postId]
                ?: if (postId !in dbAttemptedPostIds.value) {
                  dbAttemptedPostIds.update { it + postId }
                  val loaded = loadCachedChapters(postId)
                  if (loaded.isNotEmpty()) {
                    chaptersByPostId.update { current ->
                      if (postId in current) current else current + (postId to loaded)
                    }
                  }
                  chaptersByPostId.value[postId] ?: loaded
                } else {
                  emptyList()
                }

            if (chapters.isEmpty()) return@collect

            val position = state.currentPosition
            val activeIndex = chapters.indexOfLast { it.timestampMs <= position }
            val activeChapter =
              activeIndex
                .takeIf { it >= 0 }
                ?.let { idx ->
                  val candidate = chapters[idx]
                  val endMs = candidate.endTimestampMs
                  if (endMs != null && position >= endMs) null else candidate
                }

            val currentAllowed = allowedSponsorChapter.value
            if (currentAllowed != null && currentAllowed.postId == postId) {
              if (
                activeChapter != null && activeChapter.timestampMs == currentAllowed.timestampMs
              ) {
                if (!currentAllowed.entered) {
                  allowedSponsorChapter.value = currentAllowed.copy(entered = true)
                }
              } else if (currentAllowed.entered) {
                allowedSponsorChapter.value = null
              }
            }

            if (
              lastSkippedChapter != null &&
                (activeChapter == null ||
                  !activeChapter.isSponsor ||
                  lastSkippedChapter != (postId to activeChapter.timestampMs))
            ) {
              lastSkippedChapter = null
            }

            if (!skipEnabled || !state.isPlaying || state.buffering) return@collect
            if (activeChapter == null || !activeChapter.isSponsor) return@collect

            val updatedAllowed = allowedSponsorChapter.value
            if (
              updatedAllowed != null &&
                updatedAllowed.postId == postId &&
                updatedAllowed.timestampMs == activeChapter.timestampMs
            ) {
              return@collect
            }

            if (lastSkippedChapter == (postId to activeChapter.timestampMs)) {
              return@collect
            }

            var nextNonSponsorIdx = activeIndex
            while (nextNonSponsorIdx < chapters.size && chapters[nextNonSponsorIdx].isSponsor) {
              nextNonSponsorIdx++
            }
            val targetMs =
              if (nextNonSponsorIdx < chapters.size) {
                chapters[nextNonSponsorIdx].timestampMs
              } else {
                chapters[nextNonSponsorIdx - 1].endTimestampMs
              } ?: return@collect

            if (targetMs <= position + MIN_SKIP_ADVANCE_MS) return@collect
            if (state.duration > 0L && targetMs >= state.duration - END_OF_TRACK_GUARD_MS) {
              return@collect
            }

            lastSkippedChapter = postId to activeChapter.timestampMs
            audioPlayer.seekTo(targetMs)
          }
      }
  }

  private data class AllowedSponsorChapter(
    val postId: String,
    val timestampMs: Long,
    val entered: Boolean,
  )

  private data class SkipperSnapshot(
    val state: PlaybackState,
    val skipEnabled: Boolean,
    val chaptersMap: Map<String, List<MediaChapter>>,
  )

  companion object {
    private const val MIN_SKIP_ADVANCE_MS = 500L
    private const val END_OF_TRACK_GUARD_MS = 1000L
  }
}
