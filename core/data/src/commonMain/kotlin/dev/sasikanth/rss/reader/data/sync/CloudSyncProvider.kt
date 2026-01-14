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
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray

interface CloudSyncProvider {
  val id: String
  val name: String
  val isSupported: Boolean
    get() = true

  fun isSignedIn(): Flow<Boolean>

  suspend fun isSignedInImmediate(): Boolean

  suspend fun signOut()

  suspend fun upload(fileName: String, data: String): Boolean

  suspend fun download(fileName: String): String?

  suspend fun listFiles(prefix: String): List<String>

  suspend fun deleteFile(fileName: String): Boolean
}

@Serializable
data class SyncData(
  val version: Int = 2,
  val feeds: List<FeedSyncEntity>,
  val groups: List<GroupSyncEntity>,
  val bookmarks: List<String>,
  @Serializable(with = BlockedWordSyncEntityListSerializer::class)
  val blockedWords: List<BlockedWordSyncEntity> = emptyList(),
  val posts: List<PostSyncEntity> = emptyList(),
  val postChunks: List<String> = emptyList(),
  val readPosts: List<ReadPostSyncEntity> = emptyList()
)

@Serializable data class ReadPostSyncEntity(val id: String, val updatedAt: Long)

@Serializable
data class PostSyncEntity(
  val id: String,
  val sourceId: String,
  val title: String,
  val description: String,
  val imageUrl: String?,
  val postDate: Long,
  val createdAt: Long,
  val updatedAt: Long,
  val syncedAt: Long,
  val link: String,
  val commentsLink: String?,
  val flags: Set<PostFlag>,
  val isDeleted: Boolean = false,
  val rawContent: String? = null,
  val htmlContent: String? = null
)

@Serializable
data class FeedSyncEntity(
  val id: String,
  val name: String,
  val icon: String = "",
  val description: String = "",
  val link: String,
  val homepageLink: String,
  val pinnedPosition: Double = 0.0,
  val pinnedAt: Long? = null,
  val lastCleanUpAt: Long? = null,
  val alwaysFetchSourceArticle: Boolean = false,
  val lastUpdatedAt: Long? = null,
  val isDeleted: Boolean = false
)

@Serializable
data class GroupSyncEntity(
  val id: String,
  val name: String,
  val feedIds: List<String> = emptyList(),
  val pinnedPosition: Double = 0.0,
  val pinnedAt: Long? = null,
  val updatedAt: Long? = null,
  val isDeleted: Boolean = false
)

@Serializable
data class UserSyncEntity(
  val id: String,
  val name: String,
  val profileId: String,
  val email: String,
  val token: String,
  val serverUrl: String
)

@Serializable
data class BlockedWordSyncEntity(
  val id: String,
  val content: String,
  val isDeleted: Boolean,
  val updatedAt: Long
)

object BlockedWordSyncEntityListSerializer : KSerializer<List<BlockedWordSyncEntity>> {
  private val delegateSerializer = ListSerializer(BlockedWordSyncEntity.serializer())

  override val descriptor: SerialDescriptor = delegateSerializer.descriptor

  override fun serialize(encoder: Encoder, value: List<BlockedWordSyncEntity>) {
    delegateSerializer.serialize(encoder, value)
  }

  override fun deserialize(decoder: Decoder): List<BlockedWordSyncEntity> {
    val input = decoder as? JsonDecoder ?: return delegateSerializer.deserialize(decoder)
    val array = input.decodeJsonElement().jsonArray
    return array.map { element ->
      if (element is JsonPrimitive && element.isString) {
        val content = element.content
        BlockedWordSyncEntity(
          id = nameBasedUuidOf(content.lowercase()).toString(),
          content = content,
          isDeleted = false,
          updatedAt = Instant.DISTANT_PAST.toEpochMilliseconds()
        )
      } else {
        input.json.decodeFromJsonElement(BlockedWordSyncEntity.serializer(), element)
      }
    }
  }
}
