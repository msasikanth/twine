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

package dev.sasikanth.rss.reader.data.sync.miniflux

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.sasikanth.rss.reader.app.AppInfo
import dev.sasikanth.rss.reader.core.model.local.ServiceType
import dev.sasikanth.rss.reader.core.model.local.User
import dev.sasikanth.rss.reader.core.network.FullArticleFetcher
import dev.sasikanth.rss.reader.core.network.miniflux.MinifluxSource
import dev.sasikanth.rss.reader.core.network.parser.common.ArticleHtmlParser
import dev.sasikanth.rss.reader.data.database.BlockedWord
import dev.sasikanth.rss.reader.data.database.Feed
import dev.sasikanth.rss.reader.data.database.FeedGroup
import dev.sasikanth.rss.reader.data.database.Post
import dev.sasikanth.rss.reader.data.database.PostContent
import dev.sasikanth.rss.reader.data.database.ReaderDatabase
import dev.sasikanth.rss.reader.data.database.ReadingHistory
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.database.adapter.DateAdapter
import dev.sasikanth.rss.reader.data.database.adapter.PostFlagsAdapter
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

@OptIn(ExperimentalCoroutinesApi::class)
class MinifluxSyncCoordinatorTest {

  private lateinit var database: ReaderDatabase
  private lateinit var rssRepository: RssRepository
  private lateinit var userRepository: UserRepository
  private lateinit var settingsRepository: SettingsRepository
  private lateinit var refreshPolicy: RefreshPolicy
  private lateinit var minifluxSource: MinifluxSource
  private lateinit var syncCoordinator: MinifluxSyncCoordinator

  private val testDispatcher = UnconfinedTestDispatcher()
  private val testDispatchersProvider =
    object : DispatchersProvider {
      override val main: CoroutineDispatcher = testDispatcher
      override val io: CoroutineDispatcher = testDispatcher
      override val default: CoroutineDispatcher = testDispatcher
      override val databaseRead: CoroutineDispatcher = testDispatcher
      override val databaseWrite: CoroutineDispatcher = testDispatcher
    }

  private val dataStore: DataStore<Preferences> =
    PreferenceDataStoreFactory.create { File("build/test.preferences_pb") }

  @BeforeTest
  fun setup() {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    ReaderDatabase.Schema.create(driver)
    database =
      ReaderDatabase(
        driver = driver,
        postAdapter =
          Post.Adapter(
            postDateAdapter = DateAdapter,
            createdAtAdapter = DateAdapter,
            updatedAtAdapter = DateAdapter,
            syncedAtAdapter = DateAdapter,
            flagsAdapter = PostFlagsAdapter,
          ),
        feedAdapter =
          Feed.Adapter(
            createdAtAdapter = DateAdapter,
            pinnedAtAdapter = DateAdapter,
            lastCleanUpAtAdapter = DateAdapter,
            lastUpdatedAtAdapter = DateAdapter,
          ),
        feedGroupAdapter =
          FeedGroup.Adapter(
            createdAtAdapter = DateAdapter,
            updatedAtAdapter = DateAdapter,
            pinnedAtAdapter = DateAdapter,
          ),
        postContentAdapter = PostContent.Adapter(createdAtAdapter = DateAdapter),
        blockedWordAdapter = BlockedWord.Adapter(updatedAtAdapter = DateAdapter),
        userAdapter =
          dev.sasikanth.rss.reader.data.database.User.Adapter(
            serviceTypeAdapter = EnumColumnAdapter()
          ),
        readingHistoryAdapter =
          ReadingHistory.Adapter(readAtAdapter = DateAdapter, postDateAdapter = DateAdapter),
      )

    rssRepository =
      RssRepository(
        transactionRunner = TransactionRunner(database),
        feedQueries = database.feedQueries,
        postQueries = database.postQueries,
        postContentQueries = database.postContentQueries,
        postSearchFTSQueries = database.postSearchFTSQueries,
        bookmarkQueries = database.bookmarkQueries,
        feedSearchFTSQueries = database.feedSearchFTSQueries,
        feedGroupQueries = database.feedGroupQueries,
        feedGroupFeedQueries = database.feedGroupFeedQueries,
        blockedWordsQueries = database.blockedWordsQueries,
        appConfigQueries = database.appConfigQueries,
        sourceQueries = database.sourceQueries,
        readingHistoryQueries = database.readingHistoryQueries,
        readingTimeCalculator = ReadingTimeCalculator(testDispatchersProvider),
        dispatchersProvider = testDispatchersProvider,
      )

    userRepository = UserRepository(database.userQueries, testDispatchersProvider)
    settingsRepository = SettingsRepository(dataStore, AppInfo(1, "1.0.0", true, false) { "cache" })
    refreshPolicy = RefreshPolicy(dataStore)
  }

