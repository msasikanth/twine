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

package dev.sasikanth.rss.reader.blockedwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benasher44.uuid.Uuid
import dev.sasikanth.rss.reader.data.repository.BlockedWordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class BlockedWordsViewModel(private val blockedWordsRepository: BlockedWordsRepository) :
  ViewModel() {

  private val _state = MutableStateFlow(BlockedWordsState.default())
  val state: StateFlow<BlockedWordsState>
    get() = _state

  init {
    blockedWordsRepository
      .words()
      .onEach { blockedWords -> _state.update { it.copy(blockedWords = blockedWords) } }
      .launchIn(viewModelScope)
  }

  fun dispatch(event: BlockedWordsEvent) {
    when (event) {
      is BlockedWordsEvent.AddBlockedWord -> addBlockedWord(event.word)
      is BlockedWordsEvent.DeleteBlockedWord -> deleteBlockedWord(event.id)
    }
  }

  private fun deleteBlockedWord(id: Uuid) {
    viewModelScope.launch { blockedWordsRepository.removeWord(id) }
  }

  private fun addBlockedWord(word: String) {
    val word = word.trim()
    if (word.isBlank()) return

    viewModelScope.launch { blockedWordsRepository.addWord(word) }
  }
}
