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
import dev.sasikanth.rss.reader.core.model.local.MediaChapter
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
import dev.sasikanth.rss.reader.core.network.fetcher.PodcastChaptersFetcher
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
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.data.repository.PostRepository
import dev.sasikanth.rss.reader.data.repository.ReadingHistoryRepository
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SourceRepository
import dev.sasikanth.rss.reader.data.repository.SyncRepository
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.toByteArray
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class PodcastChaptersCoordinatorTest {

  private lateinit var driver: JdbcSqliteDriver
  private lateinit var database: ReaderDatabase
  private lateinit var rssRepository: RssRepository
  private lateinit var postContentRepository: PostContentRepository

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
    val readingTimeCalculator = ReadingTimeCalculator(testDispatchersProvider)
    postContentRepository =
      PostContentRepository(
        database.postContentQueries,
        testDispatchersProvider,
        readingTimeCalculator,
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
        readingTimeCalculator = readingTimeCalculator,
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

  private fun createCoordinator(
    clock: Clock = Clock.System,
    handler: suspend (url: String) -> Pair<HttpStatusCode, ByteArray>,
  ): PodcastChaptersCoordinator {
    val httpClient =
      HttpClient(MockEngine) {
        engine {
          dispatcher = testDispatchersProvider.io
          addHandler { request ->
            val (status, body) = handler(request.url.toString())
            respond(content = ByteReadChannel(body), status = status)
          }
        }
      }
    return PodcastChaptersCoordinator(
      podcastChaptersFetcher = PodcastChaptersFetcher(httpClient, testDispatchersProvider),
      postContentRepository = postContentRepository,
      dispatchersProvider = testDispatchersProvider,
      clock = clock,
    )
  }

  private suspend fun insertPodcastPost(link: String, chaptersUrl: String?): String {
    rssRepository.upsertFeedWithPosts(
      feedPayload =
        FeedPayload(
          name = "Podcast",
          icon = "",
          description = "",
          homepageLink = "https://podcast.example.com",
          link = "https://podcast.example.com/rss.xml",
          posts =
            flowOf(
              PostPayload(
                title = "Ep",
                link = link,
                description = "Notes",
                rawContent = "<p>Notes</p>",
                fullContent = null,
                imageUrl = null,
                audioUrl = "https://cdn.example.com/ep.mp3",
                date = 1_000L,
                commentsLink = null,
                isDateParsedCorrectly = true,
                chaptersUrl = chaptersUrl,
              )
            ),
        )
    )
    return nameBasedUuidOf(link).toString()
  }

  @Test
  fun deduplicatesConcurrentRequestsPrefersPodcasting2AndCachesResult() = runTest {
    val postLink = "https://podcast.example.com/ep1"
    val chaptersUrl = "https://cdn.example.com/ep1.chapters.json"
    val postId = insertPodcastPost(link = postLink, chaptersUrl = chaptersUrl)

    var jsonRequests = 0
    var mp3Requests = 0
    val release = CompletableDeferred<Unit>()
    val coordinator = createCoordinator { url ->
      if (url == chaptersUrl) {
        jsonRequests++
        release.await()
        HttpStatusCode.OK to
          """{"version":"1.2.0","chapters":[{"startTime":0,"title":"Intro"},{"startTime":60,"title":"Sponsor: Acme"}]}"""
            .toByteArray()
      } else {
        mp3Requests++
        HttpStatusCode.OK to ByteArray(0)
      }
    }

    val jobs =
      List(3) {
        async {
          coordinator.loadChaptersIfNeeded(
            postId = postId,
            createdAt = Instant.fromEpochMilliseconds(1_000L),
            chaptersUrl = null,
            audioUrl = "https://cdn.example.com/ep.mp3",
          )
        }
      }
    release.complete(Unit)
    val results = jobs.awaitAll()

    assertEquals(1, jsonRequests)
    assertEquals(0, mp3Requests)
    results.forEach { list ->
      assertEquals(
        listOf(
          MediaChapter(timestampMs = 0L, title = "Intro"),
          MediaChapter(timestampMs = 60_000L, title = "Sponsor: Acme", isSponsor = true),
        ),
        list,
      )
    }

    // Subsequent call reads DB cache without hitting network
    val cached =
      coordinator.loadChaptersIfNeeded(
        postId = postId,
        createdAt = Instant.fromEpochMilliseconds(1_000L),
        chaptersUrl = null,
        audioUrl = "https://cdn.example.com/ep.mp3",
      )
    assertEquals(1, jsonRequests)
    assertEquals(2, cached.size)
  }

  @Test
  fun emptySuccessCachesEmptyJsonAndTransientFailureAppliesCooldown() = runTest {
    val emptyPostId = insertPodcastPost("https://podcast.example.com/empty", chaptersUrl = null)
    var requests = 0
    val emptyCoordinator = createCoordinator {
      requests++
      HttpStatusCode.OK to byteArrayOf(0xFF.toByte(), 0xFB.toByte(), 0x00)
    }

    assertTrue(
      emptyCoordinator
        .loadChaptersIfNeeded(
          postId = emptyPostId,
          createdAt = Instant.fromEpochMilliseconds(1_000L),
          chaptersUrl = null,
          audioUrl = "https://cdn.example.com/empty.mp3",
        )
        .isEmpty()
    )
    assertEquals("[]", postContentRepository.postContent(emptyPostId).firstOrNull()?.chaptersJson)
    assertTrue(
      emptyCoordinator
        .loadChaptersIfNeeded(
          postId = emptyPostId,
          createdAt = Instant.fromEpochMilliseconds(1_000L),
          chaptersUrl = null,
          audioUrl = "https://cdn.example.com/empty.mp3",
        )
        .isEmpty()
    )
    assertEquals(1, requests)

    val failingPostId = insertPodcastPost("https://podcast.example.com/fail", chaptersUrl = null)
    val clock = MutableClock(Instant.fromEpochMilliseconds(1_000_000L))
    var failRequests = 0
    val failCoordinator =
      createCoordinator(clock = clock) {
        failRequests++
        HttpStatusCode.InternalServerError to ByteArray(0)
      }

    failCoordinator.loadChaptersIfNeeded(
      postId = failingPostId,
      createdAt = Instant.fromEpochMilliseconds(1_000L),
      chaptersUrl = null,
      audioUrl = "https://cdn.example.com/fail.mp3",
    )
    assertEquals(null, postContentRepository.postContent(failingPostId).firstOrNull()?.chaptersJson)

    // Still in cooldown
    clock.currentTime += 2.hours
    failCoordinator.loadChaptersIfNeeded(
      postId = failingPostId,
      createdAt = Instant.fromEpochMilliseconds(1_000L),
      chaptersUrl = null,
      audioUrl = "https://cdn.example.com/fail.mp3",
    )
    assertEquals(1, failRequests)

    // After 6h cooldown
    clock.currentTime += 5.hours
    failCoordinator.loadChaptersIfNeeded(
      postId = failingPostId,
      createdAt = Instant.fromEpochMilliseconds(1_000L),
      chaptersUrl = null,
      audioUrl = "https://cdn.example.com/fail.mp3",
    )
    assertEquals(2, failRequests)
  }
}
