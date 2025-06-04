/*
 * Copyright 2023 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.home

import androidx.compose.material3.SheetValue
import co.touchlab.crashkios.bugsnag.BugsnagKotlin
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostWithMetadata
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.feeds.FeedsEvent
import dev.sasikanth.rss.reader.feeds.FeedsPresenter
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.NTuple4
import dev.sasikanth.rss.reader.utils.ObservableDate
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
@OptIn(ExperimentalCoroutinesApi::class)
class HomePresenter(
  dispatchersProvider: DispatchersProvider,
  feedsPresenterFactory:
    (
      ComponentContext,
      openGroupSelectionSheet: () -> Unit,
      openFeedInfoSheet: (feedId: String) -> Unit,
      openAddFeedScreen: () -> Unit,
      openGroupScreen: (groupId: String) -> Unit
    ) -> FeedsPresenter,
  private val rssRepository: RssRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val observableDate: ObservableDate,
  private val settingsRepository: SettingsRepository,
  private val allPostsPager: AllPostsPager,
  @Assisted componentContext: ComponentContext,
  @Assisted private val openSearch: () -> Unit,
  @Assisted private val openBookmarks: () -> Unit,
  @Assisted private val openSettings: () -> Unit,
  @Assisted private val openPost: OpenPost,
  @Assisted private val openGroupSelectionSheet: () -> Unit,
  @Assisted private val openFeedInfoSheet: (feedId: String) -> Unit,
  @Assisted private val openAddFeedScreen: () -> Unit,
  @Assisted private val openGroupScreen: (groupId: String) -> Unit,
) : ComponentContext by componentContext {

  internal val feedsPresenter =
    feedsPresenterFactory(
      childContext("feeds_presenter"),
      openGroupSelectionSheet,
      openFeedInfoSheet,
      openAddFeedScreen,
      openGroupScreen,
    )

  private val backCallback = BackCallback {
    if (feedsPresenter.state.value.isInMultiSelectMode) {
      feedsPresenter.dispatch(FeedsEvent.CancelSourcesSelection)
      return@BackCallback
    }

    if (state.value.feedsSheetState == SheetValue.Expanded) {
      dispatch(HomeEvent.BackClicked)
      return@BackCallback
    }
  }

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        observableActiveSource = observableActiveSource,
        observableDate = observableDate,
        settingsRepository = settingsRepository,
        feedsPresenter = feedsPresenter,
        allPostsPager = allPostsPager,
      )
    }

  internal val state = presenterInstance.state

  init {
    lifecycle.doOnCreate {
      backHandler.register(backCallback)
      backCallback.isEnabled = false
    }
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      is HomeEvent.FeedsSheetStateChanged -> {
        backCallback.isEnabled = event.feedsSheetState == SheetValue.Expanded
      }
      is HomeEvent.SearchClicked -> openSearch()
      is HomeEvent.BookmarksClicked -> openBookmarks()
      is HomeEvent.SettingsClicked -> openSettings()
      is HomeEvent.OnPostClicked -> openPost(event.postIndex, event.post)
      else -> {
        // no-op
      }
    }
    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val observableActiveSource: ObservableActiveSource,
    private val observableDate: ObservableDate,
    private val settingsRepository: SettingsRepository,
    private val feedsPresenter: FeedsPresenter,
    private val allPostsPager: AllPostsPager,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)
    private val scrolledPostItems = mutableSetOf<String>()

    private val defaultState =
      HomeState.default(
        currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
      )
    private val _state = MutableStateFlow(defaultState)
    val state: StateFlow<HomeState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = defaultState
      )

    init {
      dispatch(HomeEvent.Init)
    }

    fun dispatch(event: HomeEvent) {
      when (event) {
        HomeEvent.Init -> init()
        HomeEvent.OnSwipeToRefresh -> refreshContent()
        is HomeEvent.OnPostClicked -> {
          // no-op
        }
        is HomeEvent.FeedsSheetStateChanged -> feedsSheetStateChanged(event.feedsSheetState)
        HomeEvent.OnHomeSelected -> onHomeSelected()
        HomeEvent.BackClicked -> backClicked()
        HomeEvent.SearchClicked -> {
          /* no-op */
        }
        is HomeEvent.OnPostBookmarkClick -> onPostBookmarkClicked(event.post)
        HomeEvent.BookmarksClicked -> {
          /* no-op */
        }
        HomeEvent.SettingsClicked -> {
          /* no-op */
        }
        is HomeEvent.OnPostSourceClicked -> postSourceClicked(event.feedId)
        is HomeEvent.OnPostsTypeChanged -> onPostsTypeChanged(event.postsType)
        is HomeEvent.TogglePostReadStatus -> togglePostReadStatus(event.postId, event.postRead)
        is HomeEvent.MarkPostsAsRead -> markPostsAsRead(event.source)
        is HomeEvent.OnPostItemsScrolled -> onPostItemsScrolled(event.postIds)
        HomeEvent.MarkScrolledPostsAsRead -> markScrolledPostsAsRead()
        is HomeEvent.MarkFeaturedPostsAsRead -> markFeaturedPostAsRead(event.postId)
        is HomeEvent.ChangeHomeViewMode -> changeHomeViewMode(event.homeViewMode)
      }
    }

    private fun changeHomeViewMode(homeViewMode: HomeViewMode) {
      coroutineScope.launch { settingsRepository.updateHomeViewMode(homeViewMode) }
    }

    private fun markFeaturedPostAsRead(postId: String) {
      coroutineScope.launch {
        val markPostsAsReadOn = settingsRepository.markAsReadOn.first()

        if (markPostsAsReadOn != MarkAsReadOn.Scroll) return@launch

        rssRepository.updatePostReadStatus(read = true, id = postId)
      }
    }

    private fun markScrolledPostsAsRead() {
      coroutineScope.launch {
        val markPostsAsReadOn = settingsRepository.markAsReadOn.first()

        if (markPostsAsReadOn != MarkAsReadOn.Scroll) return@launch

        rssRepository.markPostsAsRead(postIds = scrolledPostItems)
        scrolledPostItems.clear()
      }
    }

    private fun onPostItemsScrolled(postIds: List<String>) {
      scrolledPostItems += postIds
    }

    private fun markPostsAsRead(source: Source?) {
      coroutineScope.launch {
        val postsAfter = postsThresholdTime(_state.value.postsType)

        when (source) {
          is Feed -> {
            rssRepository.markPostsInFeedAsRead(
              feedIds = listOf(source.id),
              postsAfter = postsAfter
            )
          }
          is FeedGroup -> {
            rssRepository.markPostsInFeedAsRead(feedIds = source.feedIds, postsAfter = postsAfter)
          }
          null -> {
            rssRepository.markPostsAsRead(postsAfter = postsAfter)
          }
        }
      }
    }

    private fun togglePostReadStatus(postId: String, postRead: Boolean) {
      coroutineScope.launch { rssRepository.updatePostReadStatus(read = !postRead, id = postId) }
    }

    private fun onPostsTypeChanged(postsType: PostsType) {
      coroutineScope.launch { settingsRepository.updatePostsType(postsType) }
    }

    private fun postSourceClicked(feedId: String) {
      coroutineScope.launch {
        val feed = rssRepository.feedBlocking(feedId)
        observableActiveSource.changeActiveSource(feed)
      }
    }

    private fun onPostBookmarkClicked(post: PostWithMetadata) {
      coroutineScope.launch {
        rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
      }
    }

    private fun backClicked() {
      coroutineScope.launch {
        _state.update { it.copy(feedsSheetState = SheetValue.PartiallyExpanded) }
      }
    }

    private fun init() {
      val activeSourceFlow = observableActiveSource.activeSource
      val postsTypeFlow = settingsRepository.postsType

      rssRepository
        .hasFeeds()
        .distinctUntilChanged()
        .onEach { hasFeeds -> _state.update { it.copy(hasFeeds = hasFeeds) } }
        .launchIn(coroutineScope)

      combine(
          allPostsPager.allPostsPagingData,
          observableDate.dateTimeFlow,
        ) { postsPagingData, dateTime ->
          Pair(dateTime, postsPagingData)
        }
        .onEach { (dateTime, postsPagingData) ->
          _state.update { it.copy(currentDateTime = dateTime, posts = postsPagingData) }
        }
        .launchIn(coroutineScope)

      combine(
          activeSourceFlow,
          postsTypeFlow,
          settingsRepository.homeViewMode,
          allPostsPager.hasUnreadPosts,
        ) { activeSource, postsType, homeViewMode, hasUnreadPosts ->
          NTuple4(activeSource, postsType, homeViewMode, hasUnreadPosts)
        }
        .distinctUntilChanged()
        .onEach { (activeSource, postsType, homeViewMode, hasUnreadPosts) ->
          _state.update {
            it.copy(
              activeSource = activeSource,
              postsType = postsType,
              homeViewMode = homeViewMode,
              hasUnreadPosts = hasUnreadPosts
            )
          }
        }
        .launchIn(coroutineScope)
    }

    private fun postsThresholdTime(
      postsType: PostsType,
    ): Instant {
      val dateTime = _state.value.currentDateTime
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

    private fun feedsSheetStateChanged(feedsSheetState: SheetValue) {
      _state.update {
        // Clear search query once feeds sheet is collapsed
        if (feedsSheetState == SheetValue.PartiallyExpanded) {
          feedsPresenter.dispatch(FeedsEvent.ClearSearchQuery)
        }

        it.copy(feedsSheetState = feedsSheetState)
      }
    }

    private fun onHomeSelected() {
      coroutineScope.launch { observableActiveSource.clearSelection() }
    }

    private fun refreshContent() {
      coroutineScope.launch {
        _state.update { it.copy(loadingState = HomeLoadingState.Loading) }
        observableDate.refresh()

        try {
          when (val selectedSource = _state.value.activeSource) {
            is FeedGroup -> rssRepository.updateGroup(selectedSource.feedIds)
            is Feed -> rssRepository.updateFeed(selectedSource.id)
            else -> rssRepository.updateFeeds()
          }
        } catch (e: Exception) {
          BugsnagKotlin.logMessage("RefreshContent")
          BugsnagKotlin.sendHandledException(e)
        } finally {
          _state.update { it.copy(loadingState = HomeLoadingState.Idle) }
        }
      }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}

internal typealias HomePresenterFactory =
  (
    ComponentContext,
    openSearch: () -> Unit,
    openBookmarks: () -> Unit,
    openSettings: () -> Unit,
    openPost: OpenPost,
    openGroupSelectionSheet: () -> Unit,
    openFeedInfoSheet: (feedId: String) -> Unit,
    openAddFeedScreen: () -> Unit,
    openGroupScreen: (groupId: String) -> Unit,
  ) -> HomePresenter

private typealias OpenPost = (postIndex: Int, post: PostWithMetadata) -> Unit
