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

package dev.sasikanth.rss.reader.posts

import androidx.paging.PagingData
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.sync.SyncState
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlin.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AllPostsPager(
  observableActiveSource: ObservableActiveSource,
  settingsRepository: SettingsRepository,
  refreshPolicy: RefreshPolicy,
  private val rssRepository: RssRepository,
  private val syncCoordinator: SyncCoordinator,
  dispatchersProvider: DispatchersProvider,
) {
  private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  private data class PostsParameters(
    val activeSourceIds: List<String>,
    val postsAfter: Instant,
    val lastSyncedAt: Instant,
    val unreadOnly: Boolean?,
    val postsSortOrder: PostsSortOrder,
  )

  private val baseParameters =
    combine(
        observableActiveSource.activeSource,
        settingsRepository.postsType,
        settingsRepository.postsSortOrder,
        refreshPolicy.dateTimeFlow
      ) { activeSource, postsType, postsSortOrder, dateTime ->
        computeParameters(activeSource, postsType, postsSortOrder, dateTime)
      }
      .distinctUntilChanged()

  val allPostsPagingData: Flow<PagingData<PostWithMetadata>> =
    baseParameters.flatMapLatest { params ->
      createPager(config = createPagingConfig(pageSize = 20, enablePlaceholders = true)) {
          rssRepository.allPosts(
            activeSourceIds = params.activeSourceIds,
            postsSortOrder = params.postsSortOrder,
            unreadOnly = params.unreadOnly,
            after = params.postsAfter,
            lastSyncedAt = params.lastSyncedAt
          )
        }
        .flow
    }

  private val _hasUnreadPosts = MutableStateFlow(false)
  val hasUnreadPosts: StateFlow<Boolean> = _hasUnreadPosts.asStateFlow()

  private val _unreadSinceLastSync = MutableStateFlow<UnreadSinceLastSync?>(null)
  val unreadSinceLastSync: StateFlow<UnreadSinceLastSync?> = _unreadSinceLastSync.asStateFlow()

  init {
    observeHasUnreadPosts()
    observeHasNewerArticles()
  }

  private fun observeHasNewerArticles() {
    syncCoordinator.syncState
      .flatMapLatest { syncState ->
        if (syncState is SyncState.InProgress) {
          emptyFlow()
        } else {
          baseParameters.flatMapLatest { params ->
            rssRepository.unreadSinceLastSync(
              sources = params.activeSourceIds,
              postsAfter = params.postsAfter,
              lastSyncedAt = params.lastSyncedAt
            )
          }
        }
      }
      .distinctUntilChanged()
      .onEach { _unreadSinceLastSync.value = it }
      .launchIn(coroutineScope)
  }

  private fun observeHasUnreadPosts() {
    baseParameters
      .flatMapLatest { params ->
        rssRepository.hasUnreadPostsInSource(
          activeSourceIds = params.activeSourceIds,
          postsAfter = params.postsAfter,
        )
      }
      .onEach { _hasUnreadPosts.value = it }
      .launchIn(coroutineScope)
  }

  private fun computeParameters(
    activeSource: Source?,
    postsType: PostsType,
    postsSortOrder: PostsSortOrder,
    dateTime: LocalDateTime
  ): PostsParameters {
    return PostsParameters(
      activeSourceIds = activeSourceIds(activeSource),
      postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime),
      lastSyncedAt = dateTime.toInstant(TimeZone.currentSystemDefault()),
      unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(postsType),
      postsSortOrder = postsSortOrder,
    )
  }

  private fun activeSourceIds(activeSource: Source?) =
    when (activeSource) {
      is Feed -> listOf(activeSource.id)
      is FeedGroup -> activeSource.feedIds
      else -> emptyList()
    }
}
