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

package dev.sasikanth.rss.reader.opmlimport

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sasikanth.rss.reader.billing.BillingHandler
import dev.sasikanth.rss.reader.data.opml.OpmlFeed
import dev.sasikanth.rss.reader.data.opml.OpmlFeedGroup
import dev.sasikanth.rss.reader.data.opml.OpmlManager
import dev.sasikanth.rss.reader.data.opml.OpmlResult
import dev.sasikanth.rss.reader.data.opml.OpmlSource
import dev.sasikanth.rss.reader.data.repository.RssRepository
import dev.sasikanth.rss.reader.data.repository.SettingsRepository
import dev.sasikanth.rss.reader.data.sync.SyncCoordinator
import dev.sasikanth.rss.reader.util.DispatchersProvider
import dev.sasikanth.rss.reader.utils.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Stable
@Inject
class OpmlImportViewModel(
  private val opmlManager: OpmlManager,
  private val rssRepository: RssRepository,
  private val settingsRepository: SettingsRepository,
  private val syncCoordinator: SyncCoordinator,
  private val billingHandler: BillingHandler,
  private val dispatchersProvider: DispatchersProvider,
) : ViewModel() {

  private val _state = MutableStateFlow(OpmlImportState.DEFAULT)
  val state: StateFlow<OpmlImportState> = _state.asStateFlow()

  private val _effects = MutableSharedFlow<OpmlImportEffect>()
  val effects = _effects.asSharedFlow()

  private var isCancelled = false

  init {
    opmlManager.result
      .onEach { result ->
        if (result is OpmlResult.InProgress.Importing) {
          _state.update { it.copy(progress = result.progress) }
        }
      }
      .launchIn(viewModelScope)

    pickFile()
  }

  fun dispatch(event: OpmlImportEvent) {
    when (event) {
      is OpmlImportEvent.FeedsSelected -> feedsSelected(event.feeds)
      OpmlImportEvent.SelectionDismissed -> viewModelScope.launch { goBack() }
      OpmlImportEvent.CancelClicked -> cancelImport()
    }
  }

  private fun pickFile() {
    viewModelScope.launch {
      val sources = opmlManager.pickAndDecode()
      if (sources.isNullOrEmpty()) {
        goBack()
        return@launch
      }

      if (billingHandler.isSubscribed()) {
        startImport(sources)
      } else {
        val feeds =
          sources.flatMap { source ->
            when (source) {
              is OpmlFeed -> listOf(source)
              is OpmlFeedGroup -> source.feeds
            }
          }
        _state.update { it.copy(feedsToSelect = feeds, selectionLimit = remainingFreeFeeds()) }
      }
    }
  }

  private fun feedsSelected(feeds: List<OpmlFeed>) {
    viewModelScope.launch {
      _state.update { it.copy(feedsToSelect = null) }

      val feedsToImport = feeds.take(remainingFreeFeeds())
      if (feedsToImport.isEmpty()) {
        goBack()
        return@launch
      }

      startImport(feedsToImport)
    }
  }

  private suspend fun startImport(sources: List<OpmlSource>) {
    isCancelled = false
    _state.update { it.copy(progress = 0) }

    opmlManager.import(sources)

    if (isCancelled) return

    settingsRepository.completeOnboarding()
    syncCoordinator.triggerFeedPush()
    _effects.emit(OpmlImportEffect.ImportComplete)
  }

  private fun cancelImport() {
    viewModelScope.launch(dispatchersProvider.io) {
      isCancelled = true
      opmlManager.cancelAndRollbackImport()
      _effects.emit(OpmlImportEffect.GoBack)
    }
  }

  private suspend fun goBack() {
    _effects.emit(OpmlImportEffect.GoBack)
  }

  private suspend fun remainingFreeFeeds(): Int {
    val feedsCount = rssRepository.numberOfFeeds().first()
    return (Constants.MAX_FREE_FEEDS - feedsCount).coerceAtLeast(0L).toInt()
  }
}