  @Test
  fun syncArticlesShouldUseKeysetPagination() =
    runTest(testDispatcher) {
      // given
      val user =
        User(
          id = "user-id",
          name = "User",
          email = "user@example.com",
          avatarUrl = null,
          token = "token",
          refreshToken = "refresh-token",
          serverUrl = "https://miniflux.example.com",
          lastSyncStatus = "IDLE",
          serviceType = ServiceType.MINIFLUX,
        )
      userRepository.saveUser(
        id = user.id,
        name = user.name,
        email = user.email,
        avatarUrl = user.avatarUrl,
        token = user.token,
        refreshToken = user.refreshToken,
        serverUrl = user.serverUrl,
        serviceType = user.serviceType!!,
      )

      val feedId = "feed-id"
      val feedRemoteId = "123"
      database.feedQueries.upsert(
        id = feedId,
        name = "Feed",
        icon = "icon",
        description = "description",
        homepageLink = "https://example.com",
        createdAt = Instant.fromEpochMilliseconds(0),
        link = "https://example.com/feed",
        alwaysFetchSourceArticle = false,
        showFeedFavIcon = false,
        lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      )
      database.feedQueries.updateFeedRemoteId(
        feedRemoteId,
        Instant.fromEpochMilliseconds(0),
        feedId,
      )

      // Initial post to have a starting point for keyset pagination
      database.postQueries.upsert(
        id = "post-0",
        sourceId = feedId,
        title = "Post 0",
        description = "Description 0",
        imageUrl = null,
        audioUrl = null,
        postDate = Instant.fromEpochMilliseconds(1000),
        createdAt = Instant.fromEpochMilliseconds(1000),
        updatedAt = Instant.fromEpochMilliseconds(1000),
        syncedAt = Instant.fromEpochMilliseconds(1000),
        link = "https://example.com/post-0",
        commentsLink = null,
        remoteId = "1000",
        isDateParsedCorrectly = 1L,
      )

      val mockEngine = MockEngine { request ->
        val url = request.url.toString()
        when {
          url.contains("/v1/feeds") -> {
            respond(
              content =
                """
              [
                {
                  "id": $feedRemoteId,
                  "user_id": 1,
                  "feed_url": "https://example.com/feed",
                  "site_url": "https://example.com",
                  "title": "Feed",
                  "checked_at": "2026-03-10T10:00:00Z",
                  "category": { "id": 1, "title": "All", "user_id": 1 },
                  "icon": { "feed_id": $feedRemoteId, "external_icon_id": "" }
                }
              ]
              """
                  .trimIndent(),
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
          }
          url.contains("/v1/categories") -> {
            respond(
              content = """[{"id": 1, "title": "All", "user_id": 1}]""",
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
          }
          url.contains("/v1/entries") && url.contains("after_entry_id=1000") -> {
            respond(
              content =
                """
              {
                "total": 1,
                "entries": [
                  {
                    "id": 1001,
                    "feed_id": $feedRemoteId,
                    "status": "unread",
                    "title": "Post 1001",
                    "url": "https://example.com/post-1001",
                    "author": "Author",
                    "content": "Content",
                    "published_at": "2026-03-10T10:00:00Z",
                    "starred": false,
                    "comments_url": null,
                    "enclosures": []
                  }
                ]
              }
              """
                  .trimIndent(),
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
          }
          url.contains("/v1/entries") -> {
            respond(
              content = """{"total": 0, "entries": []}""",
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
            )
          }
          else -> respond(content = "{}", status = HttpStatusCode.NotFound)
        }
      }

      val httpClient =
        HttpClient(mockEngine) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
          install(Resources)
        }

      minifluxSource = MinifluxSource(httpClient, { user }, testDispatchersProvider)
      syncCoordinator =
        MinifluxSyncCoordinator(
          minifluxSource = minifluxSource,
          rssRepository = rssRepository,
          dispatchersProvider = testDispatchersProvider,
          articleHtmlParser = ArticleHtmlParser(),
          refreshPolicy = refreshPolicy,
          settingsRepository = settingsRepository,
          userRepository = userRepository,
          fullArticleFetcher =
            FullArticleFetcher({ user }, httpClient, minifluxSource, testDispatchersProvider),
        )

      // when
      syncCoordinator.pull()

      // then
      val post = rssRepository.postByRemoteId("1001")
      assertEquals("Post 1001", post?.title)
      assertEquals("1001", rssRepository.latestPostRemoteId())
    }
}
