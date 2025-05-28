/*
 * Copyright 2024 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.reader

import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.SearchSortOrder
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.reader.ReaderScreenArgs.FromScreen.*
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.getLast24HourStart
import dev.sasikanth.rss.reader.utils.getTodayStartInstant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class ReaderPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  @Assisted private val readerScreenArgs: ReaderScreenArgs,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        readerScreenArgs = readerScreenArgs,
        rssRepository = rssRepository,
        settingsRepository = settingsRepository,
        observableActiveSource = observableActiveSource,
      )
    }

  internal val state = presenterInstance.state

  fun dispatch(event: ReaderEvent) {
    when (event) {
      ReaderEvent.BackClicked -> goBack()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val readerScreenArgs: ReaderScreenArgs,
    private val settingsRepository: SettingsRepository,
    private val observableActiveSource: ObservableActiveSource,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    private val openedPostItems = mutableSetOf<String>()

    private val _state = MutableStateFlow(ReaderState.default(readerScreenArgs.postIndex))
    val state: StateFlow<ReaderState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReaderState.default(readerScreenArgs.postIndex)
      )

    init {
      init()
    }

    fun dispatch(event: ReaderEvent) {
      when (event) {
        ReaderEvent.BackClicked -> {
          /* no-op */
        }
        is ReaderEvent.TogglePostBookmark ->
          togglePostBookmark(event.postId, event.currentBookmarkStatus)
        is ReaderEvent.PostPageChanged -> postPageChange(event.post)
        is ReaderEvent.LoadFullArticleClicked -> loadFullArticleClicked(event.postId)
        is ReaderEvent.PostLoaded -> postLoaded(event.post)
      }
    }

    private fun postLoaded(post: PostWithMetadata) {
      coroutineScope.launch {
        _state.update {
          val newLoadFullArticleMap =
            if (it.loadFullArticleMap.containsKey(post.id)) {
              it.loadFullArticleMap
            } else {
              it.loadFullArticleMap + Pair(post.id, post.alwaysFetchFullArticle)
            }

          it.copy(loadFullArticleMap = newLoadFullArticleMap)
        }
      }
    }

    private fun postPageChange(post: PostWithMetadata) {
      openedPostItems += post.id
    }

    private fun loadFullArticleClicked(postId: String) {
      coroutineScope.launch {
        _state.update {
          val newLoadFullArticleMap =
            if (it.loadFullArticleMap[postId] == true) {
              it.loadFullArticleMap - postId
            } else {
              it.loadFullArticleMap + Pair(postId, true)
            }

          it.copy(loadFullArticleMap = newLoadFullArticleMap)
        }
      }
    }

    private fun markPostsAsRead(): Job {
      return coroutineScope.launch { rssRepository.markPostsAsRead(openedPostItems) }
    }

    private fun init() {
      coroutineScope.launch {
        val currentTime = Clock.System.now()
        val activeSource = observableActiveSource.activeSource.firstOrNull()
        val postsType = settingsRepository.postsType.first()

        val unreadOnly = getUnreadOnly(postsType)
        val postsAfter = getPostsAfter(postsType)
        val activeSourceIds = activeSourceIds(activeSource)

        val posts =
          createPager(
              config =
                createPagingConfig(
                  pageSize = 4,
                  enablePlaceholders = false,
                ),
              initialKey = readerScreenArgs.postIndex
            ) {
              when (readerScreenArgs.fromScreen) {
                Home -> {
                  rssRepository.allPosts(
                    activeSourceIds = activeSourceIds,
                    unreadOnly = unreadOnly,
                    after = postsAfter,
                    lastSyncedAt = currentTime,
                  )
                }
                is Search -> {
                  rssRepository.search(
                    searchQuery = readerScreenArgs.fromScreen.searchQuery,
                    sortOrder = readerScreenArgs.fromScreen.sortOrder,
                  )
                }
                Bookmarks -> {
                  rssRepository.bookmarks()
                }
              }
            }
            .flow
            .cachedIn(coroutineScope)

        _state.update { it.copy(posts = posts) }
      }
    }

    private fun getPostsAfter(postsType: PostsType) =
      when (postsType) {
        PostsType.ALL,
        PostsType.UNREAD -> Instant.DISTANT_PAST
        PostsType.TODAY -> {
          getTodayStartInstant()
        }
        PostsType.LAST_24_HOURS -> {
          getLast24HourStart()
        }
      }

    private fun getUnreadOnly(postsType: PostsType) =
      when (postsType) {
        PostsType.ALL,
        PostsType.TODAY,
        PostsType.LAST_24_HOURS -> null
        PostsType.UNREAD -> true
      }

    private fun activeSourceIds(activeSource: Source?) =
      when (activeSource) {
        is Feed -> listOf(activeSource.id)
        is FeedGroup -> activeSource.feedIds
        else -> emptyList()
      }

    private fun togglePostBookmark(postId: String, currentBookmarkStatus: Boolean) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !currentBookmarkStatus, id = postId)
      }
    }

    override fun onDestroy() {
      markPostsAsRead().invokeOnCompletion { coroutineScope.cancel() }
    }
  }
}

internal typealias ReaderPresenterFactory =
  (
    args: ReaderScreenArgs,
    ComponentContext,
    goBack: () -> Unit,
  ) -> ReaderPresenter

@Serializable
data class ReaderScreenArgs(
  val postIndex: Int,
  val fromScreen: FromScreen,
) {

  @Serializable
  sealed interface FromScreen {

    @Serializable data object Home : FromScreen

    @Serializable
    data class Search(val searchQuery: String, val sortOrder: SearchSortOrder) : FromScreen

    @Serializable data object Bookmarks : FromScreen
  }
}
