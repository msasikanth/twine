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

import dev.sasikanth.rss.reader.core.network.fetcher.PodcastArtworkEnricher
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class PodcastCoverArtCoordinator(
  private val podcastArtworkEnricher: PodcastArtworkEnricher,
  private val rssRepository: RssRepository,
  private val dispatchersProvider: DispatchersProvider,
) {

  internal constructor(
    podcastArtworkEnricher: PodcastArtworkEnricher,
    rssRepository: RssRepository,
    dispatchersProvider: DispatchersProvider,
    clock: Clock,
  ) : this(
    podcastArtworkEnricher = podcastArtworkEnricher,
    rssRepository = rssRepository,
    dispatchersProvider = dispatchersProvider,
  ) {
    this.clock = clock
  }

  private companion object {
    private val FAILED_ATTEMPT_COOLDOWN = 6.hours
    private const val CONCURRENCY_LIMIT = 3
  }

  private var clock: Clock = Clock.System
  private val stateMutex = Mutex()
  private val inFlightRequests = mutableMapOf<String, CompletableDeferred<String?>>()
  private val failedAttempts = mutableMapOf<String, Instant>()

  suspend fun enrichFeedIfNeeded(feedId: String, feedLink: String): String? {
    if (feedId.isBlank() || feedLink.isBlank()) return null

    var leaderDeferred: CompletableDeferred<String?>? = null
    var followerDeferred: CompletableDeferred<String?>? = null

    stateMutex.withLock {
      val lastFailedAt = failedAttempts[feedId]
      if (lastFailedAt != null && (clock.now() - lastFailedAt) < FAILED_ATTEMPT_COOLDOWN) {
        return null
      }

      val existing = inFlightRequests[feedId]
      if (existing != null) {
        followerDeferred = existing
      } else {
        leaderDeferred = CompletableDeferred<String?>().also { inFlightRequests[feedId] = it }
      }
    }

    followerDeferred?.let {
      return it.await()
    }

    val activeDeferred = leaderDeferred!!
    return try {
      val existingCoverArt =
        withContext(dispatchersProvider.databaseRead) { rssRepository.feed(feedId)?.coverArtUrl }
      if (!existingCoverArt.isNullOrBlank()) {
        activeDeferred.complete(existingCoverArt)
        return existingCoverArt
      }

      val fetchedCoverArt = podcastArtworkEnricher.fetchCoverArtUrl(feedLink)
      if (!fetchedCoverArt.isNullOrBlank()) {
        rssRepository.updateFeedCoverArt(feedId = feedId, coverArtUrl = fetchedCoverArt)
        stateMutex.withLock { failedAttempts.remove(feedId) }
        activeDeferred.complete(fetchedCoverArt)
        fetchedCoverArt
      } else {
        stateMutex.withLock { failedAttempts[feedId] = clock.now() }
        activeDeferred.complete(null)
        null
      }
    } catch (e: CancellationException) {
      activeDeferred.complete(null)
      throw e
    } catch (_: Exception) {
      stateMutex.withLock { failedAttempts[feedId] = clock.now() }
      activeDeferred.complete(null)
      null
    } finally {
      withContext(NonCancellable) {
        stateMutex.withLock {
          if (inFlightRequests[feedId] === activeDeferred) {
            inFlightRequests.remove(feedId)
          }
        }
      }
    }
  }

  suspend fun enrichAudioFeedsMissingCoverArt(limit: Int = 10) {
    if (limit <= 0) return

    val feedsMissingArt = rssRepository.audioFeedsMissingCoverArt(limit = limit.toLong())
    if (feedsMissingArt.isEmpty()) return

    coroutineScope {
      feedsMissingArt.chunked(CONCURRENCY_LIMIT).forEach { chunk ->
        chunk
          .map { (feedId, feedLink) ->
            async { enrichFeedIfNeeded(feedId = feedId, feedLink = feedLink) }
          }
          .awaitAll()
      }
    }
  }
}
