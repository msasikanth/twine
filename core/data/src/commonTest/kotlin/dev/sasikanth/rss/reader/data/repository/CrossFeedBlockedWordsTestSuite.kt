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
import app.cash.sqldelight.db.SqlDriver
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.Post as LocalPost
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.core.model.remote.FeedPayload
import dev.sasikanth.rss.reader.core.model.remote.PostPayload
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
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
class CrossFeedBlockedWordsTestSuite(driver: SqlDriver) {

  private val dispatchersProvider =
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

  private val noopWidgetUpdater =
    object : WidgetUpdater {
      override fun updateUnreadWidget() = Unit
    }

  val database: ReaderDatabase =
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

  val rssRepository: RssRepository =
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
      readingTimeCalculator = ReadingTimeCalculator(dispatchersProvider),
      widgetUpdater = noopWidgetUpdater,
      dispatchersProvider = dispatchersProvider,
      bookmarkRepository =
        BookmarkRepository(
          database.bookmarkQueries,
          database.postQueries,
          transactionRunner = TransactionRunner(database),
          widgetUpdater = noopWidgetUpdater,
          dispatchersProvider = dispatchersProvider,
        ),
      readingHistoryRepository =
        ReadingHistoryRepository(database.readingHistoryQueries, dispatchersProvider),
      feedGroupRepository =
        FeedGroupRepository(
          database.feedGroupQueries,
          database.feedGroupFeedQueries,
          TransactionRunner(database),
          noopWidgetUpdater,
          dispatchersProvider,
        ),
      sourceRepository = SourceRepository(database.sourceQueries, dispatchersProvider),
      feedRepository =
        FeedRepository(
          database.feedQueries,
          database.feedSearchFTSQueries,
          TransactionRunner(database),
          dispatchersProvider,
        ),
      postRepository = PostRepository(database.postQueries, dispatchersProvider),
      syncRepository =
        SyncRepository(
          feedQueries = database.feedQueries,
          postQueries = database.postQueries,
          blockedWordsQueries = database.blockedWordsQueries,
          transactionRunner = TransactionRunner(database),
          widgetUpdater = noopWidgetUpdater,
          dispatchersProvider = dispatchersProvider,
        ),
    )

  val blockedWordsRepository: BlockedWordsRepository =
    BlockedWordsRepository(
      transactionRunner = TransactionRunner(database),
      blockedWordsQueries = database.blockedWordsQueries,
      postQueries = database.postQueries,
      dispatchersProvider = dispatchersProvider,
    )

  suspend fun blockedFeedFollowedByUnblockedDuplicateAndRefreshKeepsBothBlocked() {
    val sharedLink = "https://example.com/shared-article"
    val postId = nameBasedUuidOf(sharedLink).toString()
    blockedWordsRepository.addWord("spoiler")

    val feedAId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-a.example.com/rss",
            postLink = sharedLink,
            title = "Major Movie Review",
            description = "Full article containing a major spoiler inside",
          ),
        feedId = "feed-a",
      )

    val feedBId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-b.example.com/rss",
            postLink = sharedLink,
            title = "Major Movie Review",
            description = "Short aggregator summary",
          ),
        feedId = "feed-b",
      )

    assertEquals(
      mapOf(feedAId to setOf(PostFlag.Hidden), feedBId to setOf(PostFlag.Hidden)),
      flagsBySource(postId),
    )

    rssRepository.upsertFeedWithPosts(
      feedPayload =
        feedPayload(
          feedLink = "https://feed-b.example.com/rss",
          postLink = sharedLink,
          title = "Major Movie Review",
          description = "Updated aggregator summary without keyword",
        ),
      feedId = feedBId,
    )

    assertEquals(
      mapOf(feedAId to setOf(PostFlag.Hidden), feedBId to setOf(PostFlag.Hidden)),
      flagsBySource(postId),
    )

    database.postQueries.upsertFeedPost(
      id = postId,
      sourceId = feedBId,
      title = "Major Movie Review",
      description = "Direct SQL refresh with empty flags",
      imageUrl = null,
      audioUrl = null,
      postDate = Instant.fromEpochMilliseconds(1_000L),
      createdAt = Instant.fromEpochMilliseconds(2_000L),
      updatedAt = Instant.fromEpochMilliseconds(2_000L),
      syncedAt = Instant.fromEpochMilliseconds(2_000L),
      link = sharedLink,
      commentsLink = null,
      flags = emptySet(),
      isDateParsedCorrectly = 1L,
      remoteId = null,
    )

    assertEquals(
      mapOf(feedAId to setOf(PostFlag.Hidden), feedBId to setOf(PostFlag.Hidden)),
      flagsBySource(postId),
    )
  }

  suspend fun unblockedFeedFollowedByBlockedDuplicateBlocksBothAndRemovingWordUnblocksBoth() {
    val sharedLink = "https://example.com/shared-article-2"
    val postId = nameBasedUuidOf(sharedLink).toString()
    blockedWordsRepository.addWord("crypto")
    blockedWordsRepository.addWord("nft")

    val feedAId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-a.example.com/rss",
            postLink = sharedLink,
            title = "Weekly Tech Digest",
            description = "Truncated summary",
          ),
        feedId = "feed-a",
      )
    rssRepository.updatePostReadStatus(read = true, id = postId)

    assertEquals(mapOf(feedAId to setOf(PostFlag.Read)), flagsBySource(postId))

    val feedBId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-b.example.com/rss",
            postLink = sharedLink,
            title = "Weekly Tech Digest",
            description = "Full text discussing crypto markets",
          ),
        feedId = "feed-b",
      )

    assertEquals(
      mapOf(
        feedAId to setOf(PostFlag.Read, PostFlag.Hidden),
        feedBId to setOf(PostFlag.Read, PostFlag.Hidden),
      ),
      flagsBySource(postId),
    )

    val nftWordId = blockedWordsRepository.words().first().first { it.content == "nft" }.id
    blockedWordsRepository.removeWord(nftWordId)

    assertEquals(
      mapOf(
        feedAId to setOf(PostFlag.Read, PostFlag.Hidden),
        feedBId to setOf(PostFlag.Read, PostFlag.Hidden),
      ),
      flagsBySource(postId),
    )

    val cryptoWordId = blockedWordsRepository.words().first().first { it.content == "crypto" }.id
    blockedWordsRepository.removeWord(cryptoWordId)

    assertEquals(
      mapOf(feedAId to setOf(PostFlag.Read), feedBId to setOf(PostFlag.Read)),
      flagsBySource(postId),
    )
  }

  suspend fun singleFeedAndCrossFeedEditedPostsUnblockWhenNoCopyMatchesBlockedWord() {
    val singleLink = "https://example.com/single-post"
    val singlePostId = nameBasedUuidOf(singleLink).toString()
    blockedWordsRepository.addWord("draft-only")

    val feedAId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-a.example.com/rss",
            postLink = singleLink,
            title = "Draft-only headline",
            description = "Initial body",
          ),
        feedId = "feed-a",
      )

    assertEquals(mapOf(feedAId to setOf(PostFlag.Hidden)), flagsBySource(singlePostId))

    rssRepository.upsertFeedWithPosts(
      feedPayload =
        feedPayload(
          feedLink = "https://feed-a.example.com/rss",
          postLink = singleLink,
          title = "Published headline",
          description = "Initial body",
        ),
      feedId = feedAId,
    )

    assertEquals(mapOf(feedAId to emptySet()), flagsBySource(singlePostId))

    val sharedLink = "https://example.com/cross-feed-edited"
    val sharedPostId = nameBasedUuidOf(sharedLink).toString()

    rssRepository.upsertFeedWithPosts(
      feedPayload =
        feedPayload(
          feedLink = "https://feed-a.example.com/rss",
          postLink = sharedLink,
          title = "Draft-only cross-feed title",
          description = "Body",
        ),
      feedId = feedAId,
    )
    val feedBId =
      rssRepository.upsertFeedWithPosts(
        feedPayload =
          feedPayload(
            feedLink = "https://feed-b.example.com/rss",
            postLink = sharedLink,
            title = "Clean aggregator title",
            description = "Short summary",
          ),
        feedId = "feed-b",
      )

    assertEquals(
      mapOf(feedAId to setOf(PostFlag.Hidden), feedBId to setOf(PostFlag.Hidden)),
      flagsBySource(sharedPostId),
    )

    rssRepository.upsertFeedWithPosts(
      feedPayload =
        feedPayload(
          feedLink = "https://feed-a.example.com/rss",
          postLink = sharedLink,
          title = "Final clean cross-feed title",
          description = "Body",
        ),
      feedId = feedAId,
    )

    assertEquals(mapOf(feedAId to emptySet(), feedBId to emptySet()), flagsBySource(sharedPostId))
  }

  suspend fun directUpsertAndSyncRepositoryUpsertPreserveCrossFeedHiddenFlag() {
    val sharedLink = "https://example.com/sync-and-direct-upsert"
    val postId = nameBasedUuidOf(sharedLink).toString()
    insertFeed("feed-a")
    insertFeed("feed-b")
    insertFeed("feed-c")

    blockedWordsRepository.addWord("giveaway")

    rssRepository.upsertPosts(
      listOf(
        localPost(
          id = postId,
          sourceId = "feed-a",
          title = "Mega giveaway announcement",
          description = "Full text",
          link = sharedLink,
          updatedAtEpochMillis = 1_000L,
        )
      )
    )

    assertEquals(mapOf("feed-a" to setOf(PostFlag.Hidden)), flagsBySource(postId))

    database.postQueries.upsert(
      id = postId,
      sourceId = "feed-b",
      title = "Announcement",
      description = "Short summary",
      imageUrl = null,
      audioUrl = null,
      postDate = Instant.fromEpochMilliseconds(1_000L),
      createdAt = Instant.fromEpochMilliseconds(1_000L),
      updatedAt = Instant.fromEpochMilliseconds(1_000L),
      syncedAt = Instant.fromEpochMilliseconds(1_000L),
      link = sharedLink,
      commentsLink = null,
      remoteId = null,
      isDateParsedCorrectly = 1L,
    )

    assertEquals(
      mapOf("feed-a" to setOf(PostFlag.Hidden), "feed-b" to setOf(PostFlag.Hidden)),
      flagsBySource(postId),
    )

    rssRepository.upsertPosts(
      listOf(
        localPost(
          id = postId,
          sourceId = "feed-c",
          title = "Announcement",
          description = "Another summary",
          link = sharedLink,
          updatedAtEpochMillis = 2_000L,
        ),
        localPost(
          id = postId,
          sourceId = "feed-b",
          title = "Updated announcement",
          description = "Updated summary",
          link = sharedLink,
          updatedAtEpochMillis = 3_000L,
        ),
      )
    )

    assertEquals(
      mapOf(
        "feed-a" to setOf(PostFlag.Hidden),
        "feed-b" to setOf(PostFlag.Hidden),
        "feed-c" to setOf(PostFlag.Hidden),
      ),
      flagsBySource(postId),
    )

    rssRepository.upsertPosts(
      listOf(
        localPost(
          id = postId,
          sourceId = "feed-a",
          title = "Cleaned announcement",
          description = "Full text",
          link = sharedLink,
          updatedAtEpochMillis = 4_000L,
        )
      )
    )

    assertEquals(
      mapOf("feed-a" to emptySet(), "feed-b" to emptySet(), "feed-c" to emptySet()),
      flagsBySource(postId),
    )
  }

  private suspend fun flagsBySource(postId: String): Map<String, Set<PostFlag>> {
    return rssRepository.postsByIds(setOf(postId)).associate { it.sourceId to it.flags }
  }

  private fun insertFeed(id: String) {
    database.feedQueries.upsert(
      id = id,
      name = "Feed $id",
      icon = "icon",
      description = "description",
      homepageLink = "https://$id.example.com",
      createdAt = Instant.fromEpochMilliseconds(0L),
      link = "https://$id.example.com/rss",
      alwaysFetchSourceArticle = false,
      showFeedFavIcon = false,
      lastUpdatedAt = Instant.fromEpochMilliseconds(0L),
      enableNotifications = true,
      coverArtUrl = null,
    )
  }

  private fun feedPayload(
    feedLink: String,
    postLink: String,
    title: String,
    description: String,
  ): FeedPayload {
    return FeedPayload(
      name = "Feed",
      icon = "https://example.com/icon.png",
      description = "Feed description",
      homepageLink = "https://example.com",
      link = feedLink,
      posts =
        listOf(
            PostPayload(
              title = title,
              link = postLink,
              description = description,
              rawContent = description,
              fullContent = null,
              imageUrl = null,
              audioUrl = null,
              date = 1_000L,
              commentsLink = null,
              isDateParsedCorrectly = true,
            )
          )
          .asFlow(),
    )
  }

  private fun localPost(
    id: String,
    sourceId: String,
    title: String,
    description: String,
    link: String,
    updatedAtEpochMillis: Long,
  ): LocalPost {
    val updatedAt = Instant.fromEpochMilliseconds(updatedAtEpochMillis)
    return LocalPost(
      id = id,
      sourceId = sourceId,
      title = title,
      description = description,
      imageUrl = null,
      audioUrl = null,
      postDate = Instant.fromEpochMilliseconds(1_000L),
      createdAt = Instant.fromEpochMilliseconds(1_000L),
      updatedAt = updatedAt,
      syncedAt = updatedAt,
      link = link,
      commentsLink = null,
      flags = emptySet(),
      remoteId = null,
      seedColor = null,
      audioProgress = 0,
      audioDuration = 0,
    )
  }
}
