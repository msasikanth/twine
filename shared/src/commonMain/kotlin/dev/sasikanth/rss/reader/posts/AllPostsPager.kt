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
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.time.CurrentDateTimeSource
import dev.sasikanth.rss.reader.di.scopes.AppScope
import dev.sasikanth.rss.reader.util.DispatchersProvider
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
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
@AppScope
class AllPostsPager(
  private val observableActiveSource: ObservableActiveSource,
  private val currentDateTimeSource: CurrentDateTimeSource,
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
    coroutineScope.launch {
      observeAllPosts()
      observeHasUnreadPosts()
    }
  }

  private fun observeHasUnreadPosts() {
    combine(
        observableActiveSource.activeSource,
        settingsRepository.postsType,
        currentDateTimeSource.dateTimeFlow
      ) { activeSource, postsType, dateTime ->
        Triple(activeSource, postsType, dateTime)
      }
      .flatMapLatest { (activeSource, postsType, dateTime) ->
        val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime)
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

    combine(activeSourceFlow, postsTypeFlow, currentDateTimeSource.dateTimeFlow) {
        activeSource,
        postsType,
        currentDateTime ->
        Triple(activeSource, postsType, currentDateTime)
      }
      .distinctUntilChanged()
      .onEach { (activeSource, postsType, dateTime) ->
        val unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(postsType)
        val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime)
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

  private fun activeSourceIds(activeSource: Source?) =
    when (activeSource) {
      is Feed -> listOf(activeSource.id)
      is FeedGroup -> activeSource.feedIds
      else -> emptyList()
    }
}
