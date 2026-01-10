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

import co.touchlab.kermit.Logger
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Post
import dev.sasikanth.rss.reader.core.model.local.PostFlag
import dev.sasikanth.rss.reader.data.database.AppConfigQueries
import dev.sasikanth.rss.reader.data.repository.BlockedWordsRepository
import dev.sasikanth.rss.reader.data.repository.PostContentRepository
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.repository.UserRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class CloudSyncService(
  private val rssRepository: RssRepository,
  private val postContentRepository: PostContentRepository,
  private val blockedWordsRepository: BlockedWordsRepository,
  private val userRepository: UserRepository,
  private val settingsRepository: SettingsRepository,
  private val appConfigQueries: AppConfigQueries,
) {

  private val json: Json = Json { ignoreUnknownKeys = true }

  suspend fun sync(provider: CloudSyncProvider): Boolean {
    try {
      val config = appConfigQueries.getSyncConfig().executeAsOneOrNull()
      val currentSyncVersion = config?.syncFormatVersion?.toInt() ?: 1

      val metadataFileName = "/twine_sync_metadata.json"
      val remoteDataString =
        provider.download(metadataFileName) ?: provider.download("/twine_sync_data.json")

      if (remoteDataString != null) {
        var remoteData = json.decodeFromString<SyncData>(remoteDataString)

        if (remoteData.version < currentSyncVersion) {
          remoteData = migrate(remoteData, currentSyncVersion)
        }

        val remotePosts = remoteData.posts.toMutableList()
        remoteData.postChunks.forEach { chunkFileName ->
          val chunkDataString = provider.download(chunkFileName)
          if (chunkDataString != null) {
            val chunkPosts = json.decodeFromString<List<PostSyncEntity>>(chunkDataString)
            remotePosts.addAll(chunkPosts)
          }
        }

        merge(remoteData.copy(posts = remotePosts))
      }

      val feeds =
        rssRepository.allFeedsBlocking().map {
          FeedSyncEntity(
            id = it.id,
            name = it.name,
            icon = it.icon,
            description = it.description,
            link = it.link,
            homepageLink = it.homepageLink,
            pinnedAt = it.pinnedAt?.toEpochMilliseconds(),
            lastCleanUpAt = it.lastCleanUpAt?.toEpochMilliseconds(),
            alwaysFetchSourceArticle = it.alwaysFetchSourceArticle,
            lastUpdatedAt = it.lastUpdatedAt?.toEpochMilliseconds(),
            isDeleted = it.isDeleted
          )
        }

      val groups =
        rssRepository.allFeedGroupsBlocking().map {
          GroupSyncEntity(
            id = it.id,
            name = it.name,
            feedIds = it.feedIds,
            pinnedAt = it.pinnedAt?.toEpochMilliseconds(),
            updatedAt = it.updatedAt.toEpochMilliseconds(),
            isDeleted = it.isDeleted
          )
        }

      val bookmarks = rssRepository.allBookmarkIdsBlocking()

      val blockedWords =
        blockedWordsRepository.allBlockedWordsBlocking().map {
          BlockedWordSyncEntity(
            id = it.id.toString(),
            content = it.content,
            isDeleted = it.isDeleted,
            updatedAt = it.updatedAt.toEpochMilliseconds()
          )
        }

      val user = userRepository.userBlocking()
      val userSyncEntity =
        user?.let {
          UserSyncEntity(
            id = it.id,
            name = it.name,
            profileId = it.profileId,
            email = it.email,
            token = it.token,
            serverUrl = it.serverUrl
          )
        }

      val bookmarkedPosts =
        rssRepository.allBookmarkIdsBlocking().mapNotNull { rssRepository.postOrNull(it) }
      val postChunks = mutableListOf<String>()
      val chunkSize = 500

      bookmarkedPosts.chunked(chunkSize).forEachIndexed { index, postsChunk ->
        val chunkFileName = "/twine_posts_chunk_$index.json"
        val postsSyncEntities =
          postsChunk.map {
            val content = postContentRepository.postContent(it.id).firstOrNull()
            PostSyncEntity(
              id = it.id,
              sourceId = it.sourceId,
              title = it.title,
              description = it.description,
              imageUrl = it.imageUrl,
              postDate = it.postDate.toEpochMilliseconds(),
              createdAt = it.createdAt.toEpochMilliseconds(),
              updatedAt = it.updatedAt.toEpochMilliseconds(),
              syncedAt = it.syncedAt.toEpochMilliseconds(),
              link = it.link,
              commentsLink = it.commentsLink,
              flags = it.flags,
              rawContent = content?.postContent,
              htmlContent = content?.fullArticleHtml
            )
          }
        val serializedChunk = json.encodeToString(postsSyncEntities)
        if (provider.upload(chunkFileName, serializedChunk)) {
          postChunks.add(chunkFileName)
        }
      }

      val syncData =
        SyncData(
          version = currentSyncVersion,
          feeds = feeds,
          groups = groups,
          bookmarks = bookmarks,
          blockedWords = blockedWords,
          posts = emptyList(),
          postChunks = postChunks,
          user = userSyncEntity
        )

      val serializedData = json.encodeToString(syncData)
      val result = provider.upload(metadataFileName, serializedData)

      if (result) {
        appConfigQueries.updateLastSyncedFormatVersion(currentSyncVersion.toLong())
        appConfigQueries.updateLastSyncStatus("SUCCESS")
        settingsRepository.updateLastSyncedAt(Clock.System.now())

        val allFiles = provider.listFiles("/twine_")
        val activeFiles = postChunks + metadataFileName
        allFiles.forEach { file ->
          if (file !in activeFiles && file != "/twine_sync_data.json") {
            provider.deleteFile(file)
          }
        }
      }
      return result
    } catch (e: Exception) {
      Logger.e(e) { "Failed to sync with ${provider.name}" }
      appConfigQueries.updateLastSyncStatus("FAILURE")
      return false
    }
  }

  internal fun migrate(remoteData: SyncData, toVersion: Int): SyncData {
    return remoteData.copy(version = toVersion)
  }

  internal suspend fun merge(remoteData: SyncData) {
    val remoteFeeds =
      remoteData.feeds.map {
        Feed(
          id = it.id,
          name = it.name,
          icon = it.icon,
          description = it.description,
          link = it.link,
          homepageLink = it.homepageLink,
          createdAt = Clock.System.now(),
          pinnedAt = it.pinnedAt?.let(Instant::fromEpochMilliseconds),
          lastCleanUpAt = it.lastCleanUpAt?.let(Instant::fromEpochMilliseconds),
          alwaysFetchSourceArticle = it.alwaysFetchSourceArticle,
          pinnedPosition = 0.0,
          showFeedFavIcon = true,
          lastUpdatedAt = it.lastUpdatedAt?.let(Instant::fromEpochMilliseconds),
          refreshInterval = 1.hours,
          isDeleted = false
        )
      }
    rssRepository.upsertFeeds(remoteFeeds)

    remoteData.feeds.forEach { remoteFeed ->
      val localFeed = rssRepository.feed(remoteFeed.id)
      if (localFeed != null && remoteFeed.lastCleanUpAt != null) {
        val remoteCleanUpAt = Instant.fromEpochMilliseconds(remoteFeed.lastCleanUpAt)
        val localCleanUpAt = localFeed.lastCleanUpAt ?: Instant.DISTANT_PAST
        if (remoteCleanUpAt > localCleanUpAt) {
          rssRepository.deleteReadPostsForFeedOlderThan(remoteFeed.id, remoteCleanUpAt)
          rssRepository.updateFeedsLastCleanUpAt(listOf(remoteFeed.id), remoteCleanUpAt)
        }
      }
    }

    remoteData.groups.forEach { remoteGroup ->
      val remoteUpdatedAt =
        remoteGroup.updatedAt?.let(Instant::fromEpochMilliseconds) ?: Clock.System.now()
      val localGroup = rssRepository.feedGroupBlocking(remoteGroup.id)
      val localUpdatedAt = localGroup?.updatedAt ?: Instant.DISTANT_PAST

      if (remoteUpdatedAt > localUpdatedAt) {
        rssRepository.upsertGroup(
          id = remoteGroup.id,
          name = remoteGroup.name,
          pinnedAt = remoteGroup.pinnedAt?.let(Instant::fromEpochMilliseconds),
          updatedAt = remoteUpdatedAt,
          isDeleted = remoteGroup.isDeleted
        )
        rssRepository.replaceFeedsInGroup(groupId = remoteGroup.id, feedIds = remoteGroup.feedIds)
      }
    }

    blockedWordsRepository.upsertBlockedWords(remoteData.blockedWords)

    val allFeeds = rssRepository.allFeedsBlocking()
    val cleanUpAtByFeed = allFeeds.associate { it.id to (it.lastCleanUpAt ?: Instant.DISTANT_PAST) }

    val filteredRemotePosts = filterPosts(remoteData.posts, cleanUpAtByFeed)
    val remotePosts =
      filteredRemotePosts.map {
        Post(
          id = it.id,
          sourceId = it.sourceId,
          title = it.title,
          description = it.description,
          imageUrl = it.imageUrl,
          postDate = Instant.fromEpochMilliseconds(it.postDate),
          createdAt = Instant.fromEpochMilliseconds(it.createdAt),
          updatedAt = Instant.fromEpochMilliseconds(it.updatedAt),
          syncedAt = Instant.fromEpochMilliseconds(it.syncedAt),
          link = it.link,
          commentsLink = it.commentsLink,
          flags = it.flags
        )
      }
    rssRepository.upsertPosts(remotePosts)

    filteredRemotePosts.forEach { remotePost ->
      if (remotePost.rawContent != null || remotePost.htmlContent != null) {
        postContentRepository.upsert(
          postId = remotePost.id,
          rawContent = remotePost.rawContent,
          htmlContent = remotePost.htmlContent,
          createdAt = Instant.fromEpochMilliseconds(remotePost.createdAt)
        )
      }
    }

    val remoteUser = remoteData.user
    if (remoteUser != null && userRepository.userBlocking() == null) {
      userRepository.createUser(
        id = remoteUser.id,
        name = remoteUser.name,
        profileId = remoteUser.profileId,
        email = remoteUser.email,
        token = remoteUser.token,
        serverUrl = remoteUser.serverUrl
      )
    }
  }

  companion object {
    internal fun filterPosts(
      remotePosts: List<PostSyncEntity>,
      cleanUpAtByFeed: Map<String, Instant>
    ): List<PostSyncEntity> {
      return remotePosts.filter { remotePost ->
        val cleanUpAt = cleanUpAtByFeed[remotePost.sourceId] ?: Instant.DISTANT_PAST
        val postDate = Instant.fromEpochMilliseconds(remotePost.postDate)
        val isRead = remotePost.flags.contains(PostFlag.Read)
        val isBookmarked = remotePost.flags.contains(PostFlag.Bookmarked)

        if (isBookmarked) return@filter true

        !(isRead && postDate < cleanUpAt)
      }
    }
  }
}
