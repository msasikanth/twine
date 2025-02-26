/*
 * Copyright 2025 Sasikanth Miriyampalli
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

package dev.sasikanth.rss.reader.blockedwords

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.benasher44.uuid.Uuid
import dev.sasikanth.rss.reader.data.repository.BlockedWordsRepository
import dev.sasikanth.rss.reader.util.DispatchersProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

internal typealias BlockedWordsPresenterFactory =
  (
    ComponentContext,
    goBack: () -> Unit,
  ) -> BlockedWordsPresenter

@Inject
class BlockedWordsPresenter(
  dispatchersProvider: DispatchersProvider,
  private val blockedWordsRepository: BlockedWordsRepository,
  @Assisted componentContext: ComponentContext,
  @Assisted private val goBack: () -> Unit,
) : ComponentContext by componentContext {

  private val presenterInstance =
    instanceKeeper.getOrCreate {
      PresenterInstance(
        dispatchersProvider = dispatchersProvider,
        blockedWordsRepository = blockedWordsRepository,
      )
    }

  internal val state = presenterInstance.state

  fun dispatch(event: BlockedWordsEvent) {
    when (event) {
      BlockedWordsEvent.BackClicked -> goBack()
      else -> {
        // no-op
      }
    }

    presenterInstance.dispatch(event)
  }

  private class PresenterInstance(
    dispatchersProvider: DispatchersProvider,
    private val blockedWordsRepository: BlockedWordsRepository,
  ) : InstanceKeeper.Instance {

    private val coroutineScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

    private val _state = MutableStateFlow(BlockedWordsState.default())
    val state: StateFlow<BlockedWordsState> =
      _state.stateIn(
        scope = coroutineScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BlockedWordsState.default()
      )

    init {
      blockedWordsRepository
        .words()
        .onEach { blockedWords -> _state.update { it.copy(blockedWords = blockedWords) } }
        .launchIn(coroutineScope)
    }

    fun dispatch(event: BlockedWordsEvent) {
      when (event) {
        is BlockedWordsEvent.AddBlockedWord -> addBlockedWord(event.word)
        is BlockedWordsEvent.DeleteBlockedWord -> deleteBlockedWord(event.id)
        BlockedWordsEvent.BackClicked -> {
          // no-op
        }
      }
    }

    private fun deleteBlockedWord(id: Uuid) {
      coroutineScope.launch { blockedWordsRepository.removeWord(id) }
    }

    private fun addBlockedWord(word: String) {
      coroutineScope.launch { blockedWordsRepository.addWord(word.trim()) }
    }

    override fun onDestroy() {
      coroutineScope.cancel()
    }
  }
}
