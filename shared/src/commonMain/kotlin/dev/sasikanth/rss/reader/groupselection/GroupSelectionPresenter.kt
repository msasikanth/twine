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

package dev.sasikanth.rss.reader.groupselection

import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.sasikanth.rss.reader.core.model.local.FeedGroup
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.BackClicked
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.OnConfirmGroupSelectionClicked
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.OnCreateGroup
import dev.sasikanth.rss.reader.groupselection.GroupSelectionEvent.OnToggleGroupSelection
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias GroupSelectionPresenterFactory =
  (
    ComponentContext,
    onGroupsSelected: (Set<String>) -> Unit,
    dismiss: () -> Unit,
  ) -> GroupSelectionPresenter

@Inject
class GroupSelectionPresenter(
  dispatchersProvider: DispatchersProvider,
  rssRepository: RssRepository,
  @Assisted private val componentContext: ComponentContext,
  @Assisted private val onGroupsSelected: (groupIds: Set<String>) -> Unit,
  @Assisted private val dismiss: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(dispatchersProvider = dispatchersProvider, rssRepository = rssRepository)
    }

  internal val state: StateFlow<GroupSelectionState> = presenterInstance.state

  fun dispatch(event: GroupSelectionEvent) {
    when (event) {
      BackClicked -> dismiss()
      OnConfirmGroupSelectionClicked -> {
        onGroupsSelected(state.value.selectedGroups)
      }
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    private val dispatchersProvider: DispatchersProvider,
    private val rssRepository: RssRepository,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(GroupSelectionState.DEFAULT)
    val state: StateFlow<GroupSelectionState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GroupSelectionState.DEFAULT
      )

    init {
      observeGroups()
    }

    private fun observeGroups() {
      val groups =
        createPager(config = createPagingConfig(pageSize = 20)) { rssRepository.allGroups() }
          .flow
          .cachedIn(coroutineScope)

      _state.update { it.copy(groups = groups) }
    }

    fun dispatch(event: GroupSelectionEvent) {
      when (event) {
        is OnToggleGroupSelection -> onToggleGroupSelection(event.feedGroup)
        OnConfirmGroupSelectionClicked -> {
          // no-op
        }
        BackClicked -> {
          // no-op
        }
        is OnCreateGroup -> onCreateGroup(event.name)
      }
    }

    private fun onCreateGroup(name: String) {
      coroutineScope.launch { rssRepository.createGroup(name) }
    }

    private fun onToggleGroupSelection(feedGroup: FeedGroup) {
      _state.update {
        val selectedGroups = it.selectedGroups
        if (selectedGroups.contains(feedGroup.id)) {
          it.copy(selectedGroups = selectedGroups - setOf(feedGroup.id))
        } else {
          it.copy(selectedGroups = selectedGroups + setOf(feedGroup.id))
        }
      }
    }
  }
}
