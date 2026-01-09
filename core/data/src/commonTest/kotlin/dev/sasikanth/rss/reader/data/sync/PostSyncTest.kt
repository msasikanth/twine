/*
 * Copyright 2026 Sasikanth Miriyampalli
 *
 * Licensed under the GPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package dev.sasikanth.rss.reader.data.sync

import dev.sasikanth.rss.reader.core.model.local.PostFlag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PostSyncTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun filterPostsShouldExcludeReadPostsOlderThanCleanUpAt() {
    val feedId = "feed-1"
    val cleanUpAt = Instant.fromEpochMilliseconds(1000)
    val cleanUpAtByFeed = mapOf(feedId to cleanUpAt)

    val posts =
      listOf(
        // Read post older than clean-up -> should be filtered
        PostSyncEntity(
          id = "post-1",
          sourceId = feedId,
          title = "Post 1",
          description = "",
          imageUrl = null,
          postDate = 500L,
          createdAt = 0L,
          updatedAt = 0L,
          syncedAt = 0L,
          link = "",
          commentsLink = null,
          flags = setOf(PostFlag.Read)
        ),
        // Read post newer than clean-up -> should be kept
        PostSyncEntity(
          id = "post-2",
          sourceId = feedId,
          title = "Post 2",
          description = "",
          imageUrl = null,
          postDate = 1500L,
          createdAt = 0L,
          updatedAt = 0L,
          syncedAt = 0L,
          link = "",
          commentsLink = null,
          flags = setOf(PostFlag.Read)
        ),
        // Unread post older than clean-up -> should be kept
        PostSyncEntity(
          id = "post-3",
          sourceId = feedId,
          title = "Post 3",
          description = "",
          imageUrl = null,
          postDate = 500L,
          createdAt = 0L,
          updatedAt = 0L,
          syncedAt = 0L,
          link = "",
          commentsLink = null,
          flags = emptySet()
        ),
        // Bookmarked and read post older than clean-up -> should be kept
        PostSyncEntity(
          id = "post-4",
          sourceId = feedId,
          title = "Post 4",
          description = "",
          imageUrl = null,
          postDate = 500L,
          createdAt = 0L,
          updatedAt = 0L,
          syncedAt = 0L,
          link = "",
          commentsLink = null,
          flags = setOf(PostFlag.Read, PostFlag.Bookmarked)
        )
      )

    val filtered = CloudSyncService.filterPosts(posts, cleanUpAtByFeed)

    assertEquals(3, filtered.size)
    assertEquals(setOf("post-2", "post-3", "post-4"), filtered.map { it.id }.toSet())
  }

  @Test
  fun syncDataSerializationWithPosts() {
    val syncData =
      SyncData(
        version = 2,
        feeds =
          listOf(
            FeedSyncEntity(
              id = "feed-id",
              name = "Feed",
              icon = "https://example.com/icon.png",
              description = "Description",
              link = "https://example.com/feed",
              homepageLink = "https://example.com",
              pinnedAt = 123456789L,
              lastCleanUpAt = 123456789L,
              alwaysFetchSourceArticle = true,
              lastUpdatedAt = 123456789L
            )
          ),
        groups =
          listOf(
            GroupSyncEntity(
              id = "group-id",
              name = "Group",
              feedIds = listOf("feed-id"),
              pinnedAt = null,
              updatedAt = 123456789L
            )
          ),
        bookmarks = listOf("post-1"),
        blockedWords =
          listOf(
            BlockedWordSyncEntity(
              id = "word-id",
              content = "word",
              isDeleted = false,
              updatedAt = 123456789L
            )
          ),
        posts =
          listOf(
            PostSyncEntity(
              id = "post-1",
              sourceId = "feed-id",
              title = "Post 1",
              description = "Description 1",
              imageUrl = "https://example.com/image.png",
              postDate = 123456789L,
              createdAt = 123456789L,
              updatedAt = 123456789L,
              syncedAt = 123456789L,
              link = "https://example.com/post-1",
              commentsLink = null,
              flags = setOf(PostFlag.Bookmarked, PostFlag.Read)
            )
          )
      )

    val serialized = json.encodeToString(syncData)
    val deserialized = json.decodeFromString<SyncData>(serialized)

    assertEquals(syncData, deserialized)
    assertEquals(1, deserialized.posts.size)
    assertEquals(setOf(PostFlag.Bookmarked, PostFlag.Read), deserialized.posts[0].flags)
  }

  @Test
  fun syncDataDeserializationWithMissingFields() {
    val legacyJson =
      """
      {
        "version": 1,
        "feeds": [
          {
            "id": "feed-id",
            "name": "Feed",
            "link": "https://example.com/feed",
            "homepageLink": "https://example.com"
          }
        ],
        "groups": [
          {
            "id": "group-id",
            "name": "Group"
          }
        ],
        "bookmarks": [],
        "blockedWords": [],
        "posts": []
      }
    """
        .trimIndent()

    val deserialized = json.decodeFromString<SyncData>(legacyJson)

    assertEquals(1, deserialized.feeds.size)
    assertEquals("", deserialized.feeds[0].icon)
    assertEquals("", deserialized.feeds[0].description)
    assertEquals(false, deserialized.feeds[0].alwaysFetchSourceArticle)
    assertEquals(null, deserialized.feeds[0].lastCleanUpAt)

    assertEquals(1, deserialized.groups.size)
    assertEquals(emptyList<String>(), deserialized.groups[0].feedIds)
  }

  @Test
  fun syncDataDeserializationWithLegacyBlockedWords() {
    val legacyJson =
      """
      {
        "version": 1,
        "feeds": [],
        "groups": [],
        "bookmarks": [],
        "blockedWords": ["elon musk", "musk", "trump"],
        "posts": []
      }
    """
        .trimIndent()

    val deserialized = json.decodeFromString<SyncData>(legacyJson)

    assertEquals(3, deserialized.blockedWords.size)
    assertEquals("elon musk", deserialized.blockedWords[0].content)
    assertEquals("musk", deserialized.blockedWords[1].content)
    assertEquals("trump", deserialized.blockedWords[2].content)
    // Verify ID generation
    assertEquals(
      dev.sasikanth.rss.reader.util.nameBasedUuidOf("elon musk").toString(),
      deserialized.blockedWords[0].id
    )
  }

  @Test
  fun syncDataSerializationWithPostContentAndUser() {
    val syncData =
      SyncData(
        version = 2,
        feeds = emptyList(),
        groups = emptyList(),
        bookmarks = emptyList(),
        blockedWords = emptyList(),
        posts =
          listOf(
            PostSyncEntity(
              id = "post-1",
              sourceId = "feed-id",
              title = "Post 1",
              description = "Description 1",
              imageUrl = null,
              postDate = 0L,
              createdAt = 0L,
              updatedAt = 0L,
              syncedAt = 0L,
              link = "",
              commentsLink = null,
              flags = emptySet(),
              rawContent = "Raw content",
              htmlContent = "HTML content"
            )
          ),
        user =
          UserSyncEntity(
            id = "user-id",
            name = "User",
            profileId = "profile-id",
            email = "user@example.com",
            token = "token",
            serverUrl = "https://example.com"
          )
      )

    val serialized = json.encodeToString(syncData)
    val deserialized = json.decodeFromString<SyncData>(serialized)

    assertEquals(syncData, deserialized)
    assertEquals("Raw content", deserialized.posts[0].rawContent)
    assertEquals("HTML content", deserialized.posts[0].htmlContent)
    assertEquals("user-id", deserialized.user?.id)
  }
}
