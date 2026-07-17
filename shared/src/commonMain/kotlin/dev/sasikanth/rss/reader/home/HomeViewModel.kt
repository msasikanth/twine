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
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.PostsSortOrder
import dev.sasikanth.rss.reader.core.model.local.PostsType
import dev.sasikanth.rss.reader.core.model.local.ResolvedPost
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.ThemeVariant
import dev.sasikanth.rss.reader.core.model.local.UnreadSinceLastSync
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.HomeViewMode
import dev.sasikanth.rss.reader.data.repository.MarkAsReadOn
import dev.sasikanth.rss.reader.data.repository.ObservableActiveReaderPost
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.ObservableSelectedPost
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.home.ui.PostListKey
import dev.sasikanth.rss.reader.posts.AllPostsPager
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class HomeViewModel(
  private val rssRepository: RssRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val refreshPolicy: RefreshPolicy,
  private val settingsRepository: SettingsRepository,
  private val allPostsPager: AllPostsPager,
  private val syncCoordinator: SyncCoordinator,
  private val observableSelectedPost: ObservableSelectedPost,
  observableActiveReaderPost: ObservableActiveReaderPost,
) : ViewModel() {

  val activeReaderPostId: StateFlow<String?> = observableActiveReaderPost.activePostId

  private val defaultState = HomeState.default()
  val state: StateFlow<HomeState>
    field = MutableStateFlow(defaultState)

  private val _effects = MutableSharedFlow<HomeEffect>()
  val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()

  val openPost: SharedFlow<Pair<Int, ResolvedPost>>
    field = MutableSharedFlow<Pair<Int, ResolvedPost>>()

  private var previousVisibleItems = emptyMap<String, Int>()

  private val isFeaturedSectionVisible = MutableStateFlow(true)
  private val selectedPostResolveTrigger = MutableStateFlow(0)

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
      is HomeEvent.MarkPostsAsReadByIds -> markPostsAsReadByIds(event.postIds)
      is HomeEvent.MarkFeaturedPostsAsRead -> markFeaturedPostAsRead(event.postId)
      is HomeEvent.OnVisiblePostsChanged ->
        onVisiblePostsChanged(event.visiblePosts, event.firstVisibleItemIndex)
      is HomeEvent.ChangeHomeViewMode -> changeHomeViewMode(event.homeViewMode)
      is HomeEvent.UpdateVisibleItemIndex -> updateVisibleItemIndex(event.index, event.postId)
      HomeEvent.OnScreenStarted -> onScreenStarted()
      is HomeEvent.OnScreenStopped -> onScreenStopped(event)
      is HomeEvent.UpdateFeaturedSectionVisibility -> updateFeaturedSectionVisibility(event.visible)
      HomeEvent.LoadNewArticlesClick -> loadNewArticles()
      is HomeEvent.UpdatePrevActiveSource -> updatePrevActiveSource(event)
      is HomeEvent.OnPostsSortFilterApplied -> onPostsSortFilterApplied(event)
      is HomeEvent.ShowPostsSortFilter -> showPostsSortFilter(event.show)
      is HomeEvent.OnPostClicked -> onPostClicked(event.post)
    }
  }

  private fun onScreenStarted() {
    selectedPostResolveTrigger.update { it + 1 }
  }

  private fun updateFeaturedSectionVisibility(visible: Boolean) {
    isFeaturedSectionVisible.value = visible
  }

  private fun effectiveFeaturedPosts() =
    if (isFeaturedSectionVisible.value) {
      state.value.featuredPosts.value
    } else {
      persistentListOf()
    }

  private fun onScreenStopped(event: HomeEvent.OnScreenStopped) {
    val featuredPosts = effectiveFeaturedPosts()
    val isFeaturedItem = featuredPosts.isNotEmpty() && event.firstVisibleItemIndex == 0
    val postId =
      if (isFeaturedItem) {
        featuredPosts.getOrNull(event.settledPage)?.resolvedPost?.id
      } else {
        event.firstVisibleItemKey?.let { PostListKey.decodeSafe(it)?.postId }
      }

    val fallbackIndex =
      if (event.firstVisibleItemIndex == 0) {
        if (isFeaturedItem) event.settledPage else 0
      } else {
        featuredPosts.size + event.firstVisibleItemIndex - 1
      }

    observableSelectedPost.updateSelectedPost(
      index = fallbackIndex,
      postId = postId,
      scrollOffset = event.firstVisibleItemOffset,
    )
  }

  private fun onVisiblePostsChanged(visiblePosts: Map<String, Int>, firstVisibleItemIndex: Int) {
    val newlyHiddenIds = previousVisibleItems.keys - visiblePosts.keys
    if (newlyHiddenIds.isNotEmpty()) {
      val itemsHiddenFromTop =
        newlyHiddenIds
          .filter { id ->
            val previousIndex = previousVisibleItems[id]
            previousIndex != null && previousIndex < firstVisibleItemIndex
          }
          .toSet()

      if (itemsHiddenFromTop.isNotEmpty()) {
        markPostsAsReadByIds(itemsHiddenFromTop)
      }
    }
    previousVisibleItems = visiblePosts
  }

  private fun markPostsAsReadByIds(postIds: Set<String>) {
    viewModelScope.launch {
      val markPostsAsReadOn = settingsRepository.markAsReadOn.first()
      if (markPostsAsReadOn != MarkAsReadOn.Scroll) return@launch

      rssRepository.markPostsAsRead(postIds = postIds)
    }
  }

  private fun onPostClicked(post: ResolvedPost) {
    viewModelScope.launch {
      val postsAfter = postsThresholdTime(state.value.postsType)
      val activeSourceIds = activeSourceIds(state.value.activeSource)
      val unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(state.value.postsType)
      val postsUpperBound =
        state.value.lastRefreshedAt?.toInstant(TimeZone.currentSystemDefault())
          ?: Clock.System.now()

      val position =
        rssRepository.postPosition(
          postId = post.id,
          sourceId = post.sourceId,
          activeSourceIds = activeSourceIds,
          postsSortOrder = state.value.postsSortOrder,
          unreadOnly = unreadOnly,
          after = postsAfter,
          postsUpperBound = postsUpperBound,
        )

      if (position != null) {
        openPost.emit(position to post)
      }
    }
  }

  private fun activeSourceIds(activeSource: Source?) =
    when (activeSource) {
      is Feed -> listOf(activeSource.id)
      is FeedGroup -> activeSource.feedIds
      else -> emptyList()
    }

  private suspend fun calculateHomeIndex(postId: String, index: Int): Int {
    val featuredPosts = effectiveFeaturedPosts()
    val postsAfter = postsThresholdTime(state.value.postsType)
    val activeSourceIds = activeSourceIds(state.value.activeSource)
    val unreadOnly = PostsFilterUtils.shouldGetUnreadPostsOnly(state.value.postsType)
    val lastRefreshedAt =
      state.value.lastRefreshedAt?.toInstant(TimeZone.currentSystemDefault()) ?: Clock.System.now()

    if (featuredPosts.isEmpty()) {
      return rssRepository.postPosition(
        postId = postId,
        activeSourceIds = activeSourceIds,
        postsSortOrder = state.value.postsSortOrder,
        unreadOnly = unreadOnly,
        after = postsAfter,
        postsUpperBound = lastRefreshedAt,
      ) ?: index
    }

    val featuredPostIndex = featuredPosts.indexOfFirst { it.resolvedPost.id == postId }
    if (featuredPostIndex != -1) {
      return featuredPostIndex
    }

    val featuredPostsAfter = lastRefreshedAt.minus(24.hours)

    if (unreadOnly == true) {
      val featuredWithSelectedPost =
        rssRepository.featuredPostsBlocking(
          activeSourceIds = activeSourceIds,
          postsSortOrder = state.value.postsSortOrder,
          unreadOnly = unreadOnly,
          after = postsAfter,
          featuredPostsAfter = featuredPostsAfter,
          postsUpperBound = lastRefreshedAt,
          sessionPostIds = listOf(postId),
        )
      val previousFeaturedIndex = featuredWithSelectedPost.indexOfFirst { it.id == postId }
      if (previousFeaturedIndex != -1) {
        return previousFeaturedIndex.coerceAtMost(featuredPosts.lastIndex)
      }
    }

    val position =
      rssRepository.nonFeaturedPostPosition(
        postId = postId,
        activeSourceIds = activeSourceIds,
        postsSortOrder = state.value.postsSortOrder,
        unreadOnly = unreadOnly,
        after = postsAfter,
        featuredPostsAfter = featuredPostsAfter,
        postsUpperBound = lastRefreshedAt,
      )

    return if (position != null) {
      position + featuredPosts.size
    } else {
      index
    }
  }

  private fun showPostsSortFilter(show: Boolean) {
    state.update { it.copy(showPostsSortFilter = show) }
  }

  private fun onPostsSortFilterApplied(event: HomeEvent.OnPostsSortFilterApplied) {
    viewModelScope.launch {
      settingsRepository.updatePostsType(event.postsType)
      settingsRepository.updatePostsSortOrder(event.postsSortOrder)
    }
  }

  private fun init() {
    val activeSourceFlow = observableActiveSource.activeSource
    val postsTypeFlow = settingsRepository.postsType
    val allPostsPagingData = allPostsPager.allPostsPagingData().cachedIn(viewModelScope)
    val feedPostsPagingData = allPostsPager.nonFeaturedPostsPagingData().cachedIn(viewModelScope)

    // Hot flow: keeps a single shared DB subscription so UI re-collection and the
    // `.first()` reads in calculateHomeIndex/onScreenStopped don't re-run the query.
    // Started lazily (not WhileSubscribed) so the value stays fresh while the reader
    // screen is on top of home; calculateHomeIndex depends on it to map indices.
    val featuredPosts =
      combine(allPostsPager.featuredPosts(), settingsRepository.showFeaturedSection) {
          featuredPosts,
          showFeaturedSection ->
          if (showFeaturedSection) {
            featuredPosts
          } else {
            persistentListOf()
          }
        }
        .stateIn(
          scope = viewModelScope,
          started = SharingStarted.Lazily,
          initialValue = persistentListOf(),
        )

    state.update {
      it.copy(
        allPosts = allPostsPagingData,
        feedPosts = feedPostsPagingData,
        featuredPosts = featuredPosts,
      )
    }

    combine(
        observableSelectedPost.selectedPost,
        selectedPostResolveTrigger,
        isFeaturedSectionVisible,
      ) { selectedPost, _, _ ->
        selectedPost
      }
      .mapLatest { selectedPost ->
        val postId = selectedPost?.id
        val homeIndex =
          if (postId != null) {
            calculateHomeIndex(postId, selectedPost.index)
          } else {
            selectedPost?.index ?: 0
          }
        ActivePostPosition(index = homeIndex, scrollOffset = selectedPost?.scrollOffset)
      }
      .distinctUntilChanged()
      .onEach { position ->
        state.update {
          it.copy(activePostIndex = position.index, activePostScrollOffset = position.scrollOffset)
        }
      }
      .launchIn(viewModelScope)

    syncCoordinator.syncState
      .onEach { syncState -> state.update { it.copy(syncState = syncState) } }
      .launchIn(viewModelScope)

    rssRepository
      .hasFeeds()
      .distinctUntilChanged()
      .onEach { hasFeeds -> state.update { it.copy(hasFeeds = hasFeeds) } }
      .launchIn(viewModelScope)

    settingsRepository.confirmMarkAllAsRead
      .distinctUntilChanged()
      .onEach { confirm -> state.update { it.copy(confirmMarkAllAsRead = confirm) } }
      .launchIn(viewModelScope)

    combine(
        combine(
          combine(activeSourceFlow, postsTypeFlow, settingsRepository.postsSortOrder, ::Triple),
          combine(
            settingsRepository.homeViewMode,
            settingsRepository.showFeaturedSection,
            settingsRepository.themeVariant,
            settingsRepository.showPinnedSources,
            settingsRepository.markAsReadOn,
            ::HomeSelectionFiltersCombined,
          ),
        ) { group1, combined ->
          HomeSelectionFilters(
            activeSource = group1.first,
            postsType = group1.second,
            postsSortOrder = group1.third,
            homeViewMode = combined.homeViewMode,
            showFeaturedSection = combined.showFeaturedSection,
            themeVariant = combined.themeVariant,
            showPinnedSources = combined.showPinnedSources,
            markAsReadOn = combined.markAsReadOn,
          )
        },
        combine(
          allPostsPager.hasUnreadPosts,
          allPostsPager.unreadSinceLastSync,
          refreshPolicy.lastRefreshedAtFlow,
          ::HomeUnreadStatus,
        ),
      ) { selectionFilters, unreadStatus ->
        state.update {
          it.copy(
            activeSource = selectionFilters.activeSource,
            postsType = selectionFilters.postsType,
            postsSortOrder = selectionFilters.postsSortOrder,
            homeViewMode = selectionFilters.homeViewMode,
            showFeaturedSection = selectionFilters.showFeaturedSection,
            themeVariant = selectionFilters.themeVariant,
            showPinnedSources = selectionFilters.showPinnedSources,
            markAsReadOn = selectionFilters.markAsReadOn,
            hasUnreadPosts = unreadStatus.hasUnreadPosts,
            unreadSinceLastSync = unreadStatus.unreadSinceLastSync,
            lastRefreshedAt = unreadStatus.lastRefreshedAt,
          )
        }
      }
      .launchIn(viewModelScope)
  }

  private fun updatePrevActiveSource(event: HomeEvent.UpdatePrevActiveSource) {
    state.update { it.copy(prevActiveSource = event.source) }
  }

  private fun loadNewArticles() {
    viewModelScope.launch {
      state.update { it.copy(unreadSinceLastSync = null) }
      refreshPolicy.updateLastRefreshedAt()
      _effects.emit(HomeEffect.RequestInAppRating)
    }
  }

  private fun updateVisibleItemIndex(index: Int, postId: String?) {
    observableSelectedPost.updateSelectedPost(index, postId)
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

  private fun markPostsAsRead(source: Source?) {
    viewModelScope.launch {
      val postsAfter = postsThresholdTime(state.value.postsType)

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
    val lastRefreshedAt = state.value.lastRefreshedAt
    return if (lastRefreshedAt != null) {
      PostsFilterUtils.postsThresholdTime(postsType, lastRefreshedAt)
    } else {
      when (postsType) {
        PostsType.ALL,
        PostsType.UNREAD -> Instant.DISTANT_PAST
        else -> Clock.System.now().minus(24.hours)
      }
    }
  }

  private fun feedsSheetStateChanged(feedsSheetState: SheetValue) {
    state.update { it.copy(feedsSheetState = feedsSheetState) }
  }

  private fun onHomeSelected() {
    viewModelScope.launch {
      observableActiveSource.clearSelection()
      observableSelectedPost.clear()
    }
  }

  private fun refreshContent() {
    if (state.value.unreadSinceLastSync?.hasNewArticles == true) {
      loadNewArticles()
    }
    when (val selectedSource = state.value.activeSource) {
      is FeedGroup -> syncCoordinator.triggerPull(selectedSource.feedIds)
      is Feed -> syncCoordinator.triggerPull(selectedSource.id)
      else -> syncCoordinator.triggerPull()
    }
  }
}

private data class ActivePostPosition(val index: Int, val scrollOffset: Int?)

private data class HomeSelectionFiltersCombined(
  val homeViewMode: HomeViewMode,
  val showFeaturedSection: Boolean,
  val themeVariant: ThemeVariant,
  val showPinnedSources: Boolean,
  val markAsReadOn: MarkAsReadOn,
)

private data class HomeSelectionFilters(
  val activeSource: Source?,
  val postsType: PostsType,
  val postsSortOrder: PostsSortOrder,
  val homeViewMode: HomeViewMode,
  val showFeaturedSection: Boolean,
  val themeVariant: ThemeVariant,
  val showPinnedSources: Boolean,
  val markAsReadOn: MarkAsReadOn,
)

private data class HomeUnreadStatus(
  val hasUnreadPosts: Boolean,
  val unreadSinceLastSync: UnreadSinceLastSync?,
  val lastRefreshedAt: LocalDateTime?,
)
