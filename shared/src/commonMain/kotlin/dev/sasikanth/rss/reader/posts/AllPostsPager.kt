/*
 * Copyright 2025 Sasikanth Miriyampalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.sasikanth.rss.reader.posts

import androidx.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.ObservableDate
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AllPostsPager(
  private val observableActiveSource: ObservableActiveSource,
  private val observableDate: ObservableDate,
  private val settingsRepository: SettingsRepository,
  private val rssRepository: RssRepository,
  dispatchersProvider: DispatchersProvider,
) {
  private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  private val _allPostsPagingData: MutableStateFlow<Flow<PagingData<PostWithMetadata>>> =
    MutableStateFlow(emptyFlow())
  val allPostsPagingData: Flow<Flow<PagingData<PostWithMetadata>>>
    get() = _allPostsPagingData

  private val _hasUnreadPosts: MutableStateFlow<Boolean> = MutableStateFlow(false)
  val hasUnreadPosts: Flow<Boolean>
    get() = _hasUnreadPosts

  init {
    observeAllPosts()
    observeHasUnreadPosts()
  }

  private fun observeHasUnreadPosts() {
    combine(
        observableActiveSource.activeSource,
        settingsRepository.postsType,
        observableDate.dateTimeFlow
      ) { activeSource, postsType, dateTime ->
        Triple(activeSource, postsType, dateTime)
      }
      .flatMapLatest { (activeSource, postsType, dateTime) ->
        val postsAfter = postsThresholdTime(postsType, dateTime)
        val activeSourceIds = activeSourceIds(activeSource)

        rssRepository.hasUnreadPostsInSource(
          activeSourceIds = activeSourceIds,
          postsAfter = postsAfter,
        )
      }
      .onEach { hasUnreadPosts -> _hasUnreadPosts.value = hasUnreadPosts }
      .launchIn(coroutineScope)
  }

  private fun observeAllPosts() {
    val activeSourceFlow = observableActiveSource.activeSource
    val postsTypeFlow = settingsRepository.postsType

    combine(activeSourceFlow, postsTypeFlow, observableDate.dateTimeFlow) {
        activeSource,
        postsType,
        currentDateTime ->
        Triple(activeSource, postsType, currentDateTime)
      }
      .distinctUntilChanged()
      .onEach { (activeSource, postsType, dateTime) ->
        val unreadOnly = shouldGetUnreadPostsOnly(postsType)
        val postsAfter = postsThresholdTime(postsType, dateTime)
        val activeSourceIds = activeSourceIds(activeSource)

        val postsPagingDataFlow =
          createPager(config = createPagingConfig(pageSize = 20, enablePlaceholders = true)) {
              rssRepository.allPosts(
                activeSourceIds = activeSourceIds,
                unreadOnly = unreadOnly,
                after = postsAfter,
                lastSyncedAt = dateTime.toInstant(TimeZone.currentSystemDefault())
              )
            }
            .flow
            .cachedIn(coroutineScope)

        _allPostsPagingData.value = postsPagingDataFlow
      }
      .launchIn(coroutineScope)
  }

  private fun postsThresholdTime(postsType: PostsType, dateTime: LocalDateTime): Instant {
    return when (postsType) {
      PostsType.ALL,
      PostsType.UNREAD -> Instant.DISTANT_PAST
      PostsType.TODAY -> {
        dateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault())
      }
      PostsType.LAST_24_HOURS -> {
        dateTime.toInstant(TimeZone.currentSystemDefault()).minus(24.hours)
      }
    }
  }

  private fun shouldGetUnreadPostsOnly(postsType: PostsType): Boolean? {
    return when (postsType) {
      PostsType.UNREAD -> true
      PostsType.ALL,
      PostsType.TODAY,
      PostsType.LAST_24_HOURS -> null
    }
  }

  private fun activeSourceIds(activeSource: Source?) =
    when (activeSource) {
      is Feed -> listOf(activeSource.id)
      is FeedGroup -> activeSource.feedIds
      else -> emptyList()
    }
}
