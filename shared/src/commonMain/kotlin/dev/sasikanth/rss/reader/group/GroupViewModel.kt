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

package dev.sasikanth.rss.reader.group

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.app.Screen
import dev.sasikanth.rss.reader.core.model.local.Feed
import dev.sasikanth.rss.reader.data.repository.FeedsOrderBy
import dev.sasikanth.rss.reader.data.repository.RssRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class GroupViewModel(
  private val rssRepository: RssRepository,
  @Assisted savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val groupId = savedStateHandle.toRoute<Screen.FeedGroup>().groupId
  private val _state = MutableStateFlow(GroupState.DEFAULT)
  val state: StateFlow<GroupState>
    get() = _state

  var groupName by mutableStateOf(TextFieldValue())
    private set

  init {
    init()
  }

  fun dispatch(event: GroupEvent) {
    when (event) {
      is GroupEvent.OnGroupNameChanged -> onGroupNameChanged(event.name)
      is GroupEvent.OnGroupsSelected -> onGroupsSelected(event.groupIds)
      GroupEvent.OnUngroupClicked -> onUngroupClicked()
      is GroupEvent.OnFeedsSortOrderChanged -> onFeedsSortOrderChanged(event.feedsOrderBy)
      GroupEvent.OnCancelSelectionClicked -> onCancelSelectionClicked()
      is GroupEvent.OnFeedClicked -> onFeedClicked(event.feed)
    }
  }

  private fun init() {
    rssRepository
      .groupById(groupId)
      .onEach { group ->
        groupName = groupName.copy(text = group.name)
        _state.update { it.copy(group = group) }
      }
      .combine(state) { group, state -> Pair(group, state) }
      .distinctUntilChanged { old, new ->
        old.first.feedIds == new.first.feedIds && old.second.feedsOrderBy == new.second.feedsOrderBy
      }
      .onEach { (group, state) ->
        val feeds =
          createPager(config = createPagingConfig(pageSize = 20)) {
              rssRepository.feedsInGroup(feedIds = group.feedIds, orderBy = state.feedsOrderBy)
            }
            .flow
            .cachedIn(viewModelScope)

        _state.update { it.copy(feeds = feeds) }
      }
      .launchIn(viewModelScope)
  }

  private fun onFeedClicked(feed: Feed) {
    viewModelScope.launch {
      _state.update {
        val selectedFeeds = _state.value.selectedSources
        if (selectedFeeds.any { selectedFeed -> selectedFeed.id == feed.id }) {
          it.copy(selectedSources = selectedFeeds - setOf(feed))
        } else {
          it.copy(selectedSources = selectedFeeds + setOf(feed))
        }
      }
    }
  }

  private fun onCancelSelectionClicked() {
    _state.update { it.copy(selectedSources = emptySet()) }
  }

  private fun onFeedsSortOrderChanged(feedsOrderBy: FeedsOrderBy) {
    _state.update { it.copy(feedsOrderBy = feedsOrderBy) }
  }

  private fun onUngroupClicked() {
    viewModelScope.launch {
      rssRepository.removeFeedIdsFromGroups(
        groupIds = setOf(groupId),
        feedIds = _state.value.selectedSources.map { it.id }
      )

      _state.update { it.copy(selectedSources = emptySet()) }
    }
  }

  private fun onGroupsSelected(groupIds: Set<String>) {
    viewModelScope.launch {
      rssRepository.removeFeedIdsFromGroups(
        groupIds = setOf(groupId),
        feedIds = _state.value.selectedSources.map { it.id }
      )

      rssRepository.addFeedIdsToGroups(
        groupIds = groupIds,
        feedIds = _state.value.selectedSources.map { it.id }
      )

      _state.update { it.copy(selectedSources = emptySet()) }
    }
  }

  private fun onGroupNameChanged(name: String) {
    viewModelScope.launch { rssRepository.updateGroupName(groupId = groupId, name = name) }
  }
}
