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
import androidx.paging.filter
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
import dev.sasikanth.rss.reader.feeds.ui.PinnedFeedsListItemType
import dev.sasikanth.rss.reader.home.ui.PostsType
import dev.sasikanth.rss.reader.repository.ObservableSelectedFeed
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.repository.SettingsRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
import dev.sasikanth.rss.reader.utils.getLast24HourStart
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
import kotlinx.coroutines.flow.emptyFlow
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

  internal val pinnedSectionExpanded
    get() = presenterInstance.pinnedSectionExpanded

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

    var pinnedSectionExpanded by mutableStateOf(true)
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
        is FeedsEvent.OnFeedInfoClick -> {
          // no-op
        }
        FeedsEvent.TogglePinnedSection -> onTogglePinnedSection()
      }
    }

    private fun onTogglePinnedSection() {
      pinnedSectionExpanded = !pinnedSectionExpanded
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
      val searchQueryFlow =
        snapshotFlow { searchQuery }.debounce(500.milliseconds).distinctUntilChangedBy { it.text }
      val pinnedSectionExpandedFlow = snapshotFlow { pinnedSectionExpanded }

      combine(searchQueryFlow, settingsRepository.postsType, pinnedSectionExpandedFlow) {
          searchQuery,
          postsType,
          pinnedSectionExpanded ->
          Triple(searchQuery, postsType, pinnedSectionExpanded)
        }
        .onEach { (searchQuery, postsType, pinnedSectionExpanded) ->
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

          val pinnedFeeds =
            if (searchQueryText.isBlank()) {
              pinnedFeedsPager(postsAfter = postsAfter)
            } else {
              emptyFlow()
            }

          val feedsWithHeader =
            addFeedsHeader(feeds = feeds, searchQuery = searchQuery).cachedIn(coroutineScope)

          val pinnedFeedsWithHeader =
            addPinnedFeedsHeader(
                feeds = pinnedFeeds,
                isPinnedSectionExpanded = pinnedSectionExpanded
              )
              .cachedIn(coroutineScope)

          _state.update {
            it.copy(feedsInExpandedView = feedsWithHeader, pinnedFeeds = pinnedFeedsWithHeader)
          }
        }
        .launchIn(coroutineScope)
    }

    private fun observeFeedsForCollapsedSheet() {
      val feeds =
        settingsRepository.postsType.distinctUntilChanged().flatMapLatest { postsType ->
          val postsAfter = postsAfterInstantFromPostsType(postsType)

          feedsPager(postsAfter).cachedIn(coroutineScope)
        }

      observableSelectedFeed.selectedFeed
        .distinctUntilChanged()
        .onEach { selectedFeed ->
          _state.update { it.copy(feedsInBottomBar = feeds, selectedFeed = selectedFeed) }
        }
        .launchIn(coroutineScope)
    }

    private fun pinnedFeedsPager(postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.pinnedFeeds(postsAfter = postsAfter)
        }
        .flow

    private fun feedsSearchResultsPager(transformedSearchQuery: String, postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.searchFeed(searchQuery = transformedSearchQuery, postsAfter = postsAfter)
        }
        .flow

    private fun feedsPager(postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.allFeeds(postsAfter = postsAfter)
        }
        .flow

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
        PostsType.LAST_24_HOURS -> {
          getLast24HourStart()
        }
      }

    private fun addPinnedFeedsHeader(
      feeds: Flow<PagingData<Feed>>,
      isPinnedSectionExpanded: Boolean,
    ): Flow<PagingData<PinnedFeedsListItemType>> {
      return feeds.mapLatest {
        it
          .map { feed -> PinnedFeedsListItemType.PinnedFeedListItem(feed = feed) }
          .filter { isPinnedSectionExpanded }
          .insertSeparators { before, _ ->
            when {
              before == null -> {
                PinnedFeedsListItemType.PinnedFeedsHeader(isExpanded = isPinnedSectionExpanded)
              }
              else -> {
                null
              }
            }
          }
      }
    }

    private fun addFeedsHeader(
      feeds: Flow<PagingData<Feed>>,
      searchQuery: TextFieldValue,
    ): Flow<PagingData<FeedsListItemType>> {
      return feeds.mapLatest {
        it
          .map { feed -> FeedsListItemType.FeedListItem(feed = feed) }
          .insertSeparators { before, _ ->
            when {
              before == null && searchQuery.text.length < MINIMUM_REQUIRED_SEARCH_CHARACTERS -> {
                val feedsCount = rssRepository.feedsCount()

                FeedsListItemType.AllFeedsHeader(feedsCount = feedsCount)
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
