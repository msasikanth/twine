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

package dev.sasikanth.rss.reader.data.repository

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
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
import dev.sasikanth.rss.reader.data.utils.ReadingTimeCalculator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class RssRepositoryTest {

  private lateinit var database: ReaderDatabase
  private lateinit var repository: RssRepository

  @OptIn(ExperimentalCoroutinesApi::class)
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

  @BeforeTest
  fun setup() {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
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
    repository =
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
            transactionRunner = TransactionRunner(database),
            widgetUpdater = NoopWidgetUpdater,
            dispatchersProvider = testDispatchersProvider,
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
            testDispatchersProvider,
          ),
        postRepository = PostRepository(database.postQueries, testDispatchersProvider),
        syncRepository =
          SyncRepository(
            database.feedQueries,
            database.postQueries,
            transactionRunner = TransactionRunner(database),
            widgetUpdater = NoopWidgetUpdater,
            dispatchersProvider = testDispatchersProvider,
          ),
      )
  }

  @Test
  fun featuredPostsShouldRespectSortOrder() = runTest {
    // given
    val feedId = "feed-1"
    database.feedQueries.upsert(
      id = feedId,
      name = "Feed 1",
      icon = "icon",
      description = "description",
      homepageLink = "homepage",
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "link",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      enableNotifications = true,
    )

    val now = Instant.fromEpochMilliseconds(1000000000)
    val post1 = now.minus(1.hours)
    val post2 = now.minus(2.hours)

    insertPost(id = "post-1", sourceId = feedId, postDate = post1, imageUrl = "image-1")
    insertPost(id = "post-2", sourceId = feedId, postDate = post2, imageUrl = "image-2")

    // when (Latest)
    val featuredLatest =
      repository
        .featuredPosts(activeSourceIds = emptyList(), postsSortOrder = PostsSortOrder.Latest)
        .first()

    // then
    assertEquals(2, featuredLatest.size)
    assertEquals("post-1", featuredLatest[0].id)
    assertEquals("post-2", featuredLatest[1].id)

    // when (Oldest)
    val featuredOldest =
      repository
        .featuredPosts(activeSourceIds = emptyList(), postsSortOrder = PostsSortOrder.Oldest)
        .first()

    // then
    assertEquals(2, featuredOldest.size)
    assertEquals("post-2", featuredOldest[0].id)
    assertEquals("post-1", featuredOldest[1].id)
  }

  @Test
  fun postPositionShouldRespectSortOrder() = runTest {
    // given
    val feedId = "feed-1"
    database.feedQueries.upsert(
      id = feedId,
      name = "Feed 1",
      icon = "icon",
      description = "description",
      homepageLink = "homepage",
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "link",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      enableNotifications = true,
    )

    val now = Instant.fromEpochMilliseconds(1000000000)
    val post1 = now.minus(1.hours)
    val post2 = now.minus(2.hours)
    val post3 = now.minus(3.hours)

    insertPost(id = "post-1", sourceId = feedId, postDate = post1)
    insertPost(id = "post-2", sourceId = feedId, postDate = post2)
    insertPost(id = "post-3", sourceId = feedId, postDate = post3)

    // when (Latest)
    val positionLatestPost1 =
      repository.postPosition(
        postId = "post-1",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Latest,
      )
    val positionLatestPost2 =
      repository.postPosition(
        postId = "post-2",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Latest,
      )
    val positionLatestPost3 =
      repository.postPosition(
        postId = "post-3",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Latest,
      )

    // then
    assertEquals(0, positionLatestPost1)
    assertEquals(1, positionLatestPost2)
    assertEquals(2, positionLatestPost3)

    // when (Oldest)
    val positionOldestPost1 =
      repository.postPosition(
        postId = "post-1",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Oldest,
      )
    val positionOldestPost2 =
      repository.postPosition(
        postId = "post-2",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Oldest,
      )
    val positionOldestPost3 =
      repository.postPosition(
        postId = "post-3",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Oldest,
      )

    // then
    assertEquals(2, positionOldestPost1)
    assertEquals(1, positionOldestPost2)
    assertEquals(0, positionOldestPost3)
  }

  @Test
  fun nonFeaturedPostPositionShouldRespectSortOrderAndFeaturedPosts() = runTest {
    // given
    val feedId = "feed-1"
    database.feedQueries.upsert(
      id = feedId,
      name = "Feed 1",
      icon = "icon",
      description = "description",
      homepageLink = "homepage",
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "link",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      enableNotifications = true,
    )

    val now = Instant.fromEpochMilliseconds(1000000000)
    // Featured posts (have images)
    val post1 = now.minus(1.hours)
    val post2 = now.minus(2.hours)
    // Non-featured posts (no images)
    val post3 = now.minus(3.hours)
    val post4 = now.minus(4.hours)

    insertPost(id = "post-1", sourceId = feedId, postDate = post1, imageUrl = "image-1")
    insertPost(id = "post-2", sourceId = feedId, postDate = post2, imageUrl = "image-2")
    insertPost(id = "post-3", sourceId = feedId, postDate = post3)
    insertPost(id = "post-4", sourceId = feedId, postDate = post4)

    // when (Latest)
    val featuredPostsAfter = now.minus(24.hours)
    val positionLatestPost3 =
      repository.nonFeaturedPostPosition(
        postId = "post-3",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Latest,
        featuredPostsAfter = featuredPostsAfter,
        numberOfFeaturedPosts = 2,
      )
    val positionLatestPost4 =
      repository.nonFeaturedPostPosition(
        postId = "post-4",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Latest,
        featuredPostsAfter = featuredPostsAfter,
        numberOfFeaturedPosts = 2,
      )

    // then
    assertEquals(0, positionLatestPost3)
    assertEquals(1, positionLatestPost4)

    // when (Oldest)
    val positionOldestPost3 =
      repository.nonFeaturedPostPosition(
        postId = "post-3",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Oldest,
        featuredPostsAfter = featuredPostsAfter,
        numberOfFeaturedPosts = 2,
      )
    val positionOldestPost4 =
      repository.nonFeaturedPostPosition(
        postId = "post-4",
        activeSourceIds = emptyList(),
        postsSortOrder = PostsSortOrder.Oldest,
        featuredPostsAfter = featuredPostsAfter,
        numberOfFeaturedPosts = 2,
      )

    // then
    assertEquals(1, positionOldestPost3)
    assertEquals(0, positionOldestPost4)
  }

  @Test
  fun updateSeedColorsShouldUpdateMultiplePosts() = runTest {
    // given
    val feedId = "feed-1"
    database.feedQueries.upsert(
      id = feedId,
      name = "Feed 1",
      icon = "icon",
      description = "description",
      homepageLink = "homepage",
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "link",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      enableNotifications = true,
    )

    insertPost(id = "post-1", sourceId = feedId, postDate = Instant.fromEpochMilliseconds(0))
    insertPost(id = "post-2", sourceId = feedId, postDate = Instant.fromEpochMilliseconds(0))

    val updates = mapOf("post-1" to 123, "post-2" to 456)

    // when
    repository.updateSeedColors(updates)

    // then
    val post1 = repository.post("post-1")
    val post2 = repository.post("post-2")

    assertEquals(123, post1.seedColor)
    assertEquals(456, post2.seedColor)
  }

  @Test
  fun postPositionShouldMatchIndexInAllPostsForAllSortOrders() = runTest {
    // given
    val feedId = "feed-1"
    database.feedQueries.upsert(
      id = feedId,
      name = "Feed 1",
      icon = "icon",
      description = "description",
      homepageLink = "homepage",
      createdAt = Instant.fromEpochMilliseconds(0),
      link = "link",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0),
      enableNotifications = true,
    )

    val now = Instant.fromEpochMilliseconds(1000000000)
    (1..10).forEach { i ->
      val postDate = now.minus(i.hours)
      val id = "post-$i"
      insertPost(id = id, sourceId = feedId, postDate = postDate)
      id to postDate
    }

    val sortOrders = PostsSortOrder.entries

    sortOrders.forEach { sortOrder ->
      // when
      val position0 =
        repository.postPosition(
          postId = "post-1",
          activeSourceIds = emptyList(),
          postsSortOrder = sortOrder,
        )
      val position5 =
        repository.postPosition(
          postId = "post-6",
          activeSourceIds = emptyList(),
          postsSortOrder = sortOrder,
        )
      val position9 =
        repository.postPosition(
          postId = "post-10",
          activeSourceIds = emptyList(),
          postsSortOrder = sortOrder,
        )

      // then
      when (sortOrder) {
        PostsSortOrder.Latest,
        PostsSortOrder.AddedLatest -> {
          assertEquals(0, position0, "Failed for $sortOrder")
          assertEquals(5, position5, "Failed for $sortOrder")
          assertEquals(9, position9, "Failed for $sortOrder")
        }
        PostsSortOrder.Oldest,
        PostsSortOrder.AddedOldest -> {
          assertEquals(9, position0, "Failed for $sortOrder")
          assertEquals(4, position5, "Failed for $sortOrder")
          assertEquals(0, position9, "Failed for $sortOrder")
        }
      }
    }
  }

  private fun insertPost(
    id: String,
    sourceId: String,
    postDate: Instant,
    imageUrl: String? = null,
  ) {
    database.postQueries.upsert(
      id = id,
      sourceId = sourceId,
      title = "Title $id",
      description = "Description $id",
      imageUrl = imageUrl,
      postDate = postDate,
      createdAt = Instant.fromEpochMilliseconds(0),
      updatedAt = Instant.fromEpochMilliseconds(0),
      syncedAt = Instant.fromEpochMilliseconds(0),
      link = "link-$id",
      commentsLink = null,
      audioUrl = null,
      remoteId = null,
      isDateParsedCorrectly = 1L,
    )
  }
}

object NoopWidgetUpdater : WidgetUpdater {

  override fun updateUnreadWidget() {
    // no-op
  }
}
