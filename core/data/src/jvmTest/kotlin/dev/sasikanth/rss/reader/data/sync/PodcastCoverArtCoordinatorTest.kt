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

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.fetcher.PodcastArtworkEnricher
import dev.sasikanth.rss.reader.data.database.BlockedWord
import dev.sasikanth.rss.reader.data.database.Feed
import dev.sasikanth.rss.reader.data.database.FeedGroup
import dev.sasikanth.rss.reader.data.database.Post
import dev.sasikanth.rss.reader.data.database.PostContent
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import dev.sasikanth.rss.reader.data.database.ReadingHistory
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.database.User
import dev.sasikanth.rss.reader.data.database.adapter.PostFlagsAdapter
import dev.sasikanth.rss.reader.data.repository.BookmarkRepository
import dev.sasikanth.rss.reader.data.repository.FeedGroupRepository
import dev.sasikanth.rss.reader.data.repository.FeedRepository
import dev.sasikanth.rss.reader.data.repository.NoopWidgetUpdater
import dev.sasikanth.rss.reader.data.repository.PostRepository
import dev.sasikanth.rss.reader.data.repository.ReadingHistoryRepository
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SourceRepository
import dev.sasikanth.rss.reader.data.repository.SyncRepository
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PodcastCoverArtCoordinatorTest {

  private lateinit var driver: JdbcSqliteDriver
  private lateinit var database: ReaderDatabase
  private lateinit var rssRepository: RssRepository

  private val testDispatchersProvider =
    object : DispatchersProvider {
      override val main: CoroutineDispatcher = UnconfinedTestDispatcher()
      override val io: CoroutineDispatcher = UnconfinedTestDispatcher()
      override val default: CoroutineDispatcher = UnconfinedTestDispatcher()
      override val databaseRead: CoroutineDispatcher = UnconfinedTestDispatcher()
      override val databaseWrite: CoroutineDispatcher = UnconfinedTestDispatcher()
    }

  private val dateAdapter =
    object : ColumnAdapter<Instant, Long> {
      override fun decode(databaseValue: Long): Instant =
        Instant.fromEpochMilliseconds(databaseValue)

      override fun encode(value: Instant): Long = value.toEpochMilliseconds()
    }

  private class MutableClock(var currentTime: Instant) : Clock {
    override fun now(): Instant = currentTime
  }

  @BeforeTest
  fun setup() {
    driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    ReaderDatabase.Schema.create(driver)
    database =
      ReaderDatabase(
        driver = driver,
        postAdapter =
          Post.Adapter(
            postDateAdapter = dateAdapter,
            createdAtAdapter = dateAdapter,
            updatedAtAdapter = dateAdapter,
            syncedAtAdapter = dateAdapter,
            flagsAdapter = PostFlagsAdapter,
          ),
        feedAdapter =
          Feed.Adapter(
            createdAtAdapter = dateAdapter,
            pinnedAtAdapter = dateAdapter,
            lastCleanUpAtAdapter = dateAdapter,
            lastUpdatedAtAdapter = dateAdapter,
          ),
        feedGroupAdapter =
          FeedGroup.Adapter(
            createdAtAdapter = dateAdapter,
            updatedAtAdapter = dateAdapter,
            pinnedAtAdapter = dateAdapter,
          ),
        postContentAdapter = PostContent.Adapter(createdAtAdapter = dateAdapter),
        blockedWordAdapter = BlockedWord.Adapter(updatedAtAdapter = dateAdapter),
        userAdapter = User.Adapter(serviceTypeAdapter = EnumColumnAdapter()),
        readingHistoryAdapter =
          ReadingHistory.Adapter(readAtAdapter = dateAdapter, postDateAdapter = dateAdapter),
      )
    rssRepository =
      RssRepository(
        transactionRunner = TransactionRunner(database),
        feedQueries = database.feedQueries,
        postQueries = database.postQueries,
        postContentQueries = database.postContentQueries,
        postSearchFTSQueries = database.postSearchFTSQueries,
        feedGroupQueries = database.feedGroupQueries,
        feedGroupFeedQueries = database.feedGroupFeedQueries,
        blockedWordsQueries = database.blockedWordsQueries,
        appConfigQueries = database.appConfigQueries,
        readingHistoryQueries = database.readingHistoryQueries,
        readingTimeCalculator = ReadingTimeCalculator(testDispatchersProvider),
        widgetUpdater = NoopWidgetUpdater,
        dispatchersProvider = testDispatchersProvider,
        bookmarkRepository =
          BookmarkRepository(
            database.bookmarkQueries,
            database.postQueries,
            TransactionRunner(database),
            NoopWidgetUpdater,
            testDispatchersProvider,
          ),
        readingHistoryRepository =
          ReadingHistoryRepository(database.readingHistoryQueries, testDispatchersProvider),
        feedGroupRepository =
          FeedGroupRepository(
            database.feedGroupQueries,
            database.feedGroupFeedQueries,
            TransactionRunner(database),
            NoopWidgetUpdater,
            testDispatchersProvider,
          ),
        sourceRepository = SourceRepository(database.sourceQueries, testDispatchersProvider),
        feedRepository =
          FeedRepository(
            database.feedQueries,
            database.feedSearchFTSQueries,
            TransactionRunner(database),
            testDispatchersProvider,
          ),
        postRepository = PostRepository(database.postQueries, testDispatchersProvider),
        syncRepository =
          SyncRepository(
            feedQueries = database.feedQueries,
            postQueries = database.postQueries,
            blockedWordsQueries = database.blockedWordsQueries,
            transactionRunner = TransactionRunner(database),
            widgetUpdater = NoopWidgetUpdater,
            dispatchersProvider = testDispatchersProvider,
          ),
      )
  }

  @AfterTest
  fun tearDown() {
    driver.close()
  }

  @Test
  fun deduplicatesConcurrentRequestsAndPersistsCoverArt() = runTest {
    val feedLink = "https://podcast.example.com/rss.xml"
    val feedId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          FeedPayload(
            name = "Podcast",
            icon = "data:image/png;base64,AAAA",
            description = "",
            homepageLink = "https://podcast.example.com",
            link = feedLink,
            posts =
              flowOf(
                PostPayload(
                  title = "Ep 1",
                  link = "https://podcast.example.com/ep1",
                  description = "Notes",
                  rawContent = null,
                  fullContent = null,
                  imageUrl = null,
                  audioUrl = "https://podcast.example.com/ep1.mp3",
                  date = 1_000L,
                  commentsLink = null,
                  isDateParsedCorrectly = true,
                )
              ),
          )
      )

    var requestCount = 0
    val releaseRequest = CompletableDeferred<Unit>()
    val rssXml =
      """
      <?xml version="1.0" encoding="UTF-8"?>
      <rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd">
        <channel>
          <title>Podcast</title>
          <itunes:image href="https://podcast.example.com/cover-3000.jpg" />
        </channel>
      </rss>
      """
        .trimIndent()

    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = testDispatchersProvider.io
          addHandler {
            requestCount++
            releaseRequest.await()
            respond(
              content = ByteReadChannel(rssXml.toByteArray()),
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"),
            )
          }
        }
      }

    val coordinator =
      PodcastCoverArtCoordinator(
        podcastArtworkEnricher = PodcastArtworkEnricher(httpClient, testDispatchersProvider),
        rssRepository = rssRepository,
        dispatchersProvider = testDispatchersProvider,
      )

    val firstCall = async { coordinator.enrichFeedIfNeeded(feedId, feedLink) }
    val secondCall = async { coordinator.enrichFeedIfNeeded(feedId, feedLink) }
    releaseRequest.complete(Unit)

    val results = awaitAll(firstCall, secondCall)

    assertEquals(1, requestCount)
    assertEquals(
      listOf(
        "https://podcast.example.com/cover-3000.jpg",
        "https://podcast.example.com/cover-3000.jpg",
      ),
      results,
    )
    assertEquals(
      "https://podcast.example.com/cover-3000.jpg",
      rssRepository.feed(feedId)?.coverArtUrl,
    )

    val cached = coordinator.enrichFeedIfNeeded(feedId, feedLink)
    assertEquals("https://podcast.example.com/cover-3000.jpg", cached)
    assertEquals(1, requestCount)
  }

  @Test
  fun failedAttemptsRespectSixHourCooldownAndBatchEnrichmentTargetsAudioFeedsOnly() = runTest {
    val podcastLink = "https://podcast.example.com/rss.xml"
    val articleLink = "https://blog.example.com/rss.xml"

    val podcastFeedId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          FeedPayload(
            name = "Podcast",
            icon = "",
            description = "",
            homepageLink = "https://podcast.example.com",
            link = podcastLink,
            posts =
              flowOf(
                PostPayload(
                  title = "Ep 1",
                  link = "https://podcast.example.com/ep1",
                  description = "Notes",
                  rawContent = null,
                  fullContent = null,
                  imageUrl = null,
                  audioUrl = "https://podcast.example.com/ep1.mp3",
                  date = 1_000L,
                  commentsLink = null,
                  isDateParsedCorrectly = true,
                )
              ),
          )
      )
    rssRepository.upsertFeedWithPosts(
      feedPayload =
        FeedPayload(
          name = "Blog",
          icon = "",
          description = "",
          homepageLink = "https://blog.example.com",
          link = articleLink,
          posts =
            flowOf(
              PostPayload(
                title = "Article 1",
                link = "https://blog.example.com/post1",
                description = "Body",
                rawContent = null,
                fullContent = null,
                imageUrl = null,
                audioUrl = null,
                date = 1_000L,
                commentsLink = null,
                isDateParsedCorrectly = true,
              )
            ),
        )
    )

    var shouldReturnArtwork = false
    var requestCount = 0
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = testDispatchersProvider.io
          addHandler { request ->
            requestCount++
            if (shouldReturnArtwork && request.url.toString() == podcastLink) {
              respond(
                content =
                  ByteReadChannel(
                    """<?xml version="1.0"?><rss version="2.0" xmlns:itunes="http://www.itunes.com/dtds/podcast-1.0.dtd"><channel><itunes:image href="https://podcast.example.com/recovered.jpg"/></channel></rss>"""
                      .toByteArray()
                  ),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/rss+xml"),
              )
            } else {
              respond(content = ByteReadChannel("".toByteArray()), status = HttpStatusCode.NotFound)
            }
          }
        }
      }

    val clock = MutableClock(Instant.fromEpochSeconds(1_000_000))
    val coordinator =
      PodcastCoverArtCoordinator(
        podcastArtworkEnricher = PodcastArtworkEnricher(httpClient, testDispatchersProvider),
        rssRepository = rssRepository,
        dispatchersProvider = testDispatchersProvider,
        clock = clock,
      )

    coordinator.enrichAudioFeedsMissingCoverArt(limit = 10)
    assertEquals(1, requestCount)
    assertNull(rssRepository.feed(podcastFeedId)?.coverArtUrl)

    shouldReturnArtwork = true
    clock.currentTime = clock.currentTime.plus(5.hours + 59.minutes)
    assertNull(coordinator.enrichFeedIfNeeded(podcastFeedId, podcastLink))
    assertEquals(1, requestCount)

    clock.currentTime = clock.currentTime.plus(2.minutes)
    assertEquals(
      "https://podcast.example.com/recovered.jpg",
      coordinator.enrichFeedIfNeeded(podcastFeedId, podcastLink),
    )
    assertEquals(2, requestCount)
    assertEquals(
      "https://podcast.example.com/recovered.jpg",
      rssRepository.feed(podcastFeedId)?.coverArtUrl,
    )
  }
}
