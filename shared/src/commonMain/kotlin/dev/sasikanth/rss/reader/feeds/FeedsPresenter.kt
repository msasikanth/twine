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
package dev.sasikanth.rss.reader.feeds

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.paging.PagingData
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.insertSeparators
import app.cash.paging.map
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.feeds.ui.FeedsListItemType
import dev.sasikanth.rss.reader.home.ui.PostsType
import dev.sasikanth.rss.reader.repository.ObservableSelectedFeed
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
import dev.sasikanth.rss.reader.utils.getTodayStartInstant
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FeedsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableSelectedFeed: ObservableSelectedFeed,
  @Assisted componentContext: ComponentContext,
  @Assisted private val openFeedInfo: (String) -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        settingsRepository = settingsRepository,
        observableSelectedFeed = observableSelectedFeed
      )
    }

  internal val state: StateFlow<FeedsState> = presenterInstance.state
  internal val effects = presenterInstance.effects.asSharedFlow()
  internal val searchQuery
    get() = presenterInstance.searchQuery

  init {
    lifecycle.doOnCreate { presenterInstance.dispatch(FeedsEvent.Init) }
  }

  fun dispatch(event: FeedsEvent) {
    when (event) {
      is FeedsEvent.OnFeedInfoClick -> {
        openFeedInfo(event.feedLink)
      }
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val settingsRepository: SettingsRepository,
    private val observableSelectedFeed: ObservableSelectedFeed
  ) : InstanceKeeper.Instance {

    var searchQuery by mutableStateOf(TextFieldValue())
      private set

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(FeedsState.DEFAULT)
    val state: StateFlow<FeedsState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedsState.DEFAULT
      )

    val effects = MutableSharedFlow<FeedsEffect>(extraBufferCapacity = 10)

    fun dispatch(event: FeedsEvent) {
      when (event) {
        FeedsEvent.Init -> init()
        FeedsEvent.OnGoBackClicked -> onGoBackClicked()
        is FeedsEvent.OnDeleteFeed -> onDeleteFeed(event.feed)
        is FeedsEvent.OnFeedSelected -> onFeedSelected(event.feed)
        is FeedsEvent.OnFeedNameUpdated -> onFeedNameUpdated(event.newFeedName, event.feedLink)
        is FeedsEvent.OnFeedPinClicked -> onFeedPinClicked(event.feed)
        FeedsEvent.ClearSearchQuery -> clearSearchQuery()
        is FeedsEvent.SearchQueryChanged -> onSearchQueryChanged(event.searchQuery)
        is FeedsEvent.MarkPostsInFeedAsReadClicked -> markPostsInFeedAsReadClicked(event.feedLink)
        is FeedsEvent.OnFeedInfoClick -> {
          // no-op
        }
      }
    }

    private fun markPostsInFeedAsReadClicked(feedLink: String) {
      coroutineScope.launch { rssRepository.markPostsInFeedAsRead(feedLink) }
    }

    private fun onSearchQueryChanged(searchQuery: TextFieldValue) {
      this.searchQuery = searchQuery
    }

    private fun clearSearchQuery() {
      searchQuery = TextFieldValue()
    }

    private fun onFeedPinClicked(feed: Feed) {
      coroutineScope.launch { rssRepository.toggleFeedPinStatus(feed) }
    }

    private fun onFeedNameUpdated(newFeedName: String, feedLink: String) {
      coroutineScope.launch { rssRepository.updateFeedName(newFeedName, feedLink) }
    }

    private fun onDeleteFeed(feed: Feed) {
      coroutineScope.launch {
        rssRepository.removeFeed(feed.link)
        if (_state.value.selectedFeed?.link == feed.link) {
          observableSelectedFeed.clearSelection()
        }
      }
    }

    private fun onFeedSelected(feed: Feed) {
      coroutineScope.launch {
        if (_state.value.selectedFeed?.link != feed.link) {
          observableSelectedFeed.selectFeed(feed)
        }

        effects.emit(FeedsEffect.SelectedFeedChanged)
        effects.emit(FeedsEffect.MinimizeSheet)
      }
    }

    private fun onGoBackClicked() {
      coroutineScope.launch { effects.emit(FeedsEffect.MinimizeSheet) }
    }

    private fun init() {
      observeNumberOfPinnedFeeds()
      observeShowUnreadCountPreference()
      observeFeedsForCollapsedSheet()
      observeFeedsForExpandedSheet()
    }

    private fun observeNumberOfPinnedFeeds() {
      rssRepository.numberOfPinnedFeeds().distinctUntilChanged().onEach { numberOfPinnedFeeds ->
        _state.update { it.copy(numberOfPinnedFeeds = numberOfPinnedFeeds) }
      }
    }

    @OptIn(FlowPreview::class)
    private fun observeFeedsForExpandedSheet() {
      val searchQueryFlow = snapshotFlow { searchQuery }.debounce(500.milliseconds)
      searchQueryFlow
        .distinctUntilChangedBy { it.text }
        .combine(settingsRepository.postsType) { searchQuery, postsType ->
          searchQuery to postsType
        }
        .onEach { (searchQuery, postsType) ->
          val searchQueryText = searchQuery.text
          val postsAfter = postsAfterInstantFromPostsType(postsType)
          val feeds =
            if (searchQueryText.length >= MINIMUM_REQUIRED_SEARCH_CHARACTERS) {
              feedsSearchResultsPager(
                transformedSearchQuery = searchQueryText,
                postsAfter = postsAfter
              )
            } else {
              feedsPager(postsAfter = postsAfter)
            }

          _state.update { it.copy(feedsInExpandedMode = addFeedsHeaders(feeds)) }
        }
        .launchIn(coroutineScope)
    }

    private fun observeFeedsForCollapsedSheet() {
      val feeds =
        settingsRepository.postsType.distinctUntilChanged().flatMapLatest { postsType ->
          val postsAfter = postsAfterInstantFromPostsType(postsType)

          feedsPager(postsAfter)
        }

      observableSelectedFeed.selectedFeed
        .distinctUntilChanged()
        .onEach { selectedFeed ->
          _state.update { it.copy(feeds = feeds, selectedFeed = selectedFeed) }
        }
        .launchIn(coroutineScope)
    }

    private fun feedsSearchResultsPager(transformedSearchQuery: String, postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.searchFeed(searchQuery = transformedSearchQuery, postsAfter = postsAfter)
        }
        .flow
        .cachedIn(coroutineScope)

    private fun feedsPager(postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.allFeeds(postsAfter = postsAfter)
        }
        .flow
        .cachedIn(coroutineScope)

    private fun observeShowUnreadCountPreference() {
      settingsRepository.showUnreadPostsCount
        .onEach { value -> _state.update { it.copy(canShowUnreadPostsCount = value) } }
        .launchIn(coroutineScope)
    }

    private fun postsAfterInstantFromPostsType(postsType: PostsType) =
      when (postsType) {
        PostsType.ALL,
        PostsType.UNREAD -> Instant.DISTANT_PAST
        PostsType.TODAY -> {
          getTodayStartInstant()
        }
      }

    private fun addFeedsHeaders(
      feeds: Flow<PagingData<Feed>>
    ): Flow<PagingData<FeedsListItemType>> {
      return feeds.mapLatest {
        it
          .map { feed -> FeedsListItemType.FeedListItem(feed = feed) }
          .insertSeparators { before, after ->
            when {
              before?.feed?.pinnedAt == null && after?.feed?.pinnedAt != null -> {
                FeedsListItemType.PinnedFeedsHeader
              }
              (before?.feed?.pinnedAt != null || before == null) &&
                after != null &&
                after.feed.pinnedAt == null -> {
                FeedsListItemType.AllFeedsHeader
              }
              else -> {
                null
              }
            }
          }
      }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
