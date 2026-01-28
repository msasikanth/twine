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

package dev.sasikanth.rss.reader.groupselection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import dev.sasikanth.rss.reader.app.Modals
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.OnCreateGroup
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.OnToggleGroupSelection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class GroupSelectionViewModel(
  @Assisted savedStateHandle: SavedStateHandle,
  private val rssRepository: RssRepository,
) : ViewModel() {

  private val selectedGroupIds =
    savedStateHandle
      .get<Modals.GroupSelection>(Modals.GroupSelection::class.simpleName!!)
      ?.selectedGroupIds
      ?.toSet()
      ?: emptySet()

  private val _state =
    MutableStateFlow(GroupSelectionState.DEFAULT.copy(selectedGroups = selectedGroupIds))
  val state: StateFlow<GroupSelectionState>
    get() = _state

  init {
    observeGroups()
  }

  private fun observeGroups() {
    val groups =
      createPager(config = createPagingConfig(pageSize = 20)) { rssRepository.allGroups() }
        .flow
        .cachedIn(viewModelScope)

    _state.update { it.copy(groups = groups) }
  }

  fun dispatch(event: GroupSelectionEvent) {
    when (event) {
      is OnToggleGroupSelection -> onToggleGroupSelection(event.feedGroup)
      is OnCreateGroup -> onCreateGroup(event.name)
    }
  }

  private fun onCreateGroup(name: String) {
    viewModelScope.launch {
      val groupId = rssRepository.createGroup(name)
      _state.update { it.copy(selectedGroups = setOf(groupId)) }
    }
  }

  private fun onToggleGroupSelection(feedGroup: FeedGroup) {
    _state.update {
      val selectedGroups = it.selectedGroups
      if (selectedGroups.contains(feedGroup.id)) {
        it.copy(selectedGroups = emptySet())
      } else {
        it.copy(selectedGroups = setOf(feedGroup.id))
      }
    }
  }
}
