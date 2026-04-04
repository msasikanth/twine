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

import app.cash.paging.PagingSource
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.paging3.QueryPagingSource
import dev.sasikanth.rss.reader.core.base.widget.WidgetUpdater
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.data.database.FeedGroupFeedQueries
import dev.sasikanth.rss.reader.data.database.FeedGroupQueries
import dev.sasikanth.rss.reader.data.database.TransactionRunner
import dev.sasikanth.rss.reader.data.utils.Constants
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.util.nameBasedUuidOf
import dev.sasikanth.rss.reader.util.splitAndTrim
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class FeedGroupRepository(
  private val feedGroupQueries: FeedGroupQueries,
  private val feedGroupFeedQueries: FeedGroupFeedQueries,
  private val transactionRunner: TransactionRunner,
  private val widgetUpdater: WidgetUpdater,
  private val dispatchersProvider: DispatchersProvider,
) {

  suspend fun createGroup(name: String): String {
    return withContext(dispatchersProvider.databaseWrite) {
      val id = nameBasedUuidOf(name).toString()
      feedGroupQueries.createGroup(
        id = id,
        name = name,
        createdAt = Clock.System.now(),
        updatedAt = Clock.System.now(),
      )
      id
    }
  }

  suspend fun updateGroupName(groupId: String, name: String) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateGroupName(name = name, id = groupId, updatedAt = Clock.System.now())
    }
  }

  suspend fun updateFeedGroupUpdatedAt(groupId: String, updatedAt: Instant) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateUpdatedAt(updatedAt, groupId)
    }
  }

  suspend fun updateFeedGroupRemoteId(
    remoteId: String,
    groupId: String,
    updatedAt: Instant = Clock.System.now(),
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.updateFeedGroupRemoteId(
        remoteId = remoteId,
        updatedAt = updatedAt,
        id = groupId,
      )
    }
  }

  suspend fun feedGroupBlocking(id: String): FeedGroup? {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries.group(id, mapper = ::mapToFeedGroup).executeAsOneOrNull()
    }
  }

  suspend fun allFeedGroupsBlocking(): List<FeedGroup> {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries.allGroupsBlocking(mapper = ::mapToFeedGroup).executeAsList()
    }
  }

  fun allGroups(): PagingSource<Int, FeedGroup> {
    return QueryPagingSource(
      countQuery = feedGroupQueries.count(),
      transacter = feedGroupQueries,
      context = dispatchersProvider.databaseRead,
      queryProvider = { limit, offset ->
        feedGroupQueries.groups(limit = limit, offset = offset, mapper = ::mapToFeedGroupFromGroups)
      },
    )
  }

  fun numberOfFeedGroups(): Flow<Long> {
    return feedGroupQueries.count().asFlow().mapToOne(dispatchersProvider.databaseRead)
  }

  suspend fun groupByIds(ids: Set<String>): List<FeedGroup> {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .groupsByIds(ids = ids, mapper = ::mapToFeedGroupFromGroupsByIds)
        .executeAsList()
    }
  }

  fun groupById(groupId: String): Flow<FeedGroup> {
    return feedGroupQueries
      .groupsByIds(ids = setOf(groupId), mapper = ::mapToFeedGroupFromGroupsByIds)
      .asFlow()
      .mapToOne(dispatchersProvider.databaseRead)
  }

  suspend fun feedGroupByRemoteId(remoteId: String): FeedGroup? {
    return withContext(dispatchersProvider.databaseRead) {
      feedGroupQueries
        .feedGroupByRemoteId(
          remoteId = remoteId,
          mapper = { id, name, createdAt, updatedAt, pinnedAt, pinnedPosition, isDeleted, remoteId
            ->
            FeedGroup(
              id = id,
              name = name,
              feedIds = emptyList(),
              feedHomepageLinks = emptyList(),
              feedIconLinks = emptyList(),
              feedShowFavIconSettings = emptyList(),
              createdAt = createdAt,
              updatedAt = updatedAt,
              pinnedAt = pinnedAt,
              pinnedPosition = pinnedPosition,
              isDeleted = isDeleted,
              remoteId = remoteId,
            )
          },
        )
        .executeAsOneOrNull()
    }
  }

  suspend fun upsertGroup(
    id: String,
    name: String,
    pinnedAt: Instant?,
    updatedAt: Instant,
    isDeleted: Boolean,
    remoteId: String? = null,
  ) {
    withContext(dispatchersProvider.databaseWrite) {
      feedGroupQueries.upsertSyncGroup(
        id = id,
        name = name,
        createdAt = Clock.System.now(),
        updatedAt = updatedAt,
        pinnedAt = pinnedAt,
        isDeleted = isDeleted,
        remoteId = remoteId,
      )
    }
    widgetUpdater.updateUnreadWidget()
  }

  suspend fun replaceFeedsInGroup(groupId: String, feedIds: List<String>) {
    withContext(dispatchersProvider.databaseWrite) {
      transactionRunner.invoke {
        feedGroupFeedQueries.removeAllFeedsFromGroup(groupId)
        feedIds.forEach { feedId ->
          feedGroupFeedQueries.addFeedToGroup(feedGroupId = groupId, feedId = feedId)
        }
      }
    }
  }

  suspend fun groupIdsForFeed(feedId: String): List<String> {
    return withContext(dispatchersProvider.io) {
      feedGroupFeedQueries
        .groupIdsForFeed(feedId)
        .executeAsList()
        .map { it.feedGroupId }
        .filterNotNull()
    }
  }

  private fun mapToFeedShowFavIconSettings(feedShowFavIconSettings: String?): List<Boolean> {
    return feedShowFavIconSettings?.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR)?.map {
      when (it) {
        "1" -> true
        else -> false
      }
    } ?: emptyList()
  }

  private fun mapToFeedGroup(
    id: String,
    name: String,
    feedIds: String?,
    feedHomepageLinks: String,
    feedIconLinks: String,
    feedShowFavIconSettings: String,
    createdAt: Instant,
    updatedAt: Instant,
    pinnedAt: Instant?,
    pinnedPosition: Double,
    isDeleted: Boolean,
    remoteId: String?,
  ): FeedGroup {
    return FeedGroup(
      id = id,
      name = name,
      feedIds = feedIds.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedHomepageLinks = feedHomepageLinks.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedIconLinks = feedIconLinks.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
      createdAt = createdAt,
      updatedAt = updatedAt,
      pinnedAt = pinnedAt,
      pinnedPosition = pinnedPosition,
      isDeleted = isDeleted,
      remoteId = remoteId,
    )
  }

  private fun mapToFeedGroupFromGroups(
    id: String,
    name: String,
    feedIds: String?,
    feedHomepageLinks: String,
    feedIcons: String,
    feedShowFavIconSettings: String,
    createdAt: Instant,
    updatedAt: Instant,
    pinnedAt: Instant?,
    pinnedPosition: Double,
    remoteId: String?,
  ): FeedGroup {
    return FeedGroup(
      id = id,
      name = name,
      feedIds = feedIds.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedHomepageLinks = feedHomepageLinks.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedIconLinks = feedIcons.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
      createdAt = createdAt,
      updatedAt = updatedAt,
      pinnedAt = pinnedAt,
      pinnedPosition = pinnedPosition,
      remoteId = remoteId,
    )
  }

  private fun mapToFeedGroupFromGroupsByIds(
    id: String,
    name: String,
    feedIds: String?,
    feedHomepageLinks: String,
    feedIcons: String,
    feedShowFavIconSettings: String,
    createdAt: Instant,
    updatedAt: Instant,
    pinnedAt: Instant?,
    remoteId: String?,
  ): FeedGroup {
    return FeedGroup(
      id = id,
      name = name,
      feedIds = feedIds.orEmpty().splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedHomepageLinks = feedHomepageLinks.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedIconLinks = feedIcons.splitAndTrim(Constants.GROUP_CONCAT_SEPARATOR),
      feedShowFavIconSettings = mapToFeedShowFavIconSettings(feedShowFavIconSettings),
      createdAt = createdAt,
      updatedAt = updatedAt,
      pinnedAt = pinnedAt,
      remoteId = remoteId,
    )
  }
}
