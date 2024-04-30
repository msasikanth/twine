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
import androidx.paging.insertSeparators
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import app.cash.paging.map
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.core.model.local.Source
import dev.sasikanth.rss.reader.core.model.local.SourceType
import dev.sasikanth.rss.reader.feeds.ui.FeedsViewMode
import dev.sasikanth.rss.reader.home.ui.PostsType
import dev.sasikanth.rss.reader.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.repository.ObservableActiveSource
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
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class FeedsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val observableActiveSource: ObservableActiveSource,
  @Assisted componentContext: ComponentContext,
  @Assisted private val openGroupSelectionSheet: () -> Unit,
  @Assisted private val openFeedInfoSheet: (feedId: String) -> Unit,
  @Assisted private val openAddFeedScreen: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        rssRepository = rssRepository,
        settingsRepository = settingsRepository,
        observableActiveSource = observableActiveSource
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
      is FeedsEvent.OnAddToGroupClicked -> {
        openGroupSelectionSheet()
      }
      is FeedsEvent.OnEditSourceClicked -> {
        when (val source = event.source) {
          is Feed -> openFeedInfoSheet(source.id)
          is FeedGroup -> {
            // TODO: Open edit feed group screen
          }
          else -> {
            throw IllegalArgumentException("Unknown source: $source")
          }
        }
      }
      is FeedsEvent.OnNewFeedClicked -> openAddFeedScreen()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    private val dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
    private val settingsRepository: SettingsRepository,
    private val observableActiveSource: ObservableActiveSource
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
        is FeedsEvent.OnToggleFeedSelection -> onToggleSourceSelection(event.source)
        is FeedsEvent.OnFeedNameUpdated -> onFeedNameUpdated(event.newFeedName, event.feedId)
        is FeedsEvent.OnFeedPinClicked -> onFeedPinClicked(event.feed)
        FeedsEvent.ClearSearchQuery -> clearSearchQuery()
        is FeedsEvent.SearchQueryChanged -> onSearchQueryChanged(event.searchQuery)
        is FeedsEvent.OnSourceClick -> onSourceClicked(event.source)
        FeedsEvent.TogglePinnedSection -> onTogglePinnedSection()
        is FeedsEvent.OnFeedSortOrderChanged -> onFeedSortOrderChanged(event.feedsOrderBy)
        FeedsEvent.OnChangeFeedsViewModeClick -> onChangeFeedsViewModeClick()
        is FeedsEvent.OnHomeSelected -> onHomeSelected()
        FeedsEvent.CancelSourcesSelection -> onCancelSourcesSelection()
        FeedsEvent.DeleteSelectedSources -> onDeleteSelectedSources()
        FeedsEvent.PinSelectedSources -> onPinSelectedSources()
        FeedsEvent.UnPinSelectedSources -> onUnpinSelectedSources()
        is FeedsEvent.OnCreateGroup -> onCreateGroup(event.name)
        is FeedsEvent.OnGroupsSelected -> onGroupsSelected(event.groupIds)
        FeedsEvent.OnAddToGroupClicked -> {
          // no-op
        }
        is FeedsEvent.OnEditSourceClicked -> {
          // no-op
        }
        FeedsEvent.OnNewFeedClicked -> {
          // no-op
        }
      }
    }

    private fun onGroupsSelected(groupIds: Set<String>) {
      coroutineScope.launch {
        rssRepository.addFeedIdsToGroups(
          groupIds = groupIds,
          feedIds = _state.value.selectedSources.map { it.id }
        )
        dispatch(FeedsEvent.CancelSourcesSelection)
      }
    }

    private fun onCreateGroup(name: String) {
      coroutineScope.launch { rssRepository.createGroup(name) }
    }

    private fun onSourceClicked(source: Source) {
      coroutineScope.launch {
        if (_state.value.activeSource?.id != source.id) {
          observableActiveSource.changeActiveSource(source)
        }

        effects.emit(FeedsEffect.SelectedFeedChanged)
        effects.emit(FeedsEffect.MinimizeSheet)
      }
    }

    private fun onUnpinSelectedSources() {
      coroutineScope
        .launch { rssRepository.unpinSources(_state.value.selectedSources) }
        .invokeOnCompletion { dispatch(FeedsEvent.CancelSourcesSelection) }
    }

    private fun onPinSelectedSources() {
      coroutineScope
        .launch { rssRepository.pinSources(_state.value.selectedSources) }
        .invokeOnCompletion { dispatch(FeedsEvent.CancelSourcesSelection) }
    }

    private fun onDeleteSelectedSources() {
      coroutineScope
        .launch { rssRepository.deleteSources(_state.value.selectedSources) }
        .invokeOnCompletion {
          if (_state.value.selectedSources.any { it.id == _state.value.activeSource?.id }) {
            observableActiveSource.clearSelection()
          }
          dispatch(FeedsEvent.CancelSourcesSelection)
        }
    }

    private fun onCancelSourcesSelection() {
      _state.update { it.copy(selectedSources = emptySet()) }
    }

    private fun onHomeSelected() {
      coroutineScope.launch { observableActiveSource.clearSelection() }
    }

    private fun onChangeFeedsViewModeClick() {
      val newFeedsViewMode =
        when (_state.value.feedsViewMode) {
          FeedsViewMode.Grid -> FeedsViewMode.List
          FeedsViewMode.List -> FeedsViewMode.Grid
        }

      coroutineScope.launch {
        withContext(dispatchersProvider.io) {
          settingsRepository.updateFeedsViewMode(newFeedsViewMode)
        }
      }
    }

    private fun onFeedSortOrderChanged(feedsOrderBy: FeedsOrderBy) {
      coroutineScope.launch {
        withContext(dispatchersProvider.io) {
          settingsRepository.updateFeedsSortOrder(feedsOrderBy)
        }
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
      coroutineScope.launch { rssRepository.toggleFeedPinStatus(feed) }
    }

    private fun onFeedNameUpdated(newFeedName: String, feedId: String) {
      coroutineScope.launch { rssRepository.updateFeedName(newFeedName, feedId) }
    }

    private fun onDeleteFeed(feed: Feed) {
      coroutineScope.launch {
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
          it.copy(
            selectedSources = selectedFeeds.filterNot { feed -> feed.id == source.id }.toSet()
          )
        } else {
          it.copy(selectedSources = selectedFeeds + setOf(source))
        }
      }
    }

    private fun onGoBackClicked() {
      coroutineScope.launch { effects.emit(FeedsEffect.MinimizeSheet) }
    }

    private fun init() {
      observeSources()
      observePreferences()
      observeShowUnreadCountPreference()
      observeSearchQuery()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
      val searchQueryFlow =
        snapshotFlow { searchQuery }.debounce(500.milliseconds).distinctUntilChangedBy { it.text }

      combine(searchQueryFlow, settingsRepository.postsType) { searchQuery, postsType ->
          Pair(searchQuery, postsType)
        }
        .onEach { (searchQuery, postsType) ->
          val postsAfter = postsAfterInstantFromPostsType(postsType)
          val searchResults =
            if (searchQuery.text.length >= MINIMUM_REQUIRED_SEARCH_CHARACTERS) {
              feedsSearchResultsPager(
                transformedSearchQuery = searchQuery.text,
                postsAfter = postsAfter
              )
            } else {
              flowOf(PagingData.empty())
            }

          _state.update { it.copy(feedsSearchResults = searchResults) }
        }
        .launchIn(coroutineScope)
    }

    private fun observePreferences() {
      settingsRepository.feedsViewMode
        .onEach { feedsViewMode -> _state.update { it.copy(feedsViewMode = feedsViewMode) } }
        .launchIn(coroutineScope)

      settingsRepository.feedsSortOrder
        .onEach { feedsSortOrder -> _state.update { it.copy(feedsSortOrder = feedsSortOrder) } }
        .launchIn(coroutineScope)
    }

    private fun observeSources() {
      observableActiveSource.activeSource
        .onEach { activeSource -> _state.update { it.copy(activeSource = activeSource) } }
        .launchIn(coroutineScope)

      combine(rssRepository.numberOfFeeds(), rssRepository.numberOfFeedGroups()) {
          numberOfFeeds,
          numberOfFeedGroups ->
          numberOfFeeds to numberOfFeedGroups
        }
        .onEach { (numberOfFeeds, numberOfFeedGroups) ->
          _state.update {
            it.copy(
              numberOfFeeds = numberOfFeeds.toInt(),
              numberOfFeedGroups = numberOfFeedGroups.toInt()
            )
          }
        }
        .launchIn(coroutineScope)

      combine(settingsRepository.postsType, settingsRepository.feedsSortOrder) {
          postsType,
          feedsSortOrder ->
          Pair(postsType, feedsSortOrder)
        }
        .onEach { (postsType, feedsSortOrder) ->
          val postsAfter = postsAfterInstantFromPostsType(postsType)

          val pinnedSources = pinnedSources(postsAfter = postsAfter).cachedIn(coroutineScope)
          val sources =
            sources(
              postsAfter = postsAfter,
              feedsSortOrder = feedsSortOrder,
            )
          val sourcesWithSeparator = addSourcesSeparator(sources)

          _state.update { it.copy(pinnedSources = pinnedSources, sources = sourcesWithSeparator) }
        }
        .launchIn(coroutineScope)
    }

    private fun pinnedSources(postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.pinnedSources(postsAfter = postsAfter)
        }
        .flow

    private fun feedsSearchResultsPager(transformedSearchQuery: String, postsAfter: Instant) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.searchFeed(searchQuery = transformedSearchQuery, postsAfter = postsAfter)
        }
        .flow

    private fun sources(postsAfter: Instant, feedsSortOrder: FeedsOrderBy) =
      createPager(config = createPagingConfig(pageSize = 20)) {
          rssRepository.sources(postsAfter = postsAfter, orderBy = feedsSortOrder)
        }
        .flow

    private fun observeShowUnreadCountPreference() {
      settingsRepository.showUnreadPostsCount
        .onEach { value -> _state.update { it.copy(canShowUnreadPostsCount = value) } }
        .launchIn(coroutineScope)
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

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
