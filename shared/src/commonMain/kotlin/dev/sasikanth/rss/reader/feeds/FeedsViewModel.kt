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
package dev.sasikanth.rss.reader.feeds

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.map
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.data.refreshpolicy.RefreshPolicy
import dev.sasikanth.rss.reader.data.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.data.repository.ObservableActiveSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.utils.PostsFilterUtils
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants.MINIMUM_REQUIRED_SEARCH_CHARACTERS
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.tatarka.inject.annotations.Inject

@Inject
class FeedsViewModel(
  private val dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  private val refreshPolicy: RefreshPolicy,
  private val billingHandler: BillingHandler,
) : ViewModel() {

  var searchQuery by mutableStateOf(TextFieldValue())
    private set

  private val _state = MutableStateFlow(FeedsState.DEFAULT)
  val state: StateFlow<FeedsState>
    get() = _state

  init {
    init()
  }

  fun dispatch(event: FeedsEvent) {
    when (event) {
      is FeedsEvent.OnDeleteFeed -> onDeleteFeed(event.feed)
      is FeedsEvent.OnToggleFeedSelection -> onToggleSourceSelection(event.source)
      is FeedsEvent.OnFeedNameUpdated -> onFeedNameUpdated(event.newFeedName, event.feedId)
      is FeedsEvent.OnFeedPinClicked -> onFeedPinClicked(event.feed)
      FeedsEvent.ClearSearchQuery -> clearSearchQuery()
      is FeedsEvent.SearchQueryChanged -> onSearchQueryChanged(event.searchQuery)
      is FeedsEvent.OnSourceClick -> onSourceClicked(event.source)
      FeedsEvent.TogglePinnedSection -> onTogglePinnedSection()
      is FeedsEvent.OnFeedSortOrderChanged -> onFeedSortOrderChanged(event.feedsOrderBy)
      is FeedsEvent.OnHomeSelected -> onHomeSelected()
      FeedsEvent.CancelSourcesSelection -> onCancelSourcesSelection()
      FeedsEvent.DeleteSelectedSourcesClicked -> onDeleteSelectedSourcesClicked()
      FeedsEvent.PinSelectedSources -> onPinSelectedSources()
      FeedsEvent.UnPinSelectedSources -> onUnpinSelectedSources()
      is FeedsEvent.OnCreateGroup -> onCreateGroup(event.name)
      is FeedsEvent.OnGroupsSelected -> onGroupsSelected(event.groupIds)
      FeedsEvent.OnNewFeedClicked -> {
        viewModelScope.launch {
          val feedsCount = state.value.numberOfFeeds
          val isSubscribed = billingHandler.isSubscribed()
          if (!isSubscribed && feedsCount >= 10) {
            _state.update { it.copy(openPaywall = true) }
            return@launch
          }

          _state.update { it.copy(openAddFeedScreen = true) }
        }
      }
      FeedsEvent.DeleteSelectedSources -> deleteSelectedSources()
      FeedsEvent.OnAddToGroupClicked -> onAddToGroupClicked()
      FeedsEvent.DismissDeleteConfirmation -> dismissDeleteConfirmation()
      is FeedsEvent.OnPinnedSourcePositionChanged ->
        onPinnedSourcePositionChanged(event.newSourcesList)
      is FeedsEvent.MarkOpenPaywallDone -> {
        _state.update { it.copy(openPaywall = false) }
      }
      is FeedsEvent.MarkOpenAddFeedDone -> {
        _state.update { it.copy(openAddFeedScreen = false) }
      }
      is FeedsEvent.MarkOpenGroupSelectionDone -> {
        _state.update { it.copy(openGroupSelection = null) }
      }
    }
  }

  private fun onAddToGroupClicked() {
    viewModelScope.launch {
      val selectedSources = _state.value.selectedSources
      val groupIds =
        if (selectedSources.size == 1) {
          rssRepository.groupIdsForFeed(selectedSources.first().id).toSet()
        } else {
          emptySet()
        }

      _state.update { it.copy(openGroupSelection = groupIds) }
    }
  }

  private fun init() {
    observeSources()
    observePreferences()
    observeShowUnreadCountPreference()
    observeSearchQuery()
  }

  private fun onPinnedSourcePositionChanged(newSourcesList: List<Source>) {
    viewModelScope.launch {
      _state.update { it.copy(pinnedSources = newSourcesList) }
      rssRepository.updatedSourcePinnedPosition(_state.value.pinnedSources)
    }
  }

  private fun dismissDeleteConfirmation() {
    _state.update { it.copy(showDeleteConfirmation = false) }
  }

  private fun deleteSelectedSources() {
    viewModelScope
      .launch { rssRepository.markSourcesAsDeleted(_state.value.selectedSources) }
      .invokeOnCompletion {
        if (_state.value.selectedSources.any { it.id == _state.value.activeSource?.id }) {
          observableActiveSource.clearSelection()
        }
        dispatch(FeedsEvent.CancelSourcesSelection)
      }
  }

  private fun onGroupsSelected(groupIds: Set<String>) {
    viewModelScope.launch {
      rssRepository.addFeedIdsToGroups(
        groupIds = groupIds,
        feedIds = _state.value.selectedSources.map { it.id },
      )
      dispatch(FeedsEvent.CancelSourcesSelection)
    }
  }

  private fun onCreateGroup(name: String) {
    viewModelScope.launch { rssRepository.createGroup(name) }
  }

  private fun onSourceClicked(source: Source) {
    viewModelScope.launch {
      if (_state.value.activeSource?.id != source.id) {
        observableActiveSource.changeActiveSource(source)
      }
    }
  }

  private fun onUnpinSelectedSources() {
    viewModelScope
      .launch { rssRepository.unpinSources(_state.value.selectedSources) }
      .invokeOnCompletion { dispatch(FeedsEvent.CancelSourcesSelection) }
  }

  private fun onPinSelectedSources() {
    viewModelScope
      .launch { rssRepository.pinSources(_state.value.selectedSources) }
      .invokeOnCompletion { dispatch(FeedsEvent.CancelSourcesSelection) }
  }

  private fun onDeleteSelectedSourcesClicked() {
    _state.update { it.copy(showDeleteConfirmation = true) }
  }

  private fun onCancelSourcesSelection() {
    _state.update { it.copy(selectedSources = emptySet()) }
  }

  private fun onHomeSelected() {
    viewModelScope.launch { observableActiveSource.clearSelection() }
  }

  private fun onFeedSortOrderChanged(feedsOrderBy: FeedsOrderBy) {
    viewModelScope.launch {
      withContext(dispatchersProvider.io) { settingsRepository.updateFeedsSortOrder(feedsOrderBy) }
    }
  }

  private fun onTogglePinnedSection() {
    _state.update { it.copy(isPinnedSectionExpanded = !_state.value.isPinnedSectionExpanded) }
  }

  private fun onSearchQueryChanged(searchQuery: TextFieldValue) {
    this.searchQuery = searchQuery
  }

  private fun clearSearchQuery() {
    searchQuery = TextFieldValue()
  }

  private fun onFeedPinClicked(feed: Feed) {
    viewModelScope.launch { rssRepository.toggleFeedPinStatus(feed) }
  }

  private fun onFeedNameUpdated(newFeedName: String, feedId: String) {
    viewModelScope.launch { rssRepository.updateFeedName(newFeedName, feedId) }
  }

  private fun onDeleteFeed(feed: Feed) {
    viewModelScope.launch {
      rssRepository.removeFeed(feed.id)
      if (_state.value.activeSource?.id == feed.id) {
        observableActiveSource.clearSelection()
      }
    }
  }

  private fun onToggleSourceSelection(source: Source) {
    _state.update {
      val selectedFeeds = _state.value.selectedSources
      if (selectedFeeds.any { selectedFeed -> selectedFeed.id == source.id }) {
        it.copy(selectedSources = selectedFeeds.filterNot { feed -> feed.id == source.id }.toSet())
      } else {
        it.copy(selectedSources = selectedFeeds + setOf(source))
      }
    }
  }

  @OptIn(FlowPreview::class)
  private fun observeSearchQuery() {
    val searchQueryFlow =
      snapshotFlow { searchQuery }.debounce(500.milliseconds).distinctUntilChangedBy { it.text }

    combine(searchQueryFlow, settingsRepository.postsType, refreshPolicy.lastRefreshedAtFlow) {
        searchQuery,
        postsType,
        dateTime ->
        Triple(searchQuery, postsType, dateTime)
      }
      .onEach { (searchQuery, postsType, dateTime) ->
        val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime)
        val searchResults =
          if (searchQuery.text.length >= MINIMUM_REQUIRED_SEARCH_CHARACTERS) {
            feedsSearchResultsPager(
              transformedSearchQuery = searchQuery.text,
              postsAfter = postsAfter,
            )
          } else {
            flowOf(PagingData.empty())
          }

        _state.update { it.copy(feedsSearchResults = searchResults) }
      }
      .launchIn(viewModelScope)
  }

  private fun observePreferences() {
    settingsRepository.feedsSortOrder
      .onEach { feedsSortOrder -> _state.update { it.copy(feedsSortOrder = feedsSortOrder) } }
      .launchIn(viewModelScope)
  }

  private fun observeSources() {
    val activeSourceFlow = observableActiveSource.activeSource
    val postsTypeFlow = settingsRepository.postsType

    combine(activeSourceFlow, refreshPolicy.lastRefreshedAtFlow, postsTypeFlow) {
        activeSource,
        lastRefreshedAt,
        postsType ->
        Triple(activeSource, lastRefreshedAt, postsType)
      }
      .flatMapLatest { (activeSource, lastRefreshedAt, postsType) ->
        when {
          activeSource?.pinnedAt != null || activeSource == null -> {
            flowOf(activeSource)
          }
          else -> {
            val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, lastRefreshedAt)

            rssRepository.source(
              id = activeSource.id,
              lastSyncedAt = lastRefreshedAt.toInstant(TimeZone.currentSystemDefault()),
              postsAfter = postsAfter,
            )
          }
        }
      }
      .onEach { source -> _state.update { it.copy(activeSource = source) } }
      .launchIn(viewModelScope)

    combine(rssRepository.numberOfFeeds(), rssRepository.numberOfFeedGroups()) {
        numberOfFeeds,
        numberOfFeedGroups ->
        numberOfFeeds to numberOfFeedGroups
      }
      .onEach { (numberOfFeeds, numberOfFeedGroups) ->
        _state.update {
          it.copy(
            numberOfFeeds = numberOfFeeds.toInt(),
            numberOfFeedGroups = numberOfFeedGroups.toInt(),
          )
        }
      }
      .launchIn(viewModelScope)

    val pinnedSourcesFlow =
      combine(settingsRepository.postsType, refreshPolicy.lastRefreshedAtFlow) { postsType, dateTime
          ->
          Pair(postsType, dateTime)
        }
        .flatMapLatest { (postsType, dateTime) ->
          val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime)
          rssRepository.pinnedSources(
            postsAfter = postsAfter,
            lastSyncedAt = dateTime.toInstant(TimeZone.currentSystemDefault()),
          )
        }

    val allSourcesFlow =
      combine(
          settingsRepository.postsType,
          settingsRepository.feedsSortOrder,
          refreshPolicy.lastRefreshedAtFlow,
        ) { postsType, feedsSortOrder, dateTime ->
          Triple(postsType, feedsSortOrder, dateTime)
        }
        .map { (postsType, feedsSortOrder, dateTime) ->
          val postsAfter = PostsFilterUtils.postsThresholdTime(postsType, dateTime)
          val sources =
            sources(
              postsAfter = postsAfter,
              lastSyncedAt = dateTime,
              feedsSortOrder = feedsSortOrder,
            )

          addSourcesSeparator(sources).cachedIn(viewModelScope)
        }

    combine(pinnedSourcesFlow, allSourcesFlow) { pinnedSources, allSources ->
        Pair(pinnedSources, allSources)
      }
      .onEach { (pinnedSources, allSources) ->
        _state.update { it.copy(pinnedSources = pinnedSources, sources = allSources) }
      }
      .launchIn(viewModelScope)
  }

  private fun feedsSearchResultsPager(transformedSearchQuery: String, postsAfter: Instant) =
    createPager(config = createPagingConfig(pageSize = 20)) {
        rssRepository.searchFeed(searchQuery = transformedSearchQuery, postsAfter = postsAfter)
      }
      .flow

  private fun sources(
    postsAfter: Instant,
    lastSyncedAt: LocalDateTime,
    feedsSortOrder: FeedsOrderBy,
  ) =
    createPager(config = createPagingConfig(pageSize = 20)) {
        rssRepository.sources(
          postsAfter = postsAfter,
          lastSyncedAt = lastSyncedAt.toInstant(TimeZone.currentSystemDefault()),
          orderBy = feedsSortOrder,
        )
      }
      .flow

  private fun observeShowUnreadCountPreference() {
    settingsRepository.showUnreadPostsCount
      .onEach { value -> _state.update { it.copy(canShowUnreadPostsCount = value) } }
      .launchIn(viewModelScope)
  }

  private fun addSourcesSeparator(
    sources: Flow<PagingData<Source>>
  ): Flow<PagingData<SourceListItem>> {
    return sources.mapLatest {
      it
        .map { source -> SourceListItem.SourceItem(source) }
        .insertSeparators { before, after ->
          when {
            before?.source?.sourceType == SourceType.FeedGroup &&
              after?.source?.sourceType == SourceType.Feed -> {
              SourceListItem.Separator
            }
            else -> {
              null
            }
          }
        }
    }
  }
}
