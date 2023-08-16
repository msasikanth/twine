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
@file:OptIn(ExperimentalMaterialApi::class)

package dev.sasikanth.rss.reader.home

import androidx.compose.material.ExperimentalMaterialApi
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.doOnCreate
import dev.sasikanth.rss.reader.components.bottomsheet.BottomSheetValue
import dev.sasikanth.rss.reader.database.PostWithMetadata
import dev.sasikanth.rss.reader.repository.RssRepository
import dev.sasikanth.rss.reader.utils.DispatchersProvider
import dev.sasikanth.rss.reader.utils.ObservableSelectedFeed
import io.sentry.kotlin.multiplatform.Sentry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
internal class HomeViewModel(
  lifecycle: Lifecycle,
  dispatchersProvider: DispatchersProvider,
  private val backHandler: BackHandler,
  private val rssRepository: RssRepository,
  private val postsListTransformationUseCase: PostsListTransformationUseCase,
  private val observableSelectedFeed: ObservableSelectedFeed
) : InstanceKeeper.Instance {

  private val viewModelScope = CoroutineScope(SupervisorJob() + dispatchersProvider.main)

  private val _state = MutableStateFlow(HomeState.DEFAULT)
  val state: StateFlow<HomeState> =
    _state.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = HomeState.DEFAULT
    )

  private val _effects = MutableSharedFlow<HomeEffect>(extraBufferCapacity = 10)
  val effects = _effects.asSharedFlow()

  private val backCallback = BackCallback {
    viewModelScope.launch { _effects.emit(HomeEffect.MinimizeSheet) }
  }

  init {
    lifecycle.doOnCreate {
      dispatch(HomeEvent.Init)
      backHandler.register(backCallback)
    }
  }

  fun dispatch(event: HomeEvent) {
    when (event) {
      HomeEvent.Init -> init()
      HomeEvent.OnSwipeToRefresh -> refreshContent()
      is HomeEvent.OnPostClicked -> onPostClicked(event.post)
      is HomeEvent.FeedsSheetStateChanged -> feedsSheetStateChanged(event.feedsSheetState)
      HomeEvent.OnHomeSelected -> onHomeSelected()
      HomeEvent.OnAddFeedClicked -> onAddFeedClicked()
      HomeEvent.OnCancelAddFeedClicked -> onCancelAddFeedClicked()
      is HomeEvent.AddFeed -> addFeed(event.feedLink)
      HomeEvent.OnPrimaryActionClicked -> onPrimaryActionClicked()
    }
  }

  private fun init() {
    observableSelectedFeed.selectedFeed
      .onEach { selectedFeed -> _state.update { it.copy(selectedFeed = selectedFeed) } }
      .flatMapLatest { selectedFeed -> rssRepository.posts(selectedFeedLink = selectedFeed?.link) }
      .map(postsListTransformationUseCase::transform)
      .onEach { (featuredPosts, posts) ->
        _state.update { it.copy(featuredPosts = featuredPosts, posts = posts) }
      }
      .launchIn(viewModelScope)
  }

  private fun onPrimaryActionClicked() {
    if (_state.value.feedsSheetState == BottomSheetValue.Collapsed) {
      dispatch(HomeEvent.OnHomeSelected)
    } else {
      dispatch(HomeEvent.OnAddFeedClicked)
    }
  }

  private fun addFeed(feedLink: String) {
    viewModelScope.launch {
      _state.update { it.copy(feedFetchingState = FeedFetchingState.Loading) }
      try {
        rssRepository.addFeed(feedLink)
      } catch (e: Exception) {
        Sentry.captureException(e) { scope -> scope.setContext("feed_url", feedLink) }
        _effects.emit(HomeEffect.ShowError(e.message))
      } finally {
        _state.update { it.copy(feedFetchingState = FeedFetchingState.Idle) }
      }
    }
  }

  private fun feedsSheetStateChanged(feedsSheetState: BottomSheetValue) {
    backCallback.isEnabled = feedsSheetState == BottomSheetValue.Expanded
    _state.update { it.copy(feedsSheetState = feedsSheetState) }
  }

  private fun onCancelAddFeedClicked() {
    _state.update { it.copy(canShowFeedLinkEntry = false) }
  }

  private fun onAddFeedClicked() {
    _state.update { it.copy(canShowFeedLinkEntry = true) }
  }

  private fun onHomeSelected() {
    viewModelScope.launch { observableSelectedFeed.clearSelection() }
  }

  private fun onPostClicked(post: PostWithMetadata) {
    viewModelScope.launch { _effects.emit(HomeEffect.OpenPost(post)) }
  }

  private fun refreshContent() {
    viewModelScope.launch { updateLoadingState { rssRepository.updateFeeds() } }
  }

  private suspend fun updateLoadingState(action: suspend () -> Unit) {
    _state.update { it.copy(loadingState = HomeLoadingState.Loading) }
    action()
    _state.update { it.copy(loadingState = HomeLoadingState.Idle) }
  }

  override fun onDestroy() {
    viewModelScope.cancel()
    backHandler.unregister(backCallback)
  }
}
