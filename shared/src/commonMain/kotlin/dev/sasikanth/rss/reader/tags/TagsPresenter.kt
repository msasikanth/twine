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

package dev.sasikanth.rss.reader.tags

import app.cash.paging.cachedIn
import app.cash.paging.createPager
import app.cash.paging.createPagingConfig
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.benasher44.uuid.Uuid
import dev.sasikanth.rss.reader.repository.TagRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias TagsPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> TagsPresenter

@Inject
class TagsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val tagRepository: TagRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(dispatchersProvider = dispatchersProvider, tagRepository = tagRepository)
    }

  internal val state = presenterInstance.state

  fun dispatch(event: TagsEvent) {
    when (event) {
      is TagsEvent.BackClicked -> goBack()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val tagRepository: TagRepository
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(TagsState.DEFAULT)
    val state: StateFlow<TagsState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TagsState.DEFAULT
      )

    init {
      dispatch(TagsEvent.Init)
    }

    fun dispatch(event: TagsEvent) {
      when (event) {
        TagsEvent.Init -> init()
        is TagsEvent.CreateTag -> createTag(event.label)
        TagsEvent.BackClicked -> {
          // no-op
        }
        is TagsEvent.OnTagNameChanged -> onTagNameChanged(event.tagId, event.label)
        is TagsEvent.OnDeleteTag -> onDeleteTag(event.tagId)
      }
    }

    private fun onDeleteTag(tagId: Uuid) {
      coroutineScope.launch { tagRepository.deleteTag(tagId) }
    }

    private fun onTagNameChanged(tagId: Uuid, label: String) {
      coroutineScope.launch { tagRepository.updatedTag(label, tagId) }
    }

    private fun createTag(label: String) {
      coroutineScope.launch { tagRepository.createTag(label = label) }
    }

    private fun init() {
      val tags =
        createPager(config = createPagingConfig(pageSize = 20, enablePlaceholders = false)) {
            tagRepository.tags()
          }
          .flow
          .cachedIn(coroutineScope)

      _state.update { it.copy(tags = tags) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
