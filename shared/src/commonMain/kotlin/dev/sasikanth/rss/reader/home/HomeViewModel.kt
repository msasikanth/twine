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

package dev.sasikanth.rss.reader.home

import androidx.compose.material3.SheetValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.posts.AllPostsPager
import dev.sasikanth.rss.reader.utils.InAppRating
import dev.sasikanth.rss.reader.utils.NTuple6
import dev.sasikanth.rss.reader.utils.combine
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject

@Inject
class HomeViewModel(
  private val rssRepository: RssRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val refreshPolicy: RefreshPolicy,
  private val settingsRepository: SettingsRepository,
  private val allPostsPager: AllPostsPager,
  private val syncCoordinator: SyncCoordinator,
  private val inAppRating: InAppRating,
) : ViewModel() {

  private val scrolledPostItems = mutableSetOf<String>()

  private val defaultState =
    HomeState.default(
      currentDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    )
  private val _state = MutableStateFlow(defaultState)
  val state: StateFlow<HomeState>
    get() = _state

  init {
    init()
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      HomeEvent.OnSwipeToRefresh -> refreshContent()
      is HomeEvent.FeedsSheetStateChanged -> feedsSheetStateChanged(event.feedsSheetState)
      HomeEvent.OnHomeSelected -> onHomeSelected()
      is HomeEvent.OnPostBookmarkClick -> onPostBookmarkClicked(event.post)
      is HomeEvent.OnPostSourceClicked -> postSourceClicked(event.feedId)
      is HomeEvent.OnPostsTypeChanged -> onPostsTypeChanged(event.postsType)
      is HomeEvent.UpdatePostReadStatus ->
        updatePostReadStatus(event.postId, event.updatedReadStatus)
      is HomeEvent.MarkPostsAsRead -> markPostsAsRead(event.source)
      is HomeEvent.OnPostItemsScrolled -> onPostItemsScrolled(event.postIds)
      HomeEvent.MarkScrolledPostsAsRead -> markScrolledPostsAsRead()
      is HomeEvent.MarkFeaturedPostsAsRead -> markFeaturedPostAsRead(event.postId)
      is HomeEvent.ChangeHomeViewMode -> changeHomeViewMode(event.homeViewMode)
      is HomeEvent.UpdateVisibleItemIndex -> updateVisibleItemIndex(event.index)
      is HomeEvent.LoadNewArticlesClick -> loadNewArticles()
      is HomeEvent.UpdateDate -> updateDate()
      is HomeEvent.UpdatePrevActiveSource -> updatePrevActiveSource(event)
      is HomeEvent.OnPostsSortFilterApplied -> onPostsSortFilterApplied(event)
      is HomeEvent.ShowPostsSortFilter -> showPostsSortFilter(event.show)
    }
  }

  private fun showPostsSortFilter(show: Boolean) {
    _state.update { it.copy(showPostsSortFilter = show) }
  }

  private fun onPostsSortFilterApplied(event: HomeEvent.OnPostsSortFilterApplied) {
    viewModelScope.launch {
      settingsRepository.updatePostsType(event.postsType)
      settingsRepository.updatePostsSortOrder(event.postsSortOrder)
      _state.update { it.copy(showPostsSortFilter = false) }
    }
  }

  private fun init() {
    val activeSourceFlow = observableActiveSource.activeSource
    val postsTypeFlow = settingsRepository.postsType
    val allPostsPagingData = allPostsPager.allPostsPagingData.cachedIn(viewModelScope)

    _state.update { it.copy(posts = allPostsPagingData) }

    syncCoordinator.syncState
      .onEach { syncState -> _state.update { it.copy(syncState = syncState) } }
      .launchIn(viewModelScope)

    rssRepository
      .hasFeeds()
      .distinctUntilChanged()
      .onEach { hasFeeds -> _state.update { it.copy(hasFeeds = hasFeeds) } }
      .launchIn(viewModelScope)

    combine(
        activeSourceFlow,
        postsTypeFlow,
        settingsRepository.postsSortOrder,
        settingsRepository.homeViewMode,
        allPostsPager.hasUnreadPosts,
        allPostsPager.unreadSinceLastSync,
      ) { activeSource, postsType, postsSortOrder, homeViewMode, hasUnreadPosts, unreadSinceLastSync
        ->
        NTuple6(
          activeSource,
          postsType,
          postsSortOrder,
          homeViewMode,
          hasUnreadPosts,
          unreadSinceLastSync,
        )
      }
      .distinctUntilChanged()
      .onEach {
        (activeSource, postsType, postsSortOrder, homeViewMode, hasUnreadPosts, unreadSinceLastSync)
        ->
        _state.update {
          it.copy(
            activeSource = activeSource,
            postsType = postsType,
            postsSortOrder = postsSortOrder,
            homeViewMode = homeViewMode,
            hasUnreadPosts = hasUnreadPosts,
            unreadSinceLastSync = unreadSinceLastSync,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  private fun updatePrevActiveSource(event: HomeEvent.UpdatePrevActiveSource) {
    _state.update { it.copy(prevActiveSource = event.source) }
  }

  private fun updateDate() {
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    if (_state.value.currentDateTime.date != currentDate.date) {
      _state.update { it.copy(currentDateTime = currentDate) }
    }
  }

  private fun loadNewArticles() {
    viewModelScope.launch {
      _state.update { it.copy(unreadSinceLastSync = null) }
      refreshPolicy.updateLastRefreshedAt()
      inAppRating.request()
    }
  }

  private fun updateVisibleItemIndex(index: Int) {
    viewModelScope.launch { _state.update { it.copy(activePostIndex = index) } }
  }

  private fun changeHomeViewMode(homeViewMode: HomeViewMode) {
    viewModelScope.launch { settingsRepository.updateHomeViewMode(homeViewMode) }
  }

  private fun markFeaturedPostAsRead(postId: String) {
    viewModelScope.launch {
      val markPostsAsReadOn = settingsRepository.markAsReadOn.first()

      if (markPostsAsReadOn != MarkAsReadOn.Scroll) return@launch

      rssRepository.updatePostReadStatus(read = true, id = postId)
    }
  }

  private fun markScrolledPostsAsRead() {
    viewModelScope.launch {
      val markPostsAsReadOn = settingsRepository.markAsReadOn.first()

      if (markPostsAsReadOn != MarkAsReadOn.Scroll) return@launch

      val postIds = scrolledPostItems.toSet()
      scrolledPostItems.clear()
      rssRepository.markPostsAsRead(postIds = postIds)
    }
  }

  private fun onPostItemsScrolled(postIds: List<String>) {
    scrolledPostItems += postIds
  }

  private fun markPostsAsRead(source: Source?) {
    viewModelScope.launch {
      val postsAfter = postsThresholdTime(_state.value.postsType)

      when (source) {
        is Feed -> {
          rssRepository.markPostsInFeedAsRead(feedIds = listOf(source.id), postsAfter = postsAfter)
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

  private fun updatePostReadStatus(postId: String, updatedReadStatus: Boolean) {
    viewModelScope.launch {
      rssRepository.updatePostReadStatus(read = updatedReadStatus, id = postId)
    }
  }

  private fun onPostsTypeChanged(postsType: PostsType) {
    viewModelScope.launch { settingsRepository.updatePostsType(postsType) }
  }

  private fun postSourceClicked(feedId: String) {
    viewModelScope.launch {
      val feed = rssRepository.feedBlocking(feedId)
      observableActiveSource.changeActiveSource(feed)
    }
  }

  private fun onPostBookmarkClicked(post: ResolvedPost) {
    viewModelScope.launch {
      rssRepository.updateBookmarkStatus(bookmarked = !post.bookmarked, id = post.id)
    }
  }

  private fun postsThresholdTime(postsType: PostsType): Instant {
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
    _state.update { it.copy(feedsSheetState = feedsSheetState) }
  }

  private fun onHomeSelected() {
    viewModelScope.launch { observableActiveSource.clearSelection() }
  }

  private fun refreshContent() {
    viewModelScope.launch {
      when (val selectedSource = _state.value.activeSource) {
        is FeedGroup -> syncCoordinator.pull(selectedSource.feedIds)
        is Feed -> syncCoordinator.pull(selectedSource.id)
        else -> syncCoordinator.pull()
      }
    }
  }
}
