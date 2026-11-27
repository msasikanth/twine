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

package dev.sasikanth.rss.reader.data.sync

import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.network.fetcher.PodcastChaptersFetcher
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class PodcastChaptersCoordinator(
  private val podcastChaptersFetcher: PodcastChaptersFetcher,
  private val postContentRepository: PostContentRepository,
  private val dispatchersProvider: DispatchersProvider,
) {

  internal constructor(
    podcastChaptersFetcher: PodcastChaptersFetcher,
    postContentRepository: PostContentRepository,
    dispatchersProvider: DispatchersProvider,
    clock: Clock,
  ) : this(
    podcastChaptersFetcher = podcastChaptersFetcher,
    postContentRepository = postContentRepository,
    dispatchersProvider = dispatchersProvider,
  ) {
    this.clock = clock
  }

  private var clock: Clock = Clock.System
  private val stateMutex = Mutex()
  private val inFlightRequests = mutableMapOf<String, CompletableDeferred<List<MediaChapter>>>()
  private val failedAttempts = mutableMapOf<String, Instant>()

  suspend fun loadChaptersIfNeeded(
    postId: String,
    createdAt: Instant,
    chaptersUrl: String?,
    audioUrl: String?,
  ): List<MediaChapter> {
    if (postId.isBlank()) return emptyList()

    val cachedContent =
      withContext(dispatchersProvider.databaseRead) {
        postContentRepository.postContent(postId).firstOrNull()
      }
    cachedContent?.chaptersJson?.let {
      return decodeChaptersJson(it)
    }

    val initialChaptersUrl =
      chaptersUrl?.trim()?.ifBlank { null } ?: cachedContent?.chaptersUrl?.trim()?.ifBlank { null }
    val initialAudioUrl = audioUrl?.trim()?.ifBlank { null }
    if (initialChaptersUrl == null && initialAudioUrl == null) return emptyList()

    var leaderDeferred: CompletableDeferred<List<MediaChapter>>? = null
    var followerDeferred: CompletableDeferred<List<MediaChapter>>? = null

    stateMutex.withLock {
      val lastFailedAt = failedAttempts[postId]
      if (lastFailedAt != null && (clock.now() - lastFailedAt) < FAILED_ATTEMPT_COOLDOWN) {
        return emptyList()
      }

      val existing = inFlightRequests[postId]
      if (existing != null) {
        followerDeferred = existing
      } else {
        leaderDeferred =
          CompletableDeferred<List<MediaChapter>>().also { inFlightRequests[postId] = it }
      }
    }

    followerDeferred?.let {
      return it.await()
    }

    val activeDeferred = leaderDeferred!!
    return try {
      val latestContent =
        withContext(dispatchersProvider.databaseRead) {
          postContentRepository.postContent(postId).firstOrNull()
        }
      latestContent?.chaptersJson?.let { json ->
        return decodeChaptersJson(json).also { activeDeferred.complete(it) }
      }

      val effectiveChaptersUrl =
        chaptersUrl?.trim()?.ifBlank { null }
          ?: latestContent?.chaptersUrl?.trim()?.ifBlank { null }
      val effectiveAudioUrl = audioUrl?.trim()?.ifBlank { null }

      var resolvedChapters: List<MediaChapter>? = null
      var anyAttemptFailed = false
      var attemptedAny = false

      if (!effectiveChaptersUrl.isNullOrBlank()) {
        attemptedAny = true
        val fetched = podcastChaptersFetcher.fetchPodcasting2Chapters(effectiveChaptersUrl)
        if (fetched == null) {
          anyAttemptFailed = true
        } else if (fetched.isNotEmpty()) {
          resolvedChapters = fetched
        }
      }

      if (resolvedChapters == null && !effectiveAudioUrl.isNullOrBlank()) {
        attemptedAny = true
        val fetched = podcastChaptersFetcher.fetchEmbeddedMp3Chapters(effectiveAudioUrl)
        if (fetched == null) {
          anyAttemptFailed = true
        } else if (fetched.isNotEmpty()) {
          resolvedChapters = fetched
        }
      }

      val result = resolvedChapters.orEmpty()
      if (result.isNotEmpty()) {
        postContentRepository.upsertChapters(
          postId = postId,
          createdAt = createdAt,
          chaptersJson = chaptersSerializer.encodeToString(result),
        )
        stateMutex.withLock { failedAttempts.remove(postId) }
      } else if (attemptedAny && !anyAttemptFailed) {
        postContentRepository.upsertChapters(
          postId = postId,
          createdAt = createdAt,
          chaptersJson = "[]",
        )
        stateMutex.withLock { failedAttempts.remove(postId) }
      } else if (anyAttemptFailed) {
        stateMutex.withLock { failedAttempts[postId] = clock.now() }
      }

      activeDeferred.complete(result)
      result
    } catch (e: CancellationException) {
      activeDeferred.complete(emptyList())
      throw e
    } catch (_: Exception) {
      stateMutex.withLock { failedAttempts[postId] = clock.now() }
      activeDeferred.complete(emptyList())
      emptyList()
    } finally {
      withContext(NonCancellable) {
        stateMutex.withLock {
          if (inFlightRequests[postId] === activeDeferred) {
            inFlightRequests.remove(postId)
          }
        }
      }
    }
  }

  companion object {
    private val FAILED_ATTEMPT_COOLDOWN = 6.hours
    private val chaptersSerializer = Json {
      ignoreUnknownKeys = true
      encodeDefaults = false
    }

    fun decodeChaptersJson(chaptersJson: String?): List<MediaChapter> {
      if (chaptersJson.isNullOrBlank()) return emptyList()
      return runCatching { chaptersSerializer.decodeFromString<List<MediaChapter>>(chaptersJson) }
        .getOrDefault(emptyList())
    }
  }
}
